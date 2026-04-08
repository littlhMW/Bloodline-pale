package com.littlh.bloodline;

import net.minecraft.client.Minecraft;
import net.minecraft.client.resources.sounds.SoundInstance;
import net.minecraft.client.sounds.SoundEngine;
import net.minecraft.resources.ResourceLocation;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.ClientTickEvent;
import net.neoforged.neoforge.common.NeoForge;

import java.lang.reflect.Field;

@EventBusSubscriber(modid = "bloodline", value = Dist.CLIENT)
public class ClientMusicBlocker {

    private static boolean isInitialized = false;
    private static SoundEngine soundEngine;
    private static long lastCheckTime = 0;
    private static final long CHECK_INTERVAL_MS = 100; // 每100毫秒检查一次

    @SubscribeEvent
    public static void onClientTick(ClientTickEvent.Post event) {
        Minecraft minecraft = Minecraft.getInstance();
        if (minecraft.player == null || minecraft.level == null) {
            return;
        }

        // 只在苍白摇篮维度生效
        if (!minecraft.player.level().dimension().location().toString().equals("bloodline:pale_cradle")) {
            isInitialized = false;
            soundEngine = null;
            return;
        }

        // 获取 SoundEngine
        if (soundEngine == null) {
            try {
                Field field = minecraft.getSoundManager().getClass().getDeclaredField("soundEngine");
                field.setAccessible(true);
                soundEngine = (SoundEngine) field.get(minecraft.getSoundManager());
                System.out.println("[Bloodline] Successfully hooked into SoundEngine");
            } catch (Exception e) {
                System.err.println("[Bloodline] Failed to get SoundEngine: " + e.getMessage());
                return;
            }
        }

        // 限制检查频率，降低性能开销
        long currentTime = System.currentTimeMillis();
        if (currentTime - lastCheckTime < CHECK_INTERVAL_MS) {
            return;
        }
        lastCheckTime = currentTime;

        // 拦截并停止原版音乐
        try {
            Field field = soundEngine.getClass().getDeclaredField("instanceBySource");
            field.setAccessible(true);
            Object instanceBySource = field.get(soundEngine);
            
            if (instanceBySource instanceof java.util.Map) {
                java.util.Map<?, ?> map = (java.util.Map<?, ?>) instanceBySource;
                java.util.List<SoundInstance> toStop = new java.util.ArrayList<>();

                for (Object value : map.values()) {
                    if (value instanceof SoundInstance) {
                        SoundInstance instance = (SoundInstance) value;
                        ResourceLocation loc = instance.getLocation();
                        
                        // 只处理音乐 (SoundSource.MUSIC)
                        if (instance.getSource() == net.minecraft.sounds.SoundSource.MUSIC) {
                            String namespace = loc.getNamespace();
                            // 如果命名空间不是 bloodline，说明是原版或其他模组的音乐
                            if (!namespace.equals("bloodline")) {
                                toStop.add(instance);
                            }
                        }
                    }
                }

                // 停止所有收集到的非模组音乐
                if (!toStop.isEmpty()) {
                    for (SoundInstance instance : toStop) {
                        soundEngine.stop(instance);
                        System.out.println("[Bloodline] Blocked vanilla music: " + instance.getLocation());
                    }
                }
            }
        } catch (Exception e) {
            System.err.println("[Bloodline] Error blocking vanilla music: " + e.getMessage());
        }
    }
}
