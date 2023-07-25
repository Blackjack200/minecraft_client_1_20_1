package net.minecraft.world;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.levelgen.RandomSupport;
import net.minecraft.world.level.levelgen.XoroshiroRandomSource;

public class RandomSequence {
   public static final Codec<RandomSequence> CODEC = RecordCodecBuilder.create((recordcodecbuilder_instance) -> recordcodecbuilder_instance.group(XoroshiroRandomSource.CODEC.fieldOf("source").forGetter((randomsequence) -> randomsequence.source)).apply(recordcodecbuilder_instance, RandomSequence::new));
   private final XoroshiroRandomSource source;

   public RandomSequence(XoroshiroRandomSource xoroshirorandomsource) {
      this.source = xoroshirorandomsource;
   }

   public RandomSequence(long i, ResourceLocation resourcelocation) {
      this(createSequence(i, resourcelocation));
   }

   private static XoroshiroRandomSource createSequence(long i, ResourceLocation resourcelocation) {
      return new XoroshiroRandomSource(RandomSupport.upgradeSeedTo128bitUnmixed(i).xor(seedForKey(resourcelocation)).mixed());
   }

   public static RandomSupport.Seed128bit seedForKey(ResourceLocation resourcelocation) {
      return RandomSupport.seedFromHashOf(resourcelocation.toString());
   }

   public RandomSource random() {
      return this.source;
   }
}
