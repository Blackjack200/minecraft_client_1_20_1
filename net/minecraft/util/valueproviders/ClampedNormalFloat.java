package net.minecraft.util.valueproviders;

import com.mojang.serialization.Codec;
import com.mojang.serialization.DataResult;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import java.util.function.Function;
import net.minecraft.util.Mth;
import net.minecraft.util.RandomSource;

public class ClampedNormalFloat extends FloatProvider {
   public static final Codec<ClampedNormalFloat> CODEC = RecordCodecBuilder.create((recordcodecbuilder_instance) -> recordcodecbuilder_instance.group(Codec.FLOAT.fieldOf("mean").forGetter((clampednormalfloat3) -> clampednormalfloat3.mean), Codec.FLOAT.fieldOf("deviation").forGetter((clampednormalfloat2) -> clampednormalfloat2.deviation), Codec.FLOAT.fieldOf("min").forGetter((clampednormalfloat1) -> clampednormalfloat1.min), Codec.FLOAT.fieldOf("max").forGetter((clampednormalfloat) -> clampednormalfloat.max)).apply(recordcodecbuilder_instance, ClampedNormalFloat::new)).comapFlatMap((clampednormalfloat) -> clampednormalfloat.max < clampednormalfloat.min ? DataResult.error(() -> "Max must be larger than min: [" + clampednormalfloat.min + ", " + clampednormalfloat.max + "]") : DataResult.success(clampednormalfloat), Function.identity());
   private final float mean;
   private final float deviation;
   private final float min;
   private final float max;

   public static ClampedNormalFloat of(float f, float f1, float f2, float f3) {
      return new ClampedNormalFloat(f, f1, f2, f3);
   }

   private ClampedNormalFloat(float f, float f1, float f2, float f3) {
      this.mean = f;
      this.deviation = f1;
      this.min = f2;
      this.max = f3;
   }

   public float sample(RandomSource randomsource) {
      return sample(randomsource, this.mean, this.deviation, this.min, this.max);
   }

   public static float sample(RandomSource randomsource, float f, float f1, float f2, float f3) {
      return Mth.clamp(Mth.normal(randomsource, f, f1), f2, f3);
   }

   public float getMinValue() {
      return this.min;
   }

   public float getMaxValue() {
      return this.max;
   }

   public FloatProviderType<?> getType() {
      return FloatProviderType.CLAMPED_NORMAL;
   }

   public String toString() {
      return "normal(" + this.mean + ", " + this.deviation + ") in [" + this.min + "-" + this.max + "]";
   }
}
