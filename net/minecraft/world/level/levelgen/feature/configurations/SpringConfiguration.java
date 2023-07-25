package net.minecraft.world.level.levelgen.feature.configurations;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.core.HolderSet;
import net.minecraft.core.RegistryCodecs;
import net.minecraft.core.registries.Registries;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.material.FluidState;

public class SpringConfiguration implements FeatureConfiguration {
   public static final Codec<SpringConfiguration> CODEC = RecordCodecBuilder.create((recordcodecbuilder_instance) -> recordcodecbuilder_instance.group(FluidState.CODEC.fieldOf("state").forGetter((springconfiguration4) -> springconfiguration4.state), Codec.BOOL.fieldOf("requires_block_below").orElse(true).forGetter((springconfiguration3) -> springconfiguration3.requiresBlockBelow), Codec.INT.fieldOf("rock_count").orElse(4).forGetter((springconfiguration2) -> springconfiguration2.rockCount), Codec.INT.fieldOf("hole_count").orElse(1).forGetter((springconfiguration1) -> springconfiguration1.holeCount), RegistryCodecs.homogeneousList(Registries.BLOCK).fieldOf("valid_blocks").forGetter((springconfiguration) -> springconfiguration.validBlocks)).apply(recordcodecbuilder_instance, SpringConfiguration::new));
   public final FluidState state;
   public final boolean requiresBlockBelow;
   public final int rockCount;
   public final int holeCount;
   public final HolderSet<Block> validBlocks;

   public SpringConfiguration(FluidState fluidstate, boolean flag, int i, int j, HolderSet<Block> holderset) {
      this.state = fluidstate;
      this.requiresBlockBelow = flag;
      this.rockCount = i;
      this.holeCount = j;
      this.validBlocks = holderset;
   }
}
