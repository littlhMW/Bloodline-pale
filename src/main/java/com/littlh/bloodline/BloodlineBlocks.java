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

    // 苍白草方块（继承 GrassBlock，拥有蔓延和枯萎能力）
    public static final DeferredBlock<PaleGrassBlock> PALE_GRASS_BLOCK = BLOCKS.register(
            "pale_grass_block",
            PaleGrassBlock::new
    );

    // 苍白小麦（两格高植物，仅下半选中，可被替换）
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

    // 纯白骨块（替代石头）
    public static final DeferredBlock<Block> PALE_BONE_BLOCK = BLOCKS.registerSimpleBlock(
            "pale_bone_block",
            BlockBehaviour.Properties.ofFullCopy(Blocks.STONE)
                    .mapColor(MapColor.TERRACOTTA_WHITE)
                    .sound(SoundType.BONE_BLOCK)
                    .requiresCorrectToolForDrops()
    );

    // 苍白原木
    public static final DeferredBlock<BoneLogBlock> BONE_LOG = BLOCKS.register(
            "bone_log",
            BoneLogBlock::new
    );

    // 苍白树叶
    public static final DeferredBlock<PaleLeavesBlock> PALE_LEAVES = BLOCKS.register(
            "pale_leaves",
            PaleLeavesBlock::new
    );
}
