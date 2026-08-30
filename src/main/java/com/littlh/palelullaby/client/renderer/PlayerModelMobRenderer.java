package com.littlh.palelullaby.client.renderer;

import net.minecraft.client.model.HumanoidModel;
import net.minecraft.client.model.PlayerModel;
import net.minecraft.client.model.geom.ModelLayers;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.entity.MobRenderer;
import net.minecraft.client.renderer.entity.layers.HumanoidArmorLayer;
import net.minecraft.client.renderer.entity.layers.ItemInHandLayer;
import net.minecraft.world.entity.Mob;

/**
 * 使用玩家模型渲染的实体基类，附带盔甲层。
 */
public abstract class PlayerModelMobRenderer<T extends Mob> extends MobRenderer<T, PlayerModel<T>> {
    public PlayerModelMobRenderer(EntityRendererProvider.Context context) {
        this(context, new PlayerModel<>(context.bakeLayer(ModelLayers.PLAYER), false));
    }

    protected PlayerModelMobRenderer(EntityRendererProvider.Context context, PlayerModel<T> model) {
        super(context, model, 0.5F);
        this.addLayer(new HumanoidArmorLayer<>(this,
                new HumanoidModel<>(context.bakeLayer(ModelLayers.PLAYER_INNER_ARMOR)),
                new HumanoidModel<>(context.bakeLayer(ModelLayers.PLAYER_OUTER_ARMOR)),
                context.getModelManager()));
        this.addLayer(new CrossbowUseItemInHandLayer<>(this, context.getItemInHandRenderer()));
    }
}
