package com.littlh.bloodline.client.renderer;

import com.littlh.bloodline.PaleLullaby;
import com.littlh.bloodline.entity.MullandEntity;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.resources.ResourceLocation;
import software.bernie.geckolib.renderer.GeoEntityRenderer;

public class MullandRenderer extends GeoEntityRenderer<MullandEntity> {
    private static final ResourceLocation TEXTURE = ResourceLocation.fromNamespaceAndPath(PaleLullaby.MOD_ID, "textures/entity/mulland/mulland.png");

    public MullandRenderer(EntityRendererProvider.Context context) {
        super(context, new MullandModel());
        this.shadowRadius = 1.5f;
    }

    @Override public ResourceLocation getTextureLocation(MullandEntity entity) { return TEXTURE; }
}
