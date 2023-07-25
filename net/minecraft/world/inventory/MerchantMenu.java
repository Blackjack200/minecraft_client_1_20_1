package net.minecraft.world.inventory;

import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.Container;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.npc.ClientSideMerchant;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.trading.Merchant;
import net.minecraft.world.item.trading.MerchantOffers;

public class MerchantMenu extends AbstractContainerMenu {
   protected static final int PAYMENT1_SLOT = 0;
   protected static final int PAYMENT2_SLOT = 1;
   protected static final int RESULT_SLOT = 2;
   private static final int INV_SLOT_START = 3;
   private static final int INV_SLOT_END = 30;
   private static final int USE_ROW_SLOT_START = 30;
   private static final int USE_ROW_SLOT_END = 39;
   private static final int SELLSLOT1_X = 136;
   private static final int SELLSLOT2_X = 162;
   private static final int BUYSLOT_X = 220;
   private static final int ROW_Y = 37;
   private final Merchant trader;
   private final MerchantContainer tradeContainer;
   private int merchantLevel;
   private boolean showProgressBar;
   private boolean canRestock;

   public MerchantMenu(int i, Inventory inventory) {
      this(i, inventory, new ClientSideMerchant(inventory.player));
   }

   public MerchantMenu(int i, Inventory inventory, Merchant merchant) {
      super(MenuType.MERCHANT, i);
      this.trader = merchant;
      this.tradeContainer = new MerchantContainer(merchant);
      this.addSlot(new Slot(this.tradeContainer, 0, 136, 37));
      this.addSlot(new Slot(this.tradeContainer, 1, 162, 37));
      this.addSlot(new MerchantResultSlot(inventory.player, merchant, this.tradeContainer, 2, 220, 37));

      for(int j = 0; j < 3; ++j) {
         for(int k = 0; k < 9; ++k) {
            this.addSlot(new Slot(inventory, k + j * 9 + 9, 108 + k * 18, 84 + j * 18));
         }
      }

      for(int l = 0; l < 9; ++l) {
         this.addSlot(new Slot(inventory, l, 108 + l * 18, 142));
      }

   }

   public void setShowProgressBar(boolean flag) {
      this.showProgressBar = flag;
   }

   public void slotsChanged(Container container) {
      this.tradeContainer.updateSellItem();
      super.slotsChanged(container);
   }

   public void setSelectionHint(int i) {
      this.tradeContainer.setSelectionHint(i);
   }

   public boolean stillValid(Player player) {
      return this.trader.getTradingPlayer() == player;
   }

   public int getTraderXp() {
      return this.trader.getVillagerXp();
   }

   public int getFutureTraderXp() {
      return this.tradeContainer.getFutureXp();
   }

   public void setXp(int i) {
      this.trader.overrideXp(i);
   }

   public int getTraderLevel() {
      return this.merchantLevel;
   }

   public void setMerchantLevel(int i) {
      this.merchantLevel = i;
   }

   public void setCanRestock(boolean flag) {
      this.canRestock = flag;
   }

   public boolean canRestock() {
      return this.canRestock;
   }

   public boolean canTakeItemForPickAll(ItemStack itemstack, Slot slot) {
      return false;
   }

   public ItemStack quickMoveStack(Player player, int i) {
      ItemStack itemstack = ItemStack.EMPTY;
      Slot slot = this.slots.get(i);
      if (slot != null && slot.hasItem()) {
         ItemStack itemstack1 = slot.getItem();
         itemstack = itemstack1.copy();
         if (i == 2) {
            if (!this.moveItemStackTo(itemstack1, 3, 39, true)) {
               return ItemStack.EMPTY;
            }

            slot.onQuickCraft(itemstack1, itemstack);
            this.playTradeSound();
         } else if (i != 0 && i != 1) {
            if (i >= 3 && i < 30) {
               if (!this.moveItemStackTo(itemstack1, 30, 39, false)) {
                  return ItemStack.EMPTY;
               }
            } else if (i >= 30 && i < 39 && !this.moveItemStackTo(itemstack1, 3, 30, false)) {
               return ItemStack.EMPTY;
            }
         } else if (!this.moveItemStackTo(itemstack1, 3, 39, false)) {
            return ItemStack.EMPTY;
         }

         if (itemstack1.isEmpty()) {
            slot.setByPlayer(ItemStack.EMPTY);
         } else {
            slot.setChanged();
         }

         if (itemstack1.getCount() == itemstack.getCount()) {
            return ItemStack.EMPTY;
         }

         slot.onTake(player, itemstack1);
      }

      return itemstack;
   }

   private void playTradeSound() {
      if (!this.trader.isClientSide()) {
         Entity entity = (Entity)this.trader;
         entity.level().playLocalSound(entity.getX(), entity.getY(), entity.getZ(), this.trader.getNotifyTradeSound(), SoundSource.NEUTRAL, 1.0F, 1.0F, false);
      }

   }

   public void removed(Player player) {
      super.removed(player);
      this.trader.setTradingPlayer((Player)null);
      if (!this.trader.isClientSide()) {
         if (!player.isAlive() || player instanceof ServerPlayer && ((ServerPlayer)player).hasDisconnected()) {
            ItemStack itemstack = this.tradeContainer.removeItemNoUpdate(0);
            if (!itemstack.isEmpty()) {
               player.drop(itemstack, false);
            }

            itemstack = this.tradeContainer.removeItemNoUpdate(1);
            if (!itemstack.isEmpty()) {
               player.drop(itemstack, false);
            }
         } else if (player instanceof ServerPlayer) {
            player.getInventory().placeItemBackInInventory(this.tradeContainer.removeItemNoUpdate(0));
            player.getInventory().placeItemBackInInventory(this.tradeContainer.removeItemNoUpdate(1));
         }

      }
   }

   public void tryMoveItems(int i) {
      if (i >= 0 && this.getOffers().size() > i) {
         ItemStack itemstack = this.tradeContainer.getItem(0);
         if (!itemstack.isEmpty()) {
            if (!this.moveItemStackTo(itemstack, 3, 39, true)) {
               return;
            }

            this.tradeContainer.setItem(0, itemstack);
         }

         ItemStack itemstack1 = this.tradeContainer.getItem(1);
         if (!itemstack1.isEmpty()) {
            if (!this.moveItemStackTo(itemstack1, 3, 39, true)) {
               return;
            }

            this.tradeContainer.setItem(1, itemstack1);
         }

         if (this.tradeContainer.getItem(0).isEmpty() && this.tradeContainer.getItem(1).isEmpty()) {
            ItemStack itemstack2 = this.getOffers().get(i).getCostA();
            this.moveFromInventoryToPaymentSlot(0, itemstack2);
            ItemStack itemstack3 = this.getOffers().get(i).getCostB();
            this.moveFromInventoryToPaymentSlot(1, itemstack3);
         }

      }
   }

   private void moveFromInventoryToPaymentSlot(int i, ItemStack itemstack) {
      if (!itemstack.isEmpty()) {
         for(int j = 3; j < 39; ++j) {
            ItemStack itemstack1 = this.slots.get(j).getItem();
            if (!itemstack1.isEmpty() && ItemStack.isSameItemSameTags(itemstack, itemstack1)) {
               ItemStack itemstack2 = this.tradeContainer.getItem(i);
               int k = itemstack2.isEmpty() ? 0 : itemstack2.getCount();
               int l = Math.min(itemstack.getMaxStackSize() - k, itemstack1.getCount());
               ItemStack itemstack3 = itemstack1.copy();
               int i1 = k + l;
               itemstack1.shrink(l);
               itemstack3.setCount(i1);
               this.tradeContainer.setItem(i, itemstack3);
               if (i1 >= itemstack.getMaxStackSize()) {
                  break;
               }
            }
         }
      }

   }

   public void setOffers(MerchantOffers merchantoffers) {
      this.trader.overrideOffers(merchantoffers);
   }

   public MerchantOffers getOffers() {
      return this.trader.getOffers();
   }

   public boolean showProgressBar() {
      return this.showProgressBar;
   }
}
