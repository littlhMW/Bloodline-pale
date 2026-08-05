package com.littlh.palelullaby;

import com.mojang.serialization.Codec;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.WorldGenLevel;
import net.minecraft.world.level.block.RotatedPillarBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.levelgen.Heightmap;
import net.minecraft.world.level.levelgen.feature.Feature;
import net.minecraft.world.level.levelgen.feature.FeaturePlaceContext;
import net.minecraft.world.level.levelgen.feature.configurations.NoneFeatureConfiguration;

public class ThornTreeFeature extends Feature<NoneFeatureConfiguration> {
    public ThornTreeFeature(Codec<NoneFeatureConfiguration> codec) {
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

        BlockState logY = log(Direction.Axis.Y);
        // 高大主干 13~19 格
        int trunkHeight = 13 + random.nextInt(7);
        for (int i = 1; i <= trunkHeight; i++) {
            BlockPos p = base.above(i);
            if (level.isEmptyBlock(p)) {
                setBlock(level, p, logY);
            }
        }

        // 多方向粗大分叉，横向枝干原木轴向正确
        int branches = 8 + random.nextInt(4);
        for (int b = 0; b < branches; b++) {
            int branchY = 4 + random.nextInt(Math.max(1, trunkHeight - 6));
            BlockPos pos = base.above(branchY);
            Direction dir = Direction.Plane.HORIZONTAL.getRandomDirection(random);
            int len = 4 + random.nextInt(4);

            for (int j = 0; j < len; j++) {
                if (j % 2 == 1 && j < len - 1 && random.nextBoolean()) {
                    pos = pos.above(1);
                    if (!level.isEmptyBlock(pos)) {
                        break;
                    }
                    setBlock(level, pos, logY);
                } else {
                    pos = pos.relative(dir);
                    if (!level.isEmptyBlock(pos)) {
                        break;
                    }
                    setBlock(level, pos, log(dir.getAxis()));
                }

                if (j == len - 1) {
                    placeLeafCluster(level, pos, random);
                    if (random.nextBoolean()) {
                        placeLeafCluster(level, pos.above(1), random);
                    }
                }

                // 枝条中段随机发出次级分叉
                if (j >= 3 && random.nextInt(3) == 0) {
                    Direction sub = random.nextBoolean() ? dir.getClockWise() : dir.getCounterClockWise();
                    BlockPos q = pos;
                    for (int k = 0; k < 3; k++) {
                        q = k == 0 ? q.above(1) : q.relative(sub);
                        if (!level.isEmptyBlock(q)) {
                            break;
                        }
                        setBlock(level, q, k == 0 ? logY : log(sub.getAxis()));
                    }
                    placeLeafCluster(level, q, random);
                }
            }
        }

        // 顶部伞形成簇树冠
        for (int i = 0; i < 4; i++) {
            placeLeafCluster(level, base.above(trunkHeight - (i / 2)), random);
        }
        return true;
    }

    private BlockState log(Direction.Axis axis) {
        return PaleLullabyBlocks.THORN_LOG.get().defaultBlockState().setValue(RotatedPillarBlock.AXIS, axis);
    }

    private void placeLeafCluster(WorldGenLevel level, BlockPos center, RandomSource random) {
        BlockState leaves = PaleLullabyBlocks.THORN_LEAVES.get().defaultBlockState();
        for (int dx = -2; dx <= 2; dx++) {
            for (int dy = -1; dy <= 1; dy++) {
                for (int dz = -2; dz <= 2; dz++) {
                    int manhattan = Math.abs(dx) + Math.abs(dy) + Math.abs(dz);
                    if (manhattan > 3) {
                        continue;
                    }
                    if (manhattan > 1 && random.nextFloat() < 0.35F) {
                        continue;
                    }
                    BlockPos p = center.offset(dx, dy, dz);
                    if (level.isEmptyBlock(p)) {
                        setBlock(level, p, leaves);
                    }
                }
            }
        }
    }
}
