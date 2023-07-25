package net.minecraft.world.level.levelgen.synth;

import com.google.common.annotations.VisibleForTesting;
import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import it.unimi.dsi.fastutil.doubles.DoubleArrayList;
import it.unimi.dsi.fastutil.doubles.DoubleList;
import it.unimi.dsi.fastutil.doubles.DoubleListIterator;
import java.util.List;
import net.minecraft.Util;
import net.minecraft.core.Holder;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.RegistryFileCodec;
import net.minecraft.util.RandomSource;

public class NormalNoise {
   private static final double INPUT_FACTOR = 1.0181268882175227D;
   private static final double TARGET_DEVIATION = 0.3333333333333333D;
   private final double valueFactor;
   private final PerlinNoise first;
   private final PerlinNoise second;
   private final double maxValue;
   private final NormalNoise.NoiseParameters parameters;

   /** @deprecated */
   @Deprecated
   public static NormalNoise createLegacyNetherBiome(RandomSource randomsource, NormalNoise.NoiseParameters normalnoise_noiseparameters) {
      return new NormalNoise(randomsource, normalnoise_noiseparameters, false);
   }

   public static NormalNoise create(RandomSource randomsource, int i, double... adouble) {
      return create(randomsource, new NormalNoise.NoiseParameters(i, new DoubleArrayList(adouble)));
   }

   public static NormalNoise create(RandomSource randomsource, NormalNoise.NoiseParameters normalnoise_noiseparameters) {
      return new NormalNoise(randomsource, normalnoise_noiseparameters, true);
   }

   private NormalNoise(RandomSource randomsource, NormalNoise.NoiseParameters normalnoise_noiseparameters, boolean flag) {
      int i = normalnoise_noiseparameters.firstOctave;
      DoubleList doublelist = normalnoise_noiseparameters.amplitudes;
      this.parameters = normalnoise_noiseparameters;
      if (flag) {
         this.first = PerlinNoise.create(randomsource, i, doublelist);
         this.second = PerlinNoise.create(randomsource, i, doublelist);
      } else {
         this.first = PerlinNoise.createLegacyForLegacyNetherBiome(randomsource, i, doublelist);
         this.second = PerlinNoise.createLegacyForLegacyNetherBiome(randomsource, i, doublelist);
      }

      int j = Integer.MAX_VALUE;
      int k = Integer.MIN_VALUE;
      DoubleListIterator doublelistiterator = doublelist.iterator();

      while(doublelistiterator.hasNext()) {
         int l = doublelistiterator.nextIndex();
         double d0 = doublelistiterator.nextDouble();
         if (d0 != 0.0D) {
            j = Math.min(j, l);
            k = Math.max(k, l);
         }
      }

      this.valueFactor = 0.16666666666666666D / expectedDeviation(k - j);
      this.maxValue = (this.first.maxValue() + this.second.maxValue()) * this.valueFactor;
   }

   public double maxValue() {
      return this.maxValue;
   }

   private static double expectedDeviation(int i) {
      return 0.1D * (1.0D + 1.0D / (double)(i + 1));
   }

   public double getValue(double d0, double d1, double d2) {
      double d3 = d0 * 1.0181268882175227D;
      double d4 = d1 * 1.0181268882175227D;
      double d5 = d2 * 1.0181268882175227D;
      return (this.first.getValue(d0, d1, d2) + this.second.getValue(d3, d4, d5)) * this.valueFactor;
   }

   public NormalNoise.NoiseParameters parameters() {
      return this.parameters;
   }

   @VisibleForTesting
   public void parityConfigString(StringBuilder stringbuilder) {
      stringbuilder.append("NormalNoise {");
      stringbuilder.append("first: ");
      this.first.parityConfigString(stringbuilder);
      stringbuilder.append(", second: ");
      this.second.parityConfigString(stringbuilder);
      stringbuilder.append("}");
   }

   public static record NoiseParameters(int firstOctave, DoubleList amplitudes) {
      final int firstOctave;
      final DoubleList amplitudes;
      public static final Codec<NormalNoise.NoiseParameters> DIRECT_CODEC = RecordCodecBuilder.create((recordcodecbuilder_instance) -> recordcodecbuilder_instance.group(Codec.INT.fieldOf("firstOctave").forGetter(NormalNoise.NoiseParameters::firstOctave), Codec.DOUBLE.listOf().fieldOf("amplitudes").forGetter(NormalNoise.NoiseParameters::amplitudes)).apply(recordcodecbuilder_instance, NormalNoise.NoiseParameters::new));
      public static final Codec<Holder<NormalNoise.NoiseParameters>> CODEC = RegistryFileCodec.create(Registries.NOISE, DIRECT_CODEC);

      public NoiseParameters(int i, List<Double> list) {
         this(i, new DoubleArrayList(list));
      }

      public NoiseParameters(int i, double d0, double... adouble) {
         this(i, Util.make(new DoubleArrayList(adouble), (doublearraylist) -> doublearraylist.add(0, d0)));
      }
   }
}
