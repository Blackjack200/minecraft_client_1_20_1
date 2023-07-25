package net.minecraft.world.item.crafting;

import net.minecraft.core.RegistryAccess;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.inventory.CraftingContainer;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.Level;

public class MapCloningRecipe extends CustomRecipe {
   public MapCloningRecipe(ResourceLocation resourcelocation, CraftingBookCategory craftingbookcategory) {
      super(resourcelocation, craftingbookcategory);
   }

   public boolean matches(CraftingContainer craftingcontainer, Level level) {
      int i = 0;
      ItemStack itemstack = ItemStack.EMPTY;

      for(int j = 0; j < craftingcontainer.getContainerSize(); ++j) {
         ItemStack itemstack1 = craftingcontainer.getItem(j);
         if (!itemstack1.isEmpty()) {
            if (itemstack1.is(Items.FILLED_MAP)) {
               if (!itemstack.isEmpty()) {
                  return false;
               }

               itemstack = itemstack1;
            } else {
               if (!itemstack1.is(Items.MAP)) {
                  return false;
               }

               ++i;
            }
         }
      }

      return !itemstack.isEmpty() && i > 0;
   }

   public ItemStack assemble(CraftingContainer craftingcontainer, RegistryAccess registryaccess) {
      int i = 0;
      ItemStack itemstack = ItemStack.EMPTY;

      for(int j = 0; j < craftingcontainer.getContainerSize(); ++j) {
         ItemStack itemstack1 = craftingcontainer.getItem(j);
         if (!itemstack1.isEmpty()) {
            if (itemstack1.is(Items.FILLED_MAP)) {
               if (!itemstack.isEmpty()) {
                  return ItemStack.EMPTY;
               }

               itemstack = itemstack1;
            } else {
               if (!itemstack1.is(Items.MAP)) {
                  return ItemStack.EMPTY;
               }

               ++i;
            }
         }
      }

      return !itemstack.isEmpty() && i >= 1 ? itemstack.copyWithCount(i + 1) : ItemStack.EMPTY;
   }

   public boolean canCraftInDimensions(int i, int j) {
      return i >= 3 && j >= 3;
   }

   public RecipeSerializer<?> getSerializer() {
      return RecipeSerializer.MAP_CLONING;
   }
}
