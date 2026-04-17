package net.squall.rngarena.registry;

import net.fabricmc.fabric.api.entity.event.v1.ServerLivingEntityEvents;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.damage.DamageSource;
import net.minecraft.entity.damage.DamageTypes;
import net.squall.rngarena.item.CactusStaffItem;

public final class CactusStaffEvents {
	private static final double THORN_REFLECT_CHANCE = 0.15D;
	private static final float THORN_REFLECT_DAMAGE_RATIO = 0.25F;
	private static final float MIN_REFLECT_DAMAGE = 0.5F;

	private CactusStaffEvents() {
	}

	public static void register() {
		ServerLivingEntityEvents.ALLOW_DAMAGE.register(CactusStaffEvents::onAllowDamage);
	}

	private static boolean onAllowDamage(LivingEntity target, DamageSource source, float amount) {
		if (amount <= 0.0F) {
			return true;
		}

		LivingEntity attacker = getDirectLivingAttacker(source);
		if (attacker == null) {
			return true;
		}

		if (!canReflect(target, source)) {
			return true;
		}

		if (target.getRandom().nextDouble() >= THORN_REFLECT_CHANCE) {
			return true;
		}

		float reflectedDamage = Math.max(MIN_REFLECT_DAMAGE, amount * THORN_REFLECT_DAMAGE_RATIO);
		attacker.damage(attacker.getDamageSources().thorns(target), reflectedDamage);
		return true;
	}

	private static boolean canReflect(LivingEntity target, DamageSource source) {
		if (!CactusStaffItem.isHoldingCactusStaff(target)) {
			return false;
		}
		if (source.isOf(DamageTypes.THORNS)) {
			return false;
		}
		return true;
	}

	private static LivingEntity getDirectLivingAttacker(DamageSource source) {
		if (!(source.getAttacker() instanceof LivingEntity attacker)) {
			return null;
		}
		// Direct-only: reject projectile and other proxy damage where source entity differs from attacker.
		if (source.getSource() != attacker) {
			return null;
		}
		return attacker;
	}
}
