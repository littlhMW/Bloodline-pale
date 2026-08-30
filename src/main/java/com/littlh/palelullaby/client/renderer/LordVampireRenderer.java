package com.littlh.palelullaby.client.renderer;

import com.littlh.palelullaby.entity.LordVampireEntity;
import net.minecraft.client.model.geom.ModelLayers;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.resources.ResourceLocation;

public class LordVampireRenderer extends CastingVampireRenderer<LordVampireEntity> {

    public LordVampireRenderer(EntityRendererProvider.Context context) {
        super(context, new CastingVampireModel<>(context.bakeLayer(ModelLayers.PLAYER)));
    }

    @Override
    public ResourceLocation getTextureLocation(LordVampireEntity entity) {
        return MobSkins.vampireSkin(entity, 3);
    }
}
