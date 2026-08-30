package com.littlh.palelullaby;

import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.effect.MobEffectCategory;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.monster.AbstractIllager;
import net.minecraft.world.entity.npc.Villager;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;

/**
 * 血疾：被吸血鬼近战攻击有概率获得。
 * 视野严重发红 + 恶心；周围有村民/掠夺者/玩家时，有概率不受控制地靠近并攻击一次。
 */
public class BloodFrenzyEffect extends MobEffect {
    private static final double TRIGGER_CHANCE = 0.12D;
    private static final double TARGET_RANGE = 20.0D;
    private static final double DRAG_SPEED = 0.38D;
    private static final double ATTACK_RANGE_SQ = 2.8D * 2.8D;
    private static final int EPISODE_TICKS = 100;

    private static final ConcurrentHashMap<UUID, FrenzyState> FRENZY = new ConcurrentHashMap<>();

    private record FrenzyState(int targetId, int remaining, boolean attacked) {
    }

    public BloodFrenzyEffect() {
        super(MobEffectCategory.HARMFUL, 0x8B0000);
    }

    @Override
    public boolean shouldApplyEffectTickThisTick(int duration, int amplifier) {
        return duration > 0 && duration % 2 == 0;
    }

    @Override
    public boolean applyEffectTick(LivingEntity entity, int amplifier) {
        if (!entity.level().isClientSide) {
            if (entity.getEffect(MobEffects.CONFUSION) == null) {
                entity.addEffect(new MobEffectInstance(MobEffects.CONFUSION, 20 * 8, 0, false, false, true));
            }
            // 阳光下：缓慢 + 类僵尸燃烧
            if (isExposedToSun(entity)) {
                if (entity.getEffect(MobEffects.MOVEMENT_SLOWDOWN) == null) {
                    entity.addEffect(new MobEffectInstance(MobEffects.MOVEMENT_SLOWDOWN, 20 * 6, 0, false, false, true));
                }
                float light = entity.getLightLevelDependentMagicValue();
                if (entity.getRandom().nextFloat() * 30.0F < (light - 0.4F) * 2.0F) {
                    entity.igniteForSeconds(8.0F);
                }
            }
            // 白天在水里受到伤害
            Level level = entity.level();
            if (level.isDay() && entity.isInWater() && entity.tickCount % 20 == 0) {
                entity.hurt(level.damageSources().magic(), 2.0F);
            }
            tickFrenzy(entity);
        }
        return true;
    }

    /** 是否暴露在阳光下（类似僵尸日晒判定，水里/雨里不算）。 */
    private static boolean isExposedToSun(LivingEntity entity) {
        Level level = entity.level();
        if (!level.isDay() || level.isRaining()) {
            return false;
        }
        BlockPos pos = BlockPos.containing(entity.getX(), entity.getEyeY(), entity.getZ());
        if (entity.isInWaterRainOrBubble() || entity.isInPowderSnow) {
            return false;
        }
        return entity.getLightLevelDependentMagicValue() > 0.5F && level.canSeeSky(pos);
    }

    private static void tickFrenzy(LivingEntity entity) {
        UUID id = entity.getUUID();
        FrenzyState state = FRENZY.get(id);
        if (state == null) {
            if (entity.getRandom().nextDouble() < TRIGGER_CHANCE) {
                LivingEntity target = findTarget(entity);
                if (target != null) {
                    FRENZY.put(id, new FrenzyState(target.getId(), EPISODE_TICKS, false));
                }
            }
            return;
        }
        Entity target = entity.level().getEntity(state.targetId());
        if (!(target instanceof LivingEntity living) || !living.isAlive() || entity.distanceToSqr(living) > 1600.0D) {
            FRENZY.remove(id);
            return;
        }
        int remaining = state.remaining() - 1;
        if (remaining <= 0) {
            FRENZY.remove(id);
            return;
        }
        if (entity instanceof Player player) {
            if (!state.attacked() && entity.distanceToSqr(living) <= ATTACK_RANGE_SQ) {
                player.resetAttackStrengthTicker();
                player.attack(living);
                player.swing(InteractionHand.MAIN_HAND, true); // 让血疾玩家本人也能看到攻击动画
                if (player.level() instanceof ServerLevel serverLevel) {
                    serverLevel.sendParticles(ParticleTypes.SWEEP_ATTACK,
                            living.getX(), living.getY() + living.getBbHeight() * 0.5D, living.getZ(),
                            1, 0.0D, 0.0D, 0.0D, 0.0D);
                }
                FRENZY.put(id, new FrenzyState(state.targetId(), 20, true));
                return;
            }
            Vec3 to = living.position().subtract(player.position());
            double horiz = Math.sqrt(to.x * to.x + to.z * to.z);
            if (horiz > 0.001D) {
                Vec3 vel = player.getDeltaMovement();
                player.setDeltaMovement(to.x / horiz * DRAG_SPEED, vel.y, to.z / horiz * DRAG_SPEED);
            }
        }
        FRENZY.put(id, new FrenzyState(state.targetId(), remaining, state.attacked()));
    }

    private static LivingEntity findTarget(LivingEntity entity) {
        Level level = entity.level();
        double best = TARGET_RANGE * TARGET_RANGE;
        LivingEntity found = null;
        for (Entity e : level.getEntitiesOfClass(Entity.class, entity.getBoundingBox().inflate(TARGET_RANGE),
                e -> e instanceof Villager || e instanceof AbstractIllager
                        || (e instanceof Player p && !p.isSpectator()))) {
            if (e == entity) {
                continue;
            }
            double d = entity.distanceToSqr(e);
            if (d < best) {
                best = d;
                found = (LivingEntity) e;
            }
        }
        return found;
    }
}
