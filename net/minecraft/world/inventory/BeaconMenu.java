package net.minecraft.world.inventory;

import java.util.Optional;
import javax.annotation.Nullable;
import net.minecraft.tags.ItemTags;
import net.minecraft.world.Container;
import net.minecraft.world.SimpleContainer;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Blocks;

public class BeaconMenu extends AbstractContainerMenu {
   private static final int PAYMENT_SLOT = 0;
   private static final int SLOT_COUNT = 1;
   private static final int DATA_COUNT = 3;
   private static final int INV_SLOT_START = 1;
   private static final int INV_SLOT_END = 28;
   private static final int USE_ROW_SLOT_START = 28;
   private static final int USE_ROW_SLOT_END = 37;
   private final Container beacon = new SimpleContainer(1) {
      public boolean canPlaceItem(int i, ItemStack itemstack) {
         return itemstack.is(ItemTags.BEACON_PAYMENT_ITEMS);
      }

      public int getMaxStackSize() {
         return 1;
      }
   };
   private final BeaconMenu.PaymentSlot paymentSlot;
   private final ContainerLevelAccess access;
   private final ContainerData beaconData;

   public BeaconMenu(int i, Container container) {
      this(i, container, new SimpleContainerData(3), ContainerLevelAccess.NULL);
   }

   public BeaconMenu(int i, Container container, ContainerData containerdata, ContainerLevelAccess containerlevelaccess) {
      super(MenuType.BEACON, i);
      checkContainerDataCount(containerdata, 3);
      this.beaconData = containerdata;
      this.access = containerlevelaccess;
      this.paymentSlot = new BeaconMenu.PaymentSlot(this.beacon, 0, 136, 110);
      this.addSlot(this.paymentSlot);
      this.addDataSlots(containerdata);
      int j = 36;
      int k = 137;

      for(int l = 0; l < 3; ++l) {
         for(int i1 = 0; i1 < 9; ++i1) {
            this.addSlot(new Slot(container, i1 + l * 9 + 9, 36 + i1 * 18, 137 + l * 18));
         }
      }

      for(int j1 = 0; j1 < 9; ++j1) {
         this.addSlot(new Slot(container, j1, 36 + j1 * 18, 195));
      }

   }

   public void removed(Player player) {
      super.removed(player);
      if (!player.level().isClientSide) {
         ItemStack itemstack = this.paymentSlot.remove(this.paymentSlot.getMaxStackSize());
         if (!itemstack.isEmpty()) {
            player.drop(itemstack, false);
         }

      }
   }

   public boolean stillValid(Player player) {
      return stillValid(this.access, player, Blocks.BEACON);
   }

   public void setData(int i, int j) {
      super.setData(i, j);
      this.broadcastChanges();
   }

   public ItemStack quickMoveStack(Player player, int i) {
      ItemStack itemstack = ItemStack.EMPTY;
      Slot slot = this.slots.get(i);
      if (slot != null && slot.hasItem()) {
         ItemStack itemstack1 = slot.getItem();
         itemstack = itemstack1.copy();
         if (i == 0) {
            if (!this.moveItemStackTo(itemstack1, 1, 37, true)) {
               return ItemStack.EMPTY;
            }

            slot.onQuickCraft(itemstack1, itemstack);
         } else if (!this.paymentSlot.hasItem() && this.paymentSlot.mayPlace(itemstack1) && itemstack1.getCount() == 1) {
            if (!this.moveItemStackTo(itemstack1, 0, 1, false)) {
               return ItemStack.EMPTY;
            }
         } else if (i >= 1 && i < 28) {
            if (!this.moveItemStackTo(itemstack1, 28, 37, false)) {
               return ItemStack.EMPTY;
            }
         } else if (i >= 28 && i < 37) {
            if (!this.moveItemStackTo(itemstack1, 1, 28, false)) {
               return ItemStack.EMPTY;
            }
         } else if (!this.moveItemStackTo(itemstack1, 1, 37, false)) {
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

   public int getLevels() {
      return this.beaconData.get(0);
   }

   @Nullable
   public MobEffect getPrimaryEffect() {
      return MobEffect.byId(this.beaconData.get(1));
   }

   @Nullable
   public MobEffect getSecondaryEffect() {
      return MobEffect.byId(this.beaconData.get(2));
   }

   public void updateEffects(Optional<MobEffect> optional, Optional<MobEffect> optional1) {
      if (this.paymentSlot.hasItem()) {
         this.beaconData.set(1, optional.map(MobEffect::getId).orElse(-1));
         this.beaconData.set(2, optional1.map(MobEffect::getId).orElse(-1));
         this.paymentSlot.remove(1);
         this.access.execute(Level::blockEntityChanged);
      }

   }

   public boolean hasPayment() {
      return !this.beacon.getItem(0).isEmpty();
   }

   class PaymentSlot extends Slot {
      public PaymentSlot(Container container, int i, int j, int k) {
         super(container, i, j, k);
      }

      public boolean mayPlace(ItemStack itemstack) {
         return itemstack.is(ItemTags.BEACON_PAYMENT_ITEMS);
      }

      public int getMaxStackSize() {
         return 1;
      }
   }
}
