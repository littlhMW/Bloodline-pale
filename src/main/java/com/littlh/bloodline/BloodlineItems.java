package com.littlh.bloodline;

import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.Item;
import net.neoforged.neoforge.registries.DeferredItem;
import net.neoforged.neoforge.registries.DeferredRegister;

public class BloodlineItems {
    public static final DeferredRegister.Items ITEMS =
            DeferredRegister.createItems("bloodline");

    // 苍白草方块物品
    public static final DeferredItem<BlockItem> PALE_GRASS_BLOCK_ITEM =
            ITEMS.registerSimpleBlockItem(BloodlineBlocks.PALE_GRASS_BLOCK);

    // 苍白小麦物品
    public static final DeferredItem<BlockItem> PALE_WHEAT_ITEM =
            ITEMS.registerSimpleBlockItem(BloodlineBlocks.PALE_WHEAT);

    // 苍白小麦种子（独立物品）
    public static final DeferredItem<Item> PALE_WHEAT_SEEDS = ITEMS.registerSimpleItem(
            "pale_wheat_seeds",
            new Item.Properties()
    );

    // 苍白余烬物品
    public static final DeferredItem<BlockItem> PALE_EMBER_ITEM =
            ITEMS.registerSimpleBlockItem(BloodlineBlocks.PALE_EMBER);

    // 纯白骨块物品
    public static final DeferredItem<BlockItem> PALE_BONE_BLOCK_ITEM =
            ITEMS.registerSimpleBlockItem(BloodlineBlocks.PALE_BONE_BLOCK);
}