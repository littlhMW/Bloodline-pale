package com.littlh.palelullaby.client.renderer;

import com.littlh.palelullaby.entity.DriedBloodGhostEntity;
import software.bernie.geckolib.renderer.GeoEntityRenderer;

public class DriedBloodGhostRenderer extends GeoEntityRenderer<DriedBloodGhostEntity> {
    public DriedBloodGhostRenderer(net.minecraft.client.renderer.entity.EntityRendererProvider.Context context) {
        super(context, new DriedBloodGhostModel());
        this.shadowRadius = 0.5F;
    }
}
