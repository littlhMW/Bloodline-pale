package com.littlh.palelullaby;

import com.mojang.serialization.Codec;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Vec3i;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.WorldGenLevel;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.Mirror;
import net.minecraft.world.level.block.Rotation;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.levelgen.Heightmap;
import net.minecraft.world.level.levelgen.feature.Feature;
import net.minecraft.world.level.levelgen.feature.FeaturePlaceContext;
import net.minecraft.world.level.levelgen.feature.FossilFeatureConfiguration;
import net.minecraft.world.level.levelgen.structure.BoundingBox;
import net.minecraft.world.level.levelgen.structure.templatesystem.StructurePlaceSettings;
import net.minecraft.world.level.levelgen.structure.templatesystem.StructureTemplate;
import net.minecraft.world.level.levelgen.structure.templatesystem.StructureTemplateManager;
import org.apache.commons.lang3.mutable.MutableInt;

/**
 * 地表骨架：照搬原版 FossilFeature 的逻辑，但把骨架从“地表下 15~25 格”改为贴近地表放置，
 * 用于枯萎高原的地表生成（原版 minecraft:fossil 会把骨架埋在地下，/place 后地表看不到）。
 */
public class WitheredBoneFeature extends Feature<FossilFeatureConfiguration> {
    public WitheredBoneFeature(Codec<FossilFeatureConfiguration> codec) {
        super(codec);
    }

    @Override
    public boolean place(FeaturePlaceContext<FossilFeatureConfiguration> context) {
        RandomSource random = context.random();
        WorldGenLevel level = context.level();
        BlockPos origin = context.origin();
        Rotation rotation = Rotation.getRandom(random);
        FossilFeatureConfiguration config = context.config();
        int idx = random.nextInt(config.fossilStructures.size());
        StructureTemplateManager templateManager = level.getLevel().getServer().getStructureManager();
        StructureTemplate fossil = templateManager.getOrCreate(config.fossilStructures.get(idx));
        StructureTemplate overlay = templateManager.getOrCreate(config.overlayStructures.get(idx));
        ChunkPos chunkPos = new ChunkPos(origin);
        BoundingBox boundingBox = new BoundingBox(
                chunkPos.getMinBlockX() - 16,
                level.getMinBuildHeight(),
                chunkPos.getMinBlockZ() - 16,
                chunkPos.getMaxBlockX() + 16,
                level.getMaxBuildHeight(),
                chunkPos.getMaxBlockZ() + 16
        );
        StructurePlaceSettings settings = new StructurePlaceSettings()
                .setRotation(rotation)
                .setBoundingBox(boundingBox)
                .setRandom(random);
        Vec3i size = fossil.getSize(rotation);
        BlockPos base = origin.offset(-size.getX() / 2, 0, -size.getZ() / 2);
        int minSurface = origin.getY();
        for (int k = 0; k < size.getX(); k++) {
            for (int l = 0; l < size.getZ(); l++) {
                minSurface = Math.min(minSurface,
                        level.getHeight(Heightmap.Types.OCEAN_FLOOR_WG, base.getX() + k, base.getZ() + l));
            }
        }
        // 原版埋在 j-15-random(10)；这里贴近地表放置，只下沉 1 格让骨架“趴”在荒原上
        int placeY = Math.max(minSurface - 1, level.getMinBuildHeight() + 10);
        BlockPos fossilPos = fossil.getZeroPositionWithTransform(base.atY(placeY), Mirror.NONE, rotation);
        if (countEmptyCorners(level, fossil.getBoundingBox(settings, fossilPos)) > config.maxEmptyCornersAllowed) {
            return false;
        }
        settings.clearProcessors();
        config.fossilProcessors.value().list().forEach(settings::addProcessor);
        fossil.placeInWorld(level, fossilPos, fossilPos, settings, random, 4);
        settings.clearProcessors();
        config.overlayProcessors.value().list().forEach(settings::addProcessor);
        overlay.placeInWorld(level, fossilPos, fossilPos, settings, random, 4);
        return true;
    }

    private static int countEmptyCorners(WorldGenLevel level, BoundingBox box) {
        MutableInt count = new MutableInt(0);
        box.forAllCorners(pos -> {
            BlockState state = level.getBlockState(pos);
            if (state.isAir() || state.is(Blocks.LAVA) || state.is(Blocks.WATER)) {
                count.add(1);
            }
        });
        return count.getValue();
    }
}