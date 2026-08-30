package com.littlh.palelullaby;

import com.mojang.serialization.Codec;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.QuartPos;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.WorldGenLevel;
import net.minecraft.world.level.biome.Biome;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.DripstoneThickness;
import net.minecraft.world.level.chunk.ChunkAccess;
import net.minecraft.world.level.levelgen.Heightmap;
import net.minecraft.world.level.levelgen.feature.Feature;
import net.minecraft.world.level.levelgen.feature.FeaturePlaceContext;
import net.minecraft.world.level.levelgen.feature.configurations.NoneFeatureConfiguration;

public class IceSpikeFeature extends Feature<NoneFeatureConfiguration> {
    private static final ResourceKey<Biome> GIANT_ICE_WALL = ResourceKey.create(
            Registries.BIOME, ResourceLocation.fromNamespaceAndPath(PaleLullaby.MOD_ID, "giant_ice_wall"));

    public IceSpikeFeature(Codec<NoneFeatureConfiguration> codec) {
        super(codec);
    }

    @Override
    public boolean place(FeaturePlaceContext<NoneFeatureConfiguration> context) {
        WorldGenLevel level = context.level();
        long seed = level.getSeed();
        int baseX = (context.origin().getX() >> 4) << 4;
        int baseZ = (context.origin().getZ() >> 4) << 4;
        ChunkAccess chunk = level.getChunk(baseX >> 4, baseZ >> 4);
        boolean generated = false;

        for (int x = 0; x < 16; x++) {
            for (int z = 0; z < 16; z++) {
                int wx = baseX + x;
                int wz = baseZ + z;
                int iceTop = level.getHeight(Heightmap.Types.WORLD_SURFACE_WG, wx, wz);
                if (!isIceWall(chunk, wx, iceTop, wz)) {
                    continue;
                }
                long h = hash(wx, wz, seed ^ 0x51ED5EEDL);
                if ((h & 0xFFL) >= 20) {
                    continue;
                }
                int length = 1 + (int) ((h >>> 8) % 3L);
                int maxScan = Math.min(level.getMaxBuildHeight() - 1, iceTop + 60);
                int minScan = Math.max(level.getMinBuildHeight() + 2, iceTop - 90);
                for (int y = maxScan; y >= minScan; y--) {
                    if (!isIceCeiling(level.getBlockState(new BlockPos(wx, y, wz)))) {
                        continue;
                    }
                    if (!level.getBlockState(new BlockPos(wx, y - 1, wz)).isAir()) {
                        continue;
                    }
                    if (placeSpikeColumn(level, wx, y - 1, wz, length)) {
                        generated = true;
                    }
                    break;
                }
            }
        }
        return generated;
    }

    private static boolean placeSpikeColumn(WorldGenLevel level, int wx, int topY, int wz, int length) {
        BlockState base = PaleLullabyBlocks.ICE_SPIKE.get().defaultBlockState()
                .setValue(IceSpikeBlock.TIP_DIRECTION, Direction.DOWN);
        boolean placed = false;
        boolean blocked = false;
        if (length == 1) {
            placed = tryPlace(level, wx, topY, wz, base.setValue(IceSpikeBlock.THICKNESS, DripstoneThickness.TIP));
        } else {
            placed = tryPlace(level, wx, topY, wz,
                    base.setValue(IceSpikeBlock.THICKNESS,
                            length == 2 ? DripstoneThickness.FRUSTUM : DripstoneThickness.BASE));
            for (int i = 1; i < length - 1; i++) {
                if (!tryPlace(level, wx, topY - i, wz,
                        base.setValue(IceSpikeBlock.THICKNESS, DripstoneThickness.MIDDLE))) {
                    blocked = true;
                    break;
                }
            }
            if (!blocked) {
                placed |= tryPlace(level, wx, topY - length + 1, wz,
                        base.setValue(IceSpikeBlock.THICKNESS, DripstoneThickness.TIP));
            }
        }
        return placed;
    }

    private static boolean tryPlace(WorldGenLevel level, int wx, int y, int wz, BlockState state) {
        BlockPos pos = new BlockPos(wx, y, wz);
        if (!level.getBlockState(pos).isAir()) {
            return false;
        }
        level.setBlock(pos, state, 3);
        return true;
    }

    private static boolean isIceCeiling(BlockState state) {
        return state.is(Blocks.ICE)
                || state.is(Blocks.PACKED_ICE)
                || state.is(Blocks.BLUE_ICE)
                || state.is(Blocks.FROSTED_ICE)
                || state.is(Blocks.SNOW_BLOCK)
                || state.is(Blocks.CALCITE);
    }

    private static boolean isIceWall(ChunkAccess chunk, int x, int y, int z) {
        return chunk.getNoiseBiome(QuartPos.fromBlock(x), QuartPos.fromBlock(y), QuartPos.fromBlock(z))
                .is(GIANT_ICE_WALL);
    }

    private long hash(int x, int z, long seed) {
        long h = seed;
        h ^= x * 0x9E3779B97F4A7C15L;
        h ^= z * 0xBF58476D1CE4E5B9L;
        h ^= h >>> 29;
        h *= 0x94D049BB133111EBL;
        h ^= h >>> 31;
        return h;
    }
}

