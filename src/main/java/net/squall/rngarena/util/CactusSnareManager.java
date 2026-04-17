package net.squall.rngarena.util;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.UUID;

import net.fabricmc.fabric.api.event.lifecycle.v1.ServerTickEvents;
import net.minecraft.entity.LivingEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec3d;

public final class CactusSnareManager {
	private static final int SNARE_DURATION_TICKS = 50;
	private static final int THORN_TICK_INTERVAL = 10;
	private static final float THORN_TICK_DAMAGE = 0.5F;
	private static final double MAX_HORIZONTAL_DRIFT_FROM_ANCHOR = 0.55D;
	private static final double HORIZONTAL_VELOCITY_FACTOR = 0.02D;
	private static final double MAX_UPWARD_VELOCITY_WHILE_SNARED = 0.02D;

	private static final Map<UUID, ActiveSnare> ACTIVE_SNARES_BY_TARGET = new HashMap<>();

	private CactusSnareManager() {
	}

	public static void register() {
		ServerTickEvents.END_WORLD_TICK.register(CactusSnareManager::onEndWorldTick);
	}

	public static void applySnare(ServerWorld world, LivingEntity target) {
		long nowTick = world.getServer().getTicks();
		BlockPos anchorBlockPos = target.getBlockPos();
		ACTIVE_SNARES_BY_TARGET.put(target.getUuid(),
				new ActiveSnare(target.getUuid(), world.getRegistryKey(), anchorBlockPos, nowTick + SNARE_DURATION_TICKS, nowTick + THORN_TICK_INTERVAL));

		// Charged snare hit should not launch targets away from the cactus area.
		clampVelocity(target);
		CactusSnareVisualManager.spawnOrRefresh(world, target, anchorBlockPos);
	}

	private static void onEndWorldTick(ServerWorld world) {
		long nowTick = world.getServer().getTicks();
		Iterator<ActiveSnare> iterator = ACTIVE_SNARES_BY_TARGET.values().iterator();
		while (iterator.hasNext()) {
			ActiveSnare snare = iterator.next();
			if (!snare.dimensionKey.equals(world.getRegistryKey())) {
				continue;
			}

			if (nowTick >= snare.expiresAtTick) {
				CactusSnareVisualManager.removeForTarget(world, snare.targetUuid);
				iterator.remove();
				continue;
			}

			if (!(world.getEntity(snare.targetUuid) instanceof LivingEntity target) || !target.isAlive() || target.isRemoved()) {
				CactusSnareVisualManager.removeForTarget(world, snare.targetUuid);
				iterator.remove();
				continue;
			}

			keepTargetNearAnchor(target, snare.anchorBlockPos);
			clampVelocity(target);

			if (nowTick >= snare.nextDamageTick) {
				target.damage(target.getDamageSources().cactus(), THORN_TICK_DAMAGE);
				snare.nextDamageTick = nowTick + THORN_TICK_INTERVAL;
			}
		}
	}

	private static void keepTargetNearAnchor(LivingEntity target, BlockPos anchorBlockPos) {
		Vec3d anchorCenter = Vec3d.ofBottomCenter(anchorBlockPos);
		double deltaX = target.getX() - anchorCenter.x;
		double deltaZ = target.getZ() - anchorCenter.z;

		if (Math.abs(deltaX) > MAX_HORIZONTAL_DRIFT_FROM_ANCHOR || Math.abs(deltaZ) > MAX_HORIZONTAL_DRIFT_FROM_ANCHOR) {
			double clampedX = anchorCenter.x + Math.max(-MAX_HORIZONTAL_DRIFT_FROM_ANCHOR, Math.min(MAX_HORIZONTAL_DRIFT_FROM_ANCHOR, deltaX));
			double clampedZ = anchorCenter.z + Math.max(-MAX_HORIZONTAL_DRIFT_FROM_ANCHOR, Math.min(MAX_HORIZONTAL_DRIFT_FROM_ANCHOR, deltaZ));
			target.requestTeleport(clampedX, target.getY(), clampedZ);
		}
	}

	private static void clampVelocity(LivingEntity target) {
		Vec3d currentVelocity = target.getVelocity();
		double clampedUpwardVelocity = Math.min(currentVelocity.y, MAX_UPWARD_VELOCITY_WHILE_SNARED);
		target.setVelocity(currentVelocity.x * HORIZONTAL_VELOCITY_FACTOR, clampedUpwardVelocity, currentVelocity.z * HORIZONTAL_VELOCITY_FACTOR);
		target.velocityModified = true;
	}

	private static final class ActiveSnare {
		private final UUID targetUuid;
		private final net.minecraft.registry.RegistryKey<net.minecraft.world.World> dimensionKey;
		private final BlockPos anchorBlockPos;
		private final long expiresAtTick;
		private long nextDamageTick;

		private ActiveSnare(UUID targetUuid, net.minecraft.registry.RegistryKey<net.minecraft.world.World> dimensionKey, BlockPos anchorBlockPos,
				long expiresAtTick, long nextDamageTick) {
			this.targetUuid = targetUuid;
			this.dimensionKey = dimensionKey;
			this.anchorBlockPos = anchorBlockPos.toImmutable();
			this.expiresAtTick = expiresAtTick;
			this.nextDamageTick = nextDamageTick;
		}
	}
}
