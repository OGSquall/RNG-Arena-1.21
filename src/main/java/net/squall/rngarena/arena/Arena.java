package net.squall.rngarena.arena;

import java.util.List;

import net.minecraft.util.Identifier;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.Vec3i;
import net.squall.rngarena.RNGArena;

/**
 * Describes a structure-based arena: where it is placed in the world and where players spawn
 * relative to that origin. Multiple arenas later become multiple {@link Arena} instances.
 */
public final class Arena {
	/**
	 * Single test arena: structure {@code rng-arena:arena_1} with spawns relative to placement origin.
	 */
	public static Arena createTestArena() {
		return new Arena(
			"arena_1",
			"Test Arena",
			Identifier.of(RNGArena.MOD_ID, "arena_1"),
			List.of(
				new Vec3i(28, 2, 4),
				new Vec3i(4, 2, 28)
			),
			new Vec3i(-16, 0, -16)
		);
	}

	private final String arenaId;
	private final String displayName;
	private final Identifier structureId;
	private final List<Vec3i> relativeSpawnOffsets;
	private final Vec3i relativeLookTargetOffset;
	private BlockPos origin;

	private Arena(
		String arenaId,
		String displayName,
		Identifier structureId,
		List<Vec3i> relativeSpawnOffsets,
		Vec3i relativeLookTargetOffset
	) {
		this.arenaId = arenaId;
		this.displayName = displayName;
		this.structureId = structureId;
		this.relativeSpawnOffsets = List.copyOf(relativeSpawnOffsets);
		this.relativeLookTargetOffset = relativeLookTargetOffset;
	}

	public String getArenaId() {
		return this.arenaId;
	}

	public String getDisplayName() {
		return this.displayName;
	}

	public Identifier getStructureId() {
		return this.structureId;
	}

	public List<Vec3i> getRelativeSpawnOffsets() {
		return this.relativeSpawnOffsets;
	}

	public Vec3i getRelativeLookTargetOffset() {
		return this.relativeLookTargetOffset;
	}

	public int getSpawnCount() {
		return this.relativeSpawnOffsets.size();
	}

	public boolean hasOrigin() {
		return this.origin != null;
	}

	public BlockPos getOrigin() {
		return this.origin;
	}

	/** Called once when the structure is placed (or re-attached from a known fixed origin on load). */
	public void setOrigin(BlockPos origin) {
		this.origin = origin.toImmutable();
	}

	/**
	 * World block position for spawn slot {@code index} (wraps if there are more players than spawns).
	 */
	public BlockPos getSpawnBlockPos(int index) {
		if (this.origin == null) {
			throw new IllegalStateException("Arena " + this.arenaId + " has no origin yet");
		}
		Vec3i offset = this.relativeSpawnOffsets.get(Math.floorMod(index, this.relativeSpawnOffsets.size()));
		return this.origin.add(offset);
	}

	public float getSpawnYaw(int index) {
		if (this.origin == null) {
			throw new IllegalStateException("Arena " + this.arenaId + " has no origin yet");
		}
		Vec3i spawnOffset = this.relativeSpawnOffsets.get(Math.floorMod(index, this.relativeSpawnOffsets.size()));
		double spawnX = this.origin.getX() + spawnOffset.getX() + 0.5;
		double spawnZ = this.origin.getZ() + spawnOffset.getZ() + 0.5;
		double targetX = this.origin.getX() + this.relativeLookTargetOffset.getX() + 0.5;
		double targetZ = this.origin.getZ() + this.relativeLookTargetOffset.getZ() + 0.5;
		double deltaX = targetX - spawnX;
		double deltaZ = targetZ - spawnZ;
		float yaw = (float) (Math.toDegrees(Math.atan2(deltaZ, deltaX)) - 90.0D);
		return MathHelper.wrapDegrees(yaw);
	}
}
