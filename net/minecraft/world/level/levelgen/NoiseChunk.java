package net.minecraft.world.level.levelgen;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.Lists;
import it.unimi.dsi.fastutil.longs.Long2IntMap;
import it.unimi.dsi.fastutil.longs.Long2IntOpenHashMap;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import javax.annotation.Nullable;
import net.minecraft.core.QuartPos;
import net.minecraft.core.SectionPos;
import net.minecraft.server.level.ColumnPos;
import net.minecraft.util.KeyDispatchDataCodec;
import net.minecraft.util.Mth;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.biome.Climate;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.chunk.ChunkAccess;
import net.minecraft.world.level.levelgen.blending.Blender;
import net.minecraft.world.level.levelgen.material.MaterialRuleList;

public class NoiseChunk implements DensityFunction.ContextProvider, DensityFunction.FunctionContext {
   private final NoiseSettings noiseSettings;
   final int cellCountXZ;
   final int cellCountY;
   final int cellNoiseMinY;
   private final int firstCellX;
   private final int firstCellZ;
   final int firstNoiseX;
   final int firstNoiseZ;
   final List<NoiseChunk.NoiseInterpolator> interpolators;
   final List<NoiseChunk.CacheAllInCell> cellCaches;
   private final Map<DensityFunction, DensityFunction> wrapped = new HashMap<>();
   private final Long2IntMap preliminarySurfaceLevel = new Long2IntOpenHashMap();
   private final Aquifer aquifer;
   private final DensityFunction initialDensityNoJaggedness;
   private final NoiseChunk.BlockStateFiller blockStateRule;
   private final Blender blender;
   private final NoiseChunk.FlatCache blendAlpha;
   private final NoiseChunk.FlatCache blendOffset;
   private final DensityFunctions.BeardifierOrMarker beardifier;
   private long lastBlendingDataPos = ChunkPos.INVALID_CHUNK_POS;
   private Blender.BlendingOutput lastBlendingOutput = new Blender.BlendingOutput(1.0D, 0.0D);
   final int noiseSizeXZ;
   final int cellWidth;
   final int cellHeight;
   boolean interpolating;
   boolean fillingCell;
   private int cellStartBlockX;
   int cellStartBlockY;
   private int cellStartBlockZ;
   int inCellX;
   int inCellY;
   int inCellZ;
   long interpolationCounter;
   long arrayInterpolationCounter;
   int arrayIndex;
   private final DensityFunction.ContextProvider sliceFillingContextProvider = new DensityFunction.ContextProvider() {
      public DensityFunction.FunctionContext forIndex(int i) {
         NoiseChunk.this.cellStartBlockY = (i + NoiseChunk.this.cellNoiseMinY) * NoiseChunk.this.cellHeight;
         ++NoiseChunk.this.interpolationCounter;
         NoiseChunk.this.inCellY = 0;
         NoiseChunk.this.arrayIndex = i;
         return NoiseChunk.this;
      }

      public void fillAllDirectly(double[] adouble, DensityFunction densityfunction) {
         for(int i = 0; i < NoiseChunk.this.cellCountY + 1; ++i) {
            NoiseChunk.this.cellStartBlockY = (i + NoiseChunk.this.cellNoiseMinY) * NoiseChunk.this.cellHeight;
            ++NoiseChunk.this.interpolationCounter;
            NoiseChunk.this.inCellY = 0;
            NoiseChunk.this.arrayIndex = i;
            adouble[i] = densityfunction.compute(NoiseChunk.this);
         }

      }
   };

   public static NoiseChunk forChunk(ChunkAccess chunkaccess, RandomState randomstate, DensityFunctions.BeardifierOrMarker densityfunctions_beardifierormarker, NoiseGeneratorSettings noisegeneratorsettings, Aquifer.FluidPicker aquifer_fluidpicker, Blender blender) {
      NoiseSettings noisesettings = noisegeneratorsettings.noiseSettings().clampToHeightAccessor(chunkaccess);
      ChunkPos chunkpos = chunkaccess.getPos();
      int i = 16 / noisesettings.getCellWidth();
      return new NoiseChunk(i, randomstate, chunkpos.getMinBlockX(), chunkpos.getMinBlockZ(), noisesettings, densityfunctions_beardifierormarker, noisegeneratorsettings, aquifer_fluidpicker, blender);
   }

   public NoiseChunk(int i, RandomState randomstate, int j, int k, NoiseSettings noisesettings, DensityFunctions.BeardifierOrMarker densityfunctions_beardifierormarker, NoiseGeneratorSettings noisegeneratorsettings, Aquifer.FluidPicker aquifer_fluidpicker, Blender blender) {
      this.noiseSettings = noisesettings;
      this.cellWidth = noisesettings.getCellWidth();
      this.cellHeight = noisesettings.getCellHeight();
      this.cellCountXZ = i;
      this.cellCountY = Mth.floorDiv(noisesettings.height(), this.cellHeight);
      this.cellNoiseMinY = Mth.floorDiv(noisesettings.minY(), this.cellHeight);
      this.firstCellX = Math.floorDiv(j, this.cellWidth);
      this.firstCellZ = Math.floorDiv(k, this.cellWidth);
      this.interpolators = Lists.newArrayList();
      this.cellCaches = Lists.newArrayList();
      this.firstNoiseX = QuartPos.fromBlock(j);
      this.firstNoiseZ = QuartPos.fromBlock(k);
      this.noiseSizeXZ = QuartPos.fromBlock(i * this.cellWidth);
      this.blender = blender;
      this.beardifier = densityfunctions_beardifierormarker;
      this.blendAlpha = new NoiseChunk.FlatCache(new NoiseChunk.BlendAlpha(), false);
      this.blendOffset = new NoiseChunk.FlatCache(new NoiseChunk.BlendOffset(), false);

      for(int l = 0; l <= this.noiseSizeXZ; ++l) {
         int i1 = this.firstNoiseX + l;
         int j1 = QuartPos.toBlock(i1);

         for(int k1 = 0; k1 <= this.noiseSizeXZ; ++k1) {
            int l1 = this.firstNoiseZ + k1;
            int i2 = QuartPos.toBlock(l1);
            Blender.BlendingOutput blender_blendingoutput = blender.blendOffsetAndFactor(j1, i2);
            this.blendAlpha.values[l][k1] = blender_blendingoutput.alpha();
            this.blendOffset.values[l][k1] = blender_blendingoutput.blendingOffset();
         }
      }

      NoiseRouter noiserouter = randomstate.router();
      NoiseRouter noiserouter1 = noiserouter.mapAll(this::wrap);
      if (!noisegeneratorsettings.isAquifersEnabled()) {
         this.aquifer = Aquifer.createDisabled(aquifer_fluidpicker);
      } else {
         int j2 = SectionPos.blockToSectionCoord(j);
         int k2 = SectionPos.blockToSectionCoord(k);
         this.aquifer = Aquifer.create(this, new ChunkPos(j2, k2), noiserouter1, randomstate.aquiferRandom(), noisesettings.minY(), noisesettings.height(), aquifer_fluidpicker);
      }

      ImmutableList.Builder<NoiseChunk.BlockStateFiller> immutablelist_builder = ImmutableList.builder();
      DensityFunction densityfunction = DensityFunctions.cacheAllInCell(DensityFunctions.add(noiserouter1.finalDensity(), DensityFunctions.BeardifierMarker.INSTANCE)).mapAll(this::wrap);
      immutablelist_builder.add((densityfunction_functioncontext) -> this.aquifer.computeSubstance(densityfunction_functioncontext, densityfunction.compute(densityfunction_functioncontext)));
      if (noisegeneratorsettings.oreVeinsEnabled()) {
         immutablelist_builder.add(OreVeinifier.create(noiserouter1.veinToggle(), noiserouter1.veinRidged(), noiserouter1.veinGap(), randomstate.oreRandom()));
      }

      this.blockStateRule = new MaterialRuleList(immutablelist_builder.build());
      this.initialDensityNoJaggedness = noiserouter1.initialDensityWithoutJaggedness();
   }

   protected Climate.Sampler cachedClimateSampler(NoiseRouter noiserouter, List<Climate.ParameterPoint> list) {
      return new Climate.Sampler(noiserouter.temperature().mapAll(this::wrap), noiserouter.vegetation().mapAll(this::wrap), noiserouter.continents().mapAll(this::wrap), noiserouter.erosion().mapAll(this::wrap), noiserouter.depth().mapAll(this::wrap), noiserouter.ridges().mapAll(this::wrap), list);
   }

   @Nullable
   protected BlockState getInterpolatedState() {
      return this.blockStateRule.calculate(this);
   }

   public int blockX() {
      return this.cellStartBlockX + this.inCellX;
   }

   public int blockY() {
      return this.cellStartBlockY + this.inCellY;
   }

   public int blockZ() {
      return this.cellStartBlockZ + this.inCellZ;
   }

   public int preliminarySurfaceLevel(int i, int j) {
      int k = QuartPos.toBlock(QuartPos.fromBlock(i));
      int l = QuartPos.toBlock(QuartPos.fromBlock(j));
      return this.preliminarySurfaceLevel.computeIfAbsent(ColumnPos.asLong(k, l), this::computePreliminarySurfaceLevel);
   }

   private int computePreliminarySurfaceLevel(long i1) {
      int j1 = ColumnPos.getX(i1);
      int k1 = ColumnPos.getZ(i1);
      int l1 = this.noiseSettings.minY();

      for(int i2 = l1 + this.noiseSettings.height(); i2 >= l1; i2 -= this.cellHeight) {
         if (this.initialDensityNoJaggedness.compute(new DensityFunction.SinglePointContext(j1, i2, k1)) > 0.390625D) {
            return i2;
         }
      }

      return Integer.MAX_VALUE;
   }

   public Blender getBlender() {
      return this.blender;
   }

   private void fillSlice(boolean flag, int i) {
      this.cellStartBlockX = i * this.cellWidth;
      this.inCellX = 0;

      for(int j = 0; j < this.cellCountXZ + 1; ++j) {
         int k = this.firstCellZ + j;
         this.cellStartBlockZ = k * this.cellWidth;
         this.inCellZ = 0;
         ++this.arrayInterpolationCounter;

         for(NoiseChunk.NoiseInterpolator noisechunk_noiseinterpolator : this.interpolators) {
            double[] adouble = (flag ? noisechunk_noiseinterpolator.slice0 : noisechunk_noiseinterpolator.slice1)[j];
            noisechunk_noiseinterpolator.fillArray(adouble, this.sliceFillingContextProvider);
         }
      }

      ++this.arrayInterpolationCounter;
   }

   public void initializeForFirstCellX() {
      if (this.interpolating) {
         throw new IllegalStateException("Staring interpolation twice");
      } else {
         this.interpolating = true;
         this.interpolationCounter = 0L;
         this.fillSlice(true, this.firstCellX);
      }
   }

   public void advanceCellX(int i) {
      this.fillSlice(false, this.firstCellX + i + 1);
      this.cellStartBlockX = (this.firstCellX + i) * this.cellWidth;
   }

   public NoiseChunk forIndex(int i) {
      int j = Math.floorMod(i, this.cellWidth);
      int k = Math.floorDiv(i, this.cellWidth);
      int l = Math.floorMod(k, this.cellWidth);
      int i1 = this.cellHeight - 1 - Math.floorDiv(k, this.cellWidth);
      this.inCellX = l;
      this.inCellY = i1;
      this.inCellZ = j;
      this.arrayIndex = i;
      return this;
   }

   public void fillAllDirectly(double[] adouble, DensityFunction densityfunction) {
      this.arrayIndex = 0;

      for(int i = this.cellHeight - 1; i >= 0; --i) {
         this.inCellY = i;

         for(int j = 0; j < this.cellWidth; ++j) {
            this.inCellX = j;

            for(int k = 0; k < this.cellWidth; ++k) {
               this.inCellZ = k;
               adouble[this.arrayIndex++] = densityfunction.compute(this);
            }
         }
      }

   }

   public void selectCellYZ(int i, int j) {
      this.interpolators.forEach((noisechunk_noiseinterpolator) -> noisechunk_noiseinterpolator.selectCellYZ(i, j));
      this.fillingCell = true;
      this.cellStartBlockY = (i + this.cellNoiseMinY) * this.cellHeight;
      this.cellStartBlockZ = (this.firstCellZ + j) * this.cellWidth;
      ++this.arrayInterpolationCounter;

      for(NoiseChunk.CacheAllInCell noisechunk_cacheallincell : this.cellCaches) {
         noisechunk_cacheallincell.noiseFiller.fillArray(noisechunk_cacheallincell.values, this);
      }

      ++this.arrayInterpolationCounter;
      this.fillingCell = false;
   }

   public void updateForY(int i, double d0) {
      this.inCellY = i - this.cellStartBlockY;
      this.interpolators.forEach((noisechunk_noiseinterpolator) -> noisechunk_noiseinterpolator.updateForY(d0));
   }

   public void updateForX(int i, double d0) {
      this.inCellX = i - this.cellStartBlockX;
      this.interpolators.forEach((noisechunk_noiseinterpolator) -> noisechunk_noiseinterpolator.updateForX(d0));
   }

   public void updateForZ(int i, double d0) {
      this.inCellZ = i - this.cellStartBlockZ;
      ++this.interpolationCounter;
      this.interpolators.forEach((noisechunk_noiseinterpolator) -> noisechunk_noiseinterpolator.updateForZ(d0));
   }

   public void stopInterpolation() {
      if (!this.interpolating) {
         throw new IllegalStateException("Staring interpolation twice");
      } else {
         this.interpolating = false;
      }
   }

   public void swapSlices() {
      this.interpolators.forEach(NoiseChunk.NoiseInterpolator::swapSlices);
   }

   public Aquifer aquifer() {
      return this.aquifer;
   }

   protected int cellWidth() {
      return this.cellWidth;
   }

   protected int cellHeight() {
      return this.cellHeight;
   }

   Blender.BlendingOutput getOrComputeBlendingOutput(int i, int j) {
      long k = ChunkPos.asLong(i, j);
      if (this.lastBlendingDataPos == k) {
         return this.lastBlendingOutput;
      } else {
         this.lastBlendingDataPos = k;
         Blender.BlendingOutput blender_blendingoutput = this.blender.blendOffsetAndFactor(i, j);
         this.lastBlendingOutput = blender_blendingoutput;
         return blender_blendingoutput;
      }
   }

   protected DensityFunction wrap(DensityFunction densityfunction) {
      return this.wrapped.computeIfAbsent(densityfunction, this::wrapNew);
   }

   private DensityFunction wrapNew(DensityFunction densityfunction1) {
      if (densityfunction1 instanceof DensityFunctions.Marker) {
         DensityFunctions.Marker densityfunctions_marker = (DensityFunctions.Marker)densityfunction1;
         Object var10000;
         switch (densityfunctions_marker.type()) {
            case Interpolated:
               var10000 = new NoiseChunk.NoiseInterpolator(densityfunctions_marker.wrapped());
               break;
            case FlatCache:
               var10000 = new NoiseChunk.FlatCache(densityfunctions_marker.wrapped(), true);
               break;
            case Cache2D:
               var10000 = new NoiseChunk.Cache2D(densityfunctions_marker.wrapped());
               break;
            case CacheOnce:
               var10000 = new NoiseChunk.CacheOnce(densityfunctions_marker.wrapped());
               break;
            case CacheAllInCell:
               var10000 = new NoiseChunk.CacheAllInCell(densityfunctions_marker.wrapped());
               break;
            default:
               throw new IncompatibleClassChangeError();
         }

         return (DensityFunction)var10000;
      } else {
         if (this.blender != Blender.empty()) {
            if (densityfunction1 == DensityFunctions.BlendAlpha.INSTANCE) {
               return this.blendAlpha;
            }

            if (densityfunction1 == DensityFunctions.BlendOffset.INSTANCE) {
               return this.blendOffset;
            }
         }

         if (densityfunction1 == DensityFunctions.BeardifierMarker.INSTANCE) {
            return this.beardifier;
         } else if (densityfunction1 instanceof DensityFunctions.HolderHolder) {
            DensityFunctions.HolderHolder densityfunctions_holderholder = (DensityFunctions.HolderHolder)densityfunction1;
            return densityfunctions_holderholder.function().value();
         } else {
            return densityfunction1;
         }
      }
   }

   class BlendAlpha implements NoiseChunk.NoiseChunkDensityFunction {
      public DensityFunction wrapped() {
         return DensityFunctions.BlendAlpha.INSTANCE;
      }

      public DensityFunction mapAll(DensityFunction.Visitor densityfunction_visitor) {
         return this.wrapped().mapAll(densityfunction_visitor);
      }

      public double compute(DensityFunction.FunctionContext densityfunction_functioncontext) {
         return NoiseChunk.this.getOrComputeBlendingOutput(densityfunction_functioncontext.blockX(), densityfunction_functioncontext.blockZ()).alpha();
      }

      public void fillArray(double[] adouble, DensityFunction.ContextProvider densityfunction_contextprovider) {
         densityfunction_contextprovider.fillAllDirectly(adouble, this);
      }

      public double minValue() {
         return 0.0D;
      }

      public double maxValue() {
         return 1.0D;
      }

      public KeyDispatchDataCodec<? extends DensityFunction> codec() {
         return DensityFunctions.BlendAlpha.CODEC;
      }
   }

   class BlendOffset implements NoiseChunk.NoiseChunkDensityFunction {
      public DensityFunction wrapped() {
         return DensityFunctions.BlendOffset.INSTANCE;
      }

      public DensityFunction mapAll(DensityFunction.Visitor densityfunction_visitor) {
         return this.wrapped().mapAll(densityfunction_visitor);
      }

      public double compute(DensityFunction.FunctionContext densityfunction_functioncontext) {
         return NoiseChunk.this.getOrComputeBlendingOutput(densityfunction_functioncontext.blockX(), densityfunction_functioncontext.blockZ()).blendingOffset();
      }

      public void fillArray(double[] adouble, DensityFunction.ContextProvider densityfunction_contextprovider) {
         densityfunction_contextprovider.fillAllDirectly(adouble, this);
      }

      public double minValue() {
         return Double.NEGATIVE_INFINITY;
      }

      public double maxValue() {
         return Double.POSITIVE_INFINITY;
      }

      public KeyDispatchDataCodec<? extends DensityFunction> codec() {
         return DensityFunctions.BlendOffset.CODEC;
      }
   }

   @FunctionalInterface
   public interface BlockStateFiller {
      @Nullable
      BlockState calculate(DensityFunction.FunctionContext densityfunction_functioncontext);
   }

   static class Cache2D implements DensityFunctions.MarkerOrMarked, NoiseChunk.NoiseChunkDensityFunction {
      private final DensityFunction function;
      private long lastPos2D = ChunkPos.INVALID_CHUNK_POS;
      private double lastValue;

      Cache2D(DensityFunction densityfunction) {
         this.function = densityfunction;
      }

      public double compute(DensityFunction.FunctionContext densityfunction_functioncontext) {
         int i = densityfunction_functioncontext.blockX();
         int j = densityfunction_functioncontext.blockZ();
         long k = ChunkPos.asLong(i, j);
         if (this.lastPos2D == k) {
            return this.lastValue;
         } else {
            this.lastPos2D = k;
            double d0 = this.function.compute(densityfunction_functioncontext);
            this.lastValue = d0;
            return d0;
         }
      }

      public void fillArray(double[] adouble, DensityFunction.ContextProvider densityfunction_contextprovider) {
         this.function.fillArray(adouble, densityfunction_contextprovider);
      }

      public DensityFunction wrapped() {
         return this.function;
      }

      public DensityFunctions.Marker.Type type() {
         return DensityFunctions.Marker.Type.Cache2D;
      }
   }

   class CacheAllInCell implements DensityFunctions.MarkerOrMarked, NoiseChunk.NoiseChunkDensityFunction {
      final DensityFunction noiseFiller;
      final double[] values;

      CacheAllInCell(DensityFunction densityfunction) {
         this.noiseFiller = densityfunction;
         this.values = new double[NoiseChunk.this.cellWidth * NoiseChunk.this.cellWidth * NoiseChunk.this.cellHeight];
         NoiseChunk.this.cellCaches.add(this);
      }

      public double compute(DensityFunction.FunctionContext densityfunction_functioncontext) {
         if (densityfunction_functioncontext != NoiseChunk.this) {
            return this.noiseFiller.compute(densityfunction_functioncontext);
         } else if (!NoiseChunk.this.interpolating) {
            throw new IllegalStateException("Trying to sample interpolator outside the interpolation loop");
         } else {
            int i = NoiseChunk.this.inCellX;
            int j = NoiseChunk.this.inCellY;
            int k = NoiseChunk.this.inCellZ;
            return i >= 0 && j >= 0 && k >= 0 && i < NoiseChunk.this.cellWidth && j < NoiseChunk.this.cellHeight && k < NoiseChunk.this.cellWidth ? this.values[((NoiseChunk.this.cellHeight - 1 - j) * NoiseChunk.this.cellWidth + i) * NoiseChunk.this.cellWidth + k] : this.noiseFiller.compute(densityfunction_functioncontext);
         }
      }

      public void fillArray(double[] adouble, DensityFunction.ContextProvider densityfunction_contextprovider) {
         densityfunction_contextprovider.fillAllDirectly(adouble, this);
      }

      public DensityFunction wrapped() {
         return this.noiseFiller;
      }

      public DensityFunctions.Marker.Type type() {
         return DensityFunctions.Marker.Type.CacheAllInCell;
      }
   }

   class CacheOnce implements DensityFunctions.MarkerOrMarked, NoiseChunk.NoiseChunkDensityFunction {
      private final DensityFunction function;
      private long lastCounter;
      private long lastArrayCounter;
      private double lastValue;
      @Nullable
      private double[] lastArray;

      CacheOnce(DensityFunction densityfunction) {
         this.function = densityfunction;
      }

      public double compute(DensityFunction.FunctionContext densityfunction_functioncontext) {
         if (densityfunction_functioncontext != NoiseChunk.this) {
            return this.function.compute(densityfunction_functioncontext);
         } else if (this.lastArray != null && this.lastArrayCounter == NoiseChunk.this.arrayInterpolationCounter) {
            return this.lastArray[NoiseChunk.this.arrayIndex];
         } else if (this.lastCounter == NoiseChunk.this.interpolationCounter) {
            return this.lastValue;
         } else {
            this.lastCounter = NoiseChunk.this.interpolationCounter;
            double d0 = this.function.compute(densityfunction_functioncontext);
            this.lastValue = d0;
            return d0;
         }
      }

      public void fillArray(double[] adouble, DensityFunction.ContextProvider densityfunction_contextprovider) {
         if (this.lastArray != null && this.lastArrayCounter == NoiseChunk.this.arrayInterpolationCounter) {
            System.arraycopy(this.lastArray, 0, adouble, 0, adouble.length);
         } else {
            this.wrapped().fillArray(adouble, densityfunction_contextprovider);
            if (this.lastArray != null && this.lastArray.length == adouble.length) {
               System.arraycopy(adouble, 0, this.lastArray, 0, adouble.length);
            } else {
               this.lastArray = (double[])adouble.clone();
            }

            this.lastArrayCounter = NoiseChunk.this.arrayInterpolationCounter;
         }
      }

      public DensityFunction wrapped() {
         return this.function;
      }

      public DensityFunctions.Marker.Type type() {
         return DensityFunctions.Marker.Type.CacheOnce;
      }
   }

   class FlatCache implements DensityFunctions.MarkerOrMarked, NoiseChunk.NoiseChunkDensityFunction {
      private final DensityFunction noiseFiller;
      final double[][] values;

      FlatCache(DensityFunction densityfunction, boolean flag) {
         this.noiseFiller = densityfunction;
         this.values = new double[NoiseChunk.this.noiseSizeXZ + 1][NoiseChunk.this.noiseSizeXZ + 1];
         if (flag) {
            for(int i = 0; i <= NoiseChunk.this.noiseSizeXZ; ++i) {
               int j = NoiseChunk.this.firstNoiseX + i;
               int k = QuartPos.toBlock(j);

               for(int l = 0; l <= NoiseChunk.this.noiseSizeXZ; ++l) {
                  int i1 = NoiseChunk.this.firstNoiseZ + l;
                  int j1 = QuartPos.toBlock(i1);
                  this.values[i][l] = densityfunction.compute(new DensityFunction.SinglePointContext(k, 0, j1));
               }
            }
         }

      }

      public double compute(DensityFunction.FunctionContext densityfunction_functioncontext) {
         int i = QuartPos.fromBlock(densityfunction_functioncontext.blockX());
         int j = QuartPos.fromBlock(densityfunction_functioncontext.blockZ());
         int k = i - NoiseChunk.this.firstNoiseX;
         int l = j - NoiseChunk.this.firstNoiseZ;
         int i1 = this.values.length;
         return k >= 0 && l >= 0 && k < i1 && l < i1 ? this.values[k][l] : this.noiseFiller.compute(densityfunction_functioncontext);
      }

      public void fillArray(double[] adouble, DensityFunction.ContextProvider densityfunction_contextprovider) {
         densityfunction_contextprovider.fillAllDirectly(adouble, this);
      }

      public DensityFunction wrapped() {
         return this.noiseFiller;
      }

      public DensityFunctions.Marker.Type type() {
         return DensityFunctions.Marker.Type.FlatCache;
      }
   }

   interface NoiseChunkDensityFunction extends DensityFunction {
      DensityFunction wrapped();

      default double minValue() {
         return this.wrapped().minValue();
      }

      default double maxValue() {
         return this.wrapped().maxValue();
      }
   }

   public class NoiseInterpolator implements DensityFunctions.MarkerOrMarked, NoiseChunk.NoiseChunkDensityFunction {
      double[][] slice0;
      double[][] slice1;
      private final DensityFunction noiseFiller;
      private double noise000;
      private double noise001;
      private double noise100;
      private double noise101;
      private double noise010;
      private double noise011;
      private double noise110;
      private double noise111;
      private double valueXZ00;
      private double valueXZ10;
      private double valueXZ01;
      private double valueXZ11;
      private double valueZ0;
      private double valueZ1;
      private double value;

      NoiseInterpolator(DensityFunction densityfunction) {
         this.noiseFiller = densityfunction;
         this.slice0 = this.allocateSlice(NoiseChunk.this.cellCountY, NoiseChunk.this.cellCountXZ);
         this.slice1 = this.allocateSlice(NoiseChunk.this.cellCountY, NoiseChunk.this.cellCountXZ);
         NoiseChunk.this.interpolators.add(this);
      }

      private double[][] allocateSlice(int i, int j) {
         int k = j + 1;
         int l = i + 1;
         double[][] adouble = new double[k][l];

         for(int i1 = 0; i1 < k; ++i1) {
            adouble[i1] = new double[l];
         }

         return adouble;
      }

      void selectCellYZ(int i, int j) {
         this.noise000 = this.slice0[j][i];
         this.noise001 = this.slice0[j + 1][i];
         this.noise100 = this.slice1[j][i];
         this.noise101 = this.slice1[j + 1][i];
         this.noise010 = this.slice0[j][i + 1];
         this.noise011 = this.slice0[j + 1][i + 1];
         this.noise110 = this.slice1[j][i + 1];
         this.noise111 = this.slice1[j + 1][i + 1];
      }

      void updateForY(double d0) {
         this.valueXZ00 = Mth.lerp(d0, this.noise000, this.noise010);
         this.valueXZ10 = Mth.lerp(d0, this.noise100, this.noise110);
         this.valueXZ01 = Mth.lerp(d0, this.noise001, this.noise011);
         this.valueXZ11 = Mth.lerp(d0, this.noise101, this.noise111);
      }

      void updateForX(double d0) {
         this.valueZ0 = Mth.lerp(d0, this.valueXZ00, this.valueXZ10);
         this.valueZ1 = Mth.lerp(d0, this.valueXZ01, this.valueXZ11);
      }

      void updateForZ(double d0) {
         this.value = Mth.lerp(d0, this.valueZ0, this.valueZ1);
      }

      public double compute(DensityFunction.FunctionContext densityfunction_functioncontext) {
         if (densityfunction_functioncontext != NoiseChunk.this) {
            return this.noiseFiller.compute(densityfunction_functioncontext);
         } else if (!NoiseChunk.this.interpolating) {
            throw new IllegalStateException("Trying to sample interpolator outside the interpolation loop");
         } else {
            return NoiseChunk.this.fillingCell ? Mth.lerp3((double)NoiseChunk.this.inCellX / (double)NoiseChunk.this.cellWidth, (double)NoiseChunk.this.inCellY / (double)NoiseChunk.this.cellHeight, (double)NoiseChunk.this.inCellZ / (double)NoiseChunk.this.cellWidth, this.noise000, this.noise100, this.noise010, this.noise110, this.noise001, this.noise101, this.noise011, this.noise111) : this.value;
         }
      }

      public void fillArray(double[] adouble, DensityFunction.ContextProvider densityfunction_contextprovider) {
         if (NoiseChunk.this.fillingCell) {
            densityfunction_contextprovider.fillAllDirectly(adouble, this);
         } else {
            this.wrapped().fillArray(adouble, densityfunction_contextprovider);
         }
      }

      public DensityFunction wrapped() {
         return this.noiseFiller;
      }

      private void swapSlices() {
         double[][] adouble = this.slice0;
         this.slice0 = this.slice1;
         this.slice1 = adouble;
      }

      public DensityFunctions.Marker.Type type() {
         return DensityFunctions.Marker.Type.Interpolated;
      }
   }
}
