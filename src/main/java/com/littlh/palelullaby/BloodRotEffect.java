package com.littlh.palelullaby;

import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.effect.MobEffectCategory;
import net.minecraft.world.entity.LivingEntity;

/** 腐血：持续流失生命（不致死），受到的直接治疗效果减半。 */
public class BloodRotEffect extends MobEffect {
    public BloodRotEffect() {
        super(MobEffectCategory.HARMFUL, 0x5A1A2A);
    }

    @Override
    public boolean shouldApplyEffectTickThisTick(int duration, int amplifier) {
        return duration > 0 && duration % 40 == 0;
    }

    @Override
    public boolean applyEffectTick(LivingEntity entity, int amplifier) {
        if (!entity.level().isClientSide && entity.getHealth() > 2.0F) {
            entity.hurt(entity.level().damageSources().magic(), 1.0F);
        }
        return true;
    }
}
