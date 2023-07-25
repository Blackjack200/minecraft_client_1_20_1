package net.minecraft.world.level.levelgen.feature.configurations;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.util.valueproviders.IntProvider;
import net.minecraft.world.level.block.state.BlockState;

public class ReplaceSphereConfiguration implements FeatureConfiguration {
   public static final Codec<ReplaceSphereConfiguration> CODEC = RecordCodecBuilder.create((recordcodecbuilder_instance) -> recordcodecbuilder_instance.group(BlockState.CODEC.fieldOf("target").forGetter((replacesphereconfiguration2) -> replacesphereconfiguration2.targetState), BlockState.CODEC.fieldOf("state").forGetter((replacesphereconfiguration1) -> replacesphereconfiguration1.replaceState), IntProvider.codec(0, 12).fieldOf("radius").forGetter((replacesphereconfiguration) -> replacesphereconfiguration.radius)).apply(recordcodecbuilder_instance, ReplaceSphereConfiguration::new));
   public final BlockState targetState;
   public final BlockState replaceState;
   private final IntProvider radius;

   public ReplaceSphereConfiguration(BlockState blockstate, BlockState blockstate1, IntProvider intprovider) {
      this.targetState = blockstate;
      this.replaceState = blockstate1;
      this.radius = intprovider;
   }

   public IntProvider radius() {
      return this.radius;
   }
}
