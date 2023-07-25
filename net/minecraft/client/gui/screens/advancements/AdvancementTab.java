package net.minecraft.client.gui.screens.advancements;

import com.google.common.collect.Maps;
import java.util.Map;
import java.util.Objects;
import javax.annotation.Nullable;
import net.minecraft.advancements.Advancement;
import net.minecraft.advancements.DisplayInfo;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.renderer.texture.TextureManager;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;
import net.minecraft.world.item.ItemStack;

public class AdvancementTab {
   private final Minecraft minecraft;
   private final AdvancementsScreen screen;
   private final AdvancementTabType type;
   private final int index;
   private final Advancement advancement;
   private final DisplayInfo display;
   private final ItemStack icon;
   private final Component title;
   private final AdvancementWidget root;
   private final Map<Advancement, AdvancementWidget> widgets = Maps.newLinkedHashMap();
   private double scrollX;
   private double scrollY;
   private int minX = Integer.MAX_VALUE;
   private int minY = Integer.MAX_VALUE;
   private int maxX = Integer.MIN_VALUE;
   private int maxY = Integer.MIN_VALUE;
   private float fade;
   private boolean centered;

   public AdvancementTab(Minecraft minecraft, AdvancementsScreen advancementsscreen, AdvancementTabType advancementtabtype, int i, Advancement advancement, DisplayInfo displayinfo) {
      this.minecraft = minecraft;
      this.screen = advancementsscreen;
      this.type = advancementtabtype;
      this.index = i;
      this.advancement = advancement;
      this.display = displayinfo;
      this.icon = displayinfo.getIcon();
      this.title = displayinfo.getTitle();
      this.root = new AdvancementWidget(this, minecraft, advancement, displayinfo);
      this.addWidget(this.root, advancement);
   }

   public AdvancementTabType getType() {
      return this.type;
   }

   public int getIndex() {
      return this.index;
   }

   public Advancement getAdvancement() {
      return this.advancement;
   }

   public Component getTitle() {
      return this.title;
   }

   public DisplayInfo getDisplay() {
      return this.display;
   }

   public void drawTab(GuiGraphics guigraphics, int i, int j, boolean flag) {
      this.type.draw(guigraphics, i, j, flag, this.index);
   }

   public void drawIcon(GuiGraphics guigraphics, int i, int j) {
      this.type.drawIcon(guigraphics, i, j, this.index, this.icon);
   }

   public void drawContents(GuiGraphics guigraphics, int i, int j) {
      if (!this.centered) {
         this.scrollX = (double)(117 - (this.maxX + this.minX) / 2);
         this.scrollY = (double)(56 - (this.maxY + this.minY) / 2);
         this.centered = true;
      }

      guigraphics.enableScissor(i, j, i + 234, j + 113);
      guigraphics.pose().pushPose();
      guigraphics.pose().translate((float)i, (float)j, 0.0F);
      ResourceLocation resourcelocation = Objects.requireNonNullElse(this.display.getBackground(), TextureManager.INTENTIONAL_MISSING_TEXTURE);
      int k = Mth.floor(this.scrollX);
      int l = Mth.floor(this.scrollY);
      int i1 = k % 16;
      int j1 = l % 16;

      for(int k1 = -1; k1 <= 15; ++k1) {
         for(int l1 = -1; l1 <= 8; ++l1) {
            guigraphics.blit(resourcelocation, i1 + 16 * k1, j1 + 16 * l1, 0.0F, 0.0F, 16, 16, 16, 16);
         }
      }

      this.root.drawConnectivity(guigraphics, k, l, true);
      this.root.drawConnectivity(guigraphics, k, l, false);
      this.root.draw(guigraphics, k, l);
      guigraphics.pose().popPose();
      guigraphics.disableScissor();
   }

   public void drawTooltips(GuiGraphics guigraphics, int i, int j, int k, int l) {
      guigraphics.pose().pushPose();
      guigraphics.pose().translate(0.0F, 0.0F, -200.0F);
      guigraphics.fill(0, 0, 234, 113, Mth.floor(this.fade * 255.0F) << 24);
      boolean flag = false;
      int i1 = Mth.floor(this.scrollX);
      int j1 = Mth.floor(this.scrollY);
      if (i > 0 && i < 234 && j > 0 && j < 113) {
         for(AdvancementWidget advancementwidget : this.widgets.values()) {
            if (advancementwidget.isMouseOver(i1, j1, i, j)) {
               flag = true;
               advancementwidget.drawHover(guigraphics, i1, j1, this.fade, k, l);
               break;
            }
         }
      }

      guigraphics.pose().popPose();
      if (flag) {
         this.fade = Mth.clamp(this.fade + 0.02F, 0.0F, 0.3F);
      } else {
         this.fade = Mth.clamp(this.fade - 0.04F, 0.0F, 1.0F);
      }

   }

   public boolean isMouseOver(int i, int j, double d0, double d1) {
      return this.type.isMouseOver(i, j, this.index, d0, d1);
   }

   @Nullable
   public static AdvancementTab create(Minecraft minecraft, AdvancementsScreen advancementsscreen, int i, Advancement advancement) {
      if (advancement.getDisplay() == null) {
         return null;
      } else {
         for(AdvancementTabType advancementtabtype : AdvancementTabType.values()) {
            if (i < advancementtabtype.getMax()) {
               return new AdvancementTab(minecraft, advancementsscreen, advancementtabtype, i, advancement, advancement.getDisplay());
            }

            i -= advancementtabtype.getMax();
         }

         return null;
      }
   }

   public void scroll(double d0, double d1) {
      if (this.maxX - this.minX > 234) {
         this.scrollX = Mth.clamp(this.scrollX + d0, (double)(-(this.maxX - 234)), 0.0D);
      }

      if (this.maxY - this.minY > 113) {
         this.scrollY = Mth.clamp(this.scrollY + d1, (double)(-(this.maxY - 113)), 0.0D);
      }

   }

   public void addAdvancement(Advancement advancement) {
      if (advancement.getDisplay() != null) {
         AdvancementWidget advancementwidget = new AdvancementWidget(this, this.minecraft, advancement, advancement.getDisplay());
         this.addWidget(advancementwidget, advancement);
      }
   }

   private void addWidget(AdvancementWidget advancementwidget, Advancement advancement) {
      this.widgets.put(advancement, advancementwidget);
      int i = advancementwidget.getX();
      int j = i + 28;
      int k = advancementwidget.getY();
      int l = k + 27;
      this.minX = Math.min(this.minX, i);
      this.maxX = Math.max(this.maxX, j);
      this.minY = Math.min(this.minY, k);
      this.maxY = Math.max(this.maxY, l);

      for(AdvancementWidget advancementwidget1 : this.widgets.values()) {
         advancementwidget1.attachToParent();
      }

   }

   @Nullable
   public AdvancementWidget getWidget(Advancement advancement) {
      return this.widgets.get(advancement);
   }

   public AdvancementsScreen getScreen() {
      return this.screen;
   }
}
