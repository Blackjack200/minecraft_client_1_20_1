package net.minecraft.world.level.levelgen.heightproviders;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.util.RandomSource;
import net.minecraft.util.random.SimpleWeightedRandomList;
import net.minecraft.world.level.levelgen.WorldGenerationContext;

public class WeightedListHeight extends HeightProvider {
   public static final Codec<WeightedListHeight> CODEC = RecordCodecBuilder.create((recordcodecbuilder_instance) -> recordcodecbuilder_instance.group(SimpleWeightedRandomList.wrappedCodec(HeightProvider.CODEC).fieldOf("distribution").forGetter((weightedlistheight) -> weightedlistheight.distribution)).apply(recordcodecbuilder_instance, WeightedListHeight::new));
   private final SimpleWeightedRandomList<HeightProvider> distribution;

   public WeightedListHeight(SimpleWeightedRandomList<HeightProvider> simpleweightedrandomlist) {
      this.distribution = simpleweightedrandomlist;
   }

   public int sample(RandomSource randomsource, WorldGenerationContext worldgenerationcontext) {
      return this.distribution.getRandomValue(randomsource).orElseThrow(IllegalStateException::new).sample(randomsource, worldgenerationcontext);
   }

   public HeightProviderType<?> getType() {
      return HeightProviderType.WEIGHTED_LIST;
   }
}
