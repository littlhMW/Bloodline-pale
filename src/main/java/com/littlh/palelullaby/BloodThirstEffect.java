package com.littlh.palelullaby;

import com.littlh.palelullaby.network.BloodThirstSuppressPayload;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.effect.MobEffectCategory;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.attributes.AttributeInstance;
import net.minecraft.world.entity.ai.attributes.AttributeMap;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.level.Level;
import net.neoforged.neoforge.network.PacketDistributor;

/**
 * 渴血：喝下血液有概率获得，随时间可能升级/小概率降级，3级后可能恶化为血疾。
 * 1级：速度略微加快+恶心；2级：额外视野略微变红+伤害略微提升；
 * 3级：以上全部+阳光下缓慢。喝血可暂时抑制恶心与视野变红，但有概率提升等级。
 */
public class BloodThirstEffect extends MobEffect {
    private static final ResourceLocation SPEED_MODIFIER =
            ResourceLocation.fromNamespaceAndPath(PaleLullaby.MOD_ID, "blood_thirst_speed");
    private static final ResourceLocation DAMAGE_MODIFIER =
            ResourceLocation.fromNamespaceAndPath(PaleLullaby.MOD_ID, "blood_thirst_damage");

    private static final double GAIN_CHANCE = 0.25D;
    private static final double UPGRADE_CHANCE = 0.04D;
    private static final double DOWNGRADE_CHANCE = 0.015D;
    private static final double ESCALATE_CHANCE = 0.02D;
    private static final double DRINK_UPGRADE_CHANCE = 0.45D;
    private static final int SUPPRESS_TICKS = 20 * 30;

    /** 玩家UUID -> 症状被抑制到的游戏时间（服务端）。 */
    private static final ConcurrentHashMap<UUID, Long> SUPPRESS_UNTIL = new ConcurrentHashMap<>();

    public BloodThirstEffect() {
        super(MobEffectCategory.HARMFUL, 0x7A1A3A);
    }

    public static int tierDuration(int amplifier) {
        return switch (amplifier) {
            case 0 -> 20 * 60;
            case 1 -> 20 * 90;
            default -> 20 * 120;
        };
    }

    /** 喝下血液后的处理：有概率获得渴血；已有渴血则暂时抑制症状，并可能提升等级。 */
    public static void onDrinkBlood(ServerPlayer player) {
        MobEffectInstance thirst = player.getEffect(PaleLullabyEffects.BLOOD_THIRST);
        if (thirst == null) {
            if (player.getRandom().nextDouble() < GAIN_CHANCE) {
                player.addEffect(new MobEffectInstance(PaleLullabyEffects.BLOOD_THIRST,
                        tierDuration(0), 0, false, true, true));
            }
            return;
        }
        suppress(player, player.level().getGameTime() + SUPPRESS_TICKS);
        double upgradeChance = DRINK_UPGRADE_CHANCE;
        if (player.hasEffect(PaleLullabyEffects.ROSE_NECTAR)) {
            upgradeChance *= 0.5D;
        }
        if (player.getRandom().nextDouble() < upgradeChance) {
            int amp = thirst.getAmplifier();
            if (amp >= 2) {
                player.removeEffect(PaleLullabyEffects.BLOOD_THIRST);
                player.addEffect(new MobEffectInstance(PaleLullabyEffects.BLOOD_FRENZY,
                        -1, 0, false, true, true));
            } else {
                player.addEffect(new MobEffectInstance(PaleLullabyEffects.BLOOD_THIRST,
                        tierDuration(amp + 1), amp + 1, false, true, true));
            }
        }
    }

    /** 暂时抑制渴血的恶心与视野变红，并同步给客户端。 */
    public static void suppress(ServerPlayer player, long untilGameTime) {
        SUPPRESS_UNTIL.put(player.getUUID(), untilGameTime);
        player.removeEffect(MobEffects.CONFUSION);
        PacketDistributor.sendToPlayer(player,
                new BloodThirstSuppressPayload(player.getId(), untilGameTime));
    }

    public static boolean isSuppressed(LivingEntity entity) {
        Long until = SUPPRESS_UNTIL.get(entity.getUUID());
        return until != null && until > entity.level().getGameTime();
    }

    @Override
    public void addAttributeModifiers(AttributeMap attributemap, int amplifier) {
        AttributeInstance speed = attributemap.getInstance(Attributes.MOVEMENT_SPEED);
        if (speed != null) {
            speed.addTransientModifier(new AttributeModifier(SPEED_MODIFIER,
                    0.04D * (1 + amplifier), AttributeModifier.Operation.ADD_MULTIPLIED_TOTAL));
        }
        if (amplifier >= 1) {
            AttributeInstance damage = attributemap.getInstance(Attributes.ATTACK_DAMAGE);
            if (damage != null) {
                damage.addTransientModifier(new AttributeModifier(DAMAGE_MODIFIER,
                        1.0D + (amplifier - 1) * 0.5D, AttributeModifier.Operation.ADD_VALUE));
            }
        }
    }

    @Override
    public void removeAttributeModifiers(AttributeMap attributemap) {
        AttributeInstance speed = attributemap.getInstance(Attributes.MOVEMENT_SPEED);
        if (speed != null) {
            speed.removeModifier(SPEED_MODIFIER);
        }
        AttributeInstance damage = attributemap.getInstance(Attributes.ATTACK_DAMAGE);
        if (damage != null) {
            damage.removeModifier(DAMAGE_MODIFIER);
        }
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
        if (!isSuppressed(entity) && entity.getEffect(PaleLullabyEffects.ROSE_NECTAR) == null
                && entity.getEffect(MobEffects.CONFUSION) == null) {
            entity.addEffect(new MobEffectInstance(MobEffects.CONFUSION, 20 * 8, 0, false, false, true));
        }
        if (amplifier >= 2 && isInSun(entity) && entity.getEffect(MobEffects.MOVEMENT_SLOWDOWN) == null) {
            entity.addEffect(new MobEffectInstance(MobEffects.MOVEMENT_SLOWDOWN, 20 * 6, 0, false, false, true));
        }
        tickLevelChange(entity, amplifier);
        return true;
    }

    private static void tickLevelChange(LivingEntity entity, int amplifier) {
        double roll = entity.getRandom().nextDouble();
        if (amplifier >= 2) {
            if (roll < ESCALATE_CHANCE) {
                entity.removeEffect(PaleLullabyEffects.BLOOD_THIRST);
                entity.addEffect(new MobEffectInstance(PaleLullabyEffects.BLOOD_FRENZY,
                        -1, 0, false, true, true));
            } else if (roll < ESCALATE_CHANCE + DOWNGRADE_CHANCE) {
                entity.addEffect(new MobEffectInstance(PaleLullabyEffects.BLOOD_THIRST,
                        tierDuration(1), 1, false, true, true));
            }
            return;
        }
        if (roll < UPGRADE_CHANCE) {
            entity.addEffect(new MobEffectInstance(PaleLullabyEffects.BLOOD_THIRST,
                    tierDuration(amplifier + 1), amplifier + 1, false, true, true));
        } else if (amplifier > 0 && roll < UPGRADE_CHANCE + DOWNGRADE_CHANCE) {
            entity.addEffect(new MobEffectInstance(PaleLullabyEffects.BLOOD_THIRST,
                    tierDuration(amplifier - 1), amplifier - 1, false, true, true));
        }
    }

    private static boolean isInSun(LivingEntity entity) {
        Level level = entity.level();
        return level.isDay() && !level.isRaining() && level.canSeeSky(entity.blockPosition());
    }
}
