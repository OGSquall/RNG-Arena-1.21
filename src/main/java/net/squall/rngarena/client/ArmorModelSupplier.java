package net.squall.rngarena.client;

import net.minecraft.client.model.ModelPart;
import net.minecraft.entity.LivingEntity;
import net.squall.rngarena.client.model.ArmorModel;

@FunctionalInterface
public interface ArmorModelSupplier {
	ArmorModel<LivingEntity> create(ModelPart root, boolean isSlim);
}
