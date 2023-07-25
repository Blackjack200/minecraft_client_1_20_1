package net.minecraft.world.level.levelgen.heightproviders;

import com.mojang.logging.LogUtils;
import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.util.Mth;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.levelgen.VerticalAnchor;
import net.minecraft.world.level.levelgen.WorldGenerationContext;
import org.slf4j.Logger;

public class TrapezoidHeight extends HeightProvider {
   public static final Codec<TrapezoidHeight> CODEC = RecordCodecBuilder.create((recordcodecbuilder_instance) -> recordcodecbuilder_instance.group(VerticalAnchor.CODEC.fieldOf("min_inclusive").forGetter((trapezoidheight2) -> trapezoidheight2.minInclusive), VerticalAnchor.CODEC.fieldOf("max_inclusive").forGetter((trapezoidheight1) -> trapezoidheight1.maxInclusive), Codec.INT.optionalFieldOf("plateau", Integer.valueOf(0)).forGetter((trapezoidheight) -> trapezoidheight.plateau)).apply(recordcodecbuilder_instance, TrapezoidHeight::new));
   private static final Logger LOGGER = LogUtils.getLogger();
   private final VerticalAnchor minInclusive;
   private final VerticalAnchor maxInclusive;
   private final int plateau;

   private TrapezoidHeight(VerticalAnchor verticalanchor, VerticalAnchor verticalanchor1, int i) {
      this.minInclusive = verticalanchor;
      this.maxInclusive = verticalanchor1;
      this.plateau = i;
   }

   public static TrapezoidHeight of(VerticalAnchor verticalanchor, VerticalAnchor verticalanchor1, int i) {
      return new TrapezoidHeight(verticalanchor, verticalanchor1, i);
   }

   public static TrapezoidHeight of(VerticalAnchor verticalanchor, VerticalAnchor verticalanchor1) {
      return of(verticalanchor, verticalanchor1, 0);
   }

   public int sample(RandomSource randomsource, WorldGenerationContext worldgenerationcontext) {
      int i = this.minInclusive.resolveY(worldgenerationcontext);
      int j = this.maxInclusive.resolveY(worldgenerationcontext);
      if (i > j) {
         LOGGER.warn("Empty height range: {}", (Object)this);
         return i;
      } else {
         int k = j - i;
         if (this.plateau >= k) {
            return Mth.randomBetweenInclusive(randomsource, i, j);
         } else {
            int l = (k - this.plateau) / 2;
            int i1 = k - l;
            return i + Mth.randomBetweenInclusive(randomsource, 0, i1) + Mth.randomBetweenInclusive(randomsource, 0, l);
         }
      }
   }

   public HeightProviderType<?> getType() {
      return HeightProviderType.TRAPEZOID;
   }

   public String toString() {
      return this.plateau == 0 ? "triangle (" + this.minInclusive + "-" + this.maxInclusive + ")" : "trapezoid(" + this.plateau + ") in [" + this.minInclusive + "-" + this.maxInclusive + "]";
   }
}
