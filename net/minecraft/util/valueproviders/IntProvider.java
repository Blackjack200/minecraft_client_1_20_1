package net.minecraft.util.valueproviders;

import com.mojang.datafixers.util.Either;
import com.mojang.serialization.Codec;
import com.mojang.serialization.DataResult;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.util.ExtraCodecs;
import net.minecraft.util.RandomSource;

public abstract class IntProvider {
   private static final Codec<Either<Integer, IntProvider>> CONSTANT_OR_DISPATCH_CODEC = Codec.either(Codec.INT, BuiltInRegistries.INT_PROVIDER_TYPE.byNameCodec().dispatch(IntProvider::getType, IntProviderType::codec));
   public static final Codec<IntProvider> CODEC = CONSTANT_OR_DISPATCH_CODEC.xmap((either) -> either.map(ConstantInt::of, (intprovider) -> intprovider), (intprovider) -> intprovider.getType() == IntProviderType.CONSTANT ? Either.left(((ConstantInt)intprovider).getValue()) : Either.right(intprovider));
   public static final Codec<IntProvider> NON_NEGATIVE_CODEC = codec(0, Integer.MAX_VALUE);
   public static final Codec<IntProvider> POSITIVE_CODEC = codec(1, Integer.MAX_VALUE);

   public static Codec<IntProvider> codec(int i, int j) {
      return codec(i, j, CODEC);
   }

   public static <T extends IntProvider> Codec<T> codec(int i, int j, Codec<T> codec) {
      return ExtraCodecs.validate(codec, (intprovider) -> {
         if (intprovider.getMinValue() < i) {
            return DataResult.error(() -> "Value provider too low: " + i + " [" + intprovider.getMinValue() + "-" + intprovider.getMaxValue() + "]");
         } else {
            return intprovider.getMaxValue() > j ? DataResult.error(() -> "Value provider too high: " + j + " [" + intprovider.getMinValue() + "-" + intprovider.getMaxValue() + "]") : DataResult.success(intprovider);
         }
      });
   }

   public abstract int sample(RandomSource randomsource);

   public abstract int getMinValue();

   public abstract int getMaxValue();

   public abstract IntProviderType<?> getType();
}
