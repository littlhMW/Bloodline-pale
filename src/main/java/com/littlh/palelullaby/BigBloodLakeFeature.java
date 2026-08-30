package com.littlh.palelullaby;

import com.littlh.palelullaby.fluid.ModFluids;
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

/**
 * 血泊的大血池塘：由多个随机椭圆“斑点”相互叠加而成，边缘柔和自然；
 * 水面统一固定在同一高度，不再跟随地形起伏。
 */
public class BigBloodLakeFeature extends Feature<NoneFeatureConfiguration> {

    private record Blob(double cx, double cz, double rx, double rz, double phase) {
    }

    public BigBloodLakeFeature(Codec<NoneFeatureConfiguration> codec) {
        super(codec);
    }

    @Override
    public boolean place(FeaturePlaceContext<NoneFeatureConfiguration> context) {
        WorldGenLevel level = context.level();
        BlockPos origin = context.origin();
        RandomSource random = context.random();
        int minY = level.getMinBuildHeight() + 4;
        if (origin.getY() < minY) {
            return false;
        }
        int centerX = origin.getX();
        int centerZ = origin.getZ();
        int centerSurface = level.getHeight(Heightmap.Types.WORLD_SURFACE_WG, centerX, centerZ) - 1;
        if (centerSurface < minY) {
            return false;
        }
        // 生成 3-5 个相互错开、重叠的椭圆斑点
        int blobCount = 3 + random.nextInt(3);
        Blob[] blobs = new Blob[blobCount];
        double maxRadius = 0;
        for (int i = 0; i < blobCount; i++) {
            double rx = 5.0 + random.nextDouble() * 6.0;
            double rz = 5.0 + random.nextDouble() * 6.0;
            double cx = centerX + (random.nextDouble() * 2.0 - 1.0) * 4.0;
            double cz = centerZ + (random.nextDouble() * 2.0 - 1.0) * 4.0;
            blobs[i] = new Blob(cx, cz, rx, rz, random.nextDouble() * Math.PI * 2.0);
            maxRadius = Math.max(maxRadius, Math.max(rx, rz) + Math.max(Math.abs(cx - centerX), Math.abs(cz - centerZ)));
        }
        int scanRadius = (int) Math.ceil(maxRadius) + 3;
        // 水面固定为比中心地面低 1 格，保证湖面水平
        int waterLevel = centerSurface - 1;
        int depth = 2 + random.nextInt(2);
        boolean generated = false;
        for (int dx = -scanRadius; dx <= scanRadius; dx++) {
            for (int dz = -scanRadius; dz <= scanRadius; dz++) {
                int x = centerX + dx;
                int z = centerZ + dz;
                double dist = blobDistance(blobs, x, z);
                if (dist >= 1.0) {
                    continue;
                }
                double edgeFactor = 1.0 - dist;
                int top = level.getHeight(Heightmap.Types.WORLD_SURFACE_WG, x, z) - 1;
                if (top < minY) {
                    continue;
                }
                // 斑块最外沿铺一圈余烬作湖岸
                if (edgeFactor < 0.18) {
                    setBlock(level, new BlockPos(x, top, z), floorState());
                    generated = true;
                    continue;
                }
                // 挖掉高于水面的地形，让湖面保持水平
                for (int y = top; y > waterLevel; y--) {
                    setBlock(level, new BlockPos(x, y, z), Blocks.AIR.defaultBlockState());
                }
                // 靠近边缘略浅，中心保持设定深度
                int d = Math.max(1, (int) Math.round(depth * Math.min(1.0, edgeFactor + 0.3)));
                int bottom = waterLevel - d;
                for (int y = waterLevel; y > bottom; y--) {
                    setBlock(level, new BlockPos(x, y, z), bloodState());
                }
                setBlock(level, new BlockPos(x, bottom, z), floorState());
                generated = true;
            }
        }
        return generated;
    }

    private static double blobDistance(Blob[] blobs, int x, int z) {
        double best = Double.MAX_VALUE;
        for (Blob blob : blobs) {
            double nx = (x - blob.cx) / blob.rx;
            double nz = (z - blob.cz) / blob.rz;
            double d = Math.sqrt(nx * nx + nz * nz);
            // 每个斑块带一点低频起伏，让边缘柔和而不碎
            double wobble = 0.85 + 0.3 * Math.sin(Math.atan2(nz, nx) * 3.0 + blob.phase);
            best = Math.min(best, d / wobble);
        }
        return best;
    }

    private BlockState bloodState() {
        return ModFluids.BLOOD_BLOCK.get().defaultBlockState();
    }

    private BlockState floorState() {
        return PaleLullabyBlocks.PALE_EMBER.get().defaultBlockState();
    }
}
