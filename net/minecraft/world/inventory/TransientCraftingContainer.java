package net.minecraft.world.inventory;

import java.util.List;
import net.minecraft.core.NonNullList;
import net.minecraft.world.ContainerHelper;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.player.StackedContents;
import net.minecraft.world.item.ItemStack;

public class TransientCraftingContainer implements CraftingContainer {
   private final NonNullList<ItemStack> items;
   private final int width;
   private final int height;
   private final AbstractContainerMenu menu;

   public TransientCraftingContainer(AbstractContainerMenu abstractcontainermenu, int i, int j) {
      this(abstractcontainermenu, i, j, NonNullList.withSize(i * j, ItemStack.EMPTY));
   }

   public TransientCraftingContainer(AbstractContainerMenu abstractcontainermenu, int i, int j, NonNullList<ItemStack> nonnulllist) {
      this.items = nonnulllist;
      this.menu = abstractcontainermenu;
      this.width = i;
      this.height = j;
   }

   public int getContainerSize() {
      return this.items.size();
   }

   public boolean isEmpty() {
      for(ItemStack itemstack : this.items) {
         if (!itemstack.isEmpty()) {
            return false;
         }
      }

      return true;
   }

   public ItemStack getItem(int i) {
      return i >= this.getContainerSize() ? ItemStack.EMPTY : this.items.get(i);
   }

   public ItemStack removeItemNoUpdate(int i) {
      return ContainerHelper.takeItem(this.items, i);
   }

   public ItemStack removeItem(int i, int j) {
      ItemStack itemstack = ContainerHelper.removeItem(this.items, i, j);
      if (!itemstack.isEmpty()) {
         this.menu.slotsChanged(this);
      }

      return itemstack;
   }

   public void setItem(int i, ItemStack itemstack) {
      this.items.set(i, itemstack);
      this.menu.slotsChanged(this);
   }

   public void setChanged() {
   }

   public boolean stillValid(Player player) {
      return true;
   }

   public void clearContent() {
      this.items.clear();
   }

   public int getHeight() {
      return this.height;
   }

   public int getWidth() {
      return this.width;
   }

   public List<ItemStack> getItems() {
      return List.copyOf(this.items);
   }

   public void fillStackedContents(StackedContents stackedcontents) {
      for(ItemStack itemstack : this.items) {
         stackedcontents.accountSimpleStack(itemstack);
      }

   }
}
