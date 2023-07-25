package net.minecraft.world.level.levelgen.feature.stateproviders;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import java.util.List;
import net.minecraft.Util;
import net.minecraft.core.BlockPos;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.levelgen.synth.NormalNoise;

public class NoiseThresholdProvider extends NoiseBasedStateProvider {
   public static final Codec<NoiseThresholdProvider> CODEC = RecordCodecBuilder.create((recordcodecbuilder_instance) -> noiseCodec(recordcodecbuilder_instance).and(recordcodecbuilder_instance.group(Codec.floatRange(-1.0F, 1.0F).fieldOf("threshold").forGetter((noisethresholdprovider4) -> noisethresholdprovider4.threshold), Codec.floatRange(0.0F, 1.0F).fieldOf("high_chance").forGetter((noisethresholdprovider3) -> noisethresholdprovider3.highChance), BlockState.CODEC.fieldOf("default_state").forGetter((noisethresholdprovider2) -> noisethresholdprovider2.defaultState), Codec.list(BlockState.CODEC).fieldOf("low_states").forGetter((noisethresholdprovider1) -> noisethresholdprovider1.lowStates), Codec.list(BlockState.CODEC).fieldOf("high_states").forGetter((noisethresholdprovider) -> noisethresholdprovider.highStates))).apply(recordcodecbuilder_instance, NoiseThresholdProvider::new));
   private final float threshold;
   private final float highChance;
   private final BlockState defaultState;
   private final List<BlockState> lowStates;
   private final List<BlockState> highStates;

   public NoiseThresholdProvider(long i, NormalNoise.NoiseParameters normalnoise_noiseparameters, float f, float f1, float f2, BlockState blockstate, List<BlockState> list, List<BlockState> list1) {
      super(i, normalnoise_noiseparameters, f);
      this.threshold = f1;
      this.highChance = f2;
      this.defaultState = blockstate;
      this.lowStates = list;
      this.highStates = list1;
   }

   protected BlockStateProviderType<?> type() {
      return BlockStateProviderType.NOISE_THRESHOLD_PROVIDER;
   }

   public BlockState getState(RandomSource randomsource, BlockPos blockpos) {
      double d0 = this.getNoiseValue(blockpos, (double)this.scale);
      if (d0 < (double)this.threshold) {
         return Util.getRandom(this.lowStates, randomsource);
      } else {
         return randomsource.nextFloat() < this.highChance ? Util.getRandom(this.highStates, randomsource) : this.defaultState;
      }
   }
}
