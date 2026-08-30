package com.littlh.palelullaby;

import com.mojang.serialization.MapCodec;
import net.minecraft.core.BlockPos;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.BushBlock;
import net.minecraft.world.level.block.SoundType;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.material.MapColor;
import net.minecraft.world.level.material.PushReaction;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.VoxelShape;

/** 腐心菇：黑紫钟形菇，覆血红黏液；黏液腐蚀踩上的实体，并分解死在上面的动物残骸。 */
public class RotheartMushroomBlock extends BushBlock {
    public static final MapCodec<RotheartMushroomBlock> CODEC = simpleCodec(RotheartMushroomBlock::new);
    private static final VoxelShape SHAPE = Block.box(2.0D, 0.0D, 2.0D, 14.0D, 12.0D, 14.0D);

    public RotheartMushroomBlock() {
        this(BlockBehaviour.Properties.of()
                .mapColor(MapColor.CRIMSON_STEM)
                .replaceable()
                .noCollission()
                .instabreak()
                .sound(SoundType.SLIME_BLOCK)
                .offsetType(BlockBehaviour.OffsetType.XZ)
                .pushReaction(PushReaction.DESTROY)
        );
    }

    public RotheartMushroomBlock(BlockBehaviour.Properties properties) {
        super(properties);
    }

    @Override
    protected MapCodec<? extends BushBlock> codec() {
        return CODEC;
    }

    @Override
    public VoxelShape getShape(BlockState state, BlockGetter level, BlockPos pos, CollisionContext context) {
        return SHAPE;
    }

    @Override
    public void entityInside(BlockState state, Level level, BlockPos pos, Entity entity) {
        if (level.isClientSide || !(entity instanceof LivingEntity living) || !living.isAlive()) {
            return;
        }
        // 强腐蚀：每 0.5 秒造成 1 点魔法伤害
        if (living.tickCount % 10 == 0) {
            living.hurt(level.damageSources().magic(), 1.0F);
        }
    }

    @Override
    protected boolean mayPlaceOn(BlockState state, BlockGetter level, BlockPos pos) {
        Block block = state.getBlock();
        return block == PaleLullabyBlocks.SOAKED_MUD_GRASS.get()
                || block == PaleLullabyBlocks.SOAKED_MUD.get()
                || block == Blocks.GRASS_BLOCK
                || block == Blocks.DIRT;
    }
}
