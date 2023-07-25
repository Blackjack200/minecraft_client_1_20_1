package net.minecraft.world;

import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;

public class CompoundContainer implements Container {
   private final Container container1;
   private final Container container2;

   public CompoundContainer(Container container, Container container1) {
      this.container1 = container;
      this.container2 = container1;
   }

   public int getContainerSize() {
      return this.container1.getContainerSize() + this.container2.getContainerSize();
   }

   public boolean isEmpty() {
      return this.container1.isEmpty() && this.container2.isEmpty();
   }

   public boolean contains(Container container) {
      return this.container1 == container || this.container2 == container;
   }

   public ItemStack getItem(int i) {
      return i >= this.container1.getContainerSize() ? this.container2.getItem(i - this.container1.getContainerSize()) : this.container1.getItem(i);
   }

   public ItemStack removeItem(int i, int j) {
      return i >= this.container1.getContainerSize() ? this.container2.removeItem(i - this.container1.getContainerSize(), j) : this.container1.removeItem(i, j);
   }

   public ItemStack removeItemNoUpdate(int i) {
      return i >= this.container1.getContainerSize() ? this.container2.removeItemNoUpdate(i - this.container1.getContainerSize()) : this.container1.removeItemNoUpdate(i);
   }

   public void setItem(int i, ItemStack itemstack) {
      if (i >= this.container1.getContainerSize()) {
         this.container2.setItem(i - this.container1.getContainerSize(), itemstack);
      } else {
         this.container1.setItem(i, itemstack);
      }

   }

   public int getMaxStackSize() {
      return this.container1.getMaxStackSize();
   }

   public void setChanged() {
      this.container1.setChanged();
      this.container2.setChanged();
   }

   public boolean stillValid(Player player) {
      return this.container1.stillValid(player) && this.container2.stillValid(player);
   }

   public void startOpen(Player player) {
      this.container1.startOpen(player);
      this.container2.startOpen(player);
   }

   public void stopOpen(Player player) {
      this.container1.stopOpen(player);
      this.container2.stopOpen(player);
   }

   public boolean canPlaceItem(int i, ItemStack itemstack) {
      return i >= this.container1.getContainerSize() ? this.container2.canPlaceItem(i - this.container1.getContainerSize(), itemstack) : this.container1.canPlaceItem(i, itemstack);
   }

   public void clearContent() {
      this.container1.clearContent();
      this.container2.clearContent();
   }
}
