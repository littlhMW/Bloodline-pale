package com.littlh.palelullaby.client.renderer;

import com.littlh.palelullaby.entity.NobleVampireEntity;
import net.minecraft.client.model.geom.ModelLayers;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.resources.ResourceLocation;

public class NobleVampireRenderer extends CastingVampireRenderer<NobleVampireEntity> {

    public NobleVampireRenderer(EntityRendererProvider.Context context) {
        super(context, new CastingVampireModel<>(context.bakeLayer(ModelLayers.PLAYER)));
    }

    @Override
    public ResourceLocation getTextureLocation(NobleVampireEntity entity) {
        return MobSkins.vampireSkin(entity, 2);
    }
}
