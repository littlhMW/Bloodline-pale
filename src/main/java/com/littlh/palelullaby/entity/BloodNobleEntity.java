package com.littlh.palelullaby.entity;

import io.redspace.ironsspellbooks.api.registry.SpellRegistry;
import io.redspace.ironsspellbooks.entity.mobs.abstract_spell_casting_mob.AbstractSpellCastingMob;
import io.redspace.ironsspellbooks.entity.mobs.goals.SpellBarrageGoal;
import io.redspace.ironsspellbooks.entity.mobs.goals.WarlockAttackGoal;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.level.Level;

import java.util.List;

/**
 * Blood Noble: second vampire rank. Casts a moderate set of blood spells.
 */
public class BloodNobleEntity extends AbstractSpellCastingVampireEntity {

    public BloodNobleEntity(EntityType<? extends AbstractSpellCastingMob> entityType, Level level) {
        super(entityType, level);
        this.xpReward = 18;
    }

    public static AttributeSupplier.Builder createAttributes() {
        return createMobAttributes()
                .add(Attributes.ENTITY_INTERACTION_RANGE, 3.0D)
                .add(Attributes.MAX_HEALTH, 45.0D)
                .add(Attributes.MOVEMENT_SPEED, 0.34D)
                .add(Attributes.ATTACK_DAMAGE, 8.0D)
                .add(Attributes.FOLLOW_RANGE, 42.0D)
                .add(Attributes.ARMOR, 6.0D)
                .add(Attributes.ARMOR_TOUGHNESS, 2.0D)
                .add(Attributes.KNOCKBACK_RESISTANCE, 0.4D);
    }

    @Override
    protected void registerGoals() {
        this.addVampireCoreGoals();
        // Occasional ray of siphoning barrage to drain and heal.
        this.goalSelector.addGoal(2, new SpellBarrageGoal(this, SpellRegistry.RAY_OF_SIPHONING_SPELL.get(), 1, 3, 300, 460, 1));
        // Mixed melee + blood spell combat, casts at a moderate pace.
        this.goalSelector.addGoal(3, new WarlockAttackGoal(this, 1.15D, 90, 150)
                .setSpells(
                        List.of(
                                SpellRegistry.BLOOD_NEEDLES_SPELL.get(),
                                SpellRegistry.BLOOD_SLASH_SPELL.get(),
                                SpellRegistry.HEARTSTOP_SPELL.get()),
                        List.of(),
                        List.of(SpellRegistry.BLOOD_STEP_SPELL.get()),
                        List.of())
                .setSpellQuality(0.4F, 0.8F)
                .setMeleeBias(0.15F, 0.35F));
    }

    @Override
    protected SoundEvent getAmbientSound() { return SoundEvents.ZOMBIE_AMBIENT; }
    @Override
    protected SoundEvent getHurtSound(DamageSource source) { return SoundEvents.ZOMBIE_HURT; }
    @Override
    protected SoundEvent getDeathSound() { return SoundEvents.ZOMBIE_DEATH; }
}