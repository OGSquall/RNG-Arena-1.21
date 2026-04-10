package net.squall.rngarena.lobby;

import net.fabricmc.fabric.api.event.lifecycle.v1.ServerTickEvents;
import net.minecraft.network.packet.s2c.play.OverlayMessageS2CPacket;
import net.minecraft.network.packet.s2c.play.SubtitleS2CPacket;
import net.minecraft.network.packet.s2c.play.TitleFadeS2CPacket;
import net.minecraft.network.packet.s2c.play.TitleS2CPacket;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.sound.SoundCategory;
import net.minecraft.sound.SoundEvents;
import net.minecraft.text.Text;
import net.minecraft.world.World;
import net.squall.rngarena.world.RNGArenaWorld;
import net.squall.rngarena.world.RNGArenaWorldState;

/**
 * Server-side lobby presentation (title, subtitle, action bar, sound).
 * Title/subtitle once per player per world; action bar is refreshed every second while in the lobby;
 * join sound plays on every network join when entering the lobby.
 */
public final class LobbyWelcome {
	private static final int FADE_IN = 10;
	private static final int STAY = 70;
	private static final int FADE_OUT = 20;
	private static final float SOUND_VOLUME = 0.65F;
	private static final float SOUND_PITCH = 1.15F;
	/** Vanilla action bar fades; refresh often enough to read as always-on. */
	private static final int ACTION_BAR_REFRESH_TICKS = 20;

	private static final Text ACTION_BAR_TEXT = Text.translatable("rng-arena.lobby.welcome.action_bar");

	private LobbyWelcome() {
	}

	public static void register() {
		ServerTickEvents.END_WORLD_TICK.register(world -> {
			if (!(world instanceof ServerWorld serverWorld)) {
				return;
			}
			if (!world.getRegistryKey().equals(World.OVERWORLD)) {
				return;
			}
			MinecraftServer server = serverWorld.getServer();
			if (server == null || server.getTicks() % ACTION_BAR_REFRESH_TICKS != 0) {
				return;
			}
			for (ServerPlayerEntity player : serverWorld.getPlayers()) {
				if (RNGArenaWorld.isLobbyWorld(player)) {
					sendLobbyActionBar(player);
				}
			}
		});
	}

	private static void sendLobbyActionBar(ServerPlayerEntity player) {
		player.networkHandler.sendPacket(new OverlayMessageS2CPacket(ACTION_BAR_TEXT));
	}

	/**
	 * Runs next server tick after the player is in the overworld (e.g. after teleport).
	 *
	 * @param playJoinSound true for a real network join (or first lobby presentation); false for respawn-only.
	 */
	public static void scheduleLobbyPresentation(ServerPlayerEntity player, boolean playJoinSound) {
		MinecraftServer server = player.getServer();
		if (server == null) {
			return;
		}
		server.execute(() -> applyLobbyPresentation(player, playJoinSound));
	}

	private static void applyLobbyPresentation(ServerPlayerEntity player, boolean playJoinSound) {
		MinecraftServer server = player.getServer();
		if (server == null || !RNGArenaWorld.isLobbyWorld(player)) {
			return;
		}

		RNGArenaWorldState state = RNGArenaWorld.persistentState(server);

		if (!state.hasShownLobbyWelcomeText(player.getUuid())) {
			player.networkHandler.sendPacket(new TitleFadeS2CPacket(FADE_IN, STAY, FADE_OUT));
			player.networkHandler.sendPacket(new TitleS2CPacket(Text.translatable("rng-arena.lobby.welcome.title")));
			player.networkHandler.sendPacket(new SubtitleS2CPacket(Text.translatable("rng-arena.lobby.welcome.subtitle")));
			state.markLobbyWelcomeTextShown(player.getUuid());
		}

		if (playJoinSound) {
			player.playSoundToPlayer(SoundEvents.ENTITY_PLAYER_LEVELUP, SoundCategory.PLAYERS, SOUND_VOLUME, SOUND_PITCH);
		}

		sendLobbyActionBar(player);
	}

	/** After the lobby structure is placed, catch players who joined before the lobby existed. */
	public static void tryWelcomeAllInLobby(MinecraftServer server) {
		ServerPlayerEntity[] players = server.getPlayerManager().getPlayerList().toArray(ServerPlayerEntity[]::new);
		for (ServerPlayerEntity player : players) {
			scheduleLobbyPresentation(player, true);
		}
	}
}
