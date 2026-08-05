package com.littlh.palelullaby;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.RandomSource;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.SoundType;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.block.state.properties.DirectionProperty;
import net.minecraft.world.level.material.MapColor;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.Shapes;
import net.minecraft.world.phys.shapes.VoxelShape;

public class CrimsonThornSpikeBlock extends Block {
    public static final DirectionProperty FACING = BlockStateProperties.FACING;

    private static final VoxelShape SHAPE_DOWN = box(7.0, 10.0, 7.0, 9.0, 16.0, 9.0);
    private static final VoxelShape SHAPE_UP = box(7.0, 0.0, 7.0, 9.0, 6.0, 9.0);
    private static final VoxelShape SHAPE_NORTH = box(7.0, 7.0, 0.0, 9.0, 9.0, 6.0);
    private static final VoxelShape SHAPE_SOUTH = box(7.0, 7.0, 10.0, 9.0, 9.0, 16.0);
    private static final VoxelShape SHAPE_WEST = box(0.0, 7.0, 7.0, 6.0, 9.0, 9.0);
    private static final VoxelShape SHAPE_EAST = box(10.0, 7.0, 7.0, 16.0, 9.0, 9.0);
    private static final VoxelShape[] SHAPES = new VoxelShape[]{
            SHAPE_DOWN, SHAPE_UP, SHAPE_NORTH, SHAPE_SOUTH, SHAPE_WEST, SHAPE_EAST
    };

    public CrimsonThornSpikeBlock() {
        super(BlockBehaviour.Properties.of()
                .mapColor(MapColor.CRIMSON_STEM)
                .instabreak()
                .sound(SoundType.SWEET_BERRY_BUSH)
                .randomTicks()
                .noOcclusion()
        );
        this.registerDefaultState(this.stateDefinition.any().setValue(FACING, Direction.UP));
    }

    @Override
    protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
        builder.add(FACING);
    }

    @Override
    public BlockState getStateForPlacement(BlockPlaceContext context) {
        return this.defaultBlockState().setValue(FACING, context.getClickedFace());
    }

    @Override
    public VoxelShape getShape(BlockState state, BlockGetter level, BlockPos pos, CollisionContext context) {
        return SHAPES[state.getValue(FACING).get3DDataValue()];
    }

    @Override
    public VoxelShape getOcclusionShape(BlockState state, BlockGetter level, BlockPos pos) {
        return Shapes.empty();
    }

    public static boolean canAttach(BlockGetter level, BlockPos pos, Direction attachDirection) {
        BlockState behind = level.getBlockState(pos.relative(attachDirection));
        return behind.is(PaleLullabyBlocks.CRIMSON_THORN.get())
                || (behind.is(PaleLullabyBlocks.CRIMSON_THORN_SPIKE.get())
                    && behind.getValue(FACING) == attachDirection);
    }

    @Override
    protected boolean canSurvive(BlockState state, LevelReader level, BlockPos pos) {
        Direction facing = state.getValue(FACING);
        return canAttach(level, pos, facing.getOpposite());
    }

    @Override
    protected void neighborChanged(BlockState state, Level level, BlockPos pos, Block neighborBlock, BlockPos neighborPos, boolean moved) {
        if (!state.canSurvive(level, pos)) {
            level.destroyBlock(pos, true);
        }
    }

    @Override
    protected void randomTick(BlockState state, ServerLevel level, BlockPos pos, RandomSource random) {
        if (random.nextInt(8) != 0) return;
        Direction facing = state.getValue(FACING);
        BlockPos target = pos.relative(facing);
        if (level.isEmptyBlock(target) && canAttach(level, target, facing.getOpposite())) {
            level.setBlock(target, this.defaultBlockState().setValue(FACING, facing), 3);
        }
    }
}
