// Made with Blockbench 5.1.4
// Exported for Minecraft version 1.17 or later with Mojang mappings
// Paste this class into your mod and generate all required imports


public class helmet_Converted<T extends Entity> extends EntityModel<T> {
	// This layer location should be baked with EntityRendererProvider.Context in the entity renderer and passed into this model's constructor
	public static final ModelLayerLocation LAYER_LOCATION = new ModelLayerLocation(new ResourceLocation("modid", "helmet_converted"), "main");
	private final ModelPart bone;

	public helmet_Converted(ModelPart root) {
		this.bone = root.getChild("bone");
	}

	public static LayerDefinition createBodyLayer() {
		MeshDefinition meshdefinition = new MeshDefinition();
		PartDefinition partdefinition = meshdefinition.getRoot();

		PartDefinition bone = partdefinition.addOrReplaceChild("bone", CubeListBuilder.create().texOffs(16, 45).addBox(-4.0F, -13.25F, 3.0F, 1.0F, 9.0F, 4.0F, new CubeDeformation(0.0F))
		.texOffs(48, 26).addBox(-4.0F, -13.25F, 10.0F, 1.0F, 7.0F, 3.0F, new CubeDeformation(0.0F))
		.texOffs(16, 37).addBox(-12.0F, -13.25F, 12.0F, 8.0F, 7.0F, 1.0F, new CubeDeformation(0.0F))
		.texOffs(0, 0).addBox(-12.5F, -13.25F, 3.0F, 9.0F, 1.0F, 9.5F, new CubeDeformation(0.0F))
		.texOffs(58, 38).addBox(-9.3035F, -13.25F, 1.4693F, 2.608F, 4.0F, 1.25F, new CubeDeformation(0.0F))
		.texOffs(50, 46).addBox(-4.0F, -13.25F, 7.0F, 1.0F, 6.0F, 3.0F, new CubeDeformation(0.0F))
		.texOffs(26, 47).addBox(-13.0F, -13.25F, 3.0F, 1.0F, 9.0F, 4.0F, new CubeDeformation(0.0F))
		.texOffs(0, 54).addBox(-13.0F, -13.25F, 7.0F, 1.0F, 6.0F, 3.0F, new CubeDeformation(0.0F))
		.texOffs(50, 36).addBox(-13.0F, -13.25F, 10.0F, 1.0F, 7.0F, 3.0F, new CubeDeformation(0.0F))
		.texOffs(57, 55).addBox(-8.75F, -13.75F, 1.075F, 1.5F, 3.5F, 2.925F, new CubeDeformation(0.0F))
		.texOffs(37, 69).addBox(-2.7866F, -14.3634F, 2.8654F, 0.0F, 3.5F, 8.5F, new CubeDeformation(0.0F))
		.texOffs(37, 69).addBox(-13.1834F, -14.3634F, 2.8654F, 0.0F, 3.5F, 8.5F, new CubeDeformation(0.0F))
		.texOffs(54, 0).addBox(-8.4965F, -12.4106F, 12.4824F, 0.993F, 3.5F, 3.5F, new CubeDeformation(0.0F))
		.texOffs(0, 35).addBox(-8.505F, -19.074F, 4.3494F, 1.01F, 3.0F, 7.0F, new CubeDeformation(0.0F))
		.texOffs(12, 75).addBox(-9.025F, -16.8024F, 4.9975F, 2.05F, 3.0F, 6.0F, new CubeDeformation(0.0F))
		.texOffs(77, 35).addBox(-10.5F, -9.5F, 2.25F, 5.0F, 8.5F, 0.0F, new CubeDeformation(0.0F))
		.texOffs(0, 2).addBox(-9.0F, -14.0F, 12.0F, 2.0F, 5.0F, 2.0F, new CubeDeformation(0.0F)), PartPose.offset(8.0F, 24.0F, -8.0F));

		PartDefinition cube_r1 = bone.addOrReplaceChild("cube_r1", CubeListBuilder.create().texOffs(73, 49).mirror().addBox(-0.0505F, 0.912F, -6.6154F, 2.0F, 8.5F, 0.03F, new CubeDeformation(0.0F)).mirror(false)
		.texOffs(16, 35).addBox(-1.8081F, -1.9514F, -7.0853F, 4.5F, 1.5F, 0.0F, new CubeDeformation(0.0F))
		.texOffs(58, 52).addBox(-1.4537F, 6.162F, -6.8792F, 2.0F, 2.0F, 1.0F, new CubeDeformation(0.0F))
		.texOffs(12, 60).addBox(-1.4537F, 4.162F, -6.8792F, 1.0F, 2.0F, 1.0F, new CubeDeformation(0.0F))
		.texOffs(58, 17).addBox(-0.4537F, 2.162F, -6.8792F, 3.0F, 4.0F, 1.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(-7.985F, -10.412F, 8.3812F, 0.0F, -0.3927F, 0.0F));

		PartDefinition cube_r2 = bone.addOrReplaceChild("cube_r2", CubeListBuilder.create().texOffs(73, 49).addBox(-1.9773F, 0.912F, -6.6269F, 2.0F, 8.5F, 0.03F, new CubeDeformation(0.0F))
		.texOffs(16, 35).mirror().addBox(-2.7196F, -1.9514F, -7.0967F, 4.5F, 1.5F, 0.0F, new CubeDeformation(0.0F)).mirror(false)
		.texOffs(60, 13).addBox(-0.574F, 6.162F, -6.8907F, 2.0F, 2.0F, 1.0F, new CubeDeformation(0.0F))
		.texOffs(30, 60).addBox(0.426F, 4.162F, -6.8907F, 1.0F, 2.0F, 1.0F, new CubeDeformation(0.0F))
		.texOffs(58, 33).addBox(-2.574F, 2.162F, -6.8907F, 3.0F, 4.0F, 1.0F, new CubeDeformation(0.0F))
		.texOffs(37, 88).addBox(-2.574F, -2.838F, -6.8907F, 4.0F, 4.0F, 1.25F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(-7.985F, -10.412F, 8.3812F, 0.0F, 0.3927F, 0.0F));

		PartDefinition cube_r3 = bone.addOrReplaceChild("cube_r3", CubeListBuilder.create().texOffs(36, 47).addBox(-1.015F, -6.9051F, -0.0283F, 2.0F, 2.775F, 4.5F, new CubeDeformation(0.0F))
		.texOffs(77, 3).addBox(-0.515F, -6.3635F, -0.5725F, 1.0F, 3.0F, 7.0F, new CubeDeformation(0.0F))
		.texOffs(38, 0).addBox(-0.515F, -9.1385F, -0.5725F, 1.0F, 2.775F, 7.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(-7.985F, -10.412F, 8.3812F, -0.3927F, 0.0F, 0.0F));

		PartDefinition cube_r4 = bone.addOrReplaceChild("cube_r4", CubeListBuilder.create().texOffs(60, 8).addBox(-0.5065F, 3.1479F, 4.5014F, 0.983F, 2.5F, 2.25F, new CubeDeformation(0.0F))
		.texOffs(80, 21).addBox(-0.515F, -9.5455F, -3.9101F, 1.0F, 2.775F, 3.5F, new CubeDeformation(0.0F))
		.texOffs(48, 10).addBox(-1.015F, -7.1988F, -4.6806F, 2.0F, 2.775F, 4.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(-7.985F, -10.412F, 8.3812F, 0.3927F, 0.0F, 0.0F));

		PartDefinition cube_r5 = bone.addOrReplaceChild("cube_r5", CubeListBuilder.create().texOffs(16, 58).addBox(-0.5983F, -3.3652F, -7.2812F, 1.5F, 3.5F, 2.225F, new CubeDeformation(0.0F))
		.texOffs(0, 11).addBox(-0.9805F, -4.5296F, -5.3812F, 4.5F, 2.0F, 9.993F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(-7.985F, -10.412F, 8.3812F, 0.0F, 0.0F, 0.3927F));

		PartDefinition cube_r6 = bone.addOrReplaceChild("cube_r6", CubeListBuilder.create().texOffs(16, 58).mirror().addBox(-0.9294F, -3.3766F, -7.2812F, 1.5F, 3.5F, 2.225F, new CubeDeformation(0.0F)).mirror(false)
		.texOffs(0, 23).addBox(-3.5472F, -4.5411F, -5.3812F, 4.5F, 2.0F, 9.993F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(-7.985F, -10.412F, 8.3812F, 0.0F, 0.0F, -0.3927F));

		PartDefinition cube_r7 = bone.addOrReplaceChild("cube_r7", CubeListBuilder.create().texOffs(24, 60).addBox(-0.5122F, -0.4909F, -7.3812F, 1.5F, 1.5F, 1.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(-7.985F, -10.412F, 8.3812F, 0.0F, 0.0F, 0.7854F));

		PartDefinition problem_r1 = bone.addOrReplaceChild("problem_r1", CubeListBuilder.create().texOffs(37, 88).mirror().addBox(-2.1869F, -2.0F, -0.5621F, 4.0F, 4.0F, 1.25F, new CubeDeformation(0.0F)).mirror(false), PartPose.offsetAndRotation(-4.9131F, -11.25F, 2.8121F, 0.0F, -0.3927F, 0.0F));

		return LayerDefinition.create(meshdefinition, 128, 128);
	}

	@Override
	public void setupAnim(Entity entity, float limbSwing, float limbSwingAmount, float ageInTicks, float netHeadYaw, float headPitch) {

	}

	@Override
	public void renderToBuffer(PoseStack poseStack, VertexConsumer vertexConsumer, int packedLight, int packedOverlay, float red, float green, float blue, float alpha) {
		bone.render(poseStack, vertexConsumer, packedLight, packedOverlay, red, green, blue, alpha);
	}
}