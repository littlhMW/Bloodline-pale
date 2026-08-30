package com.littlh.palelullaby;

import com.mojang.serialization.Codec;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.WorldGenLevel;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.LeavesBlock;
import net.minecraft.world.level.block.RotatedPillarBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.levelgen.Heightmap;
import net.minecraft.world.level.levelgen.feature.Feature;
import net.minecraft.world.level.levelgen.feature.FeaturePlaceContext;
import net.minecraft.world.level.levelgen.feature.configurations.NoneFeatureConfiguration;

public class MiniOakBushFeature extends Feature<NoneFeatureConfiguration> {
    public MiniOakBushFeature(Codec<NoneFeatureConfiguration> codec) {
        super(codec);
    }

    @Override
    public boolean place(FeaturePlaceContext<NoneFeatureConfiguration> context) {
        WorldGenLevel level = context.level();
        BlockPos origin = context.origin();
        RandomSource random = context.random();

        int x = origin.getX();
        int z = origin.getZ();
        int y = level.getHeight(Heightmap.Types.WORLD_SURFACE_WG, x, z) - 1;
        if (y < level.getMinBuildHeight() + 1) {
            return false;
        }
        BlockPos base = new BlockPos(x, y, z);
        if (!level.getBlockState(base.below()).isSolid()) {
            return false;
        }
        // 不要在水面/液体上生成，避免灌木树长在水里
        if (!level.getBlockState(base).getFluidState().isEmpty()
                || !level.getBlockState(base.above()).getFluidState().isEmpty()) {
            return false;
        }

        BlockState log = Blocks.OAK_LOG.defaultBlockState().setValue(RotatedPillarBlock.AXIS, Direction.Axis.Y);
        BlockState leaves = Blocks.OAK_LEAVES.defaultBlockState().setValue(LeavesBlock.PERSISTENT, true);

        int trunkHeight = 1 + random.nextInt(2); // 一颗短原木（1-2 格）
        BlockPos top = base;
        boolean placed = false;
        for (int i = 1; i <= trunkHeight; i++) {
            BlockPos pos = base.above(i);
            if (!level.isEmptyBlock(pos)) {
                break;
            }
            setBlock(level, pos, log);
            top = pos;
            placed = true;
        }
        if (!placed) {
            return false;
        }

        // 几簇树叶环绕在原木顶端
        int clusters = 3 + random.nextInt(3);
        for (int c = 0; c < clusters; c++) {
            int cx = top.getX() + (int) Math.round((random.nextDouble() - 0.5) * 4);
            int cz = top.getZ() + (int) Math.round((random.nextDouble() - 0.5) * 4);
            int cy = top.getY() + random.nextInt(2);
            int r = 1 + random.nextInt(2);
            placeLeafCluster(level, new BlockPos(cx, cy, cz), r, leaves, random);
        }
        return true;
    }

    private void placeLeafCluster(WorldGenLevel level, BlockPos center, int radius, BlockState leaves, RandomSource random) {
        for (int dx = -radius; dx <= radius; dx++) {
            for (int dy = -radius; dy <= radius; dy++) {
                for (int dz = -radius; dz <= radius; dz++) {
                    double dist = Math.sqrt(dx * dx + dy * dy + dz * dz);
                    if (dist > radius + 0.4) {
                        continue;
                    }
                    if (dist > radius - 0.5 && random.nextFloat() < 0.35F) {
                        continue;
                    }
                    BlockPos pos = center.offset(dx, dy, dz);
                    if (level.isEmptyBlock(pos)) {
                        // 不要在水面/液体上放置树叶，避免生成漂浮的“凋落物”
                        if (!level.getBlockState(pos.below()).getFluidState().isEmpty()) {
                            continue;
                        }
                        setBlock(level, pos, leaves);
                    }
                }
            }
        }
    }
}
