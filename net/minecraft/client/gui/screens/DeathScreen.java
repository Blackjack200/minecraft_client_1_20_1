package net.minecraft.client.gui.screens;

import com.google.common.collect.Lists;
import it.unimi.dsi.fastutil.booleans.BooleanConsumer;
import java.util.List;
import javax.annotation.Nullable;
import net.minecraft.ChatFormatting;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.AbstractWidget;
import net.minecraft.client.gui.components.Button;
import net.minecraft.network.chat.ClickEvent;
import net.minecraft.network.chat.CommonComponents;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.Style;

public class DeathScreen extends Screen {
   private int delayTicker;
   private final Component causeOfDeath;
   private final boolean hardcore;
   private Component deathScore;
   private final List<Button> exitButtons = Lists.newArrayList();
   @Nullable
   private Button exitToTitleButton;

   public DeathScreen(@Nullable Component component, boolean flag) {
      super(Component.translatable(flag ? "deathScreen.title.hardcore" : "deathScreen.title"));
      this.causeOfDeath = component;
      this.hardcore = flag;
   }

   protected void init() {
      this.delayTicker = 0;
      this.exitButtons.clear();
      Component component = this.hardcore ? Component.translatable("deathScreen.spectate") : Component.translatable("deathScreen.respawn");
      this.exitButtons.add(this.addRenderableWidget(Button.builder(component, (button1) -> {
         this.minecraft.player.respawn();
         button1.active = false;
      }).bounds(this.width / 2 - 100, this.height / 4 + 72, 200, 20).build()));
      this.exitToTitleButton = this.addRenderableWidget(Button.builder(Component.translatable("deathScreen.titleScreen"), (button) -> this.minecraft.getReportingContext().draftReportHandled(this.minecraft, this, this::handleExitToTitleScreen, true)).bounds(this.width / 2 - 100, this.height / 4 + 96, 200, 20).build());
      this.exitButtons.add(this.exitToTitleButton);
      this.setButtonsActive(false);
      this.deathScore = Component.translatable("deathScreen.score").append(": ").append(Component.literal(Integer.toString(this.minecraft.player.getScore())).withStyle(ChatFormatting.YELLOW));
   }

   public boolean shouldCloseOnEsc() {
      return false;
   }

   private void handleExitToTitleScreen() {
      if (this.hardcore) {
         this.exitToTitleScreen();
      } else {
         ConfirmScreen confirmscreen = new DeathScreen.TitleConfirmScreen((flag) -> {
            if (flag) {
               this.exitToTitleScreen();
            } else {
               this.minecraft.player.respawn();
               this.minecraft.setScreen((Screen)null);
            }

         }, Component.translatable("deathScreen.quit.confirm"), CommonComponents.EMPTY, Component.translatable("deathScreen.titleScreen"), Component.translatable("deathScreen.respawn"));
         this.minecraft.setScreen(confirmscreen);
         confirmscreen.setDelay(20);
      }
   }

   private void exitToTitleScreen() {
      if (this.minecraft.level != null) {
         this.minecraft.level.disconnect();
      }

      this.minecraft.clearLevel(new GenericDirtMessageScreen(Component.translatable("menu.savingLevel")));
      this.minecraft.setScreen(new TitleScreen());
   }

   public void render(GuiGraphics guigraphics, int i, int j, float f) {
      guigraphics.fillGradient(0, 0, this.width, this.height, 1615855616, -1602211792);
      guigraphics.pose().pushPose();
      guigraphics.pose().scale(2.0F, 2.0F, 2.0F);
      guigraphics.drawCenteredString(this.font, this.title, this.width / 2 / 2, 30, 16777215);
      guigraphics.pose().popPose();
      if (this.causeOfDeath != null) {
         guigraphics.drawCenteredString(this.font, this.causeOfDeath, this.width / 2, 85, 16777215);
      }

      guigraphics.drawCenteredString(this.font, this.deathScore, this.width / 2, 100, 16777215);
      if (this.causeOfDeath != null && j > 85 && j < 85 + 9) {
         Style style = this.getClickedComponentStyleAt(i);
         guigraphics.renderComponentHoverEffect(this.font, style, i, j);
      }

      super.render(guigraphics, i, j, f);
      if (this.exitToTitleButton != null && this.minecraft.getReportingContext().hasDraftReport()) {
         guigraphics.blit(AbstractWidget.WIDGETS_LOCATION, this.exitToTitleButton.getX() + this.exitToTitleButton.getWidth() - 17, this.exitToTitleButton.getY() + 3, 182, 24, 15, 15);
      }

   }

   @Nullable
   private Style getClickedComponentStyleAt(int i) {
      if (this.causeOfDeath == null) {
         return null;
      } else {
         int j = this.minecraft.font.width(this.causeOfDeath);
         int k = this.width / 2 - j / 2;
         int l = this.width / 2 + j / 2;
         return i >= k && i <= l ? this.minecraft.font.getSplitter().componentStyleAtWidth(this.causeOfDeath, i - k) : null;
      }
   }

   public boolean mouseClicked(double d0, double d1, int i) {
      if (this.causeOfDeath != null && d1 > 85.0D && d1 < (double)(85 + 9)) {
         Style style = this.getClickedComponentStyleAt((int)d0);
         if (style != null && style.getClickEvent() != null && style.getClickEvent().getAction() == ClickEvent.Action.OPEN_URL) {
            this.handleComponentClicked(style);
            return false;
         }
      }

      return super.mouseClicked(d0, d1, i);
   }

   public boolean isPauseScreen() {
      return false;
   }

   public void tick() {
      super.tick();
      ++this.delayTicker;
      if (this.delayTicker == 20) {
         this.setButtonsActive(true);
      }

   }

   private void setButtonsActive(boolean flag) {
      for(Button button : this.exitButtons) {
         button.active = flag;
      }

   }

   public static class TitleConfirmScreen extends ConfirmScreen {
      public TitleConfirmScreen(BooleanConsumer booleanconsumer, Component component, Component component1, Component component2, Component component3) {
         super(booleanconsumer, component, component1, component2, component3);
      }
   }
}
