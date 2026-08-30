package com.littlh.palelullaby.client;

import com.littlh.palelullaby.PaleLullaby;
import com.littlh.palelullaby.PaleLullabyEffects;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.BufferBuilder;
import com.mojang.blaze3d.vertex.BufferUploader;
import com.mojang.blaze3d.vertex.DefaultVertexFormat;
import com.mojang.blaze3d.vertex.Tesselator;
import com.mojang.blaze3d.vertex.VertexFormat;
import com.mojang.blaze3d.vertex.VertexSorting;
import java.util.concurrent.ConcurrentHashMap;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.GameRenderer;
import net.minecraft.util.Mth;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.entity.player.Player;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.RenderLevelStageEvent;
import org.joml.Matrix4f;

/**
 * 血疾/渴血客户端视觉效果：全屏红色视野覆盖（血疾重、渴血2级起轻微），
 * 并记录喝血对渴血症状的抑制状态。
 */
@EventBusSubscriber(modid = PaleLullaby.MOD_ID, bus = EventBusSubscriber.Bus.GAME, value = Dist.CLIENT)
public class BloodEffectClientHandler {
    /** 玩家实体ID -> 症状被抑制到的游戏时间。 */
    private static final ConcurrentHashMap<Integer, Long> SUPPRESSED_UNTIL = new ConcurrentHashMap<>();

    private BloodEffectClientHandler() {
    }

    public static void onSuppress(int playerId, long untilGameTime) {
        SUPPRESSED_UNTIL.put(playerId, untilGameTime);
    }

    @SubscribeEvent
    public static void onRenderLevel(RenderLevelStageEvent event) {
        if (event.getStage() != RenderLevelStageEvent.Stage.AFTER_LEVEL) {
            return;
        }
        Minecraft mc = Minecraft.getInstance();
        Player player = mc.player;
        if (player == null || player.level() == null) {
            return;
        }
        float alpha = 0.0F;
        if (player.hasEffect(PaleLullabyEffects.BLOOD_FRENZY) && !player.hasEffect(PaleLullabyEffects.ROSE_NECTAR)) {
            float pulse = 0.5F + 0.5F * Mth.sin(player.level().getGameTime() * 0.06F);
            alpha = 0.30F + 0.10F * pulse;
        } else {
            MobEffectInstance thirst = player.getEffect(PaleLullabyEffects.BLOOD_THIRST);
            if (thirst != null && thirst.getAmplifier() >= 1
                    && !isSuppressed(player.getId(), player.level().getGameTime())
                    && !player.hasEffect(PaleLullabyEffects.ROSE_NECTAR)) {
                alpha = 0.14F;
            }
        }
        if (alpha > 0.0F) {
            drawRedOverlay(alpha);
        }
    }

    private static boolean isSuppressed(int playerId, long gameTime) {
        Long until = SUPPRESSED_UNTIL.get(playerId);
        if (until == null) {
            return false;
        }
        if (until <= gameTime) {
            SUPPRESSED_UNTIL.remove(playerId);
            return false;
        }
        return true;
    }

    private static void drawRedOverlay(float alpha) {
        Matrix4f savedProjection = RenderSystem.getProjectionMatrix();
        RenderSystem.disableDepthTest();
        RenderSystem.enableBlend();
        RenderSystem.defaultBlendFunc();
        RenderSystem.setProjectionMatrix(new Matrix4f().setOrtho(-1.0F, 1.0F, -1.0F, 1.0F, -1.0F, 1.0F), VertexSorting.ORTHOGRAPHIC_Z);
        RenderSystem.getModelViewStack().pushMatrix();
        RenderSystem.getModelViewStack().identity();
        RenderSystem.applyModelViewMatrix();
        RenderSystem.setShader(GameRenderer::getPositionColorShader);
        Tesselator tesselator = Tesselator.getInstance();
        BufferBuilder buffer = tesselator.begin(VertexFormat.Mode.QUADS, DefaultVertexFormat.POSITION_COLOR);
        buffer.addVertex(-1.0F, -1.0F, 0.0F).setColor(1.0F, 0.05F, 0.08F, alpha);
        buffer.addVertex(1.0F, -1.0F, 0.0F).setColor(1.0F, 0.05F, 0.08F, alpha);
        buffer.addVertex(1.0F, 1.0F, 0.0F).setColor(1.0F, 0.05F, 0.08F, alpha);
        buffer.addVertex(-1.0F, 1.0F, 0.0F).setColor(1.0F, 0.05F, 0.08F, alpha);
        BufferUploader.drawWithShader(buffer.buildOrThrow());
        RenderSystem.getModelViewStack().popMatrix();
        RenderSystem.applyModelViewMatrix();
        RenderSystem.setProjectionMatrix(savedProjection, VertexSorting.byDistance(0.0F, 0.0F, 0.0F));
        RenderSystem.enableDepthTest();
        RenderSystem.disableBlend();
    }
}
