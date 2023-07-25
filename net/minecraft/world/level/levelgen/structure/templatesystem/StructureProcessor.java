package net.minecraft.world.level.levelgen.structure.templatesystem;

import java.util.List;
import javax.annotation.Nullable;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.level.ServerLevelAccessor;

public abstract class StructureProcessor {
   @Nullable
   public StructureTemplate.StructureBlockInfo processBlock(LevelReader levelreader, BlockPos blockpos, BlockPos blockpos1, StructureTemplate.StructureBlockInfo structuretemplate_structureblockinfo, StructureTemplate.StructureBlockInfo structuretemplate_structureblockinfo1, StructurePlaceSettings structureplacesettings) {
      return structuretemplate_structureblockinfo1;
   }

   protected abstract StructureProcessorType<?> getType();

   public List<StructureTemplate.StructureBlockInfo> finalizeProcessing(ServerLevelAccessor serverlevelaccessor, BlockPos blockpos, BlockPos blockpos1, List<StructureTemplate.StructureBlockInfo> list, List<StructureTemplate.StructureBlockInfo> list1, StructurePlaceSettings structureplacesettings) {
      return list1;
   }
}
