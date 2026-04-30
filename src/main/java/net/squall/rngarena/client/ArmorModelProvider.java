package net.squall.rngarena.client;

import java.util.function.Supplier;

import org.jetbrains.annotations.NotNull;

import net.minecraft.client.MinecraftClient;
import net.minecraft.client.model.TexturedModelData;
import net.minecraft.client.network.AbstractClientPlayerEntity;
import net.minecraft.client.render.entity.model.EntityModelLayer;
import net.minecraft.client.util.SkinTextures;
import net.minecraft.entity.LivingEntity;
import net.minecraft.util.Identifier;
import net.squall.rngarena.client.model.ArmorModel;

public class ArmorModelProvider {
	private final Supplier<TexturedModelData> layerDefinitionSupplier;
	protected final ArmorModelSupplier modelSupplier;
	private ArmorModel<LivingEntity> armorModel;
	private final EntityModelLayer modelLayerLocation;
	private final Identifier resourceLocation;

	public static ArmorModelProvider create(Identifier texture, EntityModelLayer modelLayer,
			ArmorModelSupplier modelSupplier, Supplier<TexturedModelData> layerDefinitionSupplier) {
		return new ArmorModelProvider(texture, modelLayer, modelSupplier, layerDefinitionSupplier);
	}

	public static ArmorModelProvider create(Identifier texture, EntityModelLayer modelLayer,
			ArmorModelSupplier modelSupplier, Supplier<TexturedModelData> layerDefinitionSupplier,
			EntityModelLayer slimLayerModel, Supplier<TexturedModelData> slimLayerDefinitionSupplier,
			Identifier slimTexture) {
		return new MixedArmorModelProvider(texture, modelLayer, modelSupplier, layerDefinitionSupplier, slimLayerModel,
				slimLayerDefinitionSupplier, slimTexture);
	}

	protected ArmorModelProvider(Identifier texture, EntityModelLayer modelLayer, ArmorModelSupplier modelSupplier,
			Supplier<TexturedModelData> layerDefinitionSupplier) {
		this.layerDefinitionSupplier = layerDefinitionSupplier;
		this.modelSupplier = modelSupplier;
		this.modelLayerLocation = modelLayer;
		this.resourceLocation = texture;
	}

	public @NotNull Identifier getTexture(LivingEntity entity) {
		return this.resourceLocation;
	}

	public @NotNull EntityModelLayer getLayerLocation() {
		return this.modelLayerLocation;
	}

	public TexturedModelData createLayer() {
		return this.layerDefinitionSupplier.get();
	}

	public static boolean isSlim(LivingEntity entity) {
		return entity instanceof AbstractClientPlayerEntity player
				&& player.getSkinTextures().model() == SkinTextures.Model.SLIM;
	}

	public ArmorModel<LivingEntity> getArmorModel(LivingEntity entity) {
		if (this.armorModel == null) {
			this.armorModel = this.modelSupplier.create(
					MinecraftClient.getInstance().getEntityModelLoader().getModelPart(this.modelLayerLocation), false);
		}

		return this.armorModel;
	}

	public static class MixedArmorModelProvider extends ArmorModelProvider {
		private final Supplier<TexturedModelData> slimLayerDefinitionSupplier;
		private final EntityModelLayer slimModelLayerLocation;
		private final Identifier slimResourceLocation;
		private ArmorModel<LivingEntity> slimArmorModel;

		protected MixedArmorModelProvider(Identifier texture, EntityModelLayer modelLayer, ArmorModelSupplier modelSupplier,
				Supplier<TexturedModelData> layerDefinitionSupplier, EntityModelLayer slimLayerModel,
				Supplier<TexturedModelData> slimLayerDefinitionSupplier, Identifier slimTexture) {
			super(texture, modelLayer, modelSupplier, layerDefinitionSupplier);
			this.slimLayerDefinitionSupplier = slimLayerDefinitionSupplier;
			this.slimModelLayerLocation = slimLayerModel;
			this.slimResourceLocation = slimTexture;
		}

		public @NotNull EntityModelLayer getSlimLayerLocation() {
			return this.slimModelLayerLocation;
		}

		public TexturedModelData createSlimLayer() {
			return this.slimLayerDefinitionSupplier.get();
		}

		@Override
		public @NotNull Identifier getTexture(LivingEntity entity) {
			return isSlim(entity) ? this.slimResourceLocation : super.getTexture(entity);
		}

		@Override
		public ArmorModel<LivingEntity> getArmorModel(LivingEntity entity) {
			if (isSlim(entity)) {
				if (this.slimArmorModel == null) {
					this.slimArmorModel = this.modelSupplier.create(
							MinecraftClient.getInstance().getEntityModelLoader().getModelPart(this.slimModelLayerLocation), true);
				}

				return this.slimArmorModel;
			}
			return super.getArmorModel(entity);
		}
	}
}
