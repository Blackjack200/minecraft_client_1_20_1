package net.minecraft.world.level.levelgen;

import net.minecraft.util.RandomSource;

public class SingleThreadedRandomSource implements BitRandomSource {
   private static final int MODULUS_BITS = 48;
   private static final long MODULUS_MASK = 281474976710655L;
   private static final long MULTIPLIER = 25214903917L;
   private static final long INCREMENT = 11L;
   private long seed;
   private final MarsagliaPolarGaussian gaussianSource = new MarsagliaPolarGaussian(this);

   public SingleThreadedRandomSource(long i) {
      this.setSeed(i);
   }

   public RandomSource fork() {
      return new SingleThreadedRandomSource(this.nextLong());
   }

   public PositionalRandomFactory forkPositional() {
      return new LegacyRandomSource.LegacyPositionalRandomFactory(this.nextLong());
   }

   public void setSeed(long i) {
      this.seed = (i ^ 25214903917L) & 281474976710655L;
      this.gaussianSource.reset();
   }

   public int next(int i) {
      long j = this.seed * 25214903917L + 11L & 281474976710655L;
      this.seed = j;
      return (int)(j >> 48 - i);
   }

   public double nextGaussian() {
      return this.gaussianSource.nextGaussian();
   }
}
