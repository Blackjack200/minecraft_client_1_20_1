package net.minecraft.world.level.levelgen.synth;

import com.google.common.annotations.VisibleForTesting;
import com.google.common.collect.ImmutableList;
import com.mojang.datafixers.util.Pair;
import it.unimi.dsi.fastutil.doubles.DoubleArrayList;
import it.unimi.dsi.fastutil.doubles.DoubleList;
import it.unimi.dsi.fastutil.ints.IntBidirectionalIterator;
import it.unimi.dsi.fastutil.ints.IntRBTreeSet;
import it.unimi.dsi.fastutil.ints.IntSortedSet;
import java.util.Arrays;
import java.util.List;
import java.util.Locale;
import java.util.Objects;
import java.util.stream.IntStream;
import javax.annotation.Nullable;
import net.minecraft.util.Mth;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.levelgen.PositionalRandomFactory;

public class PerlinNoise {
   private static final int ROUND_OFF = 33554432;
   private final ImprovedNoise[] noiseLevels;
   private final int firstOctave;
   private final DoubleList amplitudes;
   private final double lowestFreqValueFactor;
   private final double lowestFreqInputFactor;
   private final double maxValue;

   /** @deprecated */
   @Deprecated
   public static PerlinNoise createLegacyForBlendedNoise(RandomSource randomsource, IntStream intstream) {
      return new PerlinNoise(randomsource, makeAmplitudes(new IntRBTreeSet(intstream.boxed().collect(ImmutableList.toImmutableList()))), false);
   }

   /** @deprecated */
   @Deprecated
   public static PerlinNoise createLegacyForLegacyNetherBiome(RandomSource randomsource, int i, DoubleList doublelist) {
      return new PerlinNoise(randomsource, Pair.of(i, doublelist), false);
   }

   public static PerlinNoise create(RandomSource randomsource, IntStream intstream) {
      return create(randomsource, intstream.boxed().collect(ImmutableList.toImmutableList()));
   }

   public static PerlinNoise create(RandomSource randomsource, List<Integer> list) {
      return new PerlinNoise(randomsource, makeAmplitudes(new IntRBTreeSet(list)), true);
   }

   public static PerlinNoise create(RandomSource randomsource, int i, double d0, double... adouble) {
      DoubleArrayList doublearraylist = new DoubleArrayList(adouble);
      doublearraylist.add(0, d0);
      return new PerlinNoise(randomsource, Pair.of(i, doublearraylist), true);
   }

   public static PerlinNoise create(RandomSource randomsource, int i, DoubleList doublelist) {
      return new PerlinNoise(randomsource, Pair.of(i, doublelist), true);
   }

   private static Pair<Integer, DoubleList> makeAmplitudes(IntSortedSet intsortedset) {
      if (intsortedset.isEmpty()) {
         throw new IllegalArgumentException("Need some octaves!");
      } else {
         int i = -intsortedset.firstInt();
         int j = intsortedset.lastInt();
         int k = i + j + 1;
         if (k < 1) {
            throw new IllegalArgumentException("Total number of octaves needs to be >= 1");
         } else {
            DoubleList doublelist = new DoubleArrayList(new double[k]);
            IntBidirectionalIterator intbidirectionaliterator = intsortedset.iterator();

            while(intbidirectionaliterator.hasNext()) {
               int l = intbidirectionaliterator.nextInt();
               doublelist.set(l + i, 1.0D);
            }

            return Pair.of(-i, doublelist);
         }
      }
   }

   protected PerlinNoise(RandomSource randomsource, Pair<Integer, DoubleList> pair, boolean flag) {
      this.firstOctave = pair.getFirst();
      this.amplitudes = pair.getSecond();
      int i = this.amplitudes.size();
      int j = -this.firstOctave;
      this.noiseLevels = new ImprovedNoise[i];
      if (flag) {
         PositionalRandomFactory positionalrandomfactory = randomsource.forkPositional();

         for(int k = 0; k < i; ++k) {
            if (this.amplitudes.getDouble(k) != 0.0D) {
               int l = this.firstOctave + k;
               this.noiseLevels[k] = new ImprovedNoise(positionalrandomfactory.fromHashOf("octave_" + l));
            }
         }
      } else {
         ImprovedNoise improvednoise = new ImprovedNoise(randomsource);
         if (j >= 0 && j < i) {
            double d0 = this.amplitudes.getDouble(j);
            if (d0 != 0.0D) {
               this.noiseLevels[j] = improvednoise;
            }
         }

         for(int i1 = j - 1; i1 >= 0; --i1) {
            if (i1 < i) {
               double d1 = this.amplitudes.getDouble(i1);
               if (d1 != 0.0D) {
                  this.noiseLevels[i1] = new ImprovedNoise(randomsource);
               } else {
                  skipOctave(randomsource);
               }
            } else {
               skipOctave(randomsource);
            }
         }

         if (Arrays.stream(this.noiseLevels).filter(Objects::nonNull).count() != this.amplitudes.stream().filter((odouble) -> odouble != 0.0D).count()) {
            throw new IllegalStateException("Failed to create correct number of noise levels for given non-zero amplitudes");
         }

         if (j < i - 1) {
            throw new IllegalArgumentException("Positive octaves are temporarily disabled");
         }
      }

      this.lowestFreqInputFactor = Math.pow(2.0D, (double)(-j));
      this.lowestFreqValueFactor = Math.pow(2.0D, (double)(i - 1)) / (Math.pow(2.0D, (double)i) - 1.0D);
      this.maxValue = this.edgeValue(2.0D);
   }

   protected double maxValue() {
      return this.maxValue;
   }

   private static void skipOctave(RandomSource randomsource) {
      randomsource.consumeCount(262);
   }

   public double getValue(double d0, double d1, double d2) {
      return this.getValue(d0, d1, d2, 0.0D, 0.0D, false);
   }

   /** @deprecated */
   @Deprecated
   public double getValue(double d0, double d1, double d2, double d3, double d4, boolean flag) {
      double d5 = 0.0D;
      double d6 = this.lowestFreqInputFactor;
      double d7 = this.lowestFreqValueFactor;

      for(int i = 0; i < this.noiseLevels.length; ++i) {
         ImprovedNoise improvednoise = this.noiseLevels[i];
         if (improvednoise != null) {
            double d8 = improvednoise.noise(wrap(d0 * d6), flag ? -improvednoise.yo : wrap(d1 * d6), wrap(d2 * d6), d3 * d6, d4 * d6);
            d5 += this.amplitudes.getDouble(i) * d8 * d7;
         }

         d6 *= 2.0D;
         d7 /= 2.0D;
      }

      return d5;
   }

   public double maxBrokenValue(double d0) {
      return this.edgeValue(d0 + 2.0D);
   }

   private double edgeValue(double d0) {
      double d1 = 0.0D;
      double d2 = this.lowestFreqValueFactor;

      for(int i = 0; i < this.noiseLevels.length; ++i) {
         ImprovedNoise improvednoise = this.noiseLevels[i];
         if (improvednoise != null) {
            d1 += this.amplitudes.getDouble(i) * d0 * d2;
         }

         d2 /= 2.0D;
      }

      return d1;
   }

   @Nullable
   public ImprovedNoise getOctaveNoise(int i) {
      return this.noiseLevels[this.noiseLevels.length - 1 - i];
   }

   public static double wrap(double d0) {
      return d0 - (double)Mth.lfloor(d0 / 3.3554432E7D + 0.5D) * 3.3554432E7D;
   }

   protected int firstOctave() {
      return this.firstOctave;
   }

   protected DoubleList amplitudes() {
      return this.amplitudes;
   }

   @VisibleForTesting
   public void parityConfigString(StringBuilder stringbuilder) {
      stringbuilder.append("PerlinNoise{");
      List<String> list = this.amplitudes.stream().map((odouble) -> String.format(Locale.ROOT, "%.2f", odouble)).toList();
      stringbuilder.append("first octave: ").append(this.firstOctave).append(", amplitudes: ").append((Object)list).append(", noise levels: [");

      for(int i = 0; i < this.noiseLevels.length; ++i) {
         stringbuilder.append(i).append(": ");
         ImprovedNoise improvednoise = this.noiseLevels[i];
         if (improvednoise == null) {
            stringbuilder.append("null");
         } else {
            improvednoise.parityConfigString(stringbuilder);
         }

         stringbuilder.append(", ");
      }

      stringbuilder.append("]");
      stringbuilder.append("}");
   }
}
