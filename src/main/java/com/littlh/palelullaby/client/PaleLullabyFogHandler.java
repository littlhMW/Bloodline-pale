package com.littlh.palelullaby.client;

import com.littlh.palelullaby.PaleLullaby;
import com.mojang.blaze3d.shaders.FogShape;
import net.minecraft.client.Camera;
import net.minecraft.client.Minecraft;
import net.minecraft.core.BlockPos;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;
import net.minecraft.world.level.biome.Biome;
import org.joml.Vector3f;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.ViewportEvent;

/**
 * 迷花平原雾气：进入/离开群系时平滑淡入淡出，
 * 并且从群系外看向迷花平原时也能看到雾气。
 */
@EventBusSubscriber(modid = PaleLullaby.MOD_ID, bus = EventBusSubscriber.Bus.GAME, value = Dist.CLIENT)
public class PaleLullabyFogHandler {
    private static final ResourceKey<Biome> MISTY_FLOWER_PLAINS = ResourceKey.create(
            Registries.BIOME, ResourceLocation.fromNamespaceAndPath(PaleLullaby.MOD_ID, "misty_flower_plains"));

    /** 群系内部的雾最远能见度。 */
    private static final float MAX_FOG_DISTANCE = 30.0F;
    /** 迷花平原的雾颜色（fog_color 0xF0F0F0）。 */
    private static final float FOG_R = 0xF0 / 255.0F;
    private static final float FOG_G = 0xF0 / 255.0F;
    private static final float FOG_B = 0xF0 / 255.0F;

    /** 以相机为中心向四周采样的半径，用于判断是否身处群系边缘。 */
    private static final int LOCAL_RADIUS = 12;
    /** 看向群系时，最远多远的雾气会影响视野。 */
    private static final float LOOK_MAX_DISTANCE = 120.0F;

    /** 上一帧的雾强度，用于时间平滑，避免转身时雾气突变。 */
    private static float lastFactor = 0.0F;

    @SubscribeEvent
    public static void onRenderFog(ViewportEvent.RenderFog event) {
        Minecraft mc = Minecraft.getInstance();
        if (mc.level == null) {
            return;
        }
        float factor = fogFactor(mc, event.getCamera());
        // 时间平滑：转身/移动时雾气渐入渐出
        lastFactor += (factor - lastFactor) * 0.25F;
        if (lastFactor <= 0.001F) {
            return;
        }
        float smooth = smoothstep(lastFactor);
        event.setNearPlaneDistance(Mth.lerp(smooth, event.getNearPlaneDistance(), 0.1F));
        float farTarget = Math.min(event.getFarPlaneDistance(), MAX_FOG_DISTANCE);
        event.setFarPlaneDistance(Mth.lerp(smooth, event.getFarPlaneDistance(), farTarget));
        event.setFogShape(FogShape.SPHERE);
        event.setCanceled(true);
    }

    @SubscribeEvent
    public static void onComputeFogColor(ViewportEvent.ComputeFogColor event) {
        Minecraft mc = Minecraft.getInstance();
        if (mc.level == null) {
            return;
        }
        Camera camera = mc.gameRenderer.getMainCamera();
        float factor = fogFactor(mc, camera);
        if (factor <= 0.001F) {
            return;
        }
        float smooth = smoothstep(factor);
        event.setRed(Mth.lerp(smooth, event.getRed(), FOG_R));
        event.setGreen(Mth.lerp(smooth, event.getGreen(), FOG_G));
        event.setBlue(Mth.lerp(smooth, event.getBlue(), FOG_B));
    }

    /** 综合“身处群系附近”和“看向群系”两个因素，返回 0~1 的雾强度。 */
    private static float fogFactor(Minecraft mc, Camera camera) {
        float local = localFactor(mc, camera.getBlockPosition());
        float ahead = lookFactor(mc, camera);
        return Math.max(local, ahead);
    }

    /** 相机周围一圈处于迷花平原的比例（身处群系边缘时部分生效）。 */
    private static float localFactor(Minecraft mc, BlockPos center) {
        int inBiome = mc.level.getBiome(center).is(MISTY_FLOWER_PLAINS) ? 1 : 0;
        int total = 1;
        for (int i = 0; i < 8; i++) {
            double angle = Math.PI / 8.0 + i * Math.PI / 4.0;
            int dx = (int) Math.round(Math.cos(angle) * LOCAL_RADIUS);
            int dz = (int) Math.round(Math.sin(angle) * LOCAL_RADIUS);
            total++;
            if (mc.level.getBiome(center.offset(dx, 0, dz)).is(MISTY_FLOWER_PLAINS)) {
                inBiome++;
            }
        }
        return inBiome / (float) total;
    }

    /** 沿视线方向采样：越靠近迷花平原，雾越浓。 */
    private static float lookFactor(Minecraft mc, Camera camera) {
        Vector3f pos = camera.getPosition().toVector3f();
        Vector3f look = camera.getLookVector();
        for (float d = 12.0F; d <= LOOK_MAX_DISTANCE; d += 4.0F) {
            BlockPos sample = BlockPos.containing(
                    pos.x() + look.x() * d,
                    pos.y() + look.y() * d,
                    pos.z() + look.z() * d);
            if (mc.level.getBiome(sample).is(MISTY_FLOWER_PLAINS)) {
                return Mth.clamp(1.0F - d / LOOK_MAX_DISTANCE, 0.0F, 1.0F);
            }
        }
        return 0.0F;
    }

    private static float smoothstep(float x) {
        return x * x * (3.0F - 2.0F * x);
    }
}