package com.littlh.palelullaby;

import com.mojang.serialization.MapCodec;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.util.RandomSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.SoundType;
import net.minecraft.world.level.block.VineBlock;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.material.MapColor;
import net.minecraft.world.level.material.PushReaction;

/**
 * 凋萎槲寄生（吸命枝/枯骨藤）：灰白簇状寄生藤，附生在枝条上并向下生长。
 * 类似藤蔓机制：可攀爬，脚离开某段后该段（及其下方整串）才破裂断落。
 */
public class WitheredMistletoeBlock extends VineBlock {
    public static final MapCodec<VineBlock> CODEC = simpleCodec(WitheredMistletoeBlock::new);
    private static final int MAX_CHAIN = 6;

    public WitheredMistletoeBlock() {
        this(BlockBehaviour.Properties.of()
                .mapColor(MapColor.COLOR_LIGHT_GRAY)
                .replaceable()
                .noCollission()
                .randomTicks()
                .strength(0.2F)
                .sound(SoundType.VINE)
                .pushReaction(PushReaction.DESTROY)
        );
    }

    public WitheredMistletoeBlock(BlockBehaviour.Properties properties) {
        super(properties);
    }

    @Override
    public MapCodec<VineBlock> codec() {
        return CODEC;
    }

    /** 只在链的末端向下生长，整串长度有上限。 */
    @Override
    protected void randomTick(BlockState state, ServerLevel level, BlockPos pos, RandomSource random) {
        BlockPos below = pos.below();
        if (level.getBlockState(below).is(this)) {
            return; // 不是链尾
        }
        if (chainLength(level, pos) >= MAX_CHAIN) {
            return;
        }
        if (!level.isEmptyBlock(below) || random.nextInt(3) != 0) {
            return;
        }
        level.setBlock(below, state, 2);
    }

    private int chainLength(ServerLevel level, BlockPos pos) {
        int count = 1;
        BlockPos up = pos.above();
        while (level.getBlockState(up).is(this) && count < MAX_CHAIN) {
            count++;
            up = up.above();
        }
        return count;
    }

    /** 向上攀爬时：脚所在格触发，破碎脚下方那段（已爬过的），当前段保留以便继续攀爬。 */
    @Override
    public void entityInside(BlockState state, Level level, BlockPos pos, Entity entity) {
        if (level.isClientSide) {
            return;
        }
        if (!(entity instanceof LivingEntity living) || !living.isAlive()) {
            return;
        }
        if (entity.getDeltaMovement().y <= 0.01D) {
            return; // 水平穿过或下落不触发，只有主动向上爬才破裂
        }
        if (!pos.equals(BlockPos.containing(entity.getX(), entity.getY(), entity.getZ()))) {
            return; // 只有脚所在的那一格触发，身体其它部分碰到不触发
        }
        BlockPos below = pos.below();
        BlockState belowState = level.getBlockState(below);
        if (!belowState.is(this)) {
            return; // 脚下没有槲寄生（刚起步/已爬完），不破裂
        }
        breakChain(level, below, belowState);
    }

    private void breakChain(Level level, BlockPos pos, BlockState state) {
        level.levelEvent(2001, pos, Block.getId(state));
        level.playSound(null, pos, SoundEvents.VINE_BREAK, SoundSource.BLOCKS, 1.0F, 1.0F);
        dropSelfChance(level, pos);
        level.setBlock(pos, Blocks.AIR.defaultBlockState(), 3);
        BlockPos down = pos.below();
        while (level.getBlockState(down).is(this)) {
            dropSelfChance(level, down);
            level.setBlock(down, Blocks.AIR.defaultBlockState(), 3);
            down = down.below();
        }
    }

    /** 支撑面消失导致整串破裂时也有 20% 概率掉落自身。 */
    @Override
    protected BlockState updateShape(BlockState state, Direction facing, BlockState facingState, LevelAccessor level, BlockPos currentPos, BlockPos facingPos) {
        if (facing == Direction.DOWN) {
            return super.updateShape(state, facing, facingState, level, currentPos, facingPos);
        }
        if (!this.canSurvive(state, level, currentPos) && !level.isClientSide()) {
            // 世界生成阶段 level 是 WorldGenRegion，不是 Level：不能也不该在这里掉落物品
            if (level instanceof Level realLevel) {
                dropSelfChance(realLevel, currentPos);
            }
        }
        return super.updateShape(state, facing, facingState, level, currentPos, facingPos);
    }

    /** 破碎时 20% 概率掉落自身。 */
    private void dropSelfChance(Level level, BlockPos pos) {
        if (level.random.nextFloat() < 0.2F) {
            popResource(level, pos, new ItemStack(this));
        }
    }
}
