package net.minecraft.util.valueproviders;

import com.mojang.serialization.Codec;
import com.mojang.serialization.DataResult;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import java.util.function.Function;
import net.minecraft.util.Mth;
import net.minecraft.util.RandomSource;

public class UniformFloat extends FloatProvider {
   public static final Codec<UniformFloat> CODEC = RecordCodecBuilder.create((recordcodecbuilder_instance) -> recordcodecbuilder_instance.group(Codec.FLOAT.fieldOf("min_inclusive").forGetter((uniformfloat1) -> uniformfloat1.minInclusive), Codec.FLOAT.fieldOf("max_exclusive").forGetter((uniformfloat) -> uniformfloat.maxExclusive)).apply(recordcodecbuilder_instance, UniformFloat::new)).comapFlatMap((uniformfloat) -> uniformfloat.maxExclusive <= uniformfloat.minInclusive ? DataResult.error(() -> "Max must be larger than min, min_inclusive: " + uniformfloat.minInclusive + ", max_exclusive: " + uniformfloat.maxExclusive) : DataResult.success(uniformfloat), Function.identity());
   private final float minInclusive;
   private final float maxExclusive;

   private UniformFloat(float f, float f1) {
      this.minInclusive = f;
      this.maxExclusive = f1;
   }

   public static UniformFloat of(float f, float f1) {
      if (f1 <= f) {
         throw new IllegalArgumentException("Max must exceed min");
      } else {
         return new UniformFloat(f, f1);
      }
   }

   public float sample(RandomSource randomsource) {
      return Mth.randomBetween(randomsource, this.minInclusive, this.maxExclusive);
   }

   public float getMinValue() {
      return this.minInclusive;
   }

   public float getMaxValue() {
      return this.maxExclusive;
   }

   public FloatProviderType<?> getType() {
      return FloatProviderType.UNIFORM;
   }

   public String toString() {
      return "[" + this.minInclusive + "-" + this.maxExclusive + "]";
   }
}
