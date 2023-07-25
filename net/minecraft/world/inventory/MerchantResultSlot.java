package net.minecraft.world.inventory;

import net.minecraft.stats.Stats;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.trading.Merchant;
import net.minecraft.world.item.trading.MerchantOffer;

public class MerchantResultSlot extends Slot {
   private final MerchantContainer slots;
   private final Player player;
   private int removeCount;
   private final Merchant merchant;

   public MerchantResultSlot(Player player, Merchant merchant, MerchantContainer merchantcontainer, int i, int j, int k) {
      super(merchantcontainer, i, j, k);
      this.player = player;
      this.merchant = merchant;
      this.slots = merchantcontainer;
   }

   public boolean mayPlace(ItemStack itemstack) {
      return false;
   }

   public ItemStack remove(int i) {
      if (this.hasItem()) {
         this.removeCount += Math.min(i, this.getItem().getCount());
      }

      return super.remove(i);
   }

   protected void onQuickCraft(ItemStack itemstack, int i) {
      this.removeCount += i;
      this.checkTakeAchievements(itemstack);
   }

   protected void checkTakeAchievements(ItemStack itemstack) {
      itemstack.onCraftedBy(this.player.level(), this.player, this.removeCount);
      this.removeCount = 0;
   }

   public void onTake(Player player, ItemStack itemstack) {
      this.checkTakeAchievements(itemstack);
      MerchantOffer merchantoffer = this.slots.getActiveOffer();
      if (merchantoffer != null) {
         ItemStack itemstack1 = this.slots.getItem(0);
         ItemStack itemstack2 = this.slots.getItem(1);
         if (merchantoffer.take(itemstack1, itemstack2) || merchantoffer.take(itemstack2, itemstack1)) {
            this.merchant.notifyTrade(merchantoffer);
            player.awardStat(Stats.TRADED_WITH_VILLAGER);
            this.slots.setItem(0, itemstack1);
            this.slots.setItem(1, itemstack2);
         }

         this.merchant.overrideXp(this.merchant.getVillagerXp() + merchantoffer.getXp());
      }

   }
}
