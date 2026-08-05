package com.littlh.palelullaby;

import com.littlh.palelullaby.entity.PaleLullabyEntities;
import com.littlh.palelullaby.entity.MullandEntity;
import com.littlh.palelullaby.entity.minion.PaleMinionEntity;
import com.littlh.palelullaby.fluid.ModFluids;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.fml.common.Mod;
import net.neoforged.neoforge.event.entity.EntityAttributeCreationEvent;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;

@Mod("pale_lullaby")
public class PaleLullaby {
    public static final String MOD_ID = "pale_lullaby";

    public PaleLullaby(IEventBus modEventBus) {
        PaleLullabyBlocks.BLOCKS.register(modEventBus);
        PaleLullabyItems.ITEMS.register(modEventBus);
        PaleLullabyFeatures.FEATURES.register(modEventBus);
        PaleLullabyEntities.ENTITY_TYPES.register(modEventBus);
        ModFluids.register(modEventBus);
        PaleLullabyTabs.CREATIVE_TABS.register(modEventBus);
        ModSounds.register(modEventBus);
    }

    @EventBusSubscriber(modid = MOD_ID, bus = EventBusSubscriber.Bus.MOD)
    public static class ModEvents {
        @SubscribeEvent
        public static void onEntityAttributeCreation(EntityAttributeCreationEvent event) {
            event.put(PaleLullabyEntities.MULLAND.get(), MullandEntity.createAttributes().build());
            event.put(PaleLullabyEntities.PALE_MINION.get(), PaleMinionEntity.createAttributes().build());
        }
    }
}
