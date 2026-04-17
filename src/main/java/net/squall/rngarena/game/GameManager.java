package net.squall.rngarena.game;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.IdentityHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.function.Consumer;

import net.fabricmc.fabric.api.event.lifecycle.v1.ServerLifecycleEvents;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerTickEvents;
import net.minecraft.network.packet.s2c.play.SubtitleS2CPacket;
import net.minecraft.network.packet.s2c.play.TitleFadeS2CPacket;
import net.minecraft.network.packet.s2c.play.TitleS2CPacket;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.text.Text;
import net.minecraft.util.math.BlockPos;
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
	private static final int TITLE_FADE_IN = 5;
	private static final int TITLE_STAY = 15;
	private static final int TITLE_FADE_OUT = 5;

	private static final Map<MinecraftServer, GameManager> INSTANCES = new IdentityHashMap<>();

	private final MinecraftServer server;

	private GameState state = GameState.LOBBY;
	private boolean roundRunning;
	private final Set<UUID> activePlayerIds = new HashSet<>();
	private final Set<UUID> alivePlayerIds = new HashSet<>();
	private int countdownTicksRemaining;

	private GameManager(MinecraftServer server) {
		this.server = server;
	}

	public static void register() {
		ServerLifecycleEvents.SERVER_STOPPING.register(INSTANCES::remove);
		ServerTickEvents.END_SERVER_TICK.register(server -> {
			GameManager manager = INSTANCES.get(server);
			if (manager != null) {
				manager.tick();
			}
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
			player.teleport(overworld, x, y, z, 0.0F, 0.0F);
		}
		giveRandomWeaponsToPlayers(overworld, orderedRoundPlayers);

		this.state = GameState.IN_GAME;
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

	public void endGame() {
		this.state = GameState.END;
		this.roundRunning = false;
	}

	/**
	 * Clears participants and returns to lobby (e.g. after a match ends in a later phase).
	 */
	public void resetToLobby() {
		this.state = GameState.LOBBY;
		this.roundRunning = false;
		this.activePlayerIds.clear();
		this.alivePlayerIds.clear();
		this.countdownTicksRemaining = 0;
	}

	private void tick() {
		if (this.state != GameState.COUNTDOWN) {
			return;
		}
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
		ServerWorld overworld = this.server.getOverworld();
		if (overworld == null) {
			return;
		}
		for (ServerPlayerEntity player : overworld.getPlayers()) {
			if (!RNGArenaWorld.isLobbyWorld(player)) {
				continue;
			}
			UUID id = player.getUuid();
			this.activePlayerIds.add(id);
			this.alivePlayerIds.add(id);
		}
	}

	private void forEachOnlineParticipant(Set<UUID> ids, Consumer<ServerPlayerEntity> action) {
		for (UUID id : ids) {
			ServerPlayerEntity player = this.server.getPlayerManager().getPlayer(id);
			if (player != null) {
				action.accept(player);
			}
		}
	}
}
