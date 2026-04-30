package net.squall.rngarena.game;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.IdentityHashMap;
import java.util.List;
import java.util.Map;
import java.util.HashMap;
import java.util.Set;
import java.util.UUID;
import java.util.function.Consumer;

import net.fabricmc.fabric.api.entity.event.v1.ServerLivingEntityEvents;
import net.fabricmc.fabric.api.entity.event.v1.ServerPlayerEvents;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerLifecycleEvents;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerTickEvents;
import net.minecraft.network.packet.s2c.play.SubtitleS2CPacket;
import net.minecraft.network.packet.s2c.play.TitleFadeS2CPacket;
import net.minecraft.network.packet.s2c.play.TitleS2CPacket;
import net.minecraft.entity.ItemEntity;
import net.minecraft.entity.damage.DamageTypes;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.text.Text;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec3i;
import net.minecraft.world.GameMode;
import net.minecraft.world.World;
import net.squall.rngarena.RNGArena;
import net.squall.rngarena.arena.Arena;
import net.squall.rngarena.arena.ArenaManager;
import net.squall.rngarena.registry.RNGWeaponRegistry;
import net.squall.rngarena.world.RNGArenaWorld;

/**
 * Server-side controller for match flow: lobby, countdown, in-game, and end.
 * One instance per {@link MinecraftServer}.
 */
public final class GameManager {
	/** Minimum overworld lobby players required to start a countdown (raise for real matches). */
	public static final int MIN_PLAYERS_TO_START = 1;
	private static final int COUNTDOWN_SECONDS = 5;
	private static final int ROUND_END_DELAY_TICKS = 3 * 20;
	private static final int TITLE_FADE_IN = 5;
	private static final int TITLE_STAY = 15;
	private static final int TITLE_FADE_OUT = 5;

	private static final Map<MinecraftServer, GameManager> INSTANCES = new IdentityHashMap<>();

	private final MinecraftServer server;

	private GameState state = GameState.LOBBY;
	private boolean roundRunning;
	private boolean endingRound;
	private final Set<UUID> activePlayerIds = new HashSet<>();
	private final Set<UUID> alivePlayerIds = new HashSet<>();
	private final Set<UUID> teamAlphaAliveIds = new HashSet<>();
	private final Set<UUID> teamBetaAliveIds = new HashSet<>();
	private final Map<UUID, TeamSide> playerTeams = new HashMap<>();
	private final Set<UUID> eliminatedPlayerIds = new HashSet<>();
	private final Map<UUID, BlockPos> deathPositions = new HashMap<>();
	private int countdownTicksRemaining;
	private int endTicksRemaining;

	private GameManager(MinecraftServer server) {
		this.server = server;
	}

	public static void register() {
		ServerLifecycleEvents.SERVER_STOPPING.register(INSTANCES::remove);
		ServerLivingEntityEvents.AFTER_DEATH.register((entity, damageSource) -> {
			if (!(entity instanceof ServerPlayerEntity player)) {
				return;
			}
			MinecraftServer server = player.getServer();
			if (server == null) {
				return;
			}
			GameManager.get(server).handlePlayerDeath(player);
		});
		ServerTickEvents.END_SERVER_TICK.register(server -> {
			GameManager manager = INSTANCES.get(server);
			if (manager != null) {
				manager.tick();
			}
		});
		ServerPlayerEvents.AFTER_RESPAWN.register((oldPlayer, newPlayer, alive) -> {
			MinecraftServer server = newPlayer.getServer();
			if (server == null) {
				return;
			}
			GameManager.get(server).handlePlayerRespawn(newPlayer);
		});
		ServerLivingEntityEvents.ALLOW_DAMAGE.register((entity, source, amount) -> {
			if (!(entity instanceof ServerPlayerEntity targetPlayer)) {
				return true;
			}
			MinecraftServer server = targetPlayer.getServer();
			if (server == null) {
				return true;
			}
			GameManager manager = GameManager.get(server);
			if (manager.state != GameState.LOBBY || !RNGArenaWorld.isLobbyWorld(targetPlayer)) {
				return true;
			}
			if (source.isOf(DamageTypes.FALL)) {
				return false;
			}
			if (source.getAttacker() instanceof ServerPlayerEntity attackingPlayer && RNGArenaWorld.isLobbyWorld(attackingPlayer)) {
				return false;
			}
			return true;
		});
	}

	public static GameManager get(MinecraftServer server) {
		synchronized (INSTANCES) {
			return INSTANCES.computeIfAbsent(server, GameManager::new);
		}
	}

	public GameState getState() {
		return this.state;
	}

	public boolean isRoundRunning() {
		return this.roundRunning;
	}

	public Set<UUID> getActivePlayerIds() {
		return Collections.unmodifiableSet(this.activePlayerIds);
	}

	public Set<UUID> getAlivePlayerIds() {
		return Collections.unmodifiableSet(this.alivePlayerIds);
	}

	public boolean shouldHandleInGameRespawn(ServerPlayerEntity player) {
		return (this.state == GameState.IN_GAME || this.state == GameState.END) && this.eliminatedPlayerIds.contains(player.getUuid());
	}

	/**
	 * Begins the pre-match countdown if not already in countdown or in-game,
	 * and if at least {@link #MIN_PLAYERS_TO_START} lobby participants exist.
	 *
	 * @return true if countdown started
	 */
	public boolean startCountdown() {
		if (this.state == GameState.COUNTDOWN || this.state == GameState.IN_GAME) {
			return false;
		}
		refreshParticipantSets();
		if (this.activePlayerIds.size() < MIN_PLAYERS_TO_START) {
			return false;
		}
		this.state = GameState.COUNTDOWN;
		this.roundRunning = true;
		this.countdownTicksRemaining = COUNTDOWN_SECONDS * 20;
		broadcastCountdownSecond(COUNTDOWN_SECONDS);
		return true;
	}

	public void startGame() {
		ServerWorld overworld = this.server.getOverworld();
		if (overworld == null || !overworld.getRegistryKey().equals(World.OVERWORLD)) {
			RNGArena.LOGGER.error("startGame: overworld missing or unexpected; returning to lobby");
			this.state = GameState.LOBBY;
			this.roundRunning = false;
			return;
		}

		ArenaManager arenaManager = ArenaManager.get(this.server);
		arenaManager.attachOrPlace(this.server, overworld);
		Arena arena = arenaManager.getActiveTestArena();
		if (!arena.hasOrigin()) {
			RNGArena.LOGGER.error("startGame: arena structure not placed; returning to lobby");
			this.state = GameState.LOBBY;
			this.roundRunning = false;
			return;
		}

		List<ServerPlayerEntity> orderedRoundPlayers = getOrderedRoundPlayers(overworld);
		for (int i = 0; i < orderedRoundPlayers.size(); i++) {
			ServerPlayerEntity player = orderedRoundPlayers.get(i);
			BlockPos spawn = arena.getSpawnBlockPos(i);
			double x = spawn.getX() + 0.5;
			double y = spawn.getY();
			double z = spawn.getZ() + 0.5;
			float spawnYaw = calculateSpawnYawFromOffsets(arena, i);
			player.changeGameMode(GameMode.SURVIVAL);
			player.setHealth(player.getMaxHealth());
			player.getHungerManager().setFoodLevel(20);
			player.getHungerManager().setSaturationLevel(5.0F);
			player.clearStatusEffects();
			player.teleport(overworld, x, y, z, spawnYaw, 0.0F);
			player.setYaw(spawnYaw);
			player.setHeadYaw(spawnYaw);
			player.setBodyYaw(spawnYaw);
			player.setPitch(0.0F);
		}
		giveRandomWeaponsToPlayers(overworld, orderedRoundPlayers);
		rebuildRoundPlayerSets(orderedRoundPlayers);

		this.state = GameState.IN_GAME;
		this.endingRound = false;
		this.endTicksRemaining = 0;
		forEachOnlineParticipant(this.activePlayerIds, player -> {
			player.networkHandler.sendPacket(new TitleFadeS2CPacket(TITLE_FADE_IN, TITLE_STAY, TITLE_FADE_OUT));
			player.networkHandler.sendPacket(new TitleS2CPacket(Text.translatable("rng-arena.game.go.title")));
			player.networkHandler.sendPacket(new SubtitleS2CPacket(Text.translatable("rng-arena.game.go.subtitle")));
		});
		broadcastToParticipants(Text.translatable("rng-arena.game.in_game.chat"));
	}

	private void giveRandomWeaponsToPlayers(ServerWorld world, List<ServerPlayerEntity> players) {
		for (ServerPlayerEntity player : players) {
			player.getInventory().clear();
			player.getInventory().selectedSlot = 0;

			Item randomWeapon = RNGWeaponRegistry.getRandomWeapon(world.getRandom());
			player.getInventory().setStack(0, new ItemStack(randomWeapon));
			player.currentScreenHandler.sendContentUpdates();
			player.sendMessage(Text.literal("You received: ").append(randomWeapon.getName()), true);
		}
	}

	private List<ServerPlayerEntity> getOrderedRoundPlayers(ServerWorld world) {
		List<ServerPlayerEntity> players = new ArrayList<>();
		List<UUID> orderedParticipants = new ArrayList<>(this.activePlayerIds);
		Collections.sort(orderedParticipants);
		for (UUID playerId : orderedParticipants) {
			ServerPlayerEntity player = this.server.getPlayerManager().getPlayer(playerId);
			if (player == null || !player.getWorld().getRegistryKey().equals(world.getRegistryKey()) || player.isSpectator()) {
				continue;
			}
			players.add(player);
		}
		return players;
	}

	public void handlePlayerDeath(ServerPlayerEntity player) {
		if (this.state != GameState.IN_GAME || this.endingRound) {
			return;
		}
		UUID deadPlayerId = player.getUuid();
		if (!this.alivePlayerIds.remove(deadPlayerId)) {
			return;
		}
		TeamSide team = this.playerTeams.get(deadPlayerId);
		if (team == TeamSide.ALPHA) {
			this.teamAlphaAliveIds.remove(deadPlayerId);
		} else if (team == TeamSide.BETA) {
			this.teamBetaAliveIds.remove(deadPlayerId);
		}
		this.eliminatedPlayerIds.add(deadPlayerId);
		this.deathPositions.put(deadPlayerId, player.getBlockPos().toImmutable());
		player.sendMessage(Text.translatable("rng-arena.game.eliminated"), false);
		checkWinCondition();
	}

	public void handlePlayerRespawn(ServerPlayerEntity player) {
		if (this.state != GameState.IN_GAME && this.state != GameState.END) {
			return;
		}
		UUID playerId = player.getUuid();
		if (!this.eliminatedPlayerIds.contains(playerId)) {
			return;
		}
		ServerWorld overworld = this.server.getOverworld();
		if (overworld == null) {
			return;
		}
		BlockPos deathPos = this.deathPositions.get(playerId);
		player.changeGameMode(GameMode.SPECTATOR);
		if (deathPos != null) {
			player.teleport(overworld, deathPos.getX() + 0.5, deathPos.getY(), deathPos.getZ() + 0.5, 0.0F, 0.0F);
		}
	}

	public void checkWinCondition() {
		if (this.state != GameState.IN_GAME || this.endingRound) {
			return;
		}
		boolean alphaAlive = !this.teamAlphaAliveIds.isEmpty();
		boolean betaAlive = !this.teamBetaAliveIds.isEmpty();
		if (alphaAlive && betaAlive) {
			return;
		}
		if (!alphaAlive && !betaAlive) {
			endGame(null);
			return;
		}
		endGame(alphaAlive ? TeamSide.ALPHA : TeamSide.BETA);
	}

	private void endGame(TeamSide winnerTeam) {
		if (this.endingRound) {
			return;
		}
		this.endingRound = true;
		this.state = GameState.END;
		this.roundRunning = false;
		this.endTicksRemaining = ROUND_END_DELAY_TICKS;

		forEachOnlineParticipant(this.activePlayerIds, player -> {
			player.networkHandler.sendPacket(new TitleFadeS2CPacket(TITLE_FADE_IN, ROUND_END_DELAY_TICKS, TITLE_FADE_OUT));
			if (winnerTeam != null) {
				player.networkHandler.sendPacket(new TitleS2CPacket(Text.translatable(winnerTeam.titleKey())));
				player.networkHandler.sendPacket(new SubtitleS2CPacket(Text.translatable("rng-arena.game.returning_to_lobby")));
			} else {
				player.networkHandler.sendPacket(new TitleS2CPacket(Text.translatable("rng-arena.game.draw.title")));
				player.networkHandler.sendPacket(new SubtitleS2CPacket(Text.translatable("rng-arena.game.returning_to_lobby")));
			}
		});

		if (winnerTeam != null) {
			broadcastToParticipants(Text.translatable("rng-arena.game.team_winner", Text.translatable(winnerTeam.nameKey())));
		} else {
			broadcastToParticipants(Text.translatable("rng-arena.game.draw"));
		}
	}

	/**
	 * Clears participants and returns to lobby (e.g. after a match ends in a later phase).
	 */
	public void resetToLobby() {
		resetToLobby(new HashSet<>(this.activePlayerIds));
	}

	private void resetToLobby(Set<UUID> playersToReturn) {
		ServerWorld overworld = this.server.getOverworld();
		if (overworld != null) {
			clearDroppedItems(overworld);
		}

		for (UUID id : playersToReturn) {
			ServerPlayerEntity player = this.server.getPlayerManager().getPlayer(id);
			if (player == null) {
				continue;
			}
			resetPlayerForLobby(player);
			RNGArenaWorld.teleportToLobbySpawn(player);
		}

		this.state = GameState.LOBBY;
		this.roundRunning = false;
		this.endingRound = false;
		this.activePlayerIds.clear();
		this.alivePlayerIds.clear();
		this.teamAlphaAliveIds.clear();
		this.teamBetaAliveIds.clear();
		this.playerTeams.clear();
		this.eliminatedPlayerIds.clear();
		this.deathPositions.clear();
		this.countdownTicksRemaining = 0;
		this.endTicksRemaining = 0;
	}

	private void clearDroppedItems(ServerWorld world) {
		int removedCount = 0;
		for (var entity : world.iterateEntities()) {
			if (entity instanceof ItemEntity itemEntity) {
				itemEntity.discard();
				removedCount++;
			}
		}
		if (removedCount > 0) {
			RNGArena.LOGGER.info("Removed {} dropped item entities during round reset.", removedCount);
		}
	}

	private float calculateSpawnYawFromOffsets(Arena arena, int spawnIndex) {
		BlockPos arenaOrigin = arena.getOrigin();
		Vec3i spawnOffset = arena.getRelativeSpawnOffsets().get(Math.floorMod(spawnIndex, arena.getSpawnCount()));
		Vec3i targetOffset = arena.getRelativeLookTargetOffset();

		double spawnX = arenaOrigin.getX() + spawnOffset.getX() + 0.5;
		double spawnZ = arenaOrigin.getZ() + spawnOffset.getZ() + 0.5;
		double targetX = arenaOrigin.getX() + targetOffset.getX() + 0.5;
		double targetZ = arenaOrigin.getZ() + targetOffset.getZ() + 0.5;

		double dx = targetX - spawnX;
		double dz = targetZ - spawnZ;
		return (float) (Math.toDegrees(Math.atan2(dz, dx)) - 90.0D);
	}

	private void tick() {
		if (this.state == GameState.COUNTDOWN) {
			tickCountdown();
			return;
		}
		if (this.state == GameState.END && this.endingRound) {
			tickRoundEndDelay();
		}
	}

	private void tickCountdown() {
		this.countdownTicksRemaining--;
		if (this.countdownTicksRemaining <= 0) {
			startGame();
			return;
		}
		if (this.countdownTicksRemaining % 20 == 0) {
			int seconds = this.countdownTicksRemaining / 20;
			broadcastCountdownSecond(seconds);
		}
	}

	private void tickRoundEndDelay() {
		this.endTicksRemaining--;
		if (this.endTicksRemaining <= 0) {
			resetToLobby(new HashSet<>(this.activePlayerIds));
			return;
		}
	}

	private void broadcastCountdownSecond(int seconds) {
		Text chat = Text.translatable("rng-arena.game.countdown.chat", seconds);
		broadcastToParticipants(chat);
		forEachOnlineParticipant(this.activePlayerIds, player -> {
			player.networkHandler.sendPacket(new TitleFadeS2CPacket(TITLE_FADE_IN, TITLE_STAY, TITLE_FADE_OUT));
			player.networkHandler.sendPacket(new TitleS2CPacket(Text.translatable("rng-arena.game.countdown.title", seconds)));
			player.networkHandler.sendPacket(new SubtitleS2CPacket(Text.translatable("rng-arena.game.countdown.subtitle")));
		});
	}

	private void broadcastToParticipants(Text message) {
		forEachOnlineParticipant(this.activePlayerIds, player -> player.sendMessage(message, false));
	}

	private void refreshParticipantSets() {
		this.activePlayerIds.clear();
		this.alivePlayerIds.clear();
		this.teamAlphaAliveIds.clear();
		this.teamBetaAliveIds.clear();
		this.playerTeams.clear();
		this.eliminatedPlayerIds.clear();
		this.deathPositions.clear();
		ServerWorld overworld = this.server.getOverworld();
		if (overworld == null) {
			return;
		}
		int teamAssignmentIndex = 0;
		for (ServerPlayerEntity player : overworld.getPlayers()) {
			if (!RNGArenaWorld.isLobbyWorld(player)) {
				continue;
			}
			UUID id = player.getUuid();
			this.activePlayerIds.add(id);
			this.alivePlayerIds.add(id);
			TeamSide team = (teamAssignmentIndex++ % 2 == 0) ? TeamSide.ALPHA : TeamSide.BETA;
			this.playerTeams.put(id, team);
			if (team == TeamSide.ALPHA) {
				this.teamAlphaAliveIds.add(id);
			} else {
				this.teamBetaAliveIds.add(id);
			}
		}
	}

	private void rebuildRoundPlayerSets(List<ServerPlayerEntity> players) {
		this.activePlayerIds.clear();
		this.alivePlayerIds.clear();
		this.teamAlphaAliveIds.clear();
		this.teamBetaAliveIds.clear();
		this.playerTeams.clear();
		this.eliminatedPlayerIds.clear();
		this.deathPositions.clear();
		for (int i = 0; i < players.size(); i++) {
			ServerPlayerEntity player = players.get(i);
			UUID id = player.getUuid();
			TeamSide team = (i % 2 == 0) ? TeamSide.ALPHA : TeamSide.BETA;
			this.activePlayerIds.add(id);
			this.alivePlayerIds.add(id);
			this.playerTeams.put(id, team);
			if (team == TeamSide.ALPHA) {
				this.teamAlphaAliveIds.add(id);
			} else {
				this.teamBetaAliveIds.add(id);
			}
		}
	}

	private void resetPlayerForLobby(ServerPlayerEntity player) {
		player.changeGameMode(GameMode.SURVIVAL);
		player.getInventory().clear();
		player.getHungerManager().setFoodLevel(20);
		player.getHungerManager().setSaturationLevel(5.0F);
		player.clearStatusEffects();
		player.extinguish();
		player.setHealth(player.getMaxHealth());
		player.currentScreenHandler.sendContentUpdates();
	}

	private void forEachOnlineParticipant(Set<UUID> ids, Consumer<ServerPlayerEntity> action) {
		for (UUID id : ids) {
			ServerPlayerEntity player = this.server.getPlayerManager().getPlayer(id);
			if (player != null) {
				action.accept(player);
			}
		}
	}

	private enum TeamSide {
		ALPHA("rng-arena.game.team.alpha", "rng-arena.game.team.alpha.wins"),
		BETA("rng-arena.game.team.beta", "rng-arena.game.team.beta.wins");

		private final String nameKey;
		private final String titleKey;

		TeamSide(String nameKey, String titleKey) {
			this.nameKey = nameKey;
			this.titleKey = titleKey;
		}

		public String nameKey() {
			return this.nameKey;
		}

		public String titleKey() {
			return this.titleKey;
		}
	}
}
