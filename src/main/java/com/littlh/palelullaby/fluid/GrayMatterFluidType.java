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

public class GrayMatterFluidType extends FluidType {
    public GrayMatterFluidType() {
        super(Properties.create()
            .descriptionId("block.pale_lullaby.gray_matter")
            .density(2000)
            .viscosity(2000)
            .temperature(300)
            .fallDistanceModifier(0.0F)
            .canExtinguish(false)
            .canConvertToSource(false)
        );
    }

    @Override
    public void initializeClient(java.util.function.Consumer<IClientFluidTypeExtensions> consumer) {
        consumer.accept(new IClientFluidTypeExtensions() {
            @Override
            public ResourceLocation getStillTexture() {
                return ResourceLocation.fromNamespaceAndPath(PaleLullaby.MOD_ID, "block/gray_matter_still");
            }

            @Override
            public ResourceLocation getFlowingTexture() {
                return ResourceLocation.fromNamespaceAndPath(PaleLullaby.MOD_ID, "block/gray_matter_flow");
            }

            @Nullable
            @Override
            public ResourceLocation getOverlayTexture() {
                return null;
            }

            @Override
            public int getTintColor() {
                // 半透明：保留纹理的灰白色，压低透明度
                return 0x99FFFFFF;
            }

            // 水面遮罩纹理
            @Nullable
            @Override
            public ResourceLocation getRenderOverlayTexture(Minecraft mc) {
                return ResourceLocation.withDefaultNamespace("textures/misc/underwater");
            }

            // 水下颜色：灰质浓雾的暗灰色
            @Override
            public Vector3f modifyFogColor(Camera camera, float partialTick, ClientLevel level, int renderDistance,
                                           float darkenWorldAmount, Vector3f fluidFogColor) {
                return new Vector3f(0.13F, 0.13F, 0.17F);
            }

            // 水雾：灰质更浓稠，能见度比血液还低
            @Override
            public void modifyFogRender(Camera camera, FogRenderer.FogMode mode, float renderDistance, float partialTick,
                                        float nearDistance, float farDistance, FogShape shape) {
                RenderSystem.setShaderFogStart(0.0F);
                RenderSystem.setShaderFogEnd(Math.min(farDistance, 18.0F));
                RenderSystem.setShaderFogShape(FogShape.SPHERE);
            }
        });
    }
}
