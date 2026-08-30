package com.littlh.palelullaby.entity;

import io.redspace.ironsspellbooks.api.registry.SpellRegistry;
import io.redspace.ironsspellbooks.entity.mobs.abstract_spell_casting_mob.AbstractSpellCastingMob;
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
 * Blood clan vampire with Iron's Spells: lowest rank caster. Mostly melee,
 * with a single basic blood spell for ranged pressure. Only registered when
 * Iron's Spells is installed.
 */
public class SpellCastingVampireEntity extends AbstractSpellCastingVampireEntity {

    public SpellCastingVampireEntity(EntityType<? extends AbstractSpellCastingMob> entityType, Level level) {
        super(entityType, level);
        this.xpReward = 8;
    }

    public static AttributeSupplier.Builder createAttributes() {
        return createMobAttributes()
                .add(Attributes.ENTITY_INTERACTION_RANGE, 3.0D)
                .add(Attributes.MAX_HEALTH, 30.0D)
                .add(Attributes.MOVEMENT_SPEED, 0.33D)
                .add(Attributes.ATTACK_DAMAGE, 6.0D)
                .add(Attributes.FOLLOW_RANGE, 40.0D)
                .add(Attributes.ARMOR, 4.0D)
                .add(Attributes.ARMOR_TOUGHNESS, 2.0D);
    }

    @Override
    protected void registerGoals() {
        this.addVampireCoreGoals();
        // Rank 1: melee-first with a single weak blood spell, cast sparingly.
        this.goalSelector.addGoal(3, new WarlockAttackGoal(this, 1.15D, 70, 120)
                .setSpells(
                        List.of(
                                SpellRegistry.BLOOD_NEEDLES_SPELL.get(),
                                SpellRegistry.BLOOD_SLASH_SPELL.get()),
                        List.of(),
                        List.of(),
                        List.of())
                .setSpellQuality(0.2F, 0.45F)
                .setMeleeBias(0.1F, 0.3F));
    }

    @Override
    protected SoundEvent getAmbientSound() { return SoundEvents.ZOMBIE_AMBIENT; }
    @Override
    protected SoundEvent getHurtSound(DamageSource source) { return SoundEvents.ZOMBIE_HURT; }
    @Override
    protected SoundEvent getDeathSound() { return SoundEvents.ZOMBIE_DEATH; }
}