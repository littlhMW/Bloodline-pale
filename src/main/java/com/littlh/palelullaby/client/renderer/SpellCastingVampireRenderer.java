package com.littlh.palelullaby.client.renderer;

import com.littlh.palelullaby.entity.SpellCastingVampireEntity;
import net.minecraft.client.model.geom.ModelLayers;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.resources.ResourceLocation;

public class SpellCastingVampireRenderer extends CastingVampireRenderer<SpellCastingVampireEntity> {

    public SpellCastingVampireRenderer(EntityRendererProvider.Context context) {
        super(context, new CastingVampireModel<>(context.bakeLayer(ModelLayers.PLAYER)));
    }

    @Override
    public ResourceLocation getTextureLocation(SpellCastingVampireEntity entity) {
        return MobSkins.vampireSkin(entity, 1);
    }
}
