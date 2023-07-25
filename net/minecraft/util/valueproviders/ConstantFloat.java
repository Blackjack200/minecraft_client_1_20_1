package net.minecraft.util.valueproviders;

import com.mojang.datafixers.util.Either;
import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.util.RandomSource;

public class ConstantFloat extends FloatProvider {
   public static final ConstantFloat ZERO = new ConstantFloat(0.0F);
   public static final Codec<ConstantFloat> CODEC = Codec.either(Codec.FLOAT, RecordCodecBuilder.create((recordcodecbuilder_instance) -> recordcodecbuilder_instance.group(Codec.FLOAT.fieldOf("value").forGetter((constantfloat) -> constantfloat.value)).apply(recordcodecbuilder_instance, ConstantFloat::new))).xmap((either) -> either.map(ConstantFloat::of, (constantfloat) -> constantfloat), (constantfloat) -> Either.left(constantfloat.value));
   private final float value;

   public static ConstantFloat of(float f) {
      return f == 0.0F ? ZERO : new ConstantFloat(f);
   }

   private ConstantFloat(float f) {
      this.value = f;
   }

   public float getValue() {
      return this.value;
   }

   public float sample(RandomSource randomsource) {
      return this.value;
   }

   public float getMinValue() {
      return this.value;
   }

   public float getMaxValue() {
      return this.value + 1.0F;
   }

   public FloatProviderType<?> getType() {
      return FloatProviderType.CONSTANT;
   }

   public String toString() {
      return Float.toString(this.value);
   }
}
