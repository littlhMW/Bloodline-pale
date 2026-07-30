package com.littlh.bloodline.client;

import com.littlh.bloodline.PaleLullaby;
import com.littlh.bloodline.client.renderer.MullandRenderer;
import com.littlh.bloodline.entity.PaleLullabyEntities;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.EntityRenderersEvent;

@EventBusSubscriber(modid = PaleLullaby.MOD_ID, bus = EventBusSubscriber.Bus.MOD, value = Dist.CLIENT)
public class PaleLullabyClientEvents {

    @SubscribeEvent
    public static void registerRenderers(EntityRenderersEvent.RegisterRenderers event) {
        event.registerEntityRenderer(PaleLullabyEntities.MULLAND.get(), MullandRenderer::new);
    }
}
