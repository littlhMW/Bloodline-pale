package com.littlh.palelullaby;

import com.mojang.serialization.MapCodec;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.RandomSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.PipeBlock;
import net.minecraft.world.level.block.SoundType;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.material.MapColor;
import net.minecraft.world.level.pathfinder.PathComputationType;
import net.minecraft.world.phys.Vec3;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.VoxelShape;

import java.util.ArrayDeque;
import java.util.Deque;
import java.util.HashSet;
import java.util.Set;

/**
 * 猩红荆棘：参考紫颂植物（Chorus Plant）的连接方块实现。
 * 继承 PipeBlock（1.21.1 中连接方块 ConnectingBlock 的官方名），
 * 用 6 个布尔状态记录六个方向的连接，模型随连接状态动态变化。
 */
public class CrimsonThornBlock extends PipeBlock {
    public static final MapCodec<CrimsonThornBlock> CODEC = simpleCodec(CrimsonThornBlock::new);

    /** 荆棘主茎的最大高度。 */
    public static final int MAX_HEIGHT = 12;

    /** 荆棘主茎相对根部允许的最大水平伸展半径（格）。 */
    public static final int MAX_RADIUS = 3;

    /** 每向上长多少格转一次弯，形成阶梯型盘旋。 */
    public static final int SPIRAL_EVERY = 2;

    /** 盘旋转向顺序（从上方看顺时针）。 */
    public static final Direction[] SPIRAL_DIRS = {
            Direction.NORTH, Direction.EAST, Direction.SOUTH, Direction.WEST
    };

    /** 碰撞箱：只取荆棘本体（中心 8x8x8 方块），排除十字薄板和连接部分。 */
    private static final VoxelShape BODY_SHAPE = box(4.0, 4.0, 4.0, 12.0, 12.0, 12.0);

    @Override
    protected VoxelShape getShape(BlockState state, BlockGetter level, BlockPos pos, CollisionContext context) {
        return BODY_SHAPE;
    }

    @Override
    protected VoxelShape getCollisionShape(BlockState state, BlockGetter level, BlockPos pos, CollisionContext context) {
        return BODY_SHAPE;
    }

    /**
     * ????????????????????????
     */
    @Override
    protected void entityInside(BlockState state, Level level, BlockPos pos, Entity entity) {
        if (entity instanceof LivingEntity && entity.getType() != EntityType.FOX && entity.getType() != EntityType.BEE) {
            entity.makeStuckInBlock(state, new Vec3(0.8F, 0.75F, 0.8F));
            if (!level.isClientSide && (entity.xOld != entity.getX() || entity.zOld != entity.getZ())) {
                double d0 = Math.abs(entity.getX() - entity.xOld);
                double d1 = Math.abs(entity.getZ() - entity.zOld);
                if (d0 >= 0.003F || d1 >= 0.003F) {
                    entity.hurt(level.damageSources().sweetBerryBush(), 1.0F);
                }
            }
        }
    }

    public CrimsonThornBlock(BlockBehaviour.Properties properties) {
        super(0.3125F, properties);
        this.registerDefaultState(this.stateDefinition.any()
                .setValue(NORTH, false)
                .setValue(EAST, false)
                .setValue(SOUTH, false)
                .setValue(WEST, false)
                .setValue(UP, false)
                .setValue(DOWN, false));
    }

    @Override
    public MapCodec<CrimsonThornBlock> codec() {
        return CODEC;
    }

    private static boolean isThorn(BlockState state) {
        return state.is(PaleLullabyBlocks.CRIMSON_THORN.get());
    }

    /** 蔷薇/棘果这类附着在荆棘上的装饰方块。 */
    private static boolean isAttachment(BlockState state) {
        return state.is(PaleLullabyBlocks.CRIMSON_ROSE.get())
                || state.is(PaleLullabyBlocks.CRIMSON_THORN_BERRY_BLOCK.get());
    }

    /** 需要朝该方向伸出连接块的邻居：荆棘本体，以及附着在其上的蔷薇/棘果。 */
    private static boolean isLinkNeighbor(BlockState state) {
        return isThorn(state) || isAttachment(state);
    }

    /** 荆棘可扎根的方块：浸润淤泥系列（以及荆棘自身，用于横向连接）。 */
    public static boolean isSupport(BlockState state) {
        return isThorn(state)
                || state.is(PaleLullabyBlocks.SOAKED_MUD.get())
                || state.is(PaleLullabyBlocks.SOAKED_MUD_GRASS.get());
    }

    @Override
    public BlockState getStateForPlacement(BlockPlaceContext context) {
        return getStateWithConnections(context.getLevel(), context.getClickedPos(), this.defaultBlockState());
    }

    public static BlockState getStateWithConnections(BlockGetter level, BlockPos pos, BlockState state) {
        return state
                .setValue(DOWN, isSupport(level.getBlockState(pos.below())) || isAttachment(level.getBlockState(pos.below())))
                .setValue(UP, isLinkNeighbor(level.getBlockState(pos.above())))
                .setValue(NORTH, isLinkNeighbor(level.getBlockState(pos.north())))
                .setValue(EAST, isLinkNeighbor(level.getBlockState(pos.east())))
                .setValue(SOUTH, isLinkNeighbor(level.getBlockState(pos.south())))
                .setValue(WEST, isLinkNeighbor(level.getBlockState(pos.west())));
    }

    /**
     * 用当前世界状态重算某个荆棘方块的 6 向连接。
     * 世界生成时 setBlock 不会触发邻居的 updateShape，需要生成完毕后统一补齐连接。
     */
    public static void syncConnections(LevelAccessor level, BlockPos pos) {
        BlockState state = level.getBlockState(pos);
        if (!isThorn(state)) {
            return;
        }
        level.setBlock(pos, getStateWithConnections(level, pos, state), 3);
    }

    @Override
    protected BlockState updateShape(BlockState state, Direction facing, BlockState facingState, LevelAccessor level, BlockPos currentPos, BlockPos facingPos) {
        if (!state.canSurvive(level, currentPos)) {
            level.scheduleTick(currentPos, this, 1);
            return super.updateShape(state, facing, facingState, level, currentPos, facingPos);
        }
        boolean flag = isLinkNeighbor(facingState) || (facing == Direction.DOWN && isSupport(facingState));
        return state.setValue(PROPERTY_BY_DIRECTION.get(facing), flag);
    }

    @Override
    protected void tick(BlockState state, ServerLevel level, BlockPos pos, RandomSource random) {
        if (!state.canSurvive(level, pos)) {
            level.destroyBlock(pos, true);
        }
    }

    @Override
    protected boolean canSurvive(BlockState state, LevelReader level, BlockPos pos) {
        // 下方直接有支撑即可存活；否则沿水平四邻 BFS，整片中任意一块下方有支撑即可存活，
        // 支持横向无限衍生搭建，同时用 visited 防环。
        if (isSupport(level.getBlockState(pos.below()))) {
            return true;
        }
        Deque<BlockPos> queue = new ArrayDeque<>();
        Set<BlockPos> visited = new HashSet<>();
        queue.add(pos);
        visited.add(pos);
        while (!queue.isEmpty()) {
            BlockPos current = queue.poll();
            if (isSupport(level.getBlockState(current.below()))) {
                return true;
            }
            for (Direction direction : Direction.Plane.HORIZONTAL) {
                BlockPos neighbor = current.relative(direction);
                if (visited.add(neighbor) && isThorn(level.getBlockState(neighbor))) {
                    queue.add(neighbor);
                }
            }
        }
        return false;
    }

    @Override
    protected void randomTick(BlockState state, ServerLevel level, BlockPos pos, RandomSource random) {
        // 生长极其缓慢：平均约 2 小时以上才尝试长一步
        if (random.nextInt(128) != 0) {
            return;
        }
        growFrom(level, pos, random, 1);
    }

    /** 沿茎向下找到这株荆棘的根部（最下面一格）。 */
    private static BlockPos findBase(ServerLevel level, BlockPos pos) {
        BlockPos base = pos;
        while (level.getBlockState(base.below()).is(PaleLullabyBlocks.CRIMSON_THORN.get())) {
            base = base.below();
        }
        return base;
    }

    /** 判断 pos 相对根部的水平距离是否在允许范围内。 */
    public static boolean withinRadius(BlockPos base, BlockPos pos) {
        return Math.abs(pos.getX() - base.getX()) <= MAX_RADIUS
                && Math.abs(pos.getZ() - base.getZ()) <= MAX_RADIUS;
    }

    /**
     * 让荆棘从指定位置向上生长 steps 次：以根部为基准，主茎按阶梯型盘旋上升
     * （每 SPIRAL_EVERY 格水平转弯一次），高度不超过 MAX_HEIGHT，
     * 水平方向不超出 MAX_RADIUS，并在新茎上随机结出棘果或蔷薇。
     * 供随机刻与血液催熟共用。
     */
    public static void growFrom(ServerLevel level, BlockPos pos, RandomSource random, int steps) {
        if (!level.getBlockState(pos).is(PaleLullabyBlocks.CRIMSON_THORN.get())) {
            return;
        }
        BlockPos base = findBase(level, pos);
        BlockPos.MutableBlockPos top = base.mutable();
        int height = 1;
        while (height < MAX_HEIGHT && level.getBlockState(top.above()).is(PaleLullabyBlocks.CRIMSON_THORN.get())) {
            top.move(Direction.UP);
            height++;
        }
        BlockState defaultThorn = PaleLullabyBlocks.CRIMSON_THORN.get().defaultBlockState();
        BlockPos prev = null;
        boolean turnPending = false;
        for (int i = 0; i < steps; i++) {
            if (height >= MAX_HEIGHT) {
                return;
            }
            BlockPos next;
            if (turnPending) {
                // 转弯时先水平走一格，下一格竖直向上，保持相邻连接
                next = top.above();
                turnPending = false;
            } else if (height % SPIRAL_EVERY == 0) {
                Direction side = SPIRAL_DIRS[(height / SPIRAL_EVERY) % SPIRAL_DIRS.length];
                BlockPos candidate = top.relative(side);
                if (level.isEmptyBlock(candidate) && withinRadius(base, candidate)) {
                    next = candidate;
                    turnPending = true;
                } else {
                    next = top.above();
                }
            } else {
                next = top.above();
            }
            if (!level.isEmptyBlock(next)) {
                return;
            }
            level.setBlock(next, getStateWithConnections(level, next, defaultThorn), 3);
            if (prev != null) {
                level.setBlock(prev, getStateWithConnections(level, prev, level.getBlockState(prev)), 3);
            }
            prev = next;
            top.set(next);
            height++;
            // 新茎上随机结出猩红棘果或猩红蔷薇
            if (height >= 2 && random.nextFloat() < 0.35F) {
                placeAttachment(level, top, random);
            }
            // 长到一定高度后偶尔横向分支，让整体更像一棵树（分支同样受宽度限制）
            if (height >= 3 && random.nextFloat() < 0.3F) {
                Direction side = Direction.Plane.HORIZONTAL.getRandomDirection(random);
                BlockPos branch = top.relative(side);
                if (level.isEmptyBlock(branch) && withinRadius(base, branch)) {
                    level.setBlock(branch, getStateWithConnections(level, branch, defaultThorn), 3);
                    if (random.nextBoolean()) {
                        BlockPos branchTip = branch.relative(side);
                        if (level.isEmptyBlock(branchTip) && withinRadius(base, branchTip)) {
                            level.setBlock(branchTip, getStateWithConnections(level, branchTip, defaultThorn), 3);
                        }
                    }
                    if (random.nextFloat() < 0.5F) {
                        placeAttachment(level, branch, random);
                    }
                }
            }
        }
    }

    private static void placeAttachment(ServerLevel level, BlockPos pos, RandomSource random) {
        Direction side = Direction.getRandom(random);
        BlockPos sidePos = pos.relative(side);
        if (!level.isEmptyBlock(sidePos)) {
            return;
        }
        BlockState attachment = random.nextBoolean()
                ? PaleLullabyBlocks.CRIMSON_THORN_BERRY_BLOCK.get().defaultBlockState()
                : CrimsonRoseBlock.naturalState(PaleLullabyBlocks.CRIMSON_ROSE.get().defaultBlockState(), random);
        level.setBlock(sidePos, attachment.setValue(CrimsonThornAttachmentBlock.FACING, side.getOpposite()), 3);
    }

    @Override
    protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
        builder.add(NORTH, EAST, SOUTH, WEST, UP, DOWN);
    }

    @Override
    protected boolean isPathfindable(BlockState state, PathComputationType pathComputationType) {
        return false;
    }
}
