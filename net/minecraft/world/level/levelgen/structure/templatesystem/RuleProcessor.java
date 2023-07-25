package net.minecraft.world.level.levelgen.structure.templatesystem;

import com.google.common.collect.ImmutableList;
import com.mojang.serialization.Codec;
import java.util.List;
import javax.annotation.Nullable;
import net.minecraft.core.BlockPos;
import net.minecraft.util.Mth;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.level.block.state.BlockState;

public class RuleProcessor extends StructureProcessor {
   public static final Codec<RuleProcessor> CODEC = ProcessorRule.CODEC.listOf().fieldOf("rules").xmap(RuleProcessor::new, (ruleprocessor) -> ruleprocessor.rules).codec();
   private final ImmutableList<ProcessorRule> rules;

   public RuleProcessor(List<? extends ProcessorRule> list) {
      this.rules = ImmutableList.copyOf(list);
   }

   @Nullable
   public StructureTemplate.StructureBlockInfo processBlock(LevelReader levelreader, BlockPos blockpos, BlockPos blockpos1, StructureTemplate.StructureBlockInfo structuretemplate_structureblockinfo, StructureTemplate.StructureBlockInfo structuretemplate_structureblockinfo1, StructurePlaceSettings structureplacesettings) {
      RandomSource randomsource = RandomSource.create(Mth.getSeed(structuretemplate_structureblockinfo1.pos()));
      BlockState blockstate = levelreader.getBlockState(structuretemplate_structureblockinfo1.pos());

      for(ProcessorRule processorrule : this.rules) {
         if (processorrule.test(structuretemplate_structureblockinfo1.state(), blockstate, structuretemplate_structureblockinfo.pos(), structuretemplate_structureblockinfo1.pos(), blockpos1, randomsource)) {
            return new StructureTemplate.StructureBlockInfo(structuretemplate_structureblockinfo1.pos(), processorrule.getOutputState(), processorrule.getOutputTag(randomsource, structuretemplate_structureblockinfo1.nbt()));
         }
      }

      return structuretemplate_structureblockinfo1;
   }

   protected StructureProcessorType<?> getType() {
      return StructureProcessorType.RULE;
   }
}
