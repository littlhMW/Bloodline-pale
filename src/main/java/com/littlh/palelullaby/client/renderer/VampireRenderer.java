package com.littlh.palelullaby.client.renderer;

import com.littlh.palelullaby.entity.VampireEntity;
import net.minecraft.client.model.geom.ModelLayers;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.resources.ResourceLocation;

public class VampireRenderer extends CastingVampireRenderer<VampireEntity> {

    public VampireRenderer(EntityRendererProvider.Context context) {
        super(context, new CastingVampireModel<>(context.bakeLayer(ModelLayers.PLAYER)));
    }

    @Override
    public ResourceLocation getTextureLocation(VampireEntity entity) {
        return MobSkins.vampireSkin(entity, 1);
    }
}
