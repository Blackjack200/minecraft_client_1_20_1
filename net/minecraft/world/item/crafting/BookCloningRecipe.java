package net.minecraft.world.item.crafting;

import net.minecraft.core.NonNullList;
import net.minecraft.core.RegistryAccess;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.inventory.CraftingContainer;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.WrittenBookItem;
import net.minecraft.world.level.Level;

public class BookCloningRecipe extends CustomRecipe {
   public BookCloningRecipe(ResourceLocation resourcelocation, CraftingBookCategory craftingbookcategory) {
      super(resourcelocation, craftingbookcategory);
   }

   public boolean matches(CraftingContainer craftingcontainer, Level level) {
      int i = 0;
      ItemStack itemstack = ItemStack.EMPTY;

      for(int j = 0; j < craftingcontainer.getContainerSize(); ++j) {
         ItemStack itemstack1 = craftingcontainer.getItem(j);
         if (!itemstack1.isEmpty()) {
            if (itemstack1.is(Items.WRITTEN_BOOK)) {
               if (!itemstack.isEmpty()) {
                  return false;
               }

               itemstack = itemstack1;
            } else {
               if (!itemstack1.is(Items.WRITABLE_BOOK)) {
                  return false;
               }

               ++i;
            }
         }
      }

      return !itemstack.isEmpty() && itemstack.hasTag() && i > 0;
   }

   public ItemStack assemble(CraftingContainer craftingcontainer, RegistryAccess registryaccess) {
      int i = 0;
      ItemStack itemstack = ItemStack.EMPTY;

      for(int j = 0; j < craftingcontainer.getContainerSize(); ++j) {
         ItemStack itemstack1 = craftingcontainer.getItem(j);
         if (!itemstack1.isEmpty()) {
            if (itemstack1.is(Items.WRITTEN_BOOK)) {
               if (!itemstack.isEmpty()) {
                  return ItemStack.EMPTY;
               }

               itemstack = itemstack1;
            } else {
               if (!itemstack1.is(Items.WRITABLE_BOOK)) {
                  return ItemStack.EMPTY;
               }

               ++i;
            }
         }
      }

      if (!itemstack.isEmpty() && itemstack.hasTag() && i >= 1 && WrittenBookItem.getGeneration(itemstack) < 2) {
         ItemStack itemstack2 = new ItemStack(Items.WRITTEN_BOOK, i);
         CompoundTag compoundtag = itemstack.getTag().copy();
         compoundtag.putInt("generation", WrittenBookItem.getGeneration(itemstack) + 1);
         itemstack2.setTag(compoundtag);
         return itemstack2;
      } else {
         return ItemStack.EMPTY;
      }
   }

   public NonNullList<ItemStack> getRemainingItems(CraftingContainer craftingcontainer) {
      NonNullList<ItemStack> nonnulllist = NonNullList.withSize(craftingcontainer.getContainerSize(), ItemStack.EMPTY);

      for(int i = 0; i < nonnulllist.size(); ++i) {
         ItemStack itemstack = craftingcontainer.getItem(i);
         if (itemstack.getItem().hasCraftingRemainingItem()) {
            nonnulllist.set(i, new ItemStack(itemstack.getItem().getCraftingRemainingItem()));
         } else if (itemstack.getItem() instanceof WrittenBookItem) {
            nonnulllist.set(i, itemstack.copyWithCount(1));
            break;
         }
      }

      return nonnulllist;
   }

   public RecipeSerializer<?> getSerializer() {
      return RecipeSerializer.BOOK_CLONING;
   }

   public boolean canCraftInDimensions(int i, int j) {
      return i >= 3 && j >= 3;
   }
}
