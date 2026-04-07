package com.littlh.bloodline;

import net.minecraft.world.level.block.CropBlock;
import net.minecraft.world.level.block.SoundType;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.material.MapColor;

public class PaleWheatBlock extends CropBlock {
    public PaleWheatBlock() {
        super(BlockBehaviour.Properties.of()
                .mapColor(MapColor.COLOR_GRAY)
                .noCollission()
                .randomTicks()
                .instabreak()
                .sound(SoundType.CROP));
    }
}
