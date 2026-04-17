package net.squall.rngarena.effect;

import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.effect.StatusEffect;
import net.minecraft.entity.effect.StatusEffectCategory;
import net.minecraft.entity.damage.DamageSource;

/**
 * Small periodic thorn damage applied by the cactus staff.
 */
public final class PrickedStatusEffect extends StatusEffect {
	public static final int COLOR = 0x4F9F45;
	private static final float BASE_TICK_DAMAGE = 0.5F;

	public PrickedStatusEffect() {
		super(StatusEffectCategory.HARMFUL, COLOR);
	}

	public boolean applyUpdateEffect(LivingEntity entity, int amplifier) {
		DamageSource damageSource = entity.getDamageSources().magic();
		float tickDamage = BASE_TICK_DAMAGE + (0.25F * amplifier);
		entity.damage(damageSource, tickDamage);
		return true;
	}

	@Override
	public boolean canApplyUpdateEffect(int duration, int amplifier) {
		return duration % 20 == 0;
	}
}
