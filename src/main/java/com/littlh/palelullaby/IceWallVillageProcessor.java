package com.littlh.palelullaby;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import javax.annotation.Nullable;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.WorldGenRegion;
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.level.ServerLevelAccessor;
import net.minecraft.world.level.WorldGenLevel;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.levelgen.Heightmap;
import net.minecraft.world.level.levelgen.structure.templatesystem.StructurePlaceSettings;
import net.minecraft.world.level.levelgen.structure.templatesystem.StructureProcessor;
import net.minecraft.world.level.levelgen.structure.templatesystem.StructureProcessorType;
import net.minecraft.world.level.levelgen.structure.templatesystem.StructureTemplate;

/**
 * 大冰壁村庄专用处理器：
 * 1. 把土径按 4x4 斑块替换成沙砾/圆石混合；
 * 2. 村庄片段放置后，把片段正下方仍是空气/泥土/石头/草的地面，按
 *    雪块 -> 浮冰/冰 -> 蓝冰 -> 方解石 的渐变填充成冰盖，避免村庄下方露出原版地表。
 */
public class IceWallVillageProcessor extends StructureProcessor {
   private static final BlockState GRAVEL = Blocks.GRAVEL.defaultBlockState();
   private static final BlockState COBBLESTONE = Blocks.COBBLESTONE.defaultBlockState();

   private static final Map<Block, Boolean> REPLACEABLE = new HashMap<>();

   static {
      Block[] blocks = new Block[]{
         Blocks.AIR, Blocks.CAVE_AIR, Blocks.GRASS_BLOCK, Blocks.DIRT, Blocks.COARSE_DIRT,
         Blocks.STONE, Blocks.GRAVEL, Blocks.SAND, Blocks.SANDSTONE,
         Blocks.SNOW_BLOCK, Blocks.SNOW, Blocks.WATER
      };
      for (Block block : blocks) {
         REPLACEABLE.put(block, Boolean.TRUE);
      }
   }

   @Override
   @Nullable
   public StructureTemplate.StructureBlockInfo process(
         LevelReader level, BlockPos offset, BlockPos pivot,
         StructureTemplate.StructureBlockInfo blockInfo,
         StructureTemplate.StructureBlockInfo relativeBlockInfo,
         StructurePlaceSettings settings, @Nullable StructureTemplate template) {
      if (!relativeBlockInfo.state().is(Blocks.DIRT_PATH)) {
         return relativeBlockInfo;
      }
      BlockPos worldPos = relativeBlockInfo.pos();
      long h = mix(worldSeed(level), worldPos.getX() >> 2, worldPos.getZ() >> 2);
      BlockState replacement = (h & 0xFF) < 150 ? GRAVEL : COBBLESTONE;
      CompoundTag nbt = relativeBlockInfo.nbt();
      return new StructureTemplate.StructureBlockInfo(worldPos, replacement, nbt);
   }

   @Override
   public List<StructureTemplate.StructureBlockInfo> finalizeProcessing(
         ServerLevelAccessor level, BlockPos offset, BlockPos pivot,
         List<StructureTemplate.StructureBlockInfo> originalBlockInfos,
         List<StructureTemplate.StructureBlockInfo> processedBlockInfos,
         StructurePlaceSettings settings) {
      if (processedBlockInfos.isEmpty()) {
         return processedBlockInfos;
      }

      // 按列统计片段最底部方块的高度（模板不存空气，最低非空方块就是地板/地下室底）。
      Map<Long, Integer> floorY = new HashMap<>();
      for (StructureTemplate.StructureBlockInfo info : processedBlockInfos) {
         BlockPos pos = info.pos();
         long key = ((long) pos.getX() << 32) ^ (pos.getZ() & 0xFFFFFFFFL);
         floorY.merge(key, pos.getY(), Math::min);
      }

      long seed = worldSeed(level);
      for (Map.Entry<Long, Integer> entry : floorY.entrySet()) {
         long key = entry.getKey();
         int x = (int) (key >> 32);
         int z = (int) (key & 0xFFFFFFFFL);
         if (level instanceof WorldGenRegion region && !region.hasChunk(x >> 4, z >> 4)) {
            continue;
         }
         try {
            int ground = level.getHeight(Heightmap.Types.WORLD_SURFACE_WG, x, z);
            int anchor = Math.max(ground, level.getSeaLevel());
            long h = hash(x, z, seed);
            int depth = 30 + (int) ((h >>> 48) % 31L);
            int bottom = Math.max(level.getMinBuildHeight() + 4, anchor - depth);
            int top = entry.getValue() - 1;
            if (top <= bottom) {
               continue;
            }
            fillColumn(level, x, z, bottom, top, seed);
         } catch (IllegalStateException ignored) {
            // 相邻区块状态未就绪时跳过该列，避免世界生成崩溃。
         }
      }
      return processedBlockInfos;
   }

   private void fillColumn(ServerLevelAccessor level, int x, int z, int bottom, int top, long seed) {
      for (int y = bottom; y <= top; y++) {
         BlockPos pos = new BlockPos(x, y, z);
         BlockState current = level.getBlockState(pos);
         if (current.is(Blocks.BEDROCK) || !REPLACEABLE.containsKey(current.getBlock())) {
            continue;
         }
         level.setBlock(pos, glacierState(x, y, z, top, bottom, seed), 2);
      }
   }

   private static BlockState glacierState(int x, int y, int z, int top, int bottom, long seed) {
      double depthRatio = (double) (top - y) / (double) Math.max(1, top - bottom);
      if (y >= top - 2) {
         return Blocks.SNOW_BLOCK.defaultBlockState();
      }
      if (depthRatio < 0.08) {
         return hash3D(x, y, z, seed) < 0.3 ? Blocks.PACKED_ICE.defaultBlockState() : Blocks.ICE.defaultBlockState();
      }
      if (depthRatio < 0.4) {
         return Blocks.PACKED_ICE.defaultBlockState();
      }
      if (depthRatio < 0.7) {
         return hash3D(x, y, z, seed ^ 4660L) < (depthRatio - 0.4) * 3.33
               ? Blocks.BLUE_ICE.defaultBlockState() : Blocks.PACKED_ICE.defaultBlockState();
      }
      if (depthRatio < 0.95) {
         return Blocks.BLUE_ICE.defaultBlockState();
      }
      return hash3D(x, y, z, seed ^ 22136L) < (depthRatio - 0.95) * 20.0
            ? Blocks.CALCITE.defaultBlockState() : Blocks.BLUE_ICE.defaultBlockState();
   }

   private static double hash3D(int x, int y, int z, long seed) {
      long h = seed ^ (long) x * -7046029254386353131L;
      h ^= (long) y * -4132994306676758123L;
      h ^= (long) z * -4658895280553007687L;
      h ^= h >>> 29;
      h *= -7723592293110705685L;
      h ^= h >>> 31;
      return (double) (Math.abs(h) & 16777215L) / 1.6777216E7;
   }

   @Override
   protected StructureProcessorType<?> getType() {
      return StructureProcessorType.BLOCK_ROT;
   }

   private static long worldSeed(LevelReader level) {
      if (level instanceof WorldGenLevel worldGenLevel) {
         return worldGenLevel.getSeed();
      }
      if (level instanceof ServerLevel serverLevel) {
         return serverLevel.getSeed();
      }
      return 0L;
   }

   private static long mix(long seed, int x, int z) {
      long h = seed;
      h ^= x * 0x9E3779B97F4A7C15L;
      h ^= z * 0xBF58476D1CE4E5B9L;
      h ^= h >>> 29;
      h *= 0x94D049BB133111EBL;
      h ^= h >>> 31;
      return h;
   }

   private static long hash(int x, int z, long seed) {
      long h = seed ^ (long) x * -7046029254386353131L;
      h ^= (long) z * -4658895280553007687L;
      h ^= h >>> 29;
      h *= -7723592293110705685L;
      return h ^ h >>> 31;
   }
}
