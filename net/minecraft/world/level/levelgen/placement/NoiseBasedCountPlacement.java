package net.minecraft.world.level.levelgen.placement;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.core.BlockPos;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.biome.Biome;

public class NoiseBasedCountPlacement extends RepeatingPlacement {
   public static final Codec<NoiseBasedCountPlacement> CODEC = RecordCodecBuilder.create((recordcodecbuilder_instance) -> recordcodecbuilder_instance.group(Codec.INT.fieldOf("noise_to_count_ratio").forGetter((noisebasedcountplacement2) -> noisebasedcountplacement2.noiseToCountRatio), Codec.DOUBLE.fieldOf("noise_factor").forGetter((noisebasedcountplacement1) -> noisebasedcountplacement1.noiseFactor), Codec.DOUBLE.fieldOf("noise_offset").orElse(0.0D).forGetter((noisebasedcountplacement) -> noisebasedcountplacement.noiseOffset)).apply(recordcodecbuilder_instance, NoiseBasedCountPlacement::new));
   private final int noiseToCountRatio;
   private final double noiseFactor;
   private final double noiseOffset;

   private NoiseBasedCountPlacement(int i, double d0, double d1) {
      this.noiseToCountRatio = i;
      this.noiseFactor = d0;
      this.noiseOffset = d1;
   }

   public static NoiseBasedCountPlacement of(int i, double d0, double d1) {
      return new NoiseBasedCountPlacement(i, d0, d1);
   }

   protected int count(RandomSource randomsource, BlockPos blockpos) {
      double d0 = Biome.BIOME_INFO_NOISE.getValue((double)blockpos.getX() / this.noiseFactor, (double)blockpos.getZ() / this.noiseFactor, false);
      return (int)Math.ceil((d0 + this.noiseOffset) * (double)this.noiseToCountRatio);
   }

   public PlacementModifierType<?> type() {
      return PlacementModifierType.NOISE_BASED_COUNT;
   }
}
