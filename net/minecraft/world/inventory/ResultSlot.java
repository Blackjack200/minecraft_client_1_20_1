package net.minecraft.world.inventory;

import net.minecraft.core.NonNullList;
import net.minecraft.world.Container;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.RecipeType;

public class ResultSlot extends Slot {
   private final CraftingContainer craftSlots;
   private final Player player;
   private int removeCount;

   public ResultSlot(Player player, CraftingContainer craftingcontainer, Container container, int i, int j, int k) {
      super(container, i, j, k);
      this.player = player;
      this.craftSlots = craftingcontainer;
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

   protected void onSwapCraft(int i) {
      this.removeCount += i;
   }

   protected void checkTakeAchievements(ItemStack itemstack) {
      if (this.removeCount > 0) {
         itemstack.onCraftedBy(this.player.level(), this.player, this.removeCount);
      }

      Container var3 = this.container;
      if (var3 instanceof RecipeHolder recipeholder) {
         recipeholder.awardUsedRecipes(this.player, this.craftSlots.getItems());
      }

      this.removeCount = 0;
   }

   public void onTake(Player player, ItemStack itemstack) {
      this.checkTakeAchievements(itemstack);
      NonNullList<ItemStack> nonnulllist = player.level().getRecipeManager().getRemainingItemsFor(RecipeType.CRAFTING, this.craftSlots, player.level());

      for(int i = 0; i < nonnulllist.size(); ++i) {
         ItemStack itemstack1 = this.craftSlots.getItem(i);
         ItemStack itemstack2 = nonnulllist.get(i);
         if (!itemstack1.isEmpty()) {
            this.craftSlots.removeItem(i, 1);
            itemstack1 = this.craftSlots.getItem(i);
         }

         if (!itemstack2.isEmpty()) {
            if (itemstack1.isEmpty()) {
               this.craftSlots.setItem(i, itemstack2);
            } else if (ItemStack.isSameItemSameTags(itemstack1, itemstack2)) {
               itemstack2.grow(itemstack1.getCount());
               this.craftSlots.setItem(i, itemstack2);
            } else if (!this.player.getInventory().add(itemstack2)) {
               this.player.drop(itemstack2, false);
            }
         }
      }

   }
}
