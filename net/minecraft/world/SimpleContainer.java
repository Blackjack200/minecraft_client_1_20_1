package net.minecraft.world;

import com.google.common.collect.Lists;
import java.util.List;
import java.util.stream.Collectors;
import javax.annotation.Nullable;
import net.minecraft.core.NonNullList;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.player.StackedContents;
import net.minecraft.world.inventory.StackedContentsCompatible;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;

public class SimpleContainer implements Container, StackedContentsCompatible {
   private final int size;
   private final NonNullList<ItemStack> items;
   @Nullable
   private List<ContainerListener> listeners;

   public SimpleContainer(int i) {
      this.size = i;
      this.items = NonNullList.withSize(i, ItemStack.EMPTY);
   }

   public SimpleContainer(ItemStack... aitemstack) {
      this.size = aitemstack.length;
      this.items = NonNullList.of(ItemStack.EMPTY, aitemstack);
   }

   public void addListener(ContainerListener containerlistener) {
      if (this.listeners == null) {
         this.listeners = Lists.newArrayList();
      }

      this.listeners.add(containerlistener);
   }

   public void removeListener(ContainerListener containerlistener) {
      if (this.listeners != null) {
         this.listeners.remove(containerlistener);
      }

   }

   public ItemStack getItem(int i) {
      return i >= 0 && i < this.items.size() ? this.items.get(i) : ItemStack.EMPTY;
   }

   public List<ItemStack> removeAllItems() {
      List<ItemStack> list = this.items.stream().filter((itemstack) -> !itemstack.isEmpty()).collect(Collectors.toList());
      this.clearContent();
      return list;
   }

   public ItemStack removeItem(int i, int j) {
      ItemStack itemstack = ContainerHelper.removeItem(this.items, i, j);
      if (!itemstack.isEmpty()) {
         this.setChanged();
      }

      return itemstack;
   }

   public ItemStack removeItemType(Item item, int i) {
      ItemStack itemstack = new ItemStack(item, 0);

      for(int j = this.size - 1; j >= 0; --j) {
         ItemStack itemstack1 = this.getItem(j);
         if (itemstack1.getItem().equals(item)) {
            int k = i - itemstack.getCount();
            ItemStack itemstack2 = itemstack1.split(k);
            itemstack.grow(itemstack2.getCount());
            if (itemstack.getCount() == i) {
               break;
            }
         }
      }

      if (!itemstack.isEmpty()) {
         this.setChanged();
      }

      return itemstack;
   }

   public ItemStack addItem(ItemStack itemstack) {
      if (itemstack.isEmpty()) {
         return ItemStack.EMPTY;
      } else {
         ItemStack itemstack1 = itemstack.copy();
         this.moveItemToOccupiedSlotsWithSameType(itemstack1);
         if (itemstack1.isEmpty()) {
            return ItemStack.EMPTY;
         } else {
            this.moveItemToEmptySlots(itemstack1);
            return itemstack1.isEmpty() ? ItemStack.EMPTY : itemstack1;
         }
      }
   }

   public boolean canAddItem(ItemStack itemstack) {
      boolean flag = false;

      for(ItemStack itemstack1 : this.items) {
         if (itemstack1.isEmpty() || ItemStack.isSameItemSameTags(itemstack1, itemstack) && itemstack1.getCount() < itemstack1.getMaxStackSize()) {
            flag = true;
            break;
         }
      }

      return flag;
   }

   public ItemStack removeItemNoUpdate(int i) {
      ItemStack itemstack = this.items.get(i);
      if (itemstack.isEmpty()) {
         return ItemStack.EMPTY;
      } else {
         this.items.set(i, ItemStack.EMPTY);
         return itemstack;
      }
   }

   public void setItem(int i, ItemStack itemstack) {
      this.items.set(i, itemstack);
      if (!itemstack.isEmpty() && itemstack.getCount() > this.getMaxStackSize()) {
         itemstack.setCount(this.getMaxStackSize());
      }

      this.setChanged();
   }

   public int getContainerSize() {
      return this.size;
   }

   public boolean isEmpty() {
      for(ItemStack itemstack : this.items) {
         if (!itemstack.isEmpty()) {
            return false;
         }
      }

      return true;
   }

   public void setChanged() {
      if (this.listeners != null) {
         for(ContainerListener containerlistener : this.listeners) {
            containerlistener.containerChanged(this);
         }
      }

   }

   public boolean stillValid(Player player) {
      return true;
   }

   public void clearContent() {
      this.items.clear();
      this.setChanged();
   }

   public void fillStackedContents(StackedContents stackedcontents) {
      for(ItemStack itemstack : this.items) {
         stackedcontents.accountStack(itemstack);
      }

   }

   public String toString() {
      return this.items.stream().filter((itemstack) -> !itemstack.isEmpty()).collect(Collectors.toList()).toString();
   }

   private void moveItemToEmptySlots(ItemStack itemstack) {
      for(int i = 0; i < this.size; ++i) {
         ItemStack itemstack1 = this.getItem(i);
         if (itemstack1.isEmpty()) {
            this.setItem(i, itemstack.copyAndClear());
            return;
         }
      }

   }

   private void moveItemToOccupiedSlotsWithSameType(ItemStack itemstack) {
      for(int i = 0; i < this.size; ++i) {
         ItemStack itemstack1 = this.getItem(i);
         if (ItemStack.isSameItemSameTags(itemstack1, itemstack)) {
            this.moveItemsBetweenStacks(itemstack, itemstack1);
            if (itemstack.isEmpty()) {
               return;
            }
         }
      }

   }

   private void moveItemsBetweenStacks(ItemStack itemstack, ItemStack itemstack1) {
      int i = Math.min(this.getMaxStackSize(), itemstack1.getMaxStackSize());
      int j = Math.min(itemstack.getCount(), i - itemstack1.getCount());
      if (j > 0) {
         itemstack1.grow(j);
         itemstack.shrink(j);
         this.setChanged();
      }

   }

   public void fromTag(ListTag listtag) {
      this.clearContent();

      for(int i = 0; i < listtag.size(); ++i) {
         ItemStack itemstack = ItemStack.of(listtag.getCompound(i));
         if (!itemstack.isEmpty()) {
            this.addItem(itemstack);
         }
      }

   }

   public ListTag createTag() {
      ListTag listtag = new ListTag();

      for(int i = 0; i < this.getContainerSize(); ++i) {
         ItemStack itemstack = this.getItem(i);
         if (!itemstack.isEmpty()) {
            listtag.add(itemstack.save(new CompoundTag()));
         }
      }

      return listtag;
   }
}
