package com.littlh.palelullaby;

import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.effect.MobEffectCategory;

/** 荆棘之肤：受到近战攻击时反弹 3 点伤害，并消耗自身装备耐久（由事件实现）。 */
public class ThornSkinEffect extends MobEffect {
    public ThornSkinEffect() {
        super(MobEffectCategory.BENEFICIAL, 0x4A7A3A);
    }

    @Override
    public boolean shouldApplyEffectTickThisTick(int duration, int amplifier) {
        return false;
    }
}
