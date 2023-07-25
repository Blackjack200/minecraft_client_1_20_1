package net.minecraft.world.item;

import javax.annotation.Nullable;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;

public class GameMasterBlockItem extends BlockItem {
   public GameMasterBlockItem(Block block, Item.Properties item_properties) {
      super(block, item_properties);
   }

   @Nullable
   protected BlockState getPlacementState(BlockPlaceContext blockplacecontext) {
      Player player = blockplacecontext.getPlayer();
      return player != null && !player.canUseGameMasterBlocks() ? null : super.getPlacementState(blockplacecontext);
   }
}
