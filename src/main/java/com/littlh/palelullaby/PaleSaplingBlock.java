package com.littlh.palelullaby;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Registry;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.block.SaplingBlock;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.levelgen.feature.ConfiguredFeature;

public class PaleSaplingBlock extends SaplingBlock {
    private final ResourceKey<ConfiguredFeature<?, ?>> featureKey;

    public PaleSaplingBlock(ResourceLocation featureId, BlockBehaviour.Properties properties) {
        super(null, properties);
        this.featureKey = ResourceKey.create(Registries.CONFIGURED_FEATURE, featureId);
    }

    @Override
    protected boolean mayPlaceOn(BlockState state, BlockGetter level, BlockPos pos) {
        // 红针木树苗只能在浸润淤泥/浸润淤泥草方块上种植与生长
        if (featureKey.location().getPath().equals("red_needle_tree")) {
            return state.is(PaleLullabyBlocks.SOAKED_MUD.get())
                    || state.is(PaleLullabyBlocks.SOAKED_MUD_GRASS.get());
        }
        return super.mayPlaceOn(state, level, pos);
    }

    @Override
    public void advanceTree(ServerLevel level, BlockPos pos, BlockState state, RandomSource random) {
        Registry<ConfiguredFeature<?, ?>> registry = level.registryAccess().registryOrThrow(Registries.CONFIGURED_FEATURE);
        registry.getHolder(featureKey).ifPresent(holder -> {
            // Remove the sapling before placing the tree: core features like minecraft:tree need air or REPLACEABLE_BY_TREES at the root.
            BlockState fluid = level.getFluidState(pos).createLegacyBlock();
            level.setBlock(pos, fluid, 4);
            if (!holder.value().place(level, level.getChunkSource().getGenerator(), random, pos)) {
                level.setBlock(pos, state, 4);
            }
        });
    }
}
