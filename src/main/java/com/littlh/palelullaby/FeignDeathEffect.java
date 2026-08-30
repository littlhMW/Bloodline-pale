package com.littlh.palelullaby;

import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.effect.MobEffectCategory;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.ai.attributes.Attributes;

/** 假死：深度昏迷，几乎无法移动、无法攻击；配合隐形让掠食者以为你已死。 */
public class FeignDeathEffect extends MobEffect {
    private static final ResourceLocation SPEED =
            ResourceLocation.fromNamespaceAndPath(PaleLullaby.MOD_ID, "feign_death_speed");
    private static final ResourceLocation DAMAGE =
            ResourceLocation.fromNamespaceAndPath(PaleLullaby.MOD_ID, "feign_death_damage");

    public FeignDeathEffect() {
        super(MobEffectCategory.NEUTRAL, 0xE8E2D2);
        this.addAttributeModifier(Attributes.MOVEMENT_SPEED, SPEED,
                -0.85D, AttributeModifier.Operation.ADD_MULTIPLIED_TOTAL);
        this.addAttributeModifier(Attributes.ATTACK_DAMAGE, DAMAGE,
                -1.0D, AttributeModifier.Operation.ADD_MULTIPLIED_TOTAL);
    }

    @Override
    public boolean shouldApplyEffectTickThisTick(int duration, int amplifier) {
        return false;
    }
}
