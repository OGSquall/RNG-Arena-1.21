package net.squall.rngarena.world;

import java.io.IOException;
import java.io.InputStream;
import java.util.Optional;

import net.fabricmc.fabric.api.entity.event.v1.ServerPlayerEvents;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerLifecycleEvents;
import net.fabricmc.fabric.api.networking.v1.ServerPlayConnectionEvents;
import net.fabricmc.loader.api.FabricLoader;
import net.minecraft.block.Block;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.nbt.NbtIo;
import net.minecraft.nbt.NbtSizeTracker;
import net.minecraft.registry.Registries;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.structure.StructurePlacementData;
import net.minecraft.structure.StructureTemplate;
import net.minecraft.util.BlockMirror;
import net.minecraft.util.BlockRotation;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.ChunkPos;
import net.minecraft.util.math.Vec3i;
import net.minecraft.util.math.random.Random;
import net.minecraft.world.World;
import net.minecraft.world.chunk.ChunkStatus;
import net.squall.rngarena.RNGArena;
import net.squall.rngarena.arena.ArenaManager;
import net.squall.rngarena.lobby.LobbyWelcome;

public final class RNGArenaWorld {
	private static final BlockPos LOBBY_ORIGIN = new BlockPos(0, 64, 0);
	/** World spawn + join teleport target (block coordinates). */
	private static final BlockPos PLAYER_SPAWN = new BlockPos(13, 68, 13);
	private static final double PLAYER_SPAWN_X = PLAYER_SPAWN.getX() + 0.5;
	private static final double PLAYER_SPAWN_Y = PLAYER_SPAWN.getY();
	private static final double PLAYER_SPAWN_Z = PLAYER_SPAWN.getZ() + 0.5;
	private static final Identifier LOBBY_STRUCTURE = Identifier.of(RNGArena.MOD_ID, "lobby");
	/** Path inside the mod JAR / resources root (Fabric ModContainer.findPath). */
	private static final String LOBBY_NBT_RESOURCE_PATH = "data/" + RNGArena.MOD_ID + "/structures/lobby.nbt";
	private static final String STATE_KEY = "rng_arena_world_state";

	private RNGArenaWorld() {
	}

	/** True when this save has an RNG Arena lobby (structure placed); used for lobby-only UX. */
	public static boolean isLobbyWorld(ServerPlayerEntity player) {
		MinecraftServer server = player.getServer();
		if (server == null || !player.getWorld().getRegistryKey().equals(World.OVERWORLD)) {
			return false;
		}
		return persistentState(server).isSpawnLobbyPlaced();
	}

	public static RNGArenaWorldState persistentState(MinecraftServer server) {
		return server.getOverworld().getPersistentStateManager().getOrCreate(
			new net.minecraft.world.PersistentState.Type<>(RNGArenaWorldState::create, RNGArenaWorldState::fromNbt, null),
			STATE_KEY
		);
	}

	/**
	 * Teleports to the configured lobby player spawn (same as join/respawn), then refreshes lobby presentation.
	 */
	public static void teleportToLobbySpawn(ServerPlayerEntity player) {
		MinecraftServer server = player.getServer();
		if (server == null) {
			return;
		}
		ServerWorld overworld = server.getOverworld();
		if (overworld == null) {
			return;
		}
		player.teleport(overworld, PLAYER_SPAWN_X, PLAYER_SPAWN_Y, PLAYER_SPAWN_Z, 0.0F, 0.0F);
		LobbyWelcome.scheduleLobbyPresentation(player, false);
	}

	public static void initialize() {
		LobbyWelcome.register();

		ServerLifecycleEvents.SERVER_STARTED.register(server -> {
			server.execute(() -> {
				ServerWorld overworld = server.getOverworld();
				if (overworld != null) {
					overworld.setSpawnPos(PLAYER_SPAWN, 0.0F);
					placeLobbyIfNeeded(server, overworld);
					ArenaManager.get(server).attachOrPlace(server, overworld);
				}
			});
		});

		ServerPlayConnectionEvents.JOIN.register((handler, sender, server) -> {
			ServerWorld overworld = server.getOverworld();
			if (overworld == null) {
				return;
			}
			ServerPlayerEntity player = handler.player;
			if (player.getWorld() != overworld) {
				return;
			}
			player.teleport(overworld, PLAYER_SPAWN_X, PLAYER_SPAWN_Y, PLAYER_SPAWN_Z, 0.0F, 0.0F);
			LobbyWelcome.scheduleLobbyPresentation(player, true);
		});

		ServerPlayerEvents.AFTER_RESPAWN.register((oldPlayer, newPlayer, alive) -> {
			MinecraftServer srv = newPlayer.getServer();
			if (srv == null) {
				return;
			}
			ServerWorld overworld = srv.getOverworld();
			if (overworld == null || newPlayer.getWorld() != overworld) {
				return;
			}
			newPlayer.teleport(overworld, PLAYER_SPAWN_X, PLAYER_SPAWN_Y, PLAYER_SPAWN_Z, 0.0F, 0.0F);
			LobbyWelcome.scheduleLobbyPresentation(newPlayer, false);
		});
	}

	private static Optional<StructureTemplate> loadLobbyTemplate(ServerWorld world) {
		Optional<StructureTemplate> fromManager = world.getStructureTemplateManager().getTemplate(LOBBY_STRUCTURE);
		if (fromManager.isPresent()) {
			return fromManager;
		}

		Optional<InputStream> streamOpt = FabricLoader.getInstance()
			.getModContainer(RNGArena.MOD_ID)
			.flatMap(c -> c.findPath(LOBBY_NBT_RESOURCE_PATH))
			.map(path -> {
				try {
					return java.nio.file.Files.newInputStream(path);
				} catch (IOException e) {
					return null;
				}
			})
			.filter(s -> s != null);

		if (streamOpt.isEmpty()) {
			return Optional.empty();
		}

		try (InputStream in = streamOpt.get()) {
			NbtCompound nbt = NbtIo.readCompressed(in, NbtSizeTracker.ofUnlimitedBytes());
			StructureTemplate template = new StructureTemplate();
			template.readNbt(Registries.BLOCK.getReadOnlyWrapper(), nbt);
			return Optional.of(template);
		} catch (IOException e) {
			RNGArena.LOGGER.error("Failed to read lobby structure from mod resources", e);
			return Optional.empty();
		}
	}

	private static void placeLobbyIfNeeded(MinecraftServer server, ServerWorld world) {
		RNGArenaWorldState worldState = persistentState(server);
		if (worldState.isSpawnLobbyPlaced()) {
			return;
		}

		Optional<StructureTemplate> templateOpt = loadLobbyTemplate(world);
		if (templateOpt.isEmpty()) {
			RNGArena.LOGGER.error(
				"Could not load structure {} (vanilla manager + mod jar). Check {} in resources.",
				LOBBY_STRUCTURE,
				LOBBY_NBT_RESOURCE_PATH
			);
			return;
		}

		StructureTemplate template = templateOpt.get();
		Vec3i size = template.getSize();
		if (size.getX() <= 0 || size.getY() <= 0 || size.getZ() <= 0) {
			RNGArena.LOGGER.error("Structure {} has invalid size {}; not placing.", LOBBY_STRUCTURE, size);
			return;
		}

		RNGArena.LOGGER.info("Placing structure {} (size {}) at {}", LOBBY_STRUCTURE, size, LOBBY_ORIGIN);

		BlockPos footprintEnd = LOBBY_ORIGIN.add(size.getX() - 1, size.getY() - 1, size.getZ() - 1);
		ChunkPos minChunk = new ChunkPos(LOBBY_ORIGIN);
		ChunkPos maxChunk = new ChunkPos(footprintEnd);
		for (int cx = minChunk.x; cx <= maxChunk.x; cx++) {
			for (int cz = minChunk.z; cz <= maxChunk.z; cz++) {
				world.getChunkManager().getChunk(cx, cz, ChunkStatus.FULL, true);
			}
		}

		StructurePlacementData placement = new StructurePlacementData()
			.setMirror(BlockMirror.NONE)
			.setRotation(BlockRotation.NONE);

		template.place(world, LOBBY_ORIGIN, LOBBY_ORIGIN, placement, Random.create(), Block.NOTIFY_ALL);

		world.setSpawnPos(PLAYER_SPAWN, 0.0F);
		worldState.setSpawnLobbyPlaced(true);
		RNGArena.LOGGER.info("Placed RNG Arena lobby at {}", LOBBY_ORIGIN);
		LobbyWelcome.tryWelcomeAllInLobby(server);
	}
}
