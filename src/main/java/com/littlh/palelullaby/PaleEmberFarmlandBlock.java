package com.littlh.palelullaby;

import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.RandomSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;

/**
 * 苍白余烬耕地：用锄头开垦苍白余烬获得。
 * 永不干涸、默认始终湿润；跳跃/摔落可把它踩回苍白余烬；只能种植苍白菰麦。
 */
public class PaleEmberFarmlandBlock extends Block {
    public PaleEmberFarmlandBlock(Properties properties) {
        super(properties);
    }

    @Override
    protected boolean isRandomlyTicking(BlockState state) {
        return true;
    }

    @Override
    protected void randomTick(BlockState state, ServerLevel level, BlockPos pos, RandomSource random) {
        // 永不干涸：只在下方式块不再是余烬/苍白草时回退
        if (!hasSupport(level, pos)) {
            level.setBlock(pos, PaleLullabyBlocks.PALE_EMBER.get().defaultBlockState(), 3);
        }
    }

    @Override
    protected void neighborChanged(BlockState state, Level level, BlockPos pos, Block neighborBlock,
                                   BlockPos neighborPos, boolean movedByPiston) {
        if (!level.isClientSide && !hasSupport(level, pos)) {
            level.setBlock(pos, PaleLullabyBlocks.PALE_EMBER.get().defaultBlockState(), 3);
        }
    }

    @Override
    public void fallOn(Level level, BlockState state, BlockPos pos, Entity entity, float fallDistance) {
        if (!level.isClientSide && level.random.nextFloat() < fallDistance - 0.5F
                && entity instanceof LivingEntity
                && (entity instanceof Player || level.getGameRules().getBoolean(net.minecraft.world.level.GameRules.RULE_MOBGRIEFING))
                && !isUnderCrops(level, pos)) {
            // 跳跃踩踏会把耕地踩回苍白余烬
            level.setBlock(pos, PaleLullabyBlocks.PALE_EMBER.get().defaultBlockState(), 3);
        }
        super.fallOn(level, state, pos, entity, fallDistance);
    }

    private static boolean hasSupport(LevelReader level, BlockPos pos) {
        BlockState below = level.getBlockState(pos.below());
        return below.is(PaleLullabyBlocks.PALE_EMBER.get())
                || below.is(PaleLullabyBlocks.PALE_GRASS_BLOCK.get());
    }

    private static boolean isUnderCrops(BlockGetter level, BlockPos pos) {
        return level.getBlockState(pos.above()).is(PaleLullabyBlocks.PALE_WHEAT.get());
    }
}
