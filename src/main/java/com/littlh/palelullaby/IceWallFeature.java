package com.littlh.palelullaby;

import com.mojang.serialization.Codec;
import net.minecraft.core.BlockPos;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.WorldGenLevel;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.SnowLayerBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.IntegerProperty;
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

        int surfaceY = level.getHeight(Heightmap.Types.WORLD_SURFACE_WG, origin.getX(), origin.getZ()) - 1;
        if (surfaceY < level.getMinBuildHeight() + 1) {
            return false;
        }

        int radius = 10 + random.nextInt(7);
        int minSurface = surfaceY;
        for (int dx = -radius - 4; dx <= radius + 4; dx++) {
            for (int dz = -radius - 4; dz <= radius + 4; dz++) {
                int y = level.getHeight(Heightmap.Types.WORLD_SURFACE_WG, origin.getX() + dx, origin.getZ() + dz) - 1;
                if (y < minSurface) {
                    minSurface = y;
                }
            }
        }

        int height = 22 + random.nextInt(10);
        int top = Math.min(minSurface + height, 220);
        int insertDepth = 5 + random.nextInt(4);
        double noiseAngle = random.nextDouble() * Math.PI * 2;

        boolean generated = false;
        for (int dx = -radius - 3; dx <= radius + 3; dx++) {
            for (int dz = -radius - 3; dz <= radius + 3; dz++) {
                int x = origin.getX() + dx;
                int z = origin.getZ() + dz;
                double angle = Math.atan2(dz, dx);
                double dist = Math.sqrt(dx * dx + dz * dz);
                double ripple = 1.0 + 0.2 * Math.sin(angle * 3.0 + noiseAngle) + random.nextDouble() * 0.14;
                int baseY = level.getHeight(Heightmap.Types.WORLD_SURFACE_WG, x, z) - 1;
                if (baseY < level.getMinBuildHeight()) {
                    continue;
                }

                for (int y = minSurface - insertDepth; y <= top; y++) {
                    double targetRadius = radius + 0.35 * (top - y) / (double) height;
                    if (y < minSurface) {
                        double depthFactor = (minSurface - y) / (double) insertDepth;
                        targetRadius = Math.max(2.0, radius * (1.0 - depthFactor * 0.72));
                    }
                    targetRadius *= 1.0 + 0.2 * Math.sin(angle * 2.2 + noiseAngle * 0.6);
                    targetRadius *= ripple;

                    if (dist > targetRadius + 0.9) {
                        continue;
                    }

                    BlockPos pos = new BlockPos(x, y, z);
                    if (y >= top - 4) {
                        if (dist >= targetRadius - 0.8 && random.nextFloat() < 0.45F) {
                            setBlock(level, pos, Blocks.PACKED_ICE.defaultBlockState());
                        } else {
                            setBlock(level, pos, Blocks.ICE.defaultBlockState());
                        }
                        generated = true;
                        continue;
                    }

                    if (y >= top - 12) {
                        if (random.nextFloat() < 0.3F) {
                            setBlock(level, pos, Blocks.ICE.defaultBlockState());
                        } else {
                            setBlock(level, pos, Blocks.PACKED_ICE.defaultBlockState());
                        }
                        generated = true;
                        continue;
                    }

                    if (y >= minSurface - 1) {
                        if (random.nextFloat() < 0.35F) {
                            setBlock(level, pos, Blocks.CLAY.defaultBlockState());
                        } else if (random.nextFloat() < 0.4F) {
                            setBlock(level, pos, Blocks.GRAVEL.defaultBlockState());
                        } else {
                            setBlock(level, pos, Blocks.STONE.defaultBlockState());
                        }
                        generated = true;
                        continue;
                    }

                    if (y >= minSurface - 3) {
                        if (random.nextFloat() < 0.55F) {
                            setBlock(level, pos, Blocks.DEEPSLATE.defaultBlockState());
                        } else {
                            setBlock(level, pos, Blocks.BLACKSTONE.defaultBlockState());
                        }
                        generated = true;
                        continue;
                    }

                    setBlock(level, pos, Blocks.BLACKSTONE.defaultBlockState());
                    generated = true;
                }
            }
        }

        // 最后补上雪层，优先覆盖结构顶部和边缘
        for (int dx = -radius - 3; dx <= radius + 3; dx++) {
            for (int dz = -radius - 3; dz <= radius + 3; dz++) {
                int x = origin.getX() + dx;
                int z = origin.getZ() + dz;
                int y = level.getHeight(Heightmap.Types.WORLD_SURFACE_WG, x, z);
                BlockPos topPos = new BlockPos(x, y, z);
                if (!level.isEmptyBlock(topPos)) {
                    continue;
                }
                BlockState below = level.getBlockState(topPos.below());
                if (!below.isSolidRender(level, topPos.below())) {
                    continue;
                }
                double angle = Math.atan2(dz, dx);
                double dist = Math.sqrt(dx * dx + dz * dz);
                double coverage = 1.0 - Math.min(1.0, dist / (radius + 2.5));
                if (random.nextFloat() < coverage * 0.95F) {
                    if (random.nextFloat() < 0.25F) {
                        setBlock(level, topPos, Blocks.SNOW_BLOCK.defaultBlockState());
                    } else {
                        int layers = 4 + random.nextInt(5);
                        setBlock(level, topPos, Blocks.SNOW.defaultBlockState().setValue(SnowLayerBlock.LAYERS, Math.min(8, layers)));
                    }
                    generated = true;
                }
            }
        }

        // 生成侧面洞穴结构
        if (random.nextFloat() < 0.6F) {
            int caveCount = 1 + random.nextInt(2);
            for (int i = 0; i < caveCount; i++) {
                double theta = random.nextDouble() * Math.PI * 2;
                int caveX = origin.getX() + (int) Math.round(Math.cos(theta) * (radius - 1 + random.nextDouble()));
                int caveZ = origin.getZ() + (int) Math.round(Math.sin(theta) * (radius - 1 + random.nextDouble()));
                int caveY = minSurface + 3 + random.nextInt(Math.max(1, height - 6));
                int caveRadius = 2 + random.nextInt(2);
                carveCave(level, new BlockPos(caveX, caveY, caveZ), caveRadius);
            }
        }

        return generated;
    }

    private void carveCave(WorldGenLevel level, BlockPos center, int radius) {
        for (int dx = -radius; dx <= radius; dx++) {
            for (int dy = -radius; dy <= radius; dy++) {
                for (int dz = -radius; dz <= radius; dz++) {
                    double dist = Math.sqrt(dx * dx + dy * dy + dz * dz);
                    if (dist <= radius * (0.8 + 0.2 * (1 - Math.abs(dy) / (double) radius))) {
                        BlockPos pos = center.offset(dx, dy, dz);
                        if (level.getBlockState(pos).isAir()) {
                            continue;
                        }
                        setBlock(level, pos, Blocks.AIR.defaultBlockState());
                    }
                }
            }
        }
    }
}
