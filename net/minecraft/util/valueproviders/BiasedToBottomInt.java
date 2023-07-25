package net.minecraft.util.valueproviders;

import com.mojang.serialization.Codec;
import com.mojang.serialization.DataResult;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import java.util.function.Function;
import net.minecraft.util.RandomSource;

public class BiasedToBottomInt extends IntProvider {
   public static final Codec<BiasedToBottomInt> CODEC = RecordCodecBuilder.create((recordcodecbuilder_instance) -> recordcodecbuilder_instance.group(Codec.INT.fieldOf("min_inclusive").forGetter((biasedtobottomint1) -> biasedtobottomint1.minInclusive), Codec.INT.fieldOf("max_inclusive").forGetter((biasedtobottomint) -> biasedtobottomint.maxInclusive)).apply(recordcodecbuilder_instance, BiasedToBottomInt::new)).comapFlatMap((biasedtobottomint) -> biasedtobottomint.maxInclusive < biasedtobottomint.minInclusive ? DataResult.error(() -> "Max must be at least min, min_inclusive: " + biasedtobottomint.minInclusive + ", max_inclusive: " + biasedtobottomint.maxInclusive) : DataResult.success(biasedtobottomint), Function.identity());
   private final int minInclusive;
   private final int maxInclusive;

   private BiasedToBottomInt(int i, int j) {
      this.minInclusive = i;
      this.maxInclusive = j;
   }

   public static BiasedToBottomInt of(int i, int j) {
      return new BiasedToBottomInt(i, j);
   }

   public int sample(RandomSource randomsource) {
      return this.minInclusive + randomsource.nextInt(randomsource.nextInt(this.maxInclusive - this.minInclusive + 1) + 1);
   }

   public int getMinValue() {
      return this.minInclusive;
   }

   public int getMaxValue() {
      return this.maxInclusive;
   }

   public IntProviderType<?> getType() {
      return IntProviderType.BIASED_TO_BOTTOM;
   }

   public String toString() {
      return "[" + this.minInclusive + "-" + this.maxInclusive + "]";
   }
}
