package com.littlh.palelullaby.fluid;

import com.littlh.palelullaby.PaleLullaby;
import net.minecraft.resources.ResourceLocation;
import net.neoforged.neoforge.client.extensions.common.IClientFluidTypeExtensions;
import net.neoforged.neoforge.fluids.FluidType;

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
        this.tintColor = 0xFFAA0000;
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
        });
    }
}
