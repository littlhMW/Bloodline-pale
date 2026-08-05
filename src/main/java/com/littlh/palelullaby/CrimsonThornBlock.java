package com.littlh.palelullaby;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.SoundType;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.material.MapColor;

public class CrimsonThornBlock extends Block {
    public CrimsonThornBlock() {
        super(BlockBehaviour.Properties.of()
                .mapColor(MapColor.CRIMSON_STEM)
                .strength(1.5F)
                .sound(SoundType.SWEET_BERRY_BUSH)
                .randomTicks()
                .noOcclusion()
        );
    }

    @Override
    protected void randomTick(BlockState state, ServerLevel level, BlockPos pos, RandomSource random) {
        if (random.nextInt(4) != 0) return;
        Direction dir = Direction.getRandom(random);
        BlockPos target = pos.relative(dir);
        BlockState targetState = level.getBlockState(target);
        if (targetState.isAir() && CrimsonThornSpikeBlock.canAttach(level, target, dir.getOpposite())) {
            level.setBlock(target, PaleLullabyBlocks.CRIMSON_THORN_SPIKE.get().defaultBlockState()
                    .setValue(CrimsonThornSpikeBlock.FACING, dir), 3);
        }
    }
}
