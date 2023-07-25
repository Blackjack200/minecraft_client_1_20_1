package net.minecraft.world.level.levelgen.blending;

import com.google.common.collect.ImmutableMap;
import com.google.common.collect.Lists;
import it.unimi.dsi.fastutil.longs.Long2ObjectOpenHashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Stream;
import javax.annotation.Nullable;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.Direction8;
import net.minecraft.core.Holder;
import net.minecraft.core.QuartPos;
import net.minecraft.data.worldgen.NoiseData;
import net.minecraft.server.level.WorldGenRegion;
import net.minecraft.tags.BlockTags;
import net.minecraft.util.Mth;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.WorldGenLevel;
import net.minecraft.world.level.biome.Biome;
import net.minecraft.world.level.biome.BiomeResolver;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.chunk.CarvingMask;
import net.minecraft.world.level.chunk.ChunkAccess;
import net.minecraft.world.level.chunk.ProtoChunk;
import net.minecraft.world.level.levelgen.DensityFunction;
import net.minecraft.world.level.levelgen.GenerationStep;
import net.minecraft.world.level.levelgen.Heightmap;
import net.minecraft.world.level.levelgen.XoroshiroRandomSource;
import net.minecraft.world.level.levelgen.synth.NormalNoise;
import net.minecraft.world.level.material.FluidState;
import org.apache.commons.lang3.mutable.MutableDouble;
import org.apache.commons.lang3.mutable.MutableObject;

public class Blender {
   private static final Blender EMPTY = new Blender(new Long2ObjectOpenHashMap(), new Long2ObjectOpenHashMap()) {
      public Blender.BlendingOutput blendOffsetAndFactor(int i, int j) {
         return new Blender.BlendingOutput(1.0D, 0.0D);
      }

      public double blendDensity(DensityFunction.FunctionContext densityfunction_functioncontext, double d0) {
         return d0;
      }

      public BiomeResolver getBiomeResolver(BiomeResolver biomeresolver) {
         return biomeresolver;
      }
   };
   private static final NormalNoise SHIFT_NOISE = NormalNoise.create(new XoroshiroRandomSource(42L), NoiseData.DEFAULT_SHIFT);
   private static final int HEIGHT_BLENDING_RANGE_CELLS = QuartPos.fromSection(7) - 1;
   private static final int HEIGHT_BLENDING_RANGE_CHUNKS = QuartPos.toSection(HEIGHT_BLENDING_RANGE_CELLS + 3);
   private static final int DENSITY_BLENDING_RANGE_CELLS = 2;
   private static final int DENSITY_BLENDING_RANGE_CHUNKS = QuartPos.toSection(5);
   private static final double OLD_CHUNK_XZ_RADIUS = 8.0D;
   private final Long2ObjectOpenHashMap<BlendingData> heightAndBiomeBlendingData;
   private final Long2ObjectOpenHashMap<BlendingData> densityBlendingData;

   public static Blender empty() {
      return EMPTY;
   }

   public static Blender of(@Nullable WorldGenRegion worldgenregion) {
      if (worldgenregion == null) {
         return EMPTY;
      } else {
         ChunkPos chunkpos = worldgenregion.getCenter();
         if (!worldgenregion.isOldChunkAround(chunkpos, HEIGHT_BLENDING_RANGE_CHUNKS)) {
            return EMPTY;
         } else {
            Long2ObjectOpenHashMap<BlendingData> long2objectopenhashmap = new Long2ObjectOpenHashMap<>();
            Long2ObjectOpenHashMap<BlendingData> long2objectopenhashmap1 = new Long2ObjectOpenHashMap<>();
            int i = Mth.square(HEIGHT_BLENDING_RANGE_CHUNKS + 1);

            for(int j = -HEIGHT_BLENDING_RANGE_CHUNKS; j <= HEIGHT_BLENDING_RANGE_CHUNKS; ++j) {
               for(int k = -HEIGHT_BLENDING_RANGE_CHUNKS; k <= HEIGHT_BLENDING_RANGE_CHUNKS; ++k) {
                  if (j * j + k * k <= i) {
                     int l = chunkpos.x + j;
                     int i1 = chunkpos.z + k;
                     BlendingData blendingdata = BlendingData.getOrUpdateBlendingData(worldgenregion, l, i1);
                     if (blendingdata != null) {
                        long2objectopenhashmap.put(ChunkPos.asLong(l, i1), blendingdata);
                        if (j >= -DENSITY_BLENDING_RANGE_CHUNKS && j <= DENSITY_BLENDING_RANGE_CHUNKS && k >= -DENSITY_BLENDING_RANGE_CHUNKS && k <= DENSITY_BLENDING_RANGE_CHUNKS) {
                           long2objectopenhashmap1.put(ChunkPos.asLong(l, i1), blendingdata);
                        }
                     }
                  }
               }
            }

            return long2objectopenhashmap.isEmpty() && long2objectopenhashmap1.isEmpty() ? EMPTY : new Blender(long2objectopenhashmap, long2objectopenhashmap1);
         }
      }
   }

   Blender(Long2ObjectOpenHashMap<BlendingData> long2objectopenhashmap, Long2ObjectOpenHashMap<BlendingData> long2objectopenhashmap1) {
      this.heightAndBiomeBlendingData = long2objectopenhashmap;
      this.densityBlendingData = long2objectopenhashmap1;
   }

   public Blender.BlendingOutput blendOffsetAndFactor(int i, int j) {
      int k = QuartPos.fromBlock(i);
      int l = QuartPos.fromBlock(j);
      double d0 = this.getBlendingDataValue(k, 0, l, BlendingData::getHeight);
      if (d0 != Double.MAX_VALUE) {
         return new Blender.BlendingOutput(0.0D, heightToOffset(d0));
      } else {
         MutableDouble mutabledouble = new MutableDouble(0.0D);
         MutableDouble mutabledouble1 = new MutableDouble(0.0D);
         MutableDouble mutabledouble2 = new MutableDouble(Double.POSITIVE_INFINITY);
         this.heightAndBiomeBlendingData.forEach((olong, blendingdata) -> blendingdata.iterateHeights(QuartPos.fromSection(ChunkPos.getX(olong)), QuartPos.fromSection(ChunkPos.getZ(olong)), (i2, j2, d3) -> {
               double d4 = Mth.length((double)(k - i2), (double)(l - j2));
               if (!(d4 > (double)HEIGHT_BLENDING_RANGE_CELLS)) {
                  if (d4 < mutabledouble2.doubleValue()) {
                     mutabledouble2.setValue(d4);
                  }

                  double d5 = 1.0D / (d4 * d4 * d4 * d4);
                  mutabledouble1.add(d3 * d5);
                  mutabledouble.add(d5);
               }
            }));
         if (mutabledouble2.doubleValue() == Double.POSITIVE_INFINITY) {
            return new Blender.BlendingOutput(1.0D, 0.0D);
         } else {
            double d1 = mutabledouble1.doubleValue() / mutabledouble.doubleValue();
            double d2 = Mth.clamp(mutabledouble2.doubleValue() / (double)(HEIGHT_BLENDING_RANGE_CELLS + 1), 0.0D, 1.0D);
            d2 = 3.0D * d2 * d2 - 2.0D * d2 * d2 * d2;
            return new Blender.BlendingOutput(d2, heightToOffset(d1));
         }
      }
   }

   private static double heightToOffset(double d0) {
      double d1 = 1.0D;
      double d2 = d0 + 0.5D;
      double d3 = Mth.positiveModulo(d2, 8.0D);
      return 1.0D * (32.0D * (d2 - 128.0D) - 3.0D * (d2 - 120.0D) * d3 + 3.0D * d3 * d3) / (128.0D * (32.0D - 3.0D * d3));
   }

   public double blendDensity(DensityFunction.FunctionContext densityfunction_functioncontext, double d0) {
      int i = QuartPos.fromBlock(densityfunction_functioncontext.blockX());
      int j = densityfunction_functioncontext.blockY() / 8;
      int k = QuartPos.fromBlock(densityfunction_functioncontext.blockZ());
      double d1 = this.getBlendingDataValue(i, j, k, BlendingData::getDensity);
      if (d1 != Double.MAX_VALUE) {
         return d1;
      } else {
         MutableDouble mutabledouble = new MutableDouble(0.0D);
         MutableDouble mutabledouble1 = new MutableDouble(0.0D);
         MutableDouble mutabledouble2 = new MutableDouble(Double.POSITIVE_INFINITY);
         this.densityBlendingData.forEach((olong, blendingdata) -> blendingdata.iterateDensities(QuartPos.fromSection(ChunkPos.getX(olong)), QuartPos.fromSection(ChunkPos.getZ(olong)), j - 1, j + 1, (j2, k2, l2, d4) -> {
               double d5 = Mth.length((double)(i - j2), (double)((j - k2) * 2), (double)(k - l2));
               if (!(d5 > 2.0D)) {
                  if (d5 < mutabledouble2.doubleValue()) {
                     mutabledouble2.setValue(d5);
                  }

                  double d6 = 1.0D / (d5 * d5 * d5 * d5);
                  mutabledouble1.add(d4 * d6);
                  mutabledouble.add(d6);
               }
            }));
         if (mutabledouble2.doubleValue() == Double.POSITIVE_INFINITY) {
            return d0;
         } else {
            double d2 = mutabledouble1.doubleValue() / mutabledouble.doubleValue();
            double d3 = Mth.clamp(mutabledouble2.doubleValue() / 3.0D, 0.0D, 1.0D);
            return Mth.lerp(d3, d2, d0);
         }
      }
   }

   private double getBlendingDataValue(int i, int j, int k, Blender.CellValueGetter blender_cellvaluegetter) {
      int l = QuartPos.toSection(i);
      int i1 = QuartPos.toSection(k);
      boolean flag = (i & 3) == 0;
      boolean flag1 = (k & 3) == 0;
      double d0 = this.getBlendingDataValue(blender_cellvaluegetter, l, i1, i, j, k);
      if (d0 == Double.MAX_VALUE) {
         if (flag && flag1) {
            d0 = this.getBlendingDataValue(blender_cellvaluegetter, l - 1, i1 - 1, i, j, k);
         }

         if (d0 == Double.MAX_VALUE) {
            if (flag) {
               d0 = this.getBlendingDataValue(blender_cellvaluegetter, l - 1, i1, i, j, k);
            }

            if (d0 == Double.MAX_VALUE && flag1) {
               d0 = this.getBlendingDataValue(blender_cellvaluegetter, l, i1 - 1, i, j, k);
            }
         }
      }

      return d0;
   }

   private double getBlendingDataValue(Blender.CellValueGetter blender_cellvaluegetter, int i, int j, int k, int l, int i1) {
      BlendingData blendingdata = this.heightAndBiomeBlendingData.get(ChunkPos.asLong(i, j));
      return blendingdata != null ? blender_cellvaluegetter.get(blendingdata, k - QuartPos.fromSection(i), l, i1 - QuartPos.fromSection(j)) : Double.MAX_VALUE;
   }

   public BiomeResolver getBiomeResolver(BiomeResolver biomeresolver) {
      return (i, j, k, climate_sampler) -> {
         Holder<Biome> holder = this.blendBiome(i, j, k);
         return holder == null ? biomeresolver.getNoiseBiome(i, j, k, climate_sampler) : holder;
      };
   }

   @Nullable
   private Holder<Biome> blendBiome(int i, int j, int k) {
      MutableDouble mutabledouble = new MutableDouble(Double.POSITIVE_INFINITY);
      MutableObject<Holder<Biome>> mutableobject = new MutableObject<>();
      this.heightAndBiomeBlendingData.forEach((olong, blendingdata) -> blendingdata.iterateBiomes(QuartPos.fromSection(ChunkPos.getX(olong)), j, QuartPos.fromSection(ChunkPos.getZ(olong)), (i2, j2, holder) -> {
            double d2 = Mth.length((double)(i - i2), (double)(k - j2));
            if (!(d2 > (double)HEIGHT_BLENDING_RANGE_CELLS)) {
               if (d2 < mutabledouble.doubleValue()) {
                  mutableobject.setValue(holder);
                  mutabledouble.setValue(d2);
               }

            }
         }));
      if (mutabledouble.doubleValue() == Double.POSITIVE_INFINITY) {
         return null;
      } else {
         double d0 = SHIFT_NOISE.getValue((double)i, 0.0D, (double)k) * 12.0D;
         double d1 = Mth.clamp((mutabledouble.doubleValue() + d0) / (double)(HEIGHT_BLENDING_RANGE_CELLS + 1), 0.0D, 1.0D);
         return d1 > 0.5D ? null : mutableobject.getValue();
      }
   }

   public static void generateBorderTicks(WorldGenRegion worldgenregion, ChunkAccess chunkaccess) {
      ChunkPos chunkpos = chunkaccess.getPos();
      boolean flag = chunkaccess.isOldNoiseGeneration();
      BlockPos.MutableBlockPos blockpos_mutableblockpos = new BlockPos.MutableBlockPos();
      BlockPos blockpos = new BlockPos(chunkpos.getMinBlockX(), 0, chunkpos.getMinBlockZ());
      BlendingData blendingdata = chunkaccess.getBlendingData();
      if (blendingdata != null) {
         int i = blendingdata.getAreaWithOldGeneration().getMinBuildHeight();
         int j = blendingdata.getAreaWithOldGeneration().getMaxBuildHeight() - 1;
         if (flag) {
            for(int k = 0; k < 16; ++k) {
               for(int l = 0; l < 16; ++l) {
                  generateBorderTick(chunkaccess, blockpos_mutableblockpos.setWithOffset(blockpos, k, i - 1, l));
                  generateBorderTick(chunkaccess, blockpos_mutableblockpos.setWithOffset(blockpos, k, i, l));
                  generateBorderTick(chunkaccess, blockpos_mutableblockpos.setWithOffset(blockpos, k, j, l));
                  generateBorderTick(chunkaccess, blockpos_mutableblockpos.setWithOffset(blockpos, k, j + 1, l));
               }
            }
         }

         for(Direction direction : Direction.Plane.HORIZONTAL) {
            if (worldgenregion.getChunk(chunkpos.x + direction.getStepX(), chunkpos.z + direction.getStepZ()).isOldNoiseGeneration() != flag) {
               int i1 = direction == Direction.EAST ? 15 : 0;
               int j1 = direction == Direction.WEST ? 0 : 15;
               int k1 = direction == Direction.SOUTH ? 15 : 0;
               int l1 = direction == Direction.NORTH ? 0 : 15;

               for(int i2 = i1; i2 <= j1; ++i2) {
                  for(int j2 = k1; j2 <= l1; ++j2) {
                     int k2 = Math.min(j, chunkaccess.getHeight(Heightmap.Types.MOTION_BLOCKING, i2, j2)) + 1;

                     for(int l2 = i; l2 < k2; ++l2) {
                        generateBorderTick(chunkaccess, blockpos_mutableblockpos.setWithOffset(blockpos, i2, l2, j2));
                     }
                  }
               }
            }
         }

      }
   }

   private static void generateBorderTick(ChunkAccess chunkaccess, BlockPos blockpos) {
      BlockState blockstate = chunkaccess.getBlockState(blockpos);
      if (blockstate.is(BlockTags.LEAVES)) {
         chunkaccess.markPosForPostprocessing(blockpos);
      }

      FluidState fluidstate = chunkaccess.getFluidState(blockpos);
      if (!fluidstate.isEmpty()) {
         chunkaccess.markPosForPostprocessing(blockpos);
      }

   }

   public static void addAroundOldChunksCarvingMaskFilter(WorldGenLevel worldgenlevel, ProtoChunk protochunk) {
      ChunkPos chunkpos = protochunk.getPos();
      ImmutableMap.Builder<Direction8, BlendingData> immutablemap_builder = ImmutableMap.builder();

      for(Direction8 direction8 : Direction8.values()) {
         int i = chunkpos.x + direction8.getStepX();
         int j = chunkpos.z + direction8.getStepZ();
         BlendingData blendingdata = worldgenlevel.getChunk(i, j).getBlendingData();
         if (blendingdata != null) {
            immutablemap_builder.put(direction8, blendingdata);
         }
      }

      ImmutableMap<Direction8, BlendingData> immutablemap = immutablemap_builder.build();
      if (protochunk.isOldNoiseGeneration() || !immutablemap.isEmpty()) {
         Blender.DistanceGetter blender_distancegetter = makeOldChunkDistanceGetter(protochunk.getBlendingData(), immutablemap);
         CarvingMask.Mask carvingmask_mask = (k, l, i1) -> {
            double d0 = (double)k + 0.5D + SHIFT_NOISE.getValue((double)k, (double)l, (double)i1) * 4.0D;
            double d1 = (double)l + 0.5D + SHIFT_NOISE.getValue((double)l, (double)i1, (double)k) * 4.0D;
            double d2 = (double)i1 + 0.5D + SHIFT_NOISE.getValue((double)i1, (double)k, (double)l) * 4.0D;
            return blender_distancegetter.getDistance(d0, d1, d2) < 4.0D;
         };
         Stream.of(GenerationStep.Carving.values()).map(protochunk::getOrCreateCarvingMask).forEach((carvingmask) -> carvingmask.setAdditionalMask(carvingmask_mask));
      }
   }

   public static Blender.DistanceGetter makeOldChunkDistanceGetter(@Nullable BlendingData blendingdata, Map<Direction8, BlendingData> map) {
      List<Blender.DistanceGetter> list = Lists.newArrayList();
      if (blendingdata != null) {
         list.add(makeOffsetOldChunkDistanceGetter((Direction8)null, blendingdata));
      }

      map.forEach((direction8, blendingdata1) -> list.add(makeOffsetOldChunkDistanceGetter(direction8, blendingdata1)));
      return (d0, d1, d2) -> {
         double d3 = Double.POSITIVE_INFINITY;

         for(Blender.DistanceGetter blender_distancegetter : list) {
            double d4 = blender_distancegetter.getDistance(d0, d1, d2);
            if (d4 < d3) {
               d3 = d4;
            }
         }

         return d3;
      };
   }

   private static Blender.DistanceGetter makeOffsetOldChunkDistanceGetter(@Nullable Direction8 direction8, BlendingData blendingdata) {
      double d0 = 0.0D;
      double d1 = 0.0D;
      if (direction8 != null) {
         for(Direction direction : direction8.getDirections()) {
            d0 += (double)(direction.getStepX() * 16);
            d1 += (double)(direction.getStepZ() * 16);
         }
      }

      double d2 = d0;
      double d3 = d1;
      double d4 = (double)blendingdata.getAreaWithOldGeneration().getHeight() / 2.0D;
      double d5 = (double)blendingdata.getAreaWithOldGeneration().getMinBuildHeight() + d4;
      return (d10, d11, d12) -> distanceToCube(d10 - 8.0D - d2, d11 - d5, d12 - 8.0D - d3, 8.0D, d4, 8.0D);
   }

   private static double distanceToCube(double d0, double d1, double d2, double d3, double d4, double d5) {
      double d6 = Math.abs(d0) - d3;
      double d7 = Math.abs(d1) - d4;
      double d8 = Math.abs(d2) - d5;
      return Mth.length(Math.max(0.0D, d6), Math.max(0.0D, d7), Math.max(0.0D, d8));
   }

   public static record BlendingOutput(double alpha, double blendingOffset) {
   }

   interface CellValueGetter {
      double get(BlendingData blendingdata, int i, int j, int k);
   }

   public interface DistanceGetter {
      double getDistance(double d0, double d1, double d2);
   }
}
