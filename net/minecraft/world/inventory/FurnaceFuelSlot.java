package net.minecraft.world.inventory;

import net.minecraft.world.Container;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;

public class FurnaceFuelSlot extends Slot {
   private final AbstractFurnaceMenu menu;

   public FurnaceFuelSlot(AbstractFurnaceMenu abstractfurnacemenu, Container container, int i, int j, int k) {
      super(container, i, j, k);
      this.menu = abstractfurnacemenu;
   }

   public boolean mayPlace(ItemStack itemstack) {
      return this.menu.isFuel(itemstack) || isBucket(itemstack);
   }

   public int getMaxStackSize(ItemStack itemstack) {
      return isBucket(itemstack) ? 1 : super.getMaxStackSize(itemstack);
   }

   public static boolean isBucket(ItemStack itemstack) {
      return itemstack.is(Items.BUCKET);
   }
}
