package com.littlh.bloodline.client.renderer;

import com.littlh.bloodline.Bloodline;
import com.littlh.bloodline.entity.KuxuezheEntity;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.resources.ResourceLocation;
import software.bernie.geckolib.renderer.GeoEntityRenderer;

public class KuxuezheRenderer extends GeoEntityRenderer<KuxuezheEntity> {
    private static final ResourceLocation TEXTURE = ResourceLocation.fromNamespaceAndPath(Bloodline.MOD_ID, "textures/entity/kuxuezhe/kuxuezhe.png");

    public KuxuezheRenderer(EntityRendererProvider.Context context) {
        super(context, new KuxuezheModel());
        this.shadowRadius = 1.5f;
    }

    @Override public ResourceLocation getTextureLocation(KuxuezheEntity entity) { return TEXTURE; }
}
