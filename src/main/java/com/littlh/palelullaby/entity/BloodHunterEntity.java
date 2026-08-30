package com.littlh.palelullaby.entity;

import com.littlh.palelullaby.PaleLullabyEffects;
import com.littlh.palelullaby.PaleLullabyItems;
import com.littlh.palelullaby.entity.ability.AbilityCastGoal;
import com.littlh.palelullaby.entity.ability.AbilityType;
import com.littlh.palelullaby.entity.ability.BloodAbility;
import net.minecraft.core.particles.DustParticleOptions;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.world.DifficultyInstance;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.MobSpawnType;
import net.minecraft.world.entity.PathfinderMob;
import net.minecraft.world.entity.SpawnGroupData;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.ai.goal.FloatGoal;
import net.minecraft.world.entity.ai.goal.LookAtPlayerGoal;
import net.minecraft.world.entity.ai.goal.RandomLookAroundGoal;
import net.minecraft.world.entity.ai.goal.WaterAvoidingRandomStrollGoal;
import net.minecraft.world.entity.monster.RangedAttackMob;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.projectile.AbstractArrow;
import net.minecraft.world.entity.projectile.ProjectileUtil;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.ServerLevelAccessor;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import org.joml.Vector3f;

import java.util.List;
import javax.annotation.Nullable;

/**
 * 血猎：银剑近战 + 弩辅助 + 药物/增益型法术（无远程法术）。
 * 一个类覆盖三阶（初阶/中阶/高阶），阶位由实体类型决定。
 */
public class BloodHunterEntity extends PathfinderMob implements RangedAttackMob, HunterMob, CasterMob {

    private static final EntityDataAccessor<Boolean> DATA_CASTING =
            SynchedEntityData.defineId(BloodHunterEntity.class, EntityDataSerializers.BOOLEAN);

    private static final BloodAbility SILVER_BLESSING =
            BloodAbility.ability("silver_blessing", AbilityType.BUFF, 16, 240, 360, 0.0D, 0.0F, 6);
    private static final BloodAbility PURIFYING_DRAUGHT =
            BloodAbility.ability("purifying_draught", AbilityType.CLEANSE, 12, 300, 420, 0.0D, 0.0F, 6);
    private static final BloodAbility HUNTERS_MARK =
            BloodAbility.ability("hunters_mark", AbilityType.MARK, 20, 400, 500, 16.0D, 0.0F, 6);
    private static final BloodAbility STONESKIN_POTION =
            BloodAbility.ability("stoneskin_potion", AbilityType.BUFF, 24, 500, 600, 0.0D, 0.0F, 6);
    private static final BloodAbility ABSOLUTION =
            BloodAbility.ability("absolution", AbilityType.AOE, 16, 360, 480, 3.0D, 6.0F, 6);

    /** 圣银附魔剩余 tick。 */
    private int silverBlessingTicks;
    /** 石肤药剂剩余 tick。 */
    private int stoneskinTicks;
    /** 猎人印记剩余 tick。 */
    private int markTicks;
    /** 被标记目标的实体 id。 */
    @Nullable
    private Integer markedTargetId;

    public BloodHunterEntity(EntityType<? extends PathfinderMob> entityType, Level level) {
        super(entityType, level);
        this.xpReward = 10;
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

    public static AttributeSupplier.Builder createAttributes(HunterRank rank) {
        return Mob.createMobAttributes()
                .add(Attributes.MAX_HEALTH, rank.maxHealth())
                .add(Attributes.MOVEMENT_SPEED, rank.movementSpeed())
                .add(Attributes.ATTACK_DAMAGE, rank.attackDamage())
                .add(Attributes.FOLLOW_RANGE, 40.0D)
                .add(Attributes.ARMOR, rank.armor())
                .add(Attributes.ARMOR_TOUGHNESS, rank.toughness());
    }

    /** 阶位由实体类型决定，一个类服务三个层级。 */
    public HunterRank hunterRank() {
        if (this.getType() == PaleLullabyEntities.ADEPT_BLOOD_HUNTER.get()) {
            return HunterRank.RANK_2;
        }
        if (this.getType() == PaleLullabyEntities.VETERAN_BLOOD_HUNTER.get()) {
            return HunterRank.RANK_3;
        }
        return HunterRank.RANK_1;
    }

    @Override
    protected void registerGoals() {
        this.goalSelector.addGoal(0, new FloatGoal(this));
        this.goalSelector.addGoal(1, new BloodHunterMeleeGoal(this, 1.25D, true));
        this.goalSelector.addGoal(2, new AbilityCastGoal<>(this, this.abilities()));
        this.goalSelector.addGoal(3, new BloodHunterRangedGoal(this, 1.0D, 10.0F));
        this.goalSelector.addGoal(4, new WaterAvoidingRandomStrollGoal(this, 0.9D));
        this.goalSelector.addGoal(5, new LookAtPlayerGoal(this, Player.class, 8.0F));
        this.goalSelector.addGoal(6, new RandomLookAroundGoal(this));
        FactionTargets.register(this, PaleLullabyFactions.HUNTER);
    }

    @Override
    public List<BloodAbility> abilities() {
        return switch (this.hunterRank()) {
            case RANK_1 -> List.of(SILVER_BLESSING, PURIFYING_DRAUGHT);
            case RANK_2 -> List.of(SILVER_BLESSING, PURIFYING_DRAUGHT, HUNTERS_MARK);
            case RANK_3 -> List.of(SILVER_BLESSING, PURIFYING_DRAUGHT, HUNTERS_MARK, STONESKIN_POTION, ABSOLUTION);
        };
    }

    @Override
    public boolean canCastAbility(BloodAbility ability, LivingEntity target) {
        if (ability == SILVER_BLESSING) {
            if (this.silverBlessingTicks > 0 || target == null) {
                return false;
            }
            PaleLullabyFactions f = PaleLullabyFactions.of(target);
            return target instanceof VampireMob || (f != null && f.isMad());
        }
        if (ability == STONESKIN_POTION) {
            return this.stoneskinTicks <= 0 && this.getHealth() / Math.max(1.0F, this.getMaxHealth()) < 0.4F;
        }
        if (ability == PURIFYING_DRAUGHT) {
            return this.hasNegativeEffect();
        }
        if (ability == HUNTERS_MARK) {
            return this.markTicks <= 0 && target != null && target.isAlive();
        }
        return true;
    }

    private boolean hasNegativeEffect() {
        return this.isOnFire()
                || this.hasEffect(PaleLullabyEffects.SANGUINE_PLAGUE)
                || this.hasEffect(MobEffects.MOVEMENT_SLOWDOWN)
                || this.hasEffect(MobEffects.WEAKNESS);
    }

    @Override
    public void performAbility(BloodAbility ability, LivingEntity target) {
        switch (ability.type()) {
            case BUFF -> {
                if (ability == SILVER_BLESSING) {
                    this.silverBlessingTicks = 160;
                    DustParticleOptions silver = new DustParticleOptions(new Vector3f(0.9F, 0.9F, 1.0F), 1.0F);
                    for (int i = 0; i < 12; i++) {
                        this.level().addParticle(silver,
                                this.getX() + this.random.nextGaussian() * 0.6D,
                                this.getY() + this.random.nextDouble() * this.getBbHeight(),
                                this.getZ() + this.random.nextGaussian() * 0.6D, 0, 0.02D, 0);
                    }
                    this.playSound(SoundEvents.ENCHANTMENT_TABLE_USE, 1.0F, 1.0F);
                } else if (ability == STONESKIN_POTION) {
                    this.stoneskinTicks = 160;
                    this.addEffect(new MobEffectInstance(MobEffects.DAMAGE_RESISTANCE, 160, 2));
                    this.addEffect(new MobEffectInstance(MobEffects.MOVEMENT_SLOWDOWN, 160, 0));
                    DustParticleOptions stone = new DustParticleOptions(new Vector3f(0.5F, 0.45F, 0.3F), 1.0F);
                    for (int i = 0; i < 12; i++) {
                        this.level().addParticle(stone,
                                this.getX() + this.random.nextGaussian() * 0.8D,
                                this.getY() + this.random.nextDouble() * this.getBbHeight(),
                                this.getZ() + this.random.nextGaussian() * 0.8D, 0, 0.02D, 0);
                    }
                    this.playSound(SoundEvents.GENERIC_DRINK, 1.0F, 1.0F);
                }
            }
            case CLEANSE -> {
                this.removeEffect(PaleLullabyEffects.SANGUINE_PLAGUE);
                this.removeEffect(MobEffects.MOVEMENT_SLOWDOWN);
                this.removeEffect(MobEffects.WEAKNESS);
                this.clearFire();
                DustParticleOptions green = new DustParticleOptions(new Vector3f(0.4F, 0.7F, 0.3F), 1.0F);
                for (int i = 0; i < 10; i++) {
                    this.level().addParticle(green,
                            this.getX() + this.random.nextGaussian() * 0.5D,
                            this.getY() + this.random.nextDouble() * this.getBbHeight(),
                            this.getZ() + this.random.nextGaussian() * 0.5D, 0, 0, 0);
                }
                this.playSound(SoundEvents.GENERIC_DRINK, 1.0F, 1.1F);
            }
            case MARK -> {
                if (target != null) {
                    this.markedTargetId = target.getId();
                    this.markTicks = 200;
                    DustParticleOptions red = new DustParticleOptions(new Vector3f(1.0F, 0.1F, 0.1F), 1.0F);
                    for (int i = 0; i < 8; i++) {
                        this.level().addParticle(red,
                                target.getX() + this.random.nextGaussian() * 0.6D,
                                target.getY() + this.random.nextDouble() * target.getBbHeight() + 0.5D,
                                target.getZ() + this.random.nextGaussian() * 0.6D, 0, 0.02D, 0);
                    }
                    this.playSound(SoundEvents.ARROW_SHOOT, 1.0F, 1.5F);
                }
            }
            case AOE -> this.absolution();
            case RANGED, MELEE_ARC, LIFESTEAL, DEBUFF, TELEPORT, EXECUTE -> {
                // 猎人不使用吸血鬼的能力。
            }
        }
    }

    /** 净罪：以自身为中心 3 格 AOE，击退敌人并额外伤害吸血鬼/疯狂阵营。 */
    private void absolution() {
        AABB box = this.getBoundingBox().inflate(3.0D);
        List<LivingEntity> enemies = this.level().getEntitiesOfClass(LivingEntity.class, box,
                e -> e != this && e.isAlive() && FactionTargets.isEnemy(this, e));
        for (LivingEntity e : enemies) {
            float dmg = 6.0F;
            PaleLullabyFactions f = PaleLullabyFactions.of(e);
            if (f != null && (f == PaleLullabyFactions.VAMPIRE || f.isMad())) {
                dmg += 4.0F;
            }
            e.hurt(this.damageSources().indirectMagic(this, this), dmg);
            Vec3 away = e.position().subtract(this.position()).normalize();
            e.push(away.x * 1.6D, 0.4D, away.z * 1.6D);
        }
        for (int i = 0; i < 24; i++) {
            double angle = this.random.nextDouble() * Math.PI * 2.0D;
            double r = 1.0D + this.random.nextDouble() * 2.5D;
            this.level().addParticle(ParticleTypes.END_ROD,
                    this.getX() + Math.cos(angle) * r,
                    this.getY() + this.random.nextDouble() * 1.5D,
                    this.getZ() + Math.sin(angle) * r,
                    0, 0.05D, 0);
        }
        this.playSound(SoundEvents.ELDER_GUARDIAN_CURSE, 1.0F, 0.8F);
    }

    @Override
    public void aiStep() {
        if (this.silverBlessingTicks > 0) {
            this.silverBlessingTicks--;
            if (this.random.nextInt(10) == 0) {
                this.level().addParticle(ParticleTypes.END_ROD,
                        this.getX() + this.random.nextGaussian() * 0.5D,
                        this.getEyeY() + 0.2D,
                        this.getZ() + this.random.nextGaussian() * 0.5D, 0, 0.02D, 0);
            }
        }
        if (this.stoneskinTicks > 0) {
            this.stoneskinTicks--;
            DustParticleOptions stone = new DustParticleOptions(new Vector3f(0.5F, 0.45F, 0.3F), 0.8F);
            if (this.random.nextInt(8) == 0) {
                this.level().addParticle(stone,
                        this.getX() + this.random.nextGaussian() * 0.6D,
                        this.getY() + this.random.nextDouble() * this.getBbHeight(),
                        this.getZ() + this.random.nextGaussian() * 0.6D, 0, 0, 0);
            }
        }
        if (this.markTicks > 0) {
            this.markTicks--;
            if (this.markedTargetId != null) {
                Entity marked = this.level().getEntity(this.markedTargetId);
                if (marked == null || !marked.isAlive()) {
                    this.markedTargetId = null;
                    this.markTicks = 0;
                } else if (this.random.nextInt(6) == 0) {
                    this.level().addParticle(new DustParticleOptions(new Vector3f(1.0F, 0.1F, 0.1F), 0.8F),
                            marked.getX() + this.random.nextGaussian() * 0.4D,
                            marked.getY() + marked.getBbHeight() + 0.4D,
                            marked.getZ() + this.random.nextGaussian() * 0.4D, 0, 0, 0);
                }
            }
        }
        super.aiStep();
    }

    @Override
    public boolean doHurtTarget(Entity target) {
        boolean hit = super.doHurtTarget(target);
        if (hit && target instanceof LivingEntity living) {
            float bonus = 0.0F;
            if (living instanceof VampireMob) {
                bonus += this.hunterRank().silverBonus();
            }
            if (this.silverBlessingTicks > 0) {
                if (living instanceof VampireMob) {
                    bonus += 3.0F;
                } else if (PaleLullabyFactions.of(living) != null
                        && PaleLullabyFactions.of(living).isMad()) {
                    bonus += 2.0F;
                }
            }
            if (bonus > 0.0F) {
                living.hurt(this.damageSources().mobAttack(this), bonus);
            }
            if (this.markedTargetId != null && living.getId() == this.markedTargetId) {
                float base = (float) this.getAttributeValue(Attributes.ATTACK_DAMAGE);
                living.hurt(this.damageSources().mobAttack(this), base * 0.25F);
            }
        }
        return hit;
    }

    @Override
    public SpawnGroupData finalizeSpawn(ServerLevelAccessor level, DifficultyInstance difficulty,
                                        MobSpawnType spawnType, SpawnGroupData spawnGroupData) {
        SpawnGroupData data = super.finalizeSpawn(level, difficulty, spawnType, spawnGroupData);
        if (!this.level().isClientSide) {
            this.randomizeEquipment();
        }
        return data;
    }

    private void randomizeEquipment() {
        HunterEquipment.equip(this, this.hunterRank());
    }

    /** 营地随机外观：按血猎库在阶级允许的装备池内随机抽一套。 */
    @Override
    public void equipCampGear() {
        HunterEquipment.equipRandomized(this, this.hunterRank());
    }

    @Override
    public void performRangedAttack(LivingEntity target, float velocity) {
        AbstractArrow arrow = ProjectileUtil.getMobArrow(this, new ItemStack(Items.ARROW), velocity, null);
        arrow.setBaseDamage(this.hunterRank().crossbowDamage());
        double dx = target.getX() - this.getX();
        double dy = target.getEyeY() - this.getEyeY();
        double dz = target.getZ() - this.getZ();
        arrow.shoot(dx, dy + Math.sqrt(dx * dx + dz * dz) * 0.15D, dz, 1.6F,
                (float) (14 - this.level().getDifficulty().getId() * 4));
        this.level().addFreshEntity(arrow);
        this.swing(InteractionHand.OFF_HAND);
        this.playSound(SoundEvents.CROSSBOW_SHOOT, 1.0F, 1.0F);
    }

    @Override
    protected SoundEvent getAmbientSound() { return SoundEvents.VILLAGER_AMBIENT; }
    @Override
    protected SoundEvent getHurtSound(DamageSource source) { return SoundEvents.VILLAGER_HURT; }
    @Override
    protected SoundEvent getDeathSound() { return SoundEvents.VILLAGER_DEATH; }

    @Override
    protected void dropCustomDeathLoot(net.minecraft.server.level.ServerLevel level, DamageSource damageSource, boolean recentlyHit) {
        super.dropCustomDeathLoot(level, damageSource, recentlyHit);
        int marks = switch (this.hunterRank()) {
            case RANK_1 -> 1;
            case RANK_2 -> 2 + this.random.nextInt(3);
            case RANK_3 -> 5 + this.random.nextInt(4);
        };
        this.spawnAtLocation(new ItemStack(PaleLullabyItems.BLOOD_MARK.get(), marks));
        if (this.random.nextFloat() < 0.2F) {
            EquipmentSlot[] slots = EquipmentSlot.values();
            EquipmentSlot slot = slots[this.random.nextInt(slots.length)];
            ItemStack stack = this.getItemBySlot(slot);
            if (!stack.isEmpty()) {
                if (stack.isDamageableItem()) {
                    int max = Math.max(1, stack.getMaxDamage() / 3);
                    stack.setDamageValue(max + this.random.nextInt(max));
                }
                this.spawnAtLocation(stack);
                this.setItemSlot(slot, ItemStack.EMPTY);
            }
        }
    }

    /** 近距离：银剑近战；9 格内主动追击肉搏。 */
    private static class BloodHunterMeleeGoal extends net.minecraft.world.entity.ai.goal.MeleeAttackGoal {
        private static final double MAX_USE_DISTANCE_SQ = 4.0D * 4.0D;
        private static final double MAX_CHASE_DISTANCE_SQ = 9.0D * 9.0D;
        private static final double MAX_CONTINUE_DISTANCE_SQ = 12.0D * 12.0D;

        public BloodHunterMeleeGoal(BloodHunterEntity mob, double speedModifier, boolean followingTargetEvenIfNotSeen) {
            super(mob, speedModifier, followingTargetEvenIfNotSeen);
        }

        @Override
        protected void checkAndPerformAttack(LivingEntity target) {
            if (this.canPerformAttack(target)) {
                this.resetAttackCooldown();
                this.mob.doHurtTarget(target);
                this.mob.swing(InteractionHand.MAIN_HAND);
            }
        }

        @Override
        public boolean canUse() {
            LivingEntity target = this.mob.getTarget();
            return target != null && target.isAlive()
                    && this.mob.distanceToSqr(target) <= MAX_CHASE_DISTANCE_SQ
                    && super.canUse();
        }

        @Override
        public boolean canContinueToUse() {
            LivingEntity target = this.mob.getTarget();
            return target != null && target.isAlive()
                    && this.mob.distanceToSqr(target) <= MAX_CONTINUE_DISTANCE_SQ
                    && super.canContinueToUse();
        }
    }

    /** 远距离：使用弩射击。 */
    private static class BloodHunterRangedGoal extends net.minecraft.world.entity.ai.goal.Goal {
        private static final int CHARGE_TIME = 24;
        private final BloodHunterEntity mob;
        private final double speedModifier;
        private final double attackRadiusSqr;
        private int attackCooldown;
        private int chargeTicks;

        public BloodHunterRangedGoal(BloodHunterEntity mob, double speedModifier, float attackRadius) {
            this.mob = mob;
            this.speedModifier = speedModifier;
            this.attackRadiusSqr = attackRadius * attackRadius;
            this.attackCooldown = 20;
            this.chargeTicks = -1;
            this.setFlags(java.util.EnumSet.of(net.minecraft.world.entity.ai.goal.Goal.Flag.MOVE,
                    net.minecraft.world.entity.ai.goal.Goal.Flag.LOOK));
        }

        @Override
        public boolean canUse() {
            LivingEntity target = this.mob.getTarget();
            if (target == null || !target.isAlive()) {
                return false;
            }
            double dist = this.mob.distanceToSqr(target);
            return dist > 9.0D * 9.0D && dist <= 40.0D * 40.0D;
        }

        @Override
        public boolean canContinueToUse() {
            return this.canUse();
        }

        @Override
        public void start() {
            this.attackCooldown = 20;
            this.chargeTicks = -1;
        }

        @Override
        public void stop() {
            if (this.mob.isUsingItem()) {
                this.mob.stopUsingItem();
            }
        }

        @Override
        public void tick() {
            LivingEntity target = this.mob.getTarget();
            if (target == null) {
                return;
            }
            this.mob.getLookControl().setLookAt(target, 30.0F, 30.0F);
            double dist = this.mob.distanceToSqr(target);
            boolean hasLineOfSight = this.mob.getSensing().hasLineOfSight(target);
            if (this.chargeTicks >= 0) {
                this.mob.getNavigation().stop();
                this.chargeTicks--;
                if (this.chargeTicks < 0) {
                    this.mob.stopUsingItem();
                    this.mob.performRangedAttack(target, 1.0F);
                    this.attackCooldown = 30 + this.mob.getRandom().nextInt(20);
                }
                return;
            }
            if (dist > this.attackRadiusSqr || !hasLineOfSight) {
                this.mob.getNavigation().moveTo(target, this.speedModifier);
            } else {
                this.mob.getNavigation().stop();
            }
            if (this.attackCooldown > 0) {
                this.attackCooldown--;
            } else if (hasLineOfSight && dist <= this.attackRadiusSqr && !this.mob.isUsingItem()) {
                this.mob.startUsingItem(InteractionHand.OFF_HAND);
                this.chargeTicks = CHARGE_TIME;
            }
        }
    }
}
