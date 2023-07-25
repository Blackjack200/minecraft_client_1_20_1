package net.minecraft.world.level.levelgen;

import com.google.common.annotations.VisibleForTesting;
import com.mojang.serialization.Codec;
import net.minecraft.util.Mth;
import net.minecraft.util.RandomSource;

public class XoroshiroRandomSource implements RandomSource {
   private static final float FLOAT_UNIT = 5.9604645E-8F;
   private static final double DOUBLE_UNIT = (double)1.110223E-16F;
   public static final Codec<XoroshiroRandomSource> CODEC = Xoroshiro128PlusPlus.CODEC.xmap((xoroshiro128plusplus) -> new XoroshiroRandomSource(xoroshiro128plusplus), (xoroshirorandomsource) -> xoroshirorandomsource.randomNumberGenerator);
   private Xoroshiro128PlusPlus randomNumberGenerator;
   private final MarsagliaPolarGaussian gaussianSource = new MarsagliaPolarGaussian(this);

   public XoroshiroRandomSource(long i) {
      this.randomNumberGenerator = new Xoroshiro128PlusPlus(RandomSupport.upgradeSeedTo128bit(i));
   }

   public XoroshiroRandomSource(RandomSupport.Seed128bit randomsupport_seed128bit) {
      this.randomNumberGenerator = new Xoroshiro128PlusPlus(randomsupport_seed128bit);
   }

   public XoroshiroRandomSource(long i, long j) {
      this.randomNumberGenerator = new Xoroshiro128PlusPlus(i, j);
   }

   private XoroshiroRandomSource(Xoroshiro128PlusPlus xoroshiro128plusplus) {
      this.randomNumberGenerator = xoroshiro128plusplus;
   }

   public RandomSource fork() {
      return new XoroshiroRandomSource(this.randomNumberGenerator.nextLong(), this.randomNumberGenerator.nextLong());
   }

   public PositionalRandomFactory forkPositional() {
      return new XoroshiroRandomSource.XoroshiroPositionalRandomFactory(this.randomNumberGenerator.nextLong(), this.randomNumberGenerator.nextLong());
   }

   public void setSeed(long i) {
      this.randomNumberGenerator = new Xoroshiro128PlusPlus(RandomSupport.upgradeSeedTo128bit(i));
      this.gaussianSource.reset();
   }

   public int nextInt() {
      return (int)this.randomNumberGenerator.nextLong();
   }

   public int nextInt(int i) {
      if (i <= 0) {
         throw new IllegalArgumentException("Bound must be positive");
      } else {
         long j = Integer.toUnsignedLong(this.nextInt());
         long k = j * (long)i;
         long l = k & 4294967295L;
         if (l < (long)i) {
            for(int i1 = Integer.remainderUnsigned(~i + 1, i); l < (long)i1; l = k & 4294967295L) {
               j = Integer.toUnsignedLong(this.nextInt());
               k = j * (long)i;
            }
         }

         long j1 = k >> 32;
         return (int)j1;
      }
   }

   public long nextLong() {
      return this.randomNumberGenerator.nextLong();
   }

   public boolean nextBoolean() {
      return (this.randomNumberGenerator.nextLong() & 1L) != 0L;
   }

   public float nextFloat() {
      return (float)this.nextBits(24) * 5.9604645E-8F;
   }

   public double nextDouble() {
      return (double)this.nextBits(53) * (double)1.110223E-16F;
   }

   public double nextGaussian() {
      return this.gaussianSource.nextGaussian();
   }

   public void consumeCount(int i) {
      for(int j = 0; j < i; ++j) {
         this.randomNumberGenerator.nextLong();
      }

   }

   private long nextBits(int i) {
      return this.randomNumberGenerator.nextLong() >>> 64 - i;
   }

   public static class XoroshiroPositionalRandomFactory implements PositionalRandomFactory {
      private final long seedLo;
      private final long seedHi;

      public XoroshiroPositionalRandomFactory(long i, long j) {
         this.seedLo = i;
         this.seedHi = j;
      }

      public RandomSource at(int i, int j, int k) {
         long l = Mth.getSeed(i, j, k);
         long i1 = l ^ this.seedLo;
         return new XoroshiroRandomSource(i1, this.seedHi);
      }

      public RandomSource fromHashOf(String s) {
         RandomSupport.Seed128bit randomsupport_seed128bit = RandomSupport.seedFromHashOf(s);
         return new XoroshiroRandomSource(randomsupport_seed128bit.xor(this.seedLo, this.seedHi));
      }

      @VisibleForTesting
      public void parityConfigString(StringBuilder stringbuilder) {
         stringbuilder.append("seedLo: ").append(this.seedLo).append(", seedHi: ").append(this.seedHi);
      }
   }
}
