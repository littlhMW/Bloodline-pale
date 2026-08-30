package com.littlh.palelullaby;

import com.mojang.serialization.Codec;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;
import net.minecraft.core.BlockPos;
import net.minecraft.core.QuartPos;
import net.minecraft.server.level.ServerChunkCache;
import net.minecraft.world.level.LevelHeightAccessor;
import net.minecraft.world.level.WorldGenLevel;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.chunk.ChunkGenerator;
import net.minecraft.world.level.levelgen.Heightmap;
import net.minecraft.world.level.levelgen.RandomState;
import net.minecraft.world.level.levelgen.feature.Feature;
import net.minecraft.world.level.levelgen.feature.FeaturePlaceContext;
import net.minecraft.world.level.levelgen.feature.configurations.NoneFeatureConfiguration;

/**
 * 凋萎尖刺：倒插“拉长岩体”而非严格数学三棱锥。
 * 沿轴向地下根部最宽，天空尖端收窄到约 26% 再缓慢收尖；左右两侧分别用独立噪声侵蚀，底面非对称下凹；
 * 表面用双层 3D 噪声直接加减密度，让侧面长出凹槽、裂隙与岩脊，而不是光滑三角面。
 * 根部（宽）埋入地下，尖端（锐）指向天空；每组石刺 2-4 根，根部所在格必须落在枯萎高原。
 */
public class WitheredSpireFeature extends Feature<NoneFeatureConfiguration> {
    private static final int CELL = 300;
    private static final int SEARCH_CELLS = 2;
    private static final int ACTIVE_PCT = 90;
    private static final int POSITION_ATTEMPTS = 8;

    /** 调试统计：锚点群系命中/未命中与放置计数，累计到阈值后输出一条日志。 */
    private static final org.slf4j.Logger SPIKE_LOGGER = com.mojang.logging.LogUtils.getLogger();
    private static final java.util.concurrent.atomic.AtomicInteger ANCHOR_HIT = new java.util.concurrent.atomic.AtomicInteger();
    private static final java.util.concurrent.atomic.AtomicInteger ANCHOR_MISS = new java.util.concurrent.atomic.AtomicInteger();
    private static final java.util.concurrent.atomic.AtomicInteger PLACE_WITH_SPIRES = new java.util.concurrent.atomic.AtomicInteger();
    private static final java.util.concurrent.atomic.AtomicInteger PLACE_GENERATED = new java.util.concurrent.atomic.AtomicInteger();
    /** 其它活动组的安全区半径：本组刃身修剪到不进入该区域，保证不同组的尖刺不相交。 */
    private static final int NEIGHBOR_SAFE_R = 120;
    /** 跨组修剪检查的邻居网格范围（格数）。 */
    private static final int NEIGHBOR_SEARCH = 3;

    /** 每个 120x120 网格的石刺组只计算一次，跨区块复用，避免每个区块都重复查询地形与群系。 */
    private static final ConcurrentHashMap<SpireCell, List<Spire>> SPIRE_CACHE = new ConcurrentHashMap<>();
    private static final int SPIRE_CACHE_LIMIT = 4096;

    // 浅色(天空悬空尖端) -> 深色(地下粗壮根部)
    private static final BlockState[] CORE = {
            Blocks.DIORITE.defaultBlockState(),
            Blocks.DRIPSTONE_BLOCK.defaultBlockState(),
            Blocks.STONE.defaultBlockState(),
            Blocks.TUFF.defaultBlockState(),
            Blocks.BLACKSTONE.defaultBlockState(),
    };
    private static final BlockState[] SKIN = {
            Blocks.STONE.defaultBlockState(),
            Blocks.DIORITE.defaultBlockState(),
            Blocks.TUFF.defaultBlockState(),
            Blocks.DRIPSTONE_BLOCK.defaultBlockState(),
            Blocks.BLACKSTONE.defaultBlockState(),
    };

    public WitheredSpireFeature(Codec<NoneFeatureConfiguration> codec) {
        super(codec);
    }

    @Override
    public boolean place(FeaturePlaceContext<NoneFeatureConfiguration> context) {
        WorldGenLevel level = context.level();
        long seed = level.getSeed();
        int baseX = (context.origin().getX() >> 4) << 4;
        int baseZ = (context.origin().getZ() >> 4) << 4;
        ServerChunkCache chunkSource = (ServerChunkCache) level.getChunkSource();
        RandomState randomState = chunkSource.randomState();

        List<Spire> spires = collectSpires(context.chunkGenerator(), level, randomState, baseX, baseZ, seed);
        if (spires.isEmpty()) {
            return false;
        }
        PLACE_WITH_SPIRES.incrementAndGet();

        boolean generated = false;
        for (int x = 0; x < 16; x++) {
            for (int z = 0; z < 16; z++) {
                int wx = baseX + x;
                int wz = baseZ + z;
                int ground = level.getHeight(Heightmap.Types.WORLD_SURFACE_WG, wx, wz);
                for (Spire s : spires) {
                    // 不再将 ground 传入限制底部，而是让它自由悬空
                    if (fillColumn(level, wx, wz, ground, s, seed)) {
                        generated = true;
                        PLACE_GENERATED.incrementAndGet();
                    }
                }
            }
        }
        return generated;
    }

    private static List<Spire> collectSpires(ChunkGenerator chunkGenerator, LevelHeightAccessor heightAccessor,
                                             RandomState randomState, int baseX, int baseZ, long seed) {
        List<Spire> spires = new ArrayList<>();
        int cellX = Math.floorDiv(baseX, CELL);
        int cellZ = Math.floorDiv(baseZ, CELL);
        for (int gx = -SEARCH_CELLS; gx <= SEARCH_CELLS; gx++) {
            for (int gz = -SEARCH_CELLS; gz <= SEARCH_CELLS; gz++) {
                int cx = cellX + gx;
                int cz = cellZ + gz;
                SpireCell key = new SpireCell(cx, cz, seed);
                List<Spire> cell = SPIRE_CACHE.get(key);
                if (cell == null) {
                    cell = computeCellSpires(chunkGenerator, heightAccessor, randomState, cx, cz, seed);
                    if (SPIRE_CACHE.size() >= SPIRE_CACHE_LIMIT) {
                        SPIRE_CACHE.clear();
                    }
                    SPIRE_CACHE.put(key, cell);
                }
                spires.addAll(cell);
            }
        }
        return spires;
    }

    /** 计算某列石刺的顶面高度（供结构生成复用，保证结构能落在石刺上）。无石刺时返回极小值。 */
    public static int spireTopAt(ChunkGenerator chunkGenerator, RandomState randomState,
                                 LevelHeightAccessor heightAccessor, int wx, int wz, long seed) {
        int baseX = (wx >> 4) << 4;
        int baseZ = (wz >> 4) << 4;
        List<Spire> spires = collectSpires(chunkGenerator, heightAccessor, randomState, baseX, baseZ, seed);
        int best = Integer.MIN_VALUE;
        for (Spire s : spires) {
            int top = spireTopColumn(wx, wz, s, seed);
            if (top > best) {
                best = top;
            }
        }
        return best;
    }

    private static int spireTopColumn(int wx, int wz, Spire s, long seed) {
        double dx = wx - s.sx;
        double dz = wz - s.sz;
        double along = dx * s.cosYaw + dz * s.sinYaw;
        double t = -dx * s.sinYaw + dz * s.cosYaw;
        if (along < -8.0 || along > s.len + 8.0) {
            return Integer.MIN_VALUE;
        }
        double progress = along / s.len;
        if (progress < 0.0 || progress > 1.0) {
            return Integer.MIN_VALUE;
        }
        double shape = spireWidthShape(s, progress, seed, wx, wz);
        if (shape <= 0.02) {
            return Integer.MIN_VALUE;
        }
        double halfW = Math.max(1.15, (s.width * 0.5) * shape);
        if (Math.abs(t) > halfW + 4.0) {
            return Integer.MIN_VALUE;
        }
        double cy = s.yRoot + s.rise * progress;
        double edgeRatio = Math.min(1.0, Math.abs(t) / halfW);
        double roundedEdge = 1.15 * Math.pow(edgeRatio, 4.0);
        double topY = cy + t * s.latTilt - roundedEdge;
        double swell = valueNoise3(wx, 0, wz, 56, seed ^ 0xE0E0E0EL, 3) - 0.5;
        double ripple = valueNoise3(wx, 0, wz, 26, seed ^ 0xAB12CD34L, 7) - 0.5;
        double colVar = valueNoise3(wx, 0, wz, 40, seed ^ 0xE3A51E7L, 4) - 0.5;
        double topFace = topY + colVar * 0.65 + swell * 0.45 + ripple * 0.25;
        return (int) Math.ceil(topFace) + spireDepositDepth(wx, wz, progress, seed);
    }

    /** 计算单个网格内的整组石刺（结果只依赖世界种子，跨区块完全一致，可缓存）。
     *  组内尖刺可相交；不同组的刃身被修剪到不进入彼此的安全区，互不相交。 */
    private static List<Spire> computeCellSpires(ChunkGenerator chunkGenerator, LevelHeightAccessor heightAccessor,
                                                 RandomState randomState, int cx, int cz, long seed) {
        long h = hash(cx, cz, seed);
        if ((h & 0xFFL) >= ACTIVE_PCT * 255L / 100L) {
            return List.of();
        }
        // 每组 2~7 根
        int n = 2 + (int) ((h >>> 8) % 6L);
        double baseYaw = ((h >>> 32) % 360L) * Math.PI / 180.0;
        double perpX = Math.cos(baseYaw + Math.PI / 2.0);
        double perpZ = Math.sin(baseYaw + Math.PI / 2.0);

        // 每组尝试多个中心位置，直到组中心根部落在枯萎高原内，整组一起生成
        for (int attempt = 0; attempt < POSITION_ATTEMPTS; attempt++) {
            long ah = hash(cx * 7 + attempt * 13, cz * 11 + attempt * 17, seed ^ 0xFEEDCAFEL);
            int ax = cx * CELL + 45 + (int) ((ah >>> 16) % (CELL - 90L));
            int az = cz * CELL + 45 + (int) ((ah >>> 24) % (CELL - 90L));

            // 组中心根部必须落在枯萎高原（getNoiseBiome 需要 quart 坐标）
            if (!isWitheredPlateau(chunkGenerator, heightAccessor, randomState, ax, az)) {
                ANCHOR_MISS.incrementAndGet();
                logSpireStats();
                continue;
            }
            ANCHOR_HIT.incrementAndGet();
            logSpireStats();

            List<Spire> result = new ArrayList<>(n);
            for (int i = 0; i < n; i++) {
                long sh = hash(cx * 31 + i * 7, cz * 17 + i * 11, seed);
                // 组内间距 28~52（原 40~70），更紧凑
                double spacing = 28 + ((sh >>> 8) % 25L);
                double off = (i - (n - 1) * 0.5) * spacing;

                // sx, sz is the ROOT projection (buried wide base); the tip extends along yaw
                int sx = ax + (int) Math.round(off * perpX);
                int sz = az + (int) Math.round(off * perpZ) + (int) (((sh >>> 16) % 61L) - 30);

                // 长度 100~200，底部宽约 58~136，15°~45° 斜插
                double len = 100 + ((sh >>> 32) % 101L);
                // 三角截面面积缩为原来的 3/4：宽度乘 sqrt(0.75)，等腰程度(depthRatio)不变
                double width = (90 + ((sh >>> 40) % 121L)) * 0.75 * 0.75 * 0.8660254; // 最大宽度再减 1/4
                double theta = (15 + ((sh >>> 20) % 31L)) * Math.PI / 180.0;
                double normTheta = (theta * 180.0 / Math.PI - 15.0) / 30.0;
                double depthJitter = (((sh >>> 48) % 21L) - 10) / 200.0;
                double depthRatio = 0.80 + normTheta * 0.35 + depthJitter;
                double depth = width * depthRatio;
                double yaw = baseYaw + (i - (n - 1) * 0.5) * 0.5
                        + (((sh >>> 56) % 21L) - 10) * Math.PI / 180.0;
                // 不同组的尖刺不允许相交：把刃长修剪到不进入其它活动组的安全区
                len = trimLenAgainstNeighbors(chunkGenerator, heightAccessor, randomState, cx, cz, seed, sx, sz, yaw, len);
                if (len < 55) continue;
                double rise = len * Math.tan(theta);
                double phase = (sh & 0xFFL) * 0.01;

                long exh = hash(sx, sz, seed ^ 0x123456789ABCDEFL);
                double latTilt = (((exh >>> 16) % 25L) - 12) / 100.0;
                double bottomSkew = (((exh >>> 24) % 51L) - 25) / 100.0;

                // 根部中心埋在根列地表下 rise*(1-exposed) 深度，刀身从根部固定形状向上延伸
                double exposedRatio = 0.60 + ((sh >>> 24) % 26L) / 100.0;
                int groundRoot = chunkGenerator.getBaseHeight(
                        sx, sz, Heightmap.Types.WORLD_SURFACE_WG, heightAccessor, randomState);
                double yRoot = groundRoot - rise * (1.0 - exposedRatio);

                result.add(new Spire(sx, sz, len, width, depth, rise, yaw, Math.cos(yaw), Math.sin(yaw), phase, latTilt, bottomSkew, yRoot));
            }
            return result; // 该组已放置，不再尝试其它位置
        }
        return List.of();
    }

    /** 把刃长修剪到不进入其它活动组的安全区，保证不同组的尖刺不相交；同组内不做修剪。 */
    private static double trimLenAgainstNeighbors(ChunkGenerator chunkGenerator, LevelHeightAccessor heightAccessor,
                                                  RandomState randomState, int cx, int cz, long seed,
                                                  int sx, int sz, double yaw, double len) {
        double best = len;
        double dirX = Math.cos(yaw);
        double dirZ = Math.sin(yaw);
        for (int gx = -NEIGHBOR_SEARCH; gx <= NEIGHBOR_SEARCH; gx++) {
            for (int gz = -NEIGHBOR_SEARCH; gz <= NEIGHBOR_SEARCH; gz++) {
                if (gx == 0 && gz == 0) continue;
                int nx = cx + gx;
                int nz = cz + gz;
                long nh = hash(nx, nz, seed);
                if ((nh & 0xFFL) >= ACTIVE_PCT * 255L / 100L) continue;
                // 邻居组可能使用任意一次尝试的锚点，全部检查，取最保守的修剪
                for (int attempt = 0; attempt < POSITION_ATTEMPTS; attempt++) {
                    long ah = hash(nx * 7 + attempt * 13, nz * 11 + attempt * 17, seed ^ 0xFEEDCAFEL);
                    double ncx = nx * CELL + 45 + (int) ((ah >>> 16) % (CELL - 90L));
                    double ncz = nz * CELL + 45 + (int) ((ah >>> 24) % (CELL - 90L));
                    double dx = ncx - sx;
                    double dz = ncz - sz;
                    double proj = dx * dirX + dz * dirZ;
                    if (proj <= 0.0) continue;
                    double perp = Math.abs(-dx * dirZ + dz * dirX);
                    if (perp >= NEIGHBOR_SAFE_R) continue;
                    // 邻居锚点只有真的通过枯萎高原判定（会生成尖刺）才参与修剪
                    if (!isWitheredPlateau(chunkGenerator, heightAccessor, randomState, (int) ncx, (int) ncz)) continue;
                    double reach = proj - Math.sqrt(NEIGHBOR_SAFE_R * NEIGHBOR_SAFE_R - perp * perp);
                    double limit = reach - 6.0;
                    if (limit < best) best = Math.max(24.0, limit);
                }
            }
        }
        return best;
    }

    /** 判断地表点是否位于枯萎高原：锚点或四周 ±20 格任一点命中即算通过（避免高原斑块小导致整组放弃）。
     *  与尖刺锚点判定、邻居修剪共用同一逻辑。 */
    private static boolean isWitheredPlateau(ChunkGenerator chunkGenerator, LevelHeightAccessor heightAccessor,
                                             RandomState randomState, int x, int z) {
        int[] ox = {0, -20, 20, 0, 0};
        int[] oz = {0, 0, 0, -20, 20};
        for (int i = 0; i < 5; i++) {
            int sx = x + ox[i];
            int sz = z + oz[i];
            int ground = chunkGenerator.getBaseHeight(
                    sx, sz, Heightmap.Types.WORLD_SURFACE_WG, heightAccessor, randomState);
            if (chunkGenerator.getBiomeSource()
                    .getNoiseBiome(QuartPos.fromBlock(sx), QuartPos.fromBlock(ground), QuartPos.fromBlock(sz),
                            randomState.sampler())
                    .is(PaleLullabyBiomes.WITHERED_PLATEAU)) {
                return true;
            }
        }
        return false;
    }

    /** 累计到阈值后输出一次尖刺统计，避免刷屏。 */
    private static void logSpireStats() {
        if (ANCHOR_HIT.get() + ANCHOR_MISS.get() >= 512) {
            SPIKE_LOGGER.info("[spire] anchorHit={} anchorMiss={} placeWithSpires={} placeGenerated={}",
                    ANCHOR_HIT.get(), ANCHOR_MISS.get(), PLACE_WITH_SPIRES.get(), PLACE_GENERATED.get());
            ANCHOR_HIT.set(0);
            ANCHOR_MISS.set(0);
            PLACE_WITH_SPIRES.set(0);
            PLACE_GENERATED.set(0);
        }
    }

    /** 尖刺宽度包络：保持最初的长锥轮廓，用平滑曲线收尖，避免硬折线。 */
    private static double spireWidthShape(Spire s, double progress, long seed, int wx, int wz) {
        double taper = Math.pow(Math.max(0.0, 1.0 - progress), 0.92);
        double rounded = 0.035 + 0.965 * taper;
        double longNoise = valueNoise3(wx, 0, wz, 42, seed ^ 0xE5E5E5L, 9) - 0.5;
        return Math.max(0.035, rounded * (1.0 + 0.035 * longNoise));
    }

    /** 顶部积土厚度：根部(progress≈0)最厚可达 7 格，尖端(progress≈1)不积土；由低频噪声决定厚薄。返回 0 表示该列不积土。 */
    private static int spireDepositDepth(int wx, int wz, double progress, long seed) {
        // progress=0 是地下根部，progress=1 是天空尖端：根部厚、尖端无
        double tipFade = 1.0 - smoothStep(0.06, 0.62, progress);
        if (tipFade <= 0.0) return 0;
        double n = valueNoise3(wx, 0, wz, 20, seed ^ 0x5EEDD1D1L, 5) * 0.5 + 0.5;
        double x = Math.min(1.0, n * (0.35 + 0.65 * tipFade));
        if (x < 0.28) return 0;
        return 1 + (int) ((x - 0.20) * 8.0);
    }

    private static double smoothStep(double edge0, double edge1, double x) {
        double t = Math.max(0.0, Math.min(1.0, (x - edge0) / (edge1 - edge0)));
        return t * t * (3.0 - 2.0 * t);
    }

    private boolean fillColumn(WorldGenLevel level, int wx, int wz, int ground, Spire s, long seed) {
        double dx = wx - s.sx;
        double dz = wz - s.sz;
        double cosYaw = s.cosYaw;
        double sinYaw = s.sinYaw;

        double along = dx * cosYaw + dz * sinYaw;
        double t = -dx * sinYaw + dz * cosYaw;
        if (along < -8.0 || along > s.len + 8.0) return false;

        double progress = along / s.len;
        if (progress < 0.0 || progress > 1.0) return false;

        double shape = spireWidthShape(s, progress, seed, wx, wz);
        double halfW = Math.max(1.15, (s.width * 0.5) * shape);
        if (Math.abs(t) > halfW) return false;

        double H = Math.max(2.2, s.depth * shape);
        double cy = s.yRoot + s.rise * progress;

        // 顶面仍然基本平坦，只把两侧边缘圆掉约 1 格。
        double edgeRatio = Math.min(1.0, Math.abs(t) / halfW);
        double roundedEdge = 1.15 * Math.pow(edgeRatio, 4.0);
        double topY = cy + t * s.latTilt - roundedEdge;

        double tBot = s.bottomSkew * halfW;
        double yBot = cy - H;
        double yLeft = cy - halfW * s.latTilt;
        double yRight = cy + halfW * s.latTilt;
        double botY;
        double mBot;
        if (t < tBot) {
            double dt = tBot + halfW;
            mBot = dt != 0.0 ? (yBot - yLeft) / dt : 0.0;
            botY = yLeft + mBot * (t + halfW);
        } else {
            double dt = halfW - tBot;
            mBot = dt != 0.0 ? (yRight - yBot) / dt : 0.0;
            botY = yBot + mBot * (t - tBot);
        }
        if (botY >= topY) return false;

        // 低幅度的表面起伏只用来让风化边缘不完全平直。
        double swell = valueNoise3(wx, 0, wz, 56, seed ^ 0xE0E0E0EL, 3) - 0.5;
        double ripple = valueNoise3(wx, 0, wz, 26, seed ^ 0xAB12CD34L, 7) - 0.5;
        double colVar = valueNoise3(wx, 0, wz, 40, seed ^ 0xE3A51E7L, 4) - 0.5;
        double topFace = topY + colVar * 0.65 + swell * 0.45 + ripple * 0.25;
        double botFace = botY + swell * 0.45 + ripple * 0.20;

        int yLow = Math.max(level.getMinBuildHeight(), (int) Math.floor(botFace) - 1);
        int yHigh = Math.min(level.getMaxBuildHeight() - 1, (int) Math.ceil(topFace) + 1);
        if (yHigh < yLow) return false;

        boolean generated = false;
        int topRockY = Integer.MIN_VALUE;
        double topSlopeMod = Math.sqrt(1 + s.latTilt * s.latTilt);
        double botSlopeMod = Math.sqrt(1 + mBot * mBot);
        double shellThickness = 3.0;

        final int SAMPLE_STEP = 4;
        int sampleCount = (yHigh - yLow) / SAMPLE_STEP + 2;
        double[] n1s = new double[sampleCount];
        double[] n2s = new double[sampleCount];
        for (int si = 0; si < sampleCount; si++) {
            int ys = Math.min(yLow + si * SAMPLE_STEP, yHigh);
            n1s[si] = valueNoise3(wx, ys, wz, 30, seed ^ 0x51A2B3C4L, 0) - 0.5;
            n2s[si] = valueNoise3(wx, ys, wz, 12, seed ^ 0x51A2B3C4L, 1) - 0.5;
        }

        for (int y = yLow; y <= yHigh; y++) {
            int si0 = (y - yLow) / SAMPLE_STEP;
            int si1 = Math.min(si0 + 1, sampleCount - 1);
            double frac = ((y - yLow) % SAMPLE_STEP) / (double) SAMPLE_STEP;
            double n1 = n1s[si0] + (n1s[si1] - n1s[si0]) * frac;
            double n2 = n2s[si0] + (n2s[si1] - n2s[si0]) * frac;

            double dTop = (topFace - y) / topSlopeMod;
            double dBot = (y - botFace) / botSlopeMod;
            double sideDist = halfW - Math.abs(t);
            double minD = Math.min(Math.min(dTop, dBot), sideDist);

            // 表面侵蚀：稀疏、小幅、只挖壳层。尖端最后 10% 完全保护，避免断尖。
            double shellMask = 1.0 - smoothStep(0.35, shellThickness, Math.max(0.0, minD));
            double tipGuard = 1.0 - smoothStep(0.90, 0.995, progress);
            if (shellMask > 0.0 && tipGuard > 0.0) {
                double pitA = Math.max(0.0, n1 - 0.60);
                double pitB = Math.max(0.0, n2 - 0.70);
                double pit = pitA * 3.2 + pitB * 0.55;
                minD -= pit * shellMask * tipGuard;
            }
            if (minD <= 0.0) continue;

            BlockPos pos = new BlockPos(wx, y, wz);
            BlockState cur = level.getBlockState(pos);
            if (cur.is(Blocks.BEDROCK)) continue;
            if (!(cur.isAir() || cur.isSolid()
                    || cur.getFluidState().is(net.minecraft.tags.FluidTags.WATER)
                    || cur.getFluidState().is(net.minecraft.tags.FluidTags.LAVA))) {
                continue;
            }

            int idx = (int) Math.max(0, Math.min(CORE.length - 1, Math.round(
                    (1.0 - progress) * (CORE.length - 1) * 0.85 + n1 * 1.2 + n2 * 0.4)));
            boolean exposed = y >= ground;
            BlockState chosen;
            if (minD < 1.15) {
                // 露出地表的表面壳层：少量银/铁/铜矿石露头点缀
                chosen = exposed ? oreSurface(wx, y, wz, seed, SKIN[idx]) : SKIN[idx];
            } else {
                chosen = oreOrRock(wx, y, wz, progress, minD, exposed, CORE[idx], seed);
            }
            setBlock(level, pos, chosen);
            topRockY = y;
            generated = true;
        }

        if (generated && topRockY != Integer.MIN_VALUE) {
            int depth = spireDepositDepth(wx, wz, progress, seed);
            if (depth > 0) {
                boolean topGravel = valueNoise3(wx, 0, wz, 8, seed ^ 0x6A55A55L, 6) > 0.68;
                for (int i = 0; i < depth; i++) {
                    int y = topRockY + 1 + i;
                    if (y > level.getMaxBuildHeight() - 1) break;
                    BlockPos pos = new BlockPos(wx, y, wz);
                    BlockState cur2 = level.getBlockState(pos);
                    if (!(cur2.isAir() || cur2.getFluidState().is(net.minecraft.tags.FluidTags.WATER))) break;
                    setBlock(level, pos, i == 0
                            ? (topGravel ? Blocks.GRAVEL.defaultBlockState() : PaleLullabyBlocks.WITHERED_GRASS_BLOCK.get().defaultBlockState())
                            : PaleLullabyBlocks.WITHERED_DIRT.get().defaultBlockState());
                }
            }
        }
        return generated;
    }

    /** 尖刺露出地表的表面：少量银/铁/铜矿石露头（约 3%~8% 的表面方块），其余保持原皮肤。 */
    private static BlockState oreSurface(int wx, int y, int wz, long seed, BlockState fallback) {
        double n = valueNoise3(wx, y, wz, 7, seed ^ 0xD4C35E9L, 22);
        if (n > 0.968) return PaleLullabyBlocks.SILVER_ORE.get().defaultBlockState();
        if (n > 0.945) return Blocks.IRON_ORE.defaultBlockState();
        if (n > 0.922) return Blocks.COPPER_ORE.defaultBlockState();
        return fallback;
    }

    private static BlockState oreOrRock(int wx, int y, int wz, double progress, double depthIn,
                                        boolean exposed, BlockState fallback, long seed) {
        double cluster = valueNoise3(wx, y, wz, 18, seed ^ 0x9B77A11L, 21);
        if (cluster < 0.78 || depthIn < 1.7) return fallback;
        double ore = valueNoise3(wx, y, wz, 7, seed ^ 0xD4C35E9L, 22);
        // 露出地面部分：少量银/铁/铜露头，稀疏成团
        if (exposed) {
            if (ore > 0.940) return PaleLullabyBlocks.SILVER_ORE.get().defaultBlockState();
            if (ore > 0.915) return Blocks.IRON_ORE.defaultBlockState();
            if (ore > 0.890) return Blocks.COPPER_ORE.defaultBlockState();
        }
        if (y <= 20 && ore > 0.945) return Blocks.DIAMOND_ORE.defaultBlockState();
        if (y <= 32 && ore > 0.923) return Blocks.REDSTONE_ORE.defaultBlockState();
        if (y <= 80 && ore > 0.910) return Blocks.GOLD_ORE.defaultBlockState();
        if (y <= 96 && ore > 0.895) return Blocks.IRON_ORE.defaultBlockState();
        if (y <= 112 && ore > 0.890) return Blocks.COPPER_ORE.defaultBlockState();
        if (ore > 0.880) return Blocks.COAL_ORE.defaultBlockState();
        return fallback;
    }



    private static double valueNoise3(int wx, int wy, int wz, int grid, long seed, int salt) {
        int gx = Math.floorDiv(wx, grid);
        int gy = Math.floorDiv(wy, grid);
        int gz = Math.floorDiv(wz, grid);
        double fx = (double) (wx - gx * grid) / grid;
        double fy = (double) (wy - gy * grid) / grid;
        double fz = (double) (wz - gz * grid) / grid;
        double u = fx * fx * (3.0 - 2.0 * fx);
        double v = fy * fy * (3.0 - 2.0 * fy);
        double w = fz * fz * (3.0 - 2.0 * fz);
        double n000 = hash01(gx, gz, seed, salt + gy * 131);
        double n100 = hash01(gx + 1, gz, seed, salt + gy * 131);
        double n010 = hash01(gx, gz + 1, seed, salt + gy * 131);
        double n110 = hash01(gx + 1, gz + 1, seed, salt + gy * 131);
        double n001 = hash01(gx, gz, seed, salt + (gy + 1) * 131);
        double n101 = hash01(gx + 1, gz, seed, salt + (gy + 1) * 131);
        double n011 = hash01(gx, gz + 1, seed, salt + (gy + 1) * 131);
        double n111 = hash01(gx + 1, gz + 1, seed, salt + (gy + 1) * 131);
        double n00 = n000 + (n001 - n000) * v;
        double n10 = n100 + (n101 - n100) * v;
        double n01 = n010 + (n011 - n010) * v;
        double n11 = n110 + (n111 - n110) * v;
        double n0 = n00 + (n10 - n00) * u;
        double n1 = n01 + (n11 - n01) * u;
        return n0 + (n1 - n0) * w;
    }

    private static double hash01(int x, int z, long seed, int salt) {
        long h = hash((int) (x * 0x9E3779B9L + salt), (int) (z * 0xBF58476DL + salt * 31L), seed);
        return (double) (h & 0xFFFFFFL) / 0x1000000L;
    }

    private static long hash(int x, int z, long seed) {
        long h = seed;
        h ^= x * 0x9E3779B97F4A7C15L;
        h ^= z * 0xBF58476D1CE4E5B9L;
        h ^= h >>> 29;
        h *= 0x94D049BB133111EBL;
        h ^= h >>> 31;
        return h;
    }

    private record Spire(int sx, int sz, double len, double width, double depth, double rise,
                         double yaw, double cosYaw, double sinYaw, double phase, double latTilt,
                         double bottomSkew, double yRoot) {
    }

    private record SpireCell(int cx, int cz, long seed) {
    }
}
