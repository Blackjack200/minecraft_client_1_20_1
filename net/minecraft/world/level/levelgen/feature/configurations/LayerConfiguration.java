package net.minecraft.world.level.levelgen.feature.configurations;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.dimension.DimensionType;

public class LayerConfiguration implements FeatureConfiguration {
   public static final Codec<LayerConfiguration> CODEC = RecordCodecBuilder.create((recordcodecbuilder_instance) -> recordcodecbuilder_instance.group(Codec.intRange(0, DimensionType.Y_SIZE).fieldOf("height").forGetter((layerconfiguration1) -> layerconfiguration1.height), BlockState.CODEC.fieldOf("state").forGetter((layerconfiguration) -> layerconfiguration.state)).apply(recordcodecbuilder_instance, LayerConfiguration::new));
   public final int height;
   public final BlockState state;

   public LayerConfiguration(int i, BlockState blockstate) {
      this.height = i;
      this.state = blockstate;
   }
}
