package com.littlh.palelullaby.mixin;

import com.littlh.palelullaby.PaleLullabyStructureSeed;
import java.util.function.Predicate;
import net.minecraft.core.Holder;
import net.minecraft.core.RegistryAccess;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.LevelHeightAccessor;
import net.minecraft.world.level.biome.Biome;
import net.minecraft.world.level.biome.BiomeSource;
import net.minecraft.world.level.chunk.ChunkGenerator;
import net.minecraft.world.level.levelgen.RandomState;
import net.minecraft.world.level.levelgen.structure.Structure;
import net.minecraft.world.level.levelgen.structure.StructureStart;
import net.minecraft.world.level.levelgen.structure.templatesystem.StructureTemplateManager;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

/**
 * 在 Structure.generate 期间记录世界种子（所有结构生成都经过这里，包括自然生成和 /place structure），
 * 供 IceWallStructureHeightMixin 把结构放到大冰壁/石刺/悬崖的顶面上。
 */
@Mixin(Structure.class)
public abstract class StructureGenerateSeedMixin {

    @Inject(method = "generate", at = @At("HEAD"))
    private void paleLullaby$captureSeedStart(RegistryAccess registryAccess, ChunkGenerator chunkGenerator,
                                              BiomeSource biomeSource, RandomState randomState,
                                              StructureTemplateManager templateManager, long seed, ChunkPos chunkPos,
                                              int references, LevelHeightAccessor heightAccessor,
                                              Predicate<Holder<Biome>> validBiome, CallbackInfoReturnable<StructureStart> cir) {
        PaleLullabyStructureSeed.set(seed);
        // 仅记录种子，不修改返回值
    }

    @Inject(method = "generate", at = @At("TAIL"))
    private void paleLullaby$captureSeedEnd(RegistryAccess registryAccess, ChunkGenerator chunkGenerator,
                                            BiomeSource biomeSource, RandomState randomState,
                                            StructureTemplateManager templateManager, long seed, ChunkPos chunkPos,
                                            int references, LevelHeightAccessor heightAccessor,
                                            Predicate<Holder<Biome>> validBiome, CallbackInfoReturnable<StructureStart> cir) {
        PaleLullabyStructureSeed.clear();
    }
}
