package net.minecraft.world.level.levelgen.structure.templatesystem;

import com.google.common.collect.ImmutableList;
import com.mojang.serialization.Codec;
import java.util.List;
import javax.annotation.Nullable;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.BlockState;

public class BlockIgnoreProcessor extends StructureProcessor {
   public static final Codec<BlockIgnoreProcessor> CODEC = BlockState.CODEC.xmap(BlockBehaviour.BlockStateBase::getBlock, Block::defaultBlockState).listOf().fieldOf("blocks").xmap(BlockIgnoreProcessor::new, (blockignoreprocessor) -> blockignoreprocessor.toIgnore).codec();
   public static final BlockIgnoreProcessor STRUCTURE_BLOCK = new BlockIgnoreProcessor(ImmutableList.of(Blocks.STRUCTURE_BLOCK));
   public static final BlockIgnoreProcessor AIR = new BlockIgnoreProcessor(ImmutableList.of(Blocks.AIR));
   public static final BlockIgnoreProcessor STRUCTURE_AND_AIR = new BlockIgnoreProcessor(ImmutableList.of(Blocks.AIR, Blocks.STRUCTURE_BLOCK));
   private final ImmutableList<Block> toIgnore;

   public BlockIgnoreProcessor(List<Block> list) {
      this.toIgnore = ImmutableList.copyOf(list);
   }

   @Nullable
   public StructureTemplate.StructureBlockInfo processBlock(LevelReader levelreader, BlockPos blockpos, BlockPos blockpos1, StructureTemplate.StructureBlockInfo structuretemplate_structureblockinfo, StructureTemplate.StructureBlockInfo structuretemplate_structureblockinfo1, StructurePlaceSettings structureplacesettings) {
      return this.toIgnore.contains(structuretemplate_structureblockinfo1.state().getBlock()) ? null : structuretemplate_structureblockinfo1;
   }

   protected StructureProcessorType<?> getType() {
      return StructureProcessorType.BLOCK_IGNORE;
   }
}
