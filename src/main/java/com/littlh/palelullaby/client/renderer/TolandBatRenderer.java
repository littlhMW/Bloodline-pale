package com.littlh.palelullaby.client.renderer;

import com.littlh.palelullaby.PaleLullaby;
import com.littlh.palelullaby.client.model.TolandBatModel;
import com.littlh.palelullaby.entity.TolandBatEntity;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.entity.MobRenderer;
import net.minecraft.resources.ResourceLocation;

public class TolandBatRenderer extends MobRenderer<TolandBatEntity, TolandBatModel> {
    private static final ResourceLocation TEXTURE = ResourceLocation.fromNamespaceAndPath(PaleLullaby.MOD_ID, "textures/entity/toland_bat.png");

    public TolandBatRenderer(EntityRendererProvider.Context context) {
        super(context, new TolandBatModel(context.bakeLayer(TolandBatModel.LAYER_LOCATION)), 0.6F);
    }

    @Override
    public ResourceLocation getTextureLocation(TolandBatEntity entity) {
        return TEXTURE;
    }
}
