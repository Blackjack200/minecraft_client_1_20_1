package net.minecraft.client.gui.layouts;

import java.util.function.Consumer;
import net.minecraft.client.gui.screens.Screen;

public class HeaderAndFooterLayout implements Layout {
   private static final int DEFAULT_HEADER_AND_FOOTER_HEIGHT = 36;
   private static final int DEFAULT_CONTENT_MARGIN_TOP = 30;
   private final FrameLayout headerFrame = new FrameLayout();
   private final FrameLayout footerFrame = new FrameLayout();
   private final FrameLayout contentsFrame = new FrameLayout();
   private final Screen screen;
   private int headerHeight;
   private int footerHeight;

   public HeaderAndFooterLayout(Screen screen) {
      this(screen, 36);
   }

   public HeaderAndFooterLayout(Screen screen, int i) {
      this(screen, i, i);
   }

   public HeaderAndFooterLayout(Screen screen, int i, int j) {
      this.screen = screen;
      this.headerHeight = i;
      this.footerHeight = j;
      this.headerFrame.defaultChildLayoutSetting().align(0.5F, 0.5F);
      this.footerFrame.defaultChildLayoutSetting().align(0.5F, 0.5F);
      this.contentsFrame.defaultChildLayoutSetting().align(0.5F, 0.0F).paddingTop(30);
   }

   public void setX(int i) {
   }

   public void setY(int i) {
   }

   public int getX() {
      return 0;
   }

   public int getY() {
      return 0;
   }

   public int getWidth() {
      return this.screen.width;
   }

   public int getHeight() {
      return this.screen.height;
   }

   public int getFooterHeight() {
      return this.footerHeight;
   }

   public void setFooterHeight(int i) {
      this.footerHeight = i;
   }

   public void setHeaderHeight(int i) {
      this.headerHeight = i;
   }

   public int getHeaderHeight() {
      return this.headerHeight;
   }

   public void visitChildren(Consumer<LayoutElement> consumer) {
      this.headerFrame.visitChildren(consumer);
      this.contentsFrame.visitChildren(consumer);
      this.footerFrame.visitChildren(consumer);
   }

   public void arrangeElements() {
      int i = this.getHeaderHeight();
      int j = this.getFooterHeight();
      this.headerFrame.setMinWidth(this.screen.width);
      this.headerFrame.setMinHeight(i);
      this.headerFrame.setPosition(0, 0);
      this.headerFrame.arrangeElements();
      this.footerFrame.setMinWidth(this.screen.width);
      this.footerFrame.setMinHeight(j);
      this.footerFrame.arrangeElements();
      this.footerFrame.setY(this.screen.height - j);
      this.contentsFrame.setMinWidth(this.screen.width);
      this.contentsFrame.setMinHeight(this.screen.height - i - j);
      this.contentsFrame.setPosition(0, i);
      this.contentsFrame.arrangeElements();
   }

   public <T extends LayoutElement> T addToHeader(T layoutelement) {
      return this.headerFrame.addChild(layoutelement);
   }

   public <T extends LayoutElement> T addToHeader(T layoutelement, LayoutSettings layoutsettings) {
      return this.headerFrame.addChild(layoutelement, layoutsettings);
   }

   public <T extends LayoutElement> T addToFooter(T layoutelement) {
      return this.footerFrame.addChild(layoutelement);
   }

   public <T extends LayoutElement> T addToFooter(T layoutelement, LayoutSettings layoutsettings) {
      return this.footerFrame.addChild(layoutelement, layoutsettings);
   }

   public <T extends LayoutElement> T addToContents(T layoutelement) {
      return this.contentsFrame.addChild(layoutelement);
   }

   public <T extends LayoutElement> T addToContents(T layoutelement, LayoutSettings layoutsettings) {
      return this.contentsFrame.addChild(layoutelement, layoutsettings);
   }

   public LayoutSettings newHeaderLayoutSettings() {
      return this.headerFrame.newChildLayoutSettings();
   }

   public LayoutSettings newContentLayoutSettings() {
      return this.contentsFrame.newChildLayoutSettings();
   }

   public LayoutSettings newFooterLayoutSettings() {
      return this.footerFrame.newChildLayoutSettings();
   }
}
