package com.littlh.palelullaby;

import java.util.function.Consumer;
import net.minecraft.client.renderer.BlockEntityWithoutLevelRenderer;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.block.Block;
import software.bernie.geckolib.animatable.GeoItem;
import software.bernie.geckolib.animation.AnimatableManager;
import software.bernie.geckolib.animatable.client.GeoRenderProvider;
import software.bernie.geckolib.animatable.instance.AnimatableInstanceCache;
import software.bernie.geckolib.model.DefaultedBlockGeoModel;
import software.bernie.geckolib.renderer.GeoItemRenderer;
import software.bernie.geckolib.util.GeckoLibUtil;

/**
 * 铁处女物品：复用方块本身的 GeckoLib geo 模型渲染，物品栏和手持时直接显示 3D 方块。
 */
public class IronMaidenItem extends BlockItem implements GeoItem {
    private final AnimatableInstanceCache cache = GeckoLibUtil.createInstanceCache(this);

    public IronMaidenItem(Block block, Item.Properties properties) {
        super(block, properties);
    }

    @Override
    public AnimatableInstanceCache getAnimatableInstanceCache() {
        return cache;
    }

    @Override
    public void registerControllers(AnimatableManager.ControllerRegistrar controllers) {
        // 静态模型，无需动画控制器
    }

    @Override
    public void createGeoRenderer(Consumer<GeoRenderProvider> consumer) {
        consumer.accept(new GeoRenderProvider() {
            private GeoItemRenderer<IronMaidenItem> renderer;

            @Override
            public BlockEntityWithoutLevelRenderer getGeoItemRenderer() {
                if (this.renderer == null) {
                    // 复用方块模型与贴图（geo/block/iron_maiden.geo.json + textures/block/iron_maiden.png）
                    this.renderer = new GeoItemRenderer<IronMaidenItem>(new DefaultedBlockGeoModel<IronMaidenItem>(
                            ResourceLocation.fromNamespaceAndPath(PaleLullaby.MOD_ID, "iron_maiden")))
                            .withScale(0.25f, 0.25f);
                }
                return this.renderer;
            }
        });
    }
}