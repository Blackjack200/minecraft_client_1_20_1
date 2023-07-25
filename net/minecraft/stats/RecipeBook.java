package net.minecraft.stats;

import com.google.common.collect.Sets;
import java.util.Set;
import javax.annotation.Nullable;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.inventory.RecipeBookMenu;
import net.minecraft.world.inventory.RecipeBookType;
import net.minecraft.world.item.crafting.Recipe;

public class RecipeBook {
   protected final Set<ResourceLocation> known = Sets.newHashSet();
   protected final Set<ResourceLocation> highlight = Sets.newHashSet();
   private final RecipeBookSettings bookSettings = new RecipeBookSettings();

   public void copyOverData(RecipeBook recipebook) {
      this.known.clear();
      this.highlight.clear();
      this.bookSettings.replaceFrom(recipebook.bookSettings);
      this.known.addAll(recipebook.known);
      this.highlight.addAll(recipebook.highlight);
   }

   public void add(Recipe<?> recipe) {
      if (!recipe.isSpecial()) {
         this.add(recipe.getId());
      }

   }

   protected void add(ResourceLocation resourcelocation) {
      this.known.add(resourcelocation);
   }

   public boolean contains(@Nullable Recipe<?> recipe) {
      return recipe == null ? false : this.known.contains(recipe.getId());
   }

   public boolean contains(ResourceLocation resourcelocation) {
      return this.known.contains(resourcelocation);
   }

   public void remove(Recipe<?> recipe) {
      this.remove(recipe.getId());
   }

   protected void remove(ResourceLocation resourcelocation) {
      this.known.remove(resourcelocation);
      this.highlight.remove(resourcelocation);
   }

   public boolean willHighlight(Recipe<?> recipe) {
      return this.highlight.contains(recipe.getId());
   }

   public void removeHighlight(Recipe<?> recipe) {
      this.highlight.remove(recipe.getId());
   }

   public void addHighlight(Recipe<?> recipe) {
      this.addHighlight(recipe.getId());
   }

   protected void addHighlight(ResourceLocation resourcelocation) {
      this.highlight.add(resourcelocation);
   }

   public boolean isOpen(RecipeBookType recipebooktype) {
      return this.bookSettings.isOpen(recipebooktype);
   }

   public void setOpen(RecipeBookType recipebooktype, boolean flag) {
      this.bookSettings.setOpen(recipebooktype, flag);
   }

   public boolean isFiltering(RecipeBookMenu<?> recipebookmenu) {
      return this.isFiltering(recipebookmenu.getRecipeBookType());
   }

   public boolean isFiltering(RecipeBookType recipebooktype) {
      return this.bookSettings.isFiltering(recipebooktype);
   }

   public void setFiltering(RecipeBookType recipebooktype, boolean flag) {
      this.bookSettings.setFiltering(recipebooktype, flag);
   }

   public void setBookSettings(RecipeBookSettings recipebooksettings) {
      this.bookSettings.replaceFrom(recipebooksettings);
   }

   public RecipeBookSettings getBookSettings() {
      return this.bookSettings.copy();
   }

   public void setBookSetting(RecipeBookType recipebooktype, boolean flag, boolean flag1) {
      this.bookSettings.setOpen(recipebooktype, flag);
      this.bookSettings.setFiltering(recipebooktype, flag1);
   }
}
