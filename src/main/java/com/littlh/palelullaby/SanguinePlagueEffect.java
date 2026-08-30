package com.littlh.palelullaby;

import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.effect.MobEffectCategory;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.attributes.AttributeMap;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.ai.attributes.Attributes;

/**
 * 血疫：持续 6 秒，每秒 1~2 点魔法伤害，期间移动速度降低 15%。
 * 猎人的净化药剂可以解除。
 */
public class SanguinePlagueEffect extends MobEffect {
    private static final ResourceLocation SLOW_MODIFIER =
            ResourceLocation.fromNamespaceAndPath(PaleLullaby.MOD_ID, "sanguine_plague_slow");

    public SanguinePlagueEffect() {
        super(MobEffectCategory.HARMFUL, 0x4A0A1A);
    }

    @Override
    public boolean shouldApplyEffectTickThisTick(int duration, int amplifier) {
        return duration > 0 && duration % 20 == 0;
    }

    @Override
    public boolean applyEffectTick(LivingEntity entity, int amplifier) {
        if (!entity.level().isClientSide) {
            float dmg = 1.0F + entity.getRandom().nextInt(2);
            entity.hurt(entity.level().damageSources().magic(), dmg);
        }
        return true;
    }

    @Override
    public void addAttributeModifiers(AttributeMap attributes, int amplifier) {
        attributes.getInstance(Attributes.MOVEMENT_SPEED)
                .addPermanentModifier(new AttributeModifier(SLOW_MODIFIER, -0.15D, AttributeModifier.Operation.ADD_MULTIPLIED_TOTAL));
    }

    @Override
    public void removeAttributeModifiers(AttributeMap attributes) {
        var inst = attributes.getInstance(Attributes.MOVEMENT_SPEED);
        if (inst != null) {
            inst.removeModifier(SLOW_MODIFIER);
        }
    }
}
