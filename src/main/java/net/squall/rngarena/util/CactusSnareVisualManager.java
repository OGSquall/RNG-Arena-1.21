package net.squall.rngarena.util;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.UUID;

import net.fabricmc.fabric.api.event.lifecycle.v1.ServerTickEvents;
import net.minecraft.block.Blocks;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.decoration.DisplayEntity;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.nbt.NbtFloat;
import net.minecraft.nbt.NbtHelper;
import net.minecraft.nbt.NbtList;
import net.minecraft.registry.RegistryKey;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.World;
import net.squall.rngarena.RNGArena;

public final class CactusSnareVisualManager {
	private static final int SNARE_VISUAL_DURATION_TICKS = 50;
	private static final float DISPLAY_TRANSLATE_X = -0.5F;
	private static final float DISPLAY_TRANSLATE_Y = 0.0F;
	private static final float DISPLAY_TRANSLATE_Z = -0.5F;

	private static final Map<UUID, ActiveSnareVisual> ACTIVE_SNARE_VISUALS_BY_TARGET = new HashMap<>();

	private CactusSnareVisualManager() {
	}

	public static void register() {
		ServerTickEvents.END_WORLD_TICK.register(CactusSnareVisualManager::onEndWorldTick);
	}

	public static void spawnOrRefresh(ServerWorld world, Entity target) {
		spawnOrRefresh(world, target, target.getBlockPos());
	}

	public static void spawnOrRefresh(ServerWorld world, Entity target, BlockPos anchoredBlockPos) {
		removeVisualForTarget(world, target.getUuid());
		Vec3d anchoredPosition = getAnchoredBlockPosition(anchoredBlockPos);

		DisplayEntity.BlockDisplayEntity display = createBlockDisplay(world);
		if (display == null) {
			return;
		}

		configureDisplayBlockState(display);
		display.setPosition(anchoredPosition);
		logVisualPlacement("spawn", target.getUuid(), anchoredBlockPos, anchoredPosition, display);
		world.spawnEntity(display);

		ACTIVE_SNARE_VISUALS_BY_TARGET.put(target.getUuid(),
				new ActiveSnareVisual(target.getUuid(), display.getUuid(), world.getRegistryKey(),
						anchoredBlockPos, world.getServer().getTicks() + SNARE_VISUAL_DURATION_TICKS, anchoredPosition));
	}

	public static void removeForTarget(ServerWorld world, UUID targetUuid) {
		removeVisualForTarget(world, targetUuid);
	}

	private static void onEndWorldTick(ServerWorld world) {
		long nowTick = world.getServer().getTicks();
		Iterator<ActiveSnareVisual> iterator = ACTIVE_SNARE_VISUALS_BY_TARGET.values().iterator();
		while (iterator.hasNext()) {
			ActiveSnareVisual visual = iterator.next();
			if (!visual.dimensionKey.equals(world.getRegistryKey())) {
				continue;
			}

			Entity target = world.getEntity(visual.targetUuid);
			Entity display = world.getEntity(visual.displayUuid);
			if (shouldRemoveVisual(nowTick, visual, target, display)) {
				if (display != null) {
					display.discard();
				}
				iterator.remove();
				continue;
			}

			display.setPosition(visual.anchoredPosition);
			logVisualPlacement("update", visual.targetUuid, visual.anchoredBlockPos, visual.anchoredPosition, display);
		}
	}

	private static boolean shouldRemoveVisual(long nowTick, ActiveSnareVisual visual, Entity target, Entity display) {
		return nowTick >= visual.expiresAtTick
				|| target == null
				|| !target.isAlive()
				|| target.isRemoved()
				|| display == null
				|| display.isRemoved();
	}

	private static Vec3d getAnchoredBlockPosition(BlockPos blockPos) {
		return new Vec3d(blockPos.getX() + 0.5D, blockPos.getY(), blockPos.getZ() + 0.5D);
	}

	private static DisplayEntity.BlockDisplayEntity createBlockDisplay(ServerWorld world) {
		Entity entity = EntityType.BLOCK_DISPLAY.create(world);
		if (entity instanceof DisplayEntity.BlockDisplayEntity blockDisplayEntity) {
			return blockDisplayEntity;
		}
		return null;
	}

	private static void configureDisplayBlockState(DisplayEntity.BlockDisplayEntity display) {
		// In 1.21 mappings, block display state and transformation are set through NBT.
		NbtCompound nbt = new NbtCompound();
		nbt.put("block_state", NbtHelper.fromBlockState(Blocks.CACTUS.getDefaultState()));
		nbt.put("transformation", createCenteredTransformationNbt());
		display.readNbt(nbt);
	}

	private static NbtCompound createCenteredTransformationNbt() {
		NbtCompound transformation = new NbtCompound();
		transformation.put("translation", createVec3Nbt(DISPLAY_TRANSLATE_X, DISPLAY_TRANSLATE_Y, DISPLAY_TRANSLATE_Z));
		transformation.put("scale", createVec3Nbt(1.0F, 1.0F, 1.0F));
		transformation.put("left_rotation", createQuaternionNbt(0.0F, 0.0F, 0.0F, 1.0F));
		transformation.put("right_rotation", createQuaternionNbt(0.0F, 0.0F, 0.0F, 1.0F));
		return transformation;
	}

	private static NbtList createVec3Nbt(float x, float y, float z) {
		NbtList list = new NbtList();
		list.add(NbtFloat.of(x));
		list.add(NbtFloat.of(y));
		list.add(NbtFloat.of(z));
		return list;
	}

	private static NbtList createQuaternionNbt(float x, float y, float z, float w) {
		NbtList list = new NbtList();
		list.add(NbtFloat.of(x));
		list.add(NbtFloat.of(y));
		list.add(NbtFloat.of(z));
		list.add(NbtFloat.of(w));
		return list;
	}

	private static void logVisualPlacement(String phase, UUID targetUuid, BlockPos anchoredBlockPos, Vec3d anchoredPosition, Entity display) {
		RNGArena.LOGGER.info(
				"[CactusSnareDebug:{}] target={} blockPos={} anchored={} displayPos=({}, {}, {}) transform.translation=({}, {}, {})",
				phase,
				targetUuid,
				anchoredBlockPos,
				anchoredPosition,
				display.getX(),
				display.getY(),
				display.getZ(),
				DISPLAY_TRANSLATE_X,
				DISPLAY_TRANSLATE_Y,
				DISPLAY_TRANSLATE_Z);
	}

	private static void removeVisualForTarget(ServerWorld world, UUID targetUuid) {
		ActiveSnareVisual existing = ACTIVE_SNARE_VISUALS_BY_TARGET.remove(targetUuid);
		if (existing == null || !existing.dimensionKey.equals(world.getRegistryKey())) {
			return;
		}

		Entity existingDisplay = world.getEntity(existing.displayUuid);
		if (existingDisplay != null) {
			existingDisplay.discard();
		}
	}

	private static final class ActiveSnareVisual {
		private final UUID targetUuid;
		private final UUID displayUuid;
		private final RegistryKey<World> dimensionKey;
		private final BlockPos anchoredBlockPos;
		private final long expiresAtTick;
		private final Vec3d anchoredPosition;

		private ActiveSnareVisual(UUID targetUuid, UUID displayUuid, RegistryKey<World> dimensionKey, BlockPos anchoredBlockPos, long expiresAtTick,
				Vec3d anchoredPosition) {
			this.targetUuid = targetUuid;
			this.displayUuid = displayUuid;
			this.dimensionKey = dimensionKey;
			this.anchoredBlockPos = anchoredBlockPos.toImmutable();
			this.expiresAtTick = expiresAtTick;
			this.anchoredPosition = anchoredPosition;
		}
	}
}
