package com.littlh.bloodline;

import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.Item;
import net.neoforged.neoforge.registries.DeferredItem;
import net.neoforged.neoforge.registries.DeferredRegister;

public class BloodlineItems {
    public static final DeferredRegister.Items ITEMS =
            DeferredRegister.createItems("bloodline");

    public static final DeferredItem<BlockItem> PALE_GRASS_BLOCK_ITEM =
            ITEMS.registerSimpleBlockItem(BloodlineBlocks.PALE_GRASS_BLOCK);

    public static final DeferredItem<Item> PALE_WHEAT_SEEDS = ITEMS.registerSimpleItem(
            "pale_wheat_seeds",
            new Item.Properties()
    );
}
