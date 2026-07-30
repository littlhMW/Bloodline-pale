package com.littlh.bloodline;

import net.minecraft.network.chat.Component;
import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;

public class PaleLullabyTabs {
    public static final CreativeModeTab PALE_LULLABY = CreativeModeTab.builder()
            .title(Component.translatable("itemGroup.pale_lullaby"))
            .icon(() -> new ItemStack(Items.BONE))
            .displayItems((params, output) -> {
                try {
                    output.accept(new ItemStack(PaleLullabyItems.MULLAND_SPAWN_EGG.get()));
                    output.accept(new ItemStack(PaleLullabyItems.PALE_MINION_SPAWN_EGG.get()));
                    output.accept(new ItemStack(PaleLullabyItems.LEATHER_HUNTER_HIGH_HAT.get()));
                    output.accept(new ItemStack(PaleLullabyItems.LEATHER_HUNTER_JACKET.get()));
                    output.accept(new ItemStack(PaleLullabyItems.LEATHER_HUNTER_TROUSERS.get()));
                    output.accept(new ItemStack(PaleLullabyItems.LEATHER_HUNTER_BOOTS.get()));
                } catch (Exception ignored) {}
            })
            .build();
}
