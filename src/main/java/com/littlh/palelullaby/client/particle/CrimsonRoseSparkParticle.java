package com.littlh.palelullaby.client.particle;

import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.particle.Particle;
import net.minecraft.client.particle.ParticleProvider;
import net.minecraft.client.particle.ParticleRenderType;
import net.minecraft.client.particle.SpriteSet;
import net.minecraft.client.particle.TextureSheetParticle;
import net.minecraft.core.particles.SimpleParticleType;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.api.distmarker.OnlyIn;

/** Self-illuminating crimson rose spark; falls slowly along a curve instead of using the vanilla Glow particle. */
@OnlyIn(Dist.CLIENT)
public class CrimsonRoseSparkParticle extends TextureSheetParticle {
    private final SpriteSet sprites;
    private final double swaySeed;
    private final double fallSpeed;

    protected CrimsonRoseSparkParticle(ClientLevel level, double x, double y, double z, SpriteSet sprites) {
        super(level, x, y, z);
        this.sprites = sprites;
        this.swaySeed = this.random.nextDouble() * Math.PI * 2.0D;
        this.fallSpeed = 0.004D + this.random.nextDouble() * 0.006D;
        this.lifetime = 90 + this.random.nextInt(50);
        this.gravity = 0.0F;
        this.hasPhysics = false;
        this.quadSize = 0.1F + this.random.nextFloat() * 0.1F;
        this.setColor(0.9F, 0.04F + this.random.nextFloat() * 0.06F, 0.06F + this.random.nextFloat() * 0.05F);
        this.setSpriteFromAge(sprites);
    }

    @Override
    public ParticleRenderType getRenderType() {
        return ParticleRenderType.PARTICLE_SHEET_TRANSLUCENT;
    }

    @Override
    public int getLightColor(float partialTick) {
        return 0xF000F0;
    }

    @Override
    public void tick() {
        this.xo = this.x;
        this.yo = this.y;
        this.zo = this.z;
        if (this.age++ >= this.lifetime) {
            this.remove();
            return;
        }
        this.xd = Math.sin(this.age * 0.09D + this.swaySeed) * 0.006D;
        this.zd = Math.cos(this.age * 0.07D + this.swaySeed * 1.7D) * 0.006D;
        this.yd = -this.fallSpeed;
        this.move(this.xd, this.yd, this.zd);
        this.setSpriteFromAge(this.sprites);
    }

    @Override
    public float getQuadSize(float scaleFactor) {
        float remaining = 1.0F - (float) this.age / (float) this.lifetime;
        return this.quadSize * (0.4F + 0.6F * remaining);
    }

    @OnlyIn(Dist.CLIENT)
    public static class Provider implements ParticleProvider<SimpleParticleType> {
        private final SpriteSet sprites;

        public Provider(SpriteSet sprites) {
            this.sprites = sprites;
        }

        @Override
        public Particle createParticle(SimpleParticleType type, ClientLevel level, double x, double y, double z,
                                       double xSpeed, double ySpeed, double zSpeed) {
            return new CrimsonRoseSparkParticle(level, x, y, z, this.sprites);
        }
    }
}
