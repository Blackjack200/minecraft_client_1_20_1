package net.minecraft.client.gui.screens.recipebook;

import com.google.common.collect.Lists;
import java.util.List;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.AbstractWidget;
import net.minecraft.client.gui.narration.NarratedElementType;
import net.minecraft.client.gui.narration.NarrationElementOutput;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.CommonComponents;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.stats.RecipeBook;
import net.minecraft.util.Mth;
import net.minecraft.world.inventory.RecipeBookMenu;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.Recipe;

public class RecipeButton extends AbstractWidget {
   private static final ResourceLocation RECIPE_BOOK_LOCATION = new ResourceLocation("textures/gui/recipe_book.png");
   private static final float ANIMATION_TIME = 15.0F;
   private static final int BACKGROUND_SIZE = 25;
   public static final int TICKS_TO_SWAP = 30;
   private static final Component MORE_RECIPES_TOOLTIP = Component.translatable("gui.recipebook.moreRecipes");
   private RecipeBookMenu<?> menu;
   private RecipeBook book;
   private RecipeCollection collection;
   private float time;
   private float animationTime;
   private int currentIndex;

   public RecipeButton() {
      super(0, 0, 25, 25, CommonComponents.EMPTY);
   }

   public void init(RecipeCollection recipecollection, RecipeBookPage recipebookpage) {
      this.collection = recipecollection;
      this.menu = (RecipeBookMenu)recipebookpage.getMinecraft().player.containerMenu;
      this.book = recipebookpage.getRecipeBook();
      List<Recipe<?>> list = recipecollection.getRecipes(this.book.isFiltering(this.menu));

      for(Recipe<?> recipe : list) {
         if (this.book.willHighlight(recipe)) {
            recipebookpage.recipesShown(list);
            this.animationTime = 15.0F;
            break;
         }
      }

   }

   public RecipeCollection getCollection() {
      return this.collection;
   }

   public void renderWidget(GuiGraphics guigraphics, int i, int j, float f) {
      if (!Screen.hasControlDown()) {
         this.time += f;
      }

      Minecraft minecraft = Minecraft.getInstance();
      int k = 29;
      if (!this.collection.hasCraftable()) {
         k += 25;
      }

      int l = 206;
      if (this.collection.getRecipes(this.book.isFiltering(this.menu)).size() > 1) {
         l += 25;
      }

      boolean flag = this.animationTime > 0.0F;
      if (flag) {
         float f1 = 1.0F + 0.1F * (float)Math.sin((double)(this.animationTime / 15.0F * (float)Math.PI));
         guigraphics.pose().pushPose();
         guigraphics.pose().translate((float)(this.getX() + 8), (float)(this.getY() + 12), 0.0F);
         guigraphics.pose().scale(f1, f1, 1.0F);
         guigraphics.pose().translate((float)(-(this.getX() + 8)), (float)(-(this.getY() + 12)), 0.0F);
         this.animationTime -= f;
      }

      guigraphics.blit(RECIPE_BOOK_LOCATION, this.getX(), this.getY(), k, l, this.width, this.height);
      List<Recipe<?>> list = this.getOrderedRecipes();
      this.currentIndex = Mth.floor(this.time / 30.0F) % list.size();
      ItemStack itemstack = list.get(this.currentIndex).getResultItem(this.collection.registryAccess());
      int i1 = 4;
      if (this.collection.hasSingleResultItem() && this.getOrderedRecipes().size() > 1) {
         guigraphics.renderItem(itemstack, this.getX() + i1 + 1, this.getY() + i1 + 1, 0, 10);
         --i1;
      }

      guigraphics.renderFakeItem(itemstack, this.getX() + i1, this.getY() + i1);
      if (flag) {
         guigraphics.pose().popPose();
      }

   }

   private List<Recipe<?>> getOrderedRecipes() {
      List<Recipe<?>> list = this.collection.getDisplayRecipes(true);
      if (!this.book.isFiltering(this.menu)) {
         list.addAll(this.collection.getDisplayRecipes(false));
      }

      return list;
   }

   public boolean isOnlyOption() {
      return this.getOrderedRecipes().size() == 1;
   }

   public Recipe<?> getRecipe() {
      List<Recipe<?>> list = this.getOrderedRecipes();
      return list.get(this.currentIndex);
   }

   public List<Component> getTooltipText() {
      ItemStack itemstack = this.getOrderedRecipes().get(this.currentIndex).getResultItem(this.collection.registryAccess());
      List<Component> list = Lists.newArrayList(Screen.getTooltipFromItem(Minecraft.getInstance(), itemstack));
      if (this.collection.getRecipes(this.book.isFiltering(this.menu)).size() > 1) {
         list.add(MORE_RECIPES_TOOLTIP);
      }

      return list;
   }

   public void updateWidgetNarration(NarrationElementOutput narrationelementoutput) {
      ItemStack itemstack = this.getOrderedRecipes().get(this.currentIndex).getResultItem(this.collection.registryAccess());
      narrationelementoutput.add(NarratedElementType.TITLE, (Component)Component.translatable("narration.recipe", itemstack.getHoverName()));
      if (this.collection.getRecipes(this.book.isFiltering(this.menu)).size() > 1) {
         narrationelementoutput.add(NarratedElementType.USAGE, Component.translatable("narration.button.usage.hovered"), Component.translatable("narration.recipe.usage.more"));
      } else {
         narrationelementoutput.add(NarratedElementType.USAGE, (Component)Component.translatable("narration.button.usage.hovered"));
      }

   }

   public int getWidth() {
      return 25;
   }

   protected boolean isValidClickButton(int i) {
      return i == 0 || i == 1;
   }
}
