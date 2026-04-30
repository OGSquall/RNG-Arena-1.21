package net.squall.rngarena.client.model;

import net.minecraft.client.render.entity.model.BipedEntityModel;
import net.minecraft.entity.LivingEntity;

public abstract class ArmorModel<T extends LivingEntity> extends BipedEntityModel<T> {
	public final boolean isSlim;

	protected ArmorModel(net.minecraft.client.model.ModelPart root, boolean isSlim) {
		super(root);
		this.isSlim = isSlim;
	}

	protected abstract void setupArmorPartAnim(float limbSwing, float limbSwingAmount, float ageInTicks,
			float netHeadYaw, float headPitch);

	@Override
	public void setAngles(T livingEntity, float limbSwing, float limbSwingAmount, float ageInTicks, float netHeadYaw,
			float headPitch) {
		setupArmorPartAnim(limbSwing, limbSwingAmount, ageInTicks, netHeadYaw, headPitch);
	}

	public static float sinPI(float value) {
		return net.minecraft.util.math.MathHelper.sin(value * (float) Math.PI);
	}

	public static float cosPI(float value) {
		return net.minecraft.util.math.MathHelper.cos(value * (float) Math.PI);
	}

	public <M extends BipedEntityModel<? extends LivingEntity>> void copyEntityModelPosition(M parentModel) {
		this.riding = parentModel.riding;
		this.child = parentModel.child;
		this.sneaking = parentModel.sneaking;
		this.head.copyTransform(parentModel.head);
		this.hat.copyTransform(parentModel.hat);
		this.body.copyTransform(parentModel.body);
		this.rightArm.copyTransform(parentModel.rightArm);
		this.leftArm.copyTransform(parentModel.leftArm);
		this.rightLeg.copyTransform(parentModel.rightLeg);
		this.leftLeg.copyTransform(parentModel.leftLeg);
	}
}
