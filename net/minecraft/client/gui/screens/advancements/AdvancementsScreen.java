package net.minecraft.client.gui.screens.advancements;

import com.google.common.collect.Maps;
import com.mojang.blaze3d.systems.RenderSystem;
import java.util.Map;
import javax.annotation.Nullable;
import net.minecraft.advancements.Advancement;
import net.minecraft.advancements.AdvancementProgress;
import net.minecraft.client.GameNarrator;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.multiplayer.ClientAdvancements;
import net.minecraft.client.multiplayer.ClientPacketListener;
import net.minecraft.network.chat.Component;
import net.minecraft.network.protocol.game.ServerboundSeenAdvancementsPacket;
import net.minecraft.resources.ResourceLocation;

public class AdvancementsScreen extends Screen implements ClientAdvancements.Listener {
   private static final ResourceLocation WINDOW_LOCATION = new ResourceLocation("textures/gui/advancements/window.png");
   public static final ResourceLocation TABS_LOCATION = new ResourceLocation("textures/gui/advancements/tabs.png");
   public static final int WINDOW_WIDTH = 252;
   public static final int WINDOW_HEIGHT = 140;
   private static final int WINDOW_INSIDE_X = 9;
   private static final int WINDOW_INSIDE_Y = 18;
   public static final int WINDOW_INSIDE_WIDTH = 234;
   public static final int WINDOW_INSIDE_HEIGHT = 113;
   private static final int WINDOW_TITLE_X = 8;
   private static final int WINDOW_TITLE_Y = 6;
   public static final int BACKGROUND_TILE_WIDTH = 16;
   public static final int BACKGROUND_TILE_HEIGHT = 16;
   public static final int BACKGROUND_TILE_COUNT_X = 14;
   public static final int BACKGROUND_TILE_COUNT_Y = 7;
   private static final Component VERY_SAD_LABEL = Component.translatable("advancements.sad_label");
   private static final Component NO_ADVANCEMENTS_LABEL = Component.translatable("advancements.empty");
   private static final Component TITLE = Component.translatable("gui.advancements");
   private final ClientAdvancements advancements;
   private final Map<Advancement, AdvancementTab> tabs = Maps.newLinkedHashMap();
   @Nullable
   private AdvancementTab selectedTab;
   private boolean isScrolling;

   public AdvancementsScreen(ClientAdvancements clientadvancements) {
      super(GameNarrator.NO_TITLE);
      this.advancements = clientadvancements;
   }

   protected void init() {
      this.tabs.clear();
      this.selectedTab = null;
      this.advancements.setListener(this);
      if (this.selectedTab == null && !this.tabs.isEmpty()) {
         this.advancements.setSelectedTab(this.tabs.values().iterator().next().getAdvancement(), true);
      } else {
         this.advancements.setSelectedTab(this.selectedTab == null ? null : this.selectedTab.getAdvancement(), true);
      }

   }

   public void removed() {
      this.advancements.setListener((ClientAdvancements.Listener)null);
      ClientPacketListener clientpacketlistener = this.minecraft.getConnection();
      if (clientpacketlistener != null) {
         clientpacketlistener.send(ServerboundSeenAdvancementsPacket.closedScreen());
      }

   }

   public boolean mouseClicked(double d0, double d1, int i) {
      if (i == 0) {
         int j = (this.width - 252) / 2;
         int k = (this.height - 140) / 2;

         for(AdvancementTab advancementtab : this.tabs.values()) {
            if (advancementtab.isMouseOver(j, k, d0, d1)) {
               this.advancements.setSelectedTab(advancementtab.getAdvancement(), true);
               break;
            }
         }
      }

      return super.mouseClicked(d0, d1, i);
   }

   public boolean keyPressed(int i, int j, int k) {
      if (this.minecraft.options.keyAdvancements.matches(i, j)) {
         this.minecraft.setScreen((Screen)null);
         this.minecraft.mouseHandler.grabMouse();
         return true;
      } else {
         return super.keyPressed(i, j, k);
      }
   }

   public void render(GuiGraphics guigraphics, int i, int j, float f) {
      int k = (this.width - 252) / 2;
      int l = (this.height - 140) / 2;
      this.renderBackground(guigraphics);
      this.renderInside(guigraphics, i, j, k, l);
      this.renderWindow(guigraphics, k, l);
      this.renderTooltips(guigraphics, i, j, k, l);
   }

   public boolean mouseDragged(double d0, double d1, int i, double d2, double d3) {
      if (i != 0) {
         this.isScrolling = false;
         return false;
      } else {
         if (!this.isScrolling) {
            this.isScrolling = true;
         } else if (this.selectedTab != null) {
            this.selectedTab.scroll(d2, d3);
         }

         return true;
      }
   }

   private void renderInside(GuiGraphics guigraphics, int i, int j, int k, int l) {
      AdvancementTab advancementtab = this.selectedTab;
      if (advancementtab == null) {
         guigraphics.fill(k + 9, l + 18, k + 9 + 234, l + 18 + 113, -16777216);
         int i1 = k + 9 + 117;
         guigraphics.drawCenteredString(this.font, NO_ADVANCEMENTS_LABEL, i1, l + 18 + 56 - 9 / 2, -1);
         guigraphics.drawCenteredString(this.font, VERY_SAD_LABEL, i1, l + 18 + 113 - 9, -1);
      } else {
         advancementtab.drawContents(guigraphics, k + 9, l + 18);
      }
   }

   public void renderWindow(GuiGraphics guigraphics, int i, int j) {
      RenderSystem.enableBlend();
      guigraphics.blit(WINDOW_LOCATION, i, j, 0, 0, 252, 140);
      if (this.tabs.size() > 1) {
         for(AdvancementTab advancementtab : this.tabs.values()) {
            advancementtab.drawTab(guigraphics, i, j, advancementtab == this.selectedTab);
         }

         for(AdvancementTab advancementtab1 : this.tabs.values()) {
            advancementtab1.drawIcon(guigraphics, i, j);
         }
      }

      guigraphics.drawString(this.font, TITLE, i + 8, j + 6, 4210752, false);
   }

   private void renderTooltips(GuiGraphics guigraphics, int i, int j, int k, int l) {
      if (this.selectedTab != null) {
         guigraphics.pose().pushPose();
         guigraphics.pose().translate((float)(k + 9), (float)(l + 18), 400.0F);
         RenderSystem.enableDepthTest();
         this.selectedTab.drawTooltips(guigraphics, i - k - 9, j - l - 18, k, l);
         RenderSystem.disableDepthTest();
         guigraphics.pose().popPose();
      }

      if (this.tabs.size() > 1) {
         for(AdvancementTab advancementtab : this.tabs.values()) {
            if (advancementtab.isMouseOver(k, l, (double)i, (double)j)) {
               guigraphics.renderTooltip(this.font, advancementtab.getTitle(), i, j);
            }
         }
      }

   }

   public void onAddAdvancementRoot(Advancement advancement) {
      AdvancementTab advancementtab = AdvancementTab.create(this.minecraft, this, this.tabs.size(), advancement);
      if (advancementtab != null) {
         this.tabs.put(advancement, advancementtab);
      }
   }

   public void onRemoveAdvancementRoot(Advancement advancement) {
   }

   public void onAddAdvancementTask(Advancement advancement) {
      AdvancementTab advancementtab = this.getTab(advancement);
      if (advancementtab != null) {
         advancementtab.addAdvancement(advancement);
      }

   }

   public void onRemoveAdvancementTask(Advancement advancement) {
   }

   public void onUpdateAdvancementProgress(Advancement advancement, AdvancementProgress advancementprogress) {
      AdvancementWidget advancementwidget = this.getAdvancementWidget(advancement);
      if (advancementwidget != null) {
         advancementwidget.setProgress(advancementprogress);
      }

   }

   public void onSelectedTabChanged(@Nullable Advancement advancement) {
      this.selectedTab = this.tabs.get(advancement);
   }

   public void onAdvancementsCleared() {
      this.tabs.clear();
      this.selectedTab = null;
   }

   @Nullable
   public AdvancementWidget getAdvancementWidget(Advancement advancement) {
      AdvancementTab advancementtab = this.getTab(advancement);
      return advancementtab == null ? null : advancementtab.getWidget(advancement);
   }

   @Nullable
   private AdvancementTab getTab(Advancement advancement) {
      while(advancement.getParent() != null) {
         advancement = advancement.getParent();
      }

      return this.tabs.get(advancement);
   }
}
