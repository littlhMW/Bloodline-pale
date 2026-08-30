package com.littlh.palelullaby.client.renderer;

import com.littlh.palelullaby.entity.CasterMob;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Axis;
import net.minecraft.client.model.PlayerModel;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.Mob;

/**
 * Base renderer for spell-casting mobs (self-written and Iron-based). While
 * a spell is being channeled the whole body is turned to face the aim
 * direction; the matching CastingVampireModel raises both arms forward.
 */
public abstract class CastingVampireRenderer<T extends Mob> extends PlayerModelMobRenderer<T> {

    protected CastingVampireRenderer(EntityRendererProvider.Context context, PlayerModel<T> model) {
        super(context, model);
    }

    @Override
    protected void setupRotations(T entity, PoseStack poseStack, float ageInTicks, float rotationYaw, float partialTick, float scale) {
        super.setupRotations(entity, poseStack, ageInTicks, rotationYaw, partialTick, scale);
        if (entity instanceof CasterMob caster && caster.isCasting()) {
            float bodyRot = Mth.rotLerp(partialTick, entity.yBodyRotO, entity.yBodyRot);
            float headRot = Mth.rotLerp(partialTick, entity.yHeadRotO, entity.yHeadRot);
            poseStack.mulPose(Axis.YP.rotationDegrees(headRot - bodyRot));
        }
    }
}