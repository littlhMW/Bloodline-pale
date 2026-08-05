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
                    output.accept(new ItemStack(PaleLullabyItems.PALE_BONE_BLOCK_ITEM.get()));
                    output.accept(new ItemStack(PaleLullabyItems.BONE_LOG_ITEM.get()));
                    output.accept(new ItemStack(PaleLullabyItems.PALE_LEAVES_ITEM.get()));
                    output.accept(new ItemStack(PaleLullabyItems.PALE_EMBER_ITEM.get()));
                    output.accept(new ItemStack(PaleLullabyItems.PALE_WHEAT_ITEM.get()));
                    output.accept(new ItemStack(PaleLullabyItems.CRIMSON_THORN_ITEM.get()));
                    output.accept(new ItemStack(PaleLullabyItems.CRIMSON_THORN_SPIKE_ITEM.get()));
                    output.accept(new ItemStack(PaleLullabyItems.THORN_LOG_ITEM.get()));
                    output.accept(new ItemStack(PaleLullabyItems.THORN_LEAVES_ITEM.get()));

                    // 种子
                    output.accept(new ItemStack(PaleLullabyItems.PALE_WHEAT_SEEDS.get()));

                    // 灰质桶
                    output.accept(new ItemStack(com.littlh.palelullaby.fluid.ModFluids.GRAY_MATTER_BUCKET.get()));
                    // 血液桶
                    output.accept(new ItemStack(com.littlh.palelullaby.fluid.ModFluids.BLOOD_BUCKET.get()));

                    // 刷怪蛋
                    output.accept(new ItemStack(PaleLullabyItems.MULLAND_SPAWN_EGG.get()));
                    output.accept(new ItemStack(PaleLullabyItems.PALE_MINION_SPAWN_EGG.get()));

                    // 苍白悔恨混合物
                    output.accept(new ItemStack(PaleLullabyItems.PALE_REGRET_MIXTURE.get()));

                    // 血液瓶
                    output.accept(new ItemStack(PaleLullabyItems.UNREFINED_BLOOD_BOTTLE.get()));
                    output.accept(new ItemStack(PaleLullabyItems.BLOOD_BOTTLE.get()));
                    output.accept(new ItemStack(PaleLullabyItems.REFINED_BLOOD_BOTTLE.get()));
                    output.accept(new ItemStack(PaleLullabyItems.HALLOWED_BLOOD_BOTTLE.get()));

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

                    // 流放猎人
                    output.accept(new ItemStack(PaleLullabyItems.EXILED_HUNTER_HIGH_HAT.get()));
                    output.accept(new ItemStack(PaleLullabyItems.EXILED_HUNTER_JACKET.get()));
                    output.accept(new ItemStack(PaleLullabyItems.EXILED_HUNTER_TROUSERS.get()));
                    output.accept(new ItemStack(PaleLullabyItems.EXILED_HUNTER_BOOTS.get()));

                    // 堕落猎人
                    output.accept(new ItemStack(PaleLullabyItems.FALLEN_HUNTER_HIGH_HAT.get()));
                    output.accept(new ItemStack(PaleLullabyItems.FALLEN_HUNTER_JACKET.get()));
                    output.accept(new ItemStack(PaleLullabyItems.FALLEN_HUNTER_TROUSERS.get()));
                    output.accept(new ItemStack(PaleLullabyItems.FALLEN_HUNTER_BOOTS.get()));

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
                } catch (Exception ignored) {}
            })
            .build());
}
