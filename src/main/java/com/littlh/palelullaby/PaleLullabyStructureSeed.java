package com.littlh.palelullaby;

/**
 * 结构生成期间记录当前世界种子（ThreadLocal）。
 * 由 StructureGenerateSeedMixin 在 Structure.generate 入口设置/清除，
 * 供 IceWallStructureHeightMixin 判断结构高度是否要落到大冰壁/石刺/悬崖顶面上。
 */
public final class PaleLullabyStructureSeed {
    private static final ThreadLocal<Long> SEED = new ThreadLocal<>();

    public static void set(long seed) {
        SEED.set(seed);
    }

    public static void clear() {
        SEED.remove();
    }

    public static Long get() {
        return SEED.get();
    }

    private PaleLullabyStructureSeed() {
    }
}
