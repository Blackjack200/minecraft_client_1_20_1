package net.minecraft.world.item.crafting;

import com.google.gson.JsonObject;
import java.util.stream.Stream;
import net.minecraft.core.RegistryAccess;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.GsonHelper;
import net.minecraft.world.Container;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;

public class SmithingTransformRecipe implements SmithingRecipe {
   private final ResourceLocation id;
   final Ingredient template;
   final Ingredient base;
   final Ingredient addition;
   final ItemStack result;

   public SmithingTransformRecipe(ResourceLocation resourcelocation, Ingredient ingredient, Ingredient ingredient1, Ingredient ingredient2, ItemStack itemstack) {
      this.id = resourcelocation;
      this.template = ingredient;
      this.base = ingredient1;
      this.addition = ingredient2;
      this.result = itemstack;
   }

   public boolean matches(Container container, Level level) {
      return this.template.test(container.getItem(0)) && this.base.test(container.getItem(1)) && this.addition.test(container.getItem(2));
   }

   public ItemStack assemble(Container container, RegistryAccess registryaccess) {
      ItemStack itemstack = this.result.copy();
      CompoundTag compoundtag = container.getItem(1).getTag();
      if (compoundtag != null) {
         itemstack.setTag(compoundtag.copy());
      }

      return itemstack;
   }

   public ItemStack getResultItem(RegistryAccess registryaccess) {
      return this.result;
   }

   public boolean isTemplateIngredient(ItemStack itemstack) {
      return this.template.test(itemstack);
   }

   public boolean isBaseIngredient(ItemStack itemstack) {
      return this.base.test(itemstack);
   }

   public boolean isAdditionIngredient(ItemStack itemstack) {
      return this.addition.test(itemstack);
   }

   public ResourceLocation getId() {
      return this.id;
   }

   public RecipeSerializer<?> getSerializer() {
      return RecipeSerializer.SMITHING_TRANSFORM;
   }

   public boolean isIncomplete() {
      return Stream.of(this.template, this.base, this.addition).anyMatch(Ingredient::isEmpty);
   }

   public static class Serializer implements RecipeSerializer<SmithingTransformRecipe> {
      public SmithingTransformRecipe fromJson(ResourceLocation resourcelocation, JsonObject jsonobject) {
         Ingredient ingredient = Ingredient.fromJson(GsonHelper.getNonNull(jsonobject, "template"));
         Ingredient ingredient1 = Ingredient.fromJson(GsonHelper.getNonNull(jsonobject, "base"));
         Ingredient ingredient2 = Ingredient.fromJson(GsonHelper.getNonNull(jsonobject, "addition"));
         ItemStack itemstack = ShapedRecipe.itemStackFromJson(GsonHelper.getAsJsonObject(jsonobject, "result"));
         return new SmithingTransformRecipe(resourcelocation, ingredient, ingredient1, ingredient2, itemstack);
      }

      public SmithingTransformRecipe fromNetwork(ResourceLocation resourcelocation, FriendlyByteBuf friendlybytebuf) {
         Ingredient ingredient = Ingredient.fromNetwork(friendlybytebuf);
         Ingredient ingredient1 = Ingredient.fromNetwork(friendlybytebuf);
         Ingredient ingredient2 = Ingredient.fromNetwork(friendlybytebuf);
         ItemStack itemstack = friendlybytebuf.readItem();
         return new SmithingTransformRecipe(resourcelocation, ingredient, ingredient1, ingredient2, itemstack);
      }

      public void toNetwork(FriendlyByteBuf friendlybytebuf, SmithingTransformRecipe smithingtransformrecipe) {
         smithingtransformrecipe.template.toNetwork(friendlybytebuf);
         smithingtransformrecipe.base.toNetwork(friendlybytebuf);
         smithingtransformrecipe.addition.toNetwork(friendlybytebuf);
         friendlybytebuf.writeItem(smithingtransformrecipe.result);
      }
   }
}
