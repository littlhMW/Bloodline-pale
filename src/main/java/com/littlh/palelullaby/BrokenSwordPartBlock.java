package com.littlh.palelullaby;

import com.mojang.serialization.MapCodec;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.HorizontalDirectionalBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.Shapes;
import net.minecraft.world.phys.shapes.VoxelShape;

/**
 * 断剑上方的两格碰撞部件：不可见、无掉落，只提供剑身的窄碰撞体。
 * 被破坏或底座消失时整把剑一起坍塌（底座掉落本体）。
 */
public class BrokenSwordPartBlock extends HorizontalDirectionalBlock {
    public static final MapCodec<BrokenSwordPartBlock> CODEC = simpleCodec(BrokenSwordPartBlock::new);

    private static final VoxelShape BLADE_SHAPE = Shapes.box(0.4375, 0.0, 0.4375, 0.5625, 1.0, 0.5625);
    private static boolean removing = false;

    public BrokenSwordPartBlock(Properties properties) {
        super(properties);
        this.registerDefaultState(this.stateDefinition.any().setValue(FACING, Direction.NORTH));
    }

    @Override
    protected MapCodec<? extends HorizontalDirectionalBlock> codec() {
        return CODEC;
    }

    @Override
    protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
        builder.add(FACING);
    }

    @Override
    public VoxelShape getShape(BlockState state, BlockGetter level, BlockPos pos, CollisionContext context) {
        return BLADE_SHAPE;
    }

    @Override
    public VoxelShape getCollisionShape(BlockState state, BlockGetter level, BlockPos pos, CollisionContext context) {
        return BLADE_SHAPE;
    }

    @Override
    protected VoxelShape getOcclusionShape(BlockState state, BlockGetter level, BlockPos pos) {
        return Shapes.empty();
    }

    @Override
    protected int getLightBlock(BlockState state, BlockGetter level, BlockPos pos) {
        return 0;
    }

    @Override
    protected void onRemove(BlockState state, Level level, BlockPos pos, BlockState newState, boolean movedByPiston) {
        if (!newState.is(state.getBlock()) && !removing) {
            removing = true;
            try {
                collapse(level, pos);
            } finally {
                removing = false;
            }
        }
        super.onRemove(state, level, pos, newState, movedByPiston);
    }

    @Override
    protected void neighborChanged(BlockState state, Level level, BlockPos pos, Block neighborBlock,
                                   BlockPos neighborPos, boolean movedByPiston) {
        if (!level.isClientSide && !removing) {
            BlockState below = level.getBlockState(pos.below());
            if (!below.is(PaleLullabyBlocks.BROKEN_SWORD.get())
                    && !below.is(PaleLullabyBlocks.BROKEN_SWORD_PART.get())) {
                removing = true;
                try {
                    collapse(level, pos);
                } finally {
                    removing = false;
                }
            }
        }
        super.neighborChanged(state, level, pos, neighborBlock, neighborPos, movedByPiston);
    }

    /** 向下找底座并带掉落破坏，让整把断剑一起坍塌。 */
    private static void collapse(Level level, BlockPos pos) {
        for (int i = 1; i <= 2; i++) {
            BlockPos p = pos.below(i);
            if (level.getBlockState(p).is(PaleLullabyBlocks.BROKEN_SWORD.get())) {
                level.destroyBlock(p, true);
                return;
            }
        }
    }
}
