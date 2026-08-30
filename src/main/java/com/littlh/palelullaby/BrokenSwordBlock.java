package com.littlh.palelullaby;

import com.mojang.serialization.MapCodec;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.InteractionResult;
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
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.Shapes;
import net.minecraft.world.phys.shapes.VoxelShape;

/**
 * 断剑：插在花丛与石座基座里的三格高残剑。
 * 底座占放置格，剑身向上延伸两格（由不可见的部件方块提供碰撞）。
 * 右键可让玩家在「苍白摇篮」与主世界的对应坐标位置之间互相传送。
 */
public class BrokenSwordBlock extends HorizontalDirectionalBlock implements EntityBlock {
    public static final MapCodec<BrokenSwordBlock> CODEC = simpleCodec(BrokenSwordBlock::new);

    private static final VoxelShape BASE_SHAPE = Shapes.or(
            Shapes.box(0.0, 0.0, 0.0, 1.0, 0.1875, 1.0),       // 石座基座
            Shapes.box(0.4375, 0.0, 0.4375, 0.5625, 1.0, 0.5625) // 穿过本格的剑身
    );

    public BrokenSwordBlock(Properties properties) {
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
        Level level = context.getLevel();
        BlockPos pos = context.getClickedPos();
        for (int i = 1; i <= 2; i++) {
            if (!level.getBlockState(pos.above(i)).canBeReplaced(context)) {
                return null;
            }
        }
        return this.defaultBlockState().setValue(FACING, context.getHorizontalDirection());
    }

    @Override
    protected void onPlace(BlockState state, Level level, BlockPos pos, BlockState oldState, boolean movedByPiston) {
        if (!level.isClientSide) {
            Direction facing = state.getValue(FACING);
            for (int i = 1; i <= 2; i++) {
                BlockPos partPos = pos.above(i);
                if (!level.getBlockState(partPos).canBeReplaced()) {
                    continue;
                }
                level.setBlock(partPos, PaleLullabyBlocks.BROKEN_SWORD_PART.get().defaultBlockState()
                        .setValue(BrokenSwordPartBlock.FACING, facing), 3);
            }
        }
    }

    @Override
    protected void onRemove(BlockState state, Level level, BlockPos pos, BlockState newState, boolean movedByPiston) {
        if (!newState.is(state.getBlock())) {
            for (int i = 1; i <= 2; i++) {
                BlockPos partPos = pos.above(i);
                if (level.getBlockState(partPos).is(PaleLullabyBlocks.BROKEN_SWORD_PART.get())) {
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
        return BASE_SHAPE;
    }

    @Override
    public VoxelShape getShape(BlockState state, BlockGetter level, BlockPos pos, CollisionContext context) {
        return BASE_SHAPE;
    }

    @Override
    public BlockEntity newBlockEntity(BlockPos pos, BlockState state) {
        return new BrokenSwordBlockEntity(pos, state);
    }

    @Override
    protected InteractionResult useWithoutItem(BlockState state, Level level, BlockPos pos, Player player,
                                               BlockHitResult hitResult) {
        if (!level.isClientSide && player instanceof ServerPlayer serverPlayer) {
            teleportAcrossDimensions((ServerLevel) level, serverPlayer, pos);
        }
        return InteractionResult.sidedSuccess(level.isClientSide);
    }

    /**
     * 在摇篮维度右键 -> 传回主世界对应坐标；在主世界（或其他维度）右键 -> 传送到摇篮对应坐标。
     */
    private static void teleportAcrossDimensions(ServerLevel current, ServerPlayer player, BlockPos pos) {
        MinecraftServer server = current.getServer();
        ResourceKey<Level> cradleKey = ResourceKey.create(Registries.DIMENSION,
                ResourceLocation.fromNamespaceAndPath(PaleLullaby.MOD_ID, "pale_cradle"));
        ServerLevel destination = current.dimension().equals(cradleKey)
                ? server.getLevel(Level.OVERWORLD)
                : server.getLevel(cradleKey);
        if (destination == null) {
            return;
        }
        BlockPos safe = findSafeSpawn(destination, pos.getX(), pos.getZ());
        player.teleportTo(destination, safe.getX() + 0.5, safe.getY(), safe.getZ() + 0.5,
                player.getYRot(), player.getXRot());
        destination.playSound(null, safe, SoundEvents.PORTAL_TRAVEL, SoundSource.PLAYERS, 0.5F, 1.0F);
    }

    /** 在目标维度对应 x/z 处向下扫描，寻找安全的站立位置。 */
    private static BlockPos findSafeSpawn(ServerLevel world, int x, int z) {
        BlockPos.MutableBlockPos pos = new BlockPos.MutableBlockPos();
        for (int y = world.getMaxBuildHeight() - 1; y > world.getMinBuildHeight(); y--) {
            pos.set(x, y, z);
            if (!world.getBlockState(pos).isAir()
                    && world.getBlockState(pos).getFluidState().isEmpty()
                    && world.getBlockState(pos.above()).isAir()
                    && world.getBlockState(pos.above(2)).isAir()) {
                return pos.above();
            }
        }
        return new BlockPos(x, 64, z);
    }
}
