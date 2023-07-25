package net.minecraft.world.level.levelgen.feature.stateproviders;

import com.mojang.datafixers.Products;
import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import java.util.List;
import net.minecraft.core.BlockPos;
import net.minecraft.util.Mth;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.levelgen.synth.NormalNoise;

public class NoiseProvider extends NoiseBasedStateProvider {
   public static final Codec<NoiseProvider> CODEC = RecordCodecBuilder.create((recordcodecbuilder_instance) -> noiseProviderCodec(recordcodecbuilder_instance).apply(recordcodecbuilder_instance, NoiseProvider::new));
   protected final List<BlockState> states;

   protected static <P extends NoiseProvider> Products.P4<RecordCodecBuilder.Mu<P>, Long, NormalNoise.NoiseParameters, Float, List<BlockState>> noiseProviderCodec(RecordCodecBuilder.Instance<P> recordcodecbuilder_instance) {
      return noiseCodec(recordcodecbuilder_instance).and(Codec.list(BlockState.CODEC).fieldOf("states").forGetter((noiseprovider) -> noiseprovider.states));
   }

   public NoiseProvider(long i, NormalNoise.NoiseParameters normalnoise_noiseparameters, float f, List<BlockState> list) {
      super(i, normalnoise_noiseparameters, f);
      this.states = list;
   }

   protected BlockStateProviderType<?> type() {
      return BlockStateProviderType.NOISE_PROVIDER;
   }

   public BlockState getState(RandomSource randomsource, BlockPos blockpos) {
      return this.getRandomState(this.states, blockpos, (double)this.scale);
   }

   protected BlockState getRandomState(List<BlockState> list, BlockPos blockpos, double d0) {
      double d1 = this.getNoiseValue(blockpos, d0);
      return this.getRandomState(list, d1);
   }

   protected BlockState getRandomState(List<BlockState> list, double d0) {
      double d1 = Mth.clamp((1.0D + d0) / 2.0D, 0.0D, 0.9999D);
      return list.get((int)(d1 * (double)list.size()));
   }
}
