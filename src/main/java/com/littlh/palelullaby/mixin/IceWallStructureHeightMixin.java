package com.littlh.palelullaby.mixin;

import com.littlh.palelullaby.IceSheetFeature;
import com.littlh.palelullaby.GothicCliffFeature;
import com.littlh.palelullaby.PaleLullabyStructureSeed;
import com.littlh.palelullaby.WitheredSpireFeature;
import net.minecraft.core.QuartPos;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.biome.Biome;
import net.minecraft.world.level.chunk.ChunkGenerator;
import net.minecraft.world.level.levelgen.Heightmap;
import net.minecraft.world.level.LevelHeightAccessor;
import net.minecraft.world.level.levelgen.RandomState;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

/**
 * 让大型地物（巨大冰壁、凋萎石刺、哥特悬崖）群系内的结构生成在地物顶面上，
 * 而不是生成在地物尚未铺放时的原始地表（会被地物埋住）。
 */
@Mixin(ChunkGenerator.class)
public abstract class IceWallStructureHeightMixin {

    private static final ResourceKey<Biome> GIANT_ICE_WALL = ResourceKey.create(
            Registries.BIOME, ResourceLocation.fromNamespaceAndPath("pale_lullaby", "giant_ice_wall"));
    private static final ResourceKey<Biome> WITHERED_PLATEAU = ResourceKey.create(
            Registries.BIOME, ResourceLocation.fromNamespaceAndPath("pale_lullaby", "withered_plateau"));
    private static final ResourceKey<Biome> CRIMSON_GARDEN = ResourceKey.create(
            Registries.BIOME, ResourceLocation.fromNamespaceAndPath("pale_lullaby", "crimson_garden"));

    @Shadow
    public abstract net.minecraft.world.level.biome.BiomeSource getBiomeSource();

    @Shadow
    public abstract int getBaseHeight(int x, int z, Heightmap.Types type,
                                      LevelHeightAccessor heightAccessor, RandomState randomState);

    @Inject(method = "getFirstFreeHeight", at = @At("HEAD"), cancellable = true)
    private void paleLullaby$iceWallFirstFreeHeight(int x, int z, Heightmap.Types type,
                                                    LevelHeightAccessor heightAccessor, RandomState randomState,
                                                    CallbackInfoReturnable<Integer> cir) {
        Long seed = PaleLullabyStructureSeed.get();
        if (seed == null) {
            return;
        }
        int featureTop = featureTopAt(x, z, heightAccessor, randomState, seed);
        if (featureTop == Integer.MIN_VALUE) {
            return;
        }
        cir.setReturnValue(Math.max(featureTop, this.getBaseHeight(x, z, type, heightAccessor, randomState)));
    }

    @Inject(method = "getFirstOccupiedHeight", at = @At("HEAD"), cancellable = true)
    private void paleLullaby$iceWallFirstOccupiedHeight(int x, int z, Heightmap.Types type,
                                                        LevelHeightAccessor heightAccessor, RandomState randomState,
                                                        CallbackInfoReturnable<Integer> cir) {
        Long seed = PaleLullabyStructureSeed.get();
        if (seed == null) {
            return;
        }
        int featureTop = featureTopAt(x, z, heightAccessor, randomState, seed);
        if (featureTop == Integer.MIN_VALUE) {
            return;
        }
        cir.setReturnValue(Math.max(featureTop, this.getBaseHeight(x, z, type, heightAccessor, randomState) - 1));
    }

    @Unique
    private int featureTopAt(int x, int z, LevelHeightAccessor heightAccessor, RandomState randomState, long seed) {
        int ground = this.getBaseHeight(x, z, Heightmap.Types.WORLD_SURFACE_WG, heightAccessor, randomState);
        ChunkGenerator generator = (ChunkGenerator) (Object) this;
        var biome = this.getBiomeSource().getNoiseBiome(
                QuartPos.fromBlock(x), QuartPos.fromBlock(ground), QuartPos.fromBlock(z), randomState.sampler());
        if (biome.is(GIANT_ICE_WALL)) {
            return IceSheetFeature.iceWallTopAt(generator, randomState, heightAccessor, x, z, seed);
        }
        if (biome.is(WITHERED_PLATEAU)) {
            return WitheredSpireFeature.spireTopAt(generator, randomState, heightAccessor, x, z, seed);
        }
        if (biome.is(CRIMSON_GARDEN)) {
            return GothicCliffFeature.cliffTopAt(generator, randomState, heightAccessor, x, z, seed);
        }
        return Integer.MIN_VALUE;
    }
}



