package com.littlh.palelullaby;

import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.SoundType;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.material.MapColor;
import net.neoforged.neoforge.registries.DeferredBlock;
import net.neoforged.neoforge.registries.DeferredRegister;

public class PaleLullabyBlocks {
    public static final DeferredRegister.Blocks BLOCKS =
            DeferredRegister.createBlocks("pale_lullaby");

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

    // 猩红荆棘（随机在六个面生长猩红棘刺）
    public static final DeferredBlock<CrimsonThornBlock> CRIMSON_THORN = BLOCKS.register(
            "crimson_thorn",
            CrimsonThornBlock::new
    );

    // 猩红棘刺（方向性 FACING，可附着在猩红荆棘六面）
    public static final DeferredBlock<CrimsonThornSpikeBlock> CRIMSON_THORN_SPIKE = BLOCKS.register(
            "crimson_thorn_spike",
            CrimsonThornSpikeBlock::new
    );

    // 荆棘原木（深色枯木）
    public static final DeferredBlock<ThornLogBlock> THORN_LOG = BLOCKS.register(
            "thorn_log",
            ThornLogBlock::new
    );

    // 荆棘树叶（红棕色稀疏叶片）
    public static final DeferredBlock<ThornLeavesBlock> THORN_LEAVES = BLOCKS.register(
            "thorn_leaves",
            ThornLeavesBlock::new
    );

}
