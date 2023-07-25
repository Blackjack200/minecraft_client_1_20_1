package net.minecraft.world.item.crafting;

import com.google.gson.JsonObject;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.GsonHelper;

public class SimpleCraftingRecipeSerializer<T extends CraftingRecipe> implements RecipeSerializer<T> {
   private final SimpleCraftingRecipeSerializer.Factory<T> constructor;

   public SimpleCraftingRecipeSerializer(SimpleCraftingRecipeSerializer.Factory<T> simplecraftingrecipeserializer_factory) {
      this.constructor = simplecraftingrecipeserializer_factory;
   }

   public T fromJson(ResourceLocation resourcelocation, JsonObject jsonobject) {
      CraftingBookCategory craftingbookcategory = CraftingBookCategory.CODEC.byName(GsonHelper.getAsString(jsonobject, "category", (String)null), CraftingBookCategory.MISC);
      return this.constructor.create(resourcelocation, craftingbookcategory);
   }

   public T fromNetwork(ResourceLocation resourcelocation, FriendlyByteBuf friendlybytebuf) {
      CraftingBookCategory craftingbookcategory = friendlybytebuf.readEnum(CraftingBookCategory.class);
      return this.constructor.create(resourcelocation, craftingbookcategory);
   }

   public void toNetwork(FriendlyByteBuf friendlybytebuf, T craftingrecipe) {
      friendlybytebuf.writeEnum(craftingrecipe.category());
   }

   @FunctionalInterface
   public interface Factory<T extends CraftingRecipe> {
      T create(ResourceLocation resourcelocation, CraftingBookCategory craftingbookcategory);
   }
}
