package net.minecraft.world.level.levelgen.structure.templatesystem;

import com.google.common.collect.Maps;
import com.mojang.serialization.Codec;
import java.util.Map;
import net.minecraft.Util;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.SlabBlock;
import net.minecraft.world.level.block.StairBlock;
import net.minecraft.world.level.block.state.BlockState;

public class BlackstoneReplaceProcessor extends StructureProcessor {
   public static final Codec<BlackstoneReplaceProcessor> CODEC = Codec.unit(() -> BlackstoneReplaceProcessor.INSTANCE);
   public static final BlackstoneReplaceProcessor INSTANCE = new BlackstoneReplaceProcessor();
   private final Map<Block, Block> replacements = Util.make(Maps.newHashMap(), (hashmap) -> {
      hashmap.put(Blocks.COBBLESTONE, Blocks.BLACKSTONE);
      hashmap.put(Blocks.MOSSY_COBBLESTONE, Blocks.BLACKSTONE);
      hashmap.put(Blocks.STONE, Blocks.POLISHED_BLACKSTONE);
      hashmap.put(Blocks.STONE_BRICKS, Blocks.POLISHED_BLACKSTONE_BRICKS);
      hashmap.put(Blocks.MOSSY_STONE_BRICKS, Blocks.POLISHED_BLACKSTONE_BRICKS);
      hashmap.put(Blocks.COBBLESTONE_STAIRS, Blocks.BLACKSTONE_STAIRS);
      hashmap.put(Blocks.MOSSY_COBBLESTONE_STAIRS, Blocks.BLACKSTONE_STAIRS);
      hashmap.put(Blocks.STONE_STAIRS, Blocks.POLISHED_BLACKSTONE_STAIRS);
      hashmap.put(Blocks.STONE_BRICK_STAIRS, Blocks.POLISHED_BLACKSTONE_BRICK_STAIRS);
      hashmap.put(Blocks.MOSSY_STONE_BRICK_STAIRS, Blocks.POLISHED_BLACKSTONE_BRICK_STAIRS);
      hashmap.put(Blocks.COBBLESTONE_SLAB, Blocks.BLACKSTONE_SLAB);
      hashmap.put(Blocks.MOSSY_COBBLESTONE_SLAB, Blocks.BLACKSTONE_SLAB);
      hashmap.put(Blocks.SMOOTH_STONE_SLAB, Blocks.POLISHED_BLACKSTONE_SLAB);
      hashmap.put(Blocks.STONE_SLAB, Blocks.POLISHED_BLACKSTONE_SLAB);
      hashmap.put(Blocks.STONE_BRICK_SLAB, Blocks.POLISHED_BLACKSTONE_BRICK_SLAB);
      hashmap.put(Blocks.MOSSY_STONE_BRICK_SLAB, Blocks.POLISHED_BLACKSTONE_BRICK_SLAB);
      hashmap.put(Blocks.STONE_BRICK_WALL, Blocks.POLISHED_BLACKSTONE_BRICK_WALL);
      hashmap.put(Blocks.MOSSY_STONE_BRICK_WALL, Blocks.POLISHED_BLACKSTONE_BRICK_WALL);
      hashmap.put(Blocks.COBBLESTONE_WALL, Blocks.BLACKSTONE_WALL);
      hashmap.put(Blocks.MOSSY_COBBLESTONE_WALL, Blocks.BLACKSTONE_WALL);
      hashmap.put(Blocks.CHISELED_STONE_BRICKS, Blocks.CHISELED_POLISHED_BLACKSTONE);
      hashmap.put(Blocks.CRACKED_STONE_BRICKS, Blocks.CRACKED_POLISHED_BLACKSTONE_BRICKS);
      hashmap.put(Blocks.IRON_BARS, Blocks.CHAIN);
   });

   private BlackstoneReplaceProcessor() {
   }

   public StructureTemplate.StructureBlockInfo processBlock(LevelReader levelreader, BlockPos blockpos, BlockPos blockpos1, StructureTemplate.StructureBlockInfo structuretemplate_structureblockinfo, StructureTemplate.StructureBlockInfo structuretemplate_structureblockinfo1, StructurePlaceSettings structureplacesettings) {
      Block block = this.replacements.get(structuretemplate_structureblockinfo1.state().getBlock());
      if (block == null) {
         return structuretemplate_structureblockinfo1;
      } else {
         BlockState blockstate = structuretemplate_structureblockinfo1.state();
         BlockState blockstate1 = block.defaultBlockState();
         if (blockstate.hasProperty(StairBlock.FACING)) {
            blockstate1 = blockstate1.setValue(StairBlock.FACING, blockstate.getValue(StairBlock.FACING));
         }

         if (blockstate.hasProperty(StairBlock.HALF)) {
            blockstate1 = blockstate1.setValue(StairBlock.HALF, blockstate.getValue(StairBlock.HALF));
         }

         if (blockstate.hasProperty(SlabBlock.TYPE)) {
            blockstate1 = blockstate1.setValue(SlabBlock.TYPE, blockstate.getValue(SlabBlock.TYPE));
         }

         return new StructureTemplate.StructureBlockInfo(structuretemplate_structureblockinfo1.pos(), blockstate1, structuretemplate_structureblockinfo1.nbt());
      }
   }

   protected StructureProcessorType<?> getType() {
      return StructureProcessorType.BLACKSTONE_REPLACE;
   }
}
