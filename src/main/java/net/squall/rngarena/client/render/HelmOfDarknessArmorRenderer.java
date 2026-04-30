package net.squall.rngarena.client.render;

import net.fabricmc.fabric.api.client.rendering.v1.ArmorRenderer;
import net.minecraft.client.render.RenderLayer;
import net.minecraft.client.render.VertexConsumer;
import net.minecraft.client.render.VertexConsumerProvider;
import net.minecraft.client.render.entity.model.BipedEntityModel;
import net.minecraft.client.render.item.ItemRenderer;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.entity.EquipmentSlot;
import net.minecraft.entity.LivingEntity;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.util.Identifier;
import net.squall.rngarena.RNGArena;
import net.squall.rngarena.client.ArmorModelProvider;
import net.squall.rngarena.client.model.ArmorModel;
import net.squall.rngarena.client.model.HelmOfDarknessArmorModel;
import net.squall.rngarena.registry.ModItems;

public final class HelmOfDarknessArmorRenderer implements ArmorRenderer {
	private static final Identifier HELM_TEXTURE = Identifier.of(RNGArena.MOD_ID,
			"textures/models/armor/helm_of_darkness_layer_1.png");
	private static final HelmOfDarknessArmorRenderer INSTANCE = new HelmOfDarknessArmorRenderer();
	private static final ArmorModelProvider MODEL_PROVIDER = ArmorModelProvider.create(HELM_TEXTURE,
			HelmOfDarknessArmorModel.LAYER, (root, isSlim) -> new HelmOfDarknessArmorModel(root, isSlim),
			HelmOfDarknessArmorModel::createTexturedModelData);

	private HelmOfDarknessArmorRenderer() {
	}

	public static void register() {
		ArmorRenderer.register(INSTANCE, ModItems.HELM_OF_DARKNESS);
	}

	@Override
	public void render(MatrixStack matrices, VertexConsumerProvider vertexConsumers, ItemStack stack,
			LivingEntity entity, EquipmentSlot slot, int light, BipedEntityModel<LivingEntity> contextModel) {
		renderCustomArmor(matrices, vertexConsumers, stack, entity, slot, light, contextModel);
	}

	private void renderCustomArmor(MatrixStack matrices, VertexConsumerProvider vertexConsumers, ItemStack stack,
			LivingEntity entity, EquipmentSlot slot, int light, BipedEntityModel<LivingEntity> contextModel) {
		Item item = stack.getItem();
		if (item != ModItems.HELM_OF_DARKNESS || slot != EquipmentSlot.HEAD) {
			return;
		}
		if (entity.isInvisible()) {
			return;
		}

		ArmorModel<LivingEntity> armorModel = MODEL_PROVIDER.getArmorModel(entity);
		armorModel.copyEntityModelPosition(contextModel);
		armorModel.setVisible(false);
		armorModel.head.visible = true;
		armorModel.hat.visible = false;

		VertexConsumer vertexConsumer = ItemRenderer.getArmorGlintConsumer(vertexConsumers,
				RenderLayer.getArmorCutoutNoCull(MODEL_PROVIDER.getTexture(entity)), stack.hasGlint());
		armorModel.render(matrices, vertexConsumer, light, net.minecraft.client.render.OverlayTexture.DEFAULT_UV);
	}
}
