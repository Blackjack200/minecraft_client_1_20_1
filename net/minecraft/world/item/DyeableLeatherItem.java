package net.minecraft.world.item;

import java.util.List;
import net.minecraft.nbt.CompoundTag;

public interface DyeableLeatherItem {
   String TAG_COLOR = "color";
   String TAG_DISPLAY = "display";
   int DEFAULT_LEATHER_COLOR = 10511680;

   default boolean hasCustomColor(ItemStack itemstack) {
      CompoundTag compoundtag = itemstack.getTagElement("display");
      return compoundtag != null && compoundtag.contains("color", 99);
   }

   default int getColor(ItemStack itemstack) {
      CompoundTag compoundtag = itemstack.getTagElement("display");
      return compoundtag != null && compoundtag.contains("color", 99) ? compoundtag.getInt("color") : 10511680;
   }

   default void clearColor(ItemStack itemstack) {
      CompoundTag compoundtag = itemstack.getTagElement("display");
      if (compoundtag != null && compoundtag.contains("color")) {
         compoundtag.remove("color");
      }

   }

   default void setColor(ItemStack itemstack, int i) {
      itemstack.getOrCreateTagElement("display").putInt("color", i);
   }

   static ItemStack dyeArmor(ItemStack itemstack, List<DyeItem> list) {
      ItemStack itemstack1 = ItemStack.EMPTY;
      int[] aint = new int[3];
      int i = 0;
      int j = 0;
      Item item = itemstack.getItem();
      if (item instanceof DyeableLeatherItem dyeableleatheritem) {
         itemstack1 = itemstack.copyWithCount(1);
         if (dyeableleatheritem.hasCustomColor(itemstack)) {
            int k = dyeableleatheritem.getColor(itemstack1);
            float f = (float)(k >> 16 & 255) / 255.0F;
            float f1 = (float)(k >> 8 & 255) / 255.0F;
            float f2 = (float)(k & 255) / 255.0F;
            i += (int)(Math.max(f, Math.max(f1, f2)) * 255.0F);
            aint[0] += (int)(f * 255.0F);
            aint[1] += (int)(f1 * 255.0F);
            aint[2] += (int)(f2 * 255.0F);
            ++j;
         }

         for(DyeItem dyeitem : list) {
            float[] afloat = dyeitem.getDyeColor().getTextureDiffuseColors();
            int l = (int)(afloat[0] * 255.0F);
            int i1 = (int)(afloat[1] * 255.0F);
            int j1 = (int)(afloat[2] * 255.0F);
            i += Math.max(l, Math.max(i1, j1));
            aint[0] += l;
            aint[1] += i1;
            aint[2] += j1;
            ++j;
         }
      }

      if (dyeableleatheritem == null) {
         return ItemStack.EMPTY;
      } else {
         int k1 = aint[0] / j;
         int l1 = aint[1] / j;
         int i2 = aint[2] / j;
         float f3 = (float)i / (float)j;
         float f4 = (float)Math.max(k1, Math.max(l1, i2));
         k1 = (int)((float)k1 * f3 / f4);
         l1 = (int)((float)l1 * f3 / f4);
         i2 = (int)((float)i2 * f3 / f4);
         int var26 = (k1 << 8) + l1;
         var26 = (var26 << 8) + i2;
         dyeableleatheritem.setColor(itemstack1, var26);
         return itemstack1;
      }
   }
}
