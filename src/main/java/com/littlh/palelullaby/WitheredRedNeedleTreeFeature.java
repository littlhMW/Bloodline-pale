package com.littlh.palelullaby;

import com.mojang.serialization.Codec;
import java.util.ArrayList;
import java.util.List;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.WorldGenLevel;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.RotatedPillarBlock;
import net.minecraft.world.level.block.VineBlock;
import net.minecraft.world.level.block.state.properties.BooleanProperty;

import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.levelgen.feature.Feature;
import net.minecraft.world.level.levelgen.feature.FeaturePlaceContext;
import net.minecraft.world.level.levelgen.feature.configurations.NoneFeatureConfiguration;

/**
 * 枯萎红针木：红针木的枯萎变体，没有树叶也没有蔷薇，只有一根倾斜的主枝，
 * 附近地面偶尔横卧倒下的枯木。全部由金合欢木（带皮木块）构成。
 * 每次放置生成 1-3 颗疏松成组，配合 placed feature 的 rarity_filter 以较宽间隔生成。
 */
public class WitheredRedNeedleTreeFeature extends Feature<NoneFeatureConfiguration> {
    public WitheredRedNeedleTreeFeature(Codec<NoneFeatureConfiguration> codec) {
        super(codec);
    }

    @Override
    public boolean place(FeaturePlaceContext<NoneFeatureConfiguration> context) {
        WorldGenLevel level = context.level();
        BlockPos origin = context.origin();
        RandomSource random = context.random();

        BlockPos base = origin.below();
        if (!isGround(level.getBlockState(base))) {
            return false;
        }

        boolean generated = false;
        int group = 1 + random.nextInt(3); // 1-3 颗成组
        for (int i = 0; i < group; i++) {
            BlockPos treeBase = base;
            if (i > 0) {
                // 疏松成组：围绕中心 3-8 格偏移
                int ox = random.nextInt(17) - 8;
                int oz = random.nextInt(17) - 8;
                if (Math.abs(ox) < 3 && Math.abs(oz) < 3) {
                    ox = (ox < 0 ? -1 : 1) * (3 + random.nextInt(6));
                    oz = (oz < 0 ? -1 : 1) * (3 + random.nextInt(6));
                }
                BlockPos cand = base.offset(ox, 0, oz);
                if (!isGround(level.getBlockState(cand))) {
                    continue;
                }
                treeBase = cand;
            }
            generated |= placeTree(level, treeBase, random);
        }
        return generated;
    }

    /** 生成一颗倾斜枯木，树干 8-12 格高，偶尔在附近放一根倒木，枯枝上挂几串凋萎槲寄生。 */
    private boolean placeTree(WorldGenLevel level, BlockPos base, RandomSource random) {
        BlockState wood = log(Direction.Axis.Y);
        boolean generated = false;

        Direction lean = Direction.Plane.HORIZONTAL.getRandomDirection(random);
        int trunkHeight = 8 + random.nextInt(5);
        int slantInterval = 1 + random.nextInt(2);
        int leanTick = 0;
        int grown = 0;
        List<BlockPos> trunkBlocks = new ArrayList<>();
        BlockPos trunkPos = base;

        for (int i = 0; i < trunkHeight; i++) {
            if (!level.isEmptyBlock(trunkPos.above())) {
                break;
            }
            trunkPos = trunkPos.above();
            grown++;
            setBlock(level, trunkPos, wood);
            trunkBlocks.add(trunkPos);
            generated = true;
            leanTick++;
            if (leanTick >= slantInterval) {
                leanTick = 0;
                if (random.nextFloat() < 0.9F) {
                    BlockPos next = trunkPos.relative(lean);
                    if (level.isEmptyBlock(next)) {
                        trunkPos = next;
                        setBlock(level, trunkPos, wood);
                        trunkBlocks.add(trunkPos);
                    }
                    if (random.nextFloat() < 0.4F) {
                        lean = random.nextBoolean() ? lean.getClockWise() : lean.getCounterClockWise();
                    }
                }
            }
        }
        if (grown < 6) {
            return generated;
        }

        if (!trunkBlocks.isEmpty()) {
            generated |= placeMistletoe(level, trunkBlocks, random);
        }

        if (random.nextFloat() < 0.5F) {
            generated |= placeFallenLog(level, base, random);
        }
        return generated;
    }

    /** 在树附近地面放一根横躺的枯木。 */
    private boolean placeFallenLog(WorldGenLevel level, BlockPos base, RandomSource random) {
        Direction dir = Direction.Plane.HORIZONTAL.getRandomDirection(random);
        BlockPos start = base.above().offset(random.nextInt(7) - 3, 0, random.nextInt(7) - 3);
        if (!level.isEmptyBlock(start) || !level.getBlockState(start.below()).isSolid()) {
            return false;
        }
        int len = 3 + random.nextInt(4); // 3-6
        boolean placed = false;
        for (int i = 0; i < len; i++) {
            BlockPos pos = start.relative(dir, i);
            if (!level.isEmptyBlock(pos)) {
                break;
            }
            if (!level.getBlockState(pos.below()).isSolid() && random.nextFloat() < 0.6F) {
                break;
            }
            if (i > 1 && random.nextFloat() < 0.25F) {
                continue;
            }
            setBlock(level, pos, log(dir.getAxis()));
            placed = true;
        }
        return placed;
    }

    /** 在枯木侧面挂一串凋萎槲寄生。 */
    private boolean placeMistletoe(WorldGenLevel level, List<BlockPos> trunkBlocks, RandomSource random) {
        boolean placed = false;
        int count = 1 + random.nextInt(2); // 1-2 串
        for (int k = 0; k < count; k++) {
            BlockPos log = trunkBlocks.get(random.nextInt(trunkBlocks.size()));
            Direction side = Direction.Plane.HORIZONTAL.getRandomDirection(random);
            BlockPos start = log.relative(side);
            if (!level.isEmptyBlock(start)) {
                continue;
            }
            BooleanProperty face = VineBlock.getPropertyForFace(side.getOpposite());
            int len = 2 + random.nextInt(3); // 2-4 格
            for (int i = 0; i < len; i++) {
                BlockPos vp = start.below(i);
                if (!level.isEmptyBlock(vp)) {
                    break;
                }
                setBlock(level, vp, PaleLullabyBlocks.WITHERED_MISTLETOE.get().defaultBlockState().setValue(face, true));
                placed = true;
            }
        }
        return placed;
    }

    private static boolean isGround(BlockState state) {
        return state.isSolid() && state.getFluidState().isEmpty();
    }

    private BlockState log(Direction.Axis axis) {
        return Blocks.ACACIA_WOOD.defaultBlockState().setValue(RotatedPillarBlock.AXIS, axis);
    }
}
