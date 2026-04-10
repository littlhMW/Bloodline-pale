package com.littlh.bloodline;

import net.minecraft.client.Minecraft;
import net.minecraft.client.resources.sounds.SoundInstance;
import net.minecraft.client.sounds.SoundEngine;
import net.minecraft.sounds.SoundSource;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.ClientTickEvent;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@EventBusSubscriber(modid = "bloodline", value = Dist.CLIENT)
public class ClientMusicBlocker {

    private static SoundEngine soundEngine;
    private static int tickCounter = 0;
    private static final int CHECK_INTERVAL = 20; // 每秒检查一次

    @SubscribeEvent
    public static void onClientTick(ClientTickEvent.Post event) {
        Minecraft minecraft = Minecraft.getInstance();
        if (minecraft.player == null || minecraft.level == null) return;

        // 只在苍白摇篮维度执行
        if (!minecraft.player.level().dimension().location().toString().equals("bloodline:pale_cradle")) {
            soundEngine = null;
            return;
        }

        tickCounter++;
        if (tickCounter % CHECK_INTERVAL != 0) return;

        // 获取 SoundEngine
        if (soundEngine == null) {
            try {
                Field field = minecraft.getSoundManager().getClass().getDeclaredField("soundEngine");
                field.setAccessible(true);
                soundEngine = (SoundEngine) field.get(minecraft.getSoundManager());
            } catch (Exception e) {
                System.err.println("[Bloodline] Failed to get SoundEngine: " + e.getMessage());
                return;
            }
        }

        // 拦截并停止原版音乐
        try {
            Field field = soundEngine.getClass().getDeclaredField("instanceBySource");
            field.setAccessible(true);
            Object instanceBySource = field.get(soundEngine);

            if (instanceBySource instanceof Map) {
                @SuppressWarnings("unchecked")
                Map<SoundSource, ?> map = (Map<SoundSource, ?>) instanceBySource;
                List<SoundInstance> toStop = new ArrayList<>();

                for (Object value : map.values()) {
                    if (value instanceof SoundInstance instance) {
                        if (instance.getSource() == SoundSource.MUSIC) {
                            String namespace = instance.getLocation().getNamespace();
                            if (!namespace.equals("bloodline")) {
                                toStop.add(instance);
                            }
                        }
                    }
                }

                for (SoundInstance instance : toStop) {
                    soundEngine.stop(instance);
                }
            }
        } catch (Exception e) {
            System.err.println("[Bloodline] Error blocking music: " + e.getMessage());
        }
    }
}
