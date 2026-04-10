package com.littlh.bloodline;

import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.GrassBlock;
import net.minecraft.world.level.block.SoundType;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.material.MapColor;

public class PaleGrassBlock extends GrassBlock {

    public PaleGrassBlock() {
        super(BlockBehaviour.Properties.of()
                .mapColor(MapColor.COLOR_GRAY)
                .randomTicks()
                .strength(0.6F)
                .sound(SoundType.GRASS)
        );
    }

    @Override
    public void randomTick(BlockState state, ServerLevel level, BlockPos pos, RandomSource random) {
        if (!canBeGrass(state, level, pos)) {
            level.setBlockAndUpdate(pos, BloodlineBlocks.PALE_EMBER.get().defaultBlockState());
        } else {
            for (int i = 0; i < 4; ++i) {
                BlockPos targetPos = pos.offset(
                        random.nextInt(3) - 1,
                        random.nextInt(5) - 3,
                        random.nextInt(3) - 1
                );
                BlockState targetState = level.getBlockState(targetPos);
                if (targetState.is(BloodlineBlocks.PALE_EMBER.get()) && canPropagate(state, level, targetPos)) {
                    level.setBlockAndUpdate(targetPos, this.defaultBlockState());
                }
            }
        }
    }

    private static boolean canPropagate(BlockState state, ServerLevel level, BlockPos pos) {
        BlockPos posAbove = pos.above();
        return level.getRawBrightness(posAbove, 0) >= 9;
    }

    private static boolean canBeGrass(BlockState state, ServerLevel level, BlockPos pos) {
        BlockPos posAbove = pos.above();
        BlockState blockAbove = level.getBlockState(posAbove);
        return !blockAbove.isSolidRender(level, posAbove) && !blockAbove.is(Blocks.WATER);
    }
}
