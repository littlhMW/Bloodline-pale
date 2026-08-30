package com.littlh.palelullaby.client.renderer;

import com.littlh.palelullaby.entity.BloodLordEntity;
import net.minecraft.client.model.geom.ModelLayers;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.resources.ResourceLocation;

public class BloodLordRenderer extends CastingVampireRenderer<BloodLordEntity> {

    public BloodLordRenderer(EntityRendererProvider.Context context) {
        super(context, new CastingVampireModel<>(context.bakeLayer(ModelLayers.PLAYER)));
    }

    @Override
    public ResourceLocation getTextureLocation(BloodLordEntity entity) {
        return MobSkins.vampireSkin(entity, 3);
    }
}
