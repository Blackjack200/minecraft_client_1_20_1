package net.minecraft.world.item.crafting;

import com.google.gson.JsonObject;
import net.minecraft.core.NonNullList;
import net.minecraft.core.RegistryAccess;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.GsonHelper;
import net.minecraft.world.Container;
import net.minecraft.world.item.ItemStack;

public abstract class SingleItemRecipe implements Recipe<Container> {
   protected final Ingredient ingredient;
   protected final ItemStack result;
   private final RecipeType<?> type;
   private final RecipeSerializer<?> serializer;
   protected final ResourceLocation id;
   protected final String group;

   public SingleItemRecipe(RecipeType<?> recipetype, RecipeSerializer<?> recipeserializer, ResourceLocation resourcelocation, String s, Ingredient ingredient, ItemStack itemstack) {
      this.type = recipetype;
      this.serializer = recipeserializer;
      this.id = resourcelocation;
      this.group = s;
      this.ingredient = ingredient;
      this.result = itemstack;
   }

   public RecipeType<?> getType() {
      return this.type;
   }

   public RecipeSerializer<?> getSerializer() {
      return this.serializer;
   }

   public ResourceLocation getId() {
      return this.id;
   }

   public String getGroup() {
      return this.group;
   }

   public ItemStack getResultItem(RegistryAccess registryaccess) {
      return this.result;
   }

   public NonNullList<Ingredient> getIngredients() {
      NonNullList<Ingredient> nonnulllist = NonNullList.create();
      nonnulllist.add(this.ingredient);
      return nonnulllist;
   }

   public boolean canCraftInDimensions(int i, int j) {
      return true;
   }

   public ItemStack assemble(Container container, RegistryAccess registryaccess) {
      return this.result.copy();
   }

   public static class Serializer<T extends SingleItemRecipe> implements RecipeSerializer<T> {
      final SingleItemRecipe.Serializer.SingleItemMaker<T> factory;

      protected Serializer(SingleItemRecipe.Serializer.SingleItemMaker<T> singleitemrecipe_serializer_singleitemmaker) {
         this.factory = singleitemrecipe_serializer_singleitemmaker;
      }

      public T fromJson(ResourceLocation resourcelocation, JsonObject jsonobject) {
         String s = GsonHelper.getAsString(jsonobject, "group", "");
         Ingredient ingredient;
         if (GsonHelper.isArrayNode(jsonobject, "ingredient")) {
            ingredient = Ingredient.fromJson(GsonHelper.getAsJsonArray(jsonobject, "ingredient"), false);
         } else {
            ingredient = Ingredient.fromJson(GsonHelper.getAsJsonObject(jsonobject, "ingredient"), false);
         }

         String s1 = GsonHelper.getAsString(jsonobject, "result");
         int i = GsonHelper.getAsInt(jsonobject, "count");
         ItemStack itemstack = new ItemStack(BuiltInRegistries.ITEM.get(new ResourceLocation(s1)), i);
         return this.factory.create(resourcelocation, s, ingredient, itemstack);
      }

      public T fromNetwork(ResourceLocation resourcelocation, FriendlyByteBuf friendlybytebuf) {
         String s = friendlybytebuf.readUtf();
         Ingredient ingredient = Ingredient.fromNetwork(friendlybytebuf);
         ItemStack itemstack = friendlybytebuf.readItem();
         return this.factory.create(resourcelocation, s, ingredient, itemstack);
      }

      public void toNetwork(FriendlyByteBuf friendlybytebuf, T singleitemrecipe) {
         friendlybytebuf.writeUtf(singleitemrecipe.group);
         singleitemrecipe.ingredient.toNetwork(friendlybytebuf);
         friendlybytebuf.writeItem(singleitemrecipe.result);
      }

      interface SingleItemMaker<T extends SingleItemRecipe> {
         T create(ResourceLocation resourcelocation, String s, Ingredient ingredient, ItemStack itemstack);
      }
   }
}
