package com.littlh.palelullaby.client.renderer;

import com.littlh.palelullaby.entity.FallenBloodHunterEntity;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.resources.ResourceLocation;

public class FallenBloodHunterRenderer extends PlayerModelMobRenderer<FallenBloodHunterEntity> {

    public FallenBloodHunterRenderer(EntityRendererProvider.Context context) {
        super(context);
    }

    @Override
    public ResourceLocation getTextureLocation(FallenBloodHunterEntity entity) {
        return MobSkins.fallenHunterSkin(entity);
    }
}
