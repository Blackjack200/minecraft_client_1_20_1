package net.minecraft.world.level.levelgen;

import net.minecraft.util.RandomSource;

public interface BitRandomSource extends RandomSource {
   float FLOAT_MULTIPLIER = 5.9604645E-8F;
   double DOUBLE_MULTIPLIER = (double)1.110223E-16F;

   int next(int i);

   default int nextInt() {
      return this.next(32);
   }

   default int nextInt(int i) {
      if (i <= 0) {
         throw new IllegalArgumentException("Bound must be positive");
      } else if ((i & i - 1) == 0) {
         return (int)((long)i * (long)this.next(31) >> 31);
      } else {
         int j;
         int k;
         do {
            j = this.next(31);
            k = j % i;
         } while(j - k + (i - 1) < 0);

         return k;
      }
   }

   default long nextLong() {
      int i = this.next(32);
      int j = this.next(32);
      long k = (long)i << 32;
      return k + (long)j;
   }

   default boolean nextBoolean() {
      return this.next(1) != 0;
   }

   default float nextFloat() {
      return (float)this.next(24) * 5.9604645E-8F;
   }

   default double nextDouble() {
      int i = this.next(26);
      int j = this.next(27);
      long k = ((long)i << 27) + (long)j;
      return (double)k * (double)1.110223E-16F;
   }
}
