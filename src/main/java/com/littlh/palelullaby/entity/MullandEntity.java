package com.littlh.palelullaby.entity;

import com.littlh.palelullaby.PaleLullaby;
import com.littlh.palelullaby.entity.minion.PaleMinionEntity;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.*;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.ai.goal.Goal;
import net.minecraft.world.entity.ai.goal.target.NearestAttackableTargetGoal;
import net.minecraft.world.entity.monster.Monster;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import software.bernie.geckolib.animatable.GeoEntity;
import software.bernie.geckolib.animatable.instance.AnimatableInstanceCache;
import software.bernie.geckolib.animation.*;
import software.bernie.geckolib.util.GeckoLibUtil;

import java.util.EnumSet;
import java.util.List;

public class MullandEntity extends Monster implements GeoEntity {

    private static final EntityDataAccessor<String> DATA_STATE =
            SynchedEntityData.defineId(MullandEntity.class, EntityDataSerializers.STRING);
    private static final EntityDataAccessor<Boolean> DATA_PHASE_TWO =
            SynchedEntityData.defineId(MullandEntity.class, EntityDataSerializers.BOOLEAN);
    private static final EntityDataAccessor<Integer> DATA_STATE_TIMER =
            SynchedEntityData.defineId(MullandEntity.class, EntityDataSerializers.INT);

    public enum BossState {
        HANGING_DORMANT, HANGING_WAKING, HANGING_IDLE,
        HANGING_SCREAM, HANGING_CURSE, HANGING_SUMMON,
        FALLING,
        GROUND_IDLE, CRAWLING, POUNCE, JUMP_BACK, GROUND_SCREAM
    }

    private static final RawAnimation ANIM_INACTIVE = RawAnimation.begin().thenLoop("animation.boss.hang_inactive");
    private static final RawAnimation ANIM_WAKEUP = RawAnimation.begin().thenPlay("animation.boss.hang_wakeup");
    private static final RawAnimation ANIM_HANG_IDLE = RawAnimation.begin().thenLoop("animation.boss.hang_idle");
    private static final RawAnimation ANIM_HANG_SCREAM = RawAnimation.begin().thenPlay("animation.boss.hang_attack");
    private static final RawAnimation ANIM_HANG_CURSE = RawAnimation.begin().thenPlay("animation.boss.hang_curse1");
    private static final RawAnimation ANIM_HANG_SUMMON = RawAnimation.begin().thenPlay("animation.boss.hang_summon");
    private static final RawAnimation ANIM_FALL = RawAnimation.begin().thenPlay("animation.boss.fall");
    private static final RawAnimation ANIM_GROUND_IDLE = RawAnimation.begin().thenLoop("animation.boss.ground_idle");
    private static final RawAnimation ANIM_CRAWL = RawAnimation.begin().thenLoop("animation.boss.ground_crawl");
    private static final RawAnimation ANIM_POUNCE = RawAnimation.begin().thenPlay("animation.boss.ground_attack");
    private static final RawAnimation ANIM_JUMP_BACK = RawAnimation.begin().thenPlay("animation.boss.ground_jump_back");
    private static final RawAnimation ANIM_GROUND_SCREAM = RawAnimation.begin().thenPlay("animation.boss.ground_scream_attack");

    private BossState currentState = BossState.HANGING_DORMANT;
    private int stateTimer = 0;
    private int pounceCount = 0;
    private int attackCooldown = 0;
    private int curseCooldown = 0;
    private int currentCurseVariant = 0;
    
    // 【修复1】记录初始始躯干旋转角度，一阶段彻底锁死身体，只让头动
    private float lockedBodyRot = -999.0f;
    
    // 二阶段战斗状态记录
    private int recentHits = 0;
    private long lastHitTime = 0;
    private int proximityTicks = 0;

    private final AnimatableInstanceCache cache = GeckoLibUtil.createInstanceCache(this);

    private static final double WAKE_RANGE = 40.0;
    private static final int MAX_POUNCES = 5;
    private static final int MIN_POUNCES = 3;

    public MullandEntity(EntityType<? extends Monster> entityType, Level level) {
        super(entityType, level);
        this.xpReward = 500;
        this.setNoGravity(true);
        this.noPhysics = false;
    }

    public static AttributeSupplier.Builder createAttributes() {
        return Monster.createMonsterAttributes()
                .add(Attributes.MAX_HEALTH, 300.0D)
                .add(Attributes.MOVEMENT_SPEED, 0.0D)
                .add(Attributes.ATTACK_DAMAGE, 10.0D)
                .add(Attributes.FOLLOW_RANGE, 64.0D)
                .add(Attributes.ARMOR, 4.0D)
                .add(Attributes.KNOCKBACK_RESISTANCE, 1.0D);
    }

    @Override
    protected void defineSynchedData(SynchedEntityData.Builder builder) {
        super.defineSynchedData(builder);
        builder.define(DATA_STATE, BossState.HANGING_DORMANT.name());
        builder.define(DATA_PHASE_TWO, false);
        builder.define(DATA_STATE_TIMER, 0);
    }

    @Override
    public void registerControllers(AnimatableManager.ControllerRegistrar controllers) {
        controllers.add(new AnimationController<>(this, "main_controller", 0, this::mainController));
    }

    private PlayState mainController(software.bernie.geckolib.animation.AnimationState<MullandEntity> state) {
        BossState bs = getCurrentState();
        return switch (bs) {
            case HANGING_DORMANT -> state.setAndContinue(ANIM_INACTIVE);
            case HANGING_WAKING -> state.setAndContinue(ANIM_WAKEUP);
            case HANGING_IDLE -> state.setAndContinue(ANIM_HANG_IDLE);
            case HANGING_SCREAM -> state.setAndContinue(ANIM_HANG_SCREAM);
            case HANGING_CURSE -> {
                RawAnimation curse = (currentCurseVariant == 0) ? ANIM_HANG_CURSE : RawAnimation.begin().thenPlay("animation.boss.hang_curse2");
                yield state.setAndContinue(curse);
            }
            case HANGING_SUMMON -> state.setAndContinue(ANIM_HANG_SUMMON);
            case FALLING -> state.setAndContinue(ANIM_FALL);
            case GROUND_IDLE -> state.setAndContinue(ANIM_GROUND_IDLE);
            case CRAWLING -> state.setAndContinue(ANIM_CRAWL);
            case POUNCE -> state.setAndContinue(ANIM_POUNCE);
            case JUMP_BACK -> state.setAndContinue(ANIM_JUMP_BACK);
            case GROUND_SCREAM -> state.setAndContinue(ANIM_GROUND_SCREAM);
        };
    }

    public BossState getCurrentState() { return BossState.valueOf(this.entityData.get(DATA_STATE)); }
    public boolean isPhaseTwo() { return this.entityData.get(DATA_PHASE_TWO); }

    public void setCurrentState(BossState newState) {
        this.entityData.set(DATA_STATE, newState.name());
        this.stateTimer = 0;
    }

    @Override
    public AnimatableInstanceCache getAnimatableInstanceCache() { return this.cache; }

    @Override
    public boolean hurt(DamageSource source, float amount) {
        if (this.isInvulnerableTo(source)) return false;

        // 如果在待机休眠状态下被打到，强制触发苏醒
        if (this.getCurrentState() == BossState.HANGING_DORMANT && !this.level().isClientSide) {
            setCurrentState(BossState.HANGING_WAKING);
        }
        
        // 二阶段防连击防站桩
        if (this.isPhaseTwo() && !this.level().isClientSide && source.getEntity() instanceof Player) {
            long time = this.level().getGameTime();
            if (time - this.lastHitTime > 60) this.recentHits = 0;
            this.recentHits++;
            this.lastHitTime = time;
            
            if (this.recentHits >= 4 && (this.getCurrentState() == BossState.CRAWLING || this.getCurrentState() == BossState.GROUND_IDLE)) {
                this.recentHits = 0;
                this.proximityTicks = 0;
                setCurrentState(BossState.JUMP_BACK);
            }
        }
        
        return super.hurt(source, amount);
    }

    @Override
    public void tick() {
        super.tick();
        if (this.level().isClientSide) return;

        this.stateTimer++;
        if (this.attackCooldown > 0) this.attackCooldown--;
        if (this.curseCooldown > 0) this.curseCooldown--;

        BossState state = getCurrentState();
        boolean phaseTwo = isPhaseTwo();

        // 血量下降到 1/2 时转阶段
        if (!phaseTwo && this.getHealth() <= this.getMaxHealth() / 2.0f) {
            this.entityData.set(DATA_PHASE_TWO, true);
            setCurrentState(BossState.FALLING);
            phaseTwo = true;
        }

        tickState(state);

        // ================== 物理与模型朝向强制控制 ==================
        if (!phaseTwo && state != BossState.FALLING) {
            // 【一阶段控制】锁死位移，去除重力和物理碰撞
            this.setDeltaMovement(Vec3.ZERO);
            this.setNoGravity(true);
            this.noPhysics = true; 
            
            // 【修复1】初始化并锁死躯干朝向，原版MC会自动扭动身体跟随头，这里将其锁死不让身体转动
            if (this.lockedBodyRot == -999.0f) {
                this.lockedBodyRot = this.yBodyRot;
            }
            this.setYRot(this.lockedBodyRot);
            this.yBodyRot = this.lockedBodyRot;
            
        } else {
            // 【修复2】二阶段或下落阶段，无论是不是重载存档，强制保证重力和碰撞启用
            // 否则施放扑咬等有Y轴初速度的技能时，会直接无视重力升天飞走！
            this.setNoGravity(false);
            this.noPhysics = false;
        }
    }

    private void tickState(BossState state) {
        switch (state) {
            case HANGING_DORMANT -> tickHangingDormant();
            case HANGING_WAKING -> tickHangingWaking();
            case HANGING_IDLE -> tickHangingIdle();
            case HANGING_SCREAM -> tickHangingScream();
            case HANGING_CURSE -> tickHangingCurse();
            case HANGING_SUMMON -> tickHangingSummon();
            case FALLING -> tickFalling();
            case GROUND_IDLE -> tickGroundIdle();
            case CRAWLING -> tickCrawling();
            case POUNCE -> tickPounce();
            case JUMP_BACK -> tickJumpBack();
            case GROUND_SCREAM -> tickGroundScream();
        }
    }

    // ================== PHASE 1 ==================
    private void tickHangingDormant() {
        if (this.tickCount % 20 == 0) {
            Player nearest = this.level().getNearestPlayer(this, WAKE_RANGE);
            if (nearest != null && !nearest.isCreative() && !nearest.isSpectator()) {
                setCurrentState(BossState.HANGING_WAKING);
            }
        }
    }

    private void tickHangingWaking() {
        if (stateTimer >= 170) setCurrentState(BossState.HANGING_IDLE);
        if (stateTimer == 120) this.playSound(SoundEvents.WARDEN_ROAR, 1.5f, 0.5f);
        if (this.tickCount % 10 == 0) this.level().broadcastEntityEvent(this, (byte) 10);
    }

    private void tickHangingIdle() {
        LivingEntity target = this.getTarget();
        // 【修复3】如果没有目标，或者距离太远，只保持发呆（return跳过攻击逻辑），不会再回到沉睡状态！
        if (target == null || !target.isAlive() || target.distanceToSqr(this) > 64 * 64) {
            return;
        }
        
        // 凝视玩家，头颅会转向玩家，但身体已经在 tick() 里锁死了
        this.getLookControl().setLookAt(target.getX(), target.getEyeY(), target.getZ(), 30f, 30f);

        if (stateTimer > 40 && attackCooldown <= 0) {
            double dist = target.distanceToSqr(this);
            if (dist < 256.0 && this.random.nextFloat() < 0.4f && curseCooldown <= 0) {
                setCurrentState(BossState.HANGING_SUMMON);
                attackCooldown = 120;
                curseCooldown = 200;
            } 
            else if (this.random.nextBoolean() && curseCooldown <= 0) {
                setCurrentState(BossState.HANGING_CURSE);
                attackCooldown = 100;
                curseCooldown = 150;
                currentCurseVariant = this.random.nextInt(2);
            } 
            else {
                setCurrentState(BossState.HANGING_SCREAM);
                attackCooldown = 80;
            }
        }
    }

    private void tickHangingScream() {
        if (stateTimer == 5) performScreamAttack();
        if (stateTimer >= 30) setCurrentState(BossState.HANGING_IDLE);
    }

    private void tickHangingCurse() {
        int duration = (currentCurseVariant == 0) ? 130 : 160;
        if (stateTimer == 30) performCurseAttack();
        if (currentCurseVariant == 1 && stateTimer == 70) performCurseAttack();
        if (stateTimer >= duration) setCurrentState(BossState.HANGING_IDLE);
    }

    private void tickHangingSummon() {
        if (stateTimer == 40) summonMinions();
        if (stateTimer == 50) performShockwave();
        if (stateTimer >= 80) setCurrentState(BossState.HANGING_IDLE);
    }

    // ================== PHASE 2 ==================
    private void tickFalling() {
        if (stateTimer == 10) this.playSound(SoundEvents.CHAIN_BREAK, 1.5f, 0.5f);
        if (stateTimer == 60) { this.playSound(SoundEvents.GENERIC_BIG_FALL, 2.0f, 0.5f); performShockwave(); }
        if (stateTimer >= 260) {
            this.getAttribute(Attributes.MOVEMENT_SPEED).setBaseValue(0.42D);
            setCurrentState(BossState.GROUND_IDLE);
        }
    }

    private void tickGroundIdle() {
        if (stateTimer > 40) {
            LivingEntity target = this.getTarget();
            if (target == null || !target.isAlive()) {
                Player nearest = this.level().getNearestPlayer(this, 64.0);
                if (nearest != null) this.setTarget(nearest);
            }
            setCurrentState(BossState.CRAWLING);
        }
    }

    private void tickCrawling() {
        LivingEntity target = this.getTarget();
        if (target == null || !target.isAlive()) { setCurrentState(BossState.GROUND_IDLE); return; }
        
        double dist = target.distanceToSqr(this);
        
        if (dist < 16.0) this.proximityTicks++;
        else this.proximityTicks = Math.max(0, this.proximityTicks - 1);
        
        if (this.proximityTicks > 80) {
            this.proximityTicks = 0;
            this.recentHits = 0;
            setCurrentState(BossState.JUMP_BACK);
            return;
        }

        if (dist < 36.0 && attackCooldown <= 0) { 
            this.pounceCount = 0; 
            setCurrentState(BossState.POUNCE); 
            return; 
        }
    }

    private void tickPounce() {
        LivingEntity target = this.getTarget();
        if (stateTimer == 8 && target != null) {
            Vec3 dir = target.position().subtract(this.position()).normalize();
            // 在二阶段物理/重力强制开启后，赋予带有Y轴的初速度，会实现向前跃起的扑咬而不再飞天
            this.setDeltaMovement(dir.x * 1.5, 0.2, dir.z * 1.5);
            this.playSound(SoundEvents.WOLF_GROWL, 1.5f, 0.5f);
        }
        
        if (stateTimer > 8 && stateTimer < 20 && target != null) {
            if (this.getBoundingBox().inflate(1.5).intersects(target.getBoundingBox())) {
                if (this.tickCount % 5 == 0) {
                    target.hurt(this.damageSources().mobAttack(this), 12.0f);
                }
            }
        }
        
        if (stateTimer >= 30) {
            pounceCount++;
            if (pounceCount >= MIN_POUNCES && (pounceCount >= MAX_POUNCES || this.random.nextFloat() < 0.4f)) {
                setCurrentState(BossState.JUMP_BACK);
                return;
            }
            attackCooldown = 15;
            setCurrentState(BossState.CRAWLING);
        }
    }

    private void tickJumpBack() {
        if (stateTimer == 8) {
            AABB sweepBox = this.getBoundingBox().inflate(3.5);
            for (LivingEntity entity : this.level().getEntitiesOfClass(LivingEntity.class, sweepBox)) {
                if (entity != this && !(entity instanceof PaleMinionEntity)) {
                    entity.hurt(this.damageSources().mobAttack(this), 8.0f);
                    Vec3 push = entity.position().subtract(this.position()).normalize();
                    entity.setDeltaMovement(push.x * 1.0, 0.5, push.z * 1.0);
                }
            }
            this.playSound(SoundEvents.PLAYER_ATTACK_SWEEP, 1.5f, 0.5f);
            this.level().broadcastEntityEvent(this, (byte) 35);

            LivingEntity target = this.getTarget();
            if (target != null) {
                Vec3 dir = this.position().subtract(target.position()).normalize();
                this.setDeltaMovement(dir.x * 1.3, 0.4, dir.z * 1.3); // 向后跳跃反冲
            }
        }
        if (stateTimer >= 30) {
            setCurrentState(BossState.GROUND_SCREAM);
        }
    }

    private void tickGroundScream() {
        if (stateTimer == 15) {
            if (this.random.nextBoolean()) performScreamAttack();
            else performCurseAttack();
        }
        if (stateTimer >= 40) { 
            attackCooldown = 40;
            setCurrentState(BossState.CRAWLING); 
        }
    }

    // ================== COMBAT ==================
    private void performScreamAttack() {
        this.playSound(SoundEvents.WARDEN_SONIC_BOOM, 2.0f, 1.0f);
        Vec3 lookVec = this.getLookAngle();
        Vec3 start = this.getEyePosition();
        for (int i = 0; i < 15; i++) {
            Vec3 point = start.add(lookVec.scale(i * 0.8));
            AABB box = new AABB(point.x - 0.8, point.y - 0.8, point.z - 0.8,
                    point.x + 0.8, point.y + 0.8, point.z + 0.8);
            for (LivingEntity entity : this.level().getEntitiesOfClass(LivingEntity.class, box)) {
                if (entity == this || entity instanceof PaleMinionEntity) continue;
                entity.hurt(this.damageSources().sonicBoom(this), 8.0f);
            }
            this.level().broadcastEntityEvent(this, (byte) 20);
        }
        LivingEntity target = this.getTarget();
        if (target != null && target.distanceToSqr(this) < 100.0) {
            target.hurt(this.damageSources().sonicBoom(this), 10.0f);
        }
    }

    private void performCurseAttack() {
        this.playSound(SoundEvents.EVOKER_CAST_SPELL, 2.0f, 0.3f);
        LivingEntity target = this.getTarget();
        if (target instanceof Player player) {
            player.addEffect(new MobEffectInstance(MobEffects.BLINDNESS, 60, 0));
            player.addEffect(new MobEffectInstance(MobEffects.MOVEMENT_SLOWDOWN, 100, 1));
            player.addEffect(new MobEffectInstance(MobEffects.WEAKNESS, 120, 0));
            player.hurt(this.damageSources().magic(), 6.0f);
            this.level().broadcastEntityEvent(this, (byte) 25);
        }
        AABB area = this.getBoundingBox().inflate(16.0);
        for (LivingEntity entity : this.level().getEntitiesOfClass(LivingEntity.class, area)) {
            if (entity == this || entity instanceof PaleMinionEntity) continue;
            if (entity instanceof Player p && p == target) continue;
            if (entity.distanceToSqr(this) < 36.0)
                entity.addEffect(new MobEffectInstance(MobEffects.MOVEMENT_SLOWDOWN, 80, 0));
        }
    }

    private void summonMinions() {
        this.playSound(SoundEvents.EVOKER_PREPARE_SUMMON, 2.0f, 0.5f);
        int count = 3 + this.random.nextInt(3);
        for (int i = 0; i < count; i++) {
            PaleMinionEntity minion = PaleLullabyEntities.PALE_MINION.get().create(this.level());
            if (minion != null) {
                double angle = (2 * Math.PI / count) * i;
                double dx = Math.cos(angle) * 4.0;
                double dz = Math.sin(angle) * 4.0;
                minion.setPos(this.getX() + dx, this.getY() - 2, this.getZ() + dz);
                minion.setTarget(this.getTarget());
                this.level().addFreshEntity(minion);
            }
        }
    }

    private void performShockwave() {
        this.playSound(SoundEvents.GENERIC_EXPLODE.value(), 2.0f, 0.5f);
        AABB area = this.getBoundingBox().inflate(8.0, 4.0, 8.0);
        List<LivingEntity> entities = this.level().getEntitiesOfClass(LivingEntity.class, area,
                e -> e != this && !(e instanceof PaleMinionEntity));
        for (LivingEntity entity : entities) {
            Vec3 pushDir = entity.position().subtract(this.position()).normalize();
            entity.setDeltaMovement(pushDir.x * 2.5, 1.2, pushDir.z * 2.5);
            entity.hurtMarked = true;
            entity.hurt(this.damageSources().explosion(this, this), 8.0f);
        }
        this.level().broadcastEntityEvent(this, (byte) 30);
    }

    @Override
    protected void registerGoals() {
        this.goalSelector.addGoal(1, new BossCombatGoal(this));
        this.targetSelector.addGoal(1, new NearestAttackableTargetGoal<>(this, Player.class, false));
    }

    private static class BossCombatGoal extends Goal {
        private final MullandEntity boss;
        public BossCombatGoal(MullandEntity boss) { this.boss = boss; this.setFlags(EnumSet.of(Flag.MOVE, Flag.LOOK)); }
        @Override public boolean canUse() { return boss.isPhaseTwo() && (boss.getCurrentState() == BossState.CRAWLING || boss.getCurrentState() == BossState.GROUND_IDLE); }
        @Override public void tick() { 
            LivingEntity target = boss.getTarget(); 
            if (target != null) { 
                boss.getNavigation().moveTo(target, 1.4D); 
                boss.getLookControl().setLookAt(target.getX(), boss.getEyeY(), target.getZ(), 30f, 30f); 
            } 
        }
        @Override public boolean canContinueToUse() { return this.canUse() && boss.getTarget() != null; }
    }

    @Override
    protected SoundEvent getAmbientSound() {
        BossState state = getCurrentState();
        if (state == BossState.HANGING_DORMANT) return null;
        return switch (state) {
            case HANGING_SCREAM -> SoundEvents.WARDEN_SONIC_BOOM;
            case HANGING_CURSE -> SoundEvents.EVOKER_CAST_SPELL;
            case GROUND_SCREAM -> SoundEvents.WARDEN_SONIC_BOOM;
            default -> SoundEvents.WARDEN_AMBIENT;
        };
    }

    @Override protected SoundEvent getHurtSound(DamageSource source) { return SoundEvents.WARDEN_HURT; }
    @Override protected SoundEvent getDeathSound() { return SoundEvents.WARDEN_DEATH; }
    @Override public boolean fireImmune() { return true; }
    @Override public boolean isPushable() { return false; }
    @Override public boolean causeFallDamage(float pFallDistance, float pMultiplier, DamageSource pSource) { return false; }
    
    @Override
    public void handleEntityEvent(byte id) {
        switch (id) {
            case 10 -> spawnWakeupParticles();
            case 20 -> spawnScreamParticles();
            case 25 -> spawnCurseParticles();
            case 30 -> spawnShockwaveParticles();
            case 35 -> spawnSweepParticles();
            default -> super.handleEntityEvent(id);
        }
    }

    private void spawnWakeupParticles() {
        if (this.level().isClientSide)
            for (int i = 0; i < 5; i++)
                this.level().addParticle(net.minecraft.core.particles.ParticleTypes.SOUL,
                        this.getX() + (this.random.nextDouble() - 0.5) * 2.0, this.getEyeY() + this.random.nextDouble(),
                        this.getZ() + (this.random.nextDouble() - 0.5) * 2.0, 0, 0.05, 0);
    }

    private void spawnScreamParticles() {
        if (this.level().isClientSide) {
            Vec3 look = this.getLookAngle();
            for (int i = 0; i < 3; i++)
                this.level().addParticle(net.minecraft.core.particles.ParticleTypes.SONIC_BOOM,
                        this.getX() + look.x * i * 2, this.getEyeY(), this.getZ() + look.z * i * 2, 0, 0, 0);
        }
    }

    private void spawnCurseParticles() {
        if (this.level().isClientSide)
            for (int i = 0; i < 30; i++)
                this.level().addParticle(net.minecraft.core.particles.ParticleTypes.WITCH,
                        this.getX() + (this.random.nextDouble() - 0.5) * 10.0, this.getY() + this.random.nextDouble() * 5.0,
                        this.getZ() + (this.random.nextDouble() - 0.5) * 10.0, 0, 0, 0);
    }

    private void spawnShockwaveParticles() {
        if (this.level().isClientSide)
            for (int i = 0; i < 40; i++) {
                double angle = this.random.nextDouble() * Math.PI * 2;
                double radius = this.random.nextDouble() * 6.0;
                this.level().addParticle(net.minecraft.core.particles.ParticleTypes.EXPLOSION,
                        this.getX() + Math.cos(angle) * radius, this.getY() + 0.5,
                        this.getZ() + Math.sin(angle) * radius, 0, 0.1, 0);
            }
    }

    private void spawnSweepParticles() {
        if (this.level().isClientSide) {
            double d0 = -Math.sin(this.getYRot() * ((float)Math.PI / 180F));
            double d1 = Math.cos(this.getYRot() * ((float)Math.PI / 180F));
            this.level().addParticle(net.minecraft.core.particles.ParticleTypes.SWEEP_ATTACK,
                    this.getX() + d0, this.getY() + 0.5D, this.getZ() + d1, 0.0D, 0.0D, 0.0D);
        }
    }
}