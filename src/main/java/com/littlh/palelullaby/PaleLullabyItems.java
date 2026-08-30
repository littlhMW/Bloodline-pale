package com.littlh.palelullaby;import com.littlh.palelullaby.entity.PaleLullabyEntities;import com.littlh.palelullaby.item.BloodCollectorItem;import com.littlh.palelullaby.item.DrinkableBloodItem;import com.littlh.palelullaby.item.MaidensBloodMoonItem;import com.littlh.palelullaby.item.PaleRegretMixtureItem;import net.minecraft.core.Holder;import net.minecraft.world.item.ArmorItem;import net.minecraft.world.item.ArmorMaterial;import net.minecraft.world.item.ArmorMaterials;import net.minecraft.world.item.AxeItem;import net.minecraft.world.item.BlockItem;import net.minecraft.world.item.HoeItem;import net.minecraft.world.item.Item;import net.minecraft.world.item.PickaxeItem;import net.minecraft.world.item.ShovelItem;import net.minecraft.world.item.SpawnEggItem;import net.minecraft.world.item.SwordItem;import net.neoforged.neoforge.registries.DeferredItem;import net.neoforged.neoforge.registries.DeferredRegister;public class PaleLullabyItems {    public static final DeferredRegister.Items ITEMS =            DeferredRegister.createItems("pale_lullaby");    
    // 苍白草方块物品
    public static final DeferredItem<BlockItem> PALE_GRASS_BLOCK_ITEM =            ITEMS.registerSimpleBlockItem(PaleLullabyBlocks.PALE_GRASS_BLOCK);    
    // withered dirt item
    public static final DeferredItem<BlockItem> WITHERED_DIRT_ITEM =            ITEMS.registerSimpleBlockItem(PaleLullabyBlocks.WITHERED_DIRT);    
    // withered grass block item
    public static final DeferredItem<BlockItem> WITHERED_GRASS_BLOCK_ITEM =            ITEMS.registerSimpleBlockItem(PaleLullabyBlocks.WITHERED_GRASS_BLOCK);    
    // withered mistletoe item
    public static final DeferredItem<BlockItem> WITHERED_MISTLETOE_ITEM =            ITEMS.registerSimpleBlockItem(PaleLullabyBlocks.WITHERED_MISTLETOE);    
    // 苍白小麦物品
    public static final DeferredItem<BlockItem> PALE_WHEAT_ITEM =            ITEMS.registerSimpleBlockItem(PaleLullabyBlocks.PALE_WHEAT);    
    // 苍白小麦种子（独立物品）
    public static final DeferredItem<Item> PALE_WHEAT_SEEDS = ITEMS.registerSimpleItem(            "pale_wheat_seeds",            new Item.Properties()    );    
    // 苍白余烬物品
    public static final DeferredItem<BlockItem> PALE_EMBER_ITEM =            ITEMS.registerSimpleBlockItem(PaleLullabyBlocks.PALE_EMBER);    
    // 苍白余烬耕地物品
    public static final DeferredItem<BlockItem> PALE_EMBER_FARMLAND_ITEM =            ITEMS.registerSimpleBlockItem(PaleLullabyBlocks.PALE_EMBER_FARMLAND);    
    // 斑驳白质物品
    public static final DeferredItem<BlockItem> MOTTLED_WHITE_MATTER_ITEM =            ITEMS.registerSimpleBlockItem(PaleLullabyBlocks.MOTTLED_WHITE_MATTER);    
    // 白质物品
    public static final DeferredItem<BlockItem> WHITE_MATTER_ITEM =            ITEMS.registerSimpleBlockItem(PaleLullabyBlocks.WHITE_MATTER);    
    // 骨树原木物品
    public static final DeferredItem<BlockItem> BONE_LOG_ITEM =            ITEMS.registerSimpleBlockItem(PaleLullabyBlocks.BONE_LOG);    public static final DeferredItem<BlockItem> BONE_SAPLING_ITEM =            ITEMS.registerSimpleBlockItem(PaleLullabyBlocks.BONE_SAPLING);    
    // 苍白树叶物品
    public static final DeferredItem<BlockItem> PALE_LEAVES_ITEM =            ITEMS.registerSimpleBlockItem(PaleLullabyBlocks.PALE_LEAVES);    
    // 猩红荆棘物品
    public static final DeferredItem<CrimsonThornBlockItem> CRIMSON_THORN_ITEM = ITEMS.register(
            "crimson_thorn",
            () -> new CrimsonThornBlockItem(PaleLullabyBlocks.CRIMSON_THORN.get(), new Item.Properties())
    );    
    // 猩红棘果（种子：对荆棘使用可结果，对地面使用可种出荆棘）
    public static final DeferredItem<CrimsonThornBerryItem> CRIMSON_THORN_BERRY = ITEMS.register(            "crimson_thorn_berry",            () -> new CrimsonThornBerryItem(new Item.Properties())    );    
    // 猩红蔷薇物品
    public static final DeferredItem<BlockItem> CRIMSON_ROSE_ITEM =            ITEMS.registerSimpleBlockItem(PaleLullabyBlocks.CRIMSON_ROSE);

    // 猩红蔷薇蜜露（空瓶采蜜获得）
    public static final DeferredItem<Item> CRIMSON_ROSE_NECTAR =
            ITEMS.registerSimpleItem("crimson_rose_nectar", new Item.Properties().stacksTo(16));    
    // Crimson thorn berry cluster block item.
    public static final DeferredItem<BlockItem> CRIMSON_THORN_BERRY_BLOCK_ITEM =            ITEMS.registerSimpleBlockItem(PaleLullabyBlocks.CRIMSON_THORN_BERRY_BLOCK);    
    // 荆棘原木物品
    public static final DeferredItem<BlockItem> RED_NEEDLE_LOG_ITEM =            ITEMS.registerSimpleBlockItem(PaleLullabyBlocks.RED_NEEDLE_LOG);    
    // 荆棘树叶物品
    public static final DeferredItem<BlockItem> RED_NEEDLE_LEAVES_ITEM =            ITEMS.registerSimpleBlockItem(PaleLullabyBlocks.RED_NEEDLE_LEAVES);    
    // 浸润淤泥物品
    public static final DeferredItem<BlockItem> SOAKED_MUD_ITEM =            ITEMS.registerSimpleBlockItem(PaleLullabyBlocks.SOAKED_MUD);    
    // 浸润淤泥草方块物品
    public static final DeferredItem<BlockItem> SOAKED_MUD_GRASS_ITEM =            ITEMS.registerSimpleBlockItem(PaleLullabyBlocks.SOAKED_MUD_GRASS);    public static final DeferredItem<BlockItem> RED_NEEDLE_SAPLING_ITEM =            ITEMS.registerSimpleBlockItem(PaleLullabyBlocks.RED_NEEDLE_SAPLING);    
    // Blind floss flower item.
    public static final DeferredItem<BlockItem> BLIND_FLOSS_FLOWER_ITEM =            ITEMS.registerSimpleBlockItem(PaleLullabyBlocks.BLIND_FLOSS_FLOWER);    
    // 铁处女（物品用 GeckoLib geo 模型渲染，与方块一致）    
    // 冰锥物品（创造模式可直接获取；生存挖掘仍无掉落）
    public static final DeferredItem<BlockItem> ICE_SPIKE_ITEM =            ITEMS.registerSimpleBlockItem(PaleLullabyBlocks.ICE_SPIKE);    
    // 霜月花物品（可在冰块/雪块/雪层上种植）
    public static final DeferredItem<BlockItem> FROST_MOONFLOWER_ITEM =            ITEMS.registerSimpleBlockItem(PaleLullabyBlocks.FROST_MOONFLOWER);
    // 幽灵兰花
    public static final DeferredItem<BlockItem> GHOST_ORCHID_ITEM = ITEMS.registerSimpleBlockItem(PaleLullabyBlocks.GHOST_ORCHID);
    // 腐心菇
    public static final DeferredItem<BlockItem> ROTHEART_MUSHROOM_ITEM = ITEMS.registerSimpleBlockItem(PaleLullabyBlocks.ROTHEART_MUSHROOM);
    // 寡妇刺
    public static final DeferredItem<BlockItem> WIDOW_THORN_ITEM = ITEMS.registerSimpleBlockItem(PaleLullabyBlocks.WIDOW_THORN);
    // 寡妇刺果（结果状态的寡妇刺收获获得）
    public static final DeferredItem<Item> WIDOW_THORN_FRUIT = ITEMS.registerSimpleItem("widow_thorn_fruit", new Item.Properties());
    public static final DeferredItem<IronMaidenItem> IRON_MAIDEN_ITEM =            ITEMS.register("iron_maiden",                    () -> new IronMaidenItem(PaleLullabyBlocks.IRON_MAIDEN.get(), new Item.Properties()));    
    // 断剑
    public static final DeferredItem<BlockItem> BROKEN_SWORD_ITEM =            ITEMS.registerSimpleBlockItem(PaleLullabyBlocks.BROKEN_SWORD);    
    // 伤痕印记（铁处女内部受刑后概率掉落）
    public static final DeferredItem<Item> SCAR_MARK = ITEMS.registerSimpleItem("scar_mark");    
    // ===== 猎人套装（皮革/铁/布为初阶升级材质变体，属性介于初阶与中阶之间） =====
    public static final DeferredItem<ArmorItem> LEATHER_HUNTER_HIGH_HAT = ITEMS.register("leather_hunter_high_hat",            () -> battlemageArmor(PaleLullabyArmorMaterials.HUNTER_LEATHER, ArmorItem.Type.HELMET, 17));    public static final DeferredItem<ArmorItem> LEATHER_HUNTER_JACKET = ITEMS.register("leather_hunter_jacket",            () -> battlemageArmor(PaleLullabyArmorMaterials.HUNTER_LEATHER, ArmorItem.Type.CHESTPLATE, 17));    public static final DeferredItem<ArmorItem> LEATHER_HUNTER_TROUSERS = ITEMS.register("leather_hunter_trousers",            () -> battlemageArmor(PaleLullabyArmorMaterials.HUNTER_LEATHER, ArmorItem.Type.LEGGINGS, 17));    public static final DeferredItem<ArmorItem> LEATHER_HUNTER_BOOTS = ITEMS.register("leather_hunter_boots",            () -> battlemageArmor(PaleLullabyArmorMaterials.HUNTER_LEATHER, ArmorItem.Type.BOOTS, 17));    
    // 材质变体·铁（初阶升级：初阶+铁粒围一圈，耐久高）
    public static final DeferredItem<ArmorItem> IRON_HUNTER_HIGH_HAT = ITEMS.register("iron_hunter_high_hat",            () -> battlemageArmor(PaleLullabyArmorMaterials.HUNTER_IRON, ArmorItem.Type.HELMET, 18));    public static final DeferredItem<ArmorItem> IRON_HUNTER_JACKET = ITEMS.register("iron_hunter_jacket",            () -> battlemageArmor(PaleLullabyArmorMaterials.HUNTER_IRON, ArmorItem.Type.CHESTPLATE, 18));    public static final DeferredItem<ArmorItem> IRON_HUNTER_TROUSERS = ITEMS.register("iron_hunter_trousers",            () -> battlemageArmor(PaleLullabyArmorMaterials.HUNTER_IRON, ArmorItem.Type.LEGGINGS, 18));    public static final DeferredItem<ArmorItem> IRON_HUNTER_BOOTS = ITEMS.register("iron_hunter_boots",            () -> battlemageArmor(PaleLullabyArmorMaterials.HUNTER_IRON, ArmorItem.Type.BOOTS, 18));    
    // 材质变体·布（初阶升级：初阶+羊毛围一圈，附魔高）
    public static final DeferredItem<ArmorItem> CLOTH_HUNTER_HIGH_HAT = ITEMS.register("cloth_hunter_high_hat",            () -> battlemageArmor(PaleLullabyArmorMaterials.HUNTER_CLOTH, ArmorItem.Type.HELMET, 16));    public static final DeferredItem<ArmorItem> CLOTH_HUNTER_JACKET = ITEMS.register("cloth_hunter_jacket",            () -> battlemageArmor(PaleLullabyArmorMaterials.HUNTER_CLOTH, ArmorItem.Type.CHESTPLATE, 16));    public static final DeferredItem<ArmorItem> CLOTH_HUNTER_TROUSERS = ITEMS.register("cloth_hunter_trousers",            () -> battlemageArmor(PaleLullabyArmorMaterials.HUNTER_CLOTH, ArmorItem.Type.LEGGINGS, 16));    public static final DeferredItem<ArmorItem> CLOTH_HUNTER_ANKLE_BOOTS = ITEMS.register("cloth_hunter_ankle_boots",            () -> battlemageArmor(PaleLullabyArmorMaterials.HUNTER_CLOTH, ArmorItem.Type.BOOTS, 16));    
    // 初阶
    public static final DeferredItem<ArmorItem> NOVICE_HUNTER_HIGH_HAT = ITEMS.register("novice_hunter_high_hat",            () -> battlemageArmor(PaleLullabyArmorMaterials.HUNTER_NOVICE, ArmorItem.Type.HELMET, 16));    public static final DeferredItem<ArmorItem> NOVICE_HUNTER_JACKET = ITEMS.register("novice_hunter_jacket",            () -> battlemageArmor(PaleLullabyArmorMaterials.HUNTER_NOVICE, ArmorItem.Type.CHESTPLATE, 16));    public static final DeferredItem<ArmorItem> NOVICE_HUNTER_TROUSERS = ITEMS.register("novice_hunter_trousers",            () -> battlemageArmor(PaleLullabyArmorMaterials.HUNTER_NOVICE, ArmorItem.Type.LEGGINGS, 16));    public static final DeferredItem<ArmorItem> NOVICE_HUNTER_BOOTS = ITEMS.register("novice_hunter_boots",            () -> battlemageArmor(PaleLullabyArmorMaterials.HUNTER_NOVICE, ArmorItem.Type.BOOTS, 16));    
    // 中阶（更多银和铁）
    public static final DeferredItem<ArmorItem> ADEPT_HUNTER_HIGH_HAT = ITEMS.register("adept_hunter_high_hat",            () -> battlemageArmor(PaleLullabyArmorMaterials.HUNTER_ADEPT, ArmorItem.Type.HELMET, 18));    public static final DeferredItem<ArmorItem> ADEPT_HUNTER_JACKET = ITEMS.register("adept_hunter_jacket",            () -> battlemageArmor(PaleLullabyArmorMaterials.HUNTER_ADEPT, ArmorItem.Type.CHESTPLATE, 18));    public static final DeferredItem<ArmorItem> ADEPT_HUNTER_TROUSERS = ITEMS.register("adept_hunter_trousers",            () -> battlemageArmor(PaleLullabyArmorMaterials.HUNTER_ADEPT, ArmorItem.Type.LEGGINGS, 18));    public static final DeferredItem<ArmorItem> ADEPT_HUNTER_BOOTS = ITEMS.register("adept_hunter_boots",            () -> battlemageArmor(PaleLullabyArmorMaterials.HUNTER_ADEPT, ArmorItem.Type.BOOTS, 18));    
    // 高阶（更多金，远强于铁，略低于钻石）
    public static final DeferredItem<ArmorItem> EXALTED_HUNTER_HIGH_HAT = ITEMS.register("exalted_hunter_high_hat",            () -> battlemageArmor(PaleLullabyArmorMaterials.HUNTER_EXALTED, ArmorItem.Type.HELMET, 30));    public static final DeferredItem<ArmorItem> EXALTED_HUNTER_JACKET = ITEMS.register("exalted_hunter_jacket",            () -> battlemageArmor(PaleLullabyArmorMaterials.HUNTER_EXALTED, ArmorItem.Type.CHESTPLATE, 30));    public static final DeferredItem<ArmorItem> EXALTED_HUNTER_TROUSERS = ITEMS.register("exalted_hunter_trousers",            () -> battlemageArmor(PaleLullabyArmorMaterials.HUNTER_EXALTED, ArmorItem.Type.LEGGINGS, 30));    public static final DeferredItem<ArmorItem> EXALTED_HUNTER_BOOTS = ITEMS.register("exalted_hunter_boots",            () -> battlemageArmor(PaleLullabyArmorMaterials.HUNTER_EXALTED, ArmorItem.Type.BOOTS, 30));    
    // 放逐（脱离教会，中阶与高阶之间）
    public static final DeferredItem<ArmorItem> EXILED_HUNTER_HIGH_HAT = ITEMS.register("exiled_hunter_high_hat",            () -> battlemageArmor(PaleLullabyArmorMaterials.HUNTER_EXILED, ArmorItem.Type.HELMET, 22));    public static final DeferredItem<ArmorItem> EXILED_HUNTER_JACKET = ITEMS.register("exiled_hunter_jacket",            () -> battlemageArmor(PaleLullabyArmorMaterials.HUNTER_EXILED, ArmorItem.Type.CHESTPLATE, 22));    public static final DeferredItem<ArmorItem> EXILED_HUNTER_TROUSERS = ITEMS.register("exiled_hunter_trousers",            () -> battlemageArmor(PaleLullabyArmorMaterials.HUNTER_EXILED, ArmorItem.Type.LEGGINGS, 22));    public static final DeferredItem<ArmorItem> EXILED_HUNTER_BOOTS = ITEMS.register("exiled_hunter_boots",            () -> battlemageArmor(PaleLullabyArmorMaterials.HUNTER_EXILED, ArmorItem.Type.BOOTS, 22));    
    // 堕落（疯掉的破旧中阶）
    public static final DeferredItem<ArmorItem> FALLEN_HUNTER_HIGH_HAT = ITEMS.register("fallen_hunter_high_hat",            () -> battlemageArmor(PaleLullabyArmorMaterials.HUNTER_FALLEN, ArmorItem.Type.HELMET, 14));    public static final DeferredItem<ArmorItem> FALLEN_HUNTER_JACKET = ITEMS.register("fallen_hunter_jacket",            () -> battlemageArmor(PaleLullabyArmorMaterials.HUNTER_FALLEN, ArmorItem.Type.CHESTPLATE, 14));    public static final DeferredItem<ArmorItem> FALLEN_HUNTER_TROUSERS = ITEMS.register("fallen_hunter_trousers",            () -> battlemageArmor(PaleLullabyArmorMaterials.HUNTER_FALLEN, ArmorItem.Type.LEGGINGS, 14));    public static final DeferredItem<ArmorItem> FALLEN_HUNTER_BOOTS = ITEMS.register("fallen_hunter_boots",            () -> battlemageArmor(PaleLullabyArmorMaterials.HUNTER_FALLEN, ArmorItem.Type.BOOTS, 14));    
    // ===== 审判官套装（布衣+少量铁与金，约等于中阶） =====
    public static final DeferredItem<ArmorItem> INQUISITOR_HIGH_HAT = ITEMS.register("inquisitor_high_hat",            () -> battlemageArmor(PaleLullabyArmorMaterials.INQUISITOR, ArmorItem.Type.HELMET, 18));    public static final DeferredItem<ArmorItem> INQUISITOR_ROBE = ITEMS.register("inquisitor_robe",            () -> battlemageArmor(PaleLullabyArmorMaterials.INQUISITOR, ArmorItem.Type.CHESTPLATE, 18));    public static final DeferredItem<ArmorItem> INQUISITOR_TROUSERS = ITEMS.register("inquisitor_trousers",            () -> battlemageArmor(PaleLullabyArmorMaterials.INQUISITOR, ArmorItem.Type.LEGGINGS, 18));    public static final DeferredItem<ArmorItem> INQUISITOR_LONG_BOOTS = ITEMS.register("inquisitor_long_boots",            () -> battlemageArmor(PaleLullabyArmorMaterials.INQUISITOR, ArmorItem.Type.BOOTS, 18));    
    // ===== 草药学家套装（布+厚皮革，高于原版皮革、略差于铁） =====
    public static final DeferredItem<ArmorItem> HERBALIST_WIDE_BRIMMED_HAT = ITEMS.register("herbalist_wide_brimmed_hat",            () -> battlemageArmor(PaleLullabyArmorMaterials.HERBALIST, ArmorItem.Type.HELMET, 13));    public static final DeferredItem<ArmorItem> HERBALIST_TUNIC = ITEMS.register("herbalist_tunic",            () -> battlemageArmor(PaleLullabyArmorMaterials.HERBALIST, ArmorItem.Type.CHESTPLATE, 13));    public static final DeferredItem<ArmorItem> HERBALIST_BOTTOMS = ITEMS.register("herbalist_bottoms",            () -> battlemageArmor(PaleLullabyArmorMaterials.HERBALIST, ArmorItem.Type.LEGGINGS, 13));    public static final DeferredItem<ArmorItem> HERBALIST_ANKLE_BOOTS = ITEMS.register("herbalist_ankle_boots",            () -> battlemageArmor(PaleLullabyArmorMaterials.HERBALIST, ArmorItem.Type.BOOTS, 13));    
    // ===== 苦修者套装（部件等于金，无靴子） =====
    public static final DeferredItem<ArmorItem> PENITENT_NECK_YOKE = ITEMS.register("penitent_neck_yoke",            () -> battlemageArmor(ArmorMaterials.GOLD, ArmorItem.Type.HELMET, 7));    public static final DeferredItem<ArmorItem> PENITENT_ARM_SHACKLES = ITEMS.register("penitent_arm_shackles",            () -> battlemageArmor(ArmorMaterials.GOLD, ArmorItem.Type.CHESTPLATE, 7));    public static final DeferredItem<ArmorItem> PENITENT_LEG_IRONS = ITEMS.register("penitent_leg_irons",            () -> battlemageArmor(ArmorMaterials.GOLD, ArmorItem.Type.LEGGINGS, 7));    
    // ===== 银体系 =====
    public static final DeferredItem<Item> SILVER_INGOT = ITEMS.registerSimpleItem("silver_ingot");    public static final DeferredItem<Item> RAW_SILVER = ITEMS.registerSimpleItem("raw_silver");    public static final DeferredItem<Item> SILVER_NUGGET = ITEMS.registerSimpleItem("silver_nugget");    public static final DeferredItem<BlockItem> SILVER_ORE_ITEM = ITEMS.registerSimpleBlockItem(PaleLullabyBlocks.SILVER_ORE);    public static final DeferredItem<BlockItem> DEEPSLATE_SILVER_ORE_ITEM = ITEMS.registerSimpleBlockItem(PaleLullabyBlocks.DEEPSLATE_SILVER_ORE);    public static final DeferredItem<BlockItem> SILVER_BLOCK_ITEM = ITEMS.registerSimpleBlockItem(PaleLullabyBlocks.SILVER_BLOCK);    public static final DeferredItem<SwordItem> SILVER_SWORD = ITEMS.register("silver_sword",            () -> new SwordItem(PaleLullabyToolMaterials.SILVER, new Item.Properties()));    public static final DeferredItem<PickaxeItem> SILVER_PICKAXE = ITEMS.register("silver_pickaxe",            () -> new PickaxeItem(PaleLullabyToolMaterials.SILVER, new Item.Properties()));    public static final DeferredItem<AxeItem> SILVER_AXE = ITEMS.register("silver_axe",            () -> new AxeItem(PaleLullabyToolMaterials.SILVER, new Item.Properties()));    public static final DeferredItem<ShovelItem> SILVER_SHOVEL = ITEMS.register("silver_shovel",            () -> new ShovelItem(PaleLullabyToolMaterials.SILVER, new Item.Properties()));    public static final DeferredItem<HoeItem> SILVER_HOE = ITEMS.register("silver_hoe",            () -> new HoeItem(PaleLullabyToolMaterials.SILVER, new Item.Properties()));    public static final DeferredItem<ArmorItem> SILVER_HELMET = ITEMS.register("silver_helmet",            () -> armor(PaleLullabyArmorMaterials.SILVER, ArmorItem.Type.HELMET, 10));    public static final DeferredItem<ArmorItem> SILVER_CHESTPLATE = ITEMS.register("silver_chestplate",            () -> armor(PaleLullabyArmorMaterials.SILVER, ArmorItem.Type.CHESTPLATE, 10));    public static final DeferredItem<ArmorItem> SILVER_LEGGINGS = ITEMS.register("silver_leggings",            () -> armor(PaleLullabyArmorMaterials.SILVER, ArmorItem.Type.LEGGINGS, 10));    public static final DeferredItem<ArmorItem> SILVER_BOOTS = ITEMS.register("silver_boots",            () -> armor(PaleLullabyArmorMaterials.SILVER, ArmorItem.Type.BOOTS, 10));    
    // ===== 血液瓶（四级） =====
    public static final DeferredItem<Item> UNREFINED_BLOOD_BOTTLE = ITEMS.register("unrefined_blood_bottle", () -> new DrinkableBloodItem());    public static final DeferredItem<Item> BLOOD_BOTTLE = ITEMS.register("blood_bottle", () -> new DrinkableBloodItem());    public static final DeferredItem<Item> REFINED_BLOOD_BOTTLE = ITEMS.register("refined_blood_bottle", () -> new DrinkableBloodItem());    public static final DeferredItem<Item> HALLOWED_BLOOD_BOTTLE = ITEMS.register("hallowed_blood_bottle", () -> new DrinkableBloodItem());    
    // ===== 采血器与采血瓶 =====
    public static final DeferredItem<BloodCollectorItem> BLOOD_COLLECTOR = ITEMS.register("blood_collector", BloodCollectorItem::new);    public static final DeferredItem<Item> BLOOD_COLLECTION_BOTTLE = ITEMS.register("blood_collection_bottle",            () -> new Item(new Item.Properties().stacksTo(16)));    
    // ===== 枯血者BOSS刷怪蛋 =====
    public static final DeferredItem<SpawnEggItem> MULLAND_SPAWN_EGG =            ITEMS.register("mulland_spawn_egg",                    () -> new SpawnEggItem(PaleLullabyEntities.MULLAND.get(), 0x1a1a2e, 0xc41e3a, new Item.Properties()));    public static final DeferredItem<SpawnEggItem> PALE_MINION_SPAWN_EGG =            ITEMS.register("pale_minion_spawn_egg",                    () -> new SpawnEggItem(PaleLullabyEntities.PALE_MINION.get(), 0x8b8b8b, 0xc0c0c0, new Item.Properties()));    public static final DeferredItem<SpawnEggItem> VAMPIRE_SPAWN_EGG =            ITEMS.register("vampire_spawn_egg",                    () -> new SpawnEggItem(PaleLullabyEntities.VAMPIRE.get(), 0x2b0f18, 0xc41e3a, new Item.Properties()));    public static final DeferredItem<SpawnEggItem> BLOOD_HUNTER_SPAWN_EGG =            ITEMS.register("blood_hunter_spawn_egg",                    () -> new SpawnEggItem(PaleLullabyEntities.BLOOD_HUNTER.get(), 0x5b3a1e, 0x8a6b3f, new Item.Properties()));    public static final DeferredItem<SpawnEggItem> ADEPT_BLOOD_HUNTER_SPAWN_EGG =            ITEMS.register("adept_blood_hunter_spawn_egg",                    () -> new SpawnEggItem(PaleLullabyEntities.ADEPT_BLOOD_HUNTER.get(), 0x5b3a1e, 0xb0a080, new Item.Properties()));    public static final DeferredItem<SpawnEggItem> VETERAN_BLOOD_HUNTER_SPAWN_EGG =            ITEMS.register("veteran_blood_hunter_spawn_egg",                    () -> new SpawnEggItem(PaleLullabyEntities.VETERAN_BLOOD_HUNTER.get(), 0x3a3a3a, 0xcfd6dd, new Item.Properties()));    public static final DeferredItem<SpawnEggItem> FALLEN_BLOOD_HUNTER_SPAWN_EGG =            ITEMS.register("fallen_blood_hunter_spawn_egg",                    () -> new SpawnEggItem(PaleLullabyEntities.FALLEN_BLOOD_HUNTER.get(), 0x3a3a3a, 0x6e2b2b, new Item.Properties()));    public static final DeferredItem<SpawnEggItem> BLOOD_NOBLE_SPAWN_EGG =            ITEMS.register("blood_noble_spawn_egg",                    () -> new SpawnEggItem(PaleLullabyEntities.BLOOD_NOBLE.get(), 0x2a0f1e, 0x8a1a3a, new Item.Properties()));    public static final DeferredItem<SpawnEggItem> BLOOD_LORD_SPAWN_EGG =            ITEMS.register("blood_lord_spawn_egg",                    () -> new SpawnEggItem(PaleLullabyEntities.BLOOD_LORD.get(), 0x200a14, 0xa63a4a, new Item.Properties()));    public static final DeferredItem<SpawnEggItem> TOLAND_BAT_SPAWN_EGG =            ITEMS.register("toland_bat_spawn_egg",                    () -> new SpawnEggItem(PaleLullabyEntities.TOLAND_BAT.get(), 0x1a0f1e, 0xb03a4a, new Item.Properties()));    public static final DeferredItem<SpawnEggItem> DRIED_BLOOD_GHOST_SPAWN_EGG =            ITEMS.register("dried_blood_ghost_spawn_egg",                    () -> new SpawnEggItem(PaleLullabyEntities.DRIED_BLOOD_GHOST.get(), 0x8a7a6a, 0x4a3a2a, new Item.Properties()));    
    // ===== 苍白悔恨混合物 =====
    public static final DeferredItem<Item> PALE_REGRET_MIXTURE = ITEMS.register("pale_regret_mixture", PaleRegretMixtureItem::new);    
    // ===== 血痕（巨蝙蝠托兰的交易货币） =====
    public static final DeferredItem<Item> BLOOD_MARK =            ITEMS.registerSimpleItem("blood_mark", new Item.Properties().stacksTo(64));    
    // ===== 玩家阵营道具 =====    
    // 金泪滴徽章：加入血猎阵营
    public static final DeferredItem<PlayerFactionItem> GOLDEN_TEAR_BADGE = ITEMS.register("golden_tear_badge",            () -> new PlayerFactionItem(new Item.Properties(), PlayerFaction.Faction.HUNTER,                    "message.pale_lullaby.faction.join.hunter"));    
    // 铁露滴徽章：加入血族阵营
    public static final DeferredItem<PlayerFactionItem> IRON_DEW_BADGE = ITEMS.register("iron_dew_badge",            () -> new PlayerFactionItem(new Item.Properties(), PlayerFaction.Faction.VAMPIRE,                    "message.pale_lullaby.faction.join.vampire"));    
    // 无辜者的舌头：回到中立（偏血猎）
    public static final DeferredItem<PlayerFactionItem> INNOCENTS_TONGUE = ITEMS.register("innocents_tongue",            () -> new PlayerFactionItem(new Item.Properties(), PlayerFaction.Faction.NEUTRAL,                    "message.pale_lullaby.faction.reset"));    
    // ===== 少女的血月（夜晚唤醒全局血月天气） =====
    public static final DeferredItem<Item> MAIDENS_BLOOD_MOON = ITEMS.register("maidens_blood_moon", MaidensBloodMoonItem::new);

    // ===== 血族（初级）套装：革 / 铁 / 金 变体（暂用 battlemage 模型占位） =====
    public static final DeferredItem<BattlemageArmorItem> VAMPIRE_LEATHER_HELMET = ITEMS.register("vampire_leather_helmet",
            () -> battlemageArmor(PaleLullabyArmorMaterials.VAMPIRE_LEATHER, ArmorItem.Type.HELMET, 16));
    public static final DeferredItem<BattlemageArmorItem> VAMPIRE_LEATHER_CHESTPLATE = ITEMS.register("vampire_leather_chestplate",
            () -> battlemageArmor(PaleLullabyArmorMaterials.VAMPIRE_LEATHER, ArmorItem.Type.CHESTPLATE, 16));
    public static final DeferredItem<BattlemageArmorItem> VAMPIRE_LEATHER_LEGGINGS = ITEMS.register("vampire_leather_leggings",
            () -> battlemageArmor(PaleLullabyArmorMaterials.VAMPIRE_LEATHER, ArmorItem.Type.LEGGINGS, 16));
    public static final DeferredItem<BattlemageArmorItem> VAMPIRE_LEATHER_BOOTS = ITEMS.register("vampire_leather_boots",
            () -> battlemageArmor(PaleLullabyArmorMaterials.VAMPIRE_LEATHER, ArmorItem.Type.BOOTS, 16));
    public static final DeferredItem<BattlemageArmorItem> VAMPIRE_IRON_HELMET = ITEMS.register("vampire_iron_helmet",
            () -> battlemageArmor(PaleLullabyArmorMaterials.VAMPIRE_IRON, ArmorItem.Type.HELMET, 16));
    public static final DeferredItem<BattlemageArmorItem> VAMPIRE_IRON_CHESTPLATE = ITEMS.register("vampire_iron_chestplate",
            () -> battlemageArmor(PaleLullabyArmorMaterials.VAMPIRE_IRON, ArmorItem.Type.CHESTPLATE, 16));
    public static final DeferredItem<BattlemageArmorItem> VAMPIRE_IRON_LEGGINGS = ITEMS.register("vampire_iron_leggings",
            () -> battlemageArmor(PaleLullabyArmorMaterials.VAMPIRE_IRON, ArmorItem.Type.LEGGINGS, 16));
    public static final DeferredItem<BattlemageArmorItem> VAMPIRE_IRON_BOOTS = ITEMS.register("vampire_iron_boots",
            () -> battlemageArmor(PaleLullabyArmorMaterials.VAMPIRE_IRON, ArmorItem.Type.BOOTS, 16));
    public static final DeferredItem<BattlemageArmorItem> VAMPIRE_GOLD_HELMET = ITEMS.register("vampire_gold_helmet",
            () -> battlemageArmor(PaleLullabyArmorMaterials.VAMPIRE_GOLD, ArmorItem.Type.HELMET, 16));
    public static final DeferredItem<BattlemageArmorItem> VAMPIRE_GOLD_CHESTPLATE = ITEMS.register("vampire_gold_chestplate",
            () -> battlemageArmor(PaleLullabyArmorMaterials.VAMPIRE_GOLD, ArmorItem.Type.CHESTPLATE, 16));
    public static final DeferredItem<BattlemageArmorItem> VAMPIRE_GOLD_LEGGINGS = ITEMS.register("vampire_gold_leggings",
            () -> battlemageArmor(PaleLullabyArmorMaterials.VAMPIRE_GOLD, ArmorItem.Type.LEGGINGS, 16));
    public static final DeferredItem<BattlemageArmorItem> VAMPIRE_GOLD_BOOTS = ITEMS.register("vampire_gold_boots",
            () -> battlemageArmor(PaleLullabyArmorMaterials.VAMPIRE_GOLD, ArmorItem.Type.BOOTS, 16));

    // ===== 血族贵族套装（暂用 battlemage 模型占位） =====
    public static final DeferredItem<BattlemageArmorItem> BLOOD_NOBLE_HELMET = ITEMS.register("blood_noble_helmet",
            () -> battlemageArmor(PaleLullabyArmorMaterials.BLOOD_NOBLE, ArmorItem.Type.HELMET, 20));
    public static final DeferredItem<BattlemageArmorItem> BLOOD_NOBLE_CHESTPLATE = ITEMS.register("blood_noble_chestplate",
            () -> battlemageArmor(PaleLullabyArmorMaterials.BLOOD_NOBLE, ArmorItem.Type.CHESTPLATE, 20));
    public static final DeferredItem<BattlemageArmorItem> BLOOD_NOBLE_LEGGINGS = ITEMS.register("blood_noble_leggings",
            () -> battlemageArmor(PaleLullabyArmorMaterials.BLOOD_NOBLE, ArmorItem.Type.LEGGINGS, 20));
    public static final DeferredItem<BattlemageArmorItem> BLOOD_NOBLE_BOOTS = ITEMS.register("blood_noble_boots",
            () -> battlemageArmor(PaleLullabyArmorMaterials.BLOOD_NOBLE, ArmorItem.Type.BOOTS, 20));

    // ===== 血族领主套装（暂用 battlemage 模型占位） =====
    public static final DeferredItem<BattlemageArmorItem> BLOOD_LORD_HELMET = ITEMS.register("blood_lord_helmet",
            () -> battlemageArmor(PaleLullabyArmorMaterials.BLOOD_LORD, ArmorItem.Type.HELMET, 26));
    public static final DeferredItem<BattlemageArmorItem> BLOOD_LORD_CHESTPLATE = ITEMS.register("blood_lord_chestplate",
            () -> battlemageArmor(PaleLullabyArmorMaterials.BLOOD_LORD, ArmorItem.Type.CHESTPLATE, 26));
    public static final DeferredItem<BattlemageArmorItem> BLOOD_LORD_LEGGINGS = ITEMS.register("blood_lord_leggings",
            () -> battlemageArmor(PaleLullabyArmorMaterials.BLOOD_LORD, ArmorItem.Type.LEGGINGS, 26));
    public static final DeferredItem<BattlemageArmorItem> BLOOD_LORD_BOOTS = ITEMS.register("blood_lord_boots",
            () -> battlemageArmor(PaleLullabyArmorMaterials.BLOOD_LORD, ArmorItem.Type.BOOTS, 26));
    // ===== 血族套装（GeckoLib 模型盔甲） =====
    public static final DeferredItem<SanguineArmorItem> SANGUINE_HOOD = ITEMS.register("sanguine_hood",
            () -> geoArmor(PaleLullabyArmorMaterials.SANGUINE, ArmorItem.Type.HELMET, 15));
    public static final DeferredItem<SanguineArmorItem> SANGUINE_ROBE = ITEMS.register("sanguine_robe",
            () -> geoArmor(PaleLullabyArmorMaterials.SANGUINE, ArmorItem.Type.CHESTPLATE, 15));
    public static final DeferredItem<SanguineArmorItem> SANGUINE_BOTTOMS = ITEMS.register("sanguine_bottoms",
            () -> geoArmor(PaleLullabyArmorMaterials.SANGUINE, ArmorItem.Type.LEGGINGS, 15));
    public static final DeferredItem<SanguineArmorItem> SANGUINE_BOOTS = ITEMS.register("sanguine_boots",
            () -> geoArmor(PaleLullabyArmorMaterials.SANGUINE, ArmorItem.Type.BOOTS, 15));

    private static SanguineArmorItem geoArmor(Holder<ArmorMaterial> material, ArmorItem.Type type, int durabilityFactor) {
        return new SanguineArmorItem(material, type, new Item.Properties().durability(type.getDurability(durabilityFactor)));
    }
    private static BattlemageArmorItem battlemageArmor(Holder<ArmorMaterial> material, ArmorItem.Type type, int durabilityFactor) {
        return new BattlemageArmorItem(material, type, new Item.Properties().durability(type.getDurability(durabilityFactor)));
    }
    private static ArmorItem armor(Holder<ArmorMaterial> material, ArmorItem.Type type, int durabilityFactor) {        return new ArmorItem(material, type, new Item.Properties().durability(type.getDurability(durabilityFactor)));    }}
