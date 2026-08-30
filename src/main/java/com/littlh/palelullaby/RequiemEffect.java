package com.littlh.palelullaby;

import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.effect.MobEffectCategory;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.level.Level;

/**
 * 安魂：身周生成雾气粒子。持有者不会被任何生物设为目标（见 PaleLullabyForgeEvents#onTargetChange），
 * 类似创造模式 AI 忽略。
 */
public class RequiemEffect extends MobEffect {
    public RequiemEffect() {
        super(MobEffectCategory.BENEFICIAL, 0xA8C8B0);
    }

    @Override
    public boolean shouldApplyEffectTickThisTick(int duration, int amplifier) {
        return duration > 0 && duration % 5 == 0;
    }

    @Override
    public boolean applyEffectTick(LivingEntity entity, int amplifier) {
        Level level = entity.level();
        if (level.isClientSide) {
            return true;
        }
        if (level instanceof ServerLevel serverLevel) {
            serverLevel.sendParticles(ParticleTypes.CAMPFIRE_COSY_SMOKE,
                    entity.getX() + (entity.getRandom().nextDouble() - 0.5D) * 3.0D,
                    entity.getY() + 0.5D + entity.getRandom().nextDouble() * 1.5D,
                    entity.getZ() + (entity.getRandom().nextDouble() - 0.5D) * 3.0D,
                    1, 0.1D, 0.1D, 0.1D, 0.01D);
        }
        return true;
    }
}
