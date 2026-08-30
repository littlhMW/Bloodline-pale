package com.littlh.palelullaby.client.renderer;

import com.littlh.palelullaby.entity.CasterMob;
import net.minecraft.client.model.PlayerModel;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.Mob;

/**
 * Player-model variant used by spell-casting mobs (both the self-written
 * casters and the Iron's Spells casters): while the mob is channeling, both
 * arms are raised forward with a slow sway and the head tilts slightly down.
 * The body itself is turned to face the aim by CastingVampireRenderer, so
 * the relative head yaw is cancelled here.
 */
public class CastingVampireModel<T extends Mob> extends PlayerModel<T> {

    public CastingVampireModel(ModelPart root) {
        super(root, false);
    }

    @Override
    public void setupAnim(T entity, float limbSwing, float limbSwingAmount, float ageInTicks, float netHeadYaw, float headPitch) {
        super.setupAnim(entity, limbSwing, limbSwingAmount, ageInTicks, netHeadYaw, headPitch);
        if (entity instanceof CasterMob caster && caster.isCasting()) {
            // Body already faces the aim; keep the head straight on it, tilted slightly down.
            this.head.yRot = 0.0F;
            this.head.xRot = headPitch * ((float) Math.PI / 180.0F) + 0.12F;
            this.hat.copyFrom(this.head);
            // Both arms reach forward/up like channeling a spell, with a slow alternating sway.
            float f = ageInTicks * 0.05F;
            this.rightArm.xRot = -1.9F + Mth.sin(f) * 0.05F;
            this.leftArm.xRot = -1.9F + Mth.sin(f + (float) Math.PI) * 0.05F;
            this.rightArm.yRot = 0.05F;
            this.leftArm.yRot = -0.05F;
            this.rightArm.zRot = 0.05F;
            this.leftArm.zRot = -0.05F;
            this.body.xRot = 0.08F;
            // Re-copy the armor sleeves after overriding the arm poses.
            this.leftSleeve.copyFrom(this.leftArm);
            this.rightSleeve.copyFrom(this.rightArm);
        }
    }
}