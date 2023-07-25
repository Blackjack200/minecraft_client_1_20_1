package net.minecraft.world.inventory;

import com.mojang.datafixers.util.Pair;
import java.util.Optional;
import javax.annotation.Nullable;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.Container;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;

public class Slot {
   private final int slot;
   public final Container container;
   public int index;
   public final int x;
   public final int y;

   public Slot(Container container, int i, int j, int k) {
      this.container = container;
      this.slot = i;
      this.x = j;
      this.y = k;
   }

   public void onQuickCraft(ItemStack itemstack, ItemStack itemstack1) {
      int i = itemstack1.getCount() - itemstack.getCount();
      if (i > 0) {
         this.onQuickCraft(itemstack1, i);
      }

   }

   protected void onQuickCraft(ItemStack itemstack, int i) {
   }

   protected void onSwapCraft(int i) {
   }

   protected void checkTakeAchievements(ItemStack itemstack) {
   }

   public void onTake(Player player, ItemStack itemstack) {
      this.setChanged();
   }

   public boolean mayPlace(ItemStack itemstack) {
      return true;
   }

   public ItemStack getItem() {
      return this.container.getItem(this.slot);
   }

   public boolean hasItem() {
      return !this.getItem().isEmpty();
   }

   public void setByPlayer(ItemStack itemstack) {
      this.set(itemstack);
   }

   public void set(ItemStack itemstack) {
      this.container.setItem(this.slot, itemstack);
      this.setChanged();
   }

   public void setChanged() {
      this.container.setChanged();
   }

   public int getMaxStackSize() {
      return this.container.getMaxStackSize();
   }

   public int getMaxStackSize(ItemStack itemstack) {
      return Math.min(this.getMaxStackSize(), itemstack.getMaxStackSize());
   }

   @Nullable
   public Pair<ResourceLocation, ResourceLocation> getNoItemIcon() {
      return null;
   }

   public ItemStack remove(int i) {
      return this.container.removeItem(this.slot, i);
   }

   public boolean mayPickup(Player player) {
      return true;
   }

   public boolean isActive() {
      return true;
   }

   public Optional<ItemStack> tryRemove(int i, int j, Player player) {
      if (!this.mayPickup(player)) {
         return Optional.empty();
      } else if (!this.allowModification(player) && j < this.getItem().getCount()) {
         return Optional.empty();
      } else {
         i = Math.min(i, j);
         ItemStack itemstack = this.remove(i);
         if (itemstack.isEmpty()) {
            return Optional.empty();
         } else {
            if (this.getItem().isEmpty()) {
               this.setByPlayer(ItemStack.EMPTY);
            }

            return Optional.of(itemstack);
         }
      }
   }

   public ItemStack safeTake(int i, int j, Player player) {
      Optional<ItemStack> optional = this.tryRemove(i, j, player);
      optional.ifPresent((itemstack) -> this.onTake(player, itemstack));
      return optional.orElse(ItemStack.EMPTY);
   }

   public ItemStack safeInsert(ItemStack itemstack) {
      return this.safeInsert(itemstack, itemstack.getCount());
   }

   public ItemStack safeInsert(ItemStack itemstack, int i) {
      if (!itemstack.isEmpty() && this.mayPlace(itemstack)) {
         ItemStack itemstack1 = this.getItem();
         int j = Math.min(Math.min(i, itemstack.getCount()), this.getMaxStackSize(itemstack) - itemstack1.getCount());
         if (itemstack1.isEmpty()) {
            this.setByPlayer(itemstack.split(j));
         } else if (ItemStack.isSameItemSameTags(itemstack1, itemstack)) {
            itemstack.shrink(j);
            itemstack1.grow(j);
            this.setByPlayer(itemstack1);
         }

         return itemstack;
      } else {
         return itemstack;
      }
   }

   public boolean allowModification(Player player) {
      return this.mayPickup(player) && this.mayPlace(this.getItem());
   }

   public int getContainerSlot() {
      return this.slot;
   }

   public boolean isHighlightable() {
      return true;
   }
}
