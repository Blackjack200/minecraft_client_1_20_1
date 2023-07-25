package net.minecraft.world.level.levelgen.feature.stateproviders;

import com.mojang.datafixers.Products;
import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.core.BlockPos;
import net.minecraft.util.ExtraCodecs;
import net.minecraft.world.level.levelgen.LegacyRandomSource;
import net.minecraft.world.level.levelgen.WorldgenRandom;
import net.minecraft.world.level.levelgen.synth.NormalNoise;

public abstract class NoiseBasedStateProvider extends BlockStateProvider {
   protected final long seed;
   protected final NormalNoise.NoiseParameters parameters;
   protected final float scale;
   protected final NormalNoise noise;

   protected static <P extends NoiseBasedStateProvider> Products.P3<RecordCodecBuilder.Mu<P>, Long, NormalNoise.NoiseParameters, Float> noiseCodec(RecordCodecBuilder.Instance<P> recordcodecbuilder_instance) {
      return recordcodecbuilder_instance.group(Codec.LONG.fieldOf("seed").forGetter((noisebasedstateprovider2) -> noisebasedstateprovider2.seed), NormalNoise.NoiseParameters.DIRECT_CODEC.fieldOf("noise").forGetter((noisebasedstateprovider1) -> noisebasedstateprovider1.parameters), ExtraCodecs.POSITIVE_FLOAT.fieldOf("scale").forGetter((noisebasedstateprovider) -> noisebasedstateprovider.scale));
   }

   protected NoiseBasedStateProvider(long i, NormalNoise.NoiseParameters normalnoise_noiseparameters, float f) {
      this.seed = i;
      this.parameters = normalnoise_noiseparameters;
      this.scale = f;
      this.noise = NormalNoise.create(new WorldgenRandom(new LegacyRandomSource(i)), normalnoise_noiseparameters);
   }

   protected double getNoiseValue(BlockPos blockpos, double d0) {
      return this.noise.getValue((double)blockpos.getX() * d0, (double)blockpos.getY() * d0, (double)blockpos.getZ() * d0);
   }
}
