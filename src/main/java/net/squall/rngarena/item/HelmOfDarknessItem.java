package net.squall.rngarena.item;

import net.minecraft.item.ArmorItem;
import net.minecraft.item.ArmorMaterial;
import net.minecraft.item.ItemStack;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EquipmentSlot;
import net.minecraft.entity.effect.StatusEffectInstance;
import net.minecraft.entity.effect.StatusEffects;
import net.minecraft.registry.entry.RegistryEntry;
import net.minecraft.util.Hand;
import net.minecraft.util.TypedActionResult;
import net.minecraft.world.World;
import net.minecraft.entity.player.PlayerEntity;

public class HelmOfDarknessItem extends ArmorItem {
	private static final int STEALTH_INVIS_DURATION_TICKS = 10;

	public HelmOfDarknessItem(RegistryEntry<ArmorMaterial> material, Type type, Settings settings) {
		super(material, type, settings);
	}

	@Override
	public TypedActionResult<ItemStack> use(World world, PlayerEntity user, Hand hand) {
		return equipAndSwap(this, world, user, hand);
	}

	@Override
	public void inventoryTick(ItemStack stack, World world, Entity entity, int slot, boolean selected) {
		super.inventoryTick(stack, world, entity, slot, selected);
		if (world.isClient() || !(entity instanceof PlayerEntity player)) {
			return;
		}

		if (!player.getEquippedStack(EquipmentSlot.HEAD).isOf(this)) {
			return;
		}

		if (player.isSneaking()) {
			player.addStatusEffect(new StatusEffectInstance(
					StatusEffects.INVISIBILITY,
					STEALTH_INVIS_DURATION_TICKS,
					0,
					false,
					false,
					false));
			return;
		}

		StatusEffectInstance invisibility = player.getStatusEffect(StatusEffects.INVISIBILITY);
		if (invisibility != null && !invisibility.shouldShowParticles() && !invisibility.shouldShowIcon()
				&& !invisibility.isAmbient()) {
			player.removeStatusEffect(StatusEffects.INVISIBILITY);
		}
	}
}
