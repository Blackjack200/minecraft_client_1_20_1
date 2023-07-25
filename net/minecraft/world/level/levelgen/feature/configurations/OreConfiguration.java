package net.minecraft.world.level.levelgen.feature.configurations;

import com.google.common.collect.ImmutableList;
import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import java.util.List;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.levelgen.structure.templatesystem.RuleTest;

public class OreConfiguration implements FeatureConfiguration {
   public static final Codec<OreConfiguration> CODEC = RecordCodecBuilder.create((recordcodecbuilder_instance) -> recordcodecbuilder_instance.group(Codec.list(OreConfiguration.TargetBlockState.CODEC).fieldOf("targets").forGetter((oreconfiguration2) -> oreconfiguration2.targetStates), Codec.intRange(0, 64).fieldOf("size").forGetter((oreconfiguration1) -> oreconfiguration1.size), Codec.floatRange(0.0F, 1.0F).fieldOf("discard_chance_on_air_exposure").forGetter((oreconfiguration) -> oreconfiguration.discardChanceOnAirExposure)).apply(recordcodecbuilder_instance, OreConfiguration::new));
   public final List<OreConfiguration.TargetBlockState> targetStates;
   public final int size;
   public final float discardChanceOnAirExposure;

   public OreConfiguration(List<OreConfiguration.TargetBlockState> list, int i, float f) {
      this.size = i;
      this.targetStates = list;
      this.discardChanceOnAirExposure = f;
   }

   public OreConfiguration(List<OreConfiguration.TargetBlockState> list, int i) {
      this(list, i, 0.0F);
   }

   public OreConfiguration(RuleTest ruletest, BlockState blockstate, int i, float f) {
      this(ImmutableList.of(new OreConfiguration.TargetBlockState(ruletest, blockstate)), i, f);
   }

   public OreConfiguration(RuleTest ruletest, BlockState blockstate, int i) {
      this(ImmutableList.of(new OreConfiguration.TargetBlockState(ruletest, blockstate)), i, 0.0F);
   }

   public static OreConfiguration.TargetBlockState target(RuleTest ruletest, BlockState blockstate) {
      return new OreConfiguration.TargetBlockState(ruletest, blockstate);
   }

   public static class TargetBlockState {
      public static final Codec<OreConfiguration.TargetBlockState> CODEC = RecordCodecBuilder.create((recordcodecbuilder_instance) -> recordcodecbuilder_instance.group(RuleTest.CODEC.fieldOf("target").forGetter((oreconfiguration_targetblockstate1) -> oreconfiguration_targetblockstate1.target), BlockState.CODEC.fieldOf("state").forGetter((oreconfiguration_targetblockstate) -> oreconfiguration_targetblockstate.state)).apply(recordcodecbuilder_instance, OreConfiguration.TargetBlockState::new));
      public final RuleTest target;
      public final BlockState state;

      TargetBlockState(RuleTest ruletest, BlockState blockstate) {
         this.target = ruletest;
         this.state = blockstate;
      }
   }
}
