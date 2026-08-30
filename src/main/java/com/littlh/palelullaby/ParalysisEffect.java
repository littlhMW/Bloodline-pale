package com.littlh.palelullaby;

import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.effect.MobEffectCategory;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.ai.attributes.Attributes;

/** 麻痹：虚弱之油的效果，肌肉松弛无力——移速与攻击力大幅下降。 */
public class ParalysisEffect extends MobEffect {
    private static final ResourceLocation SPEED =
            ResourceLocation.fromNamespaceAndPath(PaleLullaby.MOD_ID, "paralysis_speed");
    private static final ResourceLocation DAMAGE =
            ResourceLocation.fromNamespaceAndPath(PaleLullaby.MOD_ID, "paralysis_damage");

    public ParalysisEffect() {
        super(MobEffectCategory.HARMFUL, 0x6E6A66);
        this.addAttributeModifier(Attributes.MOVEMENT_SPEED, SPEED,
                -0.30D, AttributeModifier.Operation.ADD_MULTIPLIED_TOTAL);
        this.addAttributeModifier(Attributes.ATTACK_DAMAGE, DAMAGE,
                -0.40D, AttributeModifier.Operation.ADD_MULTIPLIED_TOTAL);
    }

    @Override
    public boolean shouldApplyEffectTickThisTick(int duration, int amplifier) {
        return false;
    }
}
