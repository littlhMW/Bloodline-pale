package com.littlh.palelullaby.entity.minion;

import com.littlh.palelullaby.entity.MullandEntity;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
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

    // ==================== 状态同步 (解决客户端不播动画的问题) ====================
    private static final EntityDataAccessor<Boolean> DATA_IS_EXPLODING = 
            SynchedEntityData.defineId(PaleMinionEntity.class, EntityDataSerializers.BOOLEAN);
    private static final EntityDataAccessor<Boolean> DATA_IS_ATTACKING = 
            SynchedEntityData.defineId(PaleMinionEntity.class, EntityDataSerializers.BOOLEAN);

    private final AnimatableInstanceCache cache = GeckoLibUtil.createInstanceCache(this);

    private static final RawAnimation ANIM_CRAWL = RawAnimation.begin().thenLoop("animation.pale_minion.crawl");
    private static final RawAnimation ANIM_ATTACK = RawAnimation.begin().thenPlay("animation.pale_minion.attack");
    private static final RawAnimation ANIM_EXPLODE = RawAnimation.begin().thenPlay("animation.pale_minion.explode");

    private int explodeTimer = -1;
    private int attackTimer = 0; // 用于保证攻击动画完整播放的计时器

    public PaleMinionEntity(EntityType<? extends Monster> entityType, Level level) {
        super(entityType, level);
        this.xpReward = 3;
    }

    @Override
    protected void defineSynchedData(SynchedEntityData.Builder builder) {
        super.defineSynchedData(builder);
        builder.define(DATA_IS_EXPLODING, false);
        builder.define(DATA_IS_ATTACKING, false);
    }

    @Override
    protected void registerGoals() {
        this.goalSelector.addGoal(0, new FloatGoal(this));
        // 【修复】追击倍率从 1.8D 下调为 1.2D，更加符合正常怪物追击逻辑
        this.goalSelector.addGoal(1, new PaleMinionMeleeAttackGoal(this, 1.2D, true));
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
                // 【修复】基础移速从 0.9D 改为 0.3D (比僵尸快一点的敏捷小怪)
                .add(Attributes.MOVEMENT_SPEED, 0.3D)
                .add(Attributes.ATTACK_DAMAGE, 6.0D)
                .add(Attributes.FOLLOW_RANGE, 40.0D)
                .add(Attributes.ARMOR, 2.0D);
    }

    // ==================== GeckoLib 动画控制 ====================
    @Override
    public void registerControllers(AnimatableManager.ControllerRegistrar controllers) {
        controllers.add(new AnimationController<>(this, "main_controller", 0, this::mainController));
    }

    private PlayState mainController(AnimationState<PaleMinionEntity> state) {
        // 读取同步到客户端的数据
        if (this.entityData.get(DATA_IS_EXPLODING)) {
            return state.setAndContinue(ANIM_EXPLODE);
        }
        if (this.entityData.get(DATA_IS_ATTACKING)) {
            return state.setAndContinue(ANIM_ATTACK);
        }
        return state.setAndContinue(ANIM_CRAWL);
    }

    @Override
    public AnimatableInstanceCache getAnimatableInstanceCache() {
        return this.cache;
    }

    // ==================== 自定义近战攻击目标 (保证动画播放) ====================
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
                // 触发攻击动作计时，而不是只闪过1帧
                this.minion.startAttackAnimation(); 
            }
        }
    }

    public void startAttackAnimation() {
        this.entityData.set(DATA_IS_ATTACKING, true);
        // 你的攻击动画长度是 0.8秒 = 16 ticks，强行保持16ticks的攻击状态
        this.attackTimer = 16; 
    }

    // ==================== Sounds ====================
    @Override
    protected SoundEvent getAmbientSound() { return SoundEvents.VEX_AMBIENT; }
    @Override
    protected SoundEvent getHurtSound(DamageSource source) { return SoundEvents.VEX_HURT; }
    @Override
    protected SoundEvent getDeathSound() { return SoundEvents.VEX_DEATH; }

    // ==================== Tick 核心逻辑 ====================
    @Override
    public void tick() {
        super.tick();

        if (this.level().isClientSide) return; // 客户端只负责渲染和表现

        // 控制攻击动画还原
        if (this.attackTimer > 0) {
            this.attackTimer--;
            if (this.attackTimer == 0) {
                this.entityData.set(DATA_IS_ATTACKING, false);
            }
        }

        // 血量少于 30% 开始自爆
        float healthPercent = this.getHealth() / this.getMaxHealth();
        if (explodeTimer < 0 && healthPercent <= 0.3F && healthPercent > 0) {
            startExplosion();
        }

        if (explodeTimer > 0) {
            explodeTimer--;
            // 自爆时强制在原地停留，不让它一边爆炸一边跑
            this.setDeltaMovement(0, this.getDeltaMovement().y, 0); 
            // 粒子特效
            if (explodeTimer % 5 == 0) {
                this.level().broadcastEntityEvent(this, (byte) 10);
            }
        } else if (explodeTimer == 0) {
            performExplosion();
        }
    }

    private void startExplosion() {
        // 同步给客户端告诉它：我开始爆炸了，播放动画！
        this.entityData.set(DATA_IS_EXPLODING, true);
        this.explodeTimer = 86; // 动画长度 4.2857秒 = 85.7 ticks
        
        // 停止移动、清空仇恨
        this.getNavigation().stop();
        this.setTarget(null);
    }

    private void performExplosion() {
        // -1 防止重复触发
        this.explodeTimer = -1;
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