package com.littlh.palelullaby;

import net.minecraft.resources.ResourceLocation;
import net.minecraft.core.registries.Registries;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.SoundType;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.material.MapColor;
import net.minecraft.world.level.material.PushReaction;
import net.neoforged.neoforge.registries.DeferredBlock;
import net.neoforged.neoforge.registries.DeferredHolder;
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

    // 苍白余烬耕地（锄头开垦，永不干涸、始终湿润，可跳跃摧毁）
    public static final DeferredBlock<PaleEmberFarmlandBlock> PALE_EMBER_FARMLAND = BLOCKS.register(
            "pale_ember_farmland",
            () -> new PaleEmberFarmlandBlock(
                    BlockBehaviour.Properties.of()
                            .mapColor(MapColor.COLOR_GRAY)
                            .strength(0.6F)
                            .sound(SoundType.GRAVEL)
                            .randomTicks()
            )
    );

    // 斑驳白质（挖掘白质掉落，类似石头→圆石）
    public static final DeferredBlock<Block> MOTTLED_WHITE_MATTER = BLOCKS.registerSimpleBlock(
            "mottled_white_matter",
            BlockBehaviour.Properties.ofFullCopy(Blocks.COBBLESTONE)
                    .mapColor(MapColor.TERRACOTTA_WHITE)
                    .sound(SoundType.BONE_BLOCK)
                    .requiresCorrectToolForDrops()
    );

    // 白质（替代石头）
    public static final DeferredBlock<Block> WHITE_MATTER = BLOCKS.registerSimpleBlock(
            "white_matter",
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

    // 骨树苗
    public static final DeferredBlock<PaleSaplingBlock> BONE_SAPLING = BLOCKS.register(
            "bone_sapling",
            () -> new PaleSaplingBlock(
                    ResourceLocation.fromNamespaceAndPath(PaleLullaby.MOD_ID, "bone_tree"),
                    BlockBehaviour.Properties.of()
                            .mapColor(MapColor.PLANT)
                            .noCollission()
                            .randomTicks()
                            .instabreak()
                            .sound(SoundType.GRASS)
                            .pushReaction(PushReaction.DESTROY)
            )
    );

    // 苍白树叶
    public static final DeferredBlock<PaleLeavesBlock> PALE_LEAVES = BLOCKS.register(
            "pale_leaves",
            PaleLeavesBlock::new
    );

    // 猩红荆棘（随机向上生长，新茎上会结出猩红棘果或猩红蔷薇）
    public static final DeferredBlock<CrimsonThornBlock> CRIMSON_THORN = BLOCKS.register(
            "crimson_thorn",
            () -> new CrimsonThornBlock(
                    BlockBehaviour.Properties.of()
                            .mapColor(MapColor.CRIMSON_STEM)
                            .strength(1.5F)
                            .sound(SoundType.SWEET_BERRY_BUSH)
                            .randomTicks()
                            .noOcclusion()
            )
    );

    // 猩红棘果簇（附着在猩红荆棘上，采集得到猩红棘果）
    public static final DeferredBlock<CrimsonThornAttachmentBlock> CRIMSON_THORN_BERRY_BLOCK = BLOCKS.register(
            "crimson_thorn_berry_block",
            () -> new CrimsonThornAttachmentBlock(
                    BlockBehaviour.Properties.of()
                            .mapColor(MapColor.CRIMSON_STEM)
                            .instabreak()
                            .noOcclusion()
                            .sound(SoundType.SWEET_BERRY_BUSH)
                            .lightLevel(state -> 7)
                            .pushReaction(PushReaction.DESTROY)
            )
    );

    // 猩红蔷薇（附着在猩红荆棘上）
    public static final DeferredBlock<CrimsonRoseBlock> CRIMSON_ROSE = BLOCKS.register(
            "crimson_rose",
            () -> new CrimsonRoseBlock(
                    BlockBehaviour.Properties.of()
                            .mapColor(MapColor.CRIMSON_STEM)
                            .instabreak()
                            .noOcclusion()
                            .noCollission()
                            .sound(SoundType.SWEET_BERRY_BUSH)
                            .randomTicks()
                            .lightLevel(state -> state.getValue(CrimsonRoseBlock.HAS_NECTAR) ? 3 : 0)
                            .pushReaction(PushReaction.DESTROY)
            )
    );

    // 荆棘原木（深色枯木）
    public static final DeferredBlock<RedNeedleLogBlock> RED_NEEDLE_LOG = BLOCKS.register(
            "red_needle_log",
            RedNeedleLogBlock::new
    );

    // 荆棘树叶（红棕色稀疏叶片）
    public static final DeferredBlock<RedNeedleLeavesBlock> RED_NEEDLE_LEAVES = BLOCKS.register(
            "red_needle_leaves",
            RedNeedleLeavesBlock::new
    );

    // 浸润淤泥（被血浸湿的暗红淤泥，猩红花园地表）
    public static final DeferredBlock<Block> SOAKED_MUD = BLOCKS.registerSimpleBlock(
            "soaked_mud",
            BlockBehaviour.Properties.ofFullCopy(Blocks.MUD)
                    .mapColor(MapColor.CRIMSON_STEM)
    );

    // 浸润淤泥草方块（猩红花园地表草皮，光照不足时退化为浸润淤泥）
    public static final DeferredBlock<SoakedMudGrassBlock> SOAKED_MUD_GRASS = BLOCKS.register(
            "soaked_mud_grass",
            SoakedMudGrassBlock::new
    );

    // Red Needle Sapling
    public static final DeferredBlock<PaleSaplingBlock> RED_NEEDLE_SAPLING = BLOCKS.register(
            "red_needle_sapling",
            () -> new PaleSaplingBlock(
                    ResourceLocation.fromNamespaceAndPath(PaleLullaby.MOD_ID, "red_needle_tree"),
                    BlockBehaviour.Properties.of()
                            .mapColor(MapColor.PLANT)
                            .noCollission()
                            .randomTicks()
                            .instabreak()
                            .sound(SoundType.GRASS)
                            .pushReaction(PushReaction.DESTROY)
            )
    );

    // Blind floss flower (small white floss-like flower).
    public static final DeferredBlock<BlindFlossFlowerBlock> BLIND_FLOSS_FLOWER = BLOCKS.register(
            "blind_floss_flower",
            () -> new BlindFlossFlowerBlock()
    );

    // 冰锥（倒挂的尖锐冰柱：会伤害，打碎后摔碎无掉落，无法采集）
    public static final DeferredBlock<IceSpikeBlock> ICE_SPIKE = BLOCKS.register(
            "ice_spike",
            () -> new IceSpikeBlock()
    );

    // 霜月花（可种在冰块/雪块/雪层上，种在雪层上时不破坏雪层）
    public static final DeferredBlock<FrostMoonflowerBlock> FROST_MOONFLOWER = BLOCKS.register(
            "frost_moonflower",
            () -> new FrostMoonflowerBlock()
    );
    // 幽灵兰花（迷花平原雾涡中心的半透明无叶兰花）
    public static final DeferredBlock<GhostOrchidBlock> GHOST_ORCHID = BLOCKS.register(
            "ghost_orchid",
            () -> new GhostOrchidBlock()
    );

    // 腐心菇（猩红花园边缘，黑紫钟形菇，黏液腐蚀、分解动物残骸）
    public static final DeferredBlock<RotheartMushroomBlock> ROTHEART_MUSHROOM = BLOCKS.register(
            "rotheart_mushroom",
            () -> new RotheartMushroomBlock()
    );

    // 寡妇刺（荒芜高原极罕见，遇血开灰白小花，约 3 天后母株枯死）
    public static final DeferredBlock<WidowThornBlock> WIDOW_THORN = BLOCKS.register(
            "widow_thorn",
            () -> new WidowThornBlock()
    );

    // 银矿石
    public static final DeferredBlock<Block> SILVER_ORE = BLOCKS.registerSimpleBlock(
            "silver_ore",
            BlockBehaviour.Properties.ofFullCopy(Blocks.IRON_ORE)
                    .strength(3.0F, 3.0F)
                    .requiresCorrectToolForDrops()
    );

    // 深层银矿石
    public static final DeferredBlock<Block> DEEPSLATE_SILVER_ORE = BLOCKS.registerSimpleBlock(
            "deepslate_silver_ore",
            BlockBehaviour.Properties.ofFullCopy(Blocks.DEEPSLATE_IRON_ORE)
                    .strength(4.5F, 3.0F)
                    .requiresCorrectToolForDrops()
    );

    // 银块
    public static final DeferredBlock<Block> SILVER_BLOCK = BLOCKS.registerSimpleBlock(
            "silver_block",
            BlockBehaviour.Properties.ofFullCopy(Blocks.IRON_BLOCK)
    );

    // 铁处女：底座+四面壁碰撞、内部踩踏掉血、概率掉落伤痕印记
    public static final DeferredBlock<IronMaidenBlock> IRON_MAIDEN = BLOCKS.register(
            "iron_maiden",
            () -> new IronMaidenBlock(
                    BlockBehaviour.Properties.of()
                            .mapColor(MapColor.METAL)
                            .strength(3.5F, 6.0F)
                            .sound(SoundType.METAL)
                            .requiresCorrectToolForDrops()
            )
    );

    // é“å¤„å¥³å¤šæ–¹å—ç¢°æ’žé¨ä»¶ï¼ˆä¸‰æ ¼æœ¬ä½“+ä¸€æ ¼é¡¶é¥°ï¼‰ï¼Œä¸å¯è§?ä¸æŽ‰è½
    public static final DeferredBlock<IronMaidenPartBlock> IRON_MAIDEN_PART = BLOCKS.register(
            "iron_maiden_part",
            () -> new IronMaidenPartBlock(
                    BlockBehaviour.Properties.of()
                            .mapColor(MapColor.METAL)
                            .instabreak()
                            .noOcclusion()
                            .noLootTable()
                            .sound(SoundType.METAL)
            )
    );

    // 断剑：插在花丛石座里的三格高残剑，右键可在摇篮维度与主世界对应位置之间传送
    public static final DeferredBlock<BrokenSwordBlock> BROKEN_SWORD = BLOCKS.register(
            "broken_sword",
            () -> new BrokenSwordBlock(
                    BlockBehaviour.Properties.of()
                            .mapColor(MapColor.STONE)
                            .strength(2.0F, 6.0F)
                            .sound(SoundType.STONE)
                            .noOcclusion()
            )
    );

    // 断剑上方的两格碰撞部件
    public static final DeferredBlock<BrokenSwordPartBlock> BROKEN_SWORD_PART = BLOCKS.register(
            "broken_sword_part",
            () -> new BrokenSwordPartBlock(
                    BlockBehaviour.Properties.of()
                            .mapColor(MapColor.STONE)
                            .instabreak()
                            .noOcclusion()
                            .noLootTable()
                            .sound(SoundType.STONE)
            )
    );


    // Withered dirt: low-contrast gray-white slightly yellow dirt
    public static final DeferredBlock<WitheredDirtBlock> WITHERED_DIRT = BLOCKS.register(
            "withered_dirt",
            WitheredDirtBlock::new
    );

    // Withered grass block: GrassBlock subclass, spreads onto withered dirt
    public static final DeferredBlock<WitheredGrassBlock> WITHERED_GRASS_BLOCK = BLOCKS.register(
            "withered_grass_block",
            WitheredGrassBlock::new
    );

    // Withered mistletoe: gray-white parasitic vine, climbs but breaks when climbed
    public static final DeferredBlock<WitheredMistletoeBlock> WITHERED_MISTLETOE = BLOCKS.register(
            "withered_mistletoe",
            () -> new WitheredMistletoeBlock()
    );

    public static final DeferredRegister<BlockEntityType<?>> BLOCK_ENTITIES =
            DeferredRegister.create(Registries.BLOCK_ENTITY_TYPE, "pale_lullaby");

    public static final DeferredHolder<BlockEntityType<?>, BlockEntityType<IronMaidenBlockEntity>> IRON_MAIDEN_BE =
            BLOCK_ENTITIES.register("iron_maiden",
                    () -> BlockEntityType.Builder.of(IronMaidenBlockEntity::new, IRON_MAIDEN.get()).build(null));

    public static final DeferredHolder<BlockEntityType<?>, BlockEntityType<BrokenSwordBlockEntity>> BROKEN_SWORD_BE =
            BLOCK_ENTITIES.register("broken_sword",
                    () -> BlockEntityType.Builder.of(BrokenSwordBlockEntity::new, BROKEN_SWORD.get()).build(null));

}





