package com.littlh.palelullaby;

import com.mojang.serialization.Codec;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.WorldGenLevel;
import net.minecraft.world.level.block.LeavesBlock;
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
        int trunkHeight = 10 + random.nextInt(5);
        Direction shiftDir = Direction.Plane.HORIZONTAL.getRandomDirection(random);
        BlockPos trunkPos = base;
        for (int i = 0; i < trunkHeight; i++) {
            if (!level.isEmptyBlock(trunkPos.above())) {
                break;
            }
            trunkPos = trunkPos.above();
            placeTrunkSection(level, trunkPos, logY);
            if (random.nextInt(3) == 0) {
                shiftDir = random.nextBoolean() ? shiftDir.getClockWise() : shiftDir.getCounterClockWise();
                BlockPos next = trunkPos.relative(shiftDir);
                if (level.isEmptyBlock(next) && level.isEmptyBlock(next.above())) {
                    trunkPos = next;
                    placeTrunkSection(level, trunkPos, logY);
                }
            }
        }

        int branches = 5 + random.nextInt(3);
        for (int b = 0; b < branches; b++) {
            int branchY = 3 + random.nextInt(Math.max(1, trunkHeight - 5));
            BlockPos pos = base.above(branchY);
            Direction dir = Direction.Plane.HORIZONTAL.getRandomDirection(random);
            int len = 2 + random.nextInt(2);

            for (int j = 0; j < len; j++) {
                pos = pos.relative(dir);
                if (!level.isEmptyBlock(pos)) {
                    break;
                }
                setBlock(level, pos, log(dir.getAxis()));
                if (j == len - 1) {
                    placeFlatLeaves(level, pos, random);
                }
                if (random.nextBoolean()) {
                    BlockPos side = pos.relative(random.nextBoolean() ? dir.getClockWise() : dir.getCounterClockWise());
                    if (level.isEmptyBlock(side)) {
                        setBlock(level, side, log(dir.getAxis()));
                        placeFlatLeaves(level, side, random);
                    }
                }
            }
        }

        // 顶部扁平叶冠
        for (int dy = 0; dy <= 1; dy++) {
            placeFlatLeaves(level, base.above(trunkHeight + dy), random);
        }
        return true;
    }

    private BlockState log(Direction.Axis axis) {
        return PaleLullabyBlocks.THORN_LOG.get().defaultBlockState().setValue(RotatedPillarBlock.AXIS, axis);
    }

    private void placeTrunkSection(WorldGenLevel level, BlockPos basePos, BlockState state) {
        for (int dx = 0; dx <= 1; dx++) {
            for (int dz = 0; dz <= 1; dz++) {
                BlockPos pos = basePos.offset(dx, 0, dz);
                if (level.isEmptyBlock(pos)) {
                    setBlock(level, pos, state);
                }
            }
        }
    }

    private void placeFlatLeaves(WorldGenLevel level, BlockPos center, RandomSource random) {
        BlockState leaves = PaleLullabyBlocks.THORN_LEAVES.get().defaultBlockState()
                .setValue(LeavesBlock.PERSISTENT, true);
        for (int dx = -2; dx <= 2; dx++) {
            for (int dz = -2; dz <= 2; dz++) {
                if (Math.abs(dx) + Math.abs(dz) > 3) {
                    continue;
                }
                BlockPos pos = center.offset(dx, 0, dz);
                if (level.isEmptyBlock(pos)) {
                    setBlock(level, pos, leaves);
                }
                if (Math.abs(dx) + Math.abs(dz) <= 1) {
                    BlockPos above = pos.above();
                    if (level.isEmptyBlock(above) && random.nextFloat() < 0.25F) {
                        setBlock(level, above, leaves);
                    }
                }
            }
        }
    }
}
