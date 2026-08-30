package com.littlh.palelullaby.entity;

import com.littlh.palelullaby.PaleLullabyItems;
import com.littlh.palelullaby.entity.ability.BloodAbility;
import io.redspace.ironsspellbooks.api.registry.SpellRegistry;
import io.redspace.ironsspellbooks.entity.mobs.abstract_spell_casting_mob.AbstractSpellCastingMob;
import io.redspace.ironsspellbooks.entity.mobs.goals.WarlockAttackGoal;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.world.DifficultyInstance;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.MobSpawnType;
import net.minecraft.world.entity.SpawnGroupData;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.ai.goal.FloatGoal;
import net.minecraft.world.entity.ai.goal.MeleeAttackGoal;
import net.minecraft.world.entity.ai.goal.WaterAvoidingRandomStrollGoal;
import net.minecraft.world.entity.ai.goal.target.HurtByTargetGoal;
import net.minecraft.world.entity.ai.goal.target.NearestAttackableTargetGoal;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.ServerLevelAccessor;

import java.util.List;

/**
 * Blood hunter played through Iron's Spells (Holy school): silver-sword
 * melee + crossbow + guiding bolt / divine smite / sunbeam, healing and
 * fortify support. Rank is derived from the entity type. Only registered
 * when Iron's Spells is installed.
 */
public class SpellCastingBloodHunterEntity extends AbstractSpellCastingMob implements HunterMob, CasterMob {

    public SpellCastingBloodHunterEntity(EntityType<? extends AbstractSpellCastingMob> entityType, Level level) {
        super(entityType, level);
        this.xpReward = 10;
    }

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
    public void equipCampGear() {
        HunterEquipment.equipRandomized(this, this.hunterRank());
    }

    public static AttributeSupplier.Builder createAttributes(HunterRank rank) {
        return createMobAttributes()
                .add(Attributes.ENTITY_INTERACTION_RANGE, 3.0D)
                .add(Attributes.MAX_HEALTH, rank.maxHealth())
                .add(Attributes.MOVEMENT_SPEED, rank.movementSpeed())
                .add(Attributes.ATTACK_DAMAGE, rank.attackDamage())
                .add(Attributes.FOLLOW_RANGE, 40.0D)
                .add(Attributes.ARMOR, rank.armor())
                .add(Attributes.ARMOR_TOUGHNESS, rank.toughness());
    }

    @Override
    protected void registerGoals() {
        this.goalSelector.addGoal(0, new FloatGoal(this));
        this.goalSelector.addGoal(1, new MeleeAttackGoal(this, 1.25D, true));
        this.goalSelector.addGoal(2, this.buildCastingGoal());
        this.goalSelector.addGoal(3, new WaterAvoidingRandomStrollGoal(this, 0.9D));
        FactionTargets.register(this, PaleLullabyFactions.HUNTER);
    }

    private WarlockAttackGoal buildCastingGoal() {
        HunterRank rank = this.hunterRank();
        int castMin = rank.castIntervalMin();
        int castMax = rank.castIntervalMax();
        List<io.redspace.ironsspellbooks.api.spells.AbstractSpell> attacks = new java.util.ArrayList<>(List.of(
                SpellRegistry.GUIDING_BOLT_SPELL.get()));
        List<io.redspace.ironsspellbooks.api.spells.AbstractSpell> support = new java.util.ArrayList<>(List.of(
                SpellRegistry.HEAL_SPELL.get()));
        if (rank == HunterRank.RANK_2 || rank == HunterRank.RANK_3) {
            attacks.add(SpellRegistry.DIVINE_SMITE_SPELL.get());
            support.add(SpellRegistry.CLEANSE_SPELL.get());
        }
        if (rank == HunterRank.RANK_3) {
            attacks.add(SpellRegistry.SUNBEAM_SPELL.get());
            support.add(SpellRegistry.GREATER_HEAL_SPELL.get());
            support.add(SpellRegistry.FORTIFY_SPELL.get());
        }
        return new WarlockAttackGoal(this, 1.15D, castMin, castMax)
                .setSpells(attacks, List.of(), List.of(), support)
                .setSpellQuality(0.3F, 0.7F)
                .setMeleeBias(0.6F, 0.85F);
    }

    @Override
    public SpawnGroupData finalizeSpawn(ServerLevelAccessor level, DifficultyInstance difficulty,
                                        MobSpawnType spawnType, SpawnGroupData spawnGroupData) {
        SpawnGroupData data = super.finalizeSpawn(level, difficulty, spawnType, spawnGroupData);
        if (!this.level().isClientSide) {
            this.setItemSlot(EquipmentSlot.MAINHAND, new ItemStack(PaleLullabyItems.SILVER_SWORD.get()));
            this.setItemSlot(EquipmentSlot.OFFHAND, new ItemStack(Items.CROSSBOW));
        }
        return data;
    }

    @Override
    public boolean doHurtTarget(net.minecraft.world.entity.Entity target) {
        boolean hit = super.doHurtTarget(target);
        if (hit && target instanceof VampireMob && !this.getMainHandItem().isEmpty()) {
            target.hurt(this.damageSources().mobAttack(this), this.hunterRank().silverBonus());
        }
        return hit;
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

    @Override
    protected SoundEvent getAmbientSound() { return SoundEvents.VILLAGER_AMBIENT; }
    @Override
    protected SoundEvent getHurtSound(DamageSource source) { return SoundEvents.VILLAGER_HURT; }
    @Override
    protected SoundEvent getDeathSound() { return SoundEvents.VILLAGER_DEATH; }
}