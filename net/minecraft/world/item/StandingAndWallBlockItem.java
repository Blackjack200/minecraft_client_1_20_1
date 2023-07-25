package net.minecraft.world.item;

import java.util.Map;
import javax.annotation.Nullable;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.shapes.CollisionContext;

public class StandingAndWallBlockItem extends BlockItem {
   protected final Block wallBlock;
   private final Direction attachmentDirection;

   public StandingAndWallBlockItem(Block block, Block block1, Item.Properties item_properties, Direction direction) {
      super(block, item_properties);
      this.wallBlock = block1;
      this.attachmentDirection = direction;
   }

   protected boolean canPlace(LevelReader levelreader, BlockState blockstate, BlockPos blockpos) {
      return blockstate.canSurvive(levelreader, blockpos);
   }

   @Nullable
   protected BlockState getPlacementState(BlockPlaceContext blockplacecontext) {
      BlockState blockstate = this.wallBlock.getStateForPlacement(blockplacecontext);
      BlockState blockstate1 = null;
      LevelReader levelreader = blockplacecontext.getLevel();
      BlockPos blockpos = blockplacecontext.getClickedPos();

      for(Direction direction : blockplacecontext.getNearestLookingDirections()) {
         if (direction != this.attachmentDirection.getOpposite()) {
            BlockState blockstate2 = direction == this.attachmentDirection ? this.getBlock().getStateForPlacement(blockplacecontext) : blockstate;
            if (blockstate2 != null && this.canPlace(levelreader, blockstate2, blockpos)) {
               blockstate1 = blockstate2;
               break;
            }
         }
      }

      return blockstate1 != null && levelreader.isUnobstructed(blockstate1, blockpos, CollisionContext.empty()) ? blockstate1 : null;
   }

   public void registerBlocks(Map<Block, Item> map, Item item) {
      super.registerBlocks(map, item);
      map.put(this.wallBlock, item);
   }
}
