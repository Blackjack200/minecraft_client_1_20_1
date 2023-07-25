package net.minecraft.world.item;

import net.minecraft.sounds.SoundEvents;
import net.minecraft.stats.Stats;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;

public class SpyglassItem extends Item {
   public static final int USE_DURATION = 1200;
   public static final float ZOOM_FOV_MODIFIER = 0.1F;

   public SpyglassItem(Item.Properties item_properties) {
      super(item_properties);
   }

   public int getUseDuration(ItemStack itemstack) {
      return 1200;
   }

   public UseAnim getUseAnimation(ItemStack itemstack) {
      return UseAnim.SPYGLASS;
   }

   public InteractionResultHolder<ItemStack> use(Level level, Player player, InteractionHand interactionhand) {
      player.playSound(SoundEvents.SPYGLASS_USE, 1.0F, 1.0F);
      player.awardStat(Stats.ITEM_USED.get(this));
      return ItemUtils.startUsingInstantly(level, player, interactionhand);
   }

   public ItemStack finishUsingItem(ItemStack itemstack, Level level, LivingEntity livingentity) {
      this.stopUsing(livingentity);
      return itemstack;
   }

   public void releaseUsing(ItemStack itemstack, Level level, LivingEntity livingentity, int i) {
      this.stopUsing(livingentity);
   }

   private void stopUsing(LivingEntity livingentity) {
      livingentity.playSound(SoundEvents.SPYGLASS_STOP_USING, 1.0F, 1.0F);
   }
}
