package net.minecraft.world.item.crafting;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.GsonHelper;
import net.minecraft.world.item.ItemStack;

public class SimpleCookingSerializer<T extends AbstractCookingRecipe> implements RecipeSerializer<T> {
   private final int defaultCookingTime;
   private final SimpleCookingSerializer.CookieBaker<T> factory;

   public SimpleCookingSerializer(SimpleCookingSerializer.CookieBaker<T> simplecookingserializer_cookiebaker, int i) {
      this.defaultCookingTime = i;
      this.factory = simplecookingserializer_cookiebaker;
   }

   public T fromJson(ResourceLocation resourcelocation, JsonObject jsonobject) {
      String s = GsonHelper.getAsString(jsonobject, "group", "");
      CookingBookCategory cookingbookcategory = CookingBookCategory.CODEC.byName(GsonHelper.getAsString(jsonobject, "category", (String)null), CookingBookCategory.MISC);
      JsonElement jsonelement = (JsonElement)(GsonHelper.isArrayNode(jsonobject, "ingredient") ? GsonHelper.getAsJsonArray(jsonobject, "ingredient") : GsonHelper.getAsJsonObject(jsonobject, "ingredient"));
      Ingredient ingredient = Ingredient.fromJson(jsonelement, false);
      String s1 = GsonHelper.getAsString(jsonobject, "result");
      ResourceLocation resourcelocation1 = new ResourceLocation(s1);
      ItemStack itemstack = new ItemStack(BuiltInRegistries.ITEM.getOptional(resourcelocation1).orElseThrow(() -> new IllegalStateException("Item: " + s1 + " does not exist")));
      float f = GsonHelper.getAsFloat(jsonobject, "experience", 0.0F);
      int i = GsonHelper.getAsInt(jsonobject, "cookingtime", this.defaultCookingTime);
      return this.factory.create(resourcelocation, s, cookingbookcategory, ingredient, itemstack, f, i);
   }

   public T fromNetwork(ResourceLocation resourcelocation, FriendlyByteBuf friendlybytebuf) {
      String s = friendlybytebuf.readUtf();
      CookingBookCategory cookingbookcategory = friendlybytebuf.readEnum(CookingBookCategory.class);
      Ingredient ingredient = Ingredient.fromNetwork(friendlybytebuf);
      ItemStack itemstack = friendlybytebuf.readItem();
      float f = friendlybytebuf.readFloat();
      int i = friendlybytebuf.readVarInt();
      return this.factory.create(resourcelocation, s, cookingbookcategory, ingredient, itemstack, f, i);
   }

   public void toNetwork(FriendlyByteBuf friendlybytebuf, T abstractcookingrecipe) {
      friendlybytebuf.writeUtf(abstractcookingrecipe.group);
      friendlybytebuf.writeEnum(abstractcookingrecipe.category());
      abstractcookingrecipe.ingredient.toNetwork(friendlybytebuf);
      friendlybytebuf.writeItem(abstractcookingrecipe.result);
      friendlybytebuf.writeFloat(abstractcookingrecipe.experience);
      friendlybytebuf.writeVarInt(abstractcookingrecipe.cookingTime);
   }

   interface CookieBaker<T extends AbstractCookingRecipe> {
      T create(ResourceLocation resourcelocation, String s, CookingBookCategory cookingbookcategory, Ingredient ingredient, ItemStack itemstack, float f, int i);
   }
}
