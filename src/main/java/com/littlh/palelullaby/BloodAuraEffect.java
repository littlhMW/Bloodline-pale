package com.littlh.palelullaby;

import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.effect.MobEffectCategory;

/** 吸血光环：攻击命中时恢复造成伤害 25% 的生命（由事件实现）。 */
public class BloodAuraEffect extends MobEffect {
    public BloodAuraEffect() {
        super(MobEffectCategory.BENEFICIAL, 0xC41E3A);
    }

    @Override
    public boolean shouldApplyEffectTickThisTick(int duration, int amplifier) {
        return false;
    }
}
