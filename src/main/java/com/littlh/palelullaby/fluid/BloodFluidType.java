package com.littlh.palelullaby.fluid;

import com.littlh.palelullaby.PaleLullaby;
import com.mojang.blaze3d.shaders.FogShape;
import com.mojang.blaze3d.systems.RenderSystem;
import net.minecraft.client.Camera;
import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.renderer.FogRenderer;
import net.minecraft.resources.ResourceLocation;
import net.neoforged.neoforge.client.extensions.common.IClientFluidTypeExtensions;
import net.neoforged.neoforge.fluids.FluidType;
import org.jetbrains.annotations.Nullable;
import org.joml.Vector3f;

public class BloodFluidType extends FluidType {
    private final ResourceLocation stillTexture;
    private final ResourceLocation flowingTexture;
    private final ResourceLocation overlayTexture;
    private final int tintColor;

    public BloodFluidType() {
        super(Properties.create()
                .descriptionId("block.pale_lullaby.blood")
                .density(1060)
                .viscosity(1200)
                .canSwim(true)
                .canDrown(true)
                .canPushEntity(true)
                .fallDistanceModifier(0.5F)
                .lightLevel(0)
                .canExtinguish(false)
                .canConvertToSource(false)
        );
        this.stillTexture = ResourceLocation.fromNamespaceAndPath(PaleLullaby.MOD_ID, "block/blood_fluid_still");
        this.flowingTexture = ResourceLocation.fromNamespaceAndPath(PaleLullaby.MOD_ID, "block/blood_fluid_flow");
        this.overlayTexture = ResourceLocation.fromNamespaceAndPath(PaleLullaby.MOD_ID, "block/blood_fluid_overlay");
        // 半透明：保留纹理本身的暗红色，只把透明度压低，像水一样能看穿浅层
        this.tintColor = 0xAAFFFFFF;
    }

    @Override
    public void initializeClient(java.util.function.Consumer<IClientFluidTypeExtensions> consumer) {
        consumer.accept(new IClientFluidTypeExtensions() {
            @Override
            public ResourceLocation getStillTexture() {
                return BloodFluidType.this.stillTexture;
            }

            @Override
            public ResourceLocation getFlowingTexture() {
                return BloodFluidType.this.flowingTexture;
            }

            @Override
            public ResourceLocation getOverlayTexture() {
                return BloodFluidType.this.overlayTexture;
            }

            @Override
            public int getTintColor() {
                return BloodFluidType.this.tintColor;
            }

            // 水面（流体侧壁）遮罩纹理
            @Nullable
            @Override
            public ResourceLocation getRenderOverlayTexture(Minecraft mc) {
                return ResourceLocation.withDefaultNamespace("textures/misc/underwater");
            }

            // 水下颜色：暗红色血雾
            @Override
            public Vector3f modifyFogColor(Camera camera, float partialTick, ClientLevel level, int renderDistance,
                                           float darkenWorldAmount, Vector3f fluidFogColor) {
                return new Vector3f(0.30F, 0.02F, 0.02F);
            }

            // 水雾：像泡在浓血里，能见度很低
            @Override
            public void modifyFogRender(Camera camera, FogRenderer.FogMode mode, float renderDistance, float partialTick,
                                        float nearDistance, float farDistance, FogShape shape) {
                RenderSystem.setShaderFogStart(0.0F);
                RenderSystem.setShaderFogEnd(Math.min(farDistance, 24.0F));
                RenderSystem.setShaderFogShape(FogShape.SPHERE);
            }
        });
    }
}
