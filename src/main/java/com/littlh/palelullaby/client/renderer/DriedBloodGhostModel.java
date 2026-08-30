package com.littlh.palelullaby.client.renderer;

import com.littlh.palelullaby.PaleLullaby;
import com.littlh.palelullaby.entity.DriedBloodGhostEntity;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.resources.ResourceLocation;
import software.bernie.geckolib.model.GeoModel;

public class DriedBloodGhostModel extends GeoModel<DriedBloodGhostEntity> {
    private static final ResourceLocation MODEL =
            ResourceLocation.fromNamespaceAndPath(PaleLullaby.MOD_ID, "geo/dried_blood_ghost.geo.json");
    private static final ResourceLocation TEXTURE =
            ResourceLocation.fromNamespaceAndPath(PaleLullaby.MOD_ID, "textures/entity/dried_blood_ghost/dried_blood_ghost.png");
    private static final ResourceLocation ANIMATIONS =
            ResourceLocation.fromNamespaceAndPath(PaleLullaby.MOD_ID, "animations/ghoul_crawler.animation.json");

    @Override public ResourceLocation getModelResource(DriedBloodGhostEntity entity) { return MODEL; }
    @Override public ResourceLocation getTextureResource(DriedBloodGhostEntity entity) { return TEXTURE; }
    @Override public ResourceLocation getAnimationResource(DriedBloodGhostEntity entity) { return ANIMATIONS; }
}
