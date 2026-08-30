package com.littlh.palelullaby;

import com.mojang.serialization.MapCodec;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.util.RandomSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.ItemInteractionResult;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.BushBlock;
import net.minecraft.world.level.block.DoublePlantBlock;
import net.minecraft.world.level.block.SoundType;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.DoubleBlockHalf;
import net.minecraft.world.level.block.state.properties.EnumProperty;
import net.minecraft.world.level.block.state.properties.IntegerProperty;
import net.minecraft.world.level.material.MapColor;
import net.minecraft.world.level.material.PushReaction;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.VoxelShape;

/** 寡妇刺（两格高）：枯刺植物，附近生物流血时开花（约 3 天），随后结果；收获果实后整株枯死。像浆果丛一样扎伤踩上的实体。 */
public class WidowThornBlock extends DoublePlantBlock {
    public static final MapCodec<WidowThornBlock> CODEC = simpleCodec(WidowThornBlock::new);
    public static final EnumProperty<Stage> STAGE = EnumProperty.create("stage", Stage.class);
    /** 开花剩余天数（1-3，仅 flowering 时有意义）。 */
    public static final IntegerProperty AGE = IntegerProperty.create("age", 0, 3);
    private static final VoxelShape SHAPE = Block.box(2.0D, 0.0D, 2.0D, 14.0D, 16.0D, 14.0D);

    public enum Stage implements net.minecraft.util.StringRepresentable {
        DORMANT, FLOWERING, FRUITING;

        @Override
        public String getSerializedName() {
            return this.name().toLowerCase(java.util.Locale.ROOT);
        }
    }

    public WidowThornBlock() {
        this(BlockBehaviour.Properties.of()
                .mapColor(MapColor.WOOD)
                .replaceable()
                .noCollission()
                .instabreak()
                .randomTicks()
                .sound(SoundType.SWEET_BERRY_BUSH)
                .pushReaction(PushReaction.DESTROY)
        );
    }

    public WidowThornBlock(BlockBehaviour.Properties properties) {
        super(properties);
        this.registerDefaultState(this.stateDefinition.any()
                .setValue(STAGE, Stage.DORMANT)
                .setValue(AGE, 0)
                .setValue(HALF, DoubleBlockHalf.LOWER));
    }

    @Override
    protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
        builder.add(STAGE, AGE, HALF);
    }

    @Override
    public MapCodec<? extends DoublePlantBlock> codec() {
        return CODEC;
    }

    @Override
    public VoxelShape getShape(BlockState state, BlockGetter level, BlockPos pos, CollisionContext context) {
        return SHAPE;
    }

    /** 像浆果丛一样：踩上去时造成伤害（上下两段都会触发）。 */
    @Override
    public void entityInside(BlockState state, Level level, BlockPos pos, Entity entity) {
        if (level.isClientSide || !(entity instanceof LivingEntity living) || !living.isAlive()) {
            return;
        }
        if (living.isInvulnerableTo(level.damageSources().sweetBerryBush())) {
            return;
        }
        if (living.tickCount % 10 == 0) {
            living.hurt(level.damageSources().sweetBerryBush(), 1.0F);
        }
    }

    @Override
    protected void randomTick(BlockState state, ServerLevel level, BlockPos pos, RandomSource random) {
        if (state.getValue(HALF) != DoubleBlockHalf.LOWER || state.getValue(STAGE) != Stage.FLOWERING) {
            return;
        }
        // 每株每天约 17 次随机刻，这里约一天判一次；3 天后进入结果状态
        if (random.nextInt(17) != 0) {
            return;
        }
        int age = state.getValue(AGE);
        BlockState lower;
        if (age <= 1) {
            lower = state.setValue(STAGE, Stage.FRUITING).setValue(AGE, 0);
        } else {
            lower = state.setValue(AGE, age - 1);
        }
        level.setBlock(pos, lower, 2);
        BlockPos upperPos = pos.above();
        BlockState upper = level.getBlockState(upperPos);
        if (upper.is(this)) {
            level.setBlock(upperPos,
                    upper.setValue(STAGE, lower.getValue(STAGE)).setValue(AGE, lower.getValue(AGE)), 2);
        }
    }

    /** 只能空手采摘：手持物品右键不会触发采摘。 */
    @Override
    protected ItemInteractionResult useItemOn(ItemStack stack, BlockState state, Level level, BlockPos pos,
                                             Player player, InteractionHand hand, BlockHitResult hitResult) {
        if (stack.isEmpty()) {
            return ItemInteractionResult.PASS_TO_DEFAULT_BLOCK_INTERACTION;
        }
        return ItemInteractionResult.SKIP_DEFAULT_BLOCK_INTERACTION;
    }
    /** 空手右键收获：像仙人掌一样会被扎伤（1 点伤害），随后果实掉落、整株枯死消失。 */
    @Override
    protected InteractionResult useWithoutItem(BlockState state, Level level, BlockPos pos,
                                               Player player, BlockHitResult hitResult) {
        BlockPos lowerPos = state.getValue(HALF) == DoubleBlockHalf.LOWER ? pos : pos.below();
        BlockState lower = level.getBlockState(lowerPos);
        if (!lower.is(this) || lower.getValue(STAGE) != Stage.FRUITING) {
            return InteractionResult.PASS;
        }
        if (!level.isClientSide) {
            player.hurt(level.damageSources().cactus(), 1.0F);
            int count = 1 + level.random.nextInt(2);
            popResource(level, lowerPos, new ItemStack(PaleLullabyItems.WIDOW_THORN_FRUIT.get(), count));
            level.removeBlock(lowerPos, false);
            level.removeBlock(lowerPos.above(), false);
            level.playSound(null, lowerPos, SoundEvents.SWEET_BERRY_BUSH_PICK_BERRIES,
                    SoundSource.BLOCKS, 1.0F, 0.8F + level.random.nextFloat() * 0.4F);
        }
        return InteractionResult.sidedSuccess(level.isClientSide);
    }

    @Override
    protected boolean mayPlaceOn(BlockState state, BlockGetter level, BlockPos pos) {
        Block block = state.getBlock();
        return block == Blocks.COARSE_DIRT
                || block == Blocks.GRAVEL
                || block == Blocks.DIRT
                || block == Blocks.GRASS_BLOCK
                || block == Blocks.SAND
                || block == PaleLullabyBlocks.WITHERED_DIRT.get()
                || block == PaleLullabyBlocks.WITHERED_GRASS_BLOCK.get();
    }
}
