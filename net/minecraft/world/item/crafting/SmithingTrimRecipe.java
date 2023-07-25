package net.minecraft.world.item.crafting;

import com.google.gson.JsonObject;
import java.util.Optional;
import java.util.stream.Stream;
import net.minecraft.core.Holder;
import net.minecraft.core.RegistryAccess;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.GsonHelper;
import net.minecraft.world.Container;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.armortrim.ArmorTrim;
import net.minecraft.world.item.armortrim.TrimMaterial;
import net.minecraft.world.item.armortrim.TrimMaterials;
import net.minecraft.world.item.armortrim.TrimPattern;
import net.minecraft.world.item.armortrim.TrimPatterns;
import net.minecraft.world.level.Level;

public class SmithingTrimRecipe implements SmithingRecipe {
   private final ResourceLocation id;
   final Ingredient template;
   final Ingredient base;
   final Ingredient addition;

   public SmithingTrimRecipe(ResourceLocation resourcelocation, Ingredient ingredient, Ingredient ingredient1, Ingredient ingredient2) {
      this.id = resourcelocation;
      this.template = ingredient;
      this.base = ingredient1;
      this.addition = ingredient2;
   }

   public boolean matches(Container container, Level level) {
      return this.template.test(container.getItem(0)) && this.base.test(container.getItem(1)) && this.addition.test(container.getItem(2));
   }

   public ItemStack assemble(Container container, RegistryAccess registryaccess) {
      ItemStack itemstack = container.getItem(1);
      if (this.base.test(itemstack)) {
         Optional<Holder.Reference<TrimMaterial>> optional = TrimMaterials.getFromIngredient(registryaccess, container.getItem(2));
         Optional<Holder.Reference<TrimPattern>> optional1 = TrimPatterns.getFromTemplate(registryaccess, container.getItem(0));
         if (optional.isPresent() && optional1.isPresent()) {
            Optional<ArmorTrim> optional2 = ArmorTrim.getTrim(registryaccess, itemstack);
            if (optional2.isPresent() && optional2.get().hasPatternAndMaterial(optional1.get(), optional.get())) {
               return ItemStack.EMPTY;
            }

            ItemStack itemstack1 = itemstack.copy();
            itemstack1.setCount(1);
            ArmorTrim armortrim = new ArmorTrim(optional.get(), optional1.get());
            if (ArmorTrim.setTrim(registryaccess, itemstack1, armortrim)) {
               return itemstack1;
            }
         }
      }

      return ItemStack.EMPTY;
   }

   public ItemStack getResultItem(RegistryAccess registryaccess) {
      ItemStack itemstack = new ItemStack(Items.IRON_CHESTPLATE);
      Optional<Holder.Reference<TrimPattern>> optional = registryaccess.registryOrThrow(Registries.TRIM_PATTERN).holders().findFirst();
      if (optional.isPresent()) {
         Optional<Holder.Reference<TrimMaterial>> optional1 = registryaccess.registryOrThrow(Registries.TRIM_MATERIAL).getHolder(TrimMaterials.REDSTONE);
         if (optional1.isPresent()) {
            ArmorTrim armortrim = new ArmorTrim(optional1.get(), optional.get());
            ArmorTrim.setTrim(registryaccess, itemstack, armortrim);
         }
      }

      return itemstack;
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
      return RecipeSerializer.SMITHING_TRIM;
   }

   public boolean isIncomplete() {
      return Stream.of(this.template, this.base, this.addition).anyMatch(Ingredient::isEmpty);
   }

   public static class Serializer implements RecipeSerializer<SmithingTrimRecipe> {
      public SmithingTrimRecipe fromJson(ResourceLocation resourcelocation, JsonObject jsonobject) {
         Ingredient ingredient = Ingredient.fromJson(GsonHelper.getNonNull(jsonobject, "template"));
         Ingredient ingredient1 = Ingredient.fromJson(GsonHelper.getNonNull(jsonobject, "base"));
         Ingredient ingredient2 = Ingredient.fromJson(GsonHelper.getNonNull(jsonobject, "addition"));
         return new SmithingTrimRecipe(resourcelocation, ingredient, ingredient1, ingredient2);
      }

      public SmithingTrimRecipe fromNetwork(ResourceLocation resourcelocation, FriendlyByteBuf friendlybytebuf) {
         Ingredient ingredient = Ingredient.fromNetwork(friendlybytebuf);
         Ingredient ingredient1 = Ingredient.fromNetwork(friendlybytebuf);
         Ingredient ingredient2 = Ingredient.fromNetwork(friendlybytebuf);
         return new SmithingTrimRecipe(resourcelocation, ingredient, ingredient1, ingredient2);
      }

      public void toNetwork(FriendlyByteBuf friendlybytebuf, SmithingTrimRecipe smithingtrimrecipe) {
         smithingtrimrecipe.template.toNetwork(friendlybytebuf);
         smithingtrimrecipe.base.toNetwork(friendlybytebuf);
         smithingtrimrecipe.addition.toNetwork(friendlybytebuf);
      }
   }
}
