package net.minecraft.util.valueproviders;

import com.mojang.serialization.Codec;
import com.mojang.serialization.DataResult;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import java.util.function.Function;
import net.minecraft.util.RandomSource;

public class TrapezoidFloat extends FloatProvider {
   public static final Codec<TrapezoidFloat> CODEC = RecordCodecBuilder.create((recordcodecbuilder_instance) -> recordcodecbuilder_instance.group(Codec.FLOAT.fieldOf("min").forGetter((trapezoidfloat2) -> trapezoidfloat2.min), Codec.FLOAT.fieldOf("max").forGetter((trapezoidfloat1) -> trapezoidfloat1.max), Codec.FLOAT.fieldOf("plateau").forGetter((trapezoidfloat) -> trapezoidfloat.plateau)).apply(recordcodecbuilder_instance, TrapezoidFloat::new)).comapFlatMap((trapezoidfloat) -> {
      if (trapezoidfloat.max < trapezoidfloat.min) {
         return DataResult.error(() -> "Max must be larger than min: [" + trapezoidfloat.min + ", " + trapezoidfloat.max + "]");
      } else {
         return trapezoidfloat.plateau > trapezoidfloat.max - trapezoidfloat.min ? DataResult.error(() -> "Plateau can at most be the full span: [" + trapezoidfloat.min + ", " + trapezoidfloat.max + "]") : DataResult.success(trapezoidfloat);
      }
   }, Function.identity());
   private final float min;
   private final float max;
   private final float plateau;

   public static TrapezoidFloat of(float f, float f1, float f2) {
      return new TrapezoidFloat(f, f1, f2);
   }

   private TrapezoidFloat(float f, float f1, float f2) {
      this.min = f;
      this.max = f1;
      this.plateau = f2;
   }

   public float sample(RandomSource randomsource) {
      float f = this.max - this.min;
      float f1 = (f - this.plateau) / 2.0F;
      float f2 = f - f1;
      return this.min + randomsource.nextFloat() * f2 + randomsource.nextFloat() * f1;
   }

   public float getMinValue() {
      return this.min;
   }

   public float getMaxValue() {
      return this.max;
   }

   public FloatProviderType<?> getType() {
      return FloatProviderType.TRAPEZOID;
   }

   public String toString() {
      return "trapezoid(" + this.plateau + ") in [" + this.min + "-" + this.max + "]";
   }
}
