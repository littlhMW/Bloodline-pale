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
import net.minecraft.world.level.block.state.properties.BooleanProperty;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.Shapes;
import net.minecraft.world.phys.shapes.VoxelShape;

import java.util.EnumMap;
import java.util.Map;

/**
 * 铁处女的多方块碰撞部件：本体中部两格为三面空心墙（背面+左右），顶上一格为装饰碰撞体。
 * 不可见、无掉落；被破坏或底座消失时整个结构塌落（底座掉落本体物品）。
 */
public class IronMaidenPartBlock extends HorizontalDirectionalBlock {
    public static final MapCodec<IronMaidenPartBlock> CODEC = simpleCodec(IronMaidenPartBlock::new);
    public static final BooleanProperty TOP = BooleanProperty.create("top");

    private static final VoxelShape BODY_SHAPE = Shapes.or(
            Shapes.box(0.0, 0.0, 0.875, 1.0, 1.0, 1.0),      // 后墙
            Shapes.box(0.0, 0.0, 0.0, 0.125, 1.0, 1.0),       // 左墙
            Shapes.box(0.875, 0.0, 0.0, 1.0, 1.0, 1.0)        // 右墙
    );
    private static final VoxelShape TOP_SHAPE = Shapes.block();

    private static final Map<Direction, VoxelShape> BODY_BY_FACING = buildBodyShapes();
    private static boolean removing = false;

    public IronMaidenPartBlock(Properties properties) {
        super(properties);
        this.registerDefaultState(this.stateDefinition.any().setValue(FACING, Direction.NORTH).setValue(TOP, false));
    }

    @Override
    protected MapCodec<? extends HorizontalDirectionalBlock> codec() {
        return CODEC;
    }

    @Override
    protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
        builder.add(FACING, TOP);
    }

    @Override
    public VoxelShape getShape(BlockState state, BlockGetter level, BlockPos pos, CollisionContext context) {
        return this.getCollisionShape(state, level, pos, context);
    }

    @Override
    public VoxelShape getCollisionShape(BlockState state, BlockGetter level, BlockPos pos, CollisionContext context) {
        if (state.getValue(TOP)) {
            return TOP_SHAPE;
        }
        return BODY_BY_FACING.getOrDefault(state.getValue(FACING), BODY_SHAPE);
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
            if (!below.is(PaleLullabyBlocks.IRON_MAIDEN.get())
                    && !below.is(PaleLullabyBlocks.IRON_MAIDEN_PART.get())) {
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

    /** 向下找底座并带掉落破坏，让整个铁处女一起塌落。 */
    private static void collapse(Level level, BlockPos pos) {
        for (int i = 1; i <= 3; i++) {
            BlockPos p = pos.below(i);
            if (level.getBlockState(p).is(PaleLullabyBlocks.IRON_MAIDEN.get())) {
                level.destroyBlock(p, true);
                return;
            }
        }
    }

    private static Map<Direction, VoxelShape> buildBodyShapes() {
        EnumMap<Direction, VoxelShape> map = new EnumMap<>(Direction.class);
        map.put(Direction.NORTH, BODY_SHAPE);
        map.put(Direction.EAST, rotateY(BODY_SHAPE, 1));
        map.put(Direction.SOUTH, rotateY(BODY_SHAPE, 2));
        map.put(Direction.WEST, rotateY(BODY_SHAPE, 3));
        return map;
    }

    private static VoxelShape rotateY(VoxelShape shape, int quarters) {
        java.util.List<net.minecraft.world.phys.AABB> boxes = shape.toAabbs();
        VoxelShape result = Shapes.empty();
        for (net.minecraft.world.phys.AABB box : boxes) {
            double x1 = box.minX * 16.0, z1 = box.minZ * 16.0;
            double x2 = box.maxX * 16.0, z2 = box.maxZ * 16.0;
            for (int q = 0; q < quarters; q++) {
                double nx1 = 16.0 - z2, nz1 = x1;
                double nx2 = 16.0 - z1, nz2 = x2;
                x1 = nx1; z1 = nz1; x2 = nx2; z2 = nz2;
            }
            result = Shapes.or(result, Shapes.box(x1 / 16.0, box.minY, z1 / 16.0, x2 / 16.0, box.maxY, z2 / 16.0));
        }
        return result;
    }
}
