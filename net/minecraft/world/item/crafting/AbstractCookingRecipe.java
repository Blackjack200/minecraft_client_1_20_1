package net.minecraft.world.item.crafting;

import net.minecraft.core.NonNullList;
import net.minecraft.core.RegistryAccess;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.Container;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;

public abstract class AbstractCookingRecipe implements Recipe<Container> {
   protected final RecipeType<?> type;
   protected final ResourceLocation id;
   private final CookingBookCategory category;
   protected final String group;
   protected final Ingredient ingredient;
   protected final ItemStack result;
   protected final float experience;
   protected final int cookingTime;

   public AbstractCookingRecipe(RecipeType<?> recipetype, ResourceLocation resourcelocation, String s, CookingBookCategory cookingbookcategory, Ingredient ingredient, ItemStack itemstack, float f, int i) {
      this.type = recipetype;
      this.category = cookingbookcategory;
      this.id = resourcelocation;
      this.group = s;
      this.ingredient = ingredient;
      this.result = itemstack;
      this.experience = f;
      this.cookingTime = i;
   }

   public boolean matches(Container container, Level level) {
      return this.ingredient.test(container.getItem(0));
   }

   public ItemStack assemble(Container container, RegistryAccess registryaccess) {
      return this.result.copy();
   }

   public boolean canCraftInDimensions(int i, int j) {
      return true;
   }

   public NonNullList<Ingredient> getIngredients() {
      NonNullList<Ingredient> nonnulllist = NonNullList.create();
      nonnulllist.add(this.ingredient);
      return nonnulllist;
   }

   public float getExperience() {
      return this.experience;
   }

   public ItemStack getResultItem(RegistryAccess registryaccess) {
      return this.result;
   }

   public String getGroup() {
      return this.group;
   }

   public int getCookingTime() {
      return this.cookingTime;
   }

   public ResourceLocation getId() {
      return this.id;
   }

   public RecipeType<?> getType() {
      return this.type;
   }

   public CookingBookCategory category() {
      return this.category;
   }
}
