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
 * Blood Lord: highest vampire rank, a mini boss. Commands a
 * full arsenal of blood magic. Only registered when Iron's Spells is loaded.
 */
public class BloodLordEntity extends AbstractSpellCastingVampireEntity {


    public BloodLordEntity(EntityType<? extends AbstractSpellCastingMob> entityType, Level level) {
        super(entityType, level);
        this.xpReward = 30;
    }

    public static AttributeSupplier.Builder createAttributes() {
        return createMobAttributes()
                .add(Attributes.ENTITY_INTERACTION_RANGE, 3.0D)
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
        // Devour barrage: devours weakened targets for massive healing, used sparingly.
        this.goalSelector.addGoal(2, new SpellBarrageGoal(this, SpellRegistry.DEVOUR_SPELL.get(), 3, 6, 260, 420, 1));
        // Full blood arsenal + raise dead support + blood step escape, cast at a slower pace.
        this.goalSelector.addGoal(3, new WarlockAttackGoal(this, 1.2D, 110, 170)
                .setSpells(
                        List.of(
                                SpellRegistry.BLOOD_NEEDLES_SPELL.get(),
                                SpellRegistry.BLOOD_SLASH_SPELL.get(),
                                SpellRegistry.HEARTSTOP_SPELL.get(),
                                SpellRegistry.WITHER_SKULL_SPELL.get()),
                        List.of(),
                        List.of(SpellRegistry.BLOOD_STEP_SPELL.get()),
                        List.of(SpellRegistry.RAISE_DEAD_SPELL.get()))
                .setSpellQuality(0.6F, 1.0F)
                .setMeleeBias(0.1F, 0.3F));
    }

    @Override
    protected SoundEvent getAmbientSound() { return SoundEvents.ZOMBIE_AMBIENT; }
    @Override
    protected SoundEvent getHurtSound(DamageSource source) { return SoundEvents.ZOMBIE_HURT; }
    @Override
    protected SoundEvent getDeathSound() { return SoundEvents.ZOMBIE_DEATH; }
}
