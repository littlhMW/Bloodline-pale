package com.littlh.palelullaby;

import net.minecraft.core.particles.ParticleType;
import net.minecraft.core.particles.SimpleParticleType;
import net.minecraft.core.registries.Registries;
import net.neoforged.neoforge.registries.DeferredRegister;

import java.util.function.Supplier;

public final class PaleLullabyParticles {
    public static final DeferredRegister<ParticleType<?>> PARTICLES =
            DeferredRegister.create(Registries.PARTICLE_TYPE, PaleLullaby.MOD_ID);

    public static final Supplier<SimpleParticleType> CRIMSON_ROSE_SPARK =
            PARTICLES.register("crimson_rose_spark", () -> new SimpleParticleType(false));

    private PaleLullabyParticles() {
    }
}
