package com.littlh.palelullaby;

import net.minecraft.core.registries.Registries;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredRegister;

public class PaleLullabyTabs {
    public static final DeferredRegister<CreativeModeTab> CREATIVE_TABS =
            DeferredRegister.create(Registries.CREATIVE_MODE_TAB, PaleLullaby.MOD_ID);

    public static final DeferredHolder<CreativeModeTab, CreativeModeTab> PALE_LULLABY = CREATIVE_TABS.register("pale_lullaby", () -> CreativeModeTab.builder()
            .title(Component.translatable("itemGroup.pale_lullaby"))
            .icon(() -> new ItemStack(Items.BONE))
            .displayItems((params, output) -> {
                try {
                    // 方块
                    output.accept(new ItemStack(PaleLullabyItems.PALE_GRASS_BLOCK_ITEM.get()));
                    output.accept(new ItemStack(PaleLullabyItems.WITHERED_DIRT_ITEM.get()));
                    output.accept(new ItemStack(PaleLullabyItems.WITHERED_GRASS_BLOCK_ITEM.get()));
                    output.accept(new ItemStack(PaleLullabyItems.WHITE_MATTER_ITEM.get()));
                    output.accept(new ItemStack(PaleLullabyItems.WITHERED_MISTLETOE_ITEM.get()));
                    output.accept(new ItemStack(PaleLullabyItems.BONE_LOG_ITEM.get()));
                    output.accept(new ItemStack(PaleLullabyItems.PALE_LEAVES_ITEM.get()));
                    output.accept(new ItemStack(PaleLullabyItems.BONE_SAPLING_ITEM.get()));
                    output.accept(new ItemStack(PaleLullabyItems.RED_NEEDLE_SAPLING_ITEM.get()));
                    output.accept(new ItemStack(PaleLullabyItems.PALE_EMBER_ITEM.get()));
                    output.accept(new ItemStack(PaleLullabyItems.PALE_EMBER_FARMLAND_ITEM.get()));
                    output.accept(new ItemStack(PaleLullabyItems.MOTTLED_WHITE_MATTER_ITEM.get()));
                    output.accept(new ItemStack(PaleLullabyItems.PALE_WHEAT_ITEM.get()));
                    output.accept(new ItemStack(PaleLullabyItems.CRIMSON_THORN_ITEM.get()));
                    output.accept(new ItemStack(PaleLullabyItems.CRIMSON_THORN_BERRY.get()));
                    output.accept(new ItemStack(PaleLullabyItems.CRIMSON_ROSE_ITEM.get()));
                    output.accept(new ItemStack(PaleLullabyItems.CRIMSON_ROSE_NECTAR.get()));
                    output.accept(new ItemStack(PaleLullabyItems.RED_NEEDLE_LOG_ITEM.get()));
                    output.accept(new ItemStack(PaleLullabyItems.RED_NEEDLE_LEAVES_ITEM.get()));
                    output.accept(new ItemStack(PaleLullabyItems.SOAKED_MUD_ITEM.get()));
                    output.accept(new ItemStack(PaleLullabyItems.SOAKED_MUD_GRASS_ITEM.get()));
                    output.accept(new ItemStack(PaleLullabyItems.BLIND_FLOSS_FLOWER_ITEM.get()));
                    output.accept(new ItemStack(PaleLullabyItems.FROST_MOONFLOWER_ITEM.get()));
                    output.accept(new ItemStack(PaleLullabyItems.GHOST_ORCHID_ITEM.get()));
                    output.accept(new ItemStack(PaleLullabyItems.ROTHEART_MUSHROOM_ITEM.get()));
                    output.accept(new ItemStack(PaleLullabyItems.WIDOW_THORN_ITEM.get()));
                    output.accept(new ItemStack(PaleLullabyItems.WIDOW_THORN_FRUIT.get()));
                    output.accept(new ItemStack(PaleLullabyItems.ICE_SPIKE_ITEM.get()));
                    output.accept(new ItemStack(PaleLullabyItems.IRON_MAIDEN_ITEM.get()));
                    output.accept(new ItemStack(PaleLullabyItems.BROKEN_SWORD_ITEM.get()));
                    output.accept(new ItemStack(PaleLullabyItems.SCAR_MARK.get()));
                    output.accept(new ItemStack(PaleLullabyItems.BLOOD_MARK.get()));

                    // 种子
                    output.accept(new ItemStack(PaleLullabyItems.PALE_WHEAT_SEEDS.get()));

                    // 银体系
                    output.accept(new ItemStack(PaleLullabyItems.SILVER_ORE_ITEM.get()));
                    output.accept(new ItemStack(PaleLullabyItems.DEEPSLATE_SILVER_ORE_ITEM.get()));
                    output.accept(new ItemStack(PaleLullabyItems.SILVER_BLOCK_ITEM.get()));
                    output.accept(new ItemStack(PaleLullabyItems.RAW_SILVER.get()));
                    output.accept(new ItemStack(PaleLullabyItems.SILVER_NUGGET.get()));
                    output.accept(new ItemStack(PaleLullabyItems.SILVER_INGOT.get()));
                    output.accept(new ItemStack(PaleLullabyItems.SILVER_SWORD.get()));
                    output.accept(new ItemStack(PaleLullabyItems.SILVER_PICKAXE.get()));
                    output.accept(new ItemStack(PaleLullabyItems.SILVER_AXE.get()));
                    output.accept(new ItemStack(PaleLullabyItems.SILVER_SHOVEL.get()));
                    output.accept(new ItemStack(PaleLullabyItems.SILVER_HOE.get()));
                    output.accept(new ItemStack(PaleLullabyItems.SILVER_HELMET.get()));
                    output.accept(new ItemStack(PaleLullabyItems.SILVER_CHESTPLATE.get()));
                    output.accept(new ItemStack(PaleLullabyItems.SILVER_LEGGINGS.get()));
                    output.accept(new ItemStack(PaleLullabyItems.SILVER_BOOTS.get()));

                    // 灰质桶
                    output.accept(new ItemStack(com.littlh.palelullaby.fluid.ModFluids.GRAY_MATTER_BUCKET.get()));
                    // 血液桶
                    output.accept(new ItemStack(com.littlh.palelullaby.fluid.ModFluids.BLOOD_BUCKET.get()));

                    // 刷怪蛋
                    output.accept(new ItemStack(PaleLullabyItems.TOLAND_BAT_SPAWN_EGG.get()));
                    output.accept(new ItemStack(PaleLullabyItems.MULLAND_SPAWN_EGG.get()));
                    output.accept(new ItemStack(PaleLullabyItems.PALE_MINION_SPAWN_EGG.get()));
                    output.accept(new ItemStack(PaleLullabyItems.VAMPIRE_SPAWN_EGG.get()));
                    output.accept(new ItemStack(PaleLullabyItems.BLOOD_HUNTER_SPAWN_EGG.get()));
                    output.accept(new ItemStack(PaleLullabyItems.ADEPT_BLOOD_HUNTER_SPAWN_EGG.get()));
                    output.accept(new ItemStack(PaleLullabyItems.VETERAN_BLOOD_HUNTER_SPAWN_EGG.get()));
                    output.accept(new ItemStack(PaleLullabyItems.FALLEN_BLOOD_HUNTER_SPAWN_EGG.get()));
                    output.accept(new ItemStack(PaleLullabyItems.DRIED_BLOOD_GHOST_SPAWN_EGG.get()));
                    output.accept(new ItemStack(PaleLullabyItems.BLOOD_NOBLE_SPAWN_EGG.get()));
                    output.accept(new ItemStack(PaleLullabyItems.BLOOD_LORD_SPAWN_EGG.get()));

                    // 苍白悔恨混合物
                    output.accept(new ItemStack(PaleLullabyItems.PALE_REGRET_MIXTURE.get()));

                    // 少女的血月
                    output.accept(new ItemStack(PaleLullabyItems.MAIDENS_BLOOD_MOON.get()));

                    // 玩家阵营道具
                    output.accept(new ItemStack(PaleLullabyItems.GOLDEN_TEAR_BADGE.get()));
                    output.accept(new ItemStack(PaleLullabyItems.IRON_DEW_BADGE.get()));
                    output.accept(new ItemStack(PaleLullabyItems.INNOCENTS_TONGUE.get()));

                    // 血液瓶
                    output.accept(new ItemStack(PaleLullabyItems.UNREFINED_BLOOD_BOTTLE.get()));
                    output.accept(new ItemStack(PaleLullabyItems.BLOOD_BOTTLE.get()));
                    output.accept(new ItemStack(PaleLullabyItems.REFINED_BLOOD_BOTTLE.get()));
                    output.accept(new ItemStack(PaleLullabyItems.HALLOWED_BLOOD_BOTTLE.get()));
                    // 采血器与采血瓶
                    output.accept(new ItemStack(PaleLullabyItems.BLOOD_COLLECTOR.get()));
                    output.accept(new ItemStack(PaleLullabyItems.BLOOD_COLLECTION_BOTTLE.get()));

                    // 初级猎人（皮甲）
                    output.accept(new ItemStack(PaleLullabyItems.LEATHER_HUNTER_HIGH_HAT.get()));
                    output.accept(new ItemStack(PaleLullabyItems.LEATHER_HUNTER_JACKET.get()));
                    output.accept(new ItemStack(PaleLullabyItems.LEATHER_HUNTER_TROUSERS.get()));
                    output.accept(new ItemStack(PaleLullabyItems.LEATHER_HUNTER_BOOTS.get()));

                    // 布猎人
                    output.accept(new ItemStack(PaleLullabyItems.CLOTH_HUNTER_HIGH_HAT.get()));
                    output.accept(new ItemStack(PaleLullabyItems.CLOTH_HUNTER_JACKET.get()));
                    output.accept(new ItemStack(PaleLullabyItems.CLOTH_HUNTER_TROUSERS.get()));
                    output.accept(new ItemStack(PaleLullabyItems.CLOTH_HUNTER_ANKLE_BOOTS.get()));

                    // 新手猎人
                    output.accept(new ItemStack(PaleLullabyItems.NOVICE_HUNTER_HIGH_HAT.get()));
                    output.accept(new ItemStack(PaleLullabyItems.NOVICE_HUNTER_JACKET.get()));
                    output.accept(new ItemStack(PaleLullabyItems.NOVICE_HUNTER_TROUSERS.get()));
                    output.accept(new ItemStack(PaleLullabyItems.NOVICE_HUNTER_BOOTS.get()));

                    // 铁猎人
                    output.accept(new ItemStack(PaleLullabyItems.IRON_HUNTER_HIGH_HAT.get()));
                    output.accept(new ItemStack(PaleLullabyItems.IRON_HUNTER_JACKET.get()));
                    output.accept(new ItemStack(PaleLullabyItems.IRON_HUNTER_TROUSERS.get()));
                    output.accept(new ItemStack(PaleLullabyItems.IRON_HUNTER_BOOTS.get()));

                    // 熟练猎人
                    output.accept(new ItemStack(PaleLullabyItems.ADEPT_HUNTER_HIGH_HAT.get()));
                    output.accept(new ItemStack(PaleLullabyItems.ADEPT_HUNTER_JACKET.get()));
                    output.accept(new ItemStack(PaleLullabyItems.ADEPT_HUNTER_TROUSERS.get()));
                    output.accept(new ItemStack(PaleLullabyItems.ADEPT_HUNTER_BOOTS.get()));

                    // 崇高猎人
                    output.accept(new ItemStack(PaleLullabyItems.EXALTED_HUNTER_HIGH_HAT.get()));
                    output.accept(new ItemStack(PaleLullabyItems.EXALTED_HUNTER_JACKET.get()));
                    output.accept(new ItemStack(PaleLullabyItems.EXALTED_HUNTER_TROUSERS.get()));
                    output.accept(new ItemStack(PaleLullabyItems.EXALTED_HUNTER_BOOTS.get()));

                    // 流放/堕落猎人：仅怪物掉落，不放入创造栏

                    // 审判官
                    output.accept(new ItemStack(PaleLullabyItems.INQUISITOR_HIGH_HAT.get()));
                    output.accept(new ItemStack(PaleLullabyItems.INQUISITOR_ROBE.get()));
                    output.accept(new ItemStack(PaleLullabyItems.INQUISITOR_TROUSERS.get()));
                    output.accept(new ItemStack(PaleLullabyItems.INQUISITOR_LONG_BOOTS.get()));

                    // 草药学家
                    output.accept(new ItemStack(PaleLullabyItems.HERBALIST_WIDE_BRIMMED_HAT.get()));
                    output.accept(new ItemStack(PaleLullabyItems.HERBALIST_TUNIC.get()));
                    output.accept(new ItemStack(PaleLullabyItems.HERBALIST_BOTTOMS.get()));
                    output.accept(new ItemStack(PaleLullabyItems.HERBALIST_ANKLE_BOOTS.get()));

                    // 苦修者
                    output.accept(new ItemStack(PaleLullabyItems.PENITENT_NECK_YOKE.get()));
                    output.accept(new ItemStack(PaleLullabyItems.PENITENT_ARM_SHACKLES.get()));
                    output.accept(new ItemStack(PaleLullabyItems.PENITENT_LEG_IRONS.get()));

                    // 血族（初级）套装：革 / 铁 / 金
                    output.accept(new ItemStack(PaleLullabyItems.VAMPIRE_LEATHER_HELMET.get()));
                    output.accept(new ItemStack(PaleLullabyItems.VAMPIRE_LEATHER_CHESTPLATE.get()));
                    output.accept(new ItemStack(PaleLullabyItems.VAMPIRE_LEATHER_LEGGINGS.get()));
                    output.accept(new ItemStack(PaleLullabyItems.VAMPIRE_LEATHER_BOOTS.get()));
                    output.accept(new ItemStack(PaleLullabyItems.VAMPIRE_IRON_HELMET.get()));
                    output.accept(new ItemStack(PaleLullabyItems.VAMPIRE_IRON_CHESTPLATE.get()));
                    output.accept(new ItemStack(PaleLullabyItems.VAMPIRE_IRON_LEGGINGS.get()));
                    output.accept(new ItemStack(PaleLullabyItems.VAMPIRE_IRON_BOOTS.get()));
                    output.accept(new ItemStack(PaleLullabyItems.VAMPIRE_GOLD_HELMET.get()));
                    output.accept(new ItemStack(PaleLullabyItems.VAMPIRE_GOLD_CHESTPLATE.get()));
                    output.accept(new ItemStack(PaleLullabyItems.VAMPIRE_GOLD_LEGGINGS.get()));
                    output.accept(new ItemStack(PaleLullabyItems.VAMPIRE_GOLD_BOOTS.get()));

                    // 血族贵族套装
                    output.accept(new ItemStack(PaleLullabyItems.BLOOD_NOBLE_HELMET.get()));
                    output.accept(new ItemStack(PaleLullabyItems.BLOOD_NOBLE_CHESTPLATE.get()));
                    output.accept(new ItemStack(PaleLullabyItems.BLOOD_NOBLE_LEGGINGS.get()));
                    output.accept(new ItemStack(PaleLullabyItems.BLOOD_NOBLE_BOOTS.get()));

                    // 血族领主套装
                    output.accept(new ItemStack(PaleLullabyItems.BLOOD_LORD_HELMET.get()));
                    output.accept(new ItemStack(PaleLullabyItems.BLOOD_LORD_CHESTPLATE.get()));
                    output.accept(new ItemStack(PaleLullabyItems.BLOOD_LORD_LEGGINGS.get()));
                    output.accept(new ItemStack(PaleLullabyItems.BLOOD_LORD_BOOTS.get()));                    // 血族套装
                    output.accept(new ItemStack(PaleLullabyItems.SANGUINE_HOOD.get()));
                    output.accept(new ItemStack(PaleLullabyItems.SANGUINE_ROBE.get()));
                    output.accept(new ItemStack(PaleLullabyItems.SANGUINE_BOTTOMS.get()));
                    output.accept(new ItemStack(PaleLullabyItems.SANGUINE_BOOTS.get()));
                } catch (Exception ignored) {}
            })
            .build());
}

