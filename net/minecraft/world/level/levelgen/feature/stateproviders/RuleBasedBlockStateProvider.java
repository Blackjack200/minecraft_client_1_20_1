package net.minecraft.world.level.levelgen.feature.stateproviders;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import java.util.List;
import net.minecraft.core.BlockPos;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.WorldGenLevel;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.levelgen.blockpredicates.BlockPredicate;

public record RuleBasedBlockStateProvider(BlockStateProvider fallback, List<RuleBasedBlockStateProvider.Rule> rules) {
   public static final Codec<RuleBasedBlockStateProvider> CODEC = RecordCodecBuilder.create((recordcodecbuilder_instance) -> recordcodecbuilder_instance.group(BlockStateProvider.CODEC.fieldOf("fallback").forGetter(RuleBasedBlockStateProvider::fallback), RuleBasedBlockStateProvider.Rule.CODEC.listOf().fieldOf("rules").forGetter(RuleBasedBlockStateProvider::rules)).apply(recordcodecbuilder_instance, RuleBasedBlockStateProvider::new));

   public static RuleBasedBlockStateProvider simple(BlockStateProvider blockstateprovider) {
      return new RuleBasedBlockStateProvider(blockstateprovider, List.of());
   }

   public static RuleBasedBlockStateProvider simple(Block block) {
      return simple(BlockStateProvider.simple(block));
   }

   public BlockState getState(WorldGenLevel worldgenlevel, RandomSource randomsource, BlockPos blockpos) {
      for(RuleBasedBlockStateProvider.Rule rulebasedblockstateprovider_rule : this.rules) {
         if (rulebasedblockstateprovider_rule.ifTrue().test(worldgenlevel, blockpos)) {
            return rulebasedblockstateprovider_rule.then().getState(randomsource, blockpos);
         }
      }

      return this.fallback.getState(randomsource, blockpos);
   }

   public static record Rule(BlockPredicate ifTrue, BlockStateProvider then) {
      public static final Codec<RuleBasedBlockStateProvider.Rule> CODEC = RecordCodecBuilder.create((recordcodecbuilder_instance) -> recordcodecbuilder_instance.group(BlockPredicate.CODEC.fieldOf("if_true").forGetter(RuleBasedBlockStateProvider.Rule::ifTrue), BlockStateProvider.CODEC.fieldOf("then").forGetter(RuleBasedBlockStateProvider.Rule::then)).apply(recordcodecbuilder_instance, RuleBasedBlockStateProvider.Rule::new));
   }
}
