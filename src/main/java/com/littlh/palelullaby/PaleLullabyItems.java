package com.littlh.palelullaby;

import com.littlh.palelullaby.entity.PaleLullabyEntities;
import net.minecraft.core.Holder;
import net.minecraft.world.item.ArmorMaterials;
import net.minecraft.world.item.ArmorItem;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.Item;
import com.littlh.palelullaby.item.PaleRegretMixtureItem;
import net.minecraft.world.item.SpawnEggItem;
import net.neoforged.neoforge.registries.DeferredItem;
import net.neoforged.neoforge.registries.DeferredRegister;

public class PaleLullabyItems {
    public static final DeferredRegister.Items ITEMS =
            DeferredRegister.createItems("pale_lullaby");

    // 苍白草方块物品
    public static final DeferredItem<BlockItem> PALE_GRASS_BLOCK_ITEM =
            ITEMS.registerSimpleBlockItem(PaleLullabyBlocks.PALE_GRASS_BLOCK);

    // 苍白小麦物品
    public static final DeferredItem<BlockItem> PALE_WHEAT_ITEM =
            ITEMS.registerSimpleBlockItem(PaleLullabyBlocks.PALE_WHEAT);

    // 苍白小麦种子（独立物品）
    public static final DeferredItem<Item> PALE_WHEAT_SEEDS = ITEMS.registerSimpleItem(
            "pale_wheat_seeds",
            new Item.Properties()
    );

    // 苍白余烬物品
    public static final DeferredItem<BlockItem> PALE_EMBER_ITEM =
            ITEMS.registerSimpleBlockItem(PaleLullabyBlocks.PALE_EMBER);

    // 纯白骨块物品
    public static final DeferredItem<BlockItem> PALE_BONE_BLOCK_ITEM =
            ITEMS.registerSimpleBlockItem(PaleLullabyBlocks.PALE_BONE_BLOCK);

    // 骨树原木物品
    public static final DeferredItem<BlockItem> BONE_LOG_ITEM =
            ITEMS.registerSimpleBlockItem(PaleLullabyBlocks.BONE_LOG);

    // 苍白树叶物品
    public static final DeferredItem<BlockItem> PALE_LEAVES_ITEM =
            ITEMS.registerSimpleBlockItem(PaleLullabyBlocks.PALE_LEAVES);

    // 猩红荆棘物品
    public static final DeferredItem<BlockItem> CRIMSON_THORN_ITEM =
            ITEMS.registerSimpleBlockItem(PaleLullabyBlocks.CRIMSON_THORN);

    // 猩红棘刺物品
    public static final DeferredItem<BlockItem> CRIMSON_THORN_SPIKE_ITEM =
            ITEMS.registerSimpleBlockItem(PaleLullabyBlocks.CRIMSON_THORN_SPIKE);

    // 荆棘原木物品
    public static final DeferredItem<BlockItem> THORN_LOG_ITEM =
            ITEMS.registerSimpleBlockItem(PaleLullabyBlocks.THORN_LOG);

    // 荆棘树叶物品
    public static final DeferredItem<BlockItem> THORN_LEAVES_ITEM =
            ITEMS.registerSimpleBlockItem(PaleLullabyBlocks.THORN_LEAVES);

    // ===== Hunter / Armor Sets =====
    public static final DeferredItem<ArmorItem> LEATHER_HUNTER_HIGH_HAT = ITEMS.register("leather_hunter_high_hat",
            () -> new ArmorItem(ArmorMaterials.LEATHER, ArmorItem.Type.HELMET, new Item.Properties()));
    public static final DeferredItem<ArmorItem> LEATHER_HUNTER_JACKET = ITEMS.register("leather_hunter_jacket",
            () -> new ArmorItem(ArmorMaterials.LEATHER, ArmorItem.Type.CHESTPLATE, new Item.Properties()));
    public static final DeferredItem<ArmorItem> LEATHER_HUNTER_TROUSERS = ITEMS.register("leather_hunter_trousers",
            () -> new ArmorItem(ArmorMaterials.LEATHER, ArmorItem.Type.LEGGINGS, new Item.Properties()));
    public static final DeferredItem<ArmorItem> LEATHER_HUNTER_BOOTS = ITEMS.register("leather_hunter_boots",
            () -> new ArmorItem(ArmorMaterials.LEATHER, ArmorItem.Type.BOOTS, new Item.Properties()));

    public static final DeferredItem<ArmorItem> IRON_HUNTER_HIGH_HAT = ITEMS.register("iron_hunter_high_hat",
            () -> new ArmorItem(ArmorMaterials.IRON, ArmorItem.Type.HELMET, new Item.Properties()));
    public static final DeferredItem<ArmorItem> IRON_HUNTER_JACKET = ITEMS.register("iron_hunter_jacket",
            () -> new ArmorItem(ArmorMaterials.IRON, ArmorItem.Type.CHESTPLATE, new Item.Properties()));
    public static final DeferredItem<ArmorItem> IRON_HUNTER_TROUSERS = ITEMS.register("iron_hunter_trousers",
            () -> new ArmorItem(ArmorMaterials.IRON, ArmorItem.Type.LEGGINGS, new Item.Properties()));
    public static final DeferredItem<ArmorItem> IRON_HUNTER_BOOTS = ITEMS.register("iron_hunter_boots",
            () -> new ArmorItem(ArmorMaterials.IRON, ArmorItem.Type.BOOTS, new Item.Properties()));

    public static final DeferredItem<ArmorItem> CLOTH_HUNTER_HIGH_HAT = ITEMS.register("cloth_hunter_high_hat",
            () -> new ArmorItem(ArmorMaterials.LEATHER, ArmorItem.Type.HELMET, new Item.Properties()));
    public static final DeferredItem<ArmorItem> CLOTH_HUNTER_JACKET = ITEMS.register("cloth_hunter_jacket",
            () -> new ArmorItem(ArmorMaterials.LEATHER, ArmorItem.Type.CHESTPLATE, new Item.Properties()));
    public static final DeferredItem<ArmorItem> CLOTH_HUNTER_TROUSERS = ITEMS.register("cloth_hunter_trousers",
            () -> new ArmorItem(ArmorMaterials.LEATHER, ArmorItem.Type.LEGGINGS, new Item.Properties()));
    public static final DeferredItem<ArmorItem> CLOTH_HUNTER_ANKLE_BOOTS = ITEMS.register("cloth_hunter_ankle_boots",
            () -> new ArmorItem(ArmorMaterials.LEATHER, ArmorItem.Type.BOOTS, new Item.Properties()));

    public static final DeferredItem<ArmorItem> NOVICE_HUNTER_HIGH_HAT = ITEMS.register("novice_hunter_high_hat",
            () -> new ArmorItem(ArmorMaterials.LEATHER, ArmorItem.Type.HELMET, new Item.Properties()));
    public static final DeferredItem<ArmorItem> NOVICE_HUNTER_JACKET = ITEMS.register("novice_hunter_jacket",
            () -> new ArmorItem(ArmorMaterials.LEATHER, ArmorItem.Type.CHESTPLATE, new Item.Properties()));
    public static final DeferredItem<ArmorItem> NOVICE_HUNTER_TROUSERS = ITEMS.register("novice_hunter_trousers",
            () -> new ArmorItem(ArmorMaterials.LEATHER, ArmorItem.Type.LEGGINGS, new Item.Properties()));
    public static final DeferredItem<ArmorItem> NOVICE_HUNTER_BOOTS = ITEMS.register("novice_hunter_boots",
            () -> new ArmorItem(ArmorMaterials.LEATHER, ArmorItem.Type.BOOTS, new Item.Properties()));

    public static final DeferredItem<ArmorItem> ADEPT_HUNTER_HIGH_HAT = ITEMS.register("adept_hunter_high_hat",
            () -> new ArmorItem(ArmorMaterials.IRON, ArmorItem.Type.HELMET, new Item.Properties()));
    public static final DeferredItem<ArmorItem> ADEPT_HUNTER_JACKET = ITEMS.register("adept_hunter_jacket",
            () -> new ArmorItem(ArmorMaterials.IRON, ArmorItem.Type.CHESTPLATE, new Item.Properties()));
    public static final DeferredItem<ArmorItem> ADEPT_HUNTER_TROUSERS = ITEMS.register("adept_hunter_trousers",
            () -> new ArmorItem(ArmorMaterials.IRON, ArmorItem.Type.LEGGINGS, new Item.Properties()));
    public static final DeferredItem<ArmorItem> ADEPT_HUNTER_BOOTS = ITEMS.register("adept_hunter_boots",
            () -> new ArmorItem(ArmorMaterials.IRON, ArmorItem.Type.BOOTS, new Item.Properties()));

    public static final DeferredItem<ArmorItem> EXALTED_HUNTER_HIGH_HAT = ITEMS.register("exalted_hunter_high_hat",
            () -> new ArmorItem(ArmorMaterials.GOLD, ArmorItem.Type.HELMET, new Item.Properties()));
    public static final DeferredItem<ArmorItem> EXALTED_HUNTER_JACKET = ITEMS.register("exalted_hunter_jacket",
            () -> new ArmorItem(ArmorMaterials.GOLD, ArmorItem.Type.CHESTPLATE, new Item.Properties()));
    public static final DeferredItem<ArmorItem> EXALTED_HUNTER_TROUSERS = ITEMS.register("exalted_hunter_trousers",
            () -> new ArmorItem(ArmorMaterials.GOLD, ArmorItem.Type.LEGGINGS, new Item.Properties()));
    public static final DeferredItem<ArmorItem> EXALTED_HUNTER_BOOTS = ITEMS.register("exalted_hunter_boots",
            () -> new ArmorItem(ArmorMaterials.GOLD, ArmorItem.Type.BOOTS, new Item.Properties()));

    public static final DeferredItem<ArmorItem> EXILED_HUNTER_HIGH_HAT = ITEMS.register("exiled_hunter_high_hat",
            () -> new ArmorItem(ArmorMaterials.IRON, ArmorItem.Type.HELMET, new Item.Properties()));
    public static final DeferredItem<ArmorItem> EXILED_HUNTER_JACKET = ITEMS.register("exiled_hunter_jacket",
            () -> new ArmorItem(ArmorMaterials.IRON, ArmorItem.Type.CHESTPLATE, new Item.Properties()));
    public static final DeferredItem<ArmorItem> EXILED_HUNTER_TROUSERS = ITEMS.register("exiled_hunter_trousers",
            () -> new ArmorItem(ArmorMaterials.IRON, ArmorItem.Type.LEGGINGS, new Item.Properties()));
    public static final DeferredItem<ArmorItem> EXILED_HUNTER_BOOTS = ITEMS.register("exiled_hunter_boots",
            () -> new ArmorItem(ArmorMaterials.IRON, ArmorItem.Type.BOOTS, new Item.Properties()));

    public static final DeferredItem<ArmorItem> FALLEN_HUNTER_HIGH_HAT = ITEMS.register("fallen_hunter_high_hat",
            () -> new ArmorItem(ArmorMaterials.CHAIN, ArmorItem.Type.HELMET, new Item.Properties()));
    public static final DeferredItem<ArmorItem> FALLEN_HUNTER_JACKET = ITEMS.register("fallen_hunter_jacket",
            () -> new ArmorItem(ArmorMaterials.CHAIN, ArmorItem.Type.CHESTPLATE, new Item.Properties()));
    public static final DeferredItem<ArmorItem> FALLEN_HUNTER_TROUSERS = ITEMS.register("fallen_hunter_trousers",
            () -> new ArmorItem(ArmorMaterials.CHAIN, ArmorItem.Type.LEGGINGS, new Item.Properties()));
    public static final DeferredItem<ArmorItem> FALLEN_HUNTER_BOOTS = ITEMS.register("fallen_hunter_boots",
            () -> new ArmorItem(ArmorMaterials.CHAIN, ArmorItem.Type.BOOTS, new Item.Properties()));

    public static final DeferredItem<ArmorItem> INQUISITOR_HIGH_HAT = ITEMS.register("inquisitor_high_hat",
            () -> new ArmorItem(ArmorMaterials.CHAIN, ArmorItem.Type.HELMET, new Item.Properties()));
    public static final DeferredItem<ArmorItem> INQUISITOR_ROBE = ITEMS.register("inquisitor_robe",
            () -> new ArmorItem(ArmorMaterials.CHAIN, ArmorItem.Type.CHESTPLATE, new Item.Properties()));
    public static final DeferredItem<ArmorItem> INQUISITOR_TROUSERS = ITEMS.register("inquisitor_trousers",
            () -> new ArmorItem(ArmorMaterials.CHAIN, ArmorItem.Type.LEGGINGS, new Item.Properties()));
    public static final DeferredItem<ArmorItem> INQUISITOR_LONG_BOOTS = ITEMS.register("inquisitor_long_boots",
            () -> new ArmorItem(ArmorMaterials.CHAIN, ArmorItem.Type.BOOTS, new Item.Properties()));

    public static final DeferredItem<ArmorItem> HERBALIST_WIDE_BRIMMED_HAT = ITEMS.register("herbalist_wide_brimmed_hat",
            () -> new ArmorItem(ArmorMaterials.LEATHER, ArmorItem.Type.HELMET, new Item.Properties()));
    public static final DeferredItem<ArmorItem> HERBALIST_TUNIC = ITEMS.register("herbalist_tunic",
            () -> new ArmorItem(ArmorMaterials.LEATHER, ArmorItem.Type.CHESTPLATE, new Item.Properties()));
    public static final DeferredItem<ArmorItem> HERBALIST_BOTTOMS = ITEMS.register("herbalist_bottoms",
            () -> new ArmorItem(ArmorMaterials.LEATHER, ArmorItem.Type.LEGGINGS, new Item.Properties()));
    public static final DeferredItem<ArmorItem> HERBALIST_ANKLE_BOOTS = ITEMS.register("herbalist_ankle_boots",
            () -> new ArmorItem(ArmorMaterials.LEATHER, ArmorItem.Type.BOOTS, new Item.Properties()));

    public static final DeferredItem<ArmorItem> PENITENT_NECK_YOKE = ITEMS.register("penitent_neck_yoke",
            () -> new ArmorItem(ArmorMaterials.GOLD, ArmorItem.Type.HELMET, new Item.Properties()));
    public static final DeferredItem<ArmorItem> PENITENT_ARM_SHACKLES = ITEMS.register("penitent_arm_shackles",
            () -> new ArmorItem(ArmorMaterials.GOLD, ArmorItem.Type.CHESTPLATE, new Item.Properties()));
    public static final DeferredItem<ArmorItem> PENITENT_LEG_IRONS = ITEMS.register("penitent_leg_irons",
            () -> new ArmorItem(ArmorMaterials.GOLD, ArmorItem.Type.LEGGINGS, new Item.Properties()));

    // ===== 枯血者BOSS刷怪蛋 =====
    // ===== 血液瓶（四级） =====
    public static final DeferredItem<Item> UNREFINED_BLOOD_BOTTLE = ITEMS.registerSimpleItem("unrefined_blood_bottle");
    public static final DeferredItem<Item> BLOOD_BOTTLE = ITEMS.registerSimpleItem("blood_bottle");
    public static final DeferredItem<Item> REFINED_BLOOD_BOTTLE = ITEMS.registerSimpleItem("refined_blood_bottle");
    public static final DeferredItem<Item> HALLOWED_BLOOD_BOTTLE = ITEMS.registerSimpleItem("hallowed_blood_bottle");

    // ===== 枯血者BOSS刷怪蛋 =====
    public static final DeferredItem<SpawnEggItem> MULLAND_SPAWN_EGG =
            ITEMS.register("mulland_spawn_egg",
                    () -> new SpawnEggItem(PaleLullabyEntities.MULLAND.get(), 0x1a1a2e, 0xc41e3a, new Item.Properties()));

    public static final DeferredItem<SpawnEggItem> PALE_MINION_SPAWN_EGG =
            ITEMS.register("pale_minion_spawn_egg",
                    () -> new SpawnEggItem(PaleLullabyEntities.PALE_MINION.get(), 0x8b8b8b, 0xc0c0c0, new Item.Properties()));

    // ===== 苍白悔恨混合物 =====
    public static final DeferredItem<Item> PALE_REGRET_MIXTURE = ITEMS.register("pale_regret_mixture", PaleRegretMixtureItem::new);
}
