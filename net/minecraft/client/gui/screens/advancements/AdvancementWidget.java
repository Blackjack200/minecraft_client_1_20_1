package net.minecraft.client.gui.screens.advancements;

import com.google.common.collect.Lists;
import com.mojang.blaze3d.systems.RenderSystem;
import java.util.List;
import javax.annotation.Nullable;
import net.minecraft.advancements.Advancement;
import net.minecraft.advancements.AdvancementProgress;
import net.minecraft.advancements.DisplayInfo;
import net.minecraft.client.Minecraft;
import net.minecraft.client.StringSplitter;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.locale.Language;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.ComponentUtils;
import net.minecraft.network.chat.FormattedText;
import net.minecraft.network.chat.Style;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.FormattedCharSequence;
import net.minecraft.util.Mth;

public class AdvancementWidget {
   private static final ResourceLocation WIDGETS_LOCATION = new ResourceLocation("textures/gui/advancements/widgets.png");
   private static final int HEIGHT = 26;
   private static final int BOX_X = 0;
   private static final int BOX_WIDTH = 200;
   private static final int FRAME_WIDTH = 26;
   private static final int ICON_X = 8;
   private static final int ICON_Y = 5;
   private static final int ICON_WIDTH = 26;
   private static final int TITLE_PADDING_LEFT = 3;
   private static final int TITLE_PADDING_RIGHT = 5;
   private static final int TITLE_X = 32;
   private static final int TITLE_Y = 9;
   private static final int TITLE_MAX_WIDTH = 163;
   private static final int[] TEST_SPLIT_OFFSETS = new int[]{0, 10, -10, 25, -25};
   private final AdvancementTab tab;
   private final Advancement advancement;
   private final DisplayInfo display;
   private final FormattedCharSequence title;
   private final int width;
   private final List<FormattedCharSequence> description;
   private final Minecraft minecraft;
   @Nullable
   private AdvancementWidget parent;
   private final List<AdvancementWidget> children = Lists.newArrayList();
   @Nullable
   private AdvancementProgress progress;
   private final int x;
   private final int y;

   public AdvancementWidget(AdvancementTab advancementtab, Minecraft minecraft, Advancement advancement, DisplayInfo displayinfo) {
      this.tab = advancementtab;
      this.advancement = advancement;
      this.display = displayinfo;
      this.minecraft = minecraft;
      this.title = Language.getInstance().getVisualOrder(minecraft.font.substrByWidth(displayinfo.getTitle(), 163));
      this.x = Mth.floor(displayinfo.getX() * 28.0F);
      this.y = Mth.floor(displayinfo.getY() * 27.0F);
      int i = advancement.getMaxCriteraRequired();
      int j = String.valueOf(i).length();
      int k = i > 1 ? minecraft.font.width("  ") + minecraft.font.width("0") * j * 2 + minecraft.font.width("/") : 0;
      int l = 29 + minecraft.font.width(this.title) + k;
      this.description = Language.getInstance().getVisualOrder(this.findOptimalLines(ComponentUtils.mergeStyles(displayinfo.getDescription().copy(), Style.EMPTY.withColor(displayinfo.getFrame().getChatColor())), l));

      for(FormattedCharSequence formattedcharsequence : this.description) {
         l = Math.max(l, minecraft.font.width(formattedcharsequence));
      }

      this.width = l + 3 + 5;
   }

   private static float getMaxWidth(StringSplitter stringsplitter, List<FormattedText> list) {
      return (float)list.stream().mapToDouble(stringsplitter::stringWidth).max().orElse(0.0D);
   }

   private List<FormattedText> findOptimalLines(Component component, int i) {
      StringSplitter stringsplitter = this.minecraft.font.getSplitter();
      List<FormattedText> list = null;
      float f = Float.MAX_VALUE;

      for(int j : TEST_SPLIT_OFFSETS) {
         List<FormattedText> list1 = stringsplitter.splitLines(component, i - j, Style.EMPTY);
         float f1 = Math.abs(getMaxWidth(stringsplitter, list1) - (float)i);
         if (f1 <= 10.0F) {
            return list1;
         }

         if (f1 < f) {
            f = f1;
            list = list1;
         }
      }

      return list;
   }

   @Nullable
   private AdvancementWidget getFirstVisibleParent(Advancement advancement) {
      do {
         advancement = advancement.getParent();
      } while(advancement != null && advancement.getDisplay() == null);

      return advancement != null && advancement.getDisplay() != null ? this.tab.getWidget(advancement) : null;
   }

   public void drawConnectivity(GuiGraphics guigraphics, int i, int j, boolean flag) {
      if (this.parent != null) {
         int k = i + this.parent.x + 13;
         int l = i + this.parent.x + 26 + 4;
         int i1 = j + this.parent.y + 13;
         int j1 = i + this.x + 13;
         int k1 = j + this.y + 13;
         int l1 = flag ? -16777216 : -1;
         if (flag) {
            guigraphics.hLine(l, k, i1 - 1, l1);
            guigraphics.hLine(l + 1, k, i1, l1);
            guigraphics.hLine(l, k, i1 + 1, l1);
            guigraphics.hLine(j1, l - 1, k1 - 1, l1);
            guigraphics.hLine(j1, l - 1, k1, l1);
            guigraphics.hLine(j1, l - 1, k1 + 1, l1);
            guigraphics.vLine(l - 1, k1, i1, l1);
            guigraphics.vLine(l + 1, k1, i1, l1);
         } else {
            guigraphics.hLine(l, k, i1, l1);
            guigraphics.hLine(j1, l, k1, l1);
            guigraphics.vLine(l, k1, i1, l1);
         }
      }

      for(AdvancementWidget advancementwidget : this.children) {
         advancementwidget.drawConnectivity(guigraphics, i, j, flag);
      }

   }

   public void draw(GuiGraphics guigraphics, int i, int j) {
      if (!this.display.isHidden() || this.progress != null && this.progress.isDone()) {
         float f = this.progress == null ? 0.0F : this.progress.getPercent();
         AdvancementWidgetType advancementwidgettype;
         if (f >= 1.0F) {
            advancementwidgettype = AdvancementWidgetType.OBTAINED;
         } else {
            advancementwidgettype = AdvancementWidgetType.UNOBTAINED;
         }

         guigraphics.blit(WIDGETS_LOCATION, i + this.x + 3, j + this.y, this.display.getFrame().getTexture(), 128 + advancementwidgettype.getIndex() * 26, 26, 26);
         guigraphics.renderFakeItem(this.display.getIcon(), i + this.x + 8, j + this.y + 5);
      }

      for(AdvancementWidget advancementwidget : this.children) {
         advancementwidget.draw(guigraphics, i, j);
      }

   }

   public int getWidth() {
      return this.width;
   }

   public void setProgress(AdvancementProgress advancementprogress) {
      this.progress = advancementprogress;
   }

   public void addChild(AdvancementWidget advancementwidget) {
      this.children.add(advancementwidget);
   }

   public void drawHover(GuiGraphics guigraphics, int i, int j, float f, int k, int l) {
      boolean flag = k + i + this.x + this.width + 26 >= this.tab.getScreen().width;
      String s = this.progress == null ? null : this.progress.getProgressText();
      int i1 = s == null ? 0 : this.minecraft.font.width(s);
      boolean flag1 = 113 - j - this.y - 26 <= 6 + this.description.size() * 9;
      float f1 = this.progress == null ? 0.0F : this.progress.getPercent();
      int j1 = Mth.floor(f1 * (float)this.width);
      AdvancementWidgetType advancementwidgettype;
      AdvancementWidgetType advancementwidgettype1;
      AdvancementWidgetType advancementwidgettype2;
      if (f1 >= 1.0F) {
         j1 = this.width / 2;
         advancementwidgettype = AdvancementWidgetType.OBTAINED;
         advancementwidgettype1 = AdvancementWidgetType.OBTAINED;
         advancementwidgettype2 = AdvancementWidgetType.OBTAINED;
      } else if (j1 < 2) {
         j1 = this.width / 2;
         advancementwidgettype = AdvancementWidgetType.UNOBTAINED;
         advancementwidgettype1 = AdvancementWidgetType.UNOBTAINED;
         advancementwidgettype2 = AdvancementWidgetType.UNOBTAINED;
      } else if (j1 > this.width - 2) {
         j1 = this.width / 2;
         advancementwidgettype = AdvancementWidgetType.OBTAINED;
         advancementwidgettype1 = AdvancementWidgetType.OBTAINED;
         advancementwidgettype2 = AdvancementWidgetType.UNOBTAINED;
      } else {
         advancementwidgettype = AdvancementWidgetType.OBTAINED;
         advancementwidgettype1 = AdvancementWidgetType.UNOBTAINED;
         advancementwidgettype2 = AdvancementWidgetType.UNOBTAINED;
      }

      int k1 = this.width - j1;
      RenderSystem.enableBlend();
      int l1 = j + this.y;
      int i2;
      if (flag) {
         i2 = i + this.x - this.width + 26 + 6;
      } else {
         i2 = i + this.x;
      }

      int k2 = 32 + this.description.size() * 9;
      if (!this.description.isEmpty()) {
         if (flag1) {
            guigraphics.blitNineSliced(WIDGETS_LOCATION, i2, l1 + 26 - k2, this.width, k2, 10, 200, 26, 0, 52);
         } else {
            guigraphics.blitNineSliced(WIDGETS_LOCATION, i2, l1, this.width, k2, 10, 200, 26, 0, 52);
         }
      }

      guigraphics.blit(WIDGETS_LOCATION, i2, l1, 0, advancementwidgettype.getIndex() * 26, j1, 26);
      guigraphics.blit(WIDGETS_LOCATION, i2 + j1, l1, 200 - k1, advancementwidgettype1.getIndex() * 26, k1, 26);
      guigraphics.blit(WIDGETS_LOCATION, i + this.x + 3, j + this.y, this.display.getFrame().getTexture(), 128 + advancementwidgettype2.getIndex() * 26, 26, 26);
      if (flag) {
         guigraphics.drawString(this.minecraft.font, this.title, i2 + 5, j + this.y + 9, -1);
         if (s != null) {
            guigraphics.drawString(this.minecraft.font, s, i + this.x - i1, j + this.y + 9, -1);
         }
      } else {
         guigraphics.drawString(this.minecraft.font, this.title, i + this.x + 32, j + this.y + 9, -1);
         if (s != null) {
            guigraphics.drawString(this.minecraft.font, s, i + this.x + this.width - i1 - 5, j + this.y + 9, -1);
         }
      }

      if (flag1) {
         for(int l2 = 0; l2 < this.description.size(); ++l2) {
            guigraphics.drawString(this.minecraft.font, this.description.get(l2), i2 + 5, l1 + 26 - k2 + 7 + l2 * 9, -5592406, false);
         }
      } else {
         for(int i3 = 0; i3 < this.description.size(); ++i3) {
            guigraphics.drawString(this.minecraft.font, this.description.get(i3), i2 + 5, j + this.y + 9 + 17 + i3 * 9, -5592406, false);
         }
      }

      guigraphics.renderFakeItem(this.display.getIcon(), i + this.x + 8, j + this.y + 5);
   }

   public boolean isMouseOver(int i, int j, int k, int l) {
      if (!this.display.isHidden() || this.progress != null && this.progress.isDone()) {
         int i1 = i + this.x;
         int j1 = i1 + 26;
         int k1 = j + this.y;
         int l1 = k1 + 26;
         return k >= i1 && k <= j1 && l >= k1 && l <= l1;
      } else {
         return false;
      }
   }

   public void attachToParent() {
      if (this.parent == null && this.advancement.getParent() != null) {
         this.parent = this.getFirstVisibleParent(this.advancement);
         if (this.parent != null) {
            this.parent.addChild(this);
         }
      }

   }

   public int getY() {
      return this.y;
   }

   public int getX() {
      return this.x;
   }
}
