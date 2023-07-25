package net.minecraft.recipebook;

import java.util.Iterator;
import net.minecraft.util.Mth;
import net.minecraft.world.item.crafting.Recipe;
import net.minecraft.world.item.crafting.ShapedRecipe;

public interface PlaceRecipe<T> {
   default void placeRecipe(int i, int j, int k, Recipe<?> recipe, Iterator<T> iterator, int l) {
      int i1 = i;
      int j1 = j;
      if (recipe instanceof ShapedRecipe shapedrecipe) {
         i1 = shapedrecipe.getWidth();
         j1 = shapedrecipe.getHeight();
      }

      int k1 = 0;

      for(int l1 = 0; l1 < j; ++l1) {
         if (k1 == k) {
            ++k1;
         }

         boolean flag = (float)j1 < (float)j / 2.0F;
         int i2 = Mth.floor((float)j / 2.0F - (float)j1 / 2.0F);
         if (flag && i2 > l1) {
            k1 += i;
            ++l1;
         }

         for(int j2 = 0; j2 < i; ++j2) {
            if (!iterator.hasNext()) {
               return;
            }

            flag = (float)i1 < (float)i / 2.0F;
            i2 = Mth.floor((float)i / 2.0F - (float)i1 / 2.0F);
            int k2 = i1;
            boolean flag1 = j2 < i1;
            if (flag) {
               k2 = i2 + i1;
               flag1 = i2 <= j2 && j2 < i2 + i1;
            }

            if (flag1) {
               this.addItemToSlot(iterator, k1, l, l1, j2);
            } else if (k2 == j2) {
               k1 += i - j2;
               break;
            }

            ++k1;
         }
      }

   }

   void addItemToSlot(Iterator<T> iterator, int i, int j, int k, int l);
}
