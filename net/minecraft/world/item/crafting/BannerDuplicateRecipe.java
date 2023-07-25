package net.minecraft.world.item.crafting;

import net.minecraft.core.NonNullList;
import net.minecraft.core.RegistryAccess;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.inventory.CraftingContainer;
import net.minecraft.world.item.BannerItem;
import net.minecraft.world.item.DyeColor;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BannerBlockEntity;

public class BannerDuplicateRecipe extends CustomRecipe {
   public BannerDuplicateRecipe(ResourceLocation resourcelocation, CraftingBookCategory craftingbookcategory) {
      super(resourcelocation, craftingbookcategory);
   }

   public boolean matches(CraftingContainer craftingcontainer, Level level) {
      DyeColor dyecolor = null;
      ItemStack itemstack = null;
      ItemStack itemstack1 = null;

      for(int i = 0; i < craftingcontainer.getContainerSize(); ++i) {
         ItemStack itemstack2 = craftingcontainer.getItem(i);
         if (!itemstack2.isEmpty()) {
            Item item = itemstack2.getItem();
            if (!(item instanceof BannerItem)) {
               return false;
            }

            BannerItem banneritem = (BannerItem)item;
            if (dyecolor == null) {
               dyecolor = banneritem.getColor();
            } else if (dyecolor != banneritem.getColor()) {
               return false;
            }

            int j = BannerBlockEntity.getPatternCount(itemstack2);
            if (j > 6) {
               return false;
            }

            if (j > 0) {
               if (itemstack != null) {
                  return false;
               }

               itemstack = itemstack2;
            } else {
               if (itemstack1 != null) {
                  return false;
               }

               itemstack1 = itemstack2;
            }
         }
      }

      return itemstack != null && itemstack1 != null;
   }

   public ItemStack assemble(CraftingContainer craftingcontainer, RegistryAccess registryaccess) {
      for(int i = 0; i < craftingcontainer.getContainerSize(); ++i) {
         ItemStack itemstack = craftingcontainer.getItem(i);
         if (!itemstack.isEmpty()) {
            int j = BannerBlockEntity.getPatternCount(itemstack);
            if (j > 0 && j <= 6) {
               return itemstack.copyWithCount(1);
            }
         }
      }

      return ItemStack.EMPTY;
   }

   public NonNullList<ItemStack> getRemainingItems(CraftingContainer craftingcontainer) {
      NonNullList<ItemStack> nonnulllist = NonNullList.withSize(craftingcontainer.getContainerSize(), ItemStack.EMPTY);

      for(int i = 0; i < nonnulllist.size(); ++i) {
         ItemStack itemstack = craftingcontainer.getItem(i);
         if (!itemstack.isEmpty()) {
            if (itemstack.getItem().hasCraftingRemainingItem()) {
               nonnulllist.set(i, new ItemStack(itemstack.getItem().getCraftingRemainingItem()));
            } else if (itemstack.hasTag() && BannerBlockEntity.getPatternCount(itemstack) > 0) {
               nonnulllist.set(i, itemstack.copyWithCount(1));
            }
         }
      }

      return nonnulllist;
   }

   public RecipeSerializer<?> getSerializer() {
      return RecipeSerializer.BANNER_DUPLICATE;
   }

   public boolean canCraftInDimensions(int i, int j) {
      return i * j >= 2;
   }
}
