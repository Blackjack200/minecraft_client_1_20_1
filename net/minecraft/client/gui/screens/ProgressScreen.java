package net.minecraft.client.gui.screens;

import javax.annotation.Nullable;
import net.minecraft.client.GameNarrator;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.network.chat.Component;
import net.minecraft.util.ProgressListener;

public class ProgressScreen extends Screen implements ProgressListener {
   @Nullable
   private Component header;
   @Nullable
   private Component stage;
   private int progress;
   private boolean stop;
   private final boolean clearScreenAfterStop;

   public ProgressScreen(boolean flag) {
      super(GameNarrator.NO_TITLE);
      this.clearScreenAfterStop = flag;
   }

   public boolean shouldCloseOnEsc() {
      return false;
   }

   protected boolean shouldNarrateNavigation() {
      return false;
   }

   public void progressStartNoAbort(Component component) {
      this.progressStart(component);
   }

   public void progressStart(Component component) {
      this.header = component;
      this.progressStage(Component.translatable("progress.working"));
   }

   public void progressStage(Component component) {
      this.stage = component;
      this.progressStagePercentage(0);
   }

   public void progressStagePercentage(int i) {
      this.progress = i;
   }

   public void stop() {
      this.stop = true;
   }

   public void render(GuiGraphics guigraphics, int i, int j, float f) {
      if (this.stop) {
         if (this.clearScreenAfterStop) {
            this.minecraft.setScreen((Screen)null);
         }

      } else {
         this.renderBackground(guigraphics);
         if (this.header != null) {
            guigraphics.drawCenteredString(this.font, this.header, this.width / 2, 70, 16777215);
         }

         if (this.stage != null && this.progress != 0) {
            guigraphics.drawCenteredString(this.font, Component.empty().append(this.stage).append(" " + this.progress + "%"), this.width / 2, 90, 16777215);
         }

         super.render(guigraphics, i, j, f);
      }
   }
}
