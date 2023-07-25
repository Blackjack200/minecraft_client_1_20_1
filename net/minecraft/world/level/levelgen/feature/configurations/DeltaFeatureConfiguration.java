package net.minecraft.world.level.levelgen.feature.configurations;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.util.valueproviders.IntProvider;
import net.minecraft.world.level.block.state.BlockState;

public class DeltaFeatureConfiguration implements FeatureConfiguration {
   public static final Codec<DeltaFeatureConfiguration> CODEC = RecordCodecBuilder.create((recordcodecbuilder_instance) -> recordcodecbuilder_instance.group(BlockState.CODEC.fieldOf("contents").forGetter((deltafeatureconfiguration3) -> deltafeatureconfiguration3.contents), BlockState.CODEC.fieldOf("rim").forGetter((deltafeatureconfiguration2) -> deltafeatureconfiguration2.rim), IntProvider.codec(0, 16).fieldOf("size").forGetter((deltafeatureconfiguration1) -> deltafeatureconfiguration1.size), IntProvider.codec(0, 16).fieldOf("rim_size").forGetter((deltafeatureconfiguration) -> deltafeatureconfiguration.rimSize)).apply(recordcodecbuilder_instance, DeltaFeatureConfiguration::new));
   private final BlockState contents;
   private final BlockState rim;
   private final IntProvider size;
   private final IntProvider rimSize;

   public DeltaFeatureConfiguration(BlockState blockstate, BlockState blockstate1, IntProvider intprovider, IntProvider intprovider1) {
      this.contents = blockstate;
      this.rim = blockstate1;
      this.size = intprovider;
      this.rimSize = intprovider1;
   }

   public BlockState contents() {
      return this.contents;
   }

   public BlockState rim() {
      return this.rim;
   }

   public IntProvider size() {
      return this.size;
   }

   public IntProvider rimSize() {
      return this.rimSize;
   }
}
