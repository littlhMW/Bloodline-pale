package com.littlh.palelullaby.entity;

import com.littlh.palelullaby.PaleLullabyBiomes;
import com.littlh.palelullaby.entity.ability.BloodAbility;
import io.redspace.ironsspellbooks.entity.mobs.abstract_spell_casting_mob.AbstractSpellCastingMob;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.ai.goal.FloatGoal;
import net.minecraft.world.entity.ai.goal.Goal;
import net.minecraft.world.entity.ai.goal.LookAtPlayerGoal;
import net.minecraft.world.entity.ai.goal.RandomLookAroundGoal;
import net.minecraft.world.entity.ai.goal.WaterAvoidingRandomStrollGoal;
import net.minecraft.world.entity.ai.goal.target.HurtByTargetGoal;
import net.minecraft.world.entity.ai.goal.target.NearestAttackableTargetGoal;
import net.minecraft.world.entity.monster.Enemy;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;

import java.util.EnumSet;
import java.util.List;

/**
 * Spell-casting vampire base: Iron's Spells blood magic casters that share
 * the classic vampire traits (burn in sunlight, hurt by water in daytime,
 * flee the sun). Hostile to players and blood hunters.
 */
public abstract class AbstractSpellCastingVampireEntity extends AbstractSpellCastingMob
        implements Enemy, VampireMob, CasterMob {

    protected AbstractSpellCastingVampireEntity(EntityType<? extends AbstractSpellCastingMob> entityType, Level level) {
        super(entityType, level);
        this.xpReward = 12;
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

    /**
     * Shared movement + targeting goals. Subclasses add their spell goals
     * between priority 2 and 6.
     */
    protected void addVampireCoreGoals() {
        this.goalSelector.addGoal(0, new FleeSunAndWaterGoal(this));
        this.goalSelector.addGoal(1, new FloatGoal(this));
        this.goalSelector.addGoal(7, new WaterAvoidingRandomStrollGoal(this, 0.8D));
        this.goalSelector.addGoal(8, new LookAtPlayerGoal(this, Player.class, 8.0F));
        this.goalSelector.addGoal(9, new RandomLookAroundGoal(this));
        FactionTargets.register(this, PaleLullabyFactions.VAMPIRE);
    }

    // CasterMob: Iron manages its own casting state; no self-written abilities.
    @Override
    public void setCasting(boolean casting) {
    }

    @Override
    public List<BloodAbility> abilities() {
        return List.of();
    }

    @Override
    public void performAbility(BloodAbility ability, LivingEntity target) {
    }

    /** Runs away from sunlight and water during the day. */
    public static class FleeSunAndWaterGoal extends Goal {
        private final AbstractSpellCastingVampireEntity mob;
        private BlockPos avoidTarget;

        public FleeSunAndWaterGoal(AbstractSpellCastingVampireEntity mob) {
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