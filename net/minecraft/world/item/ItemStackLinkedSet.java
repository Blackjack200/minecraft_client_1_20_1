package net.minecraft.world.item;

import it.unimi.dsi.fastutil.Hash;
import it.unimi.dsi.fastutil.objects.ObjectLinkedOpenCustomHashSet;
import java.util.Set;
import javax.annotation.Nullable;
import net.minecraft.nbt.CompoundTag;

public class ItemStackLinkedSet {
   private static final Hash.Strategy<? super ItemStack> TYPE_AND_TAG = new Hash.Strategy<ItemStack>() {
      public int hashCode(@Nullable ItemStack itemstack) {
         return ItemStackLinkedSet.hashStackAndTag(itemstack);
      }

      public boolean equals(@Nullable ItemStack itemstack, @Nullable ItemStack itemstack1) {
         return itemstack == itemstack1 || itemstack != null && itemstack1 != null && itemstack.isEmpty() == itemstack1.isEmpty() && ItemStack.isSameItemSameTags(itemstack, itemstack1);
      }
   };

   static int hashStackAndTag(@Nullable ItemStack itemstack) {
      if (itemstack != null) {
         CompoundTag compoundtag = itemstack.getTag();
         int i = 31 + itemstack.getItem().hashCode();
         return 31 * i + (compoundtag == null ? 0 : compoundtag.hashCode());
      } else {
         return 0;
      }
   }

   public static Set<ItemStack> createTypeAndTagSet() {
      return new ObjectLinkedOpenCustomHashSet<>(TYPE_AND_TAG);
   }
}
