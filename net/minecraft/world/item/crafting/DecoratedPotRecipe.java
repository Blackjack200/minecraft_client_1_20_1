package net.minecraft.world.item.crafting;

import net.minecraft.core.RegistryAccess;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.ItemTags;
import net.minecraft.world.inventory.CraftingContainer;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.entity.DecoratedPotBlockEntity;

public class DecoratedPotRecipe extends CustomRecipe {
   public DecoratedPotRecipe(ResourceLocation resourcelocation, CraftingBookCategory craftingbookcategory) {
      super(resourcelocation, craftingbookcategory);
   }

   public boolean matches(CraftingContainer craftingcontainer, Level level) {
      if (!this.canCraftInDimensions(craftingcontainer.getWidth(), craftingcontainer.getHeight())) {
         return false;
      } else {
         for(int i = 0; i < craftingcontainer.getContainerSize(); ++i) {
            ItemStack itemstack = craftingcontainer.getItem(i);
            switch (i) {
               case 1:
               case 3:
               case 5:
               case 7:
                  if (!itemstack.is(ItemTags.DECORATED_POT_INGREDIENTS)) {
                     return false;
                  }
                  break;
               case 2:
               case 4:
               case 6:
               default:
                  if (!itemstack.is(Items.AIR)) {
                     return false;
                  }
            }
         }

         return true;
      }
   }

   public ItemStack assemble(CraftingContainer craftingcontainer, RegistryAccess registryaccess) {
      DecoratedPotBlockEntity.Decorations decoratedpotblockentity_decorations = new DecoratedPotBlockEntity.Decorations(craftingcontainer.getItem(1).getItem(), craftingcontainer.getItem(3).getItem(), craftingcontainer.getItem(5).getItem(), craftingcontainer.getItem(7).getItem());
      return createDecoratedPotItem(decoratedpotblockentity_decorations);
   }

   public static ItemStack createDecoratedPotItem(DecoratedPotBlockEntity.Decorations decoratedpotblockentity_decorations) {
      ItemStack itemstack = Items.DECORATED_POT.getDefaultInstance();
      CompoundTag compoundtag = decoratedpotblockentity_decorations.save(new CompoundTag());
      BlockItem.setBlockEntityData(itemstack, BlockEntityType.DECORATED_POT, compoundtag);
      return itemstack;
   }

   public boolean canCraftInDimensions(int i, int j) {
      return i == 3 && j == 3;
   }

   public RecipeSerializer<?> getSerializer() {
      return RecipeSerializer.DECORATED_POT_RECIPE;
   }
}
