package net.minecraft.world.item;

import net.minecraft.core.BlockPos;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;

public class DoubleHighBlockItem extends BlockItem {
   public DoubleHighBlockItem(Block block, Item.Properties item_properties) {
      super(block, item_properties);
   }

   protected boolean placeBlock(BlockPlaceContext blockplacecontext, BlockState blockstate) {
      Level level = blockplacecontext.getLevel();
      BlockPos blockpos = blockplacecontext.getClickedPos().above();
      BlockState blockstate1 = level.isWaterAt(blockpos) ? Blocks.WATER.defaultBlockState() : Blocks.AIR.defaultBlockState();
      level.setBlock(blockpos, blockstate1, 27);
      return super.placeBlock(blockplacecontext, blockstate);
   }
}
