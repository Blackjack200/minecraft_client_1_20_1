package net.minecraft.world.level.levelgen.feature.featuresize;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import java.util.OptionalInt;

public class TwoLayersFeatureSize extends FeatureSize {
   public static final Codec<TwoLayersFeatureSize> CODEC = RecordCodecBuilder.create((recordcodecbuilder_instance) -> recordcodecbuilder_instance.group(Codec.intRange(0, 81).fieldOf("limit").orElse(1).forGetter((twolayersfeaturesize2) -> twolayersfeaturesize2.limit), Codec.intRange(0, 16).fieldOf("lower_size").orElse(0).forGetter((twolayersfeaturesize1) -> twolayersfeaturesize1.lowerSize), Codec.intRange(0, 16).fieldOf("upper_size").orElse(1).forGetter((twolayersfeaturesize) -> twolayersfeaturesize.upperSize), minClippedHeightCodec()).apply(recordcodecbuilder_instance, TwoLayersFeatureSize::new));
   private final int limit;
   private final int lowerSize;
   private final int upperSize;

   public TwoLayersFeatureSize(int i, int j, int k) {
      this(i, j, k, OptionalInt.empty());
   }

   public TwoLayersFeatureSize(int i, int j, int k, OptionalInt optionalint) {
      super(optionalint);
      this.limit = i;
      this.lowerSize = j;
      this.upperSize = k;
   }

   protected FeatureSizeType<?> type() {
      return FeatureSizeType.TWO_LAYERS_FEATURE_SIZE;
   }

   public int getSizeAtHeight(int i, int j) {
      return j < this.limit ? this.lowerSize : this.upperSize;
   }
}
