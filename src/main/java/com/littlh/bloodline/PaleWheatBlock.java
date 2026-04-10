package com.littlh.bloodline;

import net.minecraft.core.BlockPos;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.level.block.Block;
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

    private static final VoxelShape LOWER_SHAPE = Block.box(2.0D, 0.0D, 2.0D, 14.0D, 16.0D, 14.0D);

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
    public VoxelShape getInteractionShape(BlockState state, BlockGetter level, BlockPos pos) {
        return state.getValue(HALF) == DoubleBlockHalf.LOWER ? LOWER_SHAPE : Shapes.empty();
    }

    @Override
    public VoxelShape getShape(BlockState state, BlockGetter level, BlockPos pos, CollisionContext context) {
        return state.getValue(HALF) == DoubleBlockHalf.LOWER ? LOWER_SHAPE : Shapes.empty();
    }

    @Override
    public VoxelShape getCollisionShape(BlockState state, BlockGetter level, BlockPos pos, CollisionContext context) {
        return Shapes.empty();
    }

    @Override
    public boolean canSurvive(BlockState state, LevelReader level, BlockPos pos) {
        if (state.getValue(HALF) != DoubleBlockHalf.LOWER) {
            return true;
        }
        BlockPos groundPos = pos.below();
        BlockState groundState = level.getBlockState(groundPos);
        return groundState.is(BloodlineBlocks.PALE_GRASS_BLOCK.get());
    }

    @Override
    public boolean mayPlaceOn(BlockState groundState, BlockGetter level, BlockPos pos) {
        return groundState.is(BloodlineBlocks.PALE_GRASS_BLOCK.get());
    }
}
