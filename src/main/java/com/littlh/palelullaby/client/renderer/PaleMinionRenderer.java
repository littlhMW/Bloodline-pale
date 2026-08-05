package com.littlh.palelullaby.client.renderer;

import com.littlh.palelullaby.PaleLullaby;
import com.littlh.palelullaby.entity.minion.PaleMinionEntity;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.resources.ResourceLocation;
import software.bernie.geckolib.renderer.GeoEntityRenderer;

public class PaleMinionRenderer extends GeoEntityRenderer<PaleMinionEntity> {
    private static final ResourceLocation TEXTURE = ResourceLocation.fromNamespaceAndPath(PaleLullaby.MOD_ID, "textures/entity/pale_minion/pale_minion.png");

    public PaleMinionRenderer(EntityRendererProvider.Context context) {
        super(context, new PaleMinionModel());
        this.shadowRadius = 0.5f;
    }

    @Override
    public ResourceLocation getTextureLocation(PaleMinionEntity entity) {
        return TEXTURE;
    }
}
