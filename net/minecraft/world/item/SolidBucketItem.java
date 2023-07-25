package net.minecraft.world.item;

import javax.annotation.Nullable;
import net.minecraft.core.BlockPos;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.gameevent.GameEvent;
import net.minecraft.world.phys.BlockHitResult;

public class SolidBucketItem extends BlockItem implements DispensibleContainerItem {
   private final SoundEvent placeSound;

   public SolidBucketItem(Block block, SoundEvent soundevent, Item.Properties item_properties) {
      super(block, item_properties);
      this.placeSound = soundevent;
   }

   public InteractionResult useOn(UseOnContext useoncontext) {
      InteractionResult interactionresult = super.useOn(useoncontext);
      Player player = useoncontext.getPlayer();
      if (interactionresult.consumesAction() && player != null && !player.isCreative()) {
         InteractionHand interactionhand = useoncontext.getHand();
         player.setItemInHand(interactionhand, Items.BUCKET.getDefaultInstance());
      }

      return interactionresult;
   }

   public String getDescriptionId() {
      return this.getOrCreateDescriptionId();
   }

   protected SoundEvent getPlaceSound(BlockState blockstate) {
      return this.placeSound;
   }

   public boolean emptyContents(@Nullable Player player, Level level, BlockPos blockpos, @Nullable BlockHitResult blockhitresult) {
      if (level.isInWorldBounds(blockpos) && level.isEmptyBlock(blockpos)) {
         if (!level.isClientSide) {
            level.setBlock(blockpos, this.getBlock().defaultBlockState(), 3);
         }

         level.gameEvent(player, GameEvent.FLUID_PLACE, blockpos);
         level.playSound(player, blockpos, this.placeSound, SoundSource.BLOCKS, 1.0F, 1.0F);
         return true;
      } else {
         return false;
      }
   }
}
