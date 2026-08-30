package com.littlh.palelullaby;

import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.effect.MobEffectCategory;
import net.minecraft.world.entity.LivingEntity;

/** 蔷薇蜜意：缓慢恢复生命；期间抑制渴血的恶心/视野变红，喝血升级概率减半。 */
public class RoseNectarEffect extends MobEffect {
    public RoseNectarEffect() {
        super(MobEffectCategory.BENEFICIAL, 0xFF8FA8);
    }

    @Override
    public boolean shouldApplyEffectTickThisTick(int duration, int amplifier) {
        return duration > 0 && duration % 40 == 0;
    }

    @Override
    public boolean applyEffectTick(LivingEntity entity, int amplifier) {
        if (!entity.level().isClientSide) {
            entity.heal(1.0F);
        }
        return true;
    }
}
