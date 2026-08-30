package com.littlh.palelullaby;

import com.mojang.serialization.Codec;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.QuartPos;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerChunkCache;
import net.minecraft.server.level.WorldGenRegion;
import net.minecraft.world.level.LevelHeightAccessor;
import net.minecraft.world.level.WorldGenLevel;
import net.minecraft.world.level.biome.Biome;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.chunk.ChunkGenerator;
import net.minecraft.world.level.levelgen.Heightmap;
import net.minecraft.world.level.levelgen.RandomState;
import net.minecraft.world.level.levelgen.feature.Feature;
import net.minecraft.world.level.levelgen.feature.FeaturePlaceContext;
import net.minecraft.world.level.levelgen.feature.configurations.NoneFeatureConfiguration;

/**
 * 哥特悬崖：3D 隐式密度场生成。
 * 前端是尖锐而随机的前缘尖角；顶面总体平坦、前高后低缓慢倾斜，最后约 30% 由后端岩根插回原始地形。
 * 侧面从顶到脚自然收窄并局部凹陷（不是规则梯形）；内部有 1-2 个大椭球空腔形成拱门式挑空。
 * 钟乳石分两种挂法：从空腔顶部垂下、或从悬崖底缘垂下；表面只用低频剥落/细裂纹/竖直裂缝雕表皮。
 * 小概率生成在猩红花园；只要岩刃根部所在格在猩红花园内即可整体生成，不会被区块截断。
 */
public class GothicCliffFeature extends Feature<NoneFeatureConfiguration> {
    private static final int GRID = 192;
    private static final int SEARCH = 3;
    /** 自然生成锚点重试次数：提高“根部落进猩红花园”的命中率。 */
    private static final int POSITION_ATTEMPTS = 4;

    private static final ResourceKey<Biome> CRIMSON_GARDEN = ResourceKey.create(
            Registries.BIOME, ResourceLocation.fromNamespaceAndPath(PaleLullaby.MOD_ID, "crimson_garden"));

    /** 调试开关：在当前格强制生成一座悬崖，无视稀有度和群系门控。 */
    public static boolean FORCE = false;

    /** 每个 GRID x GRID 网格的悬崖组只计算一次，跨区块复用。FORCE 调试模式不走缓存。 */
    private static final ConcurrentHashMap<BladeCell, List<Blade>> BLADE_CACHE = new ConcurrentHashMap<>();
    private static final int BLADE_CACHE_LIMIT = 2048;

    /** 岩体内部：直接使用猩红花园所在主世界的天然地下构成，从地表石向深层深板岩渐变。 */
    private static final BlockState[] CORE = {
            Blocks.STONE.defaultBlockState(),
            Blocks.ANDESITE.defaultBlockState(),
            Blocks.STONE.defaultBlockState(),
            Blocks.DIORITE.defaultBlockState(),
            Blocks.DEEPSLATE.defaultBlockState(),
            Blocks.DEEPSLATE.defaultBlockState(),
    };
    /** 表层岩石：花岗岩/凝灰岩少量点缀，随深度渐变为深板岩。 */
    private static final BlockState[] SKIN = {
            Blocks.STONE.defaultBlockState(),
            Blocks.GRANITE.defaultBlockState(),
            Blocks.TUFF.defaultBlockState(),
            Blocks.ANDESITE.defaultBlockState(),
            Blocks.DEEPSLATE.defaultBlockState(),
            Blocks.DEEPSLATE.defaultBlockState(),
    };

    public GothicCliffFeature(Codec<NoneFeatureConfiguration> codec) {
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

        // /place feature debug placement: force a cliff at the current chunk for preview
        boolean debugPlace = !(level instanceof WorldGenRegion);
        List<Blade> blades = collectBlades(context.chunkGenerator(), level, randomState, baseX, baseZ, seed,
                debugPlace || FORCE);
        if (blades.isEmpty()) {
            return false;
        }
        boolean generated = false;
        for (int x = 0; x < 16; x++) {
            for (int z = 0; z < 16; z++) {
                int wx = baseX + x;
                int wz = baseZ + z;
                for (Blade b : blades) {
                    if (fillColumn3D(level, wx, wz, b, seed)) {
                        generated = true;
                    }
                }
            }
        }
        return generated;
    }

    /** 计算某列哥特悬崖的顶面高度（供结构生成复用，保证结构能落在悬崖平台上）。无悬崖时返回极小值。 */
    public static int cliffTopAt(ChunkGenerator chunkGenerator, RandomState randomState,
                                 LevelHeightAccessor heightAccessor, int wx, int wz, long seed) {
        int baseX = (wx >> 4) << 4;
        int baseZ = (wz >> 4) << 4;
        List<Blade> blades = collectBlades(chunkGenerator, heightAccessor, randomState, baseX, baseZ, seed, FORCE);
        int best = Integer.MIN_VALUE;
        for (Blade b : blades) {
            int top = platformTopColumn(wx, wz, b, seed);
            if (top > best) {
                best = top;
            }
        }
        return best;
    }

    private static int platformTopColumn(int wx, int wz, Blade b, long seed) {
        double dx = wx - b.sx;
        double dz = wz - b.sz;
        double cosYaw = Math.cos(b.yaw);
        double sinYaw = Math.sin(b.yaw);
        double along = dx * cosYaw + dz * sinYaw;
        double t = -dx * sinYaw + dz * cosYaw;
        if (along < -8.0 || along > b.len + 8.0) {
            return Integer.MIN_VALUE;
        }
        double progress = along / b.len;
        if (progress < 0.0 || progress > 1.0) {
            return Integer.MIN_VALUE;
        }
        double halfW = b.width * 0.5 * widthShapeAt(b, progress, seed, wx, wz);
        if (Math.abs(t - footprintOffset(b, progress)) > halfW + 4.0) {
            return Integer.MIN_VALUE;
        }
        return (int) Math.ceil(platformTop(b, along, t, seed, wx, wz)) + cliffDepositDepth(wx, wz, progress, seed);
    }

    /** 计算单个网格内的整组哥特悬崖（结果只依赖世界种子，跨区块完全一致，可缓存）。 */
    private static List<Blade> collectBlades(ChunkGenerator chunkGenerator, LevelHeightAccessor heightAccessor,
                                             RandomState randomState, int baseX, int baseZ, long seed, boolean force) {
        List<Blade> blades = new ArrayList<>();
        int cellX = Math.floorDiv(baseX, GRID);
        int cellZ = Math.floorDiv(baseZ, GRID);
        for (int gx = -SEARCH; gx <= SEARCH; gx++) {
            for (int gz = -SEARCH; gz <= SEARCH; gz++) {
                int cx = cellX + gx;
                int cz = cellZ + gz;
                // FORCE 调试模式不缓存，保证强制生成立刻生效
                BladeCell key = new BladeCell(cx, cz, seed);
                boolean forceCell = force && cx == cellX && cz == cellZ;
                List<Blade> cell = forceCell ? null : BLADE_CACHE.get(key);
                if (cell == null) {
                    cell = computeCellBlades(chunkGenerator, heightAccessor, randomState, cx, cz, seed,
                            forceCell, baseX + 8, baseZ + 8);
                    if (!forceCell) {
                        if (BLADE_CACHE.size() >= BLADE_CACHE_LIMIT) {
                            BLADE_CACHE.clear();
                        }
                        BLADE_CACHE.put(key, cell);
                    }
                }
                blades.addAll(cell);
            }
        }
        return blades;
    }

    private static List<Blade> computeCellBlades(ChunkGenerator chunkGenerator, LevelHeightAccessor heightAccessor,
                                                 RandomState randomState, int cx, int cz, long seed,
                                                 boolean forceCell, int forceX, int forceZ) {
        long h = hash(cx, cz, seed);
        if (!forceCell && (h & 0xFFL) >= 51L) {
            return List.of(); // 约 20% 的格有哥特悬崖
        }
        double baseYaw = ((h >>> 32) % 360L) * Math.PI / 180.0;
        double perpX = Math.cos(baseYaw + Math.PI / 2.0);
        double perpZ = Math.sin(baseYaw + Math.PI / 2.0);
        int n = forceCell ? 1 : 1 + (int) ((h >>> 24) % 2L); // 1-2 座

        // 自然生成：根部必须落在猩红花园内，锚点重试数次提高命中率；
        // FORCE 调试：直接用玩家所在区块中心作为根部，保证放置后立刻可见。
        for (int attempt = 0; attempt < POSITION_ATTEMPTS; attempt++) {
            int ax;
            int az;
            if (forceCell) {
                ax = forceX;
                az = forceZ;
            } else {
                long ah = hash(cx * 7 + attempt * 13, cz * 11 + attempt * 17, seed ^ 0xFEEDCAFEL);
                ax = cx * GRID + 44 + (int) ((ah >>> 16) % (GRID - 88L));
                az = cz * GRID + 44 + (int) ((ah >>> 24) % (GRID - 88L));
                if (!isCrimsonGarden(chunkGenerator, randomState, ax, az)) {
                    continue;
                }
            }
            List<Blade> result = new ArrayList<>(n);
            for (int i = 0; i < n; i++) {
                long sh = hash(cx * 31 + i * 7, cz * 17 + i * 11, seed);
                double spacing = 45 + ((sh >>> 8) % 41L);
                double off = (i - (n - 1) * 0.5) * spacing;
                // 世界上只生成大型悬崖，整体尺寸已收小（75~120 长、45~72 宽、28~50 深）
                double len = (150 + ((sh >>> 32) % 90L)) * 0.5;    // 75~120
                double width = (90 + ((sh >>> 40) % 55L)) * 0.5;   // 45~72
                double depth = (68 + ((sh >>> 48) % 52L)) * 0.5;   // 34~60（比原来更高一些）
                double yaw = baseYaw + (i - (n - 1) * 0.5) * 0.4
                        + (((sh >>> 56) % 17L) - 8) * Math.PI / 180.0;
                double phase = (sh & 0xFFL) * 0.01;

                int sx;
                int sz;
                if (forceCell) {
                    // 让悬崖中段穿过玩家所在区块中心
                    sx = ax - (int) Math.round(Math.cos(yaw) * len * 0.5);
                    sz = az - (int) Math.round(Math.sin(yaw) * len * 0.5);
                } else {
                    sx = ax + (int) Math.round(off * perpX);
                    sz = az + (int) Math.round(off * perpZ) + (int) (((sh >>> 16) % 121L) - 60);
                }

                int groundRoot = safeBaseHeight(chunkGenerator, heightAccessor, randomState, sx, sz);
                // 后端接地岩根尽头的地表高度：让尾巴延长段真正插进后端的地里
                int tailX = sx + (int) Math.round(Math.cos(yaw) * len * 1.15);
                int tailZ = sz + (int) Math.round(Math.sin(yaw) * len * 1.15);
                int groundTail = safeBaseHeight(chunkGenerator, heightAccessor, randomState, tailX, tailZ);
                // 顶部略倾斜的草地平台：高出地表 5~11 格
                double topBase = groundRoot + 7 + ((sh >>> 24) % 9L);   // 平台抬高 7~15 格
                // 顶面沿崖长方向从高到低的总下倾量：每座悬崖随机 8~14 格，不统一
                long th = hash(cx * 3 + i * 5, cz * 7 + i * 9, seed ^ 0x5A17CL);
                double topTilt = 8 + ((th >>> 8) % 7L);
                result.add(buildBlade(cx, cz, i, seed, sx, sz, len, width, depth, yaw, phase,
                        groundRoot, groundTail, topBase, topTilt));
            }
            return result;
        }
        return List.of();
    }

    /** 构建单座悬崖 Blade：不规则尖角脚印、尾巴小径、空腔与悬挂钟乳石全部在此一次性算好。 */
    private static Blade buildBlade(int cx, int cz, int bladeIndex, long seed,
                                    int sx, int sz, double len, double width, double depth,
                                    double yaw, double phase, int groundRoot, int groundTail,
                                    double topBase, double topTilt) {
        // 脚印不规则：前端尖角横向错位大、中部最宽点、尾端偏移 三个控制点，中心线随机横移
        long qh = hash(cx * 97 + bladeIndex * 5, cz * 61 + bladeIndex * 17, seed ^ 0x9A3B2C1DL);
        double planFrontOff = (((qh >>> 40) % 121L) - 60) / 100.0;  // 前端尖角偏移 -0.60~0.60
        double planPeak = 1.02 + ((qh >>> 16) % 21L) / 100.0;       // 中部最宽点 1.02~1.22
        double planPeakP = 0.40 + ((qh >>> 32) % 26L) / 100.0;      // 最宽点位置 0.40~0.65
        double planBack = 0.92 + ((qh >>> 24) % 26L) / 100.0;       // 尾端半宽 0.92~1.17
        double planOffB = (((qh >>> 48) % 61L) - 30) / 100.0;       // 中部横向偏移 -0.30~0.30
        double planOffC = (((qh >>> 56) % 61L) - 30) / 100.0;       // 尾端横向偏移 -0.30~0.30

        // 尾巴小径：约 1/4 概率生成一条沿刀刃方向的沙砾/淤泥小路
        long trh = hash(cx * 53 + bladeIndex * 3, cz * 73 + bladeIndex * 11, seed ^ 0xC0FFEE1L);
        boolean hasTrail = (trh & 0xFFL) < 64L;
        double trailOff = (((trh >>> 8) % 61L) - 30) / 100.0;

        List<Cavity> cavities = buildCavities(cx, cz, bladeIndex, seed, len, width, depth, topTilt);
        List<Hanging> hangings = buildHangings(cx, cz, bladeIndex, seed, len, width, depth, topTilt, cavities);
        return new Blade(sx, sz, len, width, depth, yaw, phase, topBase, topTilt, groundRoot, groundTail,
                hasTrail, trailOff, planFrontOff, planPeak, planPeakP, planBack, planOffB, planOffC,
                cavities, hangings);
    }

    /** 空洞：1-2 个大型空腔沿崖长方向拉长并横跨整个宽度，把不与尾巴相连的面（前端+左右两侧）都掏空，
     *  形成三面可见的拱门式挑空；尾端连接段保持实心。最高点相对前端顶面只留 1~5 格，底端切穿岩体底部。 */
    private static List<Cavity> buildCavities(int cx, int cz, int bladeIndex, long seed,
                                              double len, double width, double depth, double topTilt) {
        long h = hash(cx * 131 + bladeIndex * 17, cz * 233 + bladeIndex * 29, seed ^ 0xC4A717D19L);
        int n = 1 + (int) ((h >>> 8) % 2L);
        List<Cavity> list = new ArrayList<>(n);
        for (int k = 0; k < n; k++) {
            long kh = hash(cx + k * 13, cz + k * 37, seed ^ (bladeIndex * 1000L + 0x51A2B3C4L));
            // 沿长方向：中心在前端，半轴大到把前端整个切穿；第二个空腔略靠后形成里厅；
            // 用 0.55 上限保证空腔绝不侵入尾巴连接段（尾巴必须保持实心接地）
            double alongFrac = 0.05 + ((kh >>> 16) % 12L) / 100.0 + k * 0.08;
            double along = len * alongFrac;
            double a = Math.min((0.55 - alongFrac) * len,
                    len * (0.32 + ((kh >>> 40) % 14L) / 100.0));
            // 横向：半轴 0.70~0.82×宽，必须大于主体最大半宽（约 0.61×宽），左右两侧崖壁都被掏穿；
            // 中心在 density 里按刀刃中心线(cFoot)对齐，跟随脚印偏移，保证两侧都能露出空腔口
            double side = ((kh >>> 24) & 1L) == 0L ? -1.0 : 1.0;
            double t = width * 0.5 * (((kh >>> 25) % 21L) - 10) / 100.0;
            double b = width * (0.70 + ((kh >>> 48) % 13L) / 100.0);
            // 竖直：更高更大的空腔。前端顶面会随 topTilt 抬高，空腔中心同步抬高，
            // 使空腔最高点相对实际顶面仍保持 1~5 格，底端切穿岩体底部
            double topThick = 1 + (int) ((kh >>> 32) % 5L);
            double c = depth * 0.88;
            double lift = topTilt * (1.0 - alongFrac);
            double y = lift - (c + topThick);
            double jitter = (kh & 0x7FL) / 100.0;
            list.add(new Cavity(along, t, y, a, b, c, side, 0.0, jitter));
        }
        return list;
    }

    /** 悬挂钟乳石：3-6 根，一部分从空腔顶部向下垂（在挑空里可见），一部分挂在悬崖底缘/侧边下缘。 */
    private static List<Hanging> buildHangings(int cx, int cz, int bladeIndex, long seed,
                                               double len, double width, double depth,
                                               double topTilt, List<Cavity> cavities) {
        long h = hash(cx * 191 + bladeIndex * 3, cz * 97 + bladeIndex * 23, seed ^ 0x1A2B3C4D5L);
        int n = 3 + (int) ((h >>> 8) % 4L);
        List<Hanging> list = new ArrayList<>(n);
        for (int k = 0; k < n; k++) {
            long kh = hash(cx + k * 7, cz + k * 11, seed ^ (bladeIndex * 77L + 0x6E7F8A9BL));
            double bend = (((kh >>> 56) % 80L) - 40) / 16.0;
            int mode = ((kh >>> 24) & 1L) == 0L ? 0 : 1;
            if (mode == 0 && !cavities.isEmpty()) {
                // 挂在空腔顶部：位置落在该空腔水平投影内，从空腔顶向下垂进挑空
                Cavity c = cavities.get((int) ((kh >>> 40) % cavities.size()));
                double u = (((kh >>> 8) % 101L) / 100.0 - 0.5) * 1.1;
                double v = (((kh >>> 16) % 101L) / 100.0 - 0.5) * 1.1;
                double along = c.along + u * c.a * 0.5;
                double t = c.t + v * c.b * 0.55;
                double hLen = 10 + ((kh >>> 32) % 21L);    // 10-30 格（更夸张）
                double r = 0.9 + ((kh >>> 48) % 22L) / 10.0;   // 0.9-3.0
                list.add(new Hanging(along, t, 0.0, hLen, r, bend, 0, c.y + c.c));
            } else {
                // 挂在悬崖底缘：贴近前端的侧边下缘，从底面向下垂
                double along = len * (0.02 + ((kh >>> 8) % 20L) / 100.0);
                double side = ((kh >>> 16) & 1L) == 0L ? -1.0 : 1.0;
                double t = width * 0.5 * side * (0.45 + ((kh >>> 32) % 41L) / 100.0);
                double hLen = 8 + ((kh >>> 40) % 15L);     // 8-22 格（更夸张）
                double r = 0.9 + ((kh >>> 48) % 22L) / 10.0;   // 0.9-3.0
                double embed = 1.0 + ((kh >>> 24) % 4L);
                list.add(new Hanging(along, t, embed, hLen, r, bend, 1, 0.0));
            }
        }
        return list;
    }

    /** 查询地表高度。世界生成阶段查询远处列时区块可能尚未就绪，异常回退海平面，避免整个特征放置失败。 */
    private static int safeBaseHeight(ChunkGenerator chunkGenerator, LevelHeightAccessor heightAccessor,
                                      RandomState randomState, int x, int z) {
        try {
            return chunkGenerator.getBaseHeight(
                    x, z, Heightmap.Types.WORLD_SURFACE_WG, heightAccessor, randomState);
        } catch (RuntimeException ex) {
            return 64;
        }
    }

    /** 平台顶面：总体平坦，只保留前高后低的缓慢倾斜；最后约 30% 由后端岩根降到地面。
     *  尾巴的斜坡不再是一条均匀的纸：下降起始位置随横向扭转错动，下降幅度叠加三个方向
     *  （沿刀刃/横跨刀刃/斜向）的低频噪声波浪，整体像被多方向扭曲的纸，而不是平整斜板。 */
    private static double platformTop(Blade b, double along, double t, long seed, int wx, int wz) {
        double progress = along / b.len;
        // 前端(progress≈0)加 topTilt 最高，整体缓慢下倾；主体不再中途塌陷
        double tilt = b.topTilt * (1.0 - progress);
        double base = b.topBase + tilt
                + 0.55 * Math.sin(along * 0.010 + b.phase * 3.7)
                + 0.4 * Math.sin(t * 0.018 + b.phase * 1.7);
        // 后端接地岩根：约 60% 处开始下降，到 progress≈1.05 已插进后端地表 3 格，
        // 之后延长段继续收窄，尾巴彻底埋进地里（后端地表异常升高时保持不下陷）
        double rootDrop = smoothStep(0.60, 1.05, progress);
        if (rootDrop <= 0.0) return base;
        double drop = Math.max(0.0, base - (b.groundTail - 3.0));
        if (drop <= 0.0) return base;
        // 1) 纸的扭转：斜坡起点/终点随横向噪声错动，局部坡度有陡有缓
        double twist = (valueNoise3(wx, 0, wz, 37, seed ^ 0x5E9F0A1BL, 31) - 0.5) * 0.14;
        double rootDropW = smoothStep(0.58 + twist * 0.8, 1.04 + twist * 0.3, progress);
        // 2) 多方向波浪：沿刀刃/横跨刀刃/斜向三组正弦波 + 低频噪声扭曲。
        //    正弦保证每个悬崖都可见扭曲，噪声保证不整齐；波长远大于 16 格且方向随刀刃旋转，
        //    不会形成"每个区块一条"的整齐台阶。
        double waveBlocks = 3.5 * Math.sin(along * 0.030 + b.phase * 5.3)
                + 2.0 * Math.sin(along * 0.070 + b.phase * 2.9)
                + 2.5 * Math.sin(t * 0.080 + b.phase * 3.1)
                + 1.5 * Math.sin(t * 0.160 + b.phase * 1.3)
                + 2.0 * Math.sin((along + t) * 0.045 + b.phase * 4.7)
                + (valueNoise3(wx, 0, wz, 31, seed ^ 0x71A2B3C4L, 7) - 0.5) * 4.0;
        double waveFrac = waveBlocks / Math.max(12.0, drop);
        waveFrac = Math.max(-0.20, Math.min(0.20, waveFrac));
        // 波浪在斜坡起点与接地终点都归零，保证平台边和地面接缝不被扰动
        double waveFade = Math.sin(Math.PI * Math.min(1.0, rootDropW));
        double warped = rootDropW + waveFrac * waveFade;
        warped = Math.max(0.0, Math.min(1.10, warped));
        return base - drop * warped;
    }

    /** 平面包络：前端收成尖锐而不规则的前缘尖角，主体保持宽阔厚实，尾端轻微收窄交给后端岩根。 */
    private static double widthShapeAt(Blade b, double progress, long seed, int wx, int wz) {
        double edgeNoise = valueNoise3(wx, 0, wz, 34, seed ^ 0x7A31C9L, 11) - 0.5;
        double jag = valueNoise3(wx, 0, wz, 11, seed ^ 0x91E7A3L, 17) - 0.5;
        // 前缘尖角：progress→0 快速收成尖锐尖点，尖点宽度带噪声抖动，形状多样随机
        double tipReach = 0.10 + 0.05 * b.phase;
        double tip = smoothStep(0.0, tipReach, progress);
        double tipW = 0.14 + 0.10 * Math.abs(edgeNoise);
        // 主体：宽阔厚实的岩台，缓慢起伏
        double body = b.planPeak + 0.055 * edgeNoise;
        double w = tipW + (body - tipW) * tip;
        // 随机锯齿：让轮廓不规则，不改变主体量级
        w *= 1.0 + 0.05 * jag;
        // 尾端延长段收窄：后端岩根降到地面后继续收窄，直到插进地里；
        // 收窄程度随横向噪声波动，让尾巴两边像被风吹皱的纸边而不是均匀收细
        double tailNarrow = smoothStep(0.90, 1.28, progress)
                * (1.0 + 0.30 * (valueNoise3(wx, 0, wz, 15, seed ^ 0xA1B2C3D4L, 41) - 0.5));
        w *= 1.0 - 0.95 * Math.max(0.0, Math.min(1.0, tailNarrow));
        return Math.max(0.05, w);
    }

    /** 脚印中心线横向偏移：前端尖角 → 中部 → 尾端 三个控制点折线过渡，形成歪斜不规则的轮廓。 */
    private static double footprintOffset(Blade b, double progress) {
        double off;
        if (progress < b.planPeakP) {
            double q = smoothStep(0.0, b.planPeakP, progress);
            off = b.planFrontOff + (b.planOffB - b.planFrontOff) * q;
        } else {
            double q = smoothStep(b.planPeakP, 1.0, progress);
            off = b.planOffB + (b.planOffC - b.planOffB) * q;
        }
        return b.width * off;
    }

    private static double smoothStep(double edge0, double edge1, double x) {
        double t = Math.max(0.0, Math.min(1.0, (x - edge0) / (edge1 - edge0)));
        return t * t * (3.0 - 2.0 * t);
    }

    /** 岩台底面深度：前缘略薄利于挑空，主体厚实，尾巴不收拢、保持深厚。 */
    private static double bottomDepthAt(Blade b, double progress, long seed, int wx, int wz) {
        double largeErosion = valueNoise3(wx, 0, wz, 52, seed ^ 0x91E7A3L, 8) - 0.5;
        double front = 1.0 - smoothStep(0.0, 0.35, progress);
        double d = b.depth * (0.62 * front + 1.0 * (1.0 - front) + 0.16 * largeErosion);
        return Math.max(2.0, d);
    }

    /** 顶部堆积层厚度：像尖刺那样按距离堆积——尾端(接地处)最厚可达 7 格，前端(挑空边)最薄也至少 1 格。
     *  由低频噪声决定厚薄。 */
    private static int cliffDepositDepth(int wx, int wz, double progress, long seed) {
        // progress=0 是前端挑空边，progress=1 是尾端接地：尾端厚、前端薄但不少于 1 格
        double frontFade = smoothStep(0.10, 0.75, progress);
        double n = valueNoise3(wx, 0, wz, 27, seed ^ 0x5EEDD1D1L, 5) * 0.5 + 0.5;
        double x = Math.min(1.0, n * (0.35 + 0.65 * frontFade));
        return 1 + Math.max(0, (int) ((x - 0.20) * 8.0));
    }

    /** 按列填充：该列只计算自身的 y 范围，跨区块不会重复全量生成。 */
    private boolean fillColumn3D(WorldGenLevel level, int wx, int wz, Blade b, long seed) {
        double dx = wx - b.sx;
        double dz = wz - b.sz;
        double cosYaw = Math.cos(b.yaw);
        double sinYaw = Math.sin(b.yaw);
        // along=0 是尖端，along=len 是地下粗壮的根部
        double along = dx * cosYaw + dz * sinYaw;
        double t = -dx * sinYaw + dz * cosYaw;
        if (along < -8.0 || along > b.len * 1.35 + 8.0) return false;
        double progress = along / b.len;
        if (progress < 0.0 || progress > 1.35) return false;
        double halfW = b.width * 0.5 * widthShapeAt(b, progress, seed, wx, wz);
        double cFoot = footprintOffset(b, progress);
        if (Math.abs(t - cFoot) > halfW + 18.0) return false;

        double topY = platformTop(b, along, t, seed, wx, wz);
        double bottomY = topY - bottomDepthAt(b, progress, seed, wx, wz);

        // 列级 y 下限：侵蚀底面，若该列附近有悬挂柱则延伸到柱尖
        int yLow = (int) Math.floor(bottomY - 2.0);
        // 只有挂在底缘的钟乳石(mode=1)需要延伸到主体底面以下；空腔顶挂的(mode=0)在主体内部，无需扩展
        for (Hanging h : b.hangings) {
            if (h.mode == 1 && Math.abs(along - h.along) < h.r + 2.0 && Math.abs(t - h.t) < h.r + 2.0 + Math.abs(h.bend)) {
                yLow = Math.min(yLow, (int) Math.floor(bottomY - h.embed - h.len - 2.0));
            }
        }
        int yHigh = (int) Math.ceil(topY + 3.0); // 预留顶部堆积层高度
        yLow = Math.max(level.getMinBuildHeight(), yLow);
        yHigh = Math.min(level.getMaxBuildHeight() - 1, yHigh);
        if (yHigh < yLow) return false;

        boolean generated = false;
        int topRockY = Integer.MIN_VALUE;
        for (int y = yLow; y <= yHigh; y++) {
            double d = density(b, along, t, progress, halfW, topY, bottomY, y, seed, wx, wz);
            if (d <= 0.0) continue;
            BlockPos pos = new BlockPos(wx, y, wz);
            BlockState cur = level.getBlockState(pos);
            if (cur.is(Blocks.BEDROCK)) continue;
            if (!(cur.isAir() || cur.isSolid())) continue;

            // 材质按实际深度渐变：地表以上石质，深入地下渐变为深板岩（对应群系天然构成）
            double depthFrac = Math.max(0.0, Math.min(1.0, (b.groundRoot - y) / 52.0 + 0.06));
            double grad = depthFrac * (CORE.length - 1) * 0.9
                    + (valueNoise3(wx, y, wz, 26, seed ^ 0x51A2B3C4L, 0) - 0.5) * 1.7;
            int idx = (int) Math.max(0, Math.min(CORE.length - 1, Math.round(grad)));

            if (d < 1.8) {
                // 表面：少量淤泥覆盖，露出石质岩面（顶部不再直接铺草/泥土，由堆积层负责）
                double mud = valueNoise3(wx, y, wz, 18, seed ^ 0x5EEDL, 2) - 0.5;
                setBlock(level, pos, mud > 0.12
                        ? PaleLullabyBlocks.SOAKED_MUD.get().defaultBlockState()
                        : SKIN[idx]);
            } else {
                setBlock(level, pos, CORE[idx]);
            }
            topRockY = y;
            generated = true;
        }

        if (generated && topRockY != Integer.MIN_VALUE) {
            int depth = cliffDepositDepth(wx, wz, progress, seed);
            if (depth > 0) {
                boolean topGrass = valueNoise3(wx, 0, wz, 14, seed ^ 0x6A6DL, 3) > 0.30;
                // 自上而下放置：最上层是草方块（或土径），下面才是泥土，避免泥土盖住草/土径
                for (int i = 0; i < depth; i++) {
                    int y = topRockY + depth - i;
                    if (y > level.getMaxBuildHeight() - 1) break;
                    BlockPos pos = new BlockPos(wx, y, wz);
                    BlockState cur2 = level.getBlockState(pos);
                    if (!(cur2.isAir() || cur2.getFluidState().is(net.minecraft.tags.FluidTags.WATER))) break;
                    // 堆积：最上层是群系草方块（淤泥草），下面是群系泥土（浸染淤泥）
                    BlockState st = PaleLullabyBlocks.SOAKED_MUD.get().defaultBlockState();
                    if (i == 0) {
                        st = topGrass ? PaleLullabyBlocks.SOAKED_MUD_GRASS.get().defaultBlockState()
                                      : PaleLullabyBlocks.SOAKED_MUD.get().defaultBlockState();
                        // 尾巴小径：概率生成一条沿刀刃方向蜿蜒的黑石小径，宽约 2~3 格
                        if (b.hasTrail) {
                            double trailZone = smoothStep(0.50, 0.92, progress);
                            if (trailZone > 0.0) {
                                double tCenter = b.trailOff * b.width * 0.5
                                        + 3.0 * Math.sin(along * 0.045 + b.phase * 6.0)
                                        + 2.0 * Math.sin(along * 0.11 + b.phase * 3.0);
                                double dist = Math.abs(t - tCenter);
                                if (dist < 1.0 + 0.5 * trailZone) {
                                    st = Blocks.BLACKSTONE.defaultBlockState();
                                }
                            }
                        }
                    }
                    setBlock(level, pos, st);
                }
            }
        }

        if (generated) {
            plantSideThorns(level, b, seed, wx, wz, along, t, progress, halfW, topY);
        }
        return generated;
    }

    /** 在悬崖侧面随机生长一小簇猩红荆棘/蔷薇：根部落在崖底地表（群系淤泥草），向上攀附 3~5 格。 */
    private void plantSideThorns(WorldGenLevel level, Blade b, long seed, int wx, int wz,
                                 double along, double t, double progress, double halfW, double topY) {
        if (progress < 0.05 || progress > 0.97) return;
        double cFoot = footprintOffset(b, progress);
        if (Math.abs(t - cFoot) < halfW * 0.30) return; // 只贴侧壁，不在平台中部
        long vh = hash(wx * 31 + b.sx, wz * 17 + b.sz, seed ^ 0x7A1CE5D1L);
        if ((vh & 0xFFL) >= 22L) return; // 约 8.6% 的侧壁列生成一簇

        // 侧面外法线方向（t 轴正方向在世界坐标的朝向）
        double sign = Math.signum(t);
        int nx = wx + (int) Math.round(-Math.sin(b.yaw) * sign);
        int nz = wz + (int) Math.round(Math.cos(b.yaw) * sign);
        if (nx == wx && nz == wz) return;

        int baseY = b.groundRoot + 1;
        int topClamp = (int) Math.floor(topY) - 1;
        if (topClamp - baseY < 5) return;
        int height = 3 + (int) ((vh >>> 8) % 3L); // 3-5 格
        int endY = Math.min(baseY + height - 1, topClamp);

        // 攀附的所有格必须位于悬崖之外（空气），且根部下方是群系地表，否则跳过这一列
        try {
            if (!CrimsonThornBlock.isSupport(level.getBlockState(new BlockPos(nx, baseY, nz).below()))) {
                return;
            }
            double nxA = nx - b.sx;
            double nzA = nz - b.sz;
            double nAlong = nxA * Math.cos(b.yaw) + nzA * Math.sin(b.yaw);
            double nT = -nxA * Math.sin(b.yaw) + nzA * Math.cos(b.yaw);
            double nProgress = nAlong / b.len;
            if (nProgress < 0.0 || nProgress > 1.0) return;
            double nHalfW = b.width * 0.5 * widthShapeAt(b, nProgress, seed, nx, nz);
            if (Math.abs(nT) > nHalfW + 18.0) return;
            double nTop = platformTop(b, nAlong, nT, seed, nx, nz);
            double nBottom = nTop - bottomDepthAt(b, nProgress, seed, nx, nz);
            for (int y = baseY; y <= endY; y++) {
                double d = density(b, nAlong, nT, nProgress, nHalfW, nTop, nBottom, y, seed, nx, nz);
                if (d > 0.0) return;
                if (!level.isEmptyBlock(new BlockPos(nx, y, nz))) return;
            }
        } catch (RuntimeException ex) {
            return; // 邻块未就绪时静默跳过，绝不影响世界生成
        }
        BlockState defaultThorn = PaleLullabyBlocks.CRIMSON_THORN.get().defaultBlockState();
        for (int y = baseY; y <= endY; y++) {
            BlockPos pos = new BlockPos(nx, y, nz);
            setBlock(level, pos, CrimsonThornBlock.getStateWithConnections(level, pos, defaultThorn));
        }
        for (int y = baseY; y <= endY; y++) {
            CrimsonThornBlock.syncConnections(level, new BlockPos(nx, y, nz));
        }
        // 顶部随机结一朵蔷薇（约一半概率）
        if ((vh & 0x100L) == 0L) {
            BlockPos topPos = new BlockPos(nx, endY, nz);
            for (Direction dir : Direction.Plane.HORIZONTAL) {
                BlockPos cand = topPos.relative(dir);
                if (cand.getX() == wx && cand.getZ() == wz) continue; // 朝崖壁的一面不放
                if (!level.isEmptyBlock(cand)) continue;
                BlockState rose = PaleLullabyBlocks.CRIMSON_ROSE.get().defaultBlockState()
                        .setValue(CrimsonRoseBlock.HAS_NECTAR, (vh & 0x200L) != 0L)
                        .setValue(CrimsonThornAttachmentBlock.FACING, dir.getOpposite());
                setBlock(level, cand, rose);
                break;
            }
        }
    }

    /** 3D 隐式密度：厚岩台 - 大空腔 + 悬挂钟乳石 + 表皮风化，正值为岩石。
     *  前端是尖锐随机的前缘，侧面从顶到脚自然收窄并局部凹陷，内部大面积挑空，底缘垂钟乳石。 */
    private double density(Blade b, double along, double t, double progress, double halfW,
                           double topY, double bottomY, double y, long seed, int wx, int wz) {
        int fy = (int) Math.floor(y);
        double f = Math.max(0.0, Math.min(1.0, (y - bottomY) / Math.max(0.01, topY - bottomY)));
        // 上宽下窄：基础收窄 + 三维噪声驱动局部收缩/凹陷，越往下波动越大，不是规则梯形
        double nS = valueNoise3(wx, fy, wz, 26, seed ^ 0x55AAL, 33) - 0.5;
        double shrink = 1.0 - 0.30 * (1.0 - f);
        shrink += nS * 0.12 * (0.30 + 0.70 * (1.0 - f));
        shrink = Math.max(0.52, Math.min(1.06, shrink));
        // 边缘随机锯齿：让轮廓不规则，不改变主体量级
        double jag = (valueNoise3(wx, fy, wz, 9, seed ^ 0x77E1L, 27) - 0.5) * 0.06;
        double cFoot = footprintOffset(b, progress);
        double sideDist = halfW * shrink * (1.0 + jag) - Math.abs(t - cFoot + jag * halfW * 0.6);
        double topDist = topY - y;
        double bottomDist = y - bottomY;
        double d = Math.min(Math.min(topDist, bottomDist), sideDist);
        if (d < -48.0) return d;

        // 表皮风化：侧面 1~4 格、底部 1~5 格、顶部边缘 1~2.5 格碎裂；
        // 低频剥落 + 细裂纹 + 竖直裂缝三种尺度，幅度收紧避免把岩体掏断。
        double sideMask = 1.0 - smoothStep(1.0, 4.0, sideDist);
        double bottomMask = 1.0 - smoothStep(1.0, 5.0, bottomDist);
        double topMask = 1.0 - smoothStep(0.5, 2.5, topDist);
        double surfaceMask = Math.max(sideMask, Math.max(bottomMask, topMask));
        if (surfaceMask > 0.0) {
            double nLarge = valueNoise3(wx, fy, wz, 30, seed ^ 0x12A7L, 2);
            double nSmall = valueNoise3(wx, fy, wz, 12, seed ^ 0x83C1L, 4);
            double nCrack = valueNoise3(wx, fy, wz, 22, seed ^ 0x3E5DL, 9);
            double pit = Math.max(0.0, nLarge - 0.68) * 1.8
                    + Math.max(0.0, nSmall - 0.78) * 0.7
                    + Math.max(0.0, nCrack - 0.85) * 1.0;
            d -= pit * surfaceMask;
            // 底部轻微掏蚀：下缘内凹，不掏断岩体
            double undercut = Math.max(0.0, nLarge - 0.70) * 1.5;
            d -= undercut * bottomMask * bottomMask;
        }

        // 大空腔：1-2 个椭球贴壁切入，在前端形成大面积拱门式挑空（不再生成岩棚）
        for (Cavity c : b.cavities) {
            double ca = along - c.along;
            // 相对刀刃中心线取横向，避免脚印横向偏移导致空腔偏向一侧而掏不穿
            double ct = (t - cFoot) - c.t;
            double cy = y - (b.topBase + c.y);
            // 边缘侧翘起：在 (t,y) 平面绕空腔中心旋转，靠近崖壁的一端抬高
            double cs = c.side * ct;
            double ctR = cs * Math.cos(c.tilt) + cy * Math.sin(c.tilt);
            double cyR = -cs * Math.sin(c.tilt) + cy * Math.cos(c.tilt);
            // 不规则抖动：用噪声局部放大/缩小空腔边界，形成不规则岩洞轮廓
            double wobble = 1.0 + 0.15 * (valueNoise3(wx, (int) Math.floor(y), wz, 13,
                    seed ^ (0xC0FFEEL + (long) (c.jitter * 1000.0)), 19) - 0.5);
            double C = (ca * ca) / (c.a * c.a) + (ctR * ctR) / (c.b * c.b) + (cyR * cyR) / (c.c * c.c);
            C /= wobble * wobble;
            if (C < 1.0) {
                d -= (1.0 - C) * 54.0;
            } else if (C < 1.45) {
                d -= (1.45 - C) * 10.0;
            }
        }

        // 悬挂钟乳石：mode=0 从空腔顶部向下垂（挑空内可见），mode=1 从悬崖底缘向下垂；只在主体宽度内生效
        for (Hanging h : b.hangings) {
            double attachY = h.mode == 0 ? (b.topBase + h.attach) : (bottomY - h.embed);
            double dy = attachY - y;
            if (dy < 0.0 || dy > h.len) continue;
            if (Math.abs(t - footprintOffset(b, progress)) > halfW + 0.5) continue;
            double q = dy / h.len;
            double tCenter = h.t + h.bend * Math.sin(dy * 0.10 + b.phase * 2.0);
            double nA = valueNoise3(wx, (int) Math.floor(y), wz, 22, seed ^ 0x12A7L, 2) - 0.5;
            double nB = valueNoise3(wx, (int) Math.floor(y), wz, 9, seed ^ 0x83C1L, 4) - 0.5;
            double erosion = 1.0 + nA * 0.16 + nB * 0.05;
            double taper = 1.0 - 0.85 * smoothStep(0.06, 1.0, q);
            double rr = Math.max(0.5, h.r * taper * erosion);
            if (q > 0.90) rr *= Math.max((1.0 - q) / 0.10, 0.10);
            double ddx = along - h.along;
            double ddt = t - tCenter;
            double dist2 = ddx * ddx + ddt * ddt;
            double r2 = rr * rr;
            if (dist2 < r2) {
                d += (r2 - dist2) / r2 * 82.0;
            }
        }
        return d;
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

    private static boolean isCrimsonGarden(ChunkGenerator chunkGenerator, RandomState randomState, int x, int z) {
        return chunkGenerator.getBiomeSource().getNoiseBiome(
                QuartPos.fromBlock(x), QuartPos.fromBlock(100), QuartPos.fromBlock(z), randomState.sampler())
                .is(CRIMSON_GARDEN);
    }

    /** 稳定网格哈希：相同 (x, z, seed) 永远得到相同值。 */
    private static long hash(int x, int z, long seed) {
        long h = seed;
        h ^= x * 0x9E3779B97F4A7C15L;
        h ^= z * 0xBF58476D1CE4E5B9L;
        h ^= h >>> 29;
        h *= 0x94D049BB133111EBL;
        h ^= h >>> 31;
        return h;
    }

    /** 空洞：椭球空腔，参数为悬崖局部坐标下的位置与半轴。 */
    private record Cavity(double along, double t, double y, double a, double b, double c,
                          double side, double tilt, double jitter) {
    }

    /** 悬挂钟乳石：mode=0 挂在空腔顶部（attach 为相对 topBase 的挂点高度），mode=1 挂在悬崖底缘（embed 嵌入底面）。 */
    private record Hanging(double along, double t, double embed, double len, double r, double bend,
                           int mode, double attach) {
    }

    private record Blade(int sx, int sz, double len, double width, double depth, double yaw, double phase,
                         double topBase, double topTilt, int groundRoot, int groundTail,
                         boolean hasTrail, double trailOff,
                         double planFrontOff, double planPeak, double planPeakP, double planBack,
                         double planOffB, double planOffC,
                         List<Cavity> cavities, List<Hanging> hangings) {
    }

    private record BladeCell(int cx, int cz, long seed) {
    }
}
