package net.minecraft.world.level.levelgen.placement;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.core.BlockPos;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.biome.Biome;

public class NoiseThresholdCountPlacement extends RepeatingPlacement {
   public static final Codec<NoiseThresholdCountPlacement> CODEC = RecordCodecBuilder.create((recordcodecbuilder_instance) -> recordcodecbuilder_instance.group(Codec.DOUBLE.fieldOf("noise_level").forGetter((noisethresholdcountplacement2) -> noisethresholdcountplacement2.noiseLevel), Codec.INT.fieldOf("below_noise").forGetter((noisethresholdcountplacement1) -> noisethresholdcountplacement1.belowNoise), Codec.INT.fieldOf("above_noise").forGetter((noisethresholdcountplacement) -> noisethresholdcountplacement.aboveNoise)).apply(recordcodecbuilder_instance, NoiseThresholdCountPlacement::new));
   private final double noiseLevel;
   private final int belowNoise;
   private final int aboveNoise;

   private NoiseThresholdCountPlacement(double d0, int i, int j) {
      this.noiseLevel = d0;
      this.belowNoise = i;
      this.aboveNoise = j;
   }

   public static NoiseThresholdCountPlacement of(double d0, int i, int j) {
      return new NoiseThresholdCountPlacement(d0, i, j);
   }

   protected int count(RandomSource randomsource, BlockPos blockpos) {
      double d0 = Biome.BIOME_INFO_NOISE.getValue((double)blockpos.getX() / 200.0D, (double)blockpos.getZ() / 200.0D, false);
      return d0 < this.noiseLevel ? this.belowNoise : this.aboveNoise;
   }

   public PlacementModifierType<?> type() {
      return PlacementModifierType.NOISE_THRESHOLD_COUNT;
   }
}
