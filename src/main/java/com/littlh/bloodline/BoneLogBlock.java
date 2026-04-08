package com.littlh.bloodline;

import net.minecraft.world.level.block.RotatedPillarBlock;
import net.minecraft.world.level.block.SoundType;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.material.MapColor;

public class BoneLogBlock extends RotatedPillarBlock {
    public BoneLogBlock() {
        super(BlockBehaviour.Properties.of()
                .mapColor(MapColor.TERRACOTTA_WHITE)
                .strength(2.0F)
                .sound(SoundType.BONE_BLOCK)
                .requiresCorrectToolForDrops()
        );
    }
}
