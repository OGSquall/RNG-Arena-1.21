package net.squall.rngarena.command;

import com.mojang.brigadier.context.CommandContext;

import net.fabricmc.fabric.api.command.v2.CommandRegistrationCallback;
import net.minecraft.server.command.CommandManager;
import net.minecraft.server.command.ServerCommandSource;
import net.minecraft.text.Text;
import net.squall.rngarena.game.GameManager;
import net.squall.rngarena.game.GameState;

public final class StartArenaCommand {
	private StartArenaCommand() {
	}

	public static void register() {
		CommandRegistrationCallback.EVENT.register((dispatcher, registryAccess, environment) -> dispatcher.register(
			CommandManager.literal("startarena").executes(StartArenaCommand::execute)
		));
	}

	private static int execute(CommandContext<ServerCommandSource> context) {
		ServerCommandSource source = context.getSource();
		GameManager manager = GameManager.get(source.getServer());
		GameState state = manager.getState();
		if (state == GameState.COUNTDOWN || state == GameState.IN_GAME) {
			source.sendError(Text.translatable("rng-arena.command.startarena.already_running"));
			return 0;
		}
		if (!manager.startCountdown()) {
			source.sendError(Text.translatable(
				"rng-arena.command.startarena.not_enough_players",
				GameManager.MIN_PLAYERS_TO_START
			));
			return 0;
		}
		source.sendFeedback(() -> Text.translatable("rng-arena.command.startarena.success"), true);
		return 1;
	}
}
