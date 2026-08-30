package com.littlh.palelullaby;

import com.mojang.serialization.MapCodec;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.EntityBlock;
import net.minecraft.world.level.block.HorizontalDirectionalBlock;
import net.minecraft.world.level.block.RenderShape;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.Shapes;
import net.minecraft.world.phys.shapes.VoxelShape;

import java.util.EnumMap;
import java.util.List;
import java.util.Map;

/**
 * 铁处女：像竖放的盒子，只有底座和四周四壁有碰撞体积，正面（门）敞开可进入。
 * 站在内部底座上会持续少量掉血，每隔一定次数有概率掉落「伤痕印记」。
 */
public class IronMaidenBlock extends HorizontalDirectionalBlock implements EntityBlock {
    public static final MapCodec<IronMaidenBlock> CODEC = simpleCodec(IronMaidenBlock::new);

    private static final VoxelShape BASE_SHAPE = Shapes.or(
            Shapes.box(0.0, 0.0, 0.0, 1.0, 0.125, 1.0),      // 底座
            Shapes.box(0.0, 0.125, 0.875, 1.0, 2.375, 1.0),  // 后壁
            Shapes.box(0.0, 0.125, 0.0, 0.125, 2.4375, 1.0), // 左壁
            Shapes.box(0.875, 0.125, 0.0, 1.0, 2.4375, 1.0)  // 右壁
    );

    private static final Map<Direction, VoxelShape> SHAPES_BY_FACING = buildShapes();

    public IronMaidenBlock(Properties properties) {
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
    public BlockState getStateForPlacement(BlockPlaceContext context) {
        // 铁处女模型有 4 格高（3 格本体 + 1 格顶饰），上方必须空出 3 格才能放置
        Level level = context.getLevel();
        BlockPos pos = context.getClickedPos();
        for (int i = 1; i <= 3; i++) {
            if (!level.getBlockState(pos.above(i)).canBeReplaced(context)) {
                return null;
            }
        }
        // 让铁处女的门（模型正面 -Z）朝向放置者
        return this.defaultBlockState().setValue(FACING, context.getHorizontalDirection().getOpposite());
    }

    @Override
    protected void onPlace(BlockState state, Level level, BlockPos pos, BlockState oldState, boolean movedByPiston) {
        if (!level.isClientSide) {
            Direction facing = state.getValue(FACING);
            for (int i = 1; i <= 3; i++) {
                BlockPos partPos = pos.above(i);
                BlockState current = level.getBlockState(partPos);
                if (!current.canBeReplaced()) {
                    continue;
                }
                level.setBlock(partPos, PaleLullabyBlocks.IRON_MAIDEN_PART.get().defaultBlockState()
                        .setValue(IronMaidenPartBlock.FACING, facing)
                        .setValue(IronMaidenPartBlock.TOP, i == 3), 3);
            }
        }
    }

    @Override
    protected void onRemove(BlockState state, Level level, BlockPos pos, BlockState newState, boolean movedByPiston) {
        if (!newState.is(state.getBlock())) {
            for (int i = 1; i <= 3; i++) {
                BlockPos partPos = pos.above(i);
                if (level.getBlockState(partPos).is(PaleLullabyBlocks.IRON_MAIDEN_PART.get())) {
                    level.removeBlock(partPos, false);
                }
            }
        }
        super.onRemove(state, level, pos, newState, movedByPiston);
    }

    @Override
    protected RenderShape getRenderShape(BlockState state) {
        return RenderShape.ENTITYBLOCK_ANIMATED;
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
    public VoxelShape getCollisionShape(BlockState state, BlockGetter level, BlockPos pos, CollisionContext context) {
        return SHAPES_BY_FACING.getOrDefault(state.getValue(FACING), BASE_SHAPE);
    }

    @Override
    public VoxelShape getShape(BlockState state, BlockGetter level, BlockPos pos, CollisionContext context) {
        return this.getCollisionShape(state, level, pos, context);
    }

    @Override
    public BlockEntity newBlockEntity(BlockPos pos, BlockState state) {
        return new IronMaidenBlockEntity(pos, state);
    }

    @Override
    protected void entityInside(BlockState state, Level level, BlockPos pos, Entity entity) {
        if (level.isClientSide) {
            return;
        }
        if (!(entity instanceof LivingEntity living) || !living.isAlive()) {
            return;
        }
        // 只有站在内部底座上才掉血：水平位于四面墙内、高度位于底座之上
        double dx = entity.getX() - pos.getX();
        double dz = entity.getZ() - pos.getZ();
        if (dx < 0.125 || dx > 0.875 || dz < 0.125 || dz > 0.875) {
            return;
        }
        double feetY = entity.getBoundingBox().minY - pos.getY();
        if (feetY < 0.125 || feetY > 2.375) {
            return;
        }
        if (level.getGameTime() % 10 != 0) {
            return;
        }
        if (living.hurt(level.damageSources().magic(), 1.0F)
                && level.getBlockEntity(pos) instanceof IronMaidenBlockEntity be) {
            be.onTrapDamage(living instanceof Player);
        }
    }

    private static Map<Direction, VoxelShape> buildShapes() {
        EnumMap<Direction, VoxelShape> map = new EnumMap<>(Direction.class);
        // 门（模型正面 -Z）在 GeoBlockRenderer 中映射到 FACING 方向，碰撞开口必须与之一致
        map.put(Direction.NORTH, BASE_SHAPE);
        map.put(Direction.EAST, rotateY(BASE_SHAPE, 1));
        map.put(Direction.SOUTH, rotateY(BASE_SHAPE, 2));
        map.put(Direction.WEST, rotateY(BASE_SHAPE, 3));
        return map;
    }

    /** 与 GeoBlockRenderer 相同的 Y 轴旋转（每 90° 一格）。 */
    private static VoxelShape rotateY(VoxelShape shape, int quarters) {
        List<AABB> boxes = shape.toAabbs();
        VoxelShape result = Shapes.empty();
        for (AABB box : boxes) {
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
