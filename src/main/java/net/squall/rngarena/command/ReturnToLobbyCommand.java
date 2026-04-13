package net.squall.rngarena.command;

import java.util.HashSet;
import java.util.Set;
import java.util.UUID;

import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;

import net.fabricmc.fabric.api.command.v2.CommandRegistrationCallback;
import net.minecraft.server.command.CommandManager;
import net.minecraft.server.command.ServerCommandSource;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.text.Text;
import net.squall.rngarena.game.GameManager;
import net.squall.rngarena.world.RNGArenaWorld;

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
		ServerPlayerEntity executor = source.getPlayerOrThrow();
		GameManager manager = GameManager.get(source.getServer());

		Set<UUID> toTeleport = new HashSet<>(manager.getActivePlayerIds());
		toTeleport.add(executor.getUuid());

		manager.resetToLobby();

		for (UUID id : toTeleport) {
			ServerPlayerEntity player = source.getServer().getPlayerManager().getPlayer(id);
			if (player != null) {
				RNGArenaWorld.teleportToLobbySpawn(player);
			}
		}

		source.sendFeedback(() -> Text.translatable("rng-arena.command.returntolobby.success"), false);
		return 1;
	}
}
