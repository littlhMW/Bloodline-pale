package com.littlh.palelullaby.entity;

import com.littlh.palelullaby.PaleLullabyBiomes;
import com.littlh.palelullaby.PaleLullabyEffects;
import com.littlh.palelullaby.entity.ability.AbilityCastGoal;
import com.littlh.palelullaby.entity.ability.AbilityType;
import com.littlh.palelullaby.entity.ability.BloodAbility;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.particles.DustParticleOptions;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.ai.goal.FloatGoal;
import net.minecraft.world.entity.ai.goal.Goal;
import net.minecraft.world.entity.ai.goal.LookAtPlayerGoal;
import net.minecraft.world.entity.ai.goal.RandomLookAroundGoal;
import net.minecraft.world.entity.ai.goal.WaterAvoidingRandomStrollGoal;
import net.minecraft.world.entity.monster.Monster;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.Vec3;
import org.joml.Vector3f;

import java.util.EnumSet;
import java.util.List;

/**
 * 吸血鬼族系基类（无铁魔法自写路径）。
 * 白天燃烧、白天泡水受伤，通过共享的 AbilityCastGoal 施放自写血魔法。
 */
public abstract class AbstractVampireEntity extends Monster implements VampireMob, CasterMob {

    private static final EntityDataAccessor<Boolean> DATA_CASTING =
            SynchedEntityData.defineId(AbstractVampireEntity.class, EntityDataSerializers.BOOLEAN);

    protected AbstractVampireEntity(EntityType<? extends Monster> entityType, Level level) {
        super(entityType, level);
    }

    @Override
    protected void defineSynchedData(SynchedEntityData.Builder builder) {
        super.defineSynchedData(builder);
        builder.define(DATA_CASTING, false);
    }

    @Override
    public boolean isCasting() {
        return this.entityData.get(DATA_CASTING);
    }

    @Override
    public void setCasting(boolean casting) {
        this.entityData.set(DATA_CASTING, casting);
    }

    /** 注册共享施法目标。 */
    protected void addCastingGoal(int priority) {
        this.goalSelector.addGoal(priority, new AbilityCastGoal<>(this, this.abilities()));
    }

    /** 共享移动与仇恨目标。子类再添加近战/施法目标。 */
    protected void addVampireCoreGoals() {
        this.goalSelector.addGoal(0, new FleeSunAndWaterGoal(this));
        this.goalSelector.addGoal(1, new FloatGoal(this));
        this.goalSelector.addGoal(7, new WaterAvoidingRandomStrollGoal(this, 0.9D));
        this.goalSelector.addGoal(8, new LookAtPlayerGoal(this, Player.class, 8.0F));
        this.goalSelector.addGoal(9, new RandomLookAroundGoal(this));
        FactionTargets.register(this, PaleLullabyFactions.VAMPIRE);
    }

    @Override
    public void aiStep() {
        if (this.isAlive() && this.isSunBurnTick() && this.getItemBySlot(EquipmentSlot.HEAD).isEmpty()) {
            this.igniteForSeconds(8.0F);
        }
        if (this.isAlive() && this.level().isDay() && this.isInWater() && this.tickCount % 20 == 0) {
            this.hurt(this.damageSources().drown(), 1.0F);
        }
        super.aiStep();
    }

    @Override
    protected boolean isSunBurnTick() {
        if (this.level().getBiome(this.blockPosition()).is(PaleLullabyBiomes.CRIMSON_GARDEN)
                || this.level().getBiome(this.blockPosition()).is(PaleLullabyBiomes.WITHERED_PLATEAU)) {
            return false;
        }
        return super.isSunBurnTick();
    }

    @Override
    public void performAbility(BloodAbility ability, LivingEntity target) {
        switch (ability.type()) {
            case RANGED -> this.bloodBoil(target, ability);
            case MELEE_ARC -> this.bloodBlade(target, ability);
            case LIFESTEAL -> this.crimsonTouch(target, ability);
            case DEBUFF -> this.sanguinePlague(target);
            case TELEPORT -> this.bloodStep(target);
            case EXECUTE -> this.execution(target, ability);
            case CLEANSE, BUFF, MARK, AOE -> {
                // 吸血鬼不使用猎人的能力。
            }
        }
    }

    /** 血沸：12 格远程魔法伤害，附带 2 秒缓慢 I。 */
    private void bloodBoil(LivingEntity target, BloodAbility ability) {
        if (target == null) {
            return;
        }
        target.hurt(this.damageSources().indirectMagic(this, this), ability.damage());
        target.addEffect(new MobEffectInstance(MobEffects.MOVEMENT_SLOWDOWN, 40, 0));
        DustParticleOptions red = new DustParticleOptions(new Vector3f(0.8F, 0.0F, 0.0F), 1.0F);
        for (int i = 0; i < 8; i++) {
            this.level().addParticle(red,
                    target.getX() + this.random.nextGaussian() * 0.5D,
                    target.getY() + this.random.nextDouble() * target.getBbHeight(),
                    target.getZ() + this.random.nextGaussian() * 0.5D,
                    0, 0, 0);
        }
        this.level().addParticle(ParticleTypes.CRIT,
                target.getX(), target.getY() + target.getBbHeight() * 0.5D, target.getZ(), 0, 0.1D, 0);
        this.playSound(SoundEvents.PLAYER_HURT, 1.0F, 1.0F);
    }

    /** 猩红之触：8 格魔法伤害，100% 吸血回复自身。 */
    private void crimsonTouch(LivingEntity target, BloodAbility ability) {
        if (target == null) {
            return;
        }
        float dmg = ability.damage();
        if (target.hurt(this.damageSources().indirectMagic(this, this), dmg)) {
            this.heal(dmg);
        }
        DustParticleOptions red = new DustParticleOptions(new Vector3f(0.6F, 0.0F, 0.0F), 1.0F);
        Vec3 from = target.getEyePosition(1.0F);
        Vec3 to = this.getEyePosition();
        for (int i = 0; i < 10; i++) {
            double t = i / 10.0D;
            this.level().addParticle(red,
                    from.x + (to.x - from.x) * t,
                    from.y + (to.y - from.y) * t,
                    from.z + (to.z - from.z) * t,
                    0, 0, 0);
        }
        this.playSound(SoundEvents.EXPERIENCE_ORB_PICKUP, 1.0F, 0.5F);
    }

    /** 血疫缠身：10 格诅咒，目标获得血疫 6 秒。 */
    private void sanguinePlague(LivingEntity target) {
        if (target == null) {
            return;
        }
        target.addEffect(new MobEffectInstance(PaleLullabyEffects.SANGUINE_PLAGUE, 120, 0));
        DustParticleOptions smoke = new DustParticleOptions(new Vector3f(0.5F, 0.0F, 0.0F), 0.8F);
        for (int i = 0; i < 8; i++) {
            this.level().addParticle(smoke,
                    target.getX() + this.random.nextGaussian() * 0.5D,
                    target.getY() + this.random.nextDouble() * target.getBbHeight(),
                    target.getZ() + this.random.nextGaussian() * 0.5D,
                    0, 0.02D, 0);
        }
        this.playSound(SoundEvents.BUBBLE_COLUMN_UPWARDS_INSIDE, 1.0F, 0.7F);
    }

    /** 血步：瞬移到目标身后 3 格；自身血量<25% 时往远离目标方向瞬移 10 格。 */
    private void bloodStep(LivingEntity target) {
        Vec3 oldPos = this.position();
        Vec3 dest;
        if (target != null && this.getHealth() / Math.max(1.0F, this.getMaxHealth()) < 0.25F) {
            Vec3 away = this.position().subtract(target.position()).normalize();
            dest = this.position().add(away.scale(10.0D));
        } else if (target != null) {
            dest = target.position().subtract(target.getLookAngle().scale(3.0D));
        } else {
            dest = this.position();
        }
        this.teleportTo(dest.x, dest.y, dest.z);
        BlockPos pos = this.blockPosition();
        if (!this.level().getBlockState(pos).isAir() || !this.level().getBlockState(pos.above()).isAir()) {
            for (int i = 1; i <= 8; i++) {
                BlockPos probe = pos.above(i);
                if (this.level().getBlockState(probe).isAir() && this.level().getBlockState(probe.above()).isAir()) {
                    this.teleportTo(probe.getX() + 0.5D, probe.getY(), probe.getZ() + 0.5D);
                    break;
                }
            }
        }
        DustParticleOptions red = new DustParticleOptions(new Vector3f(0.7F, 0.0F, 0.0F), 1.0F);
        for (int i = 0; i < 3; i++) {
            this.level().addParticle(red,
                    oldPos.x + this.random.nextGaussian() * 0.4D,
                    oldPos.y + this.random.nextDouble() * this.getBbHeight(),
                    oldPos.z + this.random.nextGaussian() * 0.4D, 0, 0, 0);
            this.level().addParticle(red,
                    this.getX() + this.random.nextGaussian() * 0.4D,
                    this.getY() + this.random.nextDouble() * this.getBbHeight(),
                    this.getZ() + this.random.nextGaussian() * 0.4D, 0, 0, 0);
        }
        this.playSound(SoundEvents.ENDERMAN_TELEPORT, 1.0F, 0.7F);
    }

    /** 血刃：近战扇形物理伤害，20% 附加血疫。 */
    private void bloodBlade(LivingEntity target, BloodAbility ability) {
        if (target != null && this.distanceToSqr(target) <= ability.range() * ability.range()) {
            target.hurt(this.damageSources().mobAttack(this), ability.damage());
            if (this.random.nextFloat() < 0.2F) {
                target.addEffect(new MobEffectInstance(PaleLullabyEffects.SANGUINE_PLAGUE, 120, 0));
            }
            DustParticleOptions red = new DustParticleOptions(new Vector3f(0.8F, 0.0F, 0.0F), 1.0F);
            for (int i = 0; i < 6; i++) {
                this.level().addParticle(red,
                        target.getX() + this.random.nextGaussian() * 0.8D,
                        target.getY() + this.random.nextDouble() * target.getBbHeight(),
                        target.getZ() + this.random.nextGaussian() * 0.8D, 0, 0, 0);
            }
            this.level().addParticle(ParticleTypes.SWEEP_ATTACK,
                    target.getX(), target.getY() + target.getBbHeight() * 0.5D, target.getZ(), 0, 0, 0);
            this.playSound(SoundEvents.PLAYER_ATTACK_SWEEP, 1.0F, 1.0F);
        }
    }

    /** 处刑：目标血量<35% 时伤害翻倍并回复其一半伤害。 */
    private void execution(LivingEntity target, BloodAbility ability) {
        if (target == null) {
            return;
        }
        float dmg = ability.damage();
        if (target.getHealth() <= target.getMaxHealth() * 0.35F) {
            dmg *= 2.0F;
            target.hurt(this.damageSources().indirectMagic(this, this), dmg);
            this.heal(dmg * 0.5F);
        } else {
            target.hurt(this.damageSources().indirectMagic(this, this), dmg);
        }
        DustParticleOptions red = new DustParticleOptions(new Vector3f(0.9F, 0.0F, 0.0F), 1.4F);
        for (int i = 0; i < 12; i++) {
            this.level().addParticle(red,
                    target.getX() + this.random.nextGaussian() * 1.0D,
                    target.getY() + this.random.nextDouble() * target.getBbHeight(),
                    target.getZ() + this.random.nextGaussian() * 1.0D,
                    this.random.nextGaussian() * 0.1D, 0.05D, this.random.nextGaussian() * 0.1D);
        }
        this.playSound(SoundEvents.WITHER_SHOOT, 1.0F, 0.8F);
    }

    /** 放风筝：吸血鬼偏好与目标保持 6~11 格距离施法；被贴近时后撤，撤不动就让位给近战。 */
    public static class VampireKiteGoal extends Goal {
        /** 偏好距离带：6~11 格。 */
        private static final double PREFERRED_MAX_SQ = 11.0 * 11.0;
        /** 低于此距离开始短距后撤。 */
        private static final double TOO_CLOSE_SQ = 6.0 * 6.0;
        /** 目标在此距离内才管理走位。 */
        private static final double ACTIVE_RANGE_SQ = 16.0 * 16.0;
        private static final int RETREAT_TICKS = 25;
        private static final int APPROACH_TICKS = 50;
        private static final int GIVE_UP_TICKS = 120;
        private static final int DISABLED_TICKS = 120;
        private final AbstractVampireEntity mob;
        private int retreatTicks;
        private int approachTicks;
        private int stuckTicks;
        private int disabledTicks;

        public VampireKiteGoal(AbstractVampireEntity mob) {
            this.mob = mob;
            this.setFlags(EnumSet.of(Goal.Flag.MOVE));
        }

        @Override
        public boolean canUse() {
            LivingEntity target = this.mob.getTarget();
            return this.disabledTicks <= 0
                    && target != null && target.isAlive()
                    && this.mob.distanceToSqr(target) < ACTIVE_RANGE_SQ;
        }

        @Override
        public boolean canContinueToUse() {
            LivingEntity target = this.mob.getTarget();
            return target != null && target.isAlive()
                    && this.stuckTicks < GIVE_UP_TICKS
                    && this.mob.distanceToSqr(target) < ACTIVE_RANGE_SQ;
        }

        @Override
        public void start() {
            this.retreatTicks = 0;
            this.approachTicks = 0;
        }

        @Override
        public void stop() {
            this.mob.getNavigation().stop();
        }

        @Override
        public void tick() {
            LivingEntity target = this.mob.getTarget();
            if (target == null) {
                return;
            }
            this.mob.getLookControl().setLookAt(target, 30.0F, 30.0F);
            double distSq = this.mob.distanceToSqr(target);

            // 后撤结束后强制拉近距离，避免吸血鬼无限逃跑。
            if (this.approachTicks > 0) {
                this.approachTicks--;
                if (distSq > PREFERRED_MAX_SQ) {
                    this.mob.getNavigation().moveTo(target, 1.15D);
                } else {
                    this.mob.getNavigation().stop();
                }
                return;
            }

            if (this.retreatTicks > 0) {
                this.retreatTicks--;
                if (this.retreatTicks <= 0 || this.mob.getNavigation().isDone()) {
                    this.retreatTicks = 0;
                    this.approachTicks = APPROACH_TICKS;
                    this.mob.getNavigation().stop();
                }
                return;
            }

            if (distSq < TOO_CLOSE_SQ) {
                Vec3 away = this.mob.position().subtract(target.position()).normalize();
                double x = this.mob.getX() + away.x * 6.0;
                double z = this.mob.getZ() + away.z * 6.0;
                boolean moved = this.mob.getNavigation().moveTo(x, this.mob.getY(), z, 1.2D);
                if (!moved) {
                    this.stuckTicks++;
                    if (this.stuckTicks >= GIVE_UP_TICKS) {
                        this.stuckTicks = 0;
                        this.disabledTicks = DISABLED_TICKS;
                    }
                } else {
                    this.stuckTicks = 0;
                    this.retreatTicks = RETREAT_TICKS;
                }
            } else if (distSq > PREFERRED_MAX_SQ) {
                this.mob.getNavigation().moveTo(target, 1.1D);
                this.stuckTicks = 0;
            } else {
                this.mob.getNavigation().stop();
            }
        }
    }

    public static class FleeSunAndWaterGoal extends Goal {
        private final AbstractVampireEntity mob;
        private BlockPos avoidTarget;

        public FleeSunAndWaterGoal(AbstractVampireEntity mob) {
            this.mob = mob;
            this.setFlags(EnumSet.of(Goal.Flag.MOVE));
        }

        @Override
        public boolean canUse() {
            if (!this.mob.level().isDay()) {
                return false;
            }
            return this.mob.isInWater() || this.mob.isSunBurnTick();
        }

        @Override
        public boolean canContinueToUse() {
            return this.canUse() && !this.mob.getNavigation().isDone();
        }

        @Override
        public void start() {
            this.avoidTarget = null;
        }

        @Override
        public void tick() {
            if (this.mob.isInWater()) {
                BlockPos land = this.findLandTarget();
                if (land != null) {
                    this.mob.getNavigation().moveTo(land.getX() + 0.5, land.getY(), land.getZ() + 0.5, 1.3D);
                    return;
                }
            }
            if (this.avoidTarget == null || this.mob.getNavigation().isDone()) {
                this.avoidTarget = this.findShadeTarget();
            }
            if (this.avoidTarget != null) {
                this.mob.getNavigation().moveTo(this.avoidTarget.getX() + 0.5, this.avoidTarget.getY(), this.avoidTarget.getZ() + 0.5, 1.3D);
            }
        }

        private BlockPos findLandTarget() {
            BlockPos pos = this.mob.blockPosition();
            Level level = this.mob.level();
            for (int i = 0; i < 12; i++) {
                Direction dir = Direction.Plane.HORIZONTAL.getRandomDirection(this.mob.getRandom());
                BlockPos probe = pos.offset(
                        dir.getStepX() * (2 + this.mob.getRandom().nextInt(5)),
                        0,
                        dir.getStepZ() * (2 + this.mob.getRandom().nextInt(5)));
                BlockState state = level.getBlockState(probe);
                if (state.isSolid() && level.getBlockState(probe.above()).isAir()) {
                    return probe.above();
                }
            }
            return null;
        }

        private BlockPos findShadeTarget() {
            BlockPos pos = this.mob.blockPosition();
            Level level = this.mob.level();
            for (int i = 0; i < 20; i++) {
                BlockPos probe = pos.offset(
                        this.mob.getRandom().nextInt(25) - 12,
                        0,
                        this.mob.getRandom().nextInt(25) - 12);
                if (level.getBlockState(probe).isAir() && !level.canSeeSky(probe)) {
                    return probe;
                }
            }
            return null;
        }
    }
}
