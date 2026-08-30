package com.littlh.palelullaby.client.renderer;

import com.littlh.palelullaby.PaleLullaby;
import com.littlh.palelullaby.BattlemageArmorItem;

import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.EquipmentSlot;

import software.bernie.geckolib.model.DefaultedItemGeoModel;
import software.bernie.geckolib.renderer.GeoArmorRenderer;

/**
 * 猎人衣服（battlemage 模型）：裙摆 armorWaist 挂在 bipedWaist 下，只在胸甲槽位显示。
 */
public class BattlemageArmorRenderer extends GeoArmorRenderer<BattlemageArmorItem> {
    public BattlemageArmorRenderer() {
        super(new DefaultedItemGeoModel<>(ResourceLocation.fromNamespaceAndPath(PaleLullaby.MOD_ID, "battlemage_armor")));
    }

    @Override
    protected void applyBoneVisibilityBySlot(EquipmentSlot currentSlot) {
        super.applyBoneVisibilityBySlot(currentSlot);
        getGeoModel().getBone("armorWaist").ifPresent(bone -> setBoneVisible(bone, currentSlot == EquipmentSlot.CHEST));
    }
}
