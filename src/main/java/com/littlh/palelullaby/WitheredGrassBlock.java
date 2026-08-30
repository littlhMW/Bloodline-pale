package com.littlh.palelullaby;

import javax.annotation.Nullable;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.RandomSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.GrassBlock;
import net.minecraft.world.level.block.SoundType;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.material.MapColor;

public class WitheredGrassBlock extends GrassBlock {
    public WitheredGrassBlock() {
        super(BlockBehaviour.Properties.of()
                .mapColor(MapColor.COLOR_LIGHT_GRAY)
                .randomTicks()
                .strength(0.6F)
                .sound(SoundType.GRASS));
    }

    @Override
    public SoundType getSoundType(BlockState state, LevelReader level, BlockPos pos, @Nullable Entity entity) {
        return SoundType.GRASS;
    }

    @Override
    public void randomTick(BlockState state, ServerLevel level, BlockPos pos, RandomSource random) {
        if (!canBeGrass(state, level, pos)) {
            level.setBlockAndUpdate(pos, PaleLullabyBlocks.WITHERED_DIRT.get().defaultBlockState());
        } else {
            for (int i = 0; i < 4; ++i) {
                BlockPos targetPos = pos.offset(
                        random.nextInt(3) - 1,
                        random.nextInt(5) - 3,
                        random.nextInt(3) - 1
                );
                BlockState targetState = level.getBlockState(targetPos);
                if (targetState.is(PaleLullabyBlocks.WITHERED_DIRT.get()) && canPropagate(state, level, targetPos)) {
                    level.setBlockAndUpdate(targetPos, this.defaultBlockState());
                }
            }
        }
    }

    private static boolean canPropagate(BlockState state, ServerLevel level, BlockPos pos) {
        return level.getRawBrightness(pos.above(), 0) >= 9;
    }

    private static boolean canBeGrass(BlockState state, ServerLevel level, BlockPos pos) {
        BlockPos posAbove = pos.above();
        BlockState blockAbove = level.getBlockState(posAbove);
        return !blockAbove.isSolidRender(level, posAbove) && !blockAbove.is(Blocks.WATER);
    }
}
