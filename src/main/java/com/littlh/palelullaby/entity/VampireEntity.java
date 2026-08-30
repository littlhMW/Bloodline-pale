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

/** 初阶吸血鬼（无铁魔法路径）：血沸牵制 + 血刃收尾的简单循环。 */
public class VampireEntity extends AbstractVampireEntity {

    private static final BloodAbility BLOOD_BOIL =
            BloodAbility.ability("blood_boil", AbilityType.RANGED, 24, 160, 280, 12.0D, 4.0F, 5);
    private static final BloodAbility BLOOD_BLADE =
            BloodAbility.ability("blood_blade", AbilityType.MELEE_ARC, 16, 120, 240, 3.0D, 6.0F, 5);

    public VampireEntity(EntityType<? extends Monster> entityType, Level level) {
        super(entityType, level);
        this.xpReward = 10;
    }

    public static AttributeSupplier.Builder createAttributes() {
        return Monster.createMonsterAttributes()
                .add(Attributes.MAX_HEALTH, CombatRank.RANK_1.maxHealth())
                .add(Attributes.MOVEMENT_SPEED, CombatRank.RANK_1.movementSpeed())
                .add(Attributes.ATTACK_DAMAGE, CombatRank.RANK_1.attackDamage())
                .add(Attributes.FOLLOW_RANGE, 40.0D)
                .add(Attributes.ARMOR, CombatRank.RANK_1.armor())
                .add(Attributes.ARMOR_TOUGHNESS, CombatRank.RANK_1.toughness())
                .add(Attributes.KNOCKBACK_RESISTANCE, CombatRank.RANK_1.knockbackResistance());
    }

    @Override
    protected void registerGoals() {
        this.addVampireCoreGoals();
        this.goalSelector.addGoal(2, new VampireKiteGoal(this));
        this.addCastingGoal(3);
        this.goalSelector.addGoal(4, new MeleeAttackGoal(this, CombatRank.RANK_1.movementSpeed(), true));
    }

    @Override
    public List<BloodAbility> abilities() {
        return List.of(BLOOD_BOIL, BLOOD_BLADE);
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
        this.spawnAtLocation(new ItemStack(PaleLullabyItems.BLOOD_MARK.get(), 1 + this.random.nextInt(2)));
    }
}
