package com.littlh.palelullaby.client.renderer;

import com.littlh.palelullaby.IronMaidenBlockEntity;
import com.littlh.palelullaby.PaleLullaby;
import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.phys.AABB;
import software.bernie.geckolib.model.DefaultedBlockGeoModel;
import software.bernie.geckolib.renderer.GeoBlockRenderer;

public class IronMaidenBlockEntityRenderer extends GeoBlockRenderer<IronMaidenBlockEntity> {
    public IronMaidenBlockEntityRenderer(BlockEntityRendererProvider.Context context) {
        super(new DefaultedBlockGeoModel<>(ResourceLocation.fromNamespaceAndPath(PaleLullaby.MOD_ID, "iron_maiden")));
    }

    @Override
    public AABB getRenderBoundingBox(IronMaidenBlockEntity blockEntity) {
        // 模型有 4 格高，扩展渲染包围盒，避免抬头时下方方块离开视锥导致整体消失
        net.minecraft.core.BlockPos pos = blockEntity.getBlockPos();
        return new AABB(pos.getX(), pos.getY(), pos.getZ(), pos.getX() + 1, pos.getY() + 4, pos.getZ() + 1);
    }

    @Override
    public boolean shouldRenderOffScreen(IronMaidenBlockEntity blockEntity) {
        return true;
    }
}
