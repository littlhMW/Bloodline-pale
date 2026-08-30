package com.littlh.palelullaby.network;

import com.littlh.palelullaby.PaleLullaby;
import com.littlh.palelullaby.client.BloodEffectClientHandler;
import com.littlh.palelullaby.client.BloodMoonClientState;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.network.event.RegisterPayloadHandlersEvent;
import net.neoforged.neoforge.network.registration.PayloadRegistrar;

@EventBusSubscriber(modid = PaleLullaby.MOD_ID, bus = EventBusSubscriber.Bus.MOD)
public class ModNetworking {
    @SubscribeEvent
    public static void registerPayloadHandlers(RegisterPayloadHandlersEvent event) {
        PayloadRegistrar registrar = event.registrar("1");
        registrar.playToClient(BloodMoonPayload.TYPE, BloodMoonPayload.STREAM_CODEC,
                (payload, context) -> context.enqueueWork(() -> BloodMoonClientState.setActive(payload.active())));
        registrar.playToClient(BloodThirstSuppressPayload.TYPE, BloodThirstSuppressPayload.STREAM_CODEC,
                (payload, context) -> context.enqueueWork(() -> BloodEffectClientHandler.onSuppress(payload.playerId(), payload.untilGameTime())));
    }
}
