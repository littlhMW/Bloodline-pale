package com.littlh.bloodline;

import net.minecraft.core.BlockPos;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.DoublePlantBlock;
import net.minecraft.world.level.block.SoundType;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.DoubleBlockHalf;
import net.minecraft.world.level.material.MapColor;
import net.minecraft.world.level.material.PushReaction;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.Shapes;
import net.minecraft.world.phys.shapes.VoxelShape;

public class PaleWheatBlock extends DoublePlantBlock {

    // 定义下半部分的形状，约等于一格高
    private static final VoxelShape LOWER_SHAPE = Block.box(2.0, 0.0, 2.0, 14.0, 16.0, 14.0);

    public PaleWheatBlock() {
        super(BlockBehaviour.Properties.of()
                .mapColor(MapColor.COLOR_GRAY)
                .replaceable()
                .noCollission()
                .instabreak()
                .sound(SoundType.GRASS)
                .offsetType(BlockBehaviour.OffsetType.XZ)
                .pushReaction(PushReaction.DESTROY)
                .ignitedByLava()
                .noOcclusion()
        );
    }

    @Override
    public VoxelShape getShape(BlockState state, BlockGetter level, BlockPos pos, CollisionContext context) {
        // 只有下半部分才有形状，用于显示轮廓
        return state.getValue(HALF) == DoubleBlockHalf.LOWER ? LOWER_SHAPE : Shapes.empty();
    }

    @Override
    public VoxelShape getInteractionShape(BlockState state, BlockGetter level, BlockPos pos) {
        // 只有下半部分才能被选中，与 getShape 保持一致
        return state.getValue(HALF) == DoubleBlockHalf.LOWER ? LOWER_SHAPE : Shapes.empty();
    }

    @Override
    public VoxelShape getCollisionShape(BlockState state, BlockGetter level, BlockPos pos, CollisionContext context) {
        // 返回空形状，使实体（包括玩家）可以穿过它
        return Shapes.empty();
    }

    @Override
    public boolean canSurvive(BlockState state, LevelReader level, BlockPos pos) {
        // 只有下半部分才检查生存条件
        if (state.getValue(HALF) != DoubleBlockHalf.LOWER) {
            return true;
        }
        BlockPos groundPos = pos.below();
        BlockState groundState = level.getBlockState(groundPos);
        // 只能放置在苍白草方块上
        return groundState.is(BloodlineBlocks.PALE_GRASS_BLOCK.get());
    }

    @Override
    public boolean mayPlaceOn(BlockState groundState, BlockGetter level, BlockPos pos) {
        // 放置时也检查脚下是否是苍白草方块
        return groundState.is(BloodlineBlocks.PALE_GRASS_BLOCK.get());
    }
}