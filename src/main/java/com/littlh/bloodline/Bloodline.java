package com.littlh.bloodline;

import com.littlh.bloodline.entity.BloodlineEntities;
import com.littlh.bloodline.entity.KuxuezheEntity;
import com.littlh.bloodline.entity.minion.PaleMinionEntity;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.fml.common.Mod;
import net.neoforged.neoforge.event.entity.EntityAttributeCreationEvent;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;

@Mod("bloodline")
public class Bloodline {
    public static final String MOD_ID = "bloodline";

    public Bloodline(IEventBus modEventBus) {
        BloodlineBlocks.BLOCKS.register(modEventBus);
        BloodlineItems.ITEMS.register(modEventBus);
        BloodlineEntities.ENTITY_TYPES.register(modEventBus);
        ModSounds.register(modEventBus);
    }

    @EventBusSubscriber(modid = MOD_ID, bus = EventBusSubscriber.Bus.MOD)
    public static class ModEvents {
        @SubscribeEvent
        public static void onEntityAttributeCreation(EntityAttributeCreationEvent event) {
            event.put(BloodlineEntities.KUXUEZHE.get(), KuxuezheEntity.createAttributes().build());
            event.put(BloodlineEntities.PALE_MINION.get(), PaleMinionEntity.createAttributes().build());
        }
    }
}
