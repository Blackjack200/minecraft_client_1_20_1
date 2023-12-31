package net.minecraft.client.gui.screens;

import com.mojang.text2speech.Narrator;
import javax.annotation.Nullable;
import net.minecraft.client.Minecraft;
import net.minecraft.client.Options;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.AbstractWidget;
import net.minecraft.client.gui.components.AccessibilityOnboardingTextWidget;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.CommonButtons;
import net.minecraft.client.gui.components.LogoRenderer;
import net.minecraft.client.gui.layouts.FrameLayout;
import net.minecraft.client.gui.layouts.GridLayout;
import net.minecraft.client.renderer.PanoramaRenderer;
import net.minecraft.network.chat.CommonComponents;
import net.minecraft.network.chat.Component;

public class AccessibilityOnboardingScreen extends Screen {
   private static final Component ONBOARDING_NARRATOR_MESSAGE = Component.translatable("accessibility.onboarding.screen.narrator");
   private static final int PADDING = 4;
   private static final int TITLE_PADDING = 16;
   private final PanoramaRenderer panorama = new PanoramaRenderer(TitleScreen.CUBE_MAP);
   private final LogoRenderer logoRenderer;
   private final Options options;
   private final boolean narratorAvailable;
   private boolean hasNarrated;
   private float timer;
   @Nullable
   private AccessibilityOnboardingTextWidget textWidget;

   public AccessibilityOnboardingScreen(Options options) {
      super(Component.translatable("accessibility.onboarding.screen.title"));
      this.options = options;
      this.logoRenderer = new LogoRenderer(true);
      this.narratorAvailable = Minecraft.getInstance().getNarrator().isActive();
   }

   public void init() {
      int i = this.initTitleYPos();
      FrameLayout framelayout = new FrameLayout(this.width, this.height - i);
      framelayout.defaultChildLayoutSetting().alignVerticallyTop().padding(4);
      GridLayout gridlayout = framelayout.addChild(new GridLayout());
      gridlayout.defaultCellSetting().alignHorizontallyCenter().padding(4);
      GridLayout.RowHelper gridlayout_rowhelper = gridlayout.createRowHelper(1);
      gridlayout_rowhelper.defaultCellSetting().padding(2);
      this.textWidget = new AccessibilityOnboardingTextWidget(this.font, this.title, this.width);
      gridlayout_rowhelper.addChild(this.textWidget, gridlayout_rowhelper.newCellSettings().paddingBottom(16));
      AbstractWidget abstractwidget = this.options.narrator().createButton(this.options, 0, 0, 150);
      abstractwidget.active = this.narratorAvailable;
      gridlayout_rowhelper.addChild(abstractwidget);
      if (this.narratorAvailable) {
         this.setInitialFocus(abstractwidget);
      }

      gridlayout_rowhelper.addChild(CommonButtons.accessibilityTextAndImage((button2) -> this.closeAndSetScreen(new AccessibilityOptionsScreen(this, this.minecraft.options))));
      gridlayout_rowhelper.addChild(CommonButtons.languageTextAndImage((button1) -> this.closeAndSetScreen(new LanguageSelectScreen(this, this.minecraft.options, this.minecraft.getLanguageManager()))));
      framelayout.addChild(Button.builder(CommonComponents.GUI_CONTINUE, (button) -> this.onClose()).build(), framelayout.newChildLayoutSettings().alignVerticallyBottom().padding(8));
      framelayout.arrangeElements();
      FrameLayout.alignInRectangle(framelayout, 0, i, this.width, this.height, 0.5F, 0.0F);
      framelayout.visitWidgets(this::addRenderableWidget);
   }

   private int initTitleYPos() {
      return 90;
   }

   public void onClose() {
      this.closeAndSetScreen(new TitleScreen(true, this.logoRenderer));
   }

   private void closeAndSetScreen(Screen screen) {
      this.options.onboardAccessibility = false;
      this.options.save();
      Narrator.getNarrator().clear();
      this.minecraft.setScreen(screen);
   }

   public void render(GuiGraphics guigraphics, int i, int j, float f) {
      this.handleInitialNarrationDelay();
      this.panorama.render(0.0F, 1.0F);
      guigraphics.fill(0, 0, this.width, this.height, -1877995504);
      this.logoRenderer.renderLogo(guigraphics, this.width, 1.0F);
      if (this.textWidget != null) {
         this.textWidget.render(guigraphics, i, j, f);
      }

      super.render(guigraphics, i, j, f);
   }

   private void handleInitialNarrationDelay() {
      if (!this.hasNarrated && this.narratorAvailable) {
         if (this.timer < 40.0F) {
            ++this.timer;
         } else if (this.minecraft.isWindowActive()) {
            Narrator.getNarrator().say(ONBOARDING_NARRATOR_MESSAGE.getString(), true);
            this.hasNarrated = true;
         }
      }

   }
}
