package net.minecraft.world.entity.npc;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.SimpleContainer;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.item.ItemStack;

public interface InventoryCarrier {
   String TAG_INVENTORY = "Inventory";

   SimpleContainer getInventory();

   static void pickUpItem(Mob mob, InventoryCarrier inventorycarrier, ItemEntity itementity) {
      ItemStack itemstack = itementity.getItem();
      if (mob.wantsToPickUp(itemstack)) {
         SimpleContainer simplecontainer = inventorycarrier.getInventory();
         boolean flag = simplecontainer.canAddItem(itemstack);
         if (!flag) {
            return;
         }

         mob.onItemPickup(itementity);
         int i = itemstack.getCount();
         ItemStack itemstack1 = simplecontainer.addItem(itemstack);
         mob.take(itementity, i - itemstack1.getCount());
         if (itemstack1.isEmpty()) {
            itementity.discard();
         } else {
            itemstack.setCount(itemstack1.getCount());
         }
      }

   }

   default void readInventoryFromTag(CompoundTag compoundtag) {
      if (compoundtag.contains("Inventory", 9)) {
         this.getInventory().fromTag(compoundtag.getList("Inventory", 10));
      }

   }

   default void writeInventoryToTag(CompoundTag compoundtag) {
      compoundtag.put("Inventory", this.getInventory().createTag());
   }
}
