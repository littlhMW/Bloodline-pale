package com.littlh.palelullaby;

import com.mojang.serialization.Codec;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.WorldGenLevel;
import net.minecraft.world.level.block.DoublePlantBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.DoubleBlockHalf;
import net.minecraft.world.level.levelgen.feature.Feature;
import net.minecraft.world.level.levelgen.feature.FeaturePlaceContext;
import net.minecraft.world.level.levelgen.feature.configurations.SimpleBlockConfiguration;

public class SafePlantFeature extends Feature<SimpleBlockConfiguration> {
    public SafePlantFeature(Codec<SimpleBlockConfiguration> codec) {
        super(codec);
    }

    @Override
    public boolean place(FeaturePlaceContext<SimpleBlockConfiguration> context) {
        SimpleBlockConfiguration config = context.config();
        WorldGenLevel level = context.level();
        BlockPos pos = context.origin();
        BlockState state = config.toPlace().getState(context.random(), pos);
        if (!state.canSurvive(level, pos)) {
            return false;
        }
        removeDoublePlantHalf(level, pos);
        removeDoublePlantHalf(level, pos.above());
        if (state.getBlock() instanceof DoublePlantBlock) {
            // 两格高植物（如寡妇刺）：检查上方空间后整株放置
            if (!level.isEmptyBlock(pos.above())) {
                return false;
            }
            DoublePlantBlock.placeAt(level, state, pos, 2);
        } else {
            level.setBlock(pos, state, 2);
        }
        return true;
    }

    private static void removeDoublePlantHalf(WorldGenLevel level, BlockPos pos) {
        BlockState state = level.getBlockState(pos);
        if (!(state.getBlock() instanceof DoublePlantBlock)) {
            return;
        }
        if (state.getValue(DoublePlantBlock.HALF) == DoubleBlockHalf.LOWER) {
            level.removeBlock(pos.above(), false);
        } else {
            level.removeBlock(pos.below(), false);
        }
    }
}
