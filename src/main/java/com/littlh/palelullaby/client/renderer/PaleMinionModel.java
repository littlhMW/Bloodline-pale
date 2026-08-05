package com.littlh.palelullaby.client.renderer;

import com.littlh.palelullaby.PaleLullaby;
import com.littlh.palelullaby.entity.minion.PaleMinionEntity;
import net.minecraft.resources.ResourceLocation;
import software.bernie.geckolib.model.GeoModel;

public class PaleMinionModel extends GeoModel<PaleMinionEntity> {
    private static final ResourceLocation MODEL = ResourceLocation.fromNamespaceAndPath(PaleLullaby.MOD_ID, "geo/pale_minion.geo.json");
    private static final ResourceLocation TEXTURE = ResourceLocation.fromNamespaceAndPath(PaleLullaby.MOD_ID, "textures/entity/pale_minion/pale_minion.png");
    private static final ResourceLocation ANIMATIONS = ResourceLocation.fromNamespaceAndPath(PaleLullaby.MOD_ID, "animations/pale_minion.animation.json");

    @Override
    public ResourceLocation getModelResource(PaleMinionEntity entity) {
        return MODEL;
    }

    @Override
    public ResourceLocation getTextureResource(PaleMinionEntity entity) {
        return TEXTURE;
    }

    @Override
    public ResourceLocation getAnimationResource(PaleMinionEntity entity) {
        return ANIMATIONS;
    }
}
