package net.minecraft.world.level.levelgen.heightproviders;

import com.mojang.datafixers.util.Either;
import com.mojang.serialization.Codec;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.levelgen.VerticalAnchor;
import net.minecraft.world.level.levelgen.WorldGenerationContext;

public abstract class HeightProvider {
   private static final Codec<Either<VerticalAnchor, HeightProvider>> CONSTANT_OR_DISPATCH_CODEC = Codec.either(VerticalAnchor.CODEC, BuiltInRegistries.HEIGHT_PROVIDER_TYPE.byNameCodec().dispatch(HeightProvider::getType, HeightProviderType::codec));
   public static final Codec<HeightProvider> CODEC = CONSTANT_OR_DISPATCH_CODEC.xmap((either) -> either.map(ConstantHeight::of, (heightprovider) -> heightprovider), (heightprovider) -> heightprovider.getType() == HeightProviderType.CONSTANT ? Either.left(((ConstantHeight)heightprovider).getValue()) : Either.right(heightprovider));

   public abstract int sample(RandomSource randomsource, WorldGenerationContext worldgenerationcontext);

   public abstract HeightProviderType<?> getType();
}
