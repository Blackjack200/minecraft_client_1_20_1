package net.minecraft.world.level.levelgen.feature.stateproviders;

import com.google.common.collect.Lists;
import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import java.util.List;
import net.minecraft.core.BlockPos;
import net.minecraft.util.ExtraCodecs;
import net.minecraft.util.InclusiveRange;
import net.minecraft.util.Mth;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.levelgen.LegacyRandomSource;
import net.minecraft.world.level.levelgen.WorldgenRandom;
import net.minecraft.world.level.levelgen.synth.NormalNoise;

public class DualNoiseProvider extends NoiseProvider {
   public static final Codec<DualNoiseProvider> CODEC = RecordCodecBuilder.create((recordcodecbuilder_instance) -> recordcodecbuilder_instance.group(InclusiveRange.codec(Codec.INT, 1, 64).fieldOf("variety").forGetter((dualnoiseprovider2) -> dualnoiseprovider2.variety), NormalNoise.NoiseParameters.DIRECT_CODEC.fieldOf("slow_noise").forGetter((dualnoiseprovider1) -> dualnoiseprovider1.slowNoiseParameters), ExtraCodecs.POSITIVE_FLOAT.fieldOf("slow_scale").forGetter((dualnoiseprovider) -> dualnoiseprovider.slowScale)).and(noiseProviderCodec(recordcodecbuilder_instance)).apply(recordcodecbuilder_instance, DualNoiseProvider::new));
   private final InclusiveRange<Integer> variety;
   private final NormalNoise.NoiseParameters slowNoiseParameters;
   private final float slowScale;
   private final NormalNoise slowNoise;

   public DualNoiseProvider(InclusiveRange<Integer> inclusiverange, NormalNoise.NoiseParameters normalnoise_noiseparameters, float f, long i, NormalNoise.NoiseParameters normalnoise_noiseparameters1, float f1, List<BlockState> list) {
      super(i, normalnoise_noiseparameters1, f1, list);
      this.variety = inclusiverange;
      this.slowNoiseParameters = normalnoise_noiseparameters;
      this.slowScale = f;
      this.slowNoise = NormalNoise.create(new WorldgenRandom(new LegacyRandomSource(i)), normalnoise_noiseparameters);
   }

   protected BlockStateProviderType<?> type() {
      return BlockStateProviderType.DUAL_NOISE_PROVIDER;
   }

   public BlockState getState(RandomSource randomsource, BlockPos blockpos) {
      double d0 = this.getSlowNoiseValue(blockpos);
      int i = (int)Mth.clampedMap(d0, -1.0D, 1.0D, (double)this.variety.minInclusive().intValue(), (double)(this.variety.maxInclusive() + 1));
      List<BlockState> list = Lists.newArrayListWithCapacity(i);

      for(int j = 0; j < i; ++j) {
         list.add(this.getRandomState(this.states, this.getSlowNoiseValue(blockpos.offset(j * '\ud511', 0, j * '\u85ba'))));
      }

      return this.getRandomState(list, blockpos, (double)this.scale);
   }

   protected double getSlowNoiseValue(BlockPos blockpos) {
      return this.slowNoise.getValue((double)((float)blockpos.getX() * this.slowScale), (double)((float)blockpos.getY() * this.slowScale), (double)((float)blockpos.getZ() * this.slowScale));
   }
}
