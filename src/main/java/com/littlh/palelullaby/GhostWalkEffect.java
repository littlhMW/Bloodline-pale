package com.littlh.palelullaby;

import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.effect.MobEffectCategory;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.LivingEntity;

/** 幽体漫步：身体半透明、接近无声，怪物索敌范围大幅缩小（依托隐身）。 */
public class GhostWalkEffect extends MobEffect {
    public GhostWalkEffect() {
        super(MobEffectCategory.BENEFICIAL, 0xAAD4FF);
    }

    @Override
    public boolean shouldApplyEffectTickThisTick(int duration, int amplifier) {
        return duration > 0 && duration % 20 == 0;
    }

    @Override
    public boolean applyEffectTick(LivingEntity entity, int amplifier) {
        if (entity.level().isClientSide) {
            return true;
        }
        if (entity.getEffect(MobEffects.INVISIBILITY) == null
                || entity.getEffect(MobEffects.INVISIBILITY).getDuration() < 100) {
            entity.addEffect(new MobEffectInstance(MobEffects.INVISIBILITY, 220, 0, false, false, false));
        }
        return true;
    }
}
