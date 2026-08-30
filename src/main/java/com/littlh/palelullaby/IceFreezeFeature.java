package com.littlh.palelullaby;

import com.mojang.serialization.Codec;
import net.minecraft.core.BlockPos;
import net.minecraft.core.QuartPos;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.FluidTags;
import net.minecraft.world.level.WorldGenLevel;
import net.minecraft.world.level.biome.Biome;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.SnowLayerBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.chunk.ChunkAccess;
import net.minecraft.world.level.levelgen.Heightmap;
import net.minecraft.world.level.levelgen.feature.Feature;
import net.minecraft.world.level.levelgen.feature.FeaturePlaceContext;
import net.minecraft.world.level.levelgen.feature.configurations.NoneFeatureConfiguration;

public class IceFreezeFeature extends Feature<NoneFeatureConfiguration> {
    private static final ResourceKey<Biome> GIANT_ICE_WALL = ResourceKey.create(
            Registries.BIOME, ResourceLocation.fromNamespaceAndPath(PaleLullaby.MOD_ID, "giant_ice_wall"));

    public IceFreezeFeature(Codec<NoneFeatureConfiguration> codec) {
        super(codec);
    }

    @Override
    public boolean place(FeaturePlaceContext<NoneFeatureConfiguration> context) {
        WorldGenLevel level = context.level();
        int baseX = (context.origin().getX() >> 4) << 4;
        int baseZ = (context.origin().getZ() >> 4) << 4;
        ChunkAccess chunk = level.getChunk(baseX >> 4, baseZ >> 4);
        int minY = level.getMinBuildHeight();
        int maxY = level.getMaxBuildHeight() - 1;
        boolean generated = false;

        for (int x = 0; x < 16; x++) {
            for (int z = 0; z < 16; z++) {
                int wx = baseX + x;
                int wz = baseZ + z;
                int surface = level.getHeight(Heightmap.Types.WORLD_SURFACE_WG, wx, wz);
                if (!isIceWall(chunk, wx, surface, wz)) {
                    continue;
                }
                // 水面不可能高于地表，从地表往下找最高水面即可：冻成冰，上方若有空气则盖一层雪。
                // 原实现从世界最高处整列扫描，在大冰壁群系里每次要扫过几百格实心冰，非常慢。
                int scanStart = Math.min(maxY, surface + 2);
                for (int y = scanStart; y >= minY; y--) {
                    BlockState state = level.getBlockState(new BlockPos(wx, y, wz));
                    if (!state.getFluidState().is(FluidTags.WATER)) {
                        continue;
                    }
                    level.setBlock(new BlockPos(wx, y, wz), Blocks.ICE.defaultBlockState(), 3);
                    BlockPos above = new BlockPos(wx, y + 1, wz);
                    if (level.getBlockState(above).isAir()) {
                        level.setBlock(above, Blocks.SNOW.defaultBlockState()
                                .setValue(SnowLayerBlock.LAYERS, 1), 3);
                    }
                    generated = true;
                    break;
                }
            }
        }
        return generated;
    }

    private static boolean isIceWall(ChunkAccess chunk, int x, int y, int z) {
        return chunk.getNoiseBiome(QuartPos.fromBlock(x), QuartPos.fromBlock(y), QuartPos.fromBlock(z))
                .is(GIANT_ICE_WALL);
    }
}
