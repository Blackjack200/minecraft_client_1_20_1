package net.minecraft.world.item.crafting;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.google.gson.JsonParseException;
import it.unimi.dsi.fastutil.ints.IntList;
import net.minecraft.core.NonNullList;
import net.minecraft.core.RegistryAccess;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.GsonHelper;
import net.minecraft.world.entity.player.StackedContents;
import net.minecraft.world.inventory.CraftingContainer;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;

public class ShapelessRecipe implements CraftingRecipe {
   private final ResourceLocation id;
   final String group;
   final CraftingBookCategory category;
   final ItemStack result;
   final NonNullList<Ingredient> ingredients;

   public ShapelessRecipe(ResourceLocation resourcelocation, String s, CraftingBookCategory craftingbookcategory, ItemStack itemstack, NonNullList<Ingredient> nonnulllist) {
      this.id = resourcelocation;
      this.group = s;
      this.category = craftingbookcategory;
      this.result = itemstack;
      this.ingredients = nonnulllist;
   }

   public ResourceLocation getId() {
      return this.id;
   }

   public RecipeSerializer<?> getSerializer() {
      return RecipeSerializer.SHAPELESS_RECIPE;
   }

   public String getGroup() {
      return this.group;
   }

   public CraftingBookCategory category() {
      return this.category;
   }

   public ItemStack getResultItem(RegistryAccess registryaccess) {
      return this.result;
   }

   public NonNullList<Ingredient> getIngredients() {
      return this.ingredients;
   }

   public boolean matches(CraftingContainer craftingcontainer, Level level) {
      StackedContents stackedcontents = new StackedContents();
      int i = 0;

      for(int j = 0; j < craftingcontainer.getContainerSize(); ++j) {
         ItemStack itemstack = craftingcontainer.getItem(j);
         if (!itemstack.isEmpty()) {
            ++i;
            stackedcontents.accountStack(itemstack, 1);
         }
      }

      return i == this.ingredients.size() && stackedcontents.canCraft(this, (IntList)null);
   }

   public ItemStack assemble(CraftingContainer craftingcontainer, RegistryAccess registryaccess) {
      return this.result.copy();
   }

   public boolean canCraftInDimensions(int i, int j) {
      return i * j >= this.ingredients.size();
   }

   public static class Serializer implements RecipeSerializer<ShapelessRecipe> {
      public ShapelessRecipe fromJson(ResourceLocation resourcelocation, JsonObject jsonobject) {
         String s = GsonHelper.getAsString(jsonobject, "group", "");
         CraftingBookCategory craftingbookcategory = CraftingBookCategory.CODEC.byName(GsonHelper.getAsString(jsonobject, "category", (String)null), CraftingBookCategory.MISC);
         NonNullList<Ingredient> nonnulllist = itemsFromJson(GsonHelper.getAsJsonArray(jsonobject, "ingredients"));
         if (nonnulllist.isEmpty()) {
            throw new JsonParseException("No ingredients for shapeless recipe");
         } else if (nonnulllist.size() > 9) {
            throw new JsonParseException("Too many ingredients for shapeless recipe");
         } else {
            ItemStack itemstack = ShapedRecipe.itemStackFromJson(GsonHelper.getAsJsonObject(jsonobject, "result"));
            return new ShapelessRecipe(resourcelocation, s, craftingbookcategory, itemstack, nonnulllist);
         }
      }

      private static NonNullList<Ingredient> itemsFromJson(JsonArray jsonarray) {
         NonNullList<Ingredient> nonnulllist = NonNullList.create();

         for(int i = 0; i < jsonarray.size(); ++i) {
            Ingredient ingredient = Ingredient.fromJson(jsonarray.get(i), false);
            if (!ingredient.isEmpty()) {
               nonnulllist.add(ingredient);
            }
         }

         return nonnulllist;
      }

      public ShapelessRecipe fromNetwork(ResourceLocation resourcelocation, FriendlyByteBuf friendlybytebuf) {
         String s = friendlybytebuf.readUtf();
         CraftingBookCategory craftingbookcategory = friendlybytebuf.readEnum(CraftingBookCategory.class);
         int i = friendlybytebuf.readVarInt();
         NonNullList<Ingredient> nonnulllist = NonNullList.withSize(i, Ingredient.EMPTY);

         for(int j = 0; j < nonnulllist.size(); ++j) {
            nonnulllist.set(j, Ingredient.fromNetwork(friendlybytebuf));
         }

         ItemStack itemstack = friendlybytebuf.readItem();
         return new ShapelessRecipe(resourcelocation, s, craftingbookcategory, itemstack, nonnulllist);
      }

      public void toNetwork(FriendlyByteBuf friendlybytebuf, ShapelessRecipe shapelessrecipe) {
         friendlybytebuf.writeUtf(shapelessrecipe.group);
         friendlybytebuf.writeEnum(shapelessrecipe.category);
         friendlybytebuf.writeVarInt(shapelessrecipe.ingredients.size());

         for(Ingredient ingredient : shapelessrecipe.ingredients) {
            ingredient.toNetwork(friendlybytebuf);
         }

         friendlybytebuf.writeItem(shapelessrecipe.result);
      }
   }
}
