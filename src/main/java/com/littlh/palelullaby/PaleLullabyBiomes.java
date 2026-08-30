package com.littlh.palelullaby;

import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.biome.Biome;

/** 本模组自定义群系。 */
public final class PaleLullabyBiomes {
    public static final ResourceKey<Biome> CRIMSON_GARDEN = ResourceKey.create(
            Registries.BIOME, ResourceLocation.fromNamespaceAndPath(PaleLullaby.MOD_ID, "crimson_garden"));
    public static final ResourceKey<Biome> WITHERED_PLATEAU = ResourceKey.create(
            Registries.BIOME, ResourceLocation.fromNamespaceAndPath(PaleLullaby.MOD_ID, "withered_plateau"));

    private PaleLullabyBiomes() {
    }
}
