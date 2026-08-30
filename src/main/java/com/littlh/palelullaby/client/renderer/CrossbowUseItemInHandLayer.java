package com.littlh.palelullaby.client.renderer;

import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.model.ArmedModel;
import net.minecraft.client.model.EntityModel;
import net.minecraft.client.renderer.ItemInHandRenderer;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.entity.RenderLayerParent;
import net.minecraft.client.renderer.entity.layers.ItemInHandLayer;
import net.minecraft.world.entity.HumanoidArm;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.CrossbowItem;
import net.minecraft.world.item.ItemDisplayContext;
import net.minecraft.world.item.ItemStack;

/**
 * ItemInHandLayer variant for mobs that hold a crossbow in the offhand (the
 * blood hunter). While the crossbow is being charged both hands are on the
 * weapon, so the main-hand sword is hidden instead of rendering on the drawing
 * arm, which previously read as a stray third hand.
 */
public class CrossbowUseItemInHandLayer<T extends LivingEntity, M extends EntityModel<T> & ArmedModel>
        extends ItemInHandLayer<T, M> {

    public CrossbowUseItemInHandLayer(RenderLayerParent<T, M> renderer, ItemInHandRenderer itemInHandRenderer) {
        super(renderer, itemInHandRenderer);
    }

    @Override
    public void render(PoseStack poseStack, MultiBufferSource buffer, int packedLight, T livingEntity,
                       float limbSwing, float limbSwingAmount, float partialTicks, float ageInTicks,
                       float netHeadYaw, float headPitch) {
        if (livingEntity.isUsingItem() && livingEntity.getUseItem().getItem() instanceof CrossbowItem) {
            boolean rightHanded = livingEntity.getMainArm() == HumanoidArm.RIGHT;
            ItemStack crossbow = rightHanded ? livingEntity.getOffhandItem() : livingEntity.getMainHandItem();
            HumanoidArm arm = rightHanded ? HumanoidArm.LEFT : HumanoidArm.RIGHT;
            ItemDisplayContext displayContext = rightHanded
                    ? ItemDisplayContext.THIRD_PERSON_LEFT_HAND
                    : ItemDisplayContext.THIRD_PERSON_RIGHT_HAND;
            this.renderArmWithItem(livingEntity, crossbow, displayContext, arm, poseStack, buffer, packedLight);
            return;
        }
        super.render(poseStack, buffer, packedLight, livingEntity, limbSwing, limbSwingAmount,
                partialTicks, ageInTicks, netHeadYaw, headPitch);
    }
}
