package net.minecraft.world.level.levelgen.feature.configurations;

import com.google.common.collect.ImmutableList;
import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import java.util.List;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.levelgen.structure.templatesystem.BlockStateMatchTest;

public class ReplaceBlockConfiguration implements FeatureConfiguration {
   public static final Codec<ReplaceBlockConfiguration> CODEC = RecordCodecBuilder.create((recordcodecbuilder_instance) -> recordcodecbuilder_instance.group(Codec.list(OreConfiguration.TargetBlockState.CODEC).fieldOf("targets").forGetter((replaceblockconfiguration) -> replaceblockconfiguration.targetStates)).apply(recordcodecbuilder_instance, ReplaceBlockConfiguration::new));
   public final List<OreConfiguration.TargetBlockState> targetStates;

   public ReplaceBlockConfiguration(BlockState blockstate, BlockState blockstate1) {
      this(ImmutableList.of(OreConfiguration.target(new BlockStateMatchTest(blockstate), blockstate1)));
   }

   public ReplaceBlockConfiguration(List<OreConfiguration.TargetBlockState> list) {
      this.targetStates = list;
   }
}
