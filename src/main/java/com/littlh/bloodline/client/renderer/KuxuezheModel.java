package com.littlh.bloodline.client.renderer;

import com.littlh.bloodline.Bloodline;
import com.littlh.bloodline.entity.KuxuezheEntity;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.resources.ResourceLocation;
import software.bernie.geckolib.model.GeoModel;

public class KuxuezheModel extends GeoModel<KuxuezheEntity> {
    private static final ResourceLocation MODEL = ResourceLocation.fromNamespaceAndPath(Bloodline.MOD_ID, "geo/kuxuezhe.geo.json");
    private static final ResourceLocation TEXTURE = ResourceLocation.fromNamespaceAndPath(Bloodline.MOD_ID, "textures/entity/kuxuezhe/kuxuezhe.png");
    private static final ResourceLocation ANIMATIONS = ResourceLocation.fromNamespaceAndPath(Bloodline.MOD_ID, "animations/kuxuezhe.animation.json");

    @Override public ResourceLocation getModelResource(KuxuezheEntity entity) { return MODEL; }
    @Override public ResourceLocation getTextureResource(KuxuezheEntity entity) { return TEXTURE; }
    @Override public ResourceLocation getAnimationResource(KuxuezheEntity entity) { return ANIMATIONS; }
}
