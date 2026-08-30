package com.littlh.palelullaby;

import net.minecraft.world.level.block.RotatedPillarBlock;
import net.minecraft.world.level.block.SoundType;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.material.MapColor;

public class RedNeedleLogBlock extends RotatedPillarBlock {
    public RedNeedleLogBlock() {
        super(BlockBehaviour.Properties.of()
                .mapColor(MapColor.COLOR_RED)
                .strength(2.0F)
                .sound(SoundType.WOOD)
        );
    }
}
