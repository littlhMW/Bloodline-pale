package com.littlh.palelullaby;

import com.mojang.serialization.Codec;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.WorldGenLevel;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.levelgen.feature.Feature;
import net.minecraft.world.level.levelgen.feature.FeaturePlaceContext;
import net.minecraft.world.level.levelgen.feature.configurations.NoneFeatureConfiguration;

import java.util.ArrayList;
import java.util.List;

/**
 * 猩红荆棘单株：在浸润淤泥上生成一株带连接状态的荆棘，
 * 偶尔带一点初始高度和棘果/蔷薇，用于自然生成。
 */
public class ThornPlantFeature extends Feature<NoneFeatureConfiguration> {
    public ThornPlantFeature(Codec<NoneFeatureConfiguration> codec) {
        super(codec);
    }

    @Override
    public boolean place(FeaturePlaceContext<NoneFeatureConfiguration> context) {
        WorldGenLevel level = context.level();
        BlockPos origin = context.origin();
        RandomSource random = context.random();
        if (!level.isEmptyBlock(origin)) {
            return false;
        }
        BlockState below = level.getBlockState(origin.below());
        if (!CrimsonThornBlock.isSupport(below)) {
            return false;
        }
        BlockState defaultThorn = PaleLullabyBlocks.CRIMSON_THORN.get().defaultBlockState();
        List<BlockPos> placed = new ArrayList<>();
        level.setBlock(origin, CrimsonThornBlock.getStateWithConnections(level, origin, defaultThorn), 3);
        placed.add(origin.immutable());
        BlockPos.MutableBlockPos top = origin.mutable();
        int height = 1;
        int target = 2 + random.nextInt(4); // 2-5 格高
        BlockPos prev = null;
        boolean turnPending = false;
        while (height < target && height < CrimsonThornBlock.MAX_HEIGHT) {
            BlockPos next;
            if (turnPending) {
                // 转弯时先水平走一格，下一格竖直向上，保持相邻连接
                next = top.above();
                turnPending = false;
            } else if (height % CrimsonThornBlock.SPIRAL_EVERY == 0) {
                Direction side = CrimsonThornBlock.SPIRAL_DIRS[(height / CrimsonThornBlock.SPIRAL_EVERY) % CrimsonThornBlock.SPIRAL_DIRS.length];
                BlockPos candidate = top.relative(side);
                if (level.isEmptyBlock(candidate) && CrimsonThornBlock.withinRadius(origin, candidate)) {
                    next = candidate;
                    turnPending = true;
                } else {
                    next = top.above();
                }
            } else {
                next = top.above();
            }
            if (!level.isEmptyBlock(next)) {
                break;
            }
            level.setBlock(next, CrimsonThornBlock.getStateWithConnections(level, next, defaultThorn), 3);
            placed.add(next.immutable());
            if (prev != null) {
                level.setBlock(prev, CrimsonThornBlock.getStateWithConnections(level, prev, level.getBlockState(prev)), 3);
            }
            prev = next;
            top.set(next);
            height++;
        }
        if (random.nextFloat() < 0.3F) {
            Direction side = Direction.Plane.HORIZONTAL.getRandomDirection(random);
            BlockPos sidePos = top.relative(side);
            if (level.isEmptyBlock(sidePos) && CrimsonThornBlock.withinRadius(origin, sidePos)) {
                BlockState attachment = random.nextBoolean()
                        ? PaleLullabyBlocks.CRIMSON_THORN_BERRY_BLOCK.get().defaultBlockState()
                        : CrimsonRoseBlock.naturalState(PaleLullabyBlocks.CRIMSON_ROSE.get().defaultBlockState(), random);
                level.setBlock(sidePos, attachment.setValue(CrimsonThornAttachmentBlock.FACING, side.getOpposite()), 3);
            }
        }
        // 世界生成时 setBlock 不触发邻居 updateShape，这里统一重算所有已放置荆棘的连接状态
        for (BlockPos p : placed) {
            CrimsonThornBlock.syncConnections(level, p);
        }
        return true;
    }
}
