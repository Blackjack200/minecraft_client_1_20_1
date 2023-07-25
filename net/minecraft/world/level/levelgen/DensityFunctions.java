package net.minecraft.world.level.levelgen;

import com.mojang.datafixers.util.Either;
import com.mojang.logging.LogUtils;
import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import it.unimi.dsi.fastutil.doubles.Double2DoubleFunction;
import java.util.Arrays;
import java.util.Optional;
import java.util.function.BiFunction;
import java.util.function.Function;
import net.minecraft.core.Holder;
import net.minecraft.core.Registry;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.util.CubicSpline;
import net.minecraft.util.KeyDispatchDataCodec;
import net.minecraft.util.Mth;
import net.minecraft.util.RandomSource;
import net.minecraft.util.StringRepresentable;
import net.minecraft.util.ToFloatFunction;
import net.minecraft.util.VisibleForDebug;
import net.minecraft.world.level.dimension.DimensionType;
import net.minecraft.world.level.levelgen.synth.BlendedNoise;
import net.minecraft.world.level.levelgen.synth.NormalNoise;
import net.minecraft.world.level.levelgen.synth.SimplexNoise;
import org.slf4j.Logger;

public final class DensityFunctions {
   private static final Codec<DensityFunction> CODEC = BuiltInRegistries.DENSITY_FUNCTION_TYPE.byNameCodec().dispatch((densityfunction) -> densityfunction.codec().codec(), Function.identity());
   protected static final double MAX_REASONABLE_NOISE_VALUE = 1000000.0D;
   static final Codec<Double> NOISE_VALUE_CODEC = Codec.doubleRange(-1000000.0D, 1000000.0D);
   public static final Codec<DensityFunction> DIRECT_CODEC = Codec.either(NOISE_VALUE_CODEC, CODEC).xmap((either) -> either.map(DensityFunctions::constant, Function.identity()), (densityfunction) -> {
      if (densityfunction instanceof DensityFunctions.Constant densityfunctions_constant) {
         return Either.left(densityfunctions_constant.value());
      } else {
         return Either.right(densityfunction);
      }
   });

   public static Codec<? extends DensityFunction> bootstrap(Registry<Codec<? extends DensityFunction>> registry) {
      register(registry, "blend_alpha", DensityFunctions.BlendAlpha.CODEC);
      register(registry, "blend_offset", DensityFunctions.BlendOffset.CODEC);
      register(registry, "beardifier", DensityFunctions.BeardifierMarker.CODEC);
      register(registry, "old_blended_noise", BlendedNoise.CODEC);

      for(DensityFunctions.Marker.Type densityfunctions_marker_type : DensityFunctions.Marker.Type.values()) {
         register(registry, densityfunctions_marker_type.getSerializedName(), densityfunctions_marker_type.codec);
      }

      register(registry, "noise", DensityFunctions.Noise.CODEC);
      register(registry, "end_islands", DensityFunctions.EndIslandDensityFunction.CODEC);
      register(registry, "weird_scaled_sampler", DensityFunctions.WeirdScaledSampler.CODEC);
      register(registry, "shifted_noise", DensityFunctions.ShiftedNoise.CODEC);
      register(registry, "range_choice", DensityFunctions.RangeChoice.CODEC);
      register(registry, "shift_a", DensityFunctions.ShiftA.CODEC);
      register(registry, "shift_b", DensityFunctions.ShiftB.CODEC);
      register(registry, "shift", DensityFunctions.Shift.CODEC);
      register(registry, "blend_density", DensityFunctions.BlendDensity.CODEC);
      register(registry, "clamp", DensityFunctions.Clamp.CODEC);

      for(DensityFunctions.Mapped.Type densityfunctions_mapped_type : DensityFunctions.Mapped.Type.values()) {
         register(registry, densityfunctions_mapped_type.getSerializedName(), densityfunctions_mapped_type.codec);
      }

      for(DensityFunctions.TwoArgumentSimpleFunction.Type densityfunctions_twoargumentsimplefunction_type : DensityFunctions.TwoArgumentSimpleFunction.Type.values()) {
         register(registry, densityfunctions_twoargumentsimplefunction_type.getSerializedName(), densityfunctions_twoargumentsimplefunction_type.codec);
      }

      register(registry, "spline", DensityFunctions.Spline.CODEC);
      register(registry, "constant", DensityFunctions.Constant.CODEC);
      return register(registry, "y_clamped_gradient", DensityFunctions.YClampedGradient.CODEC);
   }

   private static Codec<? extends DensityFunction> register(Registry<Codec<? extends DensityFunction>> registry, String s, KeyDispatchDataCodec<? extends DensityFunction> keydispatchdatacodec) {
      return Registry.register(registry, s, keydispatchdatacodec.codec());
   }

   static <A, O> KeyDispatchDataCodec<O> singleArgumentCodec(Codec<A> codec, Function<A, O> function, Function<O, A> function1) {
      return KeyDispatchDataCodec.of(codec.fieldOf("argument").xmap(function, function1));
   }

   static <O> KeyDispatchDataCodec<O> singleFunctionArgumentCodec(Function<DensityFunction, O> function, Function<O, DensityFunction> function1) {
      return singleArgumentCodec(DensityFunction.HOLDER_HELPER_CODEC, function, function1);
   }

   static <O> KeyDispatchDataCodec<O> doubleFunctionArgumentCodec(BiFunction<DensityFunction, DensityFunction, O> bifunction, Function<O, DensityFunction> function, Function<O, DensityFunction> function1) {
      return KeyDispatchDataCodec.of(RecordCodecBuilder.mapCodec((recordcodecbuilder_instance) -> recordcodecbuilder_instance.group(DensityFunction.HOLDER_HELPER_CODEC.fieldOf("argument1").forGetter(function), DensityFunction.HOLDER_HELPER_CODEC.fieldOf("argument2").forGetter(function1)).apply(recordcodecbuilder_instance, bifunction)));
   }

   static <O> KeyDispatchDataCodec<O> makeCodec(MapCodec<O> mapcodec) {
      return KeyDispatchDataCodec.of(mapcodec);
   }

   private DensityFunctions() {
   }

   public static DensityFunction interpolated(DensityFunction densityfunction) {
      return new DensityFunctions.Marker(DensityFunctions.Marker.Type.Interpolated, densityfunction);
   }

   public static DensityFunction flatCache(DensityFunction densityfunction) {
      return new DensityFunctions.Marker(DensityFunctions.Marker.Type.FlatCache, densityfunction);
   }

   public static DensityFunction cache2d(DensityFunction densityfunction) {
      return new DensityFunctions.Marker(DensityFunctions.Marker.Type.Cache2D, densityfunction);
   }

   public static DensityFunction cacheOnce(DensityFunction densityfunction) {
      return new DensityFunctions.Marker(DensityFunctions.Marker.Type.CacheOnce, densityfunction);
   }

   public static DensityFunction cacheAllInCell(DensityFunction densityfunction) {
      return new DensityFunctions.Marker(DensityFunctions.Marker.Type.CacheAllInCell, densityfunction);
   }

   public static DensityFunction mappedNoise(Holder<NormalNoise.NoiseParameters> holder, @Deprecated double d0, double d1, double d2, double d3) {
      return mapFromUnitTo(new DensityFunctions.Noise(new DensityFunction.NoiseHolder(holder), d0, d1), d2, d3);
   }

   public static DensityFunction mappedNoise(Holder<NormalNoise.NoiseParameters> holder, double d0, double d1, double d2) {
      return mappedNoise(holder, 1.0D, d0, d1, d2);
   }

   public static DensityFunction mappedNoise(Holder<NormalNoise.NoiseParameters> holder, double d0, double d1) {
      return mappedNoise(holder, 1.0D, 1.0D, d0, d1);
   }

   public static DensityFunction shiftedNoise2d(DensityFunction densityfunction, DensityFunction densityfunction1, double d0, Holder<NormalNoise.NoiseParameters> holder) {
      return new DensityFunctions.ShiftedNoise(densityfunction, zero(), densityfunction1, d0, 0.0D, new DensityFunction.NoiseHolder(holder));
   }

   public static DensityFunction noise(Holder<NormalNoise.NoiseParameters> holder) {
      return noise(holder, 1.0D, 1.0D);
   }

   public static DensityFunction noise(Holder<NormalNoise.NoiseParameters> holder, double d0, double d1) {
      return new DensityFunctions.Noise(new DensityFunction.NoiseHolder(holder), d0, d1);
   }

   public static DensityFunction noise(Holder<NormalNoise.NoiseParameters> holder, double d0) {
      return noise(holder, 1.0D, d0);
   }

   public static DensityFunction rangeChoice(DensityFunction densityfunction, double d0, double d1, DensityFunction densityfunction1, DensityFunction densityfunction2) {
      return new DensityFunctions.RangeChoice(densityfunction, d0, d1, densityfunction1, densityfunction2);
   }

   public static DensityFunction shiftA(Holder<NormalNoise.NoiseParameters> holder) {
      return new DensityFunctions.ShiftA(new DensityFunction.NoiseHolder(holder));
   }

   public static DensityFunction shiftB(Holder<NormalNoise.NoiseParameters> holder) {
      return new DensityFunctions.ShiftB(new DensityFunction.NoiseHolder(holder));
   }

   public static DensityFunction shift(Holder<NormalNoise.NoiseParameters> holder) {
      return new DensityFunctions.Shift(new DensityFunction.NoiseHolder(holder));
   }

   public static DensityFunction blendDensity(DensityFunction densityfunction) {
      return new DensityFunctions.BlendDensity(densityfunction);
   }

   public static DensityFunction endIslands(long i) {
      return new DensityFunctions.EndIslandDensityFunction(i);
   }

   public static DensityFunction weirdScaledSampler(DensityFunction densityfunction, Holder<NormalNoise.NoiseParameters> holder, DensityFunctions.WeirdScaledSampler.RarityValueMapper densityfunctions_weirdscaledsampler_rarityvaluemapper) {
      return new DensityFunctions.WeirdScaledSampler(densityfunction, new DensityFunction.NoiseHolder(holder), densityfunctions_weirdscaledsampler_rarityvaluemapper);
   }

   public static DensityFunction add(DensityFunction densityfunction, DensityFunction densityfunction1) {
      return DensityFunctions.TwoArgumentSimpleFunction.create(DensityFunctions.TwoArgumentSimpleFunction.Type.ADD, densityfunction, densityfunction1);
   }

   public static DensityFunction mul(DensityFunction densityfunction, DensityFunction densityfunction1) {
      return DensityFunctions.TwoArgumentSimpleFunction.create(DensityFunctions.TwoArgumentSimpleFunction.Type.MUL, densityfunction, densityfunction1);
   }

   public static DensityFunction min(DensityFunction densityfunction, DensityFunction densityfunction1) {
      return DensityFunctions.TwoArgumentSimpleFunction.create(DensityFunctions.TwoArgumentSimpleFunction.Type.MIN, densityfunction, densityfunction1);
   }

   public static DensityFunction max(DensityFunction densityfunction, DensityFunction densityfunction1) {
      return DensityFunctions.TwoArgumentSimpleFunction.create(DensityFunctions.TwoArgumentSimpleFunction.Type.MAX, densityfunction, densityfunction1);
   }

   public static DensityFunction spline(CubicSpline<DensityFunctions.Spline.Point, DensityFunctions.Spline.Coordinate> cubicspline) {
      return new DensityFunctions.Spline(cubicspline);
   }

   public static DensityFunction zero() {
      return DensityFunctions.Constant.ZERO;
   }

   public static DensityFunction constant(double d0) {
      return new DensityFunctions.Constant(d0);
   }

   public static DensityFunction yClampedGradient(int i, int j, double d0, double d1) {
      return new DensityFunctions.YClampedGradient(i, j, d0, d1);
   }

   public static DensityFunction map(DensityFunction densityfunction, DensityFunctions.Mapped.Type densityfunctions_mapped_type) {
      return DensityFunctions.Mapped.create(densityfunctions_mapped_type, densityfunction);
   }

   private static DensityFunction mapFromUnitTo(DensityFunction densityfunction, double d0, double d1) {
      double d2 = (d0 + d1) * 0.5D;
      double d3 = (d1 - d0) * 0.5D;
      return add(constant(d2), mul(constant(d3), densityfunction));
   }

   public static DensityFunction blendAlpha() {
      return DensityFunctions.BlendAlpha.INSTANCE;
   }

   public static DensityFunction blendOffset() {
      return DensityFunctions.BlendOffset.INSTANCE;
   }

   public static DensityFunction lerp(DensityFunction densityfunction, DensityFunction densityfunction1, DensityFunction densityfunction2) {
      if (densityfunction1 instanceof DensityFunctions.Constant densityfunctions_constant) {
         return lerp(densityfunction, densityfunctions_constant.value, densityfunction2);
      } else {
         DensityFunction densityfunction3 = cacheOnce(densityfunction);
         DensityFunction densityfunction4 = add(mul(densityfunction3, constant(-1.0D)), constant(1.0D));
         return add(mul(densityfunction1, densityfunction4), mul(densityfunction2, densityfunction3));
      }
   }

   public static DensityFunction lerp(DensityFunction densityfunction, double d0, DensityFunction densityfunction1) {
      return add(mul(densityfunction, add(densityfunction1, constant(-d0))), constant(d0));
   }

   static record Ap2(DensityFunctions.TwoArgumentSimpleFunction.Type type, DensityFunction argument1, DensityFunction argument2, double minValue, double maxValue) implements DensityFunctions.TwoArgumentSimpleFunction {
      public double compute(DensityFunction.FunctionContext densityfunction_functioncontext) {
         double d0 = this.argument1.compute(densityfunction_functioncontext);
         double var10000;
         switch (this.type) {
            case ADD:
               var10000 = d0 + this.argument2.compute(densityfunction_functioncontext);
               break;
            case MAX:
               var10000 = d0 > this.argument2.maxValue() ? d0 : Math.max(d0, this.argument2.compute(densityfunction_functioncontext));
               break;
            case MIN:
               var10000 = d0 < this.argument2.minValue() ? d0 : Math.min(d0, this.argument2.compute(densityfunction_functioncontext));
               break;
            case MUL:
               var10000 = d0 == 0.0D ? 0.0D : d0 * this.argument2.compute(densityfunction_functioncontext);
               break;
            default:
               throw new IncompatibleClassChangeError();
         }

         return var10000;
      }

      public void fillArray(double[] adouble, DensityFunction.ContextProvider densityfunction_contextprovider) {
         this.argument1.fillArray(adouble, densityfunction_contextprovider);
         switch (this.type) {
            case ADD:
               double[] adouble1 = new double[adouble.length];
               this.argument2.fillArray(adouble1, densityfunction_contextprovider);

               for(int i = 0; i < adouble.length; ++i) {
                  adouble[i] += adouble1[i];
               }
               break;
            case MAX:
               double d3 = this.argument2.maxValue();

               for(int l = 0; l < adouble.length; ++l) {
                  double d4 = adouble[l];
                  adouble[l] = d4 > d3 ? d4 : Math.max(d4, this.argument2.compute(densityfunction_contextprovider.forIndex(l)));
               }
               break;
            case MIN:
               double d1 = this.argument2.minValue();

               for(int k = 0; k < adouble.length; ++k) {
                  double d2 = adouble[k];
                  adouble[k] = d2 < d1 ? d2 : Math.min(d2, this.argument2.compute(densityfunction_contextprovider.forIndex(k)));
               }
               break;
            case MUL:
               for(int j = 0; j < adouble.length; ++j) {
                  double d0 = adouble[j];
                  adouble[j] = d0 == 0.0D ? 0.0D : d0 * this.argument2.compute(densityfunction_contextprovider.forIndex(j));
               }
         }

      }

      public DensityFunction mapAll(DensityFunction.Visitor densityfunction_visitor) {
         return densityfunction_visitor.apply(DensityFunctions.TwoArgumentSimpleFunction.create(this.type, this.argument1.mapAll(densityfunction_visitor), this.argument2.mapAll(densityfunction_visitor)));
      }
   }

   protected static enum BeardifierMarker implements DensityFunctions.BeardifierOrMarker {
      INSTANCE;

      public double compute(DensityFunction.FunctionContext densityfunction_functioncontext) {
         return 0.0D;
      }

      public void fillArray(double[] adouble, DensityFunction.ContextProvider densityfunction_contextprovider) {
         Arrays.fill(adouble, 0.0D);
      }

      public double minValue() {
         return 0.0D;
      }

      public double maxValue() {
         return 0.0D;
      }
   }

   public interface BeardifierOrMarker extends DensityFunction.SimpleFunction {
      KeyDispatchDataCodec<DensityFunction> CODEC = KeyDispatchDataCodec.of(MapCodec.unit(DensityFunctions.BeardifierMarker.INSTANCE));

      default KeyDispatchDataCodec<? extends DensityFunction> codec() {
         return CODEC;
      }
   }

   protected static enum BlendAlpha implements DensityFunction.SimpleFunction {
      INSTANCE;

      public static final KeyDispatchDataCodec<DensityFunction> CODEC = KeyDispatchDataCodec.of(MapCodec.unit(INSTANCE));

      public double compute(DensityFunction.FunctionContext densityfunction_functioncontext) {
         return 1.0D;
      }

      public void fillArray(double[] adouble, DensityFunction.ContextProvider densityfunction_contextprovider) {
         Arrays.fill(adouble, 1.0D);
      }

      public double minValue() {
         return 1.0D;
      }

      public double maxValue() {
         return 1.0D;
      }

      public KeyDispatchDataCodec<? extends DensityFunction> codec() {
         return CODEC;
      }
   }

   static record BlendDensity(DensityFunction input) implements DensityFunctions.TransformerWithContext {
      static final KeyDispatchDataCodec<DensityFunctions.BlendDensity> CODEC = DensityFunctions.singleFunctionArgumentCodec(DensityFunctions.BlendDensity::new, DensityFunctions.BlendDensity::input);

      public double transform(DensityFunction.FunctionContext densityfunction_functioncontext, double d0) {
         return densityfunction_functioncontext.getBlender().blendDensity(densityfunction_functioncontext, d0);
      }

      public DensityFunction mapAll(DensityFunction.Visitor densityfunction_visitor) {
         return densityfunction_visitor.apply(new DensityFunctions.BlendDensity(this.input.mapAll(densityfunction_visitor)));
      }

      public double minValue() {
         return Double.NEGATIVE_INFINITY;
      }

      public double maxValue() {
         return Double.POSITIVE_INFINITY;
      }

      public KeyDispatchDataCodec<? extends DensityFunction> codec() {
         return CODEC;
      }
   }

   protected static enum BlendOffset implements DensityFunction.SimpleFunction {
      INSTANCE;

      public static final KeyDispatchDataCodec<DensityFunction> CODEC = KeyDispatchDataCodec.of(MapCodec.unit(INSTANCE));

      public double compute(DensityFunction.FunctionContext densityfunction_functioncontext) {
         return 0.0D;
      }

      public void fillArray(double[] adouble, DensityFunction.ContextProvider densityfunction_contextprovider) {
         Arrays.fill(adouble, 0.0D);
      }

      public double minValue() {
         return 0.0D;
      }

      public double maxValue() {
         return 0.0D;
      }

      public KeyDispatchDataCodec<? extends DensityFunction> codec() {
         return CODEC;
      }
   }

   protected static record Clamp(DensityFunction input, double minValue, double maxValue) implements DensityFunctions.PureTransformer {
      private static final MapCodec<DensityFunctions.Clamp> DATA_CODEC = RecordCodecBuilder.mapCodec((recordcodecbuilder_instance) -> recordcodecbuilder_instance.group(DensityFunction.DIRECT_CODEC.fieldOf("input").forGetter(DensityFunctions.Clamp::input), DensityFunctions.NOISE_VALUE_CODEC.fieldOf("min").forGetter(DensityFunctions.Clamp::minValue), DensityFunctions.NOISE_VALUE_CODEC.fieldOf("max").forGetter(DensityFunctions.Clamp::maxValue)).apply(recordcodecbuilder_instance, DensityFunctions.Clamp::new));
      public static final KeyDispatchDataCodec<DensityFunctions.Clamp> CODEC = DensityFunctions.makeCodec(DATA_CODEC);

      public double transform(double d0) {
         return Mth.clamp(d0, this.minValue, this.maxValue);
      }

      public DensityFunction mapAll(DensityFunction.Visitor densityfunction_visitor) {
         return new DensityFunctions.Clamp(this.input.mapAll(densityfunction_visitor), this.minValue, this.maxValue);
      }

      public KeyDispatchDataCodec<? extends DensityFunction> codec() {
         return CODEC;
      }
   }

   static record Constant(double value) implements DensityFunction.SimpleFunction {
      final double value;
      static final KeyDispatchDataCodec<DensityFunctions.Constant> CODEC = DensityFunctions.singleArgumentCodec(DensityFunctions.NOISE_VALUE_CODEC, DensityFunctions.Constant::new, DensityFunctions.Constant::value);
      static final DensityFunctions.Constant ZERO = new DensityFunctions.Constant(0.0D);

      public double compute(DensityFunction.FunctionContext densityfunction_functioncontext) {
         return this.value;
      }

      public void fillArray(double[] adouble, DensityFunction.ContextProvider densityfunction_contextprovider) {
         Arrays.fill(adouble, this.value);
      }

      public double minValue() {
         return this.value;
      }

      public double maxValue() {
         return this.value;
      }

      public KeyDispatchDataCodec<? extends DensityFunction> codec() {
         return CODEC;
      }
   }

   protected static final class EndIslandDensityFunction implements DensityFunction.SimpleFunction {
      public static final KeyDispatchDataCodec<DensityFunctions.EndIslandDensityFunction> CODEC = KeyDispatchDataCodec.of(MapCodec.unit(new DensityFunctions.EndIslandDensityFunction(0L)));
      private static final float ISLAND_THRESHOLD = -0.9F;
      private final SimplexNoise islandNoise;

      public EndIslandDensityFunction(long i) {
         RandomSource randomsource = new LegacyRandomSource(i);
         randomsource.consumeCount(17292);
         this.islandNoise = new SimplexNoise(randomsource);
      }

      private static float getHeightValue(SimplexNoise simplexnoise, int i, int j) {
         int k = i / 2;
         int l = j / 2;
         int i1 = i % 2;
         int j1 = j % 2;
         float f = 100.0F - Mth.sqrt((float)(i * i + j * j)) * 8.0F;
         f = Mth.clamp(f, -100.0F, 80.0F);

         for(int k1 = -12; k1 <= 12; ++k1) {
            for(int l1 = -12; l1 <= 12; ++l1) {
               long i2 = (long)(k + k1);
               long j2 = (long)(l + l1);
               if (i2 * i2 + j2 * j2 > 4096L && simplexnoise.getValue((double)i2, (double)j2) < (double)-0.9F) {
                  float f1 = (Mth.abs((float)i2) * 3439.0F + Mth.abs((float)j2) * 147.0F) % 13.0F + 9.0F;
                  float f2 = (float)(i1 - k1 * 2);
                  float f3 = (float)(j1 - l1 * 2);
                  float f4 = 100.0F - Mth.sqrt(f2 * f2 + f3 * f3) * f1;
                  f4 = Mth.clamp(f4, -100.0F, 80.0F);
                  f = Math.max(f, f4);
               }
            }
         }

         return f;
      }

      public double compute(DensityFunction.FunctionContext densityfunction_functioncontext) {
         return ((double)getHeightValue(this.islandNoise, densityfunction_functioncontext.blockX() / 8, densityfunction_functioncontext.blockZ() / 8) - 8.0D) / 128.0D;
      }

      public double minValue() {
         return -0.84375D;
      }

      public double maxValue() {
         return 0.5625D;
      }

      public KeyDispatchDataCodec<? extends DensityFunction> codec() {
         return CODEC;
      }
   }

   @VisibleForDebug
   public static record HolderHolder(Holder<DensityFunction> function) implements DensityFunction {
      public double compute(DensityFunction.FunctionContext densityfunction_functioncontext) {
         return this.function.value().compute(densityfunction_functioncontext);
      }

      public void fillArray(double[] adouble, DensityFunction.ContextProvider densityfunction_contextprovider) {
         this.function.value().fillArray(adouble, densityfunction_contextprovider);
      }

      public DensityFunction mapAll(DensityFunction.Visitor densityfunction_visitor) {
         return densityfunction_visitor.apply(new DensityFunctions.HolderHolder(new Holder.Direct<>(this.function.value().mapAll(densityfunction_visitor))));
      }

      public double minValue() {
         return this.function.isBound() ? this.function.value().minValue() : Double.NEGATIVE_INFINITY;
      }

      public double maxValue() {
         return this.function.isBound() ? this.function.value().maxValue() : Double.POSITIVE_INFINITY;
      }

      public KeyDispatchDataCodec<? extends DensityFunction> codec() {
         throw new UnsupportedOperationException("Calling .codec() on HolderHolder");
      }
   }

   protected static record Mapped(DensityFunctions.Mapped.Type type, DensityFunction input, double minValue, double maxValue) implements DensityFunctions.PureTransformer {
      public static DensityFunctions.Mapped create(DensityFunctions.Mapped.Type densityfunctions_mapped_type, DensityFunction densityfunction) {
         double d0 = densityfunction.minValue();
         double d1 = transform(densityfunctions_mapped_type, d0);
         double d2 = transform(densityfunctions_mapped_type, densityfunction.maxValue());
         return densityfunctions_mapped_type != DensityFunctions.Mapped.Type.ABS && densityfunctions_mapped_type != DensityFunctions.Mapped.Type.SQUARE ? new DensityFunctions.Mapped(densityfunctions_mapped_type, densityfunction, d1, d2) : new DensityFunctions.Mapped(densityfunctions_mapped_type, densityfunction, Math.max(0.0D, d0), Math.max(d1, d2));
      }

      private static double transform(DensityFunctions.Mapped.Type densityfunctions_mapped_type, double d0) {
         double var10000;
         switch (densityfunctions_mapped_type) {
            case ABS:
               var10000 = Math.abs(d0);
               break;
            case SQUARE:
               var10000 = d0 * d0;
               break;
            case CUBE:
               var10000 = d0 * d0 * d0;
               break;
            case HALF_NEGATIVE:
               var10000 = d0 > 0.0D ? d0 : d0 * 0.5D;
               break;
            case QUARTER_NEGATIVE:
               var10000 = d0 > 0.0D ? d0 : d0 * 0.25D;
               break;
            case SQUEEZE:
               double d1 = Mth.clamp(d0, -1.0D, 1.0D);
               var10000 = d1 / 2.0D - d1 * d1 * d1 / 24.0D;
               break;
            default:
               throw new IncompatibleClassChangeError();
         }

         return var10000;
      }

      public double transform(double d0) {
         return transform(this.type, d0);
      }

      public DensityFunctions.Mapped mapAll(DensityFunction.Visitor densityfunction_visitor) {
         return create(this.type, this.input.mapAll(densityfunction_visitor));
      }

      public KeyDispatchDataCodec<? extends DensityFunction> codec() {
         return this.type.codec;
      }

      static enum Type implements StringRepresentable {
         ABS("abs"),
         SQUARE("square"),
         CUBE("cube"),
         HALF_NEGATIVE("half_negative"),
         QUARTER_NEGATIVE("quarter_negative"),
         SQUEEZE("squeeze");

         private final String name;
         final KeyDispatchDataCodec<DensityFunctions.Mapped> codec = DensityFunctions.singleFunctionArgumentCodec((densityfunction) -> DensityFunctions.Mapped.create(this, densityfunction), DensityFunctions.Mapped::input);

         private Type(String s) {
            this.name = s;
         }

         public String getSerializedName() {
            return this.name;
         }
      }
   }

   protected static record Marker(DensityFunctions.Marker.Type type, DensityFunction wrapped) implements DensityFunctions.MarkerOrMarked {
      public double compute(DensityFunction.FunctionContext densityfunction_functioncontext) {
         return this.wrapped.compute(densityfunction_functioncontext);
      }

      public void fillArray(double[] adouble, DensityFunction.ContextProvider densityfunction_contextprovider) {
         this.wrapped.fillArray(adouble, densityfunction_contextprovider);
      }

      public double minValue() {
         return this.wrapped.minValue();
      }

      public double maxValue() {
         return this.wrapped.maxValue();
      }

      static enum Type implements StringRepresentable {
         Interpolated("interpolated"),
         FlatCache("flat_cache"),
         Cache2D("cache_2d"),
         CacheOnce("cache_once"),
         CacheAllInCell("cache_all_in_cell");

         private final String name;
         final KeyDispatchDataCodec<DensityFunctions.MarkerOrMarked> codec = DensityFunctions.singleFunctionArgumentCodec((densityfunction) -> new DensityFunctions.Marker(this, densityfunction), DensityFunctions.MarkerOrMarked::wrapped);

         private Type(String s) {
            this.name = s;
         }

         public String getSerializedName() {
            return this.name;
         }
      }
   }

   public interface MarkerOrMarked extends DensityFunction {
      DensityFunctions.Marker.Type type();

      DensityFunction wrapped();

      default KeyDispatchDataCodec<? extends DensityFunction> codec() {
         return this.type().codec;
      }

      default DensityFunction mapAll(DensityFunction.Visitor densityfunction_visitor) {
         return densityfunction_visitor.apply(new DensityFunctions.Marker(this.type(), this.wrapped().mapAll(densityfunction_visitor)));
      }
   }

   static record MulOrAdd(DensityFunctions.MulOrAdd.Type specificType, DensityFunction input, double minValue, double maxValue, double argument) implements DensityFunctions.PureTransformer, DensityFunctions.TwoArgumentSimpleFunction {
      public DensityFunctions.TwoArgumentSimpleFunction.Type type() {
         return this.specificType == DensityFunctions.MulOrAdd.Type.MUL ? DensityFunctions.TwoArgumentSimpleFunction.Type.MUL : DensityFunctions.TwoArgumentSimpleFunction.Type.ADD;
      }

      public DensityFunction argument1() {
         return DensityFunctions.constant(this.argument);
      }

      public DensityFunction argument2() {
         return this.input;
      }

      public double transform(double d0) {
         double var10000;
         switch (this.specificType) {
            case MUL:
               var10000 = d0 * this.argument;
               break;
            case ADD:
               var10000 = d0 + this.argument;
               break;
            default:
               throw new IncompatibleClassChangeError();
         }

         return var10000;
      }

      public DensityFunction mapAll(DensityFunction.Visitor densityfunction_visitor) {
         DensityFunction densityfunction = this.input.mapAll(densityfunction_visitor);
         double d0 = densityfunction.minValue();
         double d1 = densityfunction.maxValue();
         double d2;
         double d3;
         if (this.specificType == DensityFunctions.MulOrAdd.Type.ADD) {
            d2 = d0 + this.argument;
            d3 = d1 + this.argument;
         } else if (this.argument >= 0.0D) {
            d2 = d0 * this.argument;
            d3 = d1 * this.argument;
         } else {
            d2 = d1 * this.argument;
            d3 = d0 * this.argument;
         }

         return new DensityFunctions.MulOrAdd(this.specificType, densityfunction, d2, d3, this.argument);
      }

      static enum Type {
         MUL,
         ADD;
      }
   }

   protected static record Noise(DensityFunction.NoiseHolder noise, double xzScale, double yScale) implements DensityFunction {
      public static final MapCodec<DensityFunctions.Noise> DATA_CODEC = RecordCodecBuilder.mapCodec((recordcodecbuilder_instance) -> recordcodecbuilder_instance.group(DensityFunction.NoiseHolder.CODEC.fieldOf("noise").forGetter(DensityFunctions.Noise::noise), Codec.DOUBLE.fieldOf("xz_scale").forGetter(DensityFunctions.Noise::xzScale), Codec.DOUBLE.fieldOf("y_scale").forGetter(DensityFunctions.Noise::yScale)).apply(recordcodecbuilder_instance, DensityFunctions.Noise::new));
      public static final KeyDispatchDataCodec<DensityFunctions.Noise> CODEC = DensityFunctions.makeCodec(DATA_CODEC);

      public double compute(DensityFunction.FunctionContext densityfunction_functioncontext) {
         return this.noise.getValue((double)densityfunction_functioncontext.blockX() * this.xzScale, (double)densityfunction_functioncontext.blockY() * this.yScale, (double)densityfunction_functioncontext.blockZ() * this.xzScale);
      }

      public void fillArray(double[] adouble, DensityFunction.ContextProvider densityfunction_contextprovider) {
         densityfunction_contextprovider.fillAllDirectly(adouble, this);
      }

      public DensityFunction mapAll(DensityFunction.Visitor densityfunction_visitor) {
         return densityfunction_visitor.apply(new DensityFunctions.Noise(densityfunction_visitor.visitNoise(this.noise), this.xzScale, this.yScale));
      }

      public double minValue() {
         return -this.maxValue();
      }

      public double maxValue() {
         return this.noise.maxValue();
      }

      public KeyDispatchDataCodec<? extends DensityFunction> codec() {
         return CODEC;
      }
   }

   interface PureTransformer extends DensityFunction {
      DensityFunction input();

      default double compute(DensityFunction.FunctionContext densityfunction_functioncontext) {
         return this.transform(this.input().compute(densityfunction_functioncontext));
      }

      default void fillArray(double[] adouble, DensityFunction.ContextProvider densityfunction_contextprovider) {
         this.input().fillArray(adouble, densityfunction_contextprovider);

         for(int i = 0; i < adouble.length; ++i) {
            adouble[i] = this.transform(adouble[i]);
         }

      }

      double transform(double d0);
   }

   static record RangeChoice(DensityFunction input, double minInclusive, double maxExclusive, DensityFunction whenInRange, DensityFunction whenOutOfRange) implements DensityFunction {
      public static final MapCodec<DensityFunctions.RangeChoice> DATA_CODEC = RecordCodecBuilder.mapCodec((recordcodecbuilder_instance) -> recordcodecbuilder_instance.group(DensityFunction.HOLDER_HELPER_CODEC.fieldOf("input").forGetter(DensityFunctions.RangeChoice::input), DensityFunctions.NOISE_VALUE_CODEC.fieldOf("min_inclusive").forGetter(DensityFunctions.RangeChoice::minInclusive), DensityFunctions.NOISE_VALUE_CODEC.fieldOf("max_exclusive").forGetter(DensityFunctions.RangeChoice::maxExclusive), DensityFunction.HOLDER_HELPER_CODEC.fieldOf("when_in_range").forGetter(DensityFunctions.RangeChoice::whenInRange), DensityFunction.HOLDER_HELPER_CODEC.fieldOf("when_out_of_range").forGetter(DensityFunctions.RangeChoice::whenOutOfRange)).apply(recordcodecbuilder_instance, DensityFunctions.RangeChoice::new));
      public static final KeyDispatchDataCodec<DensityFunctions.RangeChoice> CODEC = DensityFunctions.makeCodec(DATA_CODEC);

      public double compute(DensityFunction.FunctionContext densityfunction_functioncontext) {
         double d0 = this.input.compute(densityfunction_functioncontext);
         return d0 >= this.minInclusive && d0 < this.maxExclusive ? this.whenInRange.compute(densityfunction_functioncontext) : this.whenOutOfRange.compute(densityfunction_functioncontext);
      }

      public void fillArray(double[] adouble, DensityFunction.ContextProvider densityfunction_contextprovider) {
         this.input.fillArray(adouble, densityfunction_contextprovider);

         for(int i = 0; i < adouble.length; ++i) {
            double d0 = adouble[i];
            if (d0 >= this.minInclusive && d0 < this.maxExclusive) {
               adouble[i] = this.whenInRange.compute(densityfunction_contextprovider.forIndex(i));
            } else {
               adouble[i] = this.whenOutOfRange.compute(densityfunction_contextprovider.forIndex(i));
            }
         }

      }

      public DensityFunction mapAll(DensityFunction.Visitor densityfunction_visitor) {
         return densityfunction_visitor.apply(new DensityFunctions.RangeChoice(this.input.mapAll(densityfunction_visitor), this.minInclusive, this.maxExclusive, this.whenInRange.mapAll(densityfunction_visitor), this.whenOutOfRange.mapAll(densityfunction_visitor)));
      }

      public double minValue() {
         return Math.min(this.whenInRange.minValue(), this.whenOutOfRange.minValue());
      }

      public double maxValue() {
         return Math.max(this.whenInRange.maxValue(), this.whenOutOfRange.maxValue());
      }

      public KeyDispatchDataCodec<? extends DensityFunction> codec() {
         return CODEC;
      }
   }

   protected static record Shift(DensityFunction.NoiseHolder offsetNoise) implements DensityFunctions.ShiftNoise {
      static final KeyDispatchDataCodec<DensityFunctions.Shift> CODEC = DensityFunctions.singleArgumentCodec(DensityFunction.NoiseHolder.CODEC, DensityFunctions.Shift::new, DensityFunctions.Shift::offsetNoise);

      public double compute(DensityFunction.FunctionContext densityfunction_functioncontext) {
         return this.compute((double)densityfunction_functioncontext.blockX(), (double)densityfunction_functioncontext.blockY(), (double)densityfunction_functioncontext.blockZ());
      }

      public DensityFunction mapAll(DensityFunction.Visitor densityfunction_visitor) {
         return densityfunction_visitor.apply(new DensityFunctions.Shift(densityfunction_visitor.visitNoise(this.offsetNoise)));
      }

      public KeyDispatchDataCodec<? extends DensityFunction> codec() {
         return CODEC;
      }
   }

   protected static record ShiftA(DensityFunction.NoiseHolder offsetNoise) implements DensityFunctions.ShiftNoise {
      static final KeyDispatchDataCodec<DensityFunctions.ShiftA> CODEC = DensityFunctions.singleArgumentCodec(DensityFunction.NoiseHolder.CODEC, DensityFunctions.ShiftA::new, DensityFunctions.ShiftA::offsetNoise);

      public double compute(DensityFunction.FunctionContext densityfunction_functioncontext) {
         return this.compute((double)densityfunction_functioncontext.blockX(), 0.0D, (double)densityfunction_functioncontext.blockZ());
      }

      public DensityFunction mapAll(DensityFunction.Visitor densityfunction_visitor) {
         return densityfunction_visitor.apply(new DensityFunctions.ShiftA(densityfunction_visitor.visitNoise(this.offsetNoise)));
      }

      public KeyDispatchDataCodec<? extends DensityFunction> codec() {
         return CODEC;
      }
   }

   protected static record ShiftB(DensityFunction.NoiseHolder offsetNoise) implements DensityFunctions.ShiftNoise {
      static final KeyDispatchDataCodec<DensityFunctions.ShiftB> CODEC = DensityFunctions.singleArgumentCodec(DensityFunction.NoiseHolder.CODEC, DensityFunctions.ShiftB::new, DensityFunctions.ShiftB::offsetNoise);

      public double compute(DensityFunction.FunctionContext densityfunction_functioncontext) {
         return this.compute((double)densityfunction_functioncontext.blockZ(), (double)densityfunction_functioncontext.blockX(), 0.0D);
      }

      public DensityFunction mapAll(DensityFunction.Visitor densityfunction_visitor) {
         return densityfunction_visitor.apply(new DensityFunctions.ShiftB(densityfunction_visitor.visitNoise(this.offsetNoise)));
      }

      public KeyDispatchDataCodec<? extends DensityFunction> codec() {
         return CODEC;
      }
   }

   interface ShiftNoise extends DensityFunction {
      DensityFunction.NoiseHolder offsetNoise();

      default double minValue() {
         return -this.maxValue();
      }

      default double maxValue() {
         return this.offsetNoise().maxValue() * 4.0D;
      }

      default double compute(double d0, double d1, double d2) {
         return this.offsetNoise().getValue(d0 * 0.25D, d1 * 0.25D, d2 * 0.25D) * 4.0D;
      }

      default void fillArray(double[] adouble, DensityFunction.ContextProvider densityfunction_contextprovider) {
         densityfunction_contextprovider.fillAllDirectly(adouble, this);
      }
   }

   protected static record ShiftedNoise(DensityFunction shiftX, DensityFunction shiftY, DensityFunction shiftZ, double xzScale, double yScale, DensityFunction.NoiseHolder noise) implements DensityFunction {
      private static final MapCodec<DensityFunctions.ShiftedNoise> DATA_CODEC = RecordCodecBuilder.mapCodec((recordcodecbuilder_instance) -> recordcodecbuilder_instance.group(DensityFunction.HOLDER_HELPER_CODEC.fieldOf("shift_x").forGetter(DensityFunctions.ShiftedNoise::shiftX), DensityFunction.HOLDER_HELPER_CODEC.fieldOf("shift_y").forGetter(DensityFunctions.ShiftedNoise::shiftY), DensityFunction.HOLDER_HELPER_CODEC.fieldOf("shift_z").forGetter(DensityFunctions.ShiftedNoise::shiftZ), Codec.DOUBLE.fieldOf("xz_scale").forGetter(DensityFunctions.ShiftedNoise::xzScale), Codec.DOUBLE.fieldOf("y_scale").forGetter(DensityFunctions.ShiftedNoise::yScale), DensityFunction.NoiseHolder.CODEC.fieldOf("noise").forGetter(DensityFunctions.ShiftedNoise::noise)).apply(recordcodecbuilder_instance, DensityFunctions.ShiftedNoise::new));
      public static final KeyDispatchDataCodec<DensityFunctions.ShiftedNoise> CODEC = DensityFunctions.makeCodec(DATA_CODEC);

      public double compute(DensityFunction.FunctionContext densityfunction_functioncontext) {
         double d0 = (double)densityfunction_functioncontext.blockX() * this.xzScale + this.shiftX.compute(densityfunction_functioncontext);
         double d1 = (double)densityfunction_functioncontext.blockY() * this.yScale + this.shiftY.compute(densityfunction_functioncontext);
         double d2 = (double)densityfunction_functioncontext.blockZ() * this.xzScale + this.shiftZ.compute(densityfunction_functioncontext);
         return this.noise.getValue(d0, d1, d2);
      }

      public void fillArray(double[] adouble, DensityFunction.ContextProvider densityfunction_contextprovider) {
         densityfunction_contextprovider.fillAllDirectly(adouble, this);
      }

      public DensityFunction mapAll(DensityFunction.Visitor densityfunction_visitor) {
         return densityfunction_visitor.apply(new DensityFunctions.ShiftedNoise(this.shiftX.mapAll(densityfunction_visitor), this.shiftY.mapAll(densityfunction_visitor), this.shiftZ.mapAll(densityfunction_visitor), this.xzScale, this.yScale, densityfunction_visitor.visitNoise(this.noise)));
      }

      public double minValue() {
         return -this.maxValue();
      }

      public double maxValue() {
         return this.noise.maxValue();
      }

      public KeyDispatchDataCodec<? extends DensityFunction> codec() {
         return CODEC;
      }
   }

   public static record Spline(CubicSpline<DensityFunctions.Spline.Point, DensityFunctions.Spline.Coordinate> spline) implements DensityFunction {
      private static final Codec<CubicSpline<DensityFunctions.Spline.Point, DensityFunctions.Spline.Coordinate>> SPLINE_CODEC = CubicSpline.codec(DensityFunctions.Spline.Coordinate.CODEC);
      private static final MapCodec<DensityFunctions.Spline> DATA_CODEC = SPLINE_CODEC.fieldOf("spline").xmap(DensityFunctions.Spline::new, DensityFunctions.Spline::spline);
      public static final KeyDispatchDataCodec<DensityFunctions.Spline> CODEC = DensityFunctions.makeCodec(DATA_CODEC);

      public double compute(DensityFunction.FunctionContext densityfunction_functioncontext) {
         return (double)this.spline.apply(new DensityFunctions.Spline.Point(densityfunction_functioncontext));
      }

      public double minValue() {
         return (double)this.spline.minValue();
      }

      public double maxValue() {
         return (double)this.spline.maxValue();
      }

      public void fillArray(double[] adouble, DensityFunction.ContextProvider densityfunction_contextprovider) {
         densityfunction_contextprovider.fillAllDirectly(adouble, this);
      }

      public DensityFunction mapAll(DensityFunction.Visitor densityfunction_visitor) {
         return densityfunction_visitor.apply(new DensityFunctions.Spline(this.spline.mapAll((densityfunctions_spline_coordinate) -> densityfunctions_spline_coordinate.mapAll(densityfunction_visitor))));
      }

      public KeyDispatchDataCodec<? extends DensityFunction> codec() {
         return CODEC;
      }

      public static record Coordinate(Holder<DensityFunction> function) implements ToFloatFunction<DensityFunctions.Spline.Point> {
         public static final Codec<DensityFunctions.Spline.Coordinate> CODEC = DensityFunction.CODEC.xmap(DensityFunctions.Spline.Coordinate::new, DensityFunctions.Spline.Coordinate::function);

         public String toString() {
            Optional<ResourceKey<DensityFunction>> optional = this.function.unwrapKey();
            if (optional.isPresent()) {
               ResourceKey<DensityFunction> resourcekey = optional.get();
               if (resourcekey == NoiseRouterData.CONTINENTS) {
                  return "continents";
               }

               if (resourcekey == NoiseRouterData.EROSION) {
                  return "erosion";
               }

               if (resourcekey == NoiseRouterData.RIDGES) {
                  return "weirdness";
               }

               if (resourcekey == NoiseRouterData.RIDGES_FOLDED) {
                  return "ridges";
               }
            }

            return "Coordinate[" + this.function + "]";
         }

         public float apply(DensityFunctions.Spline.Point densityfunctions_spline_point) {
            return (float)this.function.value().compute(densityfunctions_spline_point.context());
         }

         public float minValue() {
            return this.function.isBound() ? (float)this.function.value().minValue() : Float.NEGATIVE_INFINITY;
         }

         public float maxValue() {
            return this.function.isBound() ? (float)this.function.value().maxValue() : Float.POSITIVE_INFINITY;
         }

         public DensityFunctions.Spline.Coordinate mapAll(DensityFunction.Visitor densityfunction_visitor) {
            return new DensityFunctions.Spline.Coordinate(new Holder.Direct<>(this.function.value().mapAll(densityfunction_visitor)));
         }
      }

      public static record Point(DensityFunction.FunctionContext context) {
      }
   }

   interface TransformerWithContext extends DensityFunction {
      DensityFunction input();

      default double compute(DensityFunction.FunctionContext densityfunction_functioncontext) {
         return this.transform(densityfunction_functioncontext, this.input().compute(densityfunction_functioncontext));
      }

      default void fillArray(double[] adouble, DensityFunction.ContextProvider densityfunction_contextprovider) {
         this.input().fillArray(adouble, densityfunction_contextprovider);

         for(int i = 0; i < adouble.length; ++i) {
            adouble[i] = this.transform(densityfunction_contextprovider.forIndex(i), adouble[i]);
         }

      }

      double transform(DensityFunction.FunctionContext densityfunction_functioncontext, double d0);
   }

   interface TwoArgumentSimpleFunction extends DensityFunction {
      Logger LOGGER = LogUtils.getLogger();

      static DensityFunctions.TwoArgumentSimpleFunction create(DensityFunctions.TwoArgumentSimpleFunction.Type densityfunctions_twoargumentsimplefunction_type, DensityFunction densityfunction, DensityFunction densityfunction1) {
         double d0 = densityfunction.minValue();
         double d1 = densityfunction1.minValue();
         double d2 = densityfunction.maxValue();
         double d3 = densityfunction1.maxValue();
         if (densityfunctions_twoargumentsimplefunction_type == DensityFunctions.TwoArgumentSimpleFunction.Type.MIN || densityfunctions_twoargumentsimplefunction_type == DensityFunctions.TwoArgumentSimpleFunction.Type.MAX) {
            boolean flag = d0 >= d3;
            boolean flag1 = d1 >= d2;
            if (flag || flag1) {
               LOGGER.warn("Creating a " + densityfunctions_twoargumentsimplefunction_type + " function between two non-overlapping inputs: " + densityfunction + " and " + densityfunction1);
            }
         }

         double var10000;
         switch (densityfunctions_twoargumentsimplefunction_type) {
            case ADD:
               var10000 = d0 + d1;
               break;
            case MAX:
               var10000 = Math.max(d0, d1);
               break;
            case MIN:
               var10000 = Math.min(d0, d1);
               break;
            case MUL:
               var10000 = d0 > 0.0D && d1 > 0.0D ? d0 * d1 : (d2 < 0.0D && d3 < 0.0D ? d2 * d3 : Math.min(d0 * d3, d2 * d1));
               break;
            default:
               throw new IncompatibleClassChangeError();
         }

         double d4 = var10000;
         switch (densityfunctions_twoargumentsimplefunction_type) {
            case ADD:
               var10000 = d2 + d3;
               break;
            case MAX:
               var10000 = Math.max(d2, d3);
               break;
            case MIN:
               var10000 = Math.min(d2, d3);
               break;
            case MUL:
               var10000 = d0 > 0.0D && d1 > 0.0D ? d2 * d3 : (d2 < 0.0D && d3 < 0.0D ? d0 * d1 : Math.max(d0 * d1, d2 * d3));
               break;
            default:
               throw new IncompatibleClassChangeError();
         }

         double d5 = var10000;
         if (densityfunctions_twoargumentsimplefunction_type == DensityFunctions.TwoArgumentSimpleFunction.Type.MUL || densityfunctions_twoargumentsimplefunction_type == DensityFunctions.TwoArgumentSimpleFunction.Type.ADD) {
            if (densityfunction instanceof DensityFunctions.Constant) {
               DensityFunctions.Constant densityfunctions_constant = (DensityFunctions.Constant)densityfunction;
               return new DensityFunctions.MulOrAdd(densityfunctions_twoargumentsimplefunction_type == DensityFunctions.TwoArgumentSimpleFunction.Type.ADD ? DensityFunctions.MulOrAdd.Type.ADD : DensityFunctions.MulOrAdd.Type.MUL, densityfunction1, d4, d5, densityfunctions_constant.value);
            }

            if (densityfunction1 instanceof DensityFunctions.Constant) {
               DensityFunctions.Constant densityfunctions_constant1 = (DensityFunctions.Constant)densityfunction1;
               return new DensityFunctions.MulOrAdd(densityfunctions_twoargumentsimplefunction_type == DensityFunctions.TwoArgumentSimpleFunction.Type.ADD ? DensityFunctions.MulOrAdd.Type.ADD : DensityFunctions.MulOrAdd.Type.MUL, densityfunction, d4, d5, densityfunctions_constant1.value);
            }
         }

         return new DensityFunctions.Ap2(densityfunctions_twoargumentsimplefunction_type, densityfunction, densityfunction1, d4, d5);
      }

      DensityFunctions.TwoArgumentSimpleFunction.Type type();

      DensityFunction argument1();

      DensityFunction argument2();

      default KeyDispatchDataCodec<? extends DensityFunction> codec() {
         return this.type().codec;
      }

      public static enum Type implements StringRepresentable {
         ADD("add"),
         MUL("mul"),
         MIN("min"),
         MAX("max");

         final KeyDispatchDataCodec<DensityFunctions.TwoArgumentSimpleFunction> codec = DensityFunctions.doubleFunctionArgumentCodec((densityfunction, densityfunction1) -> DensityFunctions.TwoArgumentSimpleFunction.create(this, densityfunction, densityfunction1), DensityFunctions.TwoArgumentSimpleFunction::argument1, DensityFunctions.TwoArgumentSimpleFunction::argument2);
         private final String name;

         private Type(String s) {
            this.name = s;
         }

         public String getSerializedName() {
            return this.name;
         }
      }
   }

   protected static record WeirdScaledSampler(DensityFunction input, DensityFunction.NoiseHolder noise, DensityFunctions.WeirdScaledSampler.RarityValueMapper rarityValueMapper) implements DensityFunctions.TransformerWithContext {
      private static final MapCodec<DensityFunctions.WeirdScaledSampler> DATA_CODEC = RecordCodecBuilder.mapCodec((recordcodecbuilder_instance) -> recordcodecbuilder_instance.group(DensityFunction.HOLDER_HELPER_CODEC.fieldOf("input").forGetter(DensityFunctions.WeirdScaledSampler::input), DensityFunction.NoiseHolder.CODEC.fieldOf("noise").forGetter(DensityFunctions.WeirdScaledSampler::noise), DensityFunctions.WeirdScaledSampler.RarityValueMapper.CODEC.fieldOf("rarity_value_mapper").forGetter(DensityFunctions.WeirdScaledSampler::rarityValueMapper)).apply(recordcodecbuilder_instance, DensityFunctions.WeirdScaledSampler::new));
      public static final KeyDispatchDataCodec<DensityFunctions.WeirdScaledSampler> CODEC = DensityFunctions.makeCodec(DATA_CODEC);

      public double transform(DensityFunction.FunctionContext densityfunction_functioncontext, double d0) {
         double d1 = this.rarityValueMapper.mapper.get(d0);
         return d1 * Math.abs(this.noise.getValue((double)densityfunction_functioncontext.blockX() / d1, (double)densityfunction_functioncontext.blockY() / d1, (double)densityfunction_functioncontext.blockZ() / d1));
      }

      public DensityFunction mapAll(DensityFunction.Visitor densityfunction_visitor) {
         return densityfunction_visitor.apply(new DensityFunctions.WeirdScaledSampler(this.input.mapAll(densityfunction_visitor), densityfunction_visitor.visitNoise(this.noise), this.rarityValueMapper));
      }

      public double minValue() {
         return 0.0D;
      }

      public double maxValue() {
         return this.rarityValueMapper.maxRarity * this.noise.maxValue();
      }

      public KeyDispatchDataCodec<? extends DensityFunction> codec() {
         return CODEC;
      }

      public static enum RarityValueMapper implements StringRepresentable {
         TYPE1("type_1", NoiseRouterData.QuantizedSpaghettiRarity::getSpaghettiRarity3D, 2.0D),
         TYPE2("type_2", NoiseRouterData.QuantizedSpaghettiRarity::getSphaghettiRarity2D, 3.0D);

         public static final Codec<DensityFunctions.WeirdScaledSampler.RarityValueMapper> CODEC = StringRepresentable.fromEnum(DensityFunctions.WeirdScaledSampler.RarityValueMapper::values);
         private final String name;
         final Double2DoubleFunction mapper;
         final double maxRarity;

         private RarityValueMapper(String s, Double2DoubleFunction double2doublefunction, double d0) {
            this.name = s;
            this.mapper = double2doublefunction;
            this.maxRarity = d0;
         }

         public String getSerializedName() {
            return this.name;
         }
      }
   }

   static record YClampedGradient(int fromY, int toY, double fromValue, double toValue) implements DensityFunction.SimpleFunction {
      private static final MapCodec<DensityFunctions.YClampedGradient> DATA_CODEC = RecordCodecBuilder.mapCodec((recordcodecbuilder_instance) -> recordcodecbuilder_instance.group(Codec.intRange(DimensionType.MIN_Y * 2, DimensionType.MAX_Y * 2).fieldOf("from_y").forGetter(DensityFunctions.YClampedGradient::fromY), Codec.intRange(DimensionType.MIN_Y * 2, DimensionType.MAX_Y * 2).fieldOf("to_y").forGetter(DensityFunctions.YClampedGradient::toY), DensityFunctions.NOISE_VALUE_CODEC.fieldOf("from_value").forGetter(DensityFunctions.YClampedGradient::fromValue), DensityFunctions.NOISE_VALUE_CODEC.fieldOf("to_value").forGetter(DensityFunctions.YClampedGradient::toValue)).apply(recordcodecbuilder_instance, DensityFunctions.YClampedGradient::new));
      public static final KeyDispatchDataCodec<DensityFunctions.YClampedGradient> CODEC = DensityFunctions.makeCodec(DATA_CODEC);

      public double compute(DensityFunction.FunctionContext densityfunction_functioncontext) {
         return Mth.clampedMap((double)densityfunction_functioncontext.blockY(), (double)this.fromY, (double)this.toY, this.fromValue, this.toValue);
      }

      public double minValue() {
         return Math.min(this.fromValue, this.toValue);
      }

      public double maxValue() {
         return Math.max(this.fromValue, this.toValue);
      }

      public KeyDispatchDataCodec<? extends DensityFunction> codec() {
         return CODEC;
      }
   }
}
