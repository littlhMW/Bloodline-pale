package com.littlh.palelullaby.fluid;

import net.minecraft.resources.ResourceLocation;
import net.neoforged.neoforge.client.extensions.common.IClientFluidTypeExtensions;
import net.neoforged.neoforge.fluids.FluidType;

import com.littlh.palelullaby.PaleLullaby;

import javax.annotation.Nullable;

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
                return 0xFFFFFFFF; // 纯白，无色调
            }
        });
    }
}
