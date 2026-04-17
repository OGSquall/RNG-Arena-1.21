package net.squall.rngarena;

import net.fabricmc.api.ModInitializer;
import net.squall.rngarena.arena.ArenaManager;
import net.squall.rngarena.command.ReturnToLobbyCommand;
import net.squall.rngarena.command.StartArenaCommand;
import net.squall.rngarena.game.GameManager;
import net.squall.rngarena.registry.CactusStaffEvents;
import net.squall.rngarena.registry.ModEffects;
import net.squall.rngarena.registry.ModItems;
import net.squall.rngarena.util.CactusSnareManager;
import net.squall.rngarena.util.CactusSnareVisualManager;
import net.squall.rngarena.world.RNGArenaWorld;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class RNGArena implements ModInitializer {
	public static final String MOD_ID = "rng-arena";
	public static final Logger LOGGER = LoggerFactory.getLogger(MOD_ID);

	@Override
	public void onInitialize() {
		ModEffects.register();
		ModItems.register();
		CactusStaffEvents.register();
		CactusSnareManager.register();
		CactusSnareVisualManager.register();
		GameManager.register();
		ArenaManager.register();
		StartArenaCommand.register();
		ReturnToLobbyCommand.register();
		RNGArenaWorld.initialize();
	}
}