package net.minecraft.util.valueproviders;

import com.mojang.serialization.Codec;
import com.mojang.serialization.DataResult;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import java.util.function.Function;
import net.minecraft.util.Mth;
import net.minecraft.util.RandomSource;

public class UniformInt extends IntProvider {
   public static final Codec<UniformInt> CODEC = RecordCodecBuilder.create((recordcodecbuilder_instance) -> recordcodecbuilder_instance.group(Codec.INT.fieldOf("min_inclusive").forGetter((uniformint1) -> uniformint1.minInclusive), Codec.INT.fieldOf("max_inclusive").forGetter((uniformint) -> uniformint.maxInclusive)).apply(recordcodecbuilder_instance, UniformInt::new)).comapFlatMap((uniformint) -> uniformint.maxInclusive < uniformint.minInclusive ? DataResult.error(() -> "Max must be at least min, min_inclusive: " + uniformint.minInclusive + ", max_inclusive: " + uniformint.maxInclusive) : DataResult.success(uniformint), Function.identity());
   private final int minInclusive;
   private final int maxInclusive;

   private UniformInt(int i, int j) {
      this.minInclusive = i;
      this.maxInclusive = j;
   }

   public static UniformInt of(int i, int j) {
      return new UniformInt(i, j);
   }

   public int sample(RandomSource randomsource) {
      return Mth.randomBetweenInclusive(randomsource, this.minInclusive, this.maxInclusive);
   }

   public int getMinValue() {
      return this.minInclusive;
   }

   public int getMaxValue() {
      return this.maxInclusive;
   }

   public IntProviderType<?> getType() {
      return IntProviderType.UNIFORM;
   }

   public String toString() {
      return "[" + this.minInclusive + "-" + this.maxInclusive + "]";
   }
}
