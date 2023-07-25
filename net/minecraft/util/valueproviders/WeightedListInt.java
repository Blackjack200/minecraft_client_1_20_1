package net.minecraft.util.valueproviders;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import java.util.List;
import net.minecraft.util.RandomSource;
import net.minecraft.util.random.SimpleWeightedRandomList;
import net.minecraft.util.random.WeightedEntry;

public class WeightedListInt extends IntProvider {
   public static final Codec<WeightedListInt> CODEC = RecordCodecBuilder.create((recordcodecbuilder_instance) -> recordcodecbuilder_instance.group(SimpleWeightedRandomList.wrappedCodec(IntProvider.CODEC).fieldOf("distribution").forGetter((weightedlistint) -> weightedlistint.distribution)).apply(recordcodecbuilder_instance, WeightedListInt::new));
   private final SimpleWeightedRandomList<IntProvider> distribution;
   private final int minValue;
   private final int maxValue;

   public WeightedListInt(SimpleWeightedRandomList<IntProvider> simpleweightedrandomlist) {
      this.distribution = simpleweightedrandomlist;
      List<WeightedEntry.Wrapper<IntProvider>> list = simpleweightedrandomlist.unwrap();
      int i = Integer.MAX_VALUE;
      int j = Integer.MIN_VALUE;

      for(WeightedEntry.Wrapper<IntProvider> weightedentry_wrapper : list) {
         int k = weightedentry_wrapper.getData().getMinValue();
         int l = weightedentry_wrapper.getData().getMaxValue();
         i = Math.min(i, k);
         j = Math.max(j, l);
      }

      this.minValue = i;
      this.maxValue = j;
   }

   public int sample(RandomSource randomsource) {
      return this.distribution.getRandomValue(randomsource).orElseThrow(IllegalStateException::new).sample(randomsource);
   }

   public int getMinValue() {
      return this.minValue;
   }

   public int getMaxValue() {
      return this.maxValue;
   }

   public IntProviderType<?> getType() {
      return IntProviderType.WEIGHTED_LIST;
   }
}
