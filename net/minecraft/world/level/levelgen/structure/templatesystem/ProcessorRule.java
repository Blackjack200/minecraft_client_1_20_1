package net.minecraft.world.level.levelgen.structure.templatesystem;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import javax.annotation.Nullable;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.levelgen.structure.templatesystem.rule.blockentity.Passthrough;
import net.minecraft.world.level.levelgen.structure.templatesystem.rule.blockentity.RuleBlockEntityModifier;

public class ProcessorRule {
   public static final Passthrough DEFAULT_BLOCK_ENTITY_MODIFIER = Passthrough.INSTANCE;
   public static final Codec<ProcessorRule> CODEC = RecordCodecBuilder.create((recordcodecbuilder_instance) -> recordcodecbuilder_instance.group(RuleTest.CODEC.fieldOf("input_predicate").forGetter((processorrule4) -> processorrule4.inputPredicate), RuleTest.CODEC.fieldOf("location_predicate").forGetter((processorrule3) -> processorrule3.locPredicate), PosRuleTest.CODEC.optionalFieldOf("position_predicate", PosAlwaysTrueTest.INSTANCE).forGetter((processorrule2) -> processorrule2.posPredicate), BlockState.CODEC.fieldOf("output_state").forGetter((processorrule1) -> processorrule1.outputState), RuleBlockEntityModifier.CODEC.optionalFieldOf("block_entity_modifier", DEFAULT_BLOCK_ENTITY_MODIFIER).forGetter((processorrule) -> processorrule.blockEntityModifier)).apply(recordcodecbuilder_instance, ProcessorRule::new));
   private final RuleTest inputPredicate;
   private final RuleTest locPredicate;
   private final PosRuleTest posPredicate;
   private final BlockState outputState;
   private final RuleBlockEntityModifier blockEntityModifier;

   public ProcessorRule(RuleTest ruletest, RuleTest ruletest1, BlockState blockstate) {
      this(ruletest, ruletest1, PosAlwaysTrueTest.INSTANCE, blockstate);
   }

   public ProcessorRule(RuleTest ruletest, RuleTest ruletest1, PosRuleTest posruletest, BlockState blockstate) {
      this(ruletest, ruletest1, posruletest, blockstate, DEFAULT_BLOCK_ENTITY_MODIFIER);
   }

   public ProcessorRule(RuleTest ruletest, RuleTest ruletest1, PosRuleTest posruletest, BlockState blockstate, RuleBlockEntityModifier ruleblockentitymodifier) {
      this.inputPredicate = ruletest;
      this.locPredicate = ruletest1;
      this.posPredicate = posruletest;
      this.outputState = blockstate;
      this.blockEntityModifier = ruleblockentitymodifier;
   }

   public boolean test(BlockState blockstate, BlockState blockstate1, BlockPos blockpos, BlockPos blockpos1, BlockPos blockpos2, RandomSource randomsource) {
      return this.inputPredicate.test(blockstate, randomsource) && this.locPredicate.test(blockstate1, randomsource) && this.posPredicate.test(blockpos, blockpos1, blockpos2, randomsource);
   }

   public BlockState getOutputState() {
      return this.outputState;
   }

   @Nullable
   public CompoundTag getOutputTag(RandomSource randomsource, @Nullable CompoundTag compoundtag) {
      return this.blockEntityModifier.apply(randomsource, compoundtag);
   }
}
