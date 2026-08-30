package com.littlh.palelullaby.client;

import com.littlh.palelullaby.PaleLullaby;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.BufferBuilder;
import com.mojang.blaze3d.vertex.BufferUploader;
import com.mojang.blaze3d.vertex.DefaultVertexFormat;
import com.mojang.blaze3d.vertex.Tesselator;
import com.mojang.blaze3d.vertex.VertexFormat;
import com.mojang.blaze3d.vertex.VertexSorting;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.GameRenderer;
import net.minecraft.util.Mth;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.RenderLevelStageEvent;
import net.neoforged.neoforge.client.event.ViewportEvent;
import org.joml.Matrix4f;

/**
 * 血月客户端视觉效果：暗红色雾 + 暗红天空覆盖 + 微弱全局血色滤镜。
 */
@EventBusSubscriber(modid = PaleLullaby.MOD_ID, bus = EventBusSubscriber.Bus.GAME, value = Dist.CLIENT)
public class BloodMoonClientHandler {

    @SubscribeEvent
    public static void onComputeFogColor(ViewportEvent.ComputeFogColor event) {
        if (!BloodMoonClientState.isActive()) {
            return;
        }
        Minecraft mc = Minecraft.getInstance();
        float pulse = mc.level != null
                ? 0.5F + 0.5F * Mth.sin(mc.level.getGameTime() * 0.04F)
                : 0.5F;
        event.setRed(0.42F + 0.10F * pulse);
        event.setGreen(0.05F + 0.02F * pulse);
        event.setBlue(0.07F + 0.03F * pulse);
    }

    @SubscribeEvent
    public static void onRenderLevel(RenderLevelStageEvent event) {
        if (!BloodMoonClientState.isActive()) {
            return;
        }
        if (event.getStage() == RenderLevelStageEvent.Stage.AFTER_SKY) {
            drawFullscreenQuad(0.38F, 0.55F, 0.03F, 0.05F);
        } else if (event.getStage() == RenderLevelStageEvent.Stage.AFTER_LEVEL) {
            drawFullscreenQuad(0.10F, 0.50F, 0.02F, 0.04F);
        }
    }

    /** 以 NDC 全屏四边形叠加一层血色覆盖。 */
    private static void drawFullscreenQuad(float alpha, float red, float green, float blue) {
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
        buffer.addVertex(-1.0F, -1.0F, 0.0F).setColor(red, green, blue, alpha);
        buffer.addVertex(1.0F, -1.0F, 0.0F).setColor(red, green, blue, alpha);
        buffer.addVertex(1.0F, 1.0F, 0.0F).setColor(red, green, blue, alpha);
        buffer.addVertex(-1.0F, 1.0F, 0.0F).setColor(red, green, blue, alpha);
        BufferUploader.drawWithShader(buffer.buildOrThrow());
        RenderSystem.getModelViewStack().popMatrix();
        RenderSystem.applyModelViewMatrix();
        RenderSystem.setProjectionMatrix(savedProjection, VertexSorting.byDistance(0.0F, 0.0F, 0.0F));
        RenderSystem.enableDepthTest();
        RenderSystem.disableBlend();
    }
}
