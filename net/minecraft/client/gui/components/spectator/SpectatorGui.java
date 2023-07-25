package net.minecraft.client.gui.components.spectator;

import com.mojang.blaze3d.systems.RenderSystem;
import javax.annotation.Nullable;
import net.minecraft.Util;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.spectator.SpectatorMenu;
import net.minecraft.client.gui.spectator.SpectatorMenuItem;
import net.minecraft.client.gui.spectator.SpectatorMenuListener;
import net.minecraft.client.gui.spectator.categories.SpectatorPage;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;

public class SpectatorGui implements SpectatorMenuListener {
   private static final ResourceLocation WIDGETS_LOCATION = new ResourceLocation("textures/gui/widgets.png");
   public static final ResourceLocation SPECTATOR_LOCATION = new ResourceLocation("textures/gui/spectator_widgets.png");
   private static final long FADE_OUT_DELAY = 5000L;
   private static final long FADE_OUT_TIME = 2000L;
   private final Minecraft minecraft;
   private long lastSelectionTime;
   @Nullable
   private SpectatorMenu menu;

   public SpectatorGui(Minecraft minecraft) {
      this.minecraft = minecraft;
   }

   public void onHotbarSelected(int i) {
      this.lastSelectionTime = Util.getMillis();
      if (this.menu != null) {
         this.menu.selectSlot(i);
      } else {
         this.menu = new SpectatorMenu(this);
      }

   }

   private float getHotbarAlpha() {
      long i = this.lastSelectionTime - Util.getMillis() + 5000L;
      return Mth.clamp((float)i / 2000.0F, 0.0F, 1.0F);
   }

   public void renderHotbar(GuiGraphics guigraphics) {
      if (this.menu != null) {
         float f = this.getHotbarAlpha();
         if (f <= 0.0F) {
            this.menu.exit();
         } else {
            int i = guigraphics.guiWidth() / 2;
            guigraphics.pose().pushPose();
            guigraphics.pose().translate(0.0F, 0.0F, -90.0F);
            int j = Mth.floor((float)guigraphics.guiHeight() - 22.0F * f);
            SpectatorPage spectatorpage = this.menu.getCurrentPage();
            this.renderPage(guigraphics, f, i, j, spectatorpage);
            guigraphics.pose().popPose();
         }
      }
   }

   protected void renderPage(GuiGraphics guigraphics, float f, int i, int j, SpectatorPage spectatorpage) {
      RenderSystem.enableBlend();
      guigraphics.setColor(1.0F, 1.0F, 1.0F, f);
      guigraphics.blit(WIDGETS_LOCATION, i - 91, j, 0, 0, 182, 22);
      if (spectatorpage.getSelectedSlot() >= 0) {
         guigraphics.blit(WIDGETS_LOCATION, i - 91 - 1 + spectatorpage.getSelectedSlot() * 20, j - 1, 0, 22, 24, 22);
      }

      guigraphics.setColor(1.0F, 1.0F, 1.0F, 1.0F);

      for(int k = 0; k < 9; ++k) {
         this.renderSlot(guigraphics, k, guigraphics.guiWidth() / 2 - 90 + k * 20 + 2, (float)(j + 3), f, spectatorpage.getItem(k));
      }

      RenderSystem.disableBlend();
   }

   private void renderSlot(GuiGraphics guigraphics, int i, int j, float f, float f1, SpectatorMenuItem spectatormenuitem) {
      if (spectatormenuitem != SpectatorMenu.EMPTY_SLOT) {
         int k = (int)(f1 * 255.0F);
         guigraphics.pose().pushPose();
         guigraphics.pose().translate((float)j, f, 0.0F);
         float f2 = spectatormenuitem.isEnabled() ? 1.0F : 0.25F;
         guigraphics.setColor(f2, f2, f2, f1);
         spectatormenuitem.renderIcon(guigraphics, f2, k);
         guigraphics.setColor(1.0F, 1.0F, 1.0F, 1.0F);
         guigraphics.pose().popPose();
         if (k > 3 && spectatormenuitem.isEnabled()) {
            Component component = this.minecraft.options.keyHotbarSlots[i].getTranslatedKeyMessage();
            guigraphics.drawString(this.minecraft.font, component, j + 19 - 2 - this.minecraft.font.width(component), (int)f + 6 + 3, 16777215 + (k << 24));
         }
      }

   }

   public void renderTooltip(GuiGraphics guigraphics) {
      int i = (int)(this.getHotbarAlpha() * 255.0F);
      if (i > 3 && this.menu != null) {
         SpectatorMenuItem spectatormenuitem = this.menu.getSelectedItem();
         Component component = spectatormenuitem == SpectatorMenu.EMPTY_SLOT ? this.menu.getSelectedCategory().getPrompt() : spectatormenuitem.getName();
         if (component != null) {
            int j = (guigraphics.guiWidth() - this.minecraft.font.width(component)) / 2;
            int k = guigraphics.guiHeight() - 35;
            guigraphics.drawString(this.minecraft.font, component, j, k, 16777215 + (i << 24));
         }
      }

   }

   public void onSpectatorMenuClosed(SpectatorMenu spectatormenu) {
      this.menu = null;
      this.lastSelectionTime = 0L;
   }

   public boolean isMenuActive() {
      return this.menu != null;
   }

   public void onMouseScrolled(int i) {
      int j;
      for(j = this.menu.getSelectedSlot() + i; j >= 0 && j <= 8 && (this.menu.getItem(j) == SpectatorMenu.EMPTY_SLOT || !this.menu.getItem(j).isEnabled()); j += i) {
      }

      if (j >= 0 && j <= 8) {
         this.menu.selectSlot(j);
         this.lastSelectionTime = Util.getMillis();
      }

   }

   public void onMouseMiddleClick() {
      this.lastSelectionTime = Util.getMillis();
      if (this.isMenuActive()) {
         int i = this.menu.getSelectedSlot();
         if (i != -1) {
            this.menu.selectSlot(i);
         }
      } else {
         this.menu = new SpectatorMenu(this);
      }

   }
}
