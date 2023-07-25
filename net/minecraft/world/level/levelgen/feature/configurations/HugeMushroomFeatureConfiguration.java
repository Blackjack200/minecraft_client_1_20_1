package net.minecraft.world.level.levelgen.feature.configurations;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.world.level.levelgen.feature.stateproviders.BlockStateProvider;

public class HugeMushroomFeatureConfiguration implements FeatureConfiguration {
   public static final Codec<HugeMushroomFeatureConfiguration> CODEC = RecordCodecBuilder.create((recordcodecbuilder_instance) -> recordcodecbuilder_instance.group(BlockStateProvider.CODEC.fieldOf("cap_provider").forGetter((hugemushroomfeatureconfiguration2) -> hugemushroomfeatureconfiguration2.capProvider), BlockStateProvider.CODEC.fieldOf("stem_provider").forGetter((hugemushroomfeatureconfiguration1) -> hugemushroomfeatureconfiguration1.stemProvider), Codec.INT.fieldOf("foliage_radius").orElse(2).forGetter((hugemushroomfeatureconfiguration) -> hugemushroomfeatureconfiguration.foliageRadius)).apply(recordcodecbuilder_instance, HugeMushroomFeatureConfiguration::new));
   public final BlockStateProvider capProvider;
   public final BlockStateProvider stemProvider;
   public final int foliageRadius;

   public HugeMushroomFeatureConfiguration(BlockStateProvider blockstateprovider, BlockStateProvider blockstateprovider1, int i) {
      this.capProvider = blockstateprovider;
      this.stemProvider = blockstateprovider1;
      this.foliageRadius = i;
   }
}
