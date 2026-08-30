package com.littlh.palelullaby.client.renderer;

import com.littlh.palelullaby.PaleLullaby;
import com.littlh.palelullaby.entity.HunterRank;
import net.minecraft.client.Minecraft;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.packs.resources.ResourceManager;
import net.minecraft.server.packs.resources.ResourceManagerReloadListener;
import net.minecraft.world.entity.Mob;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 吸血鬼/血猎皮肤库：动态扫描贴图文件夹。
 * <p>
 * 目录约定（每个 PNG 即一张皮肤，放进文件夹即自动加入对应池）：
 * <pre>
 * textures/entity/vampire/big/*.png        大库（所有吸血鬼等级共用）
 * textures/entity/vampire/rank1/*.png      初阶吸血鬼
 * textures/entity/vampire/rank2/*.png      贵族
 * textures/entity/vampire/rank3/*.png      领主
 * textures/entity/blood_hunter/big/*.png   大库（所有血猎等级共用）
 * textures/entity/blood_hunter/rank1/*.png 初阶血猎
 * textures/entity/blood_hunter/rank2/*.png 中阶血猎
 * textures/entity/blood_hunter/rank3/*.png 高阶血猎
 * textures/entity/fallen_blood_hunter/*.png 堕落血猎
 * </pre>
 * 实际皮肤池 = 大库 + 对应等级库，按实体 UUID 稳定抽选。
 * 资源包重载（F3+T / 重进）时会清空缓存重新扫描。
 */
public final class MobSkins implements ResourceManagerReloadListener {

    private static final MobSkins INSTANCE = new MobSkins();
    private static final Map<String, List<ResourceLocation>> CACHE = new HashMap<>();

    /** 兜底：池为空时返回不存在的路径，渲染成缺失贴图而不崩溃。 */
    private static final ResourceLocation MISSING =
            ResourceLocation.fromNamespaceAndPath(PaleLullaby.MOD_ID, "textures/entity/missing.png");

    private MobSkins() {
    }

    public static MobSkins instance() {
        return INSTANCE;
    }

    @Override
    public void onResourceManagerReload(ResourceManager resourceManager) {
        CACHE.clear();
    }

    private static List<ResourceLocation> folder(String sub) {
        return CACHE.computeIfAbsent(sub, MobSkins::scan);
    }

    private static List<ResourceLocation> scan(String sub) {
        ResourceManager rm = Minecraft.getInstance().getResourceManager();
        return rm.listResources("textures/entity/" + sub, loc -> loc.getPath().endsWith(".png"))
                .keySet().stream()
                .filter(loc -> loc.getNamespace().equals(PaleLullaby.MOD_ID))
                .sorted()
                .toList();
    }

    private static List<ResourceLocation> pool(String faction, String rank) {
        List<ResourceLocation> pool = new ArrayList<>();
        pool.addAll(folder(faction + "/big"));
        pool.addAll(folder(faction + "/" + rank));
        return List.copyOf(pool);
    }

    /** 吸血鬼皮肤，rank 1/2/3 对应初阶/贵族/领主。 */
    public static ResourceLocation vampireSkin(Mob entity, int rank) {
        return switch (rank) {
            case 2 -> pick(entity, pool("vampire", "rank2"));
            case 3 -> pick(entity, pool("vampire", "rank3"));
            default -> pick(entity, pool("vampire", "rank1"));
        };
    }

    /** 血猎皮肤，按实体类型推导的等级抽选。 */
    public static ResourceLocation hunterSkin(Mob entity, HunterRank rank) {
        return switch (rank) {
            case RANK_2 -> pick(entity, pool("blood_hunter", "rank2"));
            case RANK_3 -> pick(entity, pool("blood_hunter", "rank3"));
            default -> pick(entity, pool("blood_hunter", "rank1"));
        };
    }

    /** 堕落血猎皮肤（该文件夹内随机一张）。 */
    public static ResourceLocation fallenHunterSkin(Mob entity) {
        return pick(entity, folder("fallen_blood_hunter"));
    }

    private static ResourceLocation pick(Mob entity, List<ResourceLocation> pool) {
        if (pool.isEmpty()) {
            return MISSING;
        }
        long low = entity.getUUID().getLeastSignificantBits();
        return pool.get(Math.floorMod(low, pool.size()));
    }
}
