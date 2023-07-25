package net.minecraft.world.item.crafting;

import net.minecraft.core.RegistryAccess;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.inventory.CraftingContainer;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.alchemy.PotionUtils;
import net.minecraft.world.level.Level;

public class TippedArrowRecipe extends CustomRecipe {
   public TippedArrowRecipe(ResourceLocation resourcelocation, CraftingBookCategory craftingbookcategory) {
      super(resourcelocation, craftingbookcategory);
   }

   public boolean matches(CraftingContainer craftingcontainer, Level level) {
      if (craftingcontainer.getWidth() == 3 && craftingcontainer.getHeight() == 3) {
         for(int i = 0; i < craftingcontainer.getWidth(); ++i) {
            for(int j = 0; j < craftingcontainer.getHeight(); ++j) {
               ItemStack itemstack = craftingcontainer.getItem(i + j * craftingcontainer.getWidth());
               if (itemstack.isEmpty()) {
                  return false;
               }

               if (i == 1 && j == 1) {
                  if (!itemstack.is(Items.LINGERING_POTION)) {
                     return false;
                  }
               } else if (!itemstack.is(Items.ARROW)) {
                  return false;
               }
            }
         }

         return true;
      } else {
         return false;
      }
   }

   public ItemStack assemble(CraftingContainer craftingcontainer, RegistryAccess registryaccess) {
      ItemStack itemstack = craftingcontainer.getItem(1 + craftingcontainer.getWidth());
      if (!itemstack.is(Items.LINGERING_POTION)) {
         return ItemStack.EMPTY;
      } else {
         ItemStack itemstack1 = new ItemStack(Items.TIPPED_ARROW, 8);
         PotionUtils.setPotion(itemstack1, PotionUtils.getPotion(itemstack));
         PotionUtils.setCustomEffects(itemstack1, PotionUtils.getCustomEffects(itemstack));
         return itemstack1;
      }
   }

   public boolean canCraftInDimensions(int i, int j) {
      return i >= 2 && j >= 2;
   }

   public RecipeSerializer<?> getSerializer() {
      return RecipeSerializer.TIPPED_ARROW;
   }
}
