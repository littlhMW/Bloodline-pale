package com.littlh.palelullaby.entity;

import com.littlh.palelullaby.PaleLullabyItems;
import com.littlh.palelullaby.entity.ability.AbilityType;
import com.littlh.palelullaby.entity.ability.BloodAbility;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.item.ItemStack;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.ai.goal.MeleeAttackGoal;
import net.minecraft.world.entity.monster.Monster;
import net.minecraft.world.level.Level;

import java.util.List;

/** 鲜血领主（无铁魔法路径）：迷你 Boss，拥有全部六种血魔法。 */
public class LordVampireEntity extends AbstractVampireEntity {

    private static final BloodAbility BLOOD_BOIL =
            BloodAbility.ability("blood_boil", AbilityType.RANGED, 24, 160, 280, 12.0D, 8.0F, 5);
    private static final BloodAbility CRIMSON_TOUCH =
            BloodAbility.ability("crimson_touch", AbilityType.LIFESTEAL, 30, 240, 360, 8.0D, 8.0F, 4);
    private static final BloodAbility SANGUINE_PLAGUE =
            BloodAbility.ability("sanguine_plague", AbilityType.DEBUFF, 20, 300, 420, 10.0D, 0.0F, 4);
    private static final BloodAbility BLOOD_BLADE =
            BloodAbility.ability("blood_blade", AbilityType.MELEE_ARC, 16, 120, 240, 3.0D, 14.0F, 5);
    private static final BloodAbility BLOOD_STEP =
            BloodAbility.ability("blood_step", AbilityType.TELEPORT, 10, 200, 300, 10.0D, 0.0F, 3);
    private static final BloodAbility EXECUTION =
            BloodAbility.ability("execution", AbilityType.EXECUTE, 20, 400, 500, 3.0D, 12.0F, 4);

    public LordVampireEntity(EntityType<? extends Monster> entityType, Level level) {
        super(entityType, level);
        this.xpReward = 50;
    }

    public static AttributeSupplier.Builder createAttributes() {
        return Monster.createMonsterAttributes()
                .add(Attributes.MAX_HEALTH, CombatRank.RANK_3.maxHealth())
                .add(Attributes.MOVEMENT_SPEED, CombatRank.RANK_3.movementSpeed())
                .add(Attributes.ATTACK_DAMAGE, CombatRank.RANK_3.attackDamage())
                .add(Attributes.FOLLOW_RANGE, 48.0D)
                .add(Attributes.ARMOR, CombatRank.RANK_3.armor())
                .add(Attributes.ARMOR_TOUGHNESS, CombatRank.RANK_3.toughness())
                .add(Attributes.KNOCKBACK_RESISTANCE, CombatRank.RANK_3.knockbackResistance());
    }

    @Override
    protected void registerGoals() {
        this.addVampireCoreGoals();
        this.goalSelector.addGoal(2, new VampireKiteGoal(this));
        this.addCastingGoal(3);
        this.goalSelector.addGoal(4, new MeleeAttackGoal(this, CombatRank.RANK_3.movementSpeed(), true));
    }

    @Override
    public List<BloodAbility> abilities() {
        return List.of(BLOOD_BOIL, CRIMSON_TOUCH, SANGUINE_PLAGUE, BLOOD_BLADE, BLOOD_STEP, EXECUTION);
    }

    @Override
    protected SoundEvent getAmbientSound() { return SoundEvents.ZOMBIE_AMBIENT; }
    @Override
    protected SoundEvent getHurtSound(DamageSource source) { return SoundEvents.ZOMBIE_HURT; }
    @Override
    protected SoundEvent getDeathSound() { return SoundEvents.ZOMBIE_DEATH; }

    @Override
    protected void dropCustomDeathLoot(net.minecraft.server.level.ServerLevel level, DamageSource damageSource, boolean recentlyHit) {
        super.dropCustomDeathLoot(level, damageSource, recentlyHit);
        this.spawnAtLocation(new ItemStack(PaleLullabyItems.BLOOD_MARK.get(), 8 + this.random.nextInt(5)));
        if (this.random.nextFloat() < 0.3F) {
            this.spawnAtLocation(new ItemStack(PaleLullabyItems.GOLDEN_TEAR_BADGE.get()));
        }
    }
}
