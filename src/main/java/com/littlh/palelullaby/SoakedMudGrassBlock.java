package com.littlh.palelullaby;

import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.GrassBlock;
import net.minecraft.world.level.block.SoundType;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.material.MapColor;

/**
 * 浸润淤泥草方块：猩红花园地表草皮。光照不足时退化为浸润淤泥，
 * 并会向周围浸润淤泥蔓延，行为与苍白草方块一致。
 */
public class SoakedMudGrassBlock extends GrassBlock {

    public SoakedMudGrassBlock() {
        super(BlockBehaviour.Properties.of()
                .mapColor(MapColor.CRIMSON_STEM)
                .randomTicks()
                .strength(0.6F)
                .sound(SoundType.GRASS)
        );
    }

    @Override
    public void randomTick(BlockState state, ServerLevel level, BlockPos pos, RandomSource random) {
        if (!canBeGrass(state, level, pos)) {
            level.setBlockAndUpdate(pos, PaleLullabyBlocks.SOAKED_MUD.get().defaultBlockState());
        } else {
            for (int i = 0; i < 4; ++i) {
                BlockPos targetPos = pos.offset(
                        random.nextInt(3) - 1,
                        random.nextInt(5) - 3,
                        random.nextInt(3) - 1
                );
                BlockState targetState = level.getBlockState(targetPos);
                if (targetState.is(PaleLullabyBlocks.SOAKED_MUD.get()) && canPropagate(state, level, targetPos)) {
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
