package net.minecraft.client.gui.screens;

import net.minecraft.Util;
import net.minecraft.client.OptionInstance;
import net.minecraft.client.Options;
import net.minecraft.client.gui.components.AbstractWidget;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.Tooltip;
import net.minecraft.network.chat.CommonComponents;
import net.minecraft.network.chat.Component;

public class AccessibilityOptionsScreen extends SimpleOptionsSubScreen {
   private static OptionInstance<?>[] options(Options options) {
      return new OptionInstance[]{options.narrator(), options.showSubtitles(), options.highContrast(), options.autoJump(), options.textBackgroundOpacity(), options.backgroundForChatOnly(), options.chatOpacity(), options.chatLineSpacing(), options.chatDelay(), options.notificationDisplayTime(), options.toggleCrouch(), options.toggleSprint(), options.screenEffectScale(), options.fovEffectScale(), options.darknessEffectScale(), options.damageTiltStrength(), options.glintSpeed(), options.glintStrength(), options.hideLightningFlash(), options.darkMojangStudiosBackground(), options.panoramaSpeed()};
   }

   public AccessibilityOptionsScreen(Screen screen, Options options) {
      super(screen, options, Component.translatable("options.accessibility.title"), options(options));
   }

   protected void init() {
      super.init();
      AbstractWidget abstractwidget = this.list.findOption(this.options.highContrast());
      if (abstractwidget != null && !this.minecraft.getResourcePackRepository().getAvailableIds().contains("high_contrast")) {
         abstractwidget.active = false;
         abstractwidget.setTooltip(Tooltip.create(Component.translatable("options.accessibility.high_contrast.error.tooltip")));
      }

   }

   protected void createFooter() {
      this.addRenderableWidget(Button.builder(Component.translatable("options.accessibility.link"), (button1) -> this.minecraft.setScreen(new ConfirmLinkScreen((flag) -> {
            if (flag) {
               Util.getPlatform().openUri("https://aka.ms/MinecraftJavaAccessibility");
            }

            this.minecraft.setScreen(this);
         }, "https://aka.ms/MinecraftJavaAccessibility", true))).bounds(this.width / 2 - 155, this.height - 27, 150, 20).build());
      this.addRenderableWidget(Button.builder(CommonComponents.GUI_DONE, (button) -> this.minecraft.setScreen(this.lastScreen)).bounds(this.width / 2 + 5, this.height - 27, 150, 20).build());
   }
}
