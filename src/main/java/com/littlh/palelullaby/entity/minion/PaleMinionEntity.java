package com.littlh.palelullaby.entity.minion;

import com.littlh.palelullaby.entity.MullandEntity;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.ai.goal.*;
import net.minecraft.world.entity.ai.goal.target.NearestAttackableTargetGoal;
import net.minecraft.world.entity.monster.Monster;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import software.bernie.geckolib.animatable.GeoEntity;
import software.bernie.geckolib.animatable.instance.AnimatableInstanceCache;
import software.bernie.geckolib.animation.*;
import software.bernie.geckolib.util.GeckoLibUtil;

public class PaleMinionEntity extends Monster implements GeoEntity {

    private final AnimatableInstanceCache cache = GeckoLibUtil.createInstanceCache(this);

    private static final RawAnimation ANIM_CRAWL = RawAnimation.begin().thenLoop("animation.pale_minion.crawl");
    private static final RawAnimation ANIM_ATTACK = RawAnimation.begin().thenPlay("animation.pale_minion.attack");
    private static final RawAnimation ANIM_EXPLODE = RawAnimation.begin().thenPlay("animation.pale_minion.explode");

    private int explodeTimer = -1;
    private boolean isAttacking = false;

    public PaleMinionEntity(EntityType<? extends Monster> entityType, Level level) {
        super(entityType, level);
        this.xpReward = 3;
    }

    @Override
    protected void registerGoals() {
        this.goalSelector.addGoal(0, new FloatGoal(this));
        this.goalSelector.addGoal(1, new PaleMinionMeleeAttackGoal(this, 1.8D, true));
        this.goalSelector.addGoal(2, new WaterAvoidingRandomStrollGoal(this, 1.0D));
        this.goalSelector.addGoal(3, new LookAtPlayerGoal(this, Player.class, 8.0F));
        this.goalSelector.addGoal(4, new RandomLookAroundGoal(this));

        this.targetSelector.addGoal(1, new NearestAttackableTargetGoal<>(this, Player.class, true));
        this.targetSelector.addGoal(2, new NearestAttackableTargetGoal<>(this, LivingEntity.class, 10,
                true, false, e -> e instanceof Mob mob && mob instanceof Monster
                        && !(mob instanceof PaleMinionEntity) && !(mob instanceof MullandEntity)));
    }

    public static AttributeSupplier.Builder createAttributes() {
        return Monster.createMonsterAttributes()
                .add(Attributes.MAX_HEALTH, 20.0D)
                .add(Attributes.MOVEMENT_SPEED, 0.55D)
                .add(Attributes.ATTACK_DAMAGE, 6.0D)
                .add(Attributes.FOLLOW_RANGE, 40.0D)
                .add(Attributes.ARMOR, 2.0D);
    }

    // ==================== GeckoLib ====================
    @Override
    public void registerControllers(AnimatableManager.ControllerRegistrar controllers) {
        controllers.add(new AnimationController<>(this, "main_controller", 0, this::mainController));
    }

    private PlayState mainController(AnimationState<PaleMinionEntity> state) {
        if (explodeTimer >= 0) {
            return state.setAndContinue(ANIM_EXPLODE);
        }
        if (isAttacking) {
            return state.setAndContinue(ANIM_ATTACK);
        }
        return state.setAndContinue(ANIM_CRAWL);
    }

    @Override
    public AnimatableInstanceCache getAnimatableInstanceCache() {
        return this.cache;
    }

    // ==================== Custom Melee Goal (track attack state) ====================
    private static class PaleMinionMeleeAttackGoal extends MeleeAttackGoal {
        private final PaleMinionEntity minion;

        public PaleMinionMeleeAttackGoal(PaleMinionEntity minion, double speedModifier, boolean followingTargetEvenIfNotSeen) {
            super(minion, speedModifier, followingTargetEvenIfNotSeen);
            this.minion = minion;
        }

        @Override
        protected void checkAndPerformAttack(LivingEntity target) {
            if (this.canPerformAttack(target)) {
                this.resetAttackCooldown();
                this.minion.doHurtTarget(target);
                this.minion.isAttacking = true;
            } else {
                this.minion.isAttacking = false;
            }
        }

        @Override
        public void stop() {
            super.stop();
            this.minion.isAttacking = false;
        }
    }

    // ==================== Sounds ====================
    @Override
    protected SoundEvent getAmbientSound() { return SoundEvents.VEX_AMBIENT; }
    @Override
    protected SoundEvent getHurtSound(DamageSource source) { return SoundEvents.VEX_HURT; }
    @Override
    protected SoundEvent getDeathSound() { return SoundEvents.VEX_DEATH; }

    // ==================== Self-Destruct at Low HP ====================
    @Override
    public void tick() {
        super.tick();

        if (this.level().isClientSide) {
            return;
        }

        float healthPercent = this.getHealth() / this.getMaxHealth();
        if (explodeTimer < 0 && healthPercent <= 0.3F && healthPercent > 0) {
            startExplosion();
        }

        if (explodeTimer > 0) {
            explodeTimer--;
        } else if (explodeTimer == 0) {
            performExplosion();
        }

        if (explodeTimer > 0 && explodeTimer % 5 == 0) {
            this.level().broadcastEntityEvent(this, (byte) 10);
        }
    }

    private void startExplosion() {
        explodeTimer = 86;
        this.setNoAi(true);
        if (!this.level().isClientSide) {
            this.level().broadcastEntityEvent(this, (byte) 10);
        }
    }

    private void performExplosion() {
        this.level().explode(this, this.getX(), this.getY(), this.getZ(), 2.5F, Level.ExplosionInteraction.MOB);
        this.hurt(this.damageSources().generic(), Float.MAX_VALUE);
    }

    @Override
    public void handleEntityEvent(byte id) {
        if (id == 10) {
            if (this.level().isClientSide) {
                for (int i = 0; i < 5; i++) {
                    this.level().addParticle(
                            net.minecraft.core.particles.ParticleTypes.SMOKE,
                            this.getX() + (this.random.nextDouble() - 0.5) * 1.5,
                            this.getY() + this.random.nextDouble() * 1.5,
                            this.getZ() + (this.random.nextDouble() - 0.5) * 1.5,
                            0, 0.05, 0);
                }
            }
        } else {
            super.handleEntityEvent(id);
        }
    }
}
