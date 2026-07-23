package com.littlh.bloodline.client;

import com.littlh.bloodline.Bloodline;
import com.littlh.bloodline.client.renderer.KuxuezheRenderer;
import com.littlh.bloodline.entity.BloodlineEntities;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.EntityRenderersEvent;

@EventBusSubscriber(modid = Bloodline.MOD_ID, bus = EventBusSubscriber.Bus.MOD, value = Dist.CLIENT)
public class BloodlineClientEvents {

    @SubscribeEvent
    public static void registerRenderers(EntityRenderersEvent.RegisterRenderers event) {
        event.registerEntityRenderer(BloodlineEntities.KUXUEZHE.get(), KuxuezheRenderer::new);
    }
}
