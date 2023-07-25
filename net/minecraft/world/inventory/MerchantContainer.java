package net.minecraft.world.inventory;

import javax.annotation.Nullable;
import net.minecraft.core.NonNullList;
import net.minecraft.world.Container;
import net.minecraft.world.ContainerHelper;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.trading.Merchant;
import net.minecraft.world.item.trading.MerchantOffer;
import net.minecraft.world.item.trading.MerchantOffers;

public class MerchantContainer implements Container {
   private final Merchant merchant;
   private final NonNullList<ItemStack> itemStacks = NonNullList.withSize(3, ItemStack.EMPTY);
   @Nullable
   private MerchantOffer activeOffer;
   private int selectionHint;
   private int futureXp;

   public MerchantContainer(Merchant merchant) {
      this.merchant = merchant;
   }

   public int getContainerSize() {
      return this.itemStacks.size();
   }

   public boolean isEmpty() {
      for(ItemStack itemstack : this.itemStacks) {
         if (!itemstack.isEmpty()) {
            return false;
         }
      }

      return true;
   }

   public ItemStack getItem(int i) {
      return this.itemStacks.get(i);
   }

   public ItemStack removeItem(int i, int j) {
      ItemStack itemstack = this.itemStacks.get(i);
      if (i == 2 && !itemstack.isEmpty()) {
         return ContainerHelper.removeItem(this.itemStacks, i, itemstack.getCount());
      } else {
         ItemStack itemstack1 = ContainerHelper.removeItem(this.itemStacks, i, j);
         if (!itemstack1.isEmpty() && this.isPaymentSlot(i)) {
            this.updateSellItem();
         }

         return itemstack1;
      }
   }

   private boolean isPaymentSlot(int i) {
      return i == 0 || i == 1;
   }

   public ItemStack removeItemNoUpdate(int i) {
      return ContainerHelper.takeItem(this.itemStacks, i);
   }

   public void setItem(int i, ItemStack itemstack) {
      this.itemStacks.set(i, itemstack);
      if (!itemstack.isEmpty() && itemstack.getCount() > this.getMaxStackSize()) {
         itemstack.setCount(this.getMaxStackSize());
      }

      if (this.isPaymentSlot(i)) {
         this.updateSellItem();
      }

   }

   public boolean stillValid(Player player) {
      return this.merchant.getTradingPlayer() == player;
   }

   public void setChanged() {
      this.updateSellItem();
   }

   public void updateSellItem() {
      this.activeOffer = null;
      ItemStack itemstack;
      ItemStack itemstack1;
      if (this.itemStacks.get(0).isEmpty()) {
         itemstack = this.itemStacks.get(1);
         itemstack1 = ItemStack.EMPTY;
      } else {
         itemstack = this.itemStacks.get(0);
         itemstack1 = this.itemStacks.get(1);
      }

      if (itemstack.isEmpty()) {
         this.setItem(2, ItemStack.EMPTY);
         this.futureXp = 0;
      } else {
         MerchantOffers merchantoffers = this.merchant.getOffers();
         if (!merchantoffers.isEmpty()) {
            MerchantOffer merchantoffer = merchantoffers.getRecipeFor(itemstack, itemstack1, this.selectionHint);
            if (merchantoffer == null || merchantoffer.isOutOfStock()) {
               this.activeOffer = merchantoffer;
               merchantoffer = merchantoffers.getRecipeFor(itemstack1, itemstack, this.selectionHint);
            }

            if (merchantoffer != null && !merchantoffer.isOutOfStock()) {
               this.activeOffer = merchantoffer;
               this.setItem(2, merchantoffer.assemble());
               this.futureXp = merchantoffer.getXp();
            } else {
               this.setItem(2, ItemStack.EMPTY);
               this.futureXp = 0;
            }
         }

         this.merchant.notifyTradeUpdated(this.getItem(2));
      }
   }

   @Nullable
   public MerchantOffer getActiveOffer() {
      return this.activeOffer;
   }

   public void setSelectionHint(int i) {
      this.selectionHint = i;
      this.updateSellItem();
   }

   public void clearContent() {
      this.itemStacks.clear();
   }

   public int getFutureXp() {
      return this.futureXp;
   }
}
