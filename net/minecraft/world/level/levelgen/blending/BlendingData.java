package net.minecraft.world.level.levelgen.blending;

import com.google.common.primitives.Doubles;
import com.mojang.serialization.Codec;
import com.mojang.serialization.DataResult;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import it.unimi.dsi.fastutil.objects.ObjectArrayList;
import java.util.Arrays;
import java.util.EnumSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.function.Function;
import java.util.stream.DoubleStream;
import javax.annotation.Nullable;
import net.minecraft.Util;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.Direction8;
import net.minecraft.core.Holder;
import net.minecraft.core.QuartPos;
import net.minecraft.core.SectionPos;
import net.minecraft.server.level.WorldGenRegion;
import net.minecraft.tags.BlockTags;
import net.minecraft.util.Mth;
import net.minecraft.world.level.LevelHeightAccessor;
import net.minecraft.world.level.WorldGenLevel;
import net.minecraft.world.level.biome.Biome;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.chunk.ChunkAccess;
import net.minecraft.world.level.chunk.ChunkStatus;
import net.minecraft.world.level.levelgen.Heightmap;

public class BlendingData {
   private static final double BLENDING_DENSITY_FACTOR = 0.1D;
   protected static final int CELL_WIDTH = 4;
   protected static final int CELL_HEIGHT = 8;
   protected static final int CELL_RATIO = 2;
   private static final double SOLID_DENSITY = 1.0D;
   private static final double AIR_DENSITY = -1.0D;
   private static final int CELLS_PER_SECTION_Y = 2;
   private static final int QUARTS_PER_SECTION = QuartPos.fromBlock(16);
   private static final int CELL_HORIZONTAL_MAX_INDEX_INSIDE = QUARTS_PER_SECTION - 1;
   private static final int CELL_HORIZONTAL_MAX_INDEX_OUTSIDE = QUARTS_PER_SECTION;
   private static final int CELL_COLUMN_INSIDE_COUNT = 2 * CELL_HORIZONTAL_MAX_INDEX_INSIDE + 1;
   private static final int CELL_COLUMN_OUTSIDE_COUNT = 2 * CELL_HORIZONTAL_MAX_INDEX_OUTSIDE + 1;
   private static final int CELL_COLUMN_COUNT = CELL_COLUMN_INSIDE_COUNT + CELL_COLUMN_OUTSIDE_COUNT;
   private final LevelHeightAccessor areaWithOldGeneration;
   private static final List<Block> SURFACE_BLOCKS = List.of(Blocks.PODZOL, Blocks.GRAVEL, Blocks.GRASS_BLOCK, Blocks.STONE, Blocks.COARSE_DIRT, Blocks.SAND, Blocks.RED_SAND, Blocks.MYCELIUM, Blocks.SNOW_BLOCK, Blocks.TERRACOTTA, Blocks.DIRT);
   protected static final double NO_VALUE = Double.MAX_VALUE;
   private boolean hasCalculatedData;
   private final double[] heights;
   private final List<List<Holder<Biome>>> biomes;
   private final transient double[][] densities;
   private static final Codec<double[]> DOUBLE_ARRAY_CODEC = Codec.DOUBLE.listOf().xmap(Doubles::toArray, Doubles::asList);
   public static final Codec<BlendingData> CODEC = RecordCodecBuilder.create((recordcodecbuilder_instance) -> recordcodecbuilder_instance.group(Codec.INT.fieldOf("min_section").forGetter((blendingdata2) -> blendingdata2.areaWithOldGeneration.getMinSection()), Codec.INT.fieldOf("max_section").forGetter((blendingdata1) -> blendingdata1.areaWithOldGeneration.getMaxSection()), DOUBLE_ARRAY_CODEC.optionalFieldOf("heights").forGetter((blendingdata) -> DoubleStream.of(blendingdata.heights).anyMatch((d0) -> d0 != Double.MAX_VALUE) ? Optional.of(blendingdata.heights) : Optional.empty())).apply(recordcodecbuilder_instance, BlendingData::new)).comapFlatMap(BlendingData::validateArraySize, Function.identity());

   private static DataResult<BlendingData> validateArraySize(BlendingData blendingdata) {
      return blendingdata.heights.length != CELL_COLUMN_COUNT ? DataResult.error(() -> "heights has to be of length " + CELL_COLUMN_COUNT) : DataResult.success(blendingdata);
   }

   private BlendingData(int i, int j, Optional<double[]> optional) {
      this.heights = optional.orElse(Util.make(new double[CELL_COLUMN_COUNT], (adouble) -> Arrays.fill(adouble, Double.MAX_VALUE)));
      this.densities = new double[CELL_COLUMN_COUNT][];
      ObjectArrayList<List<Holder<Biome>>> objectarraylist = new ObjectArrayList<>(CELL_COLUMN_COUNT);
      objectarraylist.size(CELL_COLUMN_COUNT);
      this.biomes = objectarraylist;
      int k = SectionPos.sectionToBlockCoord(i);
      int l = SectionPos.sectionToBlockCoord(j) - k;
      this.areaWithOldGeneration = LevelHeightAccessor.create(k, l);
   }

   @Nullable
   public static BlendingData getOrUpdateBlendingData(WorldGenRegion worldgenregion, int i, int j) {
      ChunkAccess chunkaccess = worldgenregion.getChunk(i, j);
      BlendingData blendingdata = chunkaccess.getBlendingData();
      if (blendingdata != null && chunkaccess.getHighestGeneratedStatus().isOrAfter(ChunkStatus.BIOMES)) {
         blendingdata.calculateData(chunkaccess, sideByGenerationAge(worldgenregion, i, j, false));
         return blendingdata;
      } else {
         return null;
      }
   }

   public static Set<Direction8> sideByGenerationAge(WorldGenLevel worldgenlevel, int i, int j, boolean flag) {
      Set<Direction8> set = EnumSet.noneOf(Direction8.class);

      for(Direction8 direction8 : Direction8.values()) {
         int k = i + direction8.getStepX();
         int l = j + direction8.getStepZ();
         if (worldgenlevel.getChunk(k, l).isOldNoiseGeneration() == flag) {
            set.add(direction8);
         }
      }

      return set;
   }

   private void calculateData(ChunkAccess chunkaccess, Set<Direction8> set) {
      if (!this.hasCalculatedData) {
         if (set.contains(Direction8.NORTH) || set.contains(Direction8.WEST) || set.contains(Direction8.NORTH_WEST)) {
            this.addValuesForColumn(getInsideIndex(0, 0), chunkaccess, 0, 0);
         }

         if (set.contains(Direction8.NORTH)) {
            for(int i = 1; i < QUARTS_PER_SECTION; ++i) {
               this.addValuesForColumn(getInsideIndex(i, 0), chunkaccess, 4 * i, 0);
            }
         }

         if (set.contains(Direction8.WEST)) {
            for(int j = 1; j < QUARTS_PER_SECTION; ++j) {
               this.addValuesForColumn(getInsideIndex(0, j), chunkaccess, 0, 4 * j);
            }
         }

         if (set.contains(Direction8.EAST)) {
            for(int k = 1; k < QUARTS_PER_SECTION; ++k) {
               this.addValuesForColumn(getOutsideIndex(CELL_HORIZONTAL_MAX_INDEX_OUTSIDE, k), chunkaccess, 15, 4 * k);
            }
         }

         if (set.contains(Direction8.SOUTH)) {
            for(int l = 0; l < QUARTS_PER_SECTION; ++l) {
               this.addValuesForColumn(getOutsideIndex(l, CELL_HORIZONTAL_MAX_INDEX_OUTSIDE), chunkaccess, 4 * l, 15);
            }
         }

         if (set.contains(Direction8.EAST) && set.contains(Direction8.NORTH_EAST)) {
            this.addValuesForColumn(getOutsideIndex(CELL_HORIZONTAL_MAX_INDEX_OUTSIDE, 0), chunkaccess, 15, 0);
         }

         if (set.contains(Direction8.EAST) && set.contains(Direction8.SOUTH) && set.contains(Direction8.SOUTH_EAST)) {
            this.addValuesForColumn(getOutsideIndex(CELL_HORIZONTAL_MAX_INDEX_OUTSIDE, CELL_HORIZONTAL_MAX_INDEX_OUTSIDE), chunkaccess, 15, 15);
         }

         this.hasCalculatedData = true;
      }
   }

   private void addValuesForColumn(int i, ChunkAccess chunkaccess, int j, int k) {
      if (this.heights[i] == Double.MAX_VALUE) {
         this.heights[i] = (double)this.getHeightAtXZ(chunkaccess, j, k);
      }

      this.densities[i] = this.getDensityColumn(chunkaccess, j, k, Mth.floor(this.heights[i]));
      this.biomes.set(i, this.getBiomeColumn(chunkaccess, j, k));
   }

   private int getHeightAtXZ(ChunkAccess chunkaccess, int i, int j) {
      int k;
      if (chunkaccess.hasPrimedHeightmap(Heightmap.Types.WORLD_SURFACE_WG)) {
         k = Math.min(chunkaccess.getHeight(Heightmap.Types.WORLD_SURFACE_WG, i, j) + 1, this.areaWithOldGeneration.getMaxBuildHeight());
      } else {
         k = this.areaWithOldGeneration.getMaxBuildHeight();
      }

      int i1 = this.areaWithOldGeneration.getMinBuildHeight();
      BlockPos.MutableBlockPos blockpos_mutableblockpos = new BlockPos.MutableBlockPos(i, k, j);

      while(blockpos_mutableblockpos.getY() > i1) {
         blockpos_mutableblockpos.move(Direction.DOWN);
         if (SURFACE_BLOCKS.contains(chunkaccess.getBlockState(blockpos_mutableblockpos).getBlock())) {
            return blockpos_mutableblockpos.getY();
         }
      }

      return i1;
   }

   private static double read1(ChunkAccess chunkaccess, BlockPos.MutableBlockPos blockpos_mutableblockpos) {
      return isGround(chunkaccess, blockpos_mutableblockpos.move(Direction.DOWN)) ? 1.0D : -1.0D;
   }

   private static double read7(ChunkAccess chunkaccess, BlockPos.MutableBlockPos blockpos_mutableblockpos) {
      double d0 = 0.0D;

      for(int i = 0; i < 7; ++i) {
         d0 += read1(chunkaccess, blockpos_mutableblockpos);
      }

      return d0;
   }

   private double[] getDensityColumn(ChunkAccess chunkaccess, int i, int j, int k) {
      double[] adouble = new double[this.cellCountPerColumn()];
      Arrays.fill(adouble, -1.0D);
      BlockPos.MutableBlockPos blockpos_mutableblockpos = new BlockPos.MutableBlockPos(i, this.areaWithOldGeneration.getMaxBuildHeight(), j);
      double d0 = read7(chunkaccess, blockpos_mutableblockpos);

      for(int l = adouble.length - 2; l >= 0; --l) {
         double d1 = read1(chunkaccess, blockpos_mutableblockpos);
         double d2 = read7(chunkaccess, blockpos_mutableblockpos);
         adouble[l] = (d0 + d1 + d2) / 15.0D;
         d0 = d2;
      }

      int i1 = this.getCellYIndex(Mth.floorDiv(k, 8));
      if (i1 >= 0 && i1 < adouble.length - 1) {
         double d3 = ((double)k + 0.5D) % 8.0D / 8.0D;
         double d4 = (1.0D - d3) / d3;
         double d5 = Math.max(d4, 1.0D) * 0.25D;
         adouble[i1 + 1] = -d4 / d5;
         adouble[i1] = 1.0D / d5;
      }

      return adouble;
   }

   private List<Holder<Biome>> getBiomeColumn(ChunkAccess chunkaccess, int i, int j) {
      ObjectArrayList<Holder<Biome>> objectarraylist = new ObjectArrayList<>(this.quartCountPerColumn());
      objectarraylist.size(this.quartCountPerColumn());

      for(int k = 0; k < objectarraylist.size(); ++k) {
         int l = k + QuartPos.fromBlock(this.areaWithOldGeneration.getMinBuildHeight());
         objectarraylist.set(k, chunkaccess.getNoiseBiome(QuartPos.fromBlock(i), l, QuartPos.fromBlock(j)));
      }

      return objectarraylist;
   }

   private static boolean isGround(ChunkAccess chunkaccess, BlockPos blockpos) {
      BlockState blockstate = chunkaccess.getBlockState(blockpos);
      if (blockstate.isAir()) {
         return false;
      } else if (blockstate.is(BlockTags.LEAVES)) {
         return false;
      } else if (blockstate.is(BlockTags.LOGS)) {
         return false;
      } else if (!blockstate.is(Blocks.BROWN_MUSHROOM_BLOCK) && !blockstate.is(Blocks.RED_MUSHROOM_BLOCK)) {
         return !blockstate.getCollisionShape(chunkaccess, blockpos).isEmpty();
      } else {
         return false;
      }
   }

   protected double getHeight(int i, int j, int k) {
      if (i != CELL_HORIZONTAL_MAX_INDEX_OUTSIDE && k != CELL_HORIZONTAL_MAX_INDEX_OUTSIDE) {
         return i != 0 && k != 0 ? Double.MAX_VALUE : this.heights[getInsideIndex(i, k)];
      } else {
         return this.heights[getOutsideIndex(i, k)];
      }
   }

   private double getDensity(@Nullable double[] adouble, int i) {
      if (adouble == null) {
         return Double.MAX_VALUE;
      } else {
         int j = this.getCellYIndex(i);
         return j >= 0 && j < adouble.length ? adouble[j] * 0.1D : Double.MAX_VALUE;
      }
   }

   protected double getDensity(int i, int j, int k) {
      if (j == this.getMinY()) {
         return 0.1D;
      } else if (i != CELL_HORIZONTAL_MAX_INDEX_OUTSIDE && k != CELL_HORIZONTAL_MAX_INDEX_OUTSIDE) {
         return i != 0 && k != 0 ? Double.MAX_VALUE : this.getDensity(this.densities[getInsideIndex(i, k)], j);
      } else {
         return this.getDensity(this.densities[getOutsideIndex(i, k)], j);
      }
   }

   protected void iterateBiomes(int i, int j, int k, BlendingData.BiomeConsumer blendingdata_biomeconsumer) {
      if (j >= QuartPos.fromBlock(this.areaWithOldGeneration.getMinBuildHeight()) && j < QuartPos.fromBlock(this.areaWithOldGeneration.getMaxBuildHeight())) {
         int l = j - QuartPos.fromBlock(this.areaWithOldGeneration.getMinBuildHeight());

         for(int i1 = 0; i1 < this.biomes.size(); ++i1) {
            if (this.biomes.get(i1) != null) {
               Holder<Biome> holder = this.biomes.get(i1).get(l);
               if (holder != null) {
                  blendingdata_biomeconsumer.consume(i + getX(i1), k + getZ(i1), holder);
               }
            }
         }

      }
   }

   protected void iterateHeights(int i, int j, BlendingData.HeightConsumer blendingdata_heightconsumer) {
      for(int k = 0; k < this.heights.length; ++k) {
         double d0 = this.heights[k];
         if (d0 != Double.MAX_VALUE) {
            blendingdata_heightconsumer.consume(i + getX(k), j + getZ(k), d0);
         }
      }

   }

   protected void iterateDensities(int i, int j, int k, int l, BlendingData.DensityConsumer blendingdata_densityconsumer) {
      int i1 = this.getColumnMinY();
      int j1 = Math.max(0, k - i1);
      int k1 = Math.min(this.cellCountPerColumn(), l - i1);

      for(int l1 = 0; l1 < this.densities.length; ++l1) {
         double[] adouble = this.densities[l1];
         if (adouble != null) {
            int i2 = i + getX(l1);
            int j2 = j + getZ(l1);

            for(int k2 = j1; k2 < k1; ++k2) {
               blendingdata_densityconsumer.consume(i2, k2 + i1, j2, adouble[k2] * 0.1D);
            }
         }
      }

   }

   private int cellCountPerColumn() {
      return this.areaWithOldGeneration.getSectionsCount() * 2;
   }

   private int quartCountPerColumn() {
      return QuartPos.fromSection(this.areaWithOldGeneration.getSectionsCount());
   }

   private int getColumnMinY() {
      return this.getMinY() + 1;
   }

   private int getMinY() {
      return this.areaWithOldGeneration.getMinSection() * 2;
   }

   private int getCellYIndex(int i) {
      return i - this.getColumnMinY();
   }

   private static int getInsideIndex(int i, int j) {
      return CELL_HORIZONTAL_MAX_INDEX_INSIDE - i + j;
   }

   private static int getOutsideIndex(int i, int j) {
      return CELL_COLUMN_INSIDE_COUNT + i + CELL_HORIZONTAL_MAX_INDEX_OUTSIDE - j;
   }

   private static int getX(int i) {
      if (i < CELL_COLUMN_INSIDE_COUNT) {
         return zeroIfNegative(CELL_HORIZONTAL_MAX_INDEX_INSIDE - i);
      } else {
         int j = i - CELL_COLUMN_INSIDE_COUNT;
         return CELL_HORIZONTAL_MAX_INDEX_OUTSIDE - zeroIfNegative(CELL_HORIZONTAL_MAX_INDEX_OUTSIDE - j);
      }
   }

   private static int getZ(int i) {
      if (i < CELL_COLUMN_INSIDE_COUNT) {
         return zeroIfNegative(i - CELL_HORIZONTAL_MAX_INDEX_INSIDE);
      } else {
         int j = i - CELL_COLUMN_INSIDE_COUNT;
         return CELL_HORIZONTAL_MAX_INDEX_OUTSIDE - zeroIfNegative(j - CELL_HORIZONTAL_MAX_INDEX_OUTSIDE);
      }
   }

   private static int zeroIfNegative(int i) {
      return i & ~(i >> 31);
   }

   public LevelHeightAccessor getAreaWithOldGeneration() {
      return this.areaWithOldGeneration;
   }

   protected interface BiomeConsumer {
      void consume(int i, int j, Holder<Biome> holder);
   }

   protected interface DensityConsumer {
      void consume(int i, int j, int k, double d0);
   }

   protected interface HeightConsumer {
      void consume(int i, int j, double d0);
   }
}
