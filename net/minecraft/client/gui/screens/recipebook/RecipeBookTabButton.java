package net.minecraft.client.gui.screens.recipebook;

import com.mojang.blaze3d.systems.RenderSystem;
import java.util.List;
import net.minecraft.client.ClientRecipeBook;
import net.minecraft.client.Minecraft;
import net.minecraft.client.RecipeBookCategories;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.StateSwitchingButton;
import net.minecraft.client.renderer.entity.ItemRenderer;
import net.minecraft.world.inventory.RecipeBookMenu;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.Recipe;

public class RecipeBookTabButton extends StateSwitchingButton {
   private final RecipeBookCategories category;
   private static final float ANIMATION_TIME = 15.0F;
   private float animationTime;

   public RecipeBookTabButton(RecipeBookCategories recipebookcategories) {
      super(0, 0, 35, 27, false);
      this.category = recipebookcategories;
      this.initTextureValues(153, 2, 35, 0, RecipeBookComponent.RECIPE_BOOK_LOCATION);
   }

   public void startAnimation(Minecraft minecraft) {
      ClientRecipeBook clientrecipebook = minecraft.player.getRecipeBook();
      List<RecipeCollection> list = clientrecipebook.getCollection(this.category);
      if (minecraft.player.containerMenu instanceof RecipeBookMenu) {
         for(RecipeCollection recipecollection : list) {
            for(Recipe<?> recipe : recipecollection.getRecipes(clientrecipebook.isFiltering((RecipeBookMenu)minecraft.player.containerMenu))) {
               if (clientrecipebook.willHighlight(recipe)) {
                  this.animationTime = 15.0F;
                  return;
               }
            }
         }

      }
   }

   public void renderWidget(GuiGraphics guigraphics, int i, int j, float f) {
      if (this.animationTime > 0.0F) {
         float f1 = 1.0F + 0.1F * (float)Math.sin((double)(this.animationTime / 15.0F * (float)Math.PI));
         guigraphics.pose().pushPose();
         guigraphics.pose().translate((float)(this.getX() + 8), (float)(this.getY() + 12), 0.0F);
         guigraphics.pose().scale(1.0F, f1, 1.0F);
         guigraphics.pose().translate((float)(-(this.getX() + 8)), (float)(-(this.getY() + 12)), 0.0F);
      }

      Minecraft minecraft = Minecraft.getInstance();
      RenderSystem.disableDepthTest();
      int k = this.xTexStart;
      int l = this.yTexStart;
      if (this.isStateTriggered) {
         k += this.xDiffTex;
      }

      if (this.isHoveredOrFocused()) {
         l += this.yDiffTex;
      }

      int i1 = this.getX();
      if (this.isStateTriggered) {
         i1 -= 2;
      }

      guigraphics.blit(this.resourceLocation, i1, this.getY(), k, l, this.width, this.height);
      RenderSystem.enableDepthTest();
      this.renderIcon(guigraphics, minecraft.getItemRenderer());
      if (this.animationTime > 0.0F) {
         guigraphics.pose().popPose();
         this.animationTime -= f;
      }

   }

   private void renderIcon(GuiGraphics guigraphics, ItemRenderer itemrenderer) {
      List<ItemStack> list = this.category.getIconItems();
      int i = this.isStateTriggered ? -2 : 0;
      if (list.size() == 1) {
         guigraphics.renderFakeItem(list.get(0), this.getX() + 9 + i, this.getY() + 5);
      } else if (list.size() == 2) {
         guigraphics.renderFakeItem(list.get(0), this.getX() + 3 + i, this.getY() + 5);
         guigraphics.renderFakeItem(list.get(1), this.getX() + 14 + i, this.getY() + 5);
      }

   }

   public RecipeBookCategories getCategory() {
      return this.category;
   }

   public boolean updateVisibility(ClientRecipeBook clientrecipebook) {
      List<RecipeCollection> list = clientrecipebook.getCollection(this.category);
      this.visible = false;
      if (list != null) {
         for(RecipeCollection recipecollection : list) {
            if (recipecollection.hasKnownRecipes() && recipecollection.hasFitting()) {
               this.visible = true;
               break;
            }
         }
      }

      return this.visible;
   }
}
