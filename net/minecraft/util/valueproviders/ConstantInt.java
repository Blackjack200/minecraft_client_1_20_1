package net.minecraft.util.valueproviders;

import com.mojang.datafixers.util.Either;
import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.util.RandomSource;

public class ConstantInt extends IntProvider {
   public static final ConstantInt ZERO = new ConstantInt(0);
   public static final Codec<ConstantInt> CODEC = Codec.either(Codec.INT, RecordCodecBuilder.create((recordcodecbuilder_instance) -> recordcodecbuilder_instance.group(Codec.INT.fieldOf("value").forGetter((constantint) -> constantint.value)).apply(recordcodecbuilder_instance, ConstantInt::new))).xmap((either) -> either.map(ConstantInt::of, (constantint) -> constantint), (constantint) -> Either.left(constantint.value));
   private final int value;

   public static ConstantInt of(int i) {
      return i == 0 ? ZERO : new ConstantInt(i);
   }

   private ConstantInt(int i) {
      this.value = i;
   }

   public int getValue() {
      return this.value;
   }

   public int sample(RandomSource randomsource) {
      return this.value;
   }

   public int getMinValue() {
      return this.value;
   }

   public int getMaxValue() {
      return this.value;
   }

   public IntProviderType<?> getType() {
      return IntProviderType.CONSTANT;
   }

   public String toString() {
      return Integer.toString(this.value);
   }
}
