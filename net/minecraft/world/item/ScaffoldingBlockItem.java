package net.minecraft.world.item;

import javax.annotation.Nullable;
import net.minecraft.ChatFormatting;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.ScaffoldingBlock;
import net.minecraft.world.level.block.state.BlockState;

public class ScaffoldingBlockItem extends BlockItem {
   public ScaffoldingBlockItem(Block block, Item.Properties item_properties) {
      super(block, item_properties);
   }

   @Nullable
   public BlockPlaceContext updatePlacementContext(BlockPlaceContext blockplacecontext) {
      BlockPos blockpos = blockplacecontext.getClickedPos();
      Level level = blockplacecontext.getLevel();
      BlockState blockstate = level.getBlockState(blockpos);
      Block block = this.getBlock();
      if (!blockstate.is(block)) {
         return ScaffoldingBlock.getDistance(level, blockpos) == 7 ? null : blockplacecontext;
      } else {
         Direction direction;
         if (blockplacecontext.isSecondaryUseActive()) {
            direction = blockplacecontext.isInside() ? blockplacecontext.getClickedFace().getOpposite() : blockplacecontext.getClickedFace();
         } else {
            direction = blockplacecontext.getClickedFace() == Direction.UP ? blockplacecontext.getHorizontalDirection() : Direction.UP;
         }

         int i = 0;
         BlockPos.MutableBlockPos blockpos_mutableblockpos = blockpos.mutable().move(direction);

         while(i < 7) {
            if (!level.isClientSide && !level.isInWorldBounds(blockpos_mutableblockpos)) {
               Player player = blockplacecontext.getPlayer();
               int j = level.getMaxBuildHeight();
               if (player instanceof ServerPlayer && blockpos_mutableblockpos.getY() >= j) {
                  ((ServerPlayer)player).sendSystemMessage(Component.translatable("build.tooHigh", j - 1).withStyle(ChatFormatting.RED), true);
               }
               break;
            }

            blockstate = level.getBlockState(blockpos_mutableblockpos);
            if (!blockstate.is(this.getBlock())) {
               if (blockstate.canBeReplaced(blockplacecontext)) {
                  return BlockPlaceContext.at(blockplacecontext, blockpos_mutableblockpos, direction);
               }
               break;
            }

            blockpos_mutableblockpos.move(direction);
            if (direction.getAxis().isHorizontal()) {
               ++i;
            }
         }

         return null;
      }
   }

   protected boolean mustSurvive() {
      return false;
   }
}
