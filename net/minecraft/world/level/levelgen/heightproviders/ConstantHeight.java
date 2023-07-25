package net.minecraft.world.level.levelgen.heightproviders;

import com.mojang.datafixers.util.Either;
import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.levelgen.VerticalAnchor;
import net.minecraft.world.level.levelgen.WorldGenerationContext;

public class ConstantHeight extends HeightProvider {
   public static final ConstantHeight ZERO = new ConstantHeight(VerticalAnchor.absolute(0));
   public static final Codec<ConstantHeight> CODEC = Codec.either(VerticalAnchor.CODEC, RecordCodecBuilder.create((recordcodecbuilder_instance) -> recordcodecbuilder_instance.group(VerticalAnchor.CODEC.fieldOf("value").forGetter((constantheight) -> constantheight.value)).apply(recordcodecbuilder_instance, ConstantHeight::new))).xmap((either) -> either.map(ConstantHeight::of, (constantheight) -> constantheight), (constantheight) -> Either.left(constantheight.value));
   private final VerticalAnchor value;

   public static ConstantHeight of(VerticalAnchor verticalanchor) {
      return new ConstantHeight(verticalanchor);
   }

   private ConstantHeight(VerticalAnchor verticalanchor) {
      this.value = verticalanchor;
   }

   public VerticalAnchor getValue() {
      return this.value;
   }

   public int sample(RandomSource randomsource, WorldGenerationContext worldgenerationcontext) {
      return this.value.resolveY(worldgenerationcontext);
   }

   public HeightProviderType<?> getType() {
      return HeightProviderType.CONSTANT;
   }

   public String toString() {
      return this.value.toString();
   }
}
