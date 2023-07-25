package net.minecraft.world.level.levelgen.feature;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.levelgen.blockpredicates.BlockPredicate;
import net.minecraft.world.level.levelgen.feature.configurations.FeatureConfiguration;

public class HugeFungusConfiguration implements FeatureConfiguration {
   public static final Codec<HugeFungusConfiguration> CODEC = RecordCodecBuilder.create((recordcodecbuilder_instance) -> recordcodecbuilder_instance.group(BlockState.CODEC.fieldOf("valid_base_block").forGetter((hugefungusconfiguration5) -> hugefungusconfiguration5.validBaseState), BlockState.CODEC.fieldOf("stem_state").forGetter((hugefungusconfiguration4) -> hugefungusconfiguration4.stemState), BlockState.CODEC.fieldOf("hat_state").forGetter((hugefungusconfiguration3) -> hugefungusconfiguration3.hatState), BlockState.CODEC.fieldOf("decor_state").forGetter((hugefungusconfiguration2) -> hugefungusconfiguration2.decorState), BlockPredicate.CODEC.fieldOf("replaceable_blocks").forGetter((hugefungusconfiguration1) -> hugefungusconfiguration1.replaceableBlocks), Codec.BOOL.fieldOf("planted").orElse(false).forGetter((hugefungusconfiguration) -> hugefungusconfiguration.planted)).apply(recordcodecbuilder_instance, HugeFungusConfiguration::new));
   public final BlockState validBaseState;
   public final BlockState stemState;
   public final BlockState hatState;
   public final BlockState decorState;
   public final BlockPredicate replaceableBlocks;
   public final boolean planted;

   public HugeFungusConfiguration(BlockState blockstate, BlockState blockstate1, BlockState blockstate2, BlockState blockstate3, BlockPredicate blockpredicate, boolean flag) {
      this.validBaseState = blockstate;
      this.stemState = blockstate1;
      this.hatState = blockstate2;
      this.decorState = blockstate3;
      this.replaceableBlocks = blockpredicate;
      this.planted = flag;
   }
}
