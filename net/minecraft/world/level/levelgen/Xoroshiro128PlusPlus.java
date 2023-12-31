package net.minecraft.world.level.levelgen;

import com.mojang.serialization.Codec;
import java.util.stream.LongStream;
import net.minecraft.Util;

public class Xoroshiro128PlusPlus {
   private long seedLo;
   private long seedHi;
   public static final Codec<Xoroshiro128PlusPlus> CODEC = Codec.LONG_STREAM.comapFlatMap((longstream) -> Util.fixedSize(longstream, 2).map((along) -> new Xoroshiro128PlusPlus(along[0], along[1])), (xoroshiro128plusplus) -> LongStream.of(xoroshiro128plusplus.seedLo, xoroshiro128plusplus.seedHi));

   public Xoroshiro128PlusPlus(RandomSupport.Seed128bit randomsupport_seed128bit) {
      this(randomsupport_seed128bit.seedLo(), randomsupport_seed128bit.seedHi());
   }

   public Xoroshiro128PlusPlus(long i, long j) {
      this.seedLo = i;
      this.seedHi = j;
      if ((this.seedLo | this.seedHi) == 0L) {
         this.seedLo = -7046029254386353131L;
         this.seedHi = 7640891576956012809L;
      }

   }

   public long nextLong() {
      long i = this.seedLo;
      long j = this.seedHi;
      long k = Long.rotateLeft(i + j, 17) + i;
      j ^= i;
      this.seedLo = Long.rotateLeft(i, 49) ^ j ^ j << 21;
      this.seedHi = Long.rotateLeft(j, 28);
      return k;
   }
}
