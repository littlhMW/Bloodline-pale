package com.littlh.palelullaby;

import com.mojang.serialization.Codec;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.tags.BlockTags;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.WorldGenLevel;
import net.minecraft.world.level.block.VineBlock;
import net.minecraft.world.level.block.state.properties.BooleanProperty;
import net.minecraft.world.level.levelgen.Heightmap;
import net.minecraft.world.level.levelgen.feature.Feature;
import net.minecraft.world.level.levelgen.feature.FeaturePlaceContext;
import net.minecraft.world.level.levelgen.feature.configurations.NoneFeatureConfiguration;

/** 凋萎槲寄生：在荒芜高原的树干/横木上挂灰白槲寄生藤。 */
public class WitheredMistletoeFeature extends Feature<NoneFeatureConfiguration> {
    public WitheredMistletoeFeature(Codec<NoneFeatureConfiguration> codec) {
        super(codec);
    }

    @Override
    public boolean place(FeaturePlaceContext<NoneFeatureConfiguration> context) {
        WorldGenLevel level = context.level();
        BlockPos origin = context.origin();
        RandomSource random = context.random();
        boolean generated = false;
        for (int i = 0; i < 10; i++) {
            int x = origin.getX() + random.nextInt(16);
            int z = origin.getZ() + random.nextInt(16);
            int top = level.getHeight(Heightmap.Types.WORLD_SURFACE_WG, x, z);
            BlockPos anchor = null;
            // 地面上的横木
            BlockPos ground = new BlockPos(x, top, z);
            if (level.getBlockState(ground).is(BlockTags.LOGS)) {
                anchor = ground;
            } else {
                // 从地表向上找树干
                int maxY = Math.min(level.getMaxBuildHeight() - 1, top + 28);
                for (int y = top + 1; y <= maxY; y++) {
                    BlockPos p = new BlockPos(x, y, z);
                    if (level.getBlockState(p).is(BlockTags.LOGS)) {
                        anchor = p;
                        break;
                    }
                }
            }
            if (anchor == null) {
                continue;
            }
            if (tryHangChain(level, anchor, random)) {
                generated = true;
            }
        }
        return generated;
    }

    private boolean tryHangChain(WorldGenLevel level, BlockPos anchor, RandomSource random) {
        Direction side = Direction.Plane.HORIZONTAL.getRandomDirection(random);
        BlockPos start = anchor.relative(side);
        if (!level.isEmptyBlock(start)) {
            return false;
        }
        BooleanProperty face = VineBlock.getPropertyForFace(side.getOpposite());
        int len = 2 + random.nextInt(4); // 2-5 格
        boolean placed = false;
        for (int i = 0; i < len; i++) {
            BlockPos vp = start.below(i);
            if (!level.isEmptyBlock(vp)) {
                break;
            }
            setBlock(level, vp, PaleLullabyBlocks.WITHERED_MISTLETOE.get().defaultBlockState().setValue(face, true));
            placed = true;
        }
        return placed;
    }
}
