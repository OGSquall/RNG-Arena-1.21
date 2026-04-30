package net.squall.rngarena.arena;

import java.io.IOException;
import java.io.InputStream;
import java.util.IdentityHashMap;
import java.util.Map;
import java.util.Optional;

import net.fabricmc.fabric.api.event.lifecycle.v1.ServerLifecycleEvents;
import net.fabricmc.loader.api.FabricLoader;
import net.minecraft.block.Block;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.nbt.NbtIo;
import net.minecraft.nbt.NbtSizeTracker;
import net.minecraft.registry.Registries;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.structure.StructurePlacementData;
import net.minecraft.structure.StructureTemplate;
import net.minecraft.util.BlockMirror;
import net.minecraft.util.BlockRotation;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.ChunkPos;
import net.minecraft.util.math.Vec3i;
import net.minecraft.util.math.random.Random;
import net.minecraft.world.chunk.ChunkStatus;
import net.squall.rngarena.RNGArena;
import net.squall.rngarena.world.RNGArenaWorld;
import net.squall.rngarena.world.RNGArenaWorldState;

/**
 * Owns the test arena definition, places {@link Arena#getStructureId()} once per save, and exposes spawn positions.
 */
public final class ArenaManager {
	/**
	 * Fixed placement in the RNG Arenas overworld (flat void preset), away from the lobby at (0, 64, 0).
	 */
	private static final BlockPos TEST_ARENA_ORIGIN = new BlockPos(256, 64, 0);
	private static final String ARENA_NBT_RESOURCE_PATH = "data/" + RNGArena.MOD_ID + "/structures/arena_1.nbt";

	private static final Map<MinecraftServer, ArenaManager> INSTANCES = new IdentityHashMap<>();

	private final Arena testArena = Arena.createTestArena();

	private ArenaManager() {
	}

	public static void register() {
		ServerLifecycleEvents.SERVER_STOPPING.register(INSTANCES::remove);
	}

	public static ArenaManager get(MinecraftServer server) {
		synchronized (INSTANCES) {
			return INSTANCES.computeIfAbsent(server, s -> new ArenaManager());
		}
	}

	public Arena getActiveTestArena() {
		return this.testArena;
	}

	/**
	 * If the arena was already placed in this save, only attaches {@link Arena#setOrigin(BlockPos)}.
	 * Otherwise, loads the structure, loads chunks, places once, and persists the flag.
	 */
	public void attachOrPlace(MinecraftServer server, ServerWorld overworld) {
		RNGArenaWorldState worldState = RNGArenaWorld.persistentState(server);
		if (worldState.isArenaTestPlaced()) {
			this.testArena.setOrigin(TEST_ARENA_ORIGIN);
			return;
		}

		Optional<StructureTemplate> templateOpt = this.loadArenaTemplate(overworld);
		if (templateOpt.isEmpty()) {
			RNGArena.LOGGER.error(
				"Could not load structure {} (vanilla manager + mod jar). Check {} in resources.",
				this.testArena.getStructureId(),
				ARENA_NBT_RESOURCE_PATH
			);
			return;
		}

		StructureTemplate template = templateOpt.get();
		Vec3i size = template.getSize();
		if (size.getX() <= 0 || size.getY() <= 0 || size.getZ() <= 0) {
			RNGArena.LOGGER.error("Structure {} has invalid size {}; not placing.", this.testArena.getStructureId(), size);
			return;
		}

		RNGArena.LOGGER.info(
			"Placing arena structure {} (size {}) at {}",
			this.testArena.getStructureId(),
			size,
			TEST_ARENA_ORIGIN
		);

		BlockPos footprintEnd = TEST_ARENA_ORIGIN.add(size.getX() - 1, size.getY() - 1, size.getZ() - 1);
		ChunkPos minChunk = new ChunkPos(TEST_ARENA_ORIGIN);
		ChunkPos maxChunk = new ChunkPos(footprintEnd);
		for (int cx = minChunk.x; cx <= maxChunk.x; cx++) {
			for (int cz = minChunk.z; cz <= maxChunk.z; cz++) {
				overworld.getChunkManager().getChunk(cx, cz, ChunkStatus.FULL, true);
			}
		}

		StructurePlacementData placement = new StructurePlacementData()
			.setMirror(BlockMirror.NONE)
			.setRotation(BlockRotation.NONE);

		template.place(overworld, TEST_ARENA_ORIGIN, TEST_ARENA_ORIGIN, placement, Random.create(), Block.NOTIFY_ALL);

		this.testArena.setOrigin(TEST_ARENA_ORIGIN);
		worldState.setArenaTestPlaced(true);
		RNGArena.LOGGER.info("Placed RNG Arena test arena at {}", TEST_ARENA_ORIGIN);
	}

	private Optional<StructureTemplate> loadArenaTemplate(ServerWorld world) {
		Optional<StructureTemplate> fromManager = world.getStructureTemplateManager().getTemplate(this.testArena.getStructureId());
		if (fromManager.isPresent()) {
			return fromManager;
		}

		Optional<InputStream> streamOpt = FabricLoader.getInstance()
			.getModContainer(RNGArena.MOD_ID)
			.flatMap(c -> c.findPath(ARENA_NBT_RESOURCE_PATH))
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
			RNGArena.LOGGER.error("Failed to read arena structure from mod resources", e);
			return Optional.empty();
		}
	}
}
