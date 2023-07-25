package net.minecraft.world.level.levelgen.structure.templatesystem;

import com.mojang.serialization.Codec;
import javax.annotation.Nullable;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;

public class LavaSubmergedBlockProcessor extends StructureProcessor {
   public static final Codec<LavaSubmergedBlockProcessor> CODEC = Codec.unit(() -> LavaSubmergedBlockProcessor.INSTANCE);
   public static final LavaSubmergedBlockProcessor INSTANCE = new LavaSubmergedBlockProcessor();

   @Nullable
   public StructureTemplate.StructureBlockInfo processBlock(LevelReader levelreader, BlockPos blockpos, BlockPos blockpos1, StructureTemplate.StructureBlockInfo structuretemplate_structureblockinfo, StructureTemplate.StructureBlockInfo structuretemplate_structureblockinfo1, StructurePlaceSettings structureplacesettings) {
      BlockPos blockpos2 = structuretemplate_structureblockinfo1.pos();
      boolean flag = levelreader.getBlockState(blockpos2).is(Blocks.LAVA);
      return flag && !Block.isShapeFullBlock(structuretemplate_structureblockinfo1.state().getShape(levelreader, blockpos2)) ? new StructureTemplate.StructureBlockInfo(blockpos2, Blocks.LAVA.defaultBlockState(), structuretemplate_structureblockinfo1.nbt()) : structuretemplate_structureblockinfo1;
   }

   protected StructureProcessorType<?> getType() {
      return StructureProcessorType.LAVA_SUBMERGED_BLOCK;
   }
}
