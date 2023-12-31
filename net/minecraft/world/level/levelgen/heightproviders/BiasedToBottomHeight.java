package net.minecraft.world.level.levelgen.heightproviders;

import com.mojang.logging.LogUtils;
import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.levelgen.VerticalAnchor;
import net.minecraft.world.level.levelgen.WorldGenerationContext;
import org.slf4j.Logger;

public class BiasedToBottomHeight extends HeightProvider {
   public static final Codec<BiasedToBottomHeight> CODEC = RecordCodecBuilder.create((recordcodecbuilder_instance) -> recordcodecbuilder_instance.group(VerticalAnchor.CODEC.fieldOf("min_inclusive").forGetter((biasedtobottomheight2) -> biasedtobottomheight2.minInclusive), VerticalAnchor.CODEC.fieldOf("max_inclusive").forGetter((biasedtobottomheight1) -> biasedtobottomheight1.maxInclusive), Codec.intRange(1, Integer.MAX_VALUE).optionalFieldOf("inner", 1).forGetter((biasedtobottomheight) -> biasedtobottomheight.inner)).apply(recordcodecbuilder_instance, BiasedToBottomHeight::new));
   private static final Logger LOGGER = LogUtils.getLogger();
   private final VerticalAnchor minInclusive;
   private final VerticalAnchor maxInclusive;
   private final int inner;

   private BiasedToBottomHeight(VerticalAnchor verticalanchor, VerticalAnchor verticalanchor1, int i) {
      this.minInclusive = verticalanchor;
      this.maxInclusive = verticalanchor1;
      this.inner = i;
   }

   public static BiasedToBottomHeight of(VerticalAnchor verticalanchor, VerticalAnchor verticalanchor1, int i) {
      return new BiasedToBottomHeight(verticalanchor, verticalanchor1, i);
   }

   public int sample(RandomSource randomsource, WorldGenerationContext worldgenerationcontext) {
      int i = this.minInclusive.resolveY(worldgenerationcontext);
      int j = this.maxInclusive.resolveY(worldgenerationcontext);
      if (j - i - this.inner + 1 <= 0) {
         LOGGER.warn("Empty height range: {}", (Object)this);
         return i;
      } else {
         int k = randomsource.nextInt(j - i - this.inner + 1);
         return randomsource.nextInt(k + this.inner) + i;
      }
   }

   public HeightProviderType<?> getType() {
      return HeightProviderType.BIASED_TO_BOTTOM;
   }

   public String toString() {
      return "biased[" + this.minInclusive + "-" + this.maxInclusive + " inner: " + this.inner + "]";
   }
}
