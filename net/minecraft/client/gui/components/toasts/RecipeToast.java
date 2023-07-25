package net.minecraft.client.gui.components.toasts;

import com.google.common.collect.Lists;
import java.util.List;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.Recipe;

public class RecipeToast implements Toast {
   private static final long DISPLAY_TIME = 5000L;
   private static final Component TITLE_TEXT = Component.translatable("recipe.toast.title");
   private static final Component DESCRIPTION_TEXT = Component.translatable("recipe.toast.description");
   private final List<Recipe<?>> recipes = Lists.newArrayList();
   private long lastChanged;
   private boolean changed;

   public RecipeToast(Recipe<?> recipe) {
      this.recipes.add(recipe);
   }

   public Toast.Visibility render(GuiGraphics guigraphics, ToastComponent toastcomponent, long i) {
      if (this.changed) {
         this.lastChanged = i;
         this.changed = false;
      }

      if (this.recipes.isEmpty()) {
         return Toast.Visibility.HIDE;
      } else {
         guigraphics.blit(TEXTURE, 0, 0, 0, 32, this.width(), this.height());
         guigraphics.drawString(toastcomponent.getMinecraft().font, TITLE_TEXT, 30, 7, -11534256, false);
         guigraphics.drawString(toastcomponent.getMinecraft().font, DESCRIPTION_TEXT, 30, 18, -16777216, false);
         Recipe<?> recipe = this.recipes.get((int)((double)i / Math.max(1.0D, 5000.0D * toastcomponent.getNotificationDisplayTimeMultiplier() / (double)this.recipes.size()) % (double)this.recipes.size()));
         ItemStack itemstack = recipe.getToastSymbol();
         guigraphics.pose().pushPose();
         guigraphics.pose().scale(0.6F, 0.6F, 1.0F);
         guigraphics.renderFakeItem(itemstack, 3, 3);
         guigraphics.pose().popPose();
         guigraphics.renderFakeItem(recipe.getResultItem(toastcomponent.getMinecraft().level.registryAccess()), 8, 8);
         return (double)(i - this.lastChanged) >= 5000.0D * toastcomponent.getNotificationDisplayTimeMultiplier() ? Toast.Visibility.HIDE : Toast.Visibility.SHOW;
      }
   }

   private void addItem(Recipe<?> recipe) {
      this.recipes.add(recipe);
      this.changed = true;
   }

   public static void addOrUpdate(ToastComponent toastcomponent, Recipe<?> recipe) {
      RecipeToast recipetoast = toastcomponent.getToast(RecipeToast.class, NO_TOKEN);
      if (recipetoast == null) {
         toastcomponent.addToast(new RecipeToast(recipe));
      } else {
         recipetoast.addItem(recipe);
      }

   }
}
