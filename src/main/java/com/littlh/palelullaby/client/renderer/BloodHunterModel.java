package com.littlh.palelullaby.client.renderer;

import com.littlh.palelullaby.entity.CasterMob;
import net.minecraft.client.model.AnimationUtils;
import net.minecraft.client.model.PlayerModel;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.Mob;

/**
 * BloodHunter model: borrows the pillager's crossbow poses (animateCrossbowHold / Charge),
 * so the silver sword and crossbow look properly held; melee swings come from PlayerModel.
 * While the hunter channels a silver ability, both arms raise forward instead.
 */
public class BloodHunterModel<T extends Mob> extends PlayerModel<T> {
    public BloodHunterModel(ModelPart root) {
        super(root, false);
    }

    @Override
    public void setupAnim(T entity, float limbSwing, float limbSwingAmount,
                          float ageInTicks, float netHeadYaw, float headPitch) {
        super.setupAnim(entity, limbSwing, limbSwingAmount, ageInTicks, netHeadYaw, headPitch);
        if (entity instanceof CasterMob caster && caster.isCasting()) {
            this.head.yRot = 0.0F;
            this.head.xRot = headPitch * ((float) Math.PI / 180.0F) + 0.12F;
            this.hat.copyFrom(this.head);
            float f = ageInTicks * 0.05F;
            this.rightArm.xRot = -1.9F + Mth.sin(f) * 0.05F;
            this.leftArm.xRot = -1.9F + Mth.sin(f + (float) Math.PI) * 0.05F;
            this.rightArm.zRot = 0.05F;
            this.leftArm.zRot = -0.05F;
            this.body.xRot = 0.08F;
            this.syncArmWear();
            return;
        }
        if (this.attackTime <= 0.0F && entity.isUsingItem()) {
            AnimationUtils.animateCrossbowCharge(this.rightArm, this.leftArm, entity, false);
        }
        if (this.attackTime > 0.0F) {
            float f = Mth.sin(this.attackTime * (float) Math.PI);
            float f1 = Mth.sin((1.0F - (1.0F - this.attackTime) * (1.0F - this.attackTime)) * (float) Math.PI);
            this.rightArm.xRot = -(f * 1.6F - f1 * 0.5F);
            this.rightArm.yRot = 0.0F;
            this.rightArm.zRot = -0.2F * f;
            this.leftArm.xRot = 0.0F;
            this.leftArm.yRot = 0.0F;
            this.leftArm.zRot = 0.0F;
        }
        this.syncArmWear();
    }

    /**
     * PlayerModel copies the sleeves from the arms at the end of its own setupAnim;
     * re-copy them after we override the arm poses, otherwise the armor sleeves keep
     * the idle pose and look like extra arms/hands while the hunter charges a crossbow.
     */
    private void syncArmWear() {
        this.leftSleeve.copyFrom(this.leftArm);
        this.rightSleeve.copyFrom(this.rightArm);
    }
}