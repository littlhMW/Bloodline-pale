package com.littlh.bloodline;

import net.minecraft.client.Minecraft;
import net.minecraft.client.resources.sounds.SimpleSoundInstance;
import net.minecraft.client.resources.sounds.SoundInstance;
import net.minecraft.client.sounds.SoundManager;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundSource;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.ClientTickEvent;

import java.util.List;
import java.util.Random;

@EventBusSubscriber(modid = "bloodline", value = Dist.CLIENT)
public class ClientMusicManager {

    private static final Random RANDOM = new Random();
    private static SoundInstance currentMusic = null;
    private static int musicTimer = 0;
    private static int fadeTimer = 0;
    
    private static final int MUSIC_SWITCH_INTERVAL = 12000;
    private static final int FADE_DURATION = 40;

    @SubscribeEvent
    public static void onClientTick(ClientTickEvent.Pre event) {
        Minecraft minecraft = Minecraft.getInstance();
        if (minecraft.player == null || minecraft.level == null) return;
        
        if (!minecraft.player.level().dimension().location().toString().equals("bloodline:pale_cradle")) {
            stopMusic();
            return;
        }

        SoundManager soundManager = minecraft.getSoundManager();
        List<ResourceLocation> trackLocations = ModSounds.getMusicTrackLocations();
        if (trackLocations.isEmpty()) return;

        // 处理淡出
        if (fadeTimer > 0) {
            fadeTimer--;
            if (currentMusic != null) {
                float fadeVolume = (float) fadeTimer / FADE_DURATION;
                soundManager.stop(currentMusic);
                // 重新创建相同 SoundEvent 但音量减小的实例
                currentMusic = new SimpleSoundInstance(
                        currentMusic.getLocation(),
                        SoundSource.MUSIC,
                        fadeVolume,
                        1.0f,
                        SoundInstance.createUnseededRandom(),
                        false,
                        0,
                        SoundInstance.Attenuation.NONE,
                        0.0, 0.0, 0.0,
                        true
                );
                soundManager.play(currentMusic);
            }
        }

        // 检查是否需要开始或切换
        if (currentMusic == null || !soundManager.isActive(currentMusic)) {
            if (musicTimer <= 0) {
                playNextTrack(soundManager, trackLocations);
                musicTimer = MUSIC_SWITCH_INTERVAL;
                fadeTimer = 0;
            } else {
                musicTimer--;
            }
        } else {
            if (musicTimer > 0) {
                musicTimer--;
            }
            if (musicTimer == 0 && fadeTimer == 0) {
                fadeTimer = FADE_DURATION;
                musicTimer = MUSIC_SWITCH_INTERVAL;
            }
        }
    }

    private static void playNextTrack(SoundManager soundManager, List<ResourceLocation> trackLocations) {
        ResourceLocation nextLocation = trackLocations.get(RANDOM.nextInt(trackLocations.size()));
        currentMusic = new SimpleSoundInstance(
                nextLocation,
                SoundSource.MUSIC,
                1.0f,
                1.0f,
                SoundInstance.createUnseededRandom(),
                false,
                0,
                SoundInstance.Attenuation.NONE,
                0.0, 0.0, 0.0,
                true
        );
        soundManager.play(currentMusic);
    }

    private static void stopMusic() {
        if (currentMusic != null) {
            Minecraft.getInstance().getSoundManager().stop(currentMusic);
            currentMusic = null;
        }
        musicTimer = 0;
        fadeTimer = 0;
    }
}
