package com.littlh.palelullaby;

import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.effect.MobEffectCategory;

/**
 * 枯竭之触：枯血鬼近战命中后附加的标记效果，持续 3 秒。
 * 持有者受到的直接治疗效果减半（由 PaleLullabyForgeEvents#onLivingHeal 处理）。
 */
public class WitheringTouchEffect extends MobEffect {
    public WitheringTouchEffect() {
        super(MobEffectCategory.HARMFUL, 0x5A4A32);
    }
}
