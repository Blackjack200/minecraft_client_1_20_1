package net.minecraft.world.item;

import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;

public class BedItem extends BlockItem {
   public BedItem(Block block, Item.Properties item_properties) {
      super(block, item_properties);
   }

   protected boolean placeBlock(BlockPlaceContext blockplacecontext, BlockState blockstate) {
      return blockplacecontext.getLevel().setBlock(blockplacecontext.getClickedPos(), blockstate, 26);
   }
}
