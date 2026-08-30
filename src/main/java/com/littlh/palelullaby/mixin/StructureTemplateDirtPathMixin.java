package com.littlh.palelullaby.mixin;

import com.littlh.palelullaby.IceWallVillageProcessor;
import net.minecraft.core.BlockPos;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.WorldGenRegion;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.ServerLevelAccessor;
import net.minecraft.world.level.biome.Biome;
import net.minecraft.world.level.levelgen.structure.templatesystem.StructurePlaceSettings;
import net.minecraft.world.level.levelgen.structure.templatesystem.StructureProcessor;
import net.minecraft.world.level.levelgen.structure.templatesystem.StructureTemplate;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

/**
 * 大冰壁群系里的村庄（原版 jigsaw 雪村）放置时，给放置设置挂上专用处理器
 * （土径替换 + 村庄下方冰盖填充）。非大冰壁群系不受影响。
 */
@Mixin(StructureTemplate.class)
public abstract class StructureTemplateDirtPathMixin {

    private static final ResourceKey<Biome> GIANT_ICE_WALL = ResourceKey.create(
            Registries.BIOME, ResourceLocation.fromNamespaceAndPath("pale_lullaby", "giant_ice_wall"));

    @Inject(method = "placeInWorld", at = @At("HEAD"))
    private void paleLullaby$replaceDirtPathWithRubble(
            ServerLevelAccessor level, BlockPos offset, BlockPos pivot,
            StructurePlaceSettings settings, RandomSource random, int flags, CallbackInfoReturnable<Boolean> cir) {
        if (!inIceWall(level, pivot, offset)) {
            return;
        }
        for (StructureProcessor processor : settings.getProcessors()) {
            if (processor instanceof IceWallVillageProcessor) {
                return;
            }
        }
        settings.addProcessor(new IceWallVillageProcessor());
    }

    /**
     * 判断放置位置是否在大冰壁群系。世界生成阶段不能直接查 offset/pivot 的群系：
     * 那可能落在依赖半径内但状态尚未就绪的区块，抛
     * "Requested chunk unavailable during world generation" 导致掉线。
     * 改为查询世界生成区域中心区块（必然已生成），该判断永不抛异常。
     */
    @Unique
    private boolean inIceWall(ServerLevelAccessor level, BlockPos pivot, BlockPos offset) {
        try {
            if (level instanceof WorldGenRegion region) {
                ChunkPos center = region.getCenter();
                return level.getBiome(
                        new BlockPos(center.getMinBlockX() + 8, 0, center.getMinBlockZ() + 8))
                        .is(GIANT_ICE_WALL);
            }
            BlockPos pos = pivot != null ? pivot : offset;
            if (pos == null) {
                return false;
            }
            // 非生成阶段：先确认区块已加载，避免 getBiome 同步生成未加载区块造成卡顿。
            if (level instanceof ServerLevel serverLevel && !serverLevel.isLoaded(pos)) {
                return false;
            }
            return level.getBiome(pos).is(GIANT_ICE_WALL);
        } catch (RuntimeException ignored) {
            return false;
        }
    }
}
