package net.minecraft.world;

import java.util.List;
import java.util.function.Predicate;
import net.minecraft.core.NonNullList;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.world.item.ItemStack;

public class ContainerHelper {
   public static ItemStack removeItem(List<ItemStack> list, int i, int j) {
      return i >= 0 && i < list.size() && !list.get(i).isEmpty() && j > 0 ? list.get(i).split(j) : ItemStack.EMPTY;
   }

   public static ItemStack takeItem(List<ItemStack> list, int i) {
      return i >= 0 && i < list.size() ? list.set(i, ItemStack.EMPTY) : ItemStack.EMPTY;
   }

   public static CompoundTag saveAllItems(CompoundTag compoundtag, NonNullList<ItemStack> nonnulllist) {
      return saveAllItems(compoundtag, nonnulllist, true);
   }

   public static CompoundTag saveAllItems(CompoundTag compoundtag, NonNullList<ItemStack> nonnulllist, boolean flag) {
      ListTag listtag = new ListTag();

      for(int i = 0; i < nonnulllist.size(); ++i) {
         ItemStack itemstack = nonnulllist.get(i);
         if (!itemstack.isEmpty()) {
            CompoundTag compoundtag1 = new CompoundTag();
            compoundtag1.putByte("Slot", (byte)i);
            itemstack.save(compoundtag1);
            listtag.add(compoundtag1);
         }
      }

      if (!listtag.isEmpty() || flag) {
         compoundtag.put("Items", listtag);
      }

      return compoundtag;
   }

   public static void loadAllItems(CompoundTag compoundtag, NonNullList<ItemStack> nonnulllist) {
      ListTag listtag = compoundtag.getList("Items", 10);

      for(int i = 0; i < listtag.size(); ++i) {
         CompoundTag compoundtag1 = listtag.getCompound(i);
         int j = compoundtag1.getByte("Slot") & 255;
         if (j >= 0 && j < nonnulllist.size()) {
            nonnulllist.set(j, ItemStack.of(compoundtag1));
         }
      }

   }

   public static int clearOrCountMatchingItems(Container container, Predicate<ItemStack> predicate, int i, boolean flag) {
      int j = 0;

      for(int k = 0; k < container.getContainerSize(); ++k) {
         ItemStack itemstack = container.getItem(k);
         int l = clearOrCountMatchingItems(itemstack, predicate, i - j, flag);
         if (l > 0 && !flag && itemstack.isEmpty()) {
            container.setItem(k, ItemStack.EMPTY);
         }

         j += l;
      }

      return j;
   }

   public static int clearOrCountMatchingItems(ItemStack itemstack, Predicate<ItemStack> predicate, int i, boolean flag) {
      if (!itemstack.isEmpty() && predicate.test(itemstack)) {
         if (flag) {
            return itemstack.getCount();
         } else {
            int j = i < 0 ? itemstack.getCount() : Math.min(i, itemstack.getCount());
            itemstack.shrink(j);
            return j;
         }
      } else {
         return 0;
      }
   }
}
