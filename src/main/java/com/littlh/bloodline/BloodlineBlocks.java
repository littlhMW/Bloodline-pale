package com.littlh.bloodline;

import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.SoundType;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.material.MapColor;
import net.neoforged.neoforge.registries.DeferredBlock;
import net.neoforged.neoforge.registries.DeferredRegister;

public class BloodlineBlocks {
    public static final DeferredRegister.Blocks BLOCKS =
            DeferredRegister.createBlocks("bloodline");

    // 苍白草方块
    public static final DeferredBlock<Block> PALE_GRASS_BLOCK = BLOCKS.registerSimpleBlock(
            "pale_grass_block",
            BlockBehaviour.Properties.ofFullCopy(Blocks.GRASS_BLOCK)
                    .sound(SoundType.GRASS)
                    .mapColor(MapColor.COLOR_GRAY)
    );

    // 苍白小麦（两格高植物）
    public static final DeferredBlock<PaleWheatBlock> PALE_WHEAT = BLOCKS.register(
            "pale_wheat",
            PaleWheatBlock::new
    );

    // 苍白余烬（替代泥土）
    public static final DeferredBlock<Block> PALE_EMBER = BLOCKS.registerSimpleBlock(
            "pale_ember",
            BlockBehaviour.Properties.ofFullCopy(Blocks.DIRT)
                    .mapColor(MapColor.COLOR_GRAY)
                    .sound(SoundType.GRAVEL)
    );

    // 纯白骨块（替代石头）—— 修复：SoundType.BONE 改为 SoundType.BONE_BLOCK
    public static final DeferredBlock<Block> PALE_BONE_BLOCK = BLOCKS.registerSimpleBlock(
            "pale_bone_block",
            BlockBehaviour.Properties.ofFullCopy(Blocks.STONE)
                    .mapColor(MapColor.TERRACOTTA_WHITE)
                    .sound(SoundType.BONE_BLOCK)
                    .requiresCorrectToolForDrops()
    );
}