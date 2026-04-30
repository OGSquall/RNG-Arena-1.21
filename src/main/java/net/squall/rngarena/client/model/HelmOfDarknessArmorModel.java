package net.squall.rngarena.client.model;

import net.minecraft.client.model.ModelData;
import net.minecraft.client.model.ModelPart;
import net.minecraft.client.model.ModelPartBuilder;
import net.minecraft.client.model.ModelPartData;
import net.minecraft.client.model.ModelTransform;
import net.minecraft.client.model.TexturedModelData;
import net.minecraft.client.render.entity.model.EntityModelLayer;
import net.minecraft.client.render.entity.model.EntityModelPartNames;
import net.minecraft.entity.LivingEntity;
import net.minecraft.util.Identifier;
import net.squall.rngarena.RNGArena;

public final class HelmOfDarknessArmorModel extends ArmorModel<LivingEntity> {
	public static final EntityModelLayer LAYER = new EntityModelLayer(
			Identifier.of(RNGArena.MOD_ID, "helm_of_darkness"), "main");

	public HelmOfDarknessArmorModel(ModelPart root) {
		this(root, false);
	}

	public HelmOfDarknessArmorModel(ModelPart root, boolean isSlim) {
		super(root, isSlim);
	}

	public static TexturedModelData createTexturedModelData() {
		ModelData modelData = net.minecraft.client.render.entity.model.BipedEntityModel.getModelData(
				net.minecraft.client.model.Dilation.NONE, 0.0F);
		ModelPartData root = modelData.getRoot();
		ModelPartData head = root.getChild(EntityModelPartNames.HEAD);

		ModelPartData bone = head.addChild("bone", ModelPartBuilder.create()
				.uv(16, 45).cuboid(-4.0F, -13.25F, 3.0F, 1.0F, 9.0F, 4.0F)
				.uv(48, 26).cuboid(-4.0F, -13.25F, 10.0F, 1.0F, 7.0F, 3.0F)
				.uv(16, 37).cuboid(-12.0F, -13.25F, 12.0F, 8.0F, 7.0F, 1.0F)
				.uv(0, 0).cuboid(-12.5F, -13.25F, 3.0F, 9.0F, 1.0F, 9.5F)
				.uv(58, 38).cuboid(-9.3035F, -13.25F, 1.4693F, 2.608F, 4.0F, 1.25F)
				.uv(50, 46).cuboid(-4.0F, -13.25F, 7.0F, 1.0F, 6.0F, 3.0F)
				.uv(26, 47).cuboid(-13.0F, -13.25F, 3.0F, 1.0F, 9.0F, 4.0F)
				.uv(0, 54).cuboid(-13.0F, -13.25F, 7.0F, 1.0F, 6.0F, 3.0F)
				.uv(50, 36).cuboid(-13.0F, -13.25F, 10.0F, 1.0F, 7.0F, 3.0F)
				.uv(57, 55).cuboid(-8.75F, -13.75F, 1.075F, 1.5F, 3.5F, 2.925F)
				.uv(37, 69).cuboid(-2.7866F, -14.3634F, 2.8654F, 0.0F, 3.5F, 8.5F)
				.uv(37, 69).cuboid(-13.1834F, -14.3634F, 2.8654F, 0.0F, 3.5F, 8.5F)
				.uv(54, 0).cuboid(-8.4965F, -12.4106F, 12.4824F, 0.993F, 3.5F, 3.5F)
				.uv(0, 35).cuboid(-8.505F, -19.074F, 4.3494F, 1.01F, 3.0F, 7.0F)
				.uv(12, 75).cuboid(-9.025F, -16.8024F, 4.9975F, 2.05F, 3.0F, 6.0F)
				.uv(77, 35).cuboid(-10.5F, -9.5F, 2.25F, 5.0F, 8.5F, 0.0F)
				.uv(0, 2).cuboid(-9.0F, -14.0F, 12.0F, 2.0F, 5.0F, 2.0F),
				ModelTransform.of(8.0F, 5.0F, -8.0F, 0.0F, 0.0F, 0.0F));

		bone.addChild("cube_r1", ModelPartBuilder.create().uv(73, 49).mirrored().cuboid(-0.0504F, 0.912F, -6.6154F, 2.0F, 8.5F, 0.03F).mirrored(false)
				.uv(16, 35).cuboid(-1.8081F, -1.9514F, -7.0853F, 4.5F, 1.5F, 0.0F)
				.mirrored().uv(37, 88).cuboid(-1.4537F, -2.838F, -6.8792F, 4.0F, 4.0F, 1.25F).mirrored(false)
				.uv(58, 52).cuboid(-1.4537F, 6.162F, -6.8792F, 2.0F, 2.0F, 1.0F)
				.uv(12, 60).cuboid(-1.4537F, 4.162F, -6.8792F, 1.0F, 2.0F, 1.0F)
				.uv(58, 17).cuboid(-0.4537F, 2.162F, -6.8792F, 3.0F, 4.0F, 1.0F),
				ModelTransform.of(-7.985F, -10.412F, 8.3812F, 0.0F, -0.3927F, 0.0F));

		bone.addChild("cube_r2", ModelPartBuilder.create().uv(73, 49).cuboid(-1.9773F, 0.912F, -6.6269F, 2.0F, 8.5F, 0.03F)
				.uv(16, 35).mirrored().cuboid(-2.7196F, -1.9514F, -7.0967F, 4.5F, 1.5F, 0.0F).mirrored(false)
				.uv(60, 13).cuboid(-0.574F, 6.162F, -6.8907F, 2.0F, 2.0F, 1.0F)
				.uv(30, 60).cuboid(0.426F, 4.162F, -6.8907F, 1.0F, 2.0F, 1.0F)
				.uv(58, 33).cuboid(-2.574F, 2.162F, -6.8907F, 3.0F, 4.0F, 1.0F)
				.uv(37, 88).cuboid(-2.574F, -2.838F, -6.8907F, 4.0F, 4.0F, 1.25F),
				ModelTransform.of(-7.985F, -10.412F, 8.3812F, 0.0F, 0.3927F, 0.0F));

		bone.addChild("cube_r3", ModelPartBuilder.create().uv(36, 47).cuboid(-1.015F, -6.9051F, -0.0283F, 2.0F, 2.775F, 4.5F)
				.uv(77, 3).cuboid(-0.515F, -6.3636F, -0.5725F, 1.0F, 3.0F, 7.0F)
				.uv(38, 0).cuboid(-0.515F, -9.1385F, -0.5725F, 1.0F, 2.775F, 7.0F),
				ModelTransform.of(-7.985F, -10.412F, 8.3812F, -0.3927F, 0.0F, 0.0F));

		bone.addChild("cube_r4", ModelPartBuilder.create().uv(60, 8).cuboid(-0.5065F, 3.1479F, 4.5014F, 0.983F, 2.5F, 2.25F)
				.uv(80, 21).cuboid(-0.515F, -9.5455F, -3.91F, 1.0F, 2.775F, 3.5F)
				.uv(48, 10).cuboid(-1.015F, -7.1988F, -4.6806F, 2.0F, 2.775F, 4.0F),
				ModelTransform.of(-7.985F, -10.412F, 8.3812F, 0.3927F, 0.0F, 0.0F));

		bone.addChild("cube_r5", ModelPartBuilder.create().uv(16, 58).cuboid(-0.5983F, -3.3652F, -7.2812F, 1.5F, 3.5F, 2.225F)
				.uv(0, 11).cuboid(-0.9805F, -4.5296F, -5.3812F, 4.5F, 2.0F, 9.993F),
				ModelTransform.of(-7.985F, -10.412F, 8.3812F, 0.0F, 0.0F, 0.3927F));

		bone.addChild("cube_r6", ModelPartBuilder.create().uv(16, 58).mirrored().cuboid(-0.9294F, -3.3767F, -7.2812F, 1.5F, 3.5F, 2.225F).mirrored(false)
				.uv(0, 23).cuboid(-3.5472F, -4.5411F, -5.3812F, 4.5F, 2.0F, 9.993F),
				ModelTransform.of(-7.985F, -10.412F, 8.3812F, 0.0F, 0.0F, -0.3927F));

		bone.addChild("cube_r7", ModelPartBuilder.create().uv(24, 60).cuboid(-0.5122F, -0.491F, -7.3812F, 1.5F, 1.5F, 1.0F),
				ModelTransform.of(-7.985F, -10.412F, 8.3812F, 0.0F, 0.0F, 0.7854F));

		return TexturedModelData.of(modelData, 128, 128);
	}

	@Override
	protected void setupArmorPartAnim(float limbSwing, float limbSwingAmount, float ageInTicks, float netHeadYaw,
			float headPitch) {
	}
}