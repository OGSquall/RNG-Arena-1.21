package net.squall.rngarena.world;

import net.fabricmc.fabric.api.event.lifecycle.v1.ServerLifecycleEvents;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.math.BlockPos;
import net.squall.rngarena.RNGArena;

public final class RNGArenaWorld {
	private static final int PLATFORM_RADIUS = 2;
	private static final BlockPos SAFE_SPAWN_POS = new BlockPos(0, 64, 0);
	private static final String STATE_KEY = "rng_arena_world_state";

	private RNGArenaWorld() {
	}

	public static void initialize() {
		ServerLifecycleEvents.SERVER_STARTED.register(server -> {
			ServerWorld overworld = server.getOverworld();
			if (overworld == null) {
				return;
			}

			buildSpawnPlatformIfNeeded(server, overworld);
		});
	}

	private static void buildSpawnPlatformIfNeeded(MinecraftServer server, ServerWorld world) {
		RNGArenaWorldState worldState = server.getOverworld().getPersistentStateManager().getOrCreate(
			new net.minecraft.world.PersistentState.Type<>(RNGArenaWorldState::create, RNGArenaWorldState::fromNbt, null),
			STATE_KEY
		);
		if (worldState.isSpawnPlatformPlaced()) {
			return;
		}

		world.setSpawnPos(SAFE_SPAWN_POS, 0.0F);
		BlockPos spawnPos = world.getSpawnPos();
		int platformY = spawnPos.getY() - 1;
		BlockState platformBlock = Blocks.STONE.getDefaultState();

		for (int x = -PLATFORM_RADIUS; x <= PLATFORM_RADIUS; x++) {
			for (int z = -PLATFORM_RADIUS; z <= PLATFORM_RADIUS; z++) {
				BlockPos target = new BlockPos(spawnPos.getX() + x, platformY, spawnPos.getZ() + z);
				world.setBlockState(target, platformBlock, Block.NOTIFY_ALL);
			}
		}

		worldState.setSpawnPlatformPlaced(true);
		RNGArena.LOGGER.info("Ensured RNG Arena spawn platform at {}", spawnPos);
	}
}
