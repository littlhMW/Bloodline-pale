package com.littlh.palelullaby.client.renderer;

import com.littlh.palelullaby.PaleLullaby;
import com.littlh.palelullaby.entity.MullandEntity;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.resources.ResourceLocation;
import software.bernie.geckolib.model.GeoModel;

public class MullandModel extends GeoModel<MullandEntity> {
    private static final ResourceLocation MODEL = ResourceLocation.fromNamespaceAndPath(PaleLullaby.MOD_ID, "geo/mulland.geo.json");
    private static final ResourceLocation TEXTURE = ResourceLocation.fromNamespaceAndPath(PaleLullaby.MOD_ID, "textures/entity/mulland/mulland.png");
    private static final ResourceLocation ANIMATIONS = ResourceLocation.fromNamespaceAndPath(PaleLullaby.MOD_ID, "animations/mulland.animation.json");

    @Override public ResourceLocation getModelResource(MullandEntity entity) { return MODEL; }
    @Override public ResourceLocation getTextureResource(MullandEntity entity) { return TEXTURE; }
    @Override public ResourceLocation getAnimationResource(MullandEntity entity) { return ANIMATIONS; }
}
