package com.littlh.palelullaby.entity;

import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.world.damagesource.DamageSource;
import com.littlh.palelullaby.entity.ability.BloodAbility;
import com.littlh.palelullaby.PaleLullabyEffects;
import net.minecraft.core.particles.DustParticleOptions;
import java.util.List;
import org.joml.Vector3f;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.ai.goal.FloatGoal;
import net.minecraft.world.entity.ai.goal.LookAtPlayerGoal;
import net.minecraft.world.entity.ai.goal.MeleeAttackGoal;
import net.minecraft.world.entity.ai.goal.RandomLookAroundGoal;
import net.minecraft.world.entity.ai.goal.WaterAvoidingRandomStrollGoal;
import net.minecraft.world.entity.ai.goal.target.HurtByTargetGoal;
import net.minecraft.world.entity.ai.goal.target.NearestAttackableTargetGoal;
import net.minecraft.world.entity.monster.Monster;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import software.bernie.geckolib.animatable.GeoEntity;
import software.bernie.geckolib.animatable.instance.AnimatableInstanceCache;
import software.bernie.geckolib.animation.AnimatableManager;
import software.bernie.geckolib.animation.AnimationController;
import software.bernie.geckolib.animation.PlayState;
import software.bernie.geckolib.animation.RawAnimation;
import software.bernie.geckolib.util.GeckoLibUtil;

/**
 * 枯血鬼：被欲望扭曲的兽类吸血鬼，使用 ghoul_crawler 美术资产。
 */
public class DriedBloodGhostEntity extends AbstractVampireEntity implements GeoEntity {
    private static final EntityDataAccessor<Boolean> DATA_IS_ATTACKING =
            SynchedEntityData.defineId(DriedBloodGhostEntity.class, EntityDataSerializers.BOOLEAN);

    private static final RawAnimation ANIM_WALK = RawAnimation.begin().thenLoop("animation.ghoul_crawler.walk");
    private static final RawAnimation ANIM_RUN = RawAnimation.begin().thenLoop("animation.ghoul_crawler.run");
    private static final RawAnimation ANIM_ATTACK = RawAnimation.begin().thenPlay("animation.ghoul_crawler.attack");
    private static final RawAnimation ANIM_LEAP_BITE = RawAnimation.begin().thenPlay("animation.ghoul_crawler.leap_bite");

    private final AnimatableInstanceCache cache = GeckoLibUtil.createInstanceCache(this);
    private int attackTimer = 0;

    public DriedBloodGhostEntity(EntityType<? extends Monster> entityType, Level level) {
        super(entityType, level);
        this.xpReward = 6;
    }

    public static AttributeSupplier.Builder createAttributes() {
        return Monster.createMonsterAttributes()
                .add(Attributes.MAX_HEALTH, 24.0D)
                .add(Attributes.MOVEMENT_SPEED, 0.38D)
                .add(Attributes.ATTACK_DAMAGE, 7.0D)
                .add(Attributes.FOLLOW_RANGE, 40.0D)
                .add(Attributes.ARMOR, 0.0D)
                .add(Attributes.ARMOR_TOUGHNESS, 0.0D);
    }

    @Override
    protected void defineSynchedData(SynchedEntityData.Builder builder) {
        super.defineSynchedData(builder);
        builder.define(DATA_IS_ATTACKING, false);
    }

    @Override
    protected void registerGoals() {
        this.goalSelector.addGoal(0, new AbstractVampireEntity.FleeSunAndWaterGoal(this));
        this.goalSelector.addGoal(1, new FloatGoal(this));
        this.goalSelector.addGoal(2, new GhoulMeleeAttackGoal(this, 1.3D, true));
        this.goalSelector.addGoal(3, new WaterAvoidingRandomStrollGoal(this, 1.0D));
        this.goalSelector.addGoal(4, new LookAtPlayerGoal(this, Player.class, 8.0F));
        this.goalSelector.addGoal(5, new RandomLookAroundGoal(this));
        FactionTargets.register(this, PaleLullabyFactions.MAD_VAMPIRE);
    }

    @Override
    public void registerControllers(AnimatableManager.ControllerRegistrar controllers) {
        controllers.add(new AnimationController<>(this, "main_controller", 0, state -> {
            if (this.entityData.get(DATA_IS_ATTACKING)) {
                return state.setAndContinue(ANIM_ATTACK);
            }
            return state.setAndContinue(this.walkAnimation.speed() > 0.4F ? ANIM_RUN : ANIM_WALK);
        }));
    }

    @Override
    public AnimatableInstanceCache getAnimatableInstanceCache() { return this.cache; }

    @Override
    public void tick() {
        super.tick();
        if (this.attackTimer > 0) {
            this.attackTimer--;
            if (this.attackTimer == 0) this.entityData.set(DATA_IS_ATTACKING, false);
        }
        // 枯黄体表粒子 + 移动轨迹。
        if (this.level() instanceof net.minecraft.server.level.ServerLevel serverLevel) {
            if (this.random.nextInt(20) == 0) {
                serverLevel.sendParticles(new DustParticleOptions(new Vector3f(0.6F, 0.5F, 0.2F), 1.0F),
                        this.getX() + this.random.nextGaussian() * 0.4D,
                        this.getY() + this.random.nextDouble() * this.getBbHeight(),
                        this.getZ() + this.random.nextGaussian() * 0.4D,
                        1, 0, 0, 0, 0);
            }
            if (this.walkAnimation.speed() > 0.4F && this.random.nextInt(10) == 0) {
                serverLevel.sendParticles(new DustParticleOptions(new Vector3f(0.6F, 0.5F, 0.2F), 0.7F),
                        this.getX(), this.getY() + 0.1D, this.getZ(),
                        1, 0, 0, 0, 0);
            }
        }
    }

    @Override
    public List<BloodAbility> abilities() {
        return List.of();
    }

    public void startAttackAnimation() {
        this.entityData.set(DATA_IS_ATTACKING, true);
        this.attackTimer = 16;
    }

    private static class GhoulMeleeAttackGoal extends MeleeAttackGoal {
        private final DriedBloodGhostEntity ghoul;

        public GhoulMeleeAttackGoal(DriedBloodGhostEntity ghoul, double speedModifier, boolean followingTargetEvenIfNotSeen) {
            super(ghoul, speedModifier, followingTargetEvenIfNotSeen);
            this.ghoul = ghoul;
        }

        @Override
        protected void checkAndPerformAttack(net.minecraft.world.entity.LivingEntity target) {
            if (this.canPerformAttack(target)) {
                this.resetAttackCooldown();
                this.ghoul.doHurtTarget(target);
                this.ghoul.startAttackAnimation();
                // 枯竭之触：目标治疗减半 3 秒。
                target.addEffect(new net.minecraft.world.effect.MobEffectInstance(PaleLullabyEffects.WITHERING_TOUCH, 60, 0));
            }
        }
    }

    @Override
    protected SoundEvent getAmbientSound() { return SoundEvents.WOLF_GROWL; }
    @Override
    protected SoundEvent getHurtSound(DamageSource source) { return SoundEvents.WOLF_HURT; }
    @Override
    protected SoundEvent getDeathSound() { return SoundEvents.WOLF_DEATH; }
}
