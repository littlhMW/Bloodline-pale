package com.littlh.bloodline;

import com.littlh.bloodline.entity.PaleLullabyEntities;
import com.littlh.bloodline.entity.MullandEntity;
import com.littlh.bloodline.entity.minion.PaleMinionEntity;
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
        PaleLullabyEntities.ENTITY_TYPES.register(modEventBus);
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
