package com.littlh.palelullaby.client.renderer;

import com.littlh.palelullaby.BrokenSwordBlockEntity;
import com.littlh.palelullaby.PaleLullaby;
import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider;
import net.minecraft.core.BlockPos;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.phys.AABB;
import software.bernie.geckolib.model.DefaultedBlockGeoModel;
import software.bernie.geckolib.renderer.GeoBlockRenderer;

public class BrokenSwordBlockEntityRenderer extends GeoBlockRenderer<BrokenSwordBlockEntity> {
    public BrokenSwordBlockEntityRenderer(BlockEntityRendererProvider.Context context) {
        super(new DefaultedBlockGeoModel<>(ResourceLocation.fromNamespaceAndPath(PaleLullaby.MOD_ID, "broken_sword")));
    }

    @Override
    public AABB getRenderBoundingBox(BrokenSwordBlockEntity blockEntity) {
        // 模型约 3 格高，扩展渲染包围盒避免抬头/远处时整把剑消失
        BlockPos pos = blockEntity.getBlockPos();
        return new AABB(pos.getX(), pos.getY(), pos.getZ(), pos.getX() + 1, pos.getY() + 3, pos.getZ() + 1);
    }

    @Override
    public boolean shouldRenderOffScreen(BrokenSwordBlockEntity blockEntity) {
        return true;
    }
}
