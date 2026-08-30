package com.littlh.palelullaby;

import com.mojang.serialization.Codec;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.server.level.WorldGenRegion;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.WorldGenLevel;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.LeavesBlock;
import net.minecraft.world.level.block.RotatedPillarBlock;
import net.minecraft.world.level.block.VineBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.block.state.properties.BooleanProperty;
import net.minecraft.world.level.levelgen.feature.Feature;
import net.minecraft.world.level.levelgen.feature.FeaturePlaceContext;
import net.minecraft.world.level.levelgen.feature.configurations.NoneFeatureConfiguration;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * 红刺树：金合欢式的倾斜主干 + 夸张枯树枝，枝头挂扁平樱花瓣状叶球。
 * 主干 1-2 根、2x2 加粗、明显倾斜；树下留出大片空当；顶部是又高又宽的叶冠，几乎连成一片。
 */
public class RedNeedleTreeFeature extends Feature<NoneFeatureConfiguration> {
    public RedNeedleTreeFeature(Codec<NoneFeatureConfiguration> codec) {
        super(codec);
    }

    @Override
    public boolean place(FeaturePlaceContext<NoneFeatureConfiguration> context) {
        WorldGenLevel level = context.level();
        BlockPos origin = context.origin();
        RandomSource random = context.random();

        // 落点即树苗/地表上方的空气格：worldgen 由 heightmap 放置，树苗催熟时树苗已被移除。
        // 基座直接取落点下方一格，避免重新查地表高度时被上方树冠/树叶干扰导致催熟失败。
        BlockPos base = origin.below();
        BlockState baseState = level.getBlockState(base);
        if (!baseState.isSolid() || !baseState.getFluidState().isEmpty()
                || !isSoakedMud(baseState)) {
            return false;
        }
        // 树叶最低高度：距地表至少 5 格，彻底避免贴地树叶
        int minLeafY = base.getY() + 6;

        BlockState logY = log(Direction.Axis.Y);
        boolean generated = false;
        List<BlockPos> wood = new ArrayList<>(); // 记录所有木头位置，用于在树干侧面挂蔷薇

        // 1-2 根主干，各自朝不同方向明显倾斜，越往上越歪
        int trunkCount = 1 + random.nextInt(2);
        List<BlockPos> trunkTops = new ArrayList<>();
        for (int t = 0; t < trunkCount; t++) {
            Direction lean = Direction.Plane.HORIZONTAL.getRandomDirection(random);
            // 第二根主干偏向另一侧，让整棵树更展开
            if (t > 0) {
                lean = lean.getOpposite();
            }
            // 第二根主干从旁边 2-3 格长出，避免起点落在第一根主干的树根座上
            BlockPos trunkBase = base;
            if (t > 0) {
                Direction side = lean.getClockWise();
                trunkBase = base.offset(side.getStepX() * (2 + random.nextInt(2)), 0, side.getStepZ() * (2 + random.nextInt(2)));
            }
            BlockState trunkBaseState = level.getBlockState(trunkBase);
            if (!trunkBaseState.isSolid() || !trunkBaseState.getFluidState().isEmpty()
                    || !isSoakedMud(trunkBaseState)) {
                continue;
            }
            int trunkHeight = 24 + random.nextInt(10); // 24-33，更高大
            int slantInterval = 1 + random.nextInt(2); // 每 1-2 格横向歪 1 格，主干更倾斜
            int leanTick = 0;
            int grown = 0;
            int offX = 0;
            int offZ = 0;
            BlockPos trunkPos = trunkBase;
            for (int i = 0; i < trunkHeight; i++) {
                if (!level.isEmptyBlock(trunkPos.above())) {
                    break;
                }
                trunkPos = trunkPos.above();
                grown++;
                if (i == 0) {
                    // 根部 3x3 裙座，让斜干站稳
                    placeTrunkFlare(level, trunkPos, logY, wood);
                    generated = true;
                } else {
                    // 主干整体 2x2 加粗
                    placeTrunkSection(level, trunkPos, logY, wood);
                    generated = true;
                }
                leanTick++;
                if (leanTick >= slantInterval) {
                    leanTick = 0;
                    if (random.nextFloat() < 0.95F) {
                        // 限制主干累计横向偏移，避免树干戳到生成区域外导致方块丢失
                        int nextOffX = offX + lean.getStepX();
                        int nextOffZ = offZ + lean.getStepZ();
                        if (Math.abs(nextOffX) + Math.abs(nextOffZ) > 8) {
                            lean = Direction.Plane.HORIZONTAL.getRandomDirection(random);
                            continue;
                        }
                        BlockPos next = trunkPos.relative(lean);
                        if (level.isEmptyBlock(next)) {
                            trunkPos = next;
                            offX = nextOffX;
                            offZ = nextOffZ;
                            safeSet(level, trunkPos, logY);
                            wood.add(trunkPos.immutable());
                            generated = true;
                        }
                        // 中途偶尔换向，制造枯树那种歪扭感
                        if (random.nextFloat() < 0.45F) {
                            lean = random.nextBoolean() ? lean.getClockWise() : lean.getCounterClockWise();
                        }
                    }
                }
                // 主干中上段长出枝丫，枝头挂小叶球（避开贴近地面的低枝）
                if (i > 4 && random.nextFloat() < 0.55F) {
                    BlockPos fork = growFork(level, trunkPos, random, wood);
                    if (fork != null) {
                        placeFlatLeafBlob(level, fork, random, 3, minLeafY);
                        generated = true;
                    }
                }
            }
            // 只有长到一定高度才算有效主干，避免主干一开始被挡住时在地面生成树冠骨架
            if (grown >= 8) {
                trunkTops.add(trunkPos);
            }
        }
        if (trunkTops.isEmpty()) {
            return false;
        }

        // 夸张枯树枝：枝干较短、全部收在树冠半径内，沿枝干挂小叶团让树冠饱满，不会戳出树冠
        int branchCount = 10 + random.nextInt(6); // 10-15
        for (int b = 0; b < branchCount; b++) {
            BlockPos top = trunkTops.get(random.nextInt(trunkTops.size()));
            BlockPos branchStart = top.offset(0, -random.nextInt(4), 0);
            Direction dir = Direction.Plane.HORIZONTAL.getRandomDirection(random);
            branchStart = branchStart.relative(dir);
            if (!level.isEmptyBlock(branchStart)) {
                continue;
            }
            safeSet(level, branchStart, log(dir.getAxis()));
            wood.add(branchStart.immutable());
            generated = true;
            BlockPos cur = branchStart;
            int len = 3 + random.nextInt(4); // 3-6，不再戳出树冠
            for (int s = 0; s < len; s++) {
                // 偶尔转向，枯枝感
                if (s > 1 && random.nextFloat() < 0.3F) {
                    dir = random.nextBoolean() ? dir.getClockWise() : dir.getCounterClockWise();
                }
                int ny = cur.getY() + (random.nextFloat() < 0.25F ? 1 : 0) + (s < 2 ? 1 : 0);
                BlockPos next = new BlockPos(cur.getX() + dir.getStepX(), ny, cur.getZ() + dir.getStepZ());
                // 枝干不超出树冠水平半径（6），避免从阿米巴树冠边缘戳出
                int dx = next.getX() - top.getX();
                int dz = next.getZ() - top.getZ();
                if (dx * dx + dz * dz > 36) {
                    break;
                }
                if (!level.isEmptyBlock(next)) {
                    break;
                }
                safeSet(level, next, log(dir.getAxis()));
                wood.add(next.immutable());
                generated = true;
                cur = next;
                // 沿枝干挂小叶子团，把枝干盖住
                if (s % 3 == 2) {
                    placeFlatLeafBlob(level, cur, random, 2, minLeafY);
                }
            }
            // 枝头挂小片叶子团（缩小，不戳出树冠）
            placeFlatLeafBlob(level, cur, random, 2 + random.nextInt(2), minLeafY);
        }

        // 顶部宽大的树冠：连成一片遮天蔽日，边缘像阿米巴/不规则水滴一样弯曲
        Set<BlockPos> canopyLeaves = new HashSet<>();
        for (BlockPos top : trunkTops) {
            int canopyLayers = 2 + random.nextInt(2); // 2-3 层
            int canopyRadius = 8 + random.nextInt(5); // 8-12
            // 先铺树冠树叶（阿米巴状曲线边缘），再放骨架木头，保证木头不会戳出树冠
            for (int layer = 0; layer < canopyLayers; layer++) {
                int r = canopyRadius - layer + random.nextInt(2); // 每层半径抖动，边缘更不规则
                placeFlatCanopy(level, top.offset(0, layer + 1, 0), r, random, minLeafY, canopyLeaves);
                generated = true;
            }
            if (random.nextFloat() < 0.6F) {
                placeFlatLeafBlob(level, top.above(canopyLayers + 1), random, 3, minLeafY);
            }
            placeCanopyBranches(level, top, canopyRadius, canopyLayers, random, wood, canopyLeaves);
        }
        // 在树干/枝干侧面挂一些猩红蔷薇（树冠以下的木头才挂）
        int minCanopyY = Integer.MAX_VALUE;
        for (BlockPos top : trunkTops) {
            minCanopyY = Math.min(minCanopyY, top.getY() + 1);
        }
        scatterRoses(level, wood, minCanopyY, random);
        return generated;
    }

    /**
     * 在红刺树的干、枝侧面挂少量猩红蔷薇（树冠以下，数量封顶，避免生成过多造成卡顿）。
     */
    private void scatterRoses(WorldGenLevel level, List<BlockPos> wood, int maxY, RandomSource random) {
        int target = 6 + random.nextInt(5); // 每棵树 6-10 朵
        int placed = 0;
        for (BlockPos pos : wood) {
            if (placed >= target) {
                break;
            }
            // 树冠以下的干/枝才挂，树冠内部被树叶挡住，挂了也看不见
            if (pos.getY() >= maxY) {
                continue;
            }
            // 每个位置约 50% 概率挂一朵，避免太多
            if (random.nextFloat() < 0.5F) {
                continue;
            }
            Direction side = Direction.Plane.HORIZONTAL.getRandomDirection(random);
            BlockPos rosePos = pos.relative(side);
            if (!level.isEmptyBlock(rosePos)) {
                continue;
            }
            safeSet(level, rosePos, CrimsonRoseBlock.naturalState(PaleLullabyBlocks.CRIMSON_ROSE.get().defaultBlockState(), random)
                    .setValue(CrimsonThornAttachmentBlock.FACING, side.getOpposite()));
            placed++;
        }
    }

    private BlockState log(Direction.Axis axis) {
        return PaleLullabyBlocks.RED_NEEDLE_LOG.get().defaultBlockState().setValue(RotatedPillarBlock.AXIS, axis);
    }

    /** 红刺树只能扎根在浸润淤泥（草皮）上。 */
    private static boolean isSoakedMud(BlockState state) {
        return state.is(PaleLullabyBlocks.SOAKED_MUD.get())
                || state.is(PaleLullabyBlocks.SOAKED_MUD_GRASS.get());
    }

    /**
     * 树冠内部的木质骨架：中心竖木贯穿各层，再向四周长出几条弯曲枝干。
     * 所有木头只放在被树叶包裹的位置，不会伸出树冠，也不会生成整齐的十字直线。
     */
    private void placeCanopyBranches(WorldGenLevel level, BlockPos center, int radius, int layers, RandomSource random, List<BlockPos> wood, Set<BlockPos> leaves) {
        // 中心竖木贯穿树冠所有层，连通顶部叶球（位置都被树叶覆盖）
        for (int y = 0; y <= layers + 1; y++) {
            BlockPos pos = center.offset(0, y, 0);
            if (leaves.contains(pos)) {
                safeSet(level, pos, log(Direction.Axis.Y));
                wood.add(pos.immutable());
            }
        }
        // 从树冠中心向四周长几条弯曲枝干，只放在被树叶包裹的位置，绝不戳出树冠
        int arms = 4 + random.nextInt(3); // 4-6 根
        for (int a = 0; a < arms; a++) {
            Direction dir = Direction.Plane.HORIZONTAL.getRandomDirection(random);
            int startY = 1 + random.nextInt(layers); // 从不同层长出
            BlockPos cur = center.offset(0, startY, 0);
            int maxLen = Math.max(2, radius - 1 + random.nextInt(3));
            for (int s = 0; s < maxLen; s++) {
                // 中途随机转向，让枝干自然弯曲
                if (s > 0 && random.nextFloat() < 0.35F) {
                    dir = random.nextBoolean() ? dir.getClockWise() : dir.getCounterClockWise();
                }
                int ny = cur.getY() + (random.nextFloat() < 0.3F ? 1 : 0);
                BlockPos next = new BlockPos(cur.getX() + dir.getStepX(), ny, cur.getZ() + dir.getStepZ());
                // 只有被树叶包裹的位置才放木头：本身是树叶且至少两个水平邻居也是树叶
                if (!isCoveredByLeaves(leaves, next)) {
                    break;
                }
                if (!level.getBlockState(next).is(PaleLullabyBlocks.RED_NEEDLE_LEAVES.get())) {
                    break;
                }
                safeSet(level, next, log(dir.getAxis()));
                wood.add(next.immutable());
                cur = next;
            }
        }
    }

    /** 判断位置是否藏在树冠内：该位置有树叶，且至少两个水平邻居也有树叶。 */
    private boolean isCoveredByLeaves(Set<BlockPos> leaves, BlockPos pos) {
        if (!leaves.contains(pos)) {
            return false;
        }
        int neighbors = 0;
        for (Direction dir : Direction.Plane.HORIZONTAL) {
            if (leaves.contains(pos.relative(dir))) {
                neighbors++;
            }
        }
        return neighbors >= 2;
    }

    private void placeTrunkSection(WorldGenLevel level, BlockPos basePos, BlockState state, List<BlockPos> wood) {
        for (int dx = 0; dx <= 1; dx++) {
            for (int dz = 0; dz <= 1; dz++) {
                BlockPos pos = basePos.offset(dx, 0, dz);
                if (level.isEmptyBlock(pos)) {
                    safeSet(level, pos, state);
                    wood.add(pos.immutable());
                }
            }
        }
    }

    private void placeTrunkFlare(WorldGenLevel level, BlockPos basePos, BlockState state, List<BlockPos> wood) {
        for (int dx = -1; dx <= 1; dx++) {
            for (int dz = -1; dz <= 1; dz++) {
                BlockPos pos = basePos.offset(dx, 0, dz);
                if (level.isEmptyBlock(pos)) {
                    safeSet(level, pos, state);
                    wood.add(pos.immutable());
                }
            }
        }
    }

    private BlockState leaves() {
        return PaleLullabyBlocks.RED_NEEDLE_LEAVES.get().defaultBlockState()
                .setValue(LeavesBlock.PERSISTENT, true);
    }

    /** 从枝干侧面长出一小段次枝，返回末端位置（被挡住时返回 null）。 */
    private BlockPos growFork(WorldGenLevel level, BlockPos from, RandomSource random, List<BlockPos> wood) {
        Direction forkDir = Direction.Plane.HORIZONTAL.getRandomDirection(random);
        BlockPos cur = from;
        int forkLen = 3 + random.nextInt(4); // 3-6 // 4-9，次枝也加长
        for (int s = 0; s < forkLen; s++) {
            if (random.nextFloat() < 0.4F) {
                forkDir = random.nextBoolean() ? forkDir.getClockWise() : forkDir.getCounterClockWise();
            }
            int ny = cur.getY() + (random.nextFloat() < 0.4F ? 1 : 0);
            BlockPos next = new BlockPos(cur.getX() + forkDir.getStepX(), ny, cur.getZ() + forkDir.getStepZ());
            if (!level.isEmptyBlock(next)) {
                return s > 1 ? cur : null;
            }
            safeSet(level, next, log(forkDir.getAxis()));
            wood.add(next.immutable());
            cur = next;
        }
        return cur;
    }

    /** 扁扁的一片叶子球：横向半径大，纵向只有 1-2 格厚。 */
    private void placeFlatLeafBlob(WorldGenLevel level, BlockPos center, RandomSource random, int radius, int minY) {
        BlockState leaves = leaves();
        int thickness = 1 + random.nextInt(2);
        for (int dx = -radius; dx <= radius; dx++) {
            for (int dz = -radius; dz <= radius; dz++) {
                for (int dy = -thickness; dy <= thickness; dy++) {
                    double h = Math.sqrt(dx * dx + dz * dz) / (radius + 0.5);
                    double v = Math.abs(dy) / (thickness + 0.5);
                    if (h * h + v * v > 1.05) {
                        continue;
                    }
                    // 边缘留缺口，让树冠透气
                    if (h > 0.65 && random.nextFloat() < 0.4F) {
                        continue;
                    }
                    BlockPos pos = center.offset(dx, dy, dz);
                    if (pos.getY() < minY) {
                        continue;
                    }
                    if (level.isEmptyBlock(pos)) {
                        safeSet(level, pos, leaves);
                    }
                }
            }
        }
    }

    /** 顶部扁冠：边缘用多层正弦叠加成阿米巴/不规则水滴状曲线，边缘稀疏，内部少量留洞。 */
    private void placeFlatCanopy(WorldGenLevel level, BlockPos center, int radius, RandomSource random, int minY, Set<BlockPos> leafSet) {
        BlockState leavesState = leaves();
        double phase1 = random.nextDouble() * Math.PI * 2;
        double phase2 = random.nextDouble() * Math.PI * 2;
        double phase3 = random.nextDouble() * Math.PI * 2;
        for (int dx = -radius - 2; dx <= radius + 2; dx++) {
            for (int dz = -radius - 2; dz <= radius + 2; dz++) {
                double dist = Math.sqrt(dx * dx + dz * dz);
                double angle = Math.atan2(dz, dx);
                // 阿米巴边缘：3 条不同频率的正弦波叠加，每棵树的相位随机
                double wobble = 1.0
                        + 0.22 * Math.sin(3 * angle + phase1)
                        + 0.13 * Math.sin(5 * angle + phase2)
                        + 0.06 * Math.sin(7 * angle + phase3);
                double edge = radius * wobble;
                if (dist > edge + 0.5) {
                    continue;
                }
                // 边缘少量镂空，阿米巴形状本身已经不规则
                if (dist > edge - 2.0 && random.nextFloat() < 0.3F) {
                    continue;
                }
                if (dist < radius - 2.0 && random.nextFloat() < 0.12F) {
                    continue;
                }
                // 树冠内部留极少量缝隙透气
                if (random.nextFloat() < 0.05F) {
                    continue;
                }
                BlockPos pos = center.offset(dx, 0, dz);
                if (pos.getY() < minY) {
                    continue;
                }
                if (level.isEmptyBlock(pos)) {
                    safeSet(level, pos, leavesState);
                    leafSet.add(pos.immutable());
                    // 树冠侧面有概率垂挂藤蔓
                    if (dist > edge - 2.5 && random.nextFloat() < 0.25F) {
                        placeHangingVines(level, pos, random);
                    }
                }
            }
        }
    }

    /** 只把方块写到当前生成区域（中心 3x3 区块）内，避免 far chunk 警告与方块丢失。 */
    private void safeSet(WorldGenLevel level, BlockPos pos, BlockState state) {
        if (level instanceof WorldGenRegion region) {
            ChunkPos center = region.getCenter();
            int cx = pos.getX() >> 4;
            int cz = pos.getZ() >> 4;
            if (Math.abs(center.x - cx) > 1 || Math.abs(center.z - cz) > 1) {
                return;
            }
        }
        setBlock(level, pos, state);
    }

    /** 从树冠侧面垂挂一串藤蔓（参照原版 LeaveVineDecorator 的挂藤方式）。 */
    private void placeHangingVines(WorldGenLevel level, BlockPos leafPos, RandomSource random) {
        Direction side = Direction.Plane.HORIZONTAL.getRandomDirection(random);
        BooleanProperty attachFace = VineBlock.getPropertyForFace(side.getOpposite());
        int length = 2 + random.nextInt(3); // 2-4 格
        BlockPos vinePos = leafPos.relative(side);
        for (int i = 0; i < length; i++) {
            BlockPos pos = vinePos.below(i);
            if (!level.isEmptyBlock(pos)) {
                return;
            }
            safeSet(level, pos, Blocks.VINE.defaultBlockState().setValue(attachFace, true));
        }
    }
}
