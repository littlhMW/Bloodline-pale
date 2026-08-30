package com.littlh.palelullaby;

import com.mojang.serialization.MapCodec;
import javax.annotation.Nullable;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.RandomSource;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.item.FallingBlockEntity;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.Fallable;
import net.minecraft.world.level.block.SimpleWaterloggedBlock;
import net.minecraft.world.level.block.SoundType;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.block.state.properties.BooleanProperty;
import net.minecraft.world.level.block.state.properties.DirectionProperty;
import net.minecraft.world.level.block.state.properties.DripstoneThickness;
import net.minecraft.world.level.block.state.properties.EnumProperty;
import net.minecraft.world.level.material.FluidState;
import net.minecraft.world.level.material.Fluids;
import net.minecraft.world.level.material.MapColor;
import net.minecraft.world.level.material.PushReaction;
import net.minecraft.world.level.pathfinder.PathComputationType;
import net.minecraft.world.phys.Vec3;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.Shapes;
import net.minecraft.world.phys.shapes.VoxelShape;

public class IceSpikeBlock extends Block implements Fallable, SimpleWaterloggedBlock {
    public static final MapCodec<IceSpikeBlock> CODEC = simpleCodec(IceSpikeBlock::new);
    public static final DirectionProperty TIP_DIRECTION = BlockStateProperties.VERTICAL_DIRECTION;
    public static final EnumProperty<DripstoneThickness> THICKNESS = BlockStateProperties.DRIPSTONE_THICKNESS;
    public static final BooleanProperty WATERLOGGED = BlockStateProperties.WATERLOGGED;

    private static final float STALACTITE_DAMAGE_PER_FALL_DISTANCE_AND_SIZE = 1.0F;
    private static final int STALACTITE_MAX_DAMAGE = 40;
    private static final int MAX_STALACTITE_HEIGHT_FOR_DAMAGE_CALCULATION = 6;
    private static final float STALAGMITE_FALL_DISTANCE_OFFSET = 2.0F;
    private static final int STALAGMITE_FALL_DAMAGE_MODIFIER = 2;
    private static final float FALL_CHANCE_PER_ENTITY_TICK = 0.3F;
    private static final float MAX_HORIZONTAL_OFFSET = 0.125F;

    private static final VoxelShape TIP_MERGE_SHAPE = Block.box(5.0, 0.0, 5.0, 11.0, 16.0, 11.0);
    private static final VoxelShape TIP_SHAPE_UP = Block.box(5.0, 0.0, 5.0, 11.0, 11.0, 11.0);
    private static final VoxelShape TIP_SHAPE_DOWN = Block.box(5.0, 5.0, 5.0, 11.0, 16.0, 11.0);
    private static final VoxelShape FRUSTUM_SHAPE = Block.box(4.0, 0.0, 4.0, 12.0, 16.0, 12.0);
    private static final VoxelShape MIDDLE_SHAPE = Block.box(3.0, 0.0, 3.0, 13.0, 16.0, 13.0);
    private static final VoxelShape BASE_SHAPE = Block.box(2.0, 0.0, 2.0, 14.0, 16.0, 14.0);

    public IceSpikeBlock() {
        this(BlockBehaviour.Properties.of()
                .mapColor(MapColor.ICE)
                .strength(0.5F)
                .noOcclusion()
                .sound(SoundType.GLASS)
                .noLootTable()
                .dynamicShape()
                .offsetType(BlockBehaviour.OffsetType.XZ)
                .pushReaction(PushReaction.DESTROY)
        );
    }

    public IceSpikeBlock(BlockBehaviour.Properties properties) {
        super(properties);
        this.registerDefaultState(this.stateDefinition.any()
                .setValue(TIP_DIRECTION, Direction.UP)
                .setValue(THICKNESS, DripstoneThickness.TIP)
                .setValue(WATERLOGGED, false));
    }

    @Override
    protected MapCodec<? extends Block> codec() {
        return CODEC;
    }

    @Override
    protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
        builder.add(TIP_DIRECTION, THICKNESS, WATERLOGGED);
    }

    @Override
    protected boolean canSurvive(BlockState state, LevelReader level, BlockPos pos) {
        return isValidPlacement(level, pos, state.getValue(TIP_DIRECTION));
    }

    @Nullable
    @Override
    public BlockState getStateForPlacement(BlockPlaceContext context) {
        LevelAccessor level = context.getLevel();
        BlockPos pos = context.getClickedPos();
        Direction direction = context.getNearestLookingVerticalDirection().getOpposite();
        Direction tipDir = calculateTipDirection(level, pos, direction);
        if (tipDir == null) {
            return null;
        }
        DripstoneThickness thickness = calculateDripstoneThickness(level, pos, tipDir, !context.isSecondaryUseActive());
        return this.defaultBlockState()
                .setValue(TIP_DIRECTION, tipDir)
                .setValue(THICKNESS, thickness)
                .setValue(WATERLOGGED, level.getFluidState(pos).getType() == Fluids.WATER);
    }

    @Nullable
    private static Direction calculateTipDirection(LevelReader level, BlockPos pos, Direction dir) {
        Direction direction;
        if (isValidPlacement(level, pos, dir)) {
            direction = dir;
        } else {
            if (!isValidPlacement(level, pos, dir.getOpposite())) {
                return null;
            }
            direction = dir.getOpposite();
        }
        return direction;
    }

    @Override
    protected BlockState updateShape(BlockState state, Direction direction, BlockState neighborState,
                                     LevelAccessor level, BlockPos pos, BlockPos neighborPos) {
        if (state.getValue(WATERLOGGED)) {
            level.scheduleTick(pos, Fluids.WATER, Fluids.WATER.getTickDelay(level));
        }
        if (direction != Direction.UP && direction != Direction.DOWN) {
            return state;
        }
        Direction tipDir = state.getValue(TIP_DIRECTION);
        if (tipDir == Direction.DOWN && level.getBlockTicks().hasScheduledTick(pos, this)) {
            return state;
        }
        if (direction == tipDir.getOpposite() && !this.canSurvive(state, level, pos)) {
            if (tipDir == Direction.DOWN) {
                level.scheduleTick(pos, this, 2);
            } else {
                level.scheduleTick(pos, this, 1);
            }
            return state;
        }
        boolean isTipMerge = state.getValue(THICKNESS) == DripstoneThickness.TIP_MERGE;
        DripstoneThickness thickness = calculateDripstoneThickness(level, pos, tipDir, isTipMerge);
        return state.setValue(THICKNESS, thickness);
    }

    @Override
    protected void tick(BlockState state, ServerLevel level, BlockPos pos, RandomSource random) {
        if (isStalagmite(state) && !this.canSurvive(state, level, pos)) {
            level.destroyBlock(pos, true);
        } else {
            spawnFallingStalactite(state, level, pos);
        }
    }

    @Override
    public void entityInside(BlockState state, Level level, BlockPos pos, Entity entity) {
        if (level.isClientSide || !(entity instanceof LivingEntity)) {
            return;
        }
        // 实体在本格或冰锥正下方（上方最多 6 格）经过时，0.3 概率触发整根冰锥掉落
        for (int d = 0; d <= 6; d++) {
            BlockPos check = pos.above(d);
            BlockState spike = level.getBlockState(check);
            if (isIceSpikeWithDirection(spike, Direction.DOWN)) {
                if (level.random.nextFloat() < FALL_CHANCE_PER_ENTITY_TICK) {
                    spawnFallingStalactiteFromRoot(spike, level, check);
                }
                return;
            }
        }
    }
    private static void spawnFallingStalactiteFromRoot(BlockState state, Level level, BlockPos pos) {
        BlockPos.MutableBlockPos root = pos.mutable();
        BlockState rootState = state;
        while (isIceSpikeWithDirection(level.getBlockState(root.above()), Direction.DOWN)) {
            root.move(Direction.UP);
            rootState = level.getBlockState(root);
        }
        spawnFallingStalactite(rootState, (ServerLevel) level, root.immutable());
    }

    private static void spawnFallingStalactite(BlockState state, ServerLevel level, BlockPos pos) {
        BlockPos.MutableBlockPos mutable = pos.mutable();
        BlockState blockState = state;
        while (isIceSpikeWithDirection(blockState, Direction.DOWN)) {
            FallingBlockEntity falling = FallingBlockEntity.fall(level, mutable, blockState);
            falling.disableDrop();
            if (isTip(blockState, true)) {
                int i = Math.max(1 + pos.getY() - mutable.getY(), MAX_STALACTITE_HEIGHT_FOR_DAMAGE_CALCULATION);
                float f = STALACTITE_DAMAGE_PER_FALL_DISTANCE_AND_SIZE * (float) i;
                falling.setHurtsEntities(f, STALACTITE_MAX_DAMAGE);
                break;
            }
            mutable.move(Direction.DOWN);
            blockState = level.getBlockState(mutable);
        }
    }

    @Override
    public void fallOn(Level level, BlockState state, BlockPos pos, Entity entity, float fallDistance) {
        if (state.getValue(TIP_DIRECTION) == Direction.UP && state.getValue(THICKNESS) == DripstoneThickness.TIP) {
            entity.causeFallDamage(fallDistance + STALAGMITE_FALL_DISTANCE_OFFSET, STALAGMITE_FALL_DAMAGE_MODIFIER,
                    level.damageSources().stalagmite());
        } else {
            super.fallOn(level, state, pos, entity, fallDistance);
        }
    }

    @Override
    public void onBrokenAfterFall(Level level, BlockPos pos, FallingBlockEntity fallingBlock) {
        if (!fallingBlock.isSilent()) {
            level.levelEvent(1045, pos, 0);
        }
    }

    @Override
    public void onLand(Level level, BlockPos pos, BlockState state, BlockState replaceableState,
                       FallingBlockEntity fallingBlock) {
        if (level.getBlockState(pos).is(this)) {
            level.setBlock(pos, Blocks.AIR.defaultBlockState(), 3);
        }
        level.levelEvent(1045, pos, 0);
    }
    @Override
    public DamageSource getFallDamageSource(Entity entity) {
        return entity.damageSources().fallingStalactite(entity);
    }

    @Override
    protected VoxelShape getShape(BlockState state, BlockGetter level, BlockPos pos, CollisionContext context) {
        DripstoneThickness thickness = state.getValue(THICKNESS);
        VoxelShape shape;
        if (thickness == DripstoneThickness.TIP_MERGE) {
            shape = TIP_MERGE_SHAPE;
        } else if (thickness == DripstoneThickness.TIP) {
            shape = state.getValue(TIP_DIRECTION) == Direction.DOWN ? TIP_SHAPE_DOWN : TIP_SHAPE_UP;
        } else if (thickness == DripstoneThickness.FRUSTUM) {
            shape = FRUSTUM_SHAPE;
        } else if (thickness == DripstoneThickness.MIDDLE) {
            shape = MIDDLE_SHAPE;
        } else {
            shape = BASE_SHAPE;
        }
        Vec3 offset = state.getOffset(level, pos);
        return shape.move(offset.x, 0.0, offset.z);
    }

    @Override
    protected boolean isCollisionShapeFullBlock(BlockState state, BlockGetter level, BlockPos pos) {
        return false;
    }

    @Override
    protected float getMaxHorizontalOffset() {
        return MAX_HORIZONTAL_OFFSET;
    }

    @Override
    protected FluidState getFluidState(BlockState state) {
        return state.getValue(WATERLOGGED) ? Fluids.WATER.getSource(false) : super.getFluidState(state);
    }

    @Override
    protected VoxelShape getOcclusionShape(BlockState state, BlockGetter level, BlockPos pos) {
        return Shapes.empty();
    }

    @Override
    protected boolean isPathfindable(BlockState state, PathComputationType pathComputationType) {
        return false;
    }

    private static DripstoneThickness calculateDripstoneThickness(LevelReader level, BlockPos pos, Direction dir, boolean isTipMerge) {
        Direction opposite = dir.getOpposite();
        BlockState next = level.getBlockState(pos.relative(dir));
        if (isIceSpikeWithDirection(next, opposite)) {
            return !isTipMerge && next.getValue(THICKNESS) != DripstoneThickness.TIP_MERGE
                    ? DripstoneThickness.TIP : DripstoneThickness.TIP_MERGE;
        } else if (!isIceSpikeWithDirection(next, dir)) {
            return DripstoneThickness.TIP;
        } else {
            DripstoneThickness thickness = next.getValue(THICKNESS);
            if (thickness != DripstoneThickness.TIP && thickness != DripstoneThickness.TIP_MERGE) {
                BlockState prev = level.getBlockState(pos.relative(opposite));
                return !isIceSpikeWithDirection(prev, dir) ? DripstoneThickness.BASE : DripstoneThickness.MIDDLE;
            } else {
                return DripstoneThickness.FRUSTUM;
            }
        }
    }

    private static boolean isValidPlacement(LevelReader level, BlockPos pos, Direction dir) {
        BlockPos supportPos = pos.relative(dir.getOpposite());
        BlockState support = level.getBlockState(supportPos);
        if (support.isFaceSturdy(level, supportPos, dir) || isIceSpikeWithDirection(support, dir)) {
            return true;
        }
        // 倒挂冰锥允许立在平面上（落地时会由 onLand 打碎，不掉落物）
        if (dir == Direction.DOWN) {
            BlockPos floorPos = pos.below();
            BlockState floor = level.getBlockState(floorPos);
            return floor.isFaceSturdy(level, floorPos, Direction.UP) || isIceSpikeWithDirection(floor, Direction.DOWN);
        }
        return false;
    }

    private static boolean isTip(BlockState state, boolean isTipMerge) {
        if (!(state.getBlock() instanceof IceSpikeBlock)) {
            return false;
        }
        DripstoneThickness thickness = state.getValue(THICKNESS);
        return thickness == DripstoneThickness.TIP || isTipMerge && thickness == DripstoneThickness.TIP_MERGE;
    }

    private static boolean isStalactite(BlockState state) {
        return isIceSpikeWithDirection(state, Direction.DOWN);
    }

    private static boolean isStalagmite(BlockState state) {
        return isIceSpikeWithDirection(state, Direction.UP);
    }

    private static boolean isIceSpikeWithDirection(BlockState state, Direction dir) {
        return state.getBlock() instanceof IceSpikeBlock && state.getValue(TIP_DIRECTION) == dir;
    }
}
