package com.littlh.palelullaby.client.renderer;

import com.littlh.palelullaby.entity.BloodNobleEntity;
import net.minecraft.client.model.geom.ModelLayers;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.resources.ResourceLocation;

public class BloodNobleRenderer extends CastingVampireRenderer<BloodNobleEntity> {

    public BloodNobleRenderer(EntityRendererProvider.Context context) {
        super(context, new CastingVampireModel<>(context.bakeLayer(ModelLayers.PLAYER)));
    }

    @Override
    public ResourceLocation getTextureLocation(BloodNobleEntity entity) {
        return MobSkins.vampireSkin(entity, 2);
    }
}
