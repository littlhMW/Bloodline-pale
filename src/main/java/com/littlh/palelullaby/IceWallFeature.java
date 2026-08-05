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

public class IceWallFeature extends Feature<NoneFeatureConfiguration> {
    public IceWallFeature(Codec<NoneFeatureConfiguration> codec) {
        super(codec);
    }

    @Override
    public boolean place(FeaturePlaceContext<NoneFeatureConfiguration> context) {
        WorldGenLevel level = context.level();
        BlockPos origin = context.origin();
        RandomSource random = context.random();

        int radius = 5 + random.nextInt(4);
        int scanR = radius + 3;
        int surfaceY = level.getHeight(Heightmap.Types.WORLD_SURFACE_WG, origin.getX(), origin.getZ()) - 1;
        if (surfaceY < level.getMinBuildHeight() + 1) {
            return false;
        }

        // 扫描周围，取最低地表作为冰壁基座，保证顶部齐平
        int minSurface = surfaceY;
        for (int dx = -scanR; dx <= scanR; dx++) {
            for (int dz = -scanR; dz <= scanR; dz++) {
                int y = level.getHeight(Heightmap.Types.WORLD_SURFACE_WG, origin.getX() + dx, origin.getZ() + dz) - 1;
                if (y < minSurface) {
                    minSurface = y;
                }
            }
        }

        int top = minSurface + 10 + random.nextInt(14);
        if (top > 200) {
            top = 200;
        }

        // 在部分冰壁内部挖出洞穴
        int caveCount = 1 + random.nextInt(2);
        BlockPos[] caveCenters = new BlockPos[caveCount];
        for (int i = 0; i < caveCount; i++) {
            int cx = origin.getX() + random.nextInt(-radius, radius + 1);
            int cz = origin.getZ() + random.nextInt(-radius, radius + 1);
            int cy = minSurface + 2 + random.nextInt(Math.max(2, top - minSurface - 4));
            caveCenters[i] = new BlockPos(cx, cy, cz);
        }

        boolean generated = false;
        for (int dx = -radius; dx <= radius; dx++) {
            for (int dz = -radius; dz <= radius; dz++) {
                int x = origin.getX() + dx;
                int z = origin.getZ() + dz;
                int baseY = level.getHeight(Heightmap.Types.WORLD_SURFACE_WG, x, z) - 1;
                if (baseY < level.getMinBuildHeight()) {
                    continue;
                }
                // 只让接近平台高度的区域长成冰壁，避免变成峰峦
                if (baseY > minSurface + 5) {
                    continue;
                }
                double dist = Math.sqrt(dx * dx + dz * dz);
                if (dist > radius + random.nextDouble() * 0.5) {
                    continue;
                }

                for (int y = baseY; y <= top; y++) {
                    BlockPos p = new BlockPos(x, y, z);
                    boolean cave = false;
                    for (BlockPos c : caveCenters) {
                        if (Math.abs(p.getX() - c.getX()) < 2 && p.getY() >= c.getY() - 1 && p.getY() <= c.getY() + 1 && Math.abs(p.getZ() - c.getZ()) < 2) {
                            cave = true;
                            break;
                        }
                    }
                    if (cave) {
                        continue;
                    }
                    BlockState state;
                    if (y == top) {
                        state = Blocks.BLUE_ICE.defaultBlockState();
                    } else if (y - baseY < 2 && random.nextFloat() < 0.35F) {
                        state = Blocks.PACKED_ICE.defaultBlockState();
                    } else if (random.nextFloat() < 0.08F) {
                        state = Blocks.BLUE_ICE.defaultBlockState();
                    } else {
                        state = Blocks.ICE.defaultBlockState();
                    }
                    setBlock(level, p, state);
                    generated = true;
                }
            }
        }
        return generated;
    }
}
