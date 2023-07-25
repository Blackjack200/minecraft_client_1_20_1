package net.minecraft.client.gui.components;

import java.util.OptionalInt;
import net.minecraft.Util;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.network.chat.Component;
import net.minecraft.util.SingleKeyCache;

public class MultiLineTextWidget extends AbstractStringWidget {
   private OptionalInt maxWidth = OptionalInt.empty();
   private OptionalInt maxRows = OptionalInt.empty();
   private final SingleKeyCache<MultiLineTextWidget.CacheKey, MultiLineLabel> cache;
   private boolean centered = false;

   public MultiLineTextWidget(Component component, Font font) {
      this(0, 0, component, font);
   }

   public MultiLineTextWidget(int i, int j, Component component, Font font) {
      super(i, j, 0, 0, component, font);
      this.cache = Util.singleKeyCache((multilinetextwidget_cachekey) -> multilinetextwidget_cachekey.maxRows.isPresent() ? MultiLineLabel.create(font, multilinetextwidget_cachekey.message, multilinetextwidget_cachekey.maxWidth, multilinetextwidget_cachekey.maxRows.getAsInt()) : MultiLineLabel.create(font, multilinetextwidget_cachekey.message, multilinetextwidget_cachekey.maxWidth));
      this.active = false;
   }

   public MultiLineTextWidget setColor(int i) {
      super.setColor(i);
      return this;
   }

   public MultiLineTextWidget setMaxWidth(int i) {
      this.maxWidth = OptionalInt.of(i);
      return this;
   }

   public MultiLineTextWidget setMaxRows(int i) {
      this.maxRows = OptionalInt.of(i);
      return this;
   }

   public MultiLineTextWidget setCentered(boolean flag) {
      this.centered = flag;
      return this;
   }

   public int getWidth() {
      return this.cache.getValue(this.getFreshCacheKey()).getWidth();
   }

   public int getHeight() {
      return this.cache.getValue(this.getFreshCacheKey()).getLineCount() * 9;
   }

   public void renderWidget(GuiGraphics guigraphics, int i, int j, float f) {
      MultiLineLabel multilinelabel = this.cache.getValue(this.getFreshCacheKey());
      int k = this.getX();
      int l = this.getY();
      int i1 = 9;
      int j1 = this.getColor();
      if (this.centered) {
         multilinelabel.renderCentered(guigraphics, k + this.getWidth() / 2, l, i1, j1);
      } else {
         multilinelabel.renderLeftAligned(guigraphics, k, l, i1, j1);
      }

   }

   private MultiLineTextWidget.CacheKey getFreshCacheKey() {
      return new MultiLineTextWidget.CacheKey(this.getMessage(), this.maxWidth.orElse(Integer.MAX_VALUE), this.maxRows);
   }

   static record CacheKey(Component message, int maxWidth, OptionalInt maxRows) {
      final Component message;
      final int maxWidth;
      final OptionalInt maxRows;
   }
}
