package net.minecraft.world.item.context;

import javax.annotation.Nullable;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.Vec3;

public class BlockPlaceContext extends UseOnContext {
   private final BlockPos relativePos;
   protected boolean replaceClicked = true;

   public BlockPlaceContext(Player player, InteractionHand interactionhand, ItemStack itemstack, BlockHitResult blockhitresult) {
      this(player.level(), player, interactionhand, itemstack, blockhitresult);
   }

   public BlockPlaceContext(UseOnContext useoncontext) {
      this(useoncontext.getLevel(), useoncontext.getPlayer(), useoncontext.getHand(), useoncontext.getItemInHand(), useoncontext.getHitResult());
   }

   protected BlockPlaceContext(Level level, @Nullable Player player, InteractionHand interactionhand, ItemStack itemstack, BlockHitResult blockhitresult) {
      super(level, player, interactionhand, itemstack, blockhitresult);
      this.relativePos = blockhitresult.getBlockPos().relative(blockhitresult.getDirection());
      this.replaceClicked = level.getBlockState(blockhitresult.getBlockPos()).canBeReplaced(this);
   }

   public static BlockPlaceContext at(BlockPlaceContext blockplacecontext, BlockPos blockpos, Direction direction) {
      return new BlockPlaceContext(blockplacecontext.getLevel(), blockplacecontext.getPlayer(), blockplacecontext.getHand(), blockplacecontext.getItemInHand(), new BlockHitResult(new Vec3((double)blockpos.getX() + 0.5D + (double)direction.getStepX() * 0.5D, (double)blockpos.getY() + 0.5D + (double)direction.getStepY() * 0.5D, (double)blockpos.getZ() + 0.5D + (double)direction.getStepZ() * 0.5D), direction, blockpos, false));
   }

   public BlockPos getClickedPos() {
      return this.replaceClicked ? super.getClickedPos() : this.relativePos;
   }

   public boolean canPlace() {
      return this.replaceClicked || this.getLevel().getBlockState(this.getClickedPos()).canBeReplaced(this);
   }

   public boolean replacingClickedOnBlock() {
      return this.replaceClicked;
   }

   public Direction getNearestLookingDirection() {
      return Direction.orderedByNearest(this.getPlayer())[0];
   }

   public Direction getNearestLookingVerticalDirection() {
      return Direction.getFacingAxis(this.getPlayer(), Direction.Axis.Y);
   }

   public Direction[] getNearestLookingDirections() {
      Direction[] adirection = Direction.orderedByNearest(this.getPlayer());
      if (this.replaceClicked) {
         return adirection;
      } else {
         Direction direction = this.getClickedFace();

         int i;
         for(i = 0; i < adirection.length && adirection[i] != direction.getOpposite(); ++i) {
         }

         if (i > 0) {
            System.arraycopy(adirection, 0, adirection, 1, i);
            adirection[0] = direction.getOpposite();
         }

         return adirection;
      }
   }
}
