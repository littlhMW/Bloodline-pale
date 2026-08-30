package com.littlh.palelullaby;

import javax.annotation.Nullable;
import net.minecraft.core.BlockPos;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.SoundType;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.material.MapColor;

public class WitheredDirtBlock extends Block {
    public WitheredDirtBlock() {
        super(BlockBehaviour.Properties.ofFullCopy(Blocks.DIRT)
                .mapColor(MapColor.COLOR_LIGHT_GRAY)
                .sound(SoundType.GRAVEL));
    }

    @Override
    public SoundType getSoundType(BlockState state, LevelReader level, BlockPos pos, @Nullable Entity entity) {
        return SoundType.GRAVEL;
    }
}
