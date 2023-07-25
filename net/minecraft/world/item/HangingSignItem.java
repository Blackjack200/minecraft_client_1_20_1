package net.minecraft.world.item;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.WallHangingSignBlock;
import net.minecraft.world.level.block.state.BlockState;

public class HangingSignItem extends SignItem {
   public HangingSignItem(Block block, Block block1, Item.Properties item_properties) {
      super(item_properties, block, block1, Direction.UP);
   }

   protected boolean canPlace(LevelReader levelreader, BlockState blockstate, BlockPos blockpos) {
      Block var5 = blockstate.getBlock();
      if (var5 instanceof WallHangingSignBlock wallhangingsignblock) {
         if (!wallhangingsignblock.canPlace(blockstate, levelreader, blockpos)) {
            return false;
         }
      }

      return super.canPlace(levelreader, blockstate, blockpos);
   }
}
