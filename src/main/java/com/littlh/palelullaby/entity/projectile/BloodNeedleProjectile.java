package com.littlh.palelullaby.entity.projectile;

import com.littlh.palelullaby.PaleLullabyItems;
import net.minecraft.core.particles.ParticleOptions;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.projectile.AbstractHurtingProjectile;
import net.minecraft.world.entity.projectile.ItemSupplier;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.EntityHitResult;
import net.minecraft.world.phys.Vec3;

/**
 * Straight-flying blood needle fired by self-written vampires. Simple
 * indirect-magic damage, no gravity, no explosions.
 */
public class BloodNeedleProjectile extends AbstractHurtingProjectile implements ItemSupplier {
    private float damage = 3.0F;

    public BloodNeedleProjectile(EntityType<BloodNeedleProjectile> entityType, Level level) {
        super(entityType, level);
    }

    public BloodNeedleProjectile(EntityType<BloodNeedleProjectile> entityType, LivingEntity owner, Vec3 velocity, Level level) {
        super(entityType, owner, velocity, level);
    }

    public void setDamage(float damage) {
        this.damage = damage;
    }

    @Override
    protected boolean shouldBurn() {
        return false;
    }

    @Override
    protected ParticleOptions getTrailParticle() {
        return ParticleTypes.CRIMSON_SPORE;
    }

    @Override
    protected void onHitEntity(EntityHitResult hitResult) {
        if (!this.level().isClientSide) {
            Entity target = hitResult.getEntity();
            Entity owner = this.getOwner();
            if (owner instanceof LivingEntity livingOwner && target != owner && target.isAlive()) {
                target.hurt(this.damageSources().indirectMagic(this, livingOwner), this.damage);
                this.level().addParticle(ParticleTypes.DAMAGE_INDICATOR,
                        target.getX(), target.getY() + target.getBbHeight() * 0.5D, target.getZ(), 0, 0.1D, 0);
                for (int i = 0; i < 6; i++) {
                    this.level().addParticle(ParticleTypes.CRIT,
                            target.getX() + this.random.nextGaussian() * 0.4D,
                            target.getY() + this.random.nextDouble() * target.getBbHeight(),
                            target.getZ() + this.random.nextGaussian() * 0.4D, 0, 0.05D, 0);
                }
                this.level().playSound(null, target.getX(), target.getY(), target.getZ(),
                        SoundEvents.PLAYER_ATTACK_CRIT, SoundSource.HOSTILE, 0.8F, 1.0F);
            }
            this.discard();
        }
    }

    @Override
    protected void onHitBlock(BlockHitResult hitResult) {
        this.discard();
    }

    @Override
    public ItemStack getItem() {
        return new ItemStack(PaleLullabyItems.UNREFINED_BLOOD_BOTTLE.get());
    }
}
