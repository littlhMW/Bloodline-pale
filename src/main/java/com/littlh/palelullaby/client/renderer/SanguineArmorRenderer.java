package com.littlh.palelullaby.client.renderer;

import com.littlh.palelullaby.PaleLullaby;
import com.littlh.palelullaby.SanguineArmorItem;

import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.EquipmentSlot;

import software.bernie.geckolib.model.DefaultedItemGeoModel;
import software.bernie.geckolib.renderer.GeoArmorRenderer;

/**
 * 血族套装盔甲渲染器：裙摆（armorWaist）挂在 armorBody 下跟随身体动画，但只在胸甲槽位显示。
 */
public class SanguineArmorRenderer extends GeoArmorRenderer<SanguineArmorItem> {
    public SanguineArmorRenderer() {
        super(new DefaultedItemGeoModel<>(ResourceLocation.fromNamespaceAndPath(PaleLullaby.MOD_ID, "sanguine_armor")));
    }

    @Override
    protected void applyBoneVisibilityBySlot(EquipmentSlot currentSlot) {
        super.applyBoneVisibilityBySlot(currentSlot);
        // armorWaist 不是 GeckoLib 的标准盔甲骨，默认不会被槽位隐藏，需显式控制
        getGeoModel().getBone("armorWaist").ifPresent(bone -> setBoneVisible(bone, currentSlot == EquipmentSlot.CHEST));
    }
}
