package net.minecraft.world.level.levelgen.heightproviders;

import com.mojang.logging.LogUtils;
import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import it.unimi.dsi.fastutil.longs.LongOpenHashSet;
import it.unimi.dsi.fastutil.longs.LongSet;
import net.minecraft.util.Mth;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.levelgen.VerticalAnchor;
import net.minecraft.world.level.levelgen.WorldGenerationContext;
import org.slf4j.Logger;

public class UniformHeight extends HeightProvider {
   public static final Codec<UniformHeight> CODEC = RecordCodecBuilder.create((recordcodecbuilder_instance) -> recordcodecbuilder_instance.group(VerticalAnchor.CODEC.fieldOf("min_inclusive").forGetter((uniformheight1) -> uniformheight1.minInclusive), VerticalAnchor.CODEC.fieldOf("max_inclusive").forGetter((uniformheight) -> uniformheight.maxInclusive)).apply(recordcodecbuilder_instance, UniformHeight::new));
   private static final Logger LOGGER = LogUtils.getLogger();
   private final VerticalAnchor minInclusive;
   private final VerticalAnchor maxInclusive;
   private final LongSet warnedFor = new LongOpenHashSet();

   private UniformHeight(VerticalAnchor verticalanchor, VerticalAnchor verticalanchor1) {
      this.minInclusive = verticalanchor;
      this.maxInclusive = verticalanchor1;
   }

   public static UniformHeight of(VerticalAnchor verticalanchor, VerticalAnchor verticalanchor1) {
      return new UniformHeight(verticalanchor, verticalanchor1);
   }

   public int sample(RandomSource randomsource, WorldGenerationContext worldgenerationcontext) {
      int i = this.minInclusive.resolveY(worldgenerationcontext);
      int j = this.maxInclusive.resolveY(worldgenerationcontext);
      if (i > j) {
         if (this.warnedFor.add((long)i << 32 | (long)j)) {
            LOGGER.warn("Empty height range: {}", (Object)this);
         }

         return i;
      } else {
         return Mth.randomBetweenInclusive(randomsource, i, j);
      }
   }

   public HeightProviderType<?> getType() {
      return HeightProviderType.UNIFORM;
   }

   public String toString() {
      return "[" + this.minInclusive + "-" + this.maxInclusive + "]";
   }
}
