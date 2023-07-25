package net.minecraft.world.entity;

import java.util.function.Predicate;
import net.minecraft.world.Container;
import net.minecraft.world.item.ItemStack;

public interface SlotAccess {
   SlotAccess NULL = new SlotAccess() {
      public ItemStack get() {
         return ItemStack.EMPTY;
      }

      public boolean set(ItemStack itemstack) {
         return false;
      }
   };

   static SlotAccess forContainer(final Container container, final int i, final Predicate<ItemStack> predicate) {
      return new SlotAccess() {
         public ItemStack get() {
            return container.getItem(i);
         }

         public boolean set(ItemStack itemstack) {
            if (!predicate.test(itemstack)) {
               return false;
            } else {
               container.setItem(i, itemstack);
               return true;
            }
         }
      };
   }

   static SlotAccess forContainer(Container container, int i) {
      return forContainer(container, i, (itemstack) -> true);
   }

   static SlotAccess forEquipmentSlot(final LivingEntity livingentity, final EquipmentSlot equipmentslot, final Predicate<ItemStack> predicate) {
      return new SlotAccess() {
         public ItemStack get() {
            return livingentity.getItemBySlot(equipmentslot);
         }

         public boolean set(ItemStack itemstack) {
            if (!predicate.test(itemstack)) {
               return false;
            } else {
               livingentity.setItemSlot(equipmentslot, itemstack);
               return true;
            }
         }
      };
   }

   static SlotAccess forEquipmentSlot(LivingEntity livingentity, EquipmentSlot equipmentslot) {
      return forEquipmentSlot(livingentity, equipmentslot, (itemstack) -> true);
   }

   ItemStack get();

   boolean set(ItemStack itemstack);
}
