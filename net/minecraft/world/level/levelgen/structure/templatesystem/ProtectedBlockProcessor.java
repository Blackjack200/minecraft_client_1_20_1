package net.minecraft.world.level.levelgen.structure.templatesystem;

import com.mojang.serialization.Codec;
import javax.annotation.Nullable;
import net.minecraft.core.BlockPos;
import net.minecraft.core.registries.Registries;
import net.minecraft.tags.TagKey;
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.levelgen.feature.Feature;

public class ProtectedBlockProcessor extends StructureProcessor {
   public final TagKey<Block> cannotReplace;
   public static final Codec<ProtectedBlockProcessor> CODEC = TagKey.hashedCodec(Registries.BLOCK).xmap(ProtectedBlockProcessor::new, (protectedblockprocessor) -> protectedblockprocessor.cannotReplace);

   public ProtectedBlockProcessor(TagKey<Block> tagkey) {
      this.cannotReplace = tagkey;
   }

   @Nullable
   public StructureTemplate.StructureBlockInfo processBlock(LevelReader levelreader, BlockPos blockpos, BlockPos blockpos1, StructureTemplate.StructureBlockInfo structuretemplate_structureblockinfo, StructureTemplate.StructureBlockInfo structuretemplate_structureblockinfo1, StructurePlaceSettings structureplacesettings) {
      return Feature.isReplaceable(this.cannotReplace).test(levelreader.getBlockState(structuretemplate_structureblockinfo1.pos())) ? structuretemplate_structureblockinfo1 : null;
   }

   protected StructureProcessorType<?> getType() {
      return StructureProcessorType.PROTECTED_BLOCKS;
   }
}
