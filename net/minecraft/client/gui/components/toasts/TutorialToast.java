package net.minecraft.client.gui.components.toasts;

import com.mojang.blaze3d.systems.RenderSystem;
import javax.annotation.Nullable;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.network.chat.Component;
import net.minecraft.util.Mth;

public class TutorialToast implements Toast {
   public static final int PROGRESS_BAR_WIDTH = 154;
   public static final int PROGRESS_BAR_HEIGHT = 1;
   public static final int PROGRESS_BAR_X = 3;
   public static final int PROGRESS_BAR_Y = 28;
   private final TutorialToast.Icons icon;
   private final Component title;
   @Nullable
   private final Component message;
   private Toast.Visibility visibility = Toast.Visibility.SHOW;
   private long lastProgressTime;
   private float lastProgress;
   private float progress;
   private final boolean progressable;

   public TutorialToast(TutorialToast.Icons tutorialtoast_icons, Component component, @Nullable Component component1, boolean flag) {
      this.icon = tutorialtoast_icons;
      this.title = component;
      this.message = component1;
      this.progressable = flag;
   }

   public Toast.Visibility render(GuiGraphics guigraphics, ToastComponent toastcomponent, long i) {
      guigraphics.blit(TEXTURE, 0, 0, 0, 96, this.width(), this.height());
      this.icon.render(guigraphics, 6, 6);
      if (this.message == null) {
         guigraphics.drawString(toastcomponent.getMinecraft().font, this.title, 30, 12, -11534256, false);
      } else {
         guigraphics.drawString(toastcomponent.getMinecraft().font, this.title, 30, 7, -11534256, false);
         guigraphics.drawString(toastcomponent.getMinecraft().font, this.message, 30, 18, -16777216, false);
      }

      if (this.progressable) {
         guigraphics.fill(3, 28, 157, 29, -1);
         float f = Mth.clampedLerp(this.lastProgress, this.progress, (float)(i - this.lastProgressTime) / 100.0F);
         int j;
         if (this.progress >= this.lastProgress) {
            j = -16755456;
         } else {
            j = -11206656;
         }

         guigraphics.fill(3, 28, (int)(3.0F + 154.0F * f), 29, j);
         this.lastProgress = f;
         this.lastProgressTime = i;
      }

      return this.visibility;
   }

   public void hide() {
      this.visibility = Toast.Visibility.HIDE;
   }

   public void updateProgress(float f) {
      this.progress = f;
   }

   public static enum Icons {
      MOVEMENT_KEYS(0, 0),
      MOUSE(1, 0),
      TREE(2, 0),
      RECIPE_BOOK(0, 1),
      WOODEN_PLANKS(1, 1),
      SOCIAL_INTERACTIONS(2, 1),
      RIGHT_CLICK(3, 1);

      private final int x;
      private final int y;

      private Icons(int i, int j) {
         this.x = i;
         this.y = j;
      }

      public void render(GuiGraphics guigraphics, int i, int j) {
         RenderSystem.enableBlend();
         guigraphics.blit(Toast.TEXTURE, i, j, 176 + this.x * 20, this.y * 20, 20, 20);
      }
   }
}
