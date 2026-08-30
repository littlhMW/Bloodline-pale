package com.littlh.palelullaby;

import com.mojang.serialization.Codec;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import net.minecraft.core.BlockPos;
import net.minecraft.core.QuartPos;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerChunkCache;
import net.minecraft.tags.FluidTags;
import net.minecraft.world.level.LevelHeightAccessor;
import net.minecraft.world.level.WorldGenLevel;
import net.minecraft.world.level.biome.Biome;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.SnowLayerBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.chunk.ChunkAccess;
import net.minecraft.world.level.chunk.ChunkGenerator;
import net.minecraft.world.level.levelgen.RandomState;
import net.minecraft.world.level.levelgen.Heightmap.Types;
import net.minecraft.world.level.levelgen.feature.Feature;
import net.minecraft.world.level.levelgen.feature.FeaturePlaceContext;
import net.minecraft.world.level.levelgen.feature.configurations.NoneFeatureConfiguration;

public class IceSheetFeature extends Feature<NoneFeatureConfiguration> {
   private static final int CAP_ABOVE_GROUND = 100;
   private static final int CAP_GRID = 256;
   private static final int MIN_DEPTH = 30;
   private static final int MAX_DEPTH = 60;
   private static final int EDGE_MAX_CELLS = 5;
   private static final int EDGE_NOISE_GRID = 24;
   private static final int EDGE_NOISE_SALT = 60782;
   private static final double[][] CRACK_LINES = new double[][]{{120.0, 45.0, 0.007, 0.0, 0.02, 1.0}, {380.0, 55.0, 0.005, 1.3, 0.015, 2.2}};
   private static final ResourceKey<Biome> GIANT_ICE_WALL = ResourceKey.create(
      Registries.BIOME, ResourceLocation.fromNamespaceAndPath("pale_lullaby", "giant_ice_wall")
   );
   private static final ConcurrentHashMap<Long, Integer> HEIGHT_CACHE = new ConcurrentHashMap<>();
   private static final ConcurrentHashMap<Long, Double> HASH3D_CACHE = new ConcurrentHashMap<>();

   public IceSheetFeature(Codec<NoneFeatureConfiguration> codec) {
      super(codec);
   }

   public boolean place(FeaturePlaceContext<NoneFeatureConfiguration> context) {
      WorldGenLevel level = context.level();
      long seed = level.getSeed();
      int minY = level.getMinBuildHeight() + 4;
      int maxY = level.getMaxBuildHeight() - 1;
      int baseX = context.origin().getX() >> 4 << 4;
      int baseZ = context.origin().getZ() >> 4 << 4;
      ChunkAccess chunk = level.getChunk(baseX >> 4, baseZ >> 4);
      RandomState randomState = ((ServerChunkCache)level.getChunkSource()).randomState();
      boolean generated = false;
      Map<Long, Double> nearCache = new HashMap<>();

      for (int x = 0; x < 16; x++) {
         for (int z = 0; z < 16; z++) {
            int wx = baseX + x;
            int wz = baseZ + z;
            int ground = level.getHeight(Types.WORLD_SURFACE_WG, wx, wz);
            if (!isIceWall(chunk, wx, ground, wz)) {
               double edge = smoothEdgeStrength(level, wx, ground, wz, nearCache);
               if (edge <= 0.0 || valueNoise(wx, wz, 24, seed, 60782) >= edge) {
                  continue;
               }
            }

            int anchor = Math.max(ground, level.getSeaLevel());
            int top = iceWallTopAt(context.chunkGenerator(), randomState, level, wx, wz, seed);
            if (top > anchor) {
               int depth = 30 + (int)((hash(wx, wz, seed) >>> 48) % 31L);
               int bottom = Math.max(minY, anchor - depth);
               if (this.fillColumn(level, wx, wz, bottom, top, seed)) {
                  generated = true;
               }
            }
         }
      }

      return generated;
   }

      private boolean fillColumn(WorldGenLevel level, int wx, int wz, int bottom, int top, long seed) {
      double baseCrackDist = this.getCrackDistance((double)wx, (double)wz);
      int crackDepth = 60 + (int)(Math.abs(valueNoise(wx, wz, 100, seed, 4)) * 45.0);
      int crackFloor = Math.max(bottom, top - crackDepth);
      int snowDepth = 1 + (int)(valueNoise(wx, wz, 24, seed, 12) * 3.9);
      double fissureMask = valueNoise(wx, wz, 80, seed, 99);
      boolean generated = false;
      int sampleCount = (top - bottom) / 2 + 2;
      double[] cdArr = new double[sampleCount];
      double[] cwArr = new double[sampleCount];
      double[] fisArr = new double[sampleCount];
      double[] fnArr = new double[sampleCount];

      for (int si = 0; si < sampleCount; si++) {
         int y = Math.min(bottom + si * 2, top);
         cdArr[si] = (this.noise3D((double)wx * 0.012, (double)y * 0.018, (double)wz * 0.012, seed) - 0.5) * 16.0;
         cwArr[si] = 8.0 + (this.noise3D((double)wx * 0.05, (double)y * 0.05, (double)wz * 0.05, seed ^ 1092L) - 0.5) * 5.0;
         double yWarp = (double)y;
         double fn = this.noise3D((double)wx * 0.012, yWarp * 0.025, (double)wz * 0.012, seed ^ 2184L);
         fisArr[si] = Math.abs(fn - 0.5) * 2.0;
         fnArr[si] = this.noise3D((double)wx * 0.03, (double)y * 0.04, (double)wz * 0.03, seed ^ 2730L);
      }

      double streaks = (valueNoise(wx, wz, 12, seed, 7) - 0.5) * 0.4;
      double strataBase = (valueNoise(wx, wz, 40, seed, 8) - 0.5) * 0.2;
      double strataPhase = valueNoise(wx, wz, 25, seed, 9) * 10.0;

      int height = top - bottom;
      boolean[] inCanyonArr = new boolean[height];
      boolean[] inFissureArr = new boolean[height];
      boolean[] nearCanyonArr = new boolean[height];
      boolean[] nearFissureArr = new boolean[height];
      int crackMinY = -1;
      int crackMaxY = -1;

      for (int y = bottom; y < top; y++) {
         int si0 = (y - bottom) / 2;
         int si1 = Math.min(si0 + 1, sampleCount - 1);
         double frac = (double)((y - bottom) % 2) / 2.0;
         double canyonDistort = cdArr[si0] + (cdArr[si1] - cdArr[si0]) * frac;
         double canyonWidth = cwArr[si0] + (cwArr[si1] - cwArr[si0]) * frac;
         double actualCrackDist = baseCrackDist + canyonDistort;
         boolean inCanyon = actualCrackDist < canyonWidth && y > crackFloor;
         boolean nearCanyon = actualCrackDist < canyonWidth + 3.0 && y > crackFloor;
         double fissure = fisArr[si0] + (fisArr[si1] - fisArr[si0]) * frac;
         double fNoise = fnArr[si0] + (fnArr[si1] - fnArr[si0]) * frac;
         double fSkew = fNoise * fNoise * fNoise;
         double fissureWidth = 0.02 + fSkew * 0.1;
         boolean inFissure = fissure < fissureWidth && fissureMask > 0.6 && y < top - snowDepth - 2;
         boolean nearFissure = fissure < fissureWidth + 0.1 && fissureMask > 0.55 && y < top - snowDepth - 1;
         int idx = y - bottom;
         inCanyonArr[idx] = inCanyon;
         inFissureArr[idx] = inFissure;
         nearCanyonArr[idx] = nearCanyon;
         nearFissureArr[idx] = nearFissure;
         if (inCanyon) {
            if (crackMinY < 0) {
               crackMinY = y;
            }
            crackMaxY = y;
         }
      }

      // 同一列的裂隙合并成一条连续的竖直通道：既能清掉裂隙里悬空的冰块/方解石，
      // 也能避免裂隙顶部只差几格就露头时留下一整块悬空的雪帽。
      if (crackMaxY >= 0) {
         if (crackMaxY >= top - snowDepth - 5) {
            crackMaxY = top - 1;
         }
         for (int y = Math.max(crackMinY, crackFloor + 1); y <= crackMaxY; y++) {
            inCanyonArr[y - bottom] = true;
            nearCanyonArr[y - bottom] = true;
         }
      }

      for (int y = bottom; y < top; y++) {
         int idx = y - bottom;
         boolean inCanyon = inCanyonArr[idx];
         boolean inFissure = inFissureArr[idx];
         boolean nearCanyon = nearCanyonArr[idx];
         boolean nearFissure = nearFissureArr[idx];
         BlockState state;
         if (inCanyon || inFissure) {
            state = Blocks.AIR.defaultBlockState();
         } else if (y >= top - snowDepth) {
            state = Blocks.SNOW_BLOCK.defaultBlockState();
         } else {
            double depthRatio = (double)(top - y) / (double)Math.max(1, top - bottom);
            double strata = strataBase + Math.sin((double)y * 0.25 + strataPhase) * 0.15;
            double score = depthRatio + streaks + strata;
            if (nearCanyon || nearFissure) {
               score += 0.35;
            }

            state = this.getGlacierState(wx, y, wz, score, seed);
         }

         BlockPos pos = new BlockPos(wx, y, wz);
         BlockState cur = level.getBlockState(pos);
         if (!cur.is(Blocks.BEDROCK) && (cur.isAir() || cur.isSolid() || cur.getFluidState().is(FluidTags.WATER))) {
            this.setBlock(level, pos, state);
            generated = true;
         }
      }

      if (baseCrackDist < 6.0 && (hash(wx, wz, seed ^ 1374510829L) & 255L) < 4L) {
         BlockPos flowerPos = new BlockPos(wx, crackFloor + 1, wz);
         BlockState flower = ((FrostMoonflowerBlock)PaleLullabyBlocks.FROST_MOONFLOWER.get()).defaultBlockState();
         if (level.getBlockState(flowerPos).isAir() && flower.canSurvive(level, flowerPos)) {
            this.setBlock(level, flowerPos, flower);
            generated = true;
         }
      }

      // 雪层不再按距裂隙的距离跳过：只要顶面是实心就盖雪，裂隙开口处由 placeSnow 的下方实心检查自动跳过。
      double drift = valueNoise(wx, wz, 16, seed, 5);
      int layers = 1 + (int)(drift * 7.9);
      if (layers >= 8) {
         BlockPos mound = new BlockPos(wx, top, wz);
         BlockState moundBelow = level.getBlockState(mound.below());
         boolean moundSolid = !moundBelow.isAir() && !moundBelow.getFluidState().is(FluidTags.WATER);
         if (moundSolid && level.getBlockState(mound).isAir()) {
            this.setBlock(level, mound, Blocks.SNOW_BLOCK.defaultBlockState());
            generated = true;
            int extraLayers = 1 + (int)((drift - 0.8) * 15.0);
            if (extraLayers > 0) {
               this.placeSnow(level, wx, top + 1, wz, extraLayers);
            }
         }
      } else if (this.placeSnow(level, wx, top, wz, layers)) {
         generated = true;
      }

      return generated;
   }

   private BlockState getGlacierState(int x, int y, int z, double score, long seed) {
      if (score < 0.08) {
         return this.hash3D(x, y, z, seed) < 0.3 ? Blocks.PACKED_ICE.defaultBlockState() : Blocks.ICE.defaultBlockState();
      } else if (score < 0.4) {
         return Blocks.PACKED_ICE.defaultBlockState();
      } else if (score < 0.7) {
         return this.hash3D(x, y, z, seed ^ 4660L) < (score - 0.4) * 3.33 ? Blocks.BLUE_ICE.defaultBlockState() : Blocks.PACKED_ICE.defaultBlockState();
      } else if (score < 0.95) {
         return Blocks.BLUE_ICE.defaultBlockState();
      } else {
         return this.hash3D(x, y, z, seed ^ 22136L) < (score - 0.95) * 20.0 ? Blocks.CALCITE.defaultBlockState() : Blocks.BLUE_ICE.defaultBlockState();
      }
   }

   private double getCrackDistance(double wx, double wz) {
      double minD = 999.0;
      double cellX = (wz % 512.0 + 512.0) % 512.0;

      for (int i = 0; i < CRACK_LINES.length; i++) {
         double[] l = CRACK_LINES[i];
         double center = l[0] + l[1] * Math.sin(wx * l[2] + l[3]) + l[1] * 0.4 * Math.sin(wx * l[4] + l[5]);
         double d = Math.abs(cellX - center);
         minD = Math.min(minD, Math.min(d, 512.0 - d));
      }

      double cellZ = (wx % 512.0 + 512.0) % 512.0;

      for (int i = 0; i < CRACK_LINES.length; i++) {
         double[] l = CRACK_LINES[i];
         double center = l[0] + l[1] * Math.sin(wz * l[2] + l[3]) + l[1] * 0.4 * Math.sin(wz * l[4] + l[5]);
         double d = Math.abs(cellZ - center);
         minD = Math.min(minD, Math.min(d, 512.0 - d));
      }

      return minD;
   }

   private boolean placeSnow(WorldGenLevel level, int wx, int y, int wz, int layers) {
      if (y >= level.getMaxBuildHeight() - 1) {
         return false;
      } else {
         BlockPos pos = new BlockPos(wx, y, wz);
         if (level.getBlockState(pos).isAir()) {
            BlockState below = level.getBlockState(pos.below());
            if (!below.isAir() && !below.getFluidState().is(FluidTags.WATER)) {
               this.setBlock(level, pos, (BlockState)Blocks.SNOW.defaultBlockState().setValue(SnowLayerBlock.LAYERS, Math.max(1, Math.min(8, layers))));
               return true;
            } else {
               return false;
            }
         } else {
            return false;
         }
      }
   }

   private double noise3D(double x, double y, double z, long seed) {
      int x0 = (int)Math.floor(x);
      int y0 = (int)Math.floor(y);
      int z0 = (int)Math.floor(z);
      double fx = x - (double)x0;
      double fy = y - (double)y0;
      double fz = z - (double)z0;
      double u = fx * fx * (3.0 - 2.0 * fx);
      double v = fy * fy * (3.0 - 2.0 * fy);
      double w = fz * fz * (3.0 - 2.0 * fz);
      double n00 = this.hash3D(x0, y0, z0, seed) * (1.0 - u) + this.hash3D(x0 + 1, y0, z0, seed) * u;
      double n10 = this.hash3D(x0, y0 + 1, z0, seed) * (1.0 - u) + this.hash3D(x0 + 1, y0 + 1, z0, seed) * u;
      double n01 = this.hash3D(x0, y0, z0 + 1, seed) * (1.0 - u) + this.hash3D(x0 + 1, y0, z0 + 1, seed) * u;
      double n11 = this.hash3D(x0, y0 + 1, z0 + 1, seed) * (1.0 - u) + this.hash3D(x0 + 1, y0 + 1, z0 + 1, seed) * u;
      double n0 = n00 * (1.0 - v) + n10 * v;
      double n1 = n01 * (1.0 - v) + n11 * v;
      return n0 * (1.0 - w) + n1 * w;
   }

   private double hash3D(int x, int y, int z, long seed) {
      long key = hash3DKey(x, y, z, seed);
      Double cached = HASH3D_CACHE.get(key);
      if (cached != null) {
         return cached;
      } else {
         double v = computeHash3D(x, y, z, seed);
         if (HASH3D_CACHE.size() > 262144) {
            HASH3D_CACHE.clear();
         }

         HASH3D_CACHE.put(key, v);
         return v;
      }
   }

   private static long hash3DKey(int x, int y, int z, long seed) {
      long h = seed ^ (long)x * -7046029254386353131L;
      h ^= (long)y * -4132994306676758123L;
      h ^= (long)z * -4658895280553007687L;
      h ^= h >>> 29;
      h *= -7723592293110705685L;
      return h ^ h >>> 31;
   }

   private static double computeHash3D(int x, int y, int z, long seed) {
      long h = seed ^ (long)x * -7046029254386353131L;
      h ^= (long)y * -4132994306676758123L;
      h ^= (long)z * -4658895280553007687L;
      h ^= h >>> 29;
      h *= -7723592293110705685L;
      h ^= h >>> 31;
      return (double)(Math.abs(h) & 16777215L) / 1.6777216E7;
   }

   private static double smoothTerrain(ChunkGenerator chunkGenerator, RandomState randomState, LevelHeightAccessor heightAccessor, int wx, int wz, long seed) {
      int gx = Math.floorDiv(wx, 256);
      int gz = Math.floorDiv(wz, 256);
      double fx = (double)(wx - gx * 256) / 256.0;
      double fz = (double)(wz - gz * 256) / 256.0;
      double u = fx * fx * (3.0 - 2.0 * fx);
      double v = fz * fz * (3.0 - 2.0 * fz);
      double h00 = terrainAt(chunkGenerator, randomState, heightAccessor, gx, gz, seed);
      double h10 = terrainAt(chunkGenerator, randomState, heightAccessor, gx + 1, gz, seed);
      double h01 = terrainAt(chunkGenerator, randomState, heightAccessor, gx, gz + 1, seed);
      double h11 = terrainAt(chunkGenerator, randomState, heightAccessor, gx + 1, gz + 1, seed);
      return h00 * (1.0 - u) * (1.0 - v) + h10 * u * (1.0 - v) + h01 * (1.0 - u) * v + h11 * u * v;
   }

   private static double terrainAt(ChunkGenerator chunkGenerator, RandomState randomState, LevelHeightAccessor heightAccessor, int gx, int gz, long seed) {
      long key = hash(gx, gz, seed ^ -7046029254386353131L);
      Integer cached = HEIGHT_CACHE.get(key);
      if (cached != null) {
         return (double)cached.intValue();
      } else {
         int h = chunkGenerator.getBaseHeight(gx * 256 + 8, gz * 256 + 8, Types.WORLD_SURFACE_WG, heightAccessor, randomState);
         if (HEIGHT_CACHE.size() > 65536) {
            HEIGHT_CACHE.clear();
         }

         HEIGHT_CACHE.put(key, h);
         return (double)h;
      }
   }

   public static int iceWallTopAt(ChunkGenerator chunkGenerator, RandomState randomState, LevelHeightAccessor heightAccessor, int wx, int wz, long seed) {
      int ground = chunkGenerator.getBaseHeight(wx, wz, Types.WORLD_SURFACE_WG, heightAccessor, randomState);
      double terrainRef = smoothTerrain(chunkGenerator, randomState, heightAccessor, wx, wz, seed);
      double roll = (valueNoise(wx, wz, 512, seed, 0) - 0.5) * 12.0;
      double micro = (valueNoise(wx, wz, 48, seed, 1) - 0.5) * 3.0;
      int maxY = heightAccessor.getMaxBuildHeight() - 1;
      int top = Math.min(maxY, (int)Math.round(terrainRef + 100.0 + roll + micro));
      double isolationNoise = valueNoise(wx, wz, 140, seed ^ 624485L, 2);
      if (isolationNoise > 0.65) {
         top = Math.min(maxY, top + 40 + (int)(valueNoise(wx, wz, 20, seed, 1) * 4.0));
      } else if (isolationNoise > 0.35) {
         top = Math.min(maxY, top + 20 + (int)(valueNoise(wx, wz, 20, seed, 1) * 3.0));
      }

      return top;
   }

   private static double valueNoise(int wx, int wz, int grid, long seed, int salt) {
      int gx = Math.floorDiv(wx, grid);
      int gz = Math.floorDiv(wz, grid);
      double fx = (double)(wx - gx * grid) / (double)grid;
      double fz = (double)(wz - gz * grid) / (double)grid;
      double u = fx * fx * (3.0 - 2.0 * fx);
      double v = fz * fz * (3.0 - 2.0 * fz);
      double n00 = hash01(gx, gz, seed, salt);
      double n10 = hash01(gx + 1, gz, seed, salt);
      double n01 = hash01(gx, gz + 1, seed, salt);
      double n11 = hash01(gx + 1, gz + 1, seed, salt);
      return n00 * (1.0 - u) * (1.0 - v) + n10 * u * (1.0 - v) + n01 * (1.0 - u) * v + n11 * u * v;
   }

   private static double hash01(int x, int z, long seed, int salt) {
      long h = hash((int)((long)x * 2654435769L + (long)salt), (int)((long)z * 3210233709L + (long)salt * 31L), seed);
      return (double)(h & 16777215L) / 1.6777216E7;
   }

   private static boolean isIceWall(ChunkAccess chunk, int x, int y, int z) {
      return chunk.getNoiseBiome(QuartPos.fromBlock(x), QuartPos.fromBlock(y), QuartPos.fromBlock(z)).is(GIANT_ICE_WALL);
   }

   private static double smoothEdgeStrength(WorldGenLevel level, int x, int y, int z, Map<Long, Double> cache) {
      long key = (long)(x >> 2) << 32 ^ (long)(z >> 2);
      Double cached = cache.get(key);
      if (cached != null) {
         return cached;
      } else {
         int cellX = x >> 2;
         int cellZ = z >> 2;
         boolean nearby = false;

         for (int dx = -1; dx <= 1 && !nearby; dx++) {
            for (int dz = -1; dz <= 1; dz++) {
               if ((dx != 0 || dz != 0) && isIceWallCell(level, cellX + dx, cellZ + dz, y)) {
                  nearby = true;
                  break;
               }
            }
         }

         double result = -1.0;
         if (nearby) {
            int minDist = 5;

            for (int dx = -5; dx <= 5; dx++) {
               for (int dzx = -5; dzx <= 5; dzx++) {
                  if (dx != 0 || dzx != 0) {
                     int dist = Math.max(Math.abs(dx), Math.abs(dzx));
                     if (dist < minDist && isIceWallCell(level, cellX + dx, cellZ + dzx, y)) {
                        minDist = dist;
                     }
                  }
               }
            }

            result = 1.0 - (double)minDist / 5.0;
         }

         cache.put(key, result);
         return result;
      }
   }

   private static boolean isIceWallCell(WorldGenLevel level, int cellX, int cellZ, int y) {
      return level.getUncachedNoiseBiome(cellX * 4 + 2, y, cellZ * 4 + 2).is(GIANT_ICE_WALL);
   }

   private static long hash(int x, int z, long seed) {
      long h = seed ^ (long)x * -7046029254386353131L;
      h ^= (long)z * -4658895280553007687L;
      h ^= h >>> 29;
      h *= -7723592293110705685L;
      return h ^ h >>> 31;
   }
}
