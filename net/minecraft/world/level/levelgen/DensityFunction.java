package net.minecraft.world.level.levelgen;

import com.mojang.serialization.Codec;
import javax.annotation.Nullable;
import net.minecraft.core.Holder;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.RegistryFileCodec;
import net.minecraft.util.KeyDispatchDataCodec;
import net.minecraft.world.level.levelgen.blending.Blender;
import net.minecraft.world.level.levelgen.synth.NormalNoise;

public interface DensityFunction {
   Codec<DensityFunction> DIRECT_CODEC = DensityFunctions.DIRECT_CODEC;
   Codec<Holder<DensityFunction>> CODEC = RegistryFileCodec.create(Registries.DENSITY_FUNCTION, DIRECT_CODEC);
   Codec<DensityFunction> HOLDER_HELPER_CODEC = CODEC.xmap(DensityFunctions.HolderHolder::new, (densityfunction) -> {
      if (densityfunction instanceof DensityFunctions.HolderHolder densityfunctions_holderholder) {
         return densityfunctions_holderholder.function();
      } else {
         return new Holder.Direct<>(densityfunction);
      }
   });

   double compute(DensityFunction.FunctionContext densityfunction_functioncontext);

   void fillArray(double[] adouble, DensityFunction.ContextProvider densityfunction_contextprovider);

   DensityFunction mapAll(DensityFunction.Visitor densityfunction_visitor);

   double minValue();

   double maxValue();

   KeyDispatchDataCodec<? extends DensityFunction> codec();

   default DensityFunction clamp(double d0, double d1) {
      return new DensityFunctions.Clamp(this, d0, d1);
   }

   default DensityFunction abs() {
      return DensityFunctions.map(this, DensityFunctions.Mapped.Type.ABS);
   }

   default DensityFunction square() {
      return DensityFunctions.map(this, DensityFunctions.Mapped.Type.SQUARE);
   }

   default DensityFunction cube() {
      return DensityFunctions.map(this, DensityFunctions.Mapped.Type.CUBE);
   }

   default DensityFunction halfNegative() {
      return DensityFunctions.map(this, DensityFunctions.Mapped.Type.HALF_NEGATIVE);
   }

   default DensityFunction quarterNegative() {
      return DensityFunctions.map(this, DensityFunctions.Mapped.Type.QUARTER_NEGATIVE);
   }

   default DensityFunction squeeze() {
      return DensityFunctions.map(this, DensityFunctions.Mapped.Type.SQUEEZE);
   }

   public interface ContextProvider {
      DensityFunction.FunctionContext forIndex(int i);

      void fillAllDirectly(double[] adouble, DensityFunction densityfunction);
   }

   public interface FunctionContext {
      int blockX();

      int blockY();

      int blockZ();

      default Blender getBlender() {
         return Blender.empty();
      }
   }

   public static record NoiseHolder(Holder<NormalNoise.NoiseParameters> noiseData, @Nullable NormalNoise noise) {
      public static final Codec<DensityFunction.NoiseHolder> CODEC = NormalNoise.NoiseParameters.CODEC.xmap((holder) -> new DensityFunction.NoiseHolder(holder, (NormalNoise)null), DensityFunction.NoiseHolder::noiseData);

      public NoiseHolder(Holder<NormalNoise.NoiseParameters> holder) {
         this(holder, (NormalNoise)null);
      }

      public double getValue(double d0, double d1, double d2) {
         return this.noise == null ? 0.0D : this.noise.getValue(d0, d1, d2);
      }

      public double maxValue() {
         return this.noise == null ? 2.0D : this.noise.maxValue();
      }
   }

   public interface SimpleFunction extends DensityFunction {
      default void fillArray(double[] adouble, DensityFunction.ContextProvider densityfunction_contextprovider) {
         densityfunction_contextprovider.fillAllDirectly(adouble, this);
      }

      default DensityFunction mapAll(DensityFunction.Visitor densityfunction_visitor) {
         return densityfunction_visitor.apply(this);
      }
   }

   public static record SinglePointContext(int blockX, int blockY, int blockZ) implements DensityFunction.FunctionContext {
   }

   public interface Visitor {
      DensityFunction apply(DensityFunction densityfunction);

      default DensityFunction.NoiseHolder visitNoise(DensityFunction.NoiseHolder densityfunction_noiseholder) {
         return densityfunction_noiseholder;
      }
   }
}
