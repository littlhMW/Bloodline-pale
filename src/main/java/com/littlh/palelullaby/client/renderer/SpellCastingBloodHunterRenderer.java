package com.littlh.palelullaby.client.renderer;

import com.littlh.palelullaby.entity.SpellCastingBloodHunterEntity;
import net.minecraft.client.model.geom.ModelLayers;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.resources.ResourceLocation;

public class SpellCastingBloodHunterRenderer extends CastingVampireRenderer<SpellCastingBloodHunterEntity> {

    public SpellCastingBloodHunterRenderer(EntityRendererProvider.Context context) {
        super(context, new BloodHunterModel<>(context.bakeLayer(ModelLayers.PLAYER)));
    }

    @Override
    public ResourceLocation getTextureLocation(SpellCastingBloodHunterEntity entity) {
        return MobSkins.hunterSkin(entity, entity.hunterRank());
    }
}
