package com.littlh.palelullaby;

import com.mojang.serialization.Codec;
import net.minecraft.core.BlockPos;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.WorldGenLevel;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.levelgen.Heightmap;
import net.minecraft.world.level.levelgen.feature.Feature;
import net.minecraft.world.level.levelgen.feature.FeaturePlaceContext;
import net.minecraft.world.level.levelgen.feature.configurations.NoneFeatureConfiguration;

public class GothicCliffFeature extends Feature<NoneFeatureConfiguration> {
    public GothicCliffFeature(Codec<NoneFeatureConfiguration> codec) {
        super(codec);
    }

    @Override
    public boolean place(FeaturePlaceContext<NoneFeatureConfiguration> context) {
        WorldGenLevel level = context.level();
        BlockPos origin = context.origin();
        RandomSource random = context.random();

        int towers = 2 + random.nextInt(3);
        boolean generated = false;
        for (int t = 0; t < towers; t++) {
            int tx = origin.getX() + random.nextInt(-5, 6);
            int tz = origin.getZ() + random.nextInt(-5, 6);
            int y = level.getHeight(Heightmap.Types.WORLD_SURFACE_WG, tx, tz) - 1;
            if (y < level.getMinBuildHeight() + 1) {
                continue;
            }
            int height = 12 + random.nextInt(14);
            int baseR = 2 + random.nextInt(2);
            for (int layer = 0; layer <= height; layer++) {
                int ly = y + layer;
                int r = Math.max(1, baseR - (layer * baseR) / Math.max(1, height));
                for (int dx = -r; dx <= r; dx++) {
                    for (int dz = -r; dz <= r; dz++) {
                        if (dx * dx + dz * dz > r * r + 1) {
                            continue;
                        }
                        if (Math.abs(dx) == r && Math.abs(dz) == r && random.nextFloat() < 0.5F) {
                            continue;
                        }
                        BlockPos p = new BlockPos(tx + dx, ly, tz + dz);
                        BlockState cur = level.getBlockState(p);
                        if (!cur.isAir() && !cur.canBeReplaced()) {
                            continue;
                        }
                        BlockState st = random.nextFloat() < 0.1F ? Blocks.COBBLESTONE.defaultBlockState()
                                : random.nextFloat() < 0.06F ? Blocks.MOSSY_COBBLESTONE.defaultBlockState()
                                : random.nextFloat() < 0.05F ? Blocks.DEEPSLATE.defaultBlockState()
                                : Blocks.STONE.defaultBlockState();
                        setBlock(level, p, st);
                        generated = true;
                    }
                }
            }
            // 尖塔顶部
            for (int i = 1; i <= 3; i++) {
                BlockPos p = new BlockPos(tx, y + height + i, tz);
                if (level.isEmptyBlock(p)) {
                    setBlock(level, p, Blocks.STONE.defaultBlockState());
                }
            }
        }
        return generated;
    }
}
