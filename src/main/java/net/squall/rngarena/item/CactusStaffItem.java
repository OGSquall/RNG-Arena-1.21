package net.squall.rngarena.item;

import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

import net.minecraft.component.DataComponentTypes;
import net.minecraft.component.type.NbtComponent;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.effect.StatusEffectInstance;
import net.minecraft.entity.effect.StatusEffects;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.item.SwordItem;
import net.minecraft.item.ToolMaterial;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.particle.ParticleTypes;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.sound.SoundCategory;
import net.minecraft.sound.SoundEvents;
import net.minecraft.text.Text;
import net.minecraft.util.Hand;
import net.minecraft.util.TypedActionResult;
import net.minecraft.world.World;
import net.squall.rngarena.registry.ModEffects;
import net.squall.rngarena.util.CactusSnareManager;

public final class CactusStaffItem extends SwordItem {
	private static final String NBT_SNARE_ARMED = "SnareArmed";

	public static final int BASE_ATTACK_DAMAGE = 1;
	public static final float NORMAL_ATTACK_SPEED = -2.4F;

	public static final int PRICKED_DURATION_TICKS = 5 * 20;
	public static final long PRICKED_COOLDOWN_MS = 10_000L;

	public static final int SNARE_ARM_COOLDOWN_TICKS = 11 * 20;
	public static final int SNARE_SLOW_DURATION_TICKS = 50;
	public static final int SNARE_DOT_DURATION_TICKS = 50;
	public static final int SNARE_INSTANT_DAMAGE = 1;

	private static final Map<UUID, Long> PRICKED_LAST_APPLIED_AT = new ConcurrentHashMap<>();

	public CactusStaffItem(ToolMaterial toolMaterial, Item.Settings settings) {
		super(toolMaterial, settings.attributeModifiers(
				SwordItem.createAttributeModifiers(toolMaterial, BASE_ATTACK_DAMAGE, NORMAL_ATTACK_SPEED)));
	}

	@Override
	public boolean postHit(ItemStack stack, LivingEntity target, LivingEntity attacker) {
		if (!(attacker.getWorld() instanceof ServerWorld serverWorld)) {
			return super.postHit(stack, target, attacker);
		}

		if (target instanceof PlayerEntity targetPlayer && !isPrickedOnCooldown(targetPlayer)) {
			targetPlayer.addStatusEffect(new StatusEffectInstance(ModEffects.PRICKED, PRICKED_DURATION_TICKS, 0));
			PRICKED_LAST_APPLIED_AT.put(targetPlayer.getUuid(), System.currentTimeMillis());
			spawnPrickedParticles(serverWorld, targetPlayer);
		}

		if (isSnareArmed(stack)) {
			applySnare(serverWorld, target);
			setSnareArmed(stack, false);
		}

		return super.postHit(stack, target, attacker);
	}

	@Override
	public TypedActionResult<ItemStack> use(World world, PlayerEntity user, Hand hand) {
		ItemStack stack = user.getStackInHand(hand);
		if (user.getItemCooldownManager().isCoolingDown(this)) {
			if (!world.isClient()) {
				user.sendMessage(Text.literal("Cactus Snare is recharging."), true);
			}
			return TypedActionResult.fail(stack);
		}

		setSnareArmed(stack, true);
		user.getItemCooldownManager().set(this, SNARE_ARM_COOLDOWN_TICKS);

		if (world instanceof ServerWorld serverWorld) {
			serverWorld.playSound(null, user.getBlockPos(), SoundEvents.ENTITY_PLAYER_ATTACK_WEAK, SoundCategory.PLAYERS, 0.9F, 1.1F);
			serverWorld.spawnParticles(ParticleTypes.COMPOSTER, user.getX(), user.getBodyY(0.5D), user.getZ(), 16, 0.35D, 0.35D, 0.35D, 0.01D);
			user.sendMessage(Text.literal("Cactus Snare armed"), true);
		}

		return TypedActionResult.consume(stack);
	}

	public static boolean isHoldingCactusStaff(LivingEntity entity) {
		return entity.getMainHandStack().getItem() instanceof CactusStaffItem
				|| entity.getOffHandStack().getItem() instanceof CactusStaffItem;
	}

	private static void applySnare(ServerWorld world, LivingEntity target) {
		target.addStatusEffect(new StatusEffectInstance(StatusEffects.SLOWNESS, SNARE_SLOW_DURATION_TICKS, 2));
		target.addStatusEffect(new StatusEffectInstance(ModEffects.PRICKED, SNARE_DOT_DURATION_TICKS, 0));
		target.damage(target.getDamageSources().cactus(), SNARE_INSTANT_DAMAGE);
		CactusSnareManager.applySnare(world, target);
		world.spawnParticles(ParticleTypes.COMPOSTER, target.getX() +0.5D, target.getY() + 0.1D, target.getZ() +0.5D, 24, 0.35D, 0.1D, 0.35D, 0.02D);
	}

	private static void spawnPrickedParticles(ServerWorld world, LivingEntity target) {
		world.spawnParticles(ParticleTypes.COMPOSTER, target.getX(), target.getBodyY(0.4D), target.getZ(), 10, 0.25D, 0.2D, 0.25D, 0.01D);
	}

	private static boolean isPrickedOnCooldown(PlayerEntity targetPlayer) {
		Long lastApplied = PRICKED_LAST_APPLIED_AT.get(targetPlayer.getUuid());
		return lastApplied != null && (System.currentTimeMillis() - lastApplied) < PRICKED_COOLDOWN_MS;
	}

	private static boolean isSnareArmed(ItemStack stack) {
		NbtCompound nbt = stack.getOrDefault(DataComponentTypes.CUSTOM_DATA, NbtComponent.DEFAULT).copyNbt();
		return nbt.getBoolean(NBT_SNARE_ARMED);
	}

	private static void setSnareArmed(ItemStack stack, boolean armed) {
		NbtCompound nbt = stack.getOrDefault(DataComponentTypes.CUSTOM_DATA, NbtComponent.DEFAULT).copyNbt();
		nbt.putBoolean(NBT_SNARE_ARMED, armed);
		stack.set(DataComponentTypes.CUSTOM_DATA, NbtComponent.of(nbt));
	}
}
