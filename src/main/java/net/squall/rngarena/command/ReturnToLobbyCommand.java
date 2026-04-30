package net.squall.rngarena.command;

import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;

import net.fabricmc.fabric.api.command.v2.CommandRegistrationCallback;
import net.minecraft.server.command.CommandManager;
import net.minecraft.server.command.ServerCommandSource;
import net.minecraft.text.Text;
import net.squall.rngarena.game.GameManager;

/**
 * Temporary debug command for development. Remove or gate behind permissions when round-end flow exists.
 */
public final class ReturnToLobbyCommand {
	private ReturnToLobbyCommand() {
	}

	public static void register() {
		CommandRegistrationCallback.EVENT.register((dispatcher, registryAccess, environment) -> dispatcher.register(
			CommandManager.literal("returntolobby")
				.requires(ServerCommandSource::isExecutedByPlayer)
				.executes(ReturnToLobbyCommand::execute)
		));
	}

	private static int execute(CommandContext<ServerCommandSource> context) throws CommandSyntaxException {
		ServerCommandSource source = context.getSource();
		source.getPlayerOrThrow();
		GameManager manager = GameManager.get(source.getServer());

		manager.resetToLobby();

		source.sendFeedback(() -> Text.translatable("rng-arena.command.returntolobby.success"), false);
		return 1;
	}
}
