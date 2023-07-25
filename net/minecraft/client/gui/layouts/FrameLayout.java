package net.minecraft.client.gui.layouts;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;
import net.minecraft.client.gui.navigation.ScreenRectangle;
import net.minecraft.util.Mth;

public class FrameLayout extends AbstractLayout {
   private final List<FrameLayout.ChildContainer> children = new ArrayList<>();
   private int minWidth;
   private int minHeight;
   private final LayoutSettings defaultChildLayoutSettings = LayoutSettings.defaults().align(0.5F, 0.5F);

   public FrameLayout() {
      this(0, 0, 0, 0);
   }

   public FrameLayout(int i, int j) {
      this(0, 0, i, j);
   }

   public FrameLayout(int i, int j, int k, int l) {
      super(i, j, k, l);
      this.setMinDimensions(k, l);
   }

   public FrameLayout setMinDimensions(int i, int j) {
      return this.setMinWidth(i).setMinHeight(j);
   }

   public FrameLayout setMinHeight(int i) {
      this.minHeight = i;
      return this;
   }

   public FrameLayout setMinWidth(int i) {
      this.minWidth = i;
      return this;
   }

   public LayoutSettings newChildLayoutSettings() {
      return this.defaultChildLayoutSettings.copy();
   }

   public LayoutSettings defaultChildLayoutSetting() {
      return this.defaultChildLayoutSettings;
   }

   public void arrangeElements() {
      super.arrangeElements();
      int i = this.minWidth;
      int j = this.minHeight;

      for(FrameLayout.ChildContainer framelayout_childcontainer : this.children) {
         i = Math.max(i, framelayout_childcontainer.getWidth());
         j = Math.max(j, framelayout_childcontainer.getHeight());
      }

      for(FrameLayout.ChildContainer framelayout_childcontainer1 : this.children) {
         framelayout_childcontainer1.setX(this.getX(), i);
         framelayout_childcontainer1.setY(this.getY(), j);
      }

      this.width = i;
      this.height = j;
   }

   public <T extends LayoutElement> T addChild(T layoutelement) {
      return this.addChild(layoutelement, this.newChildLayoutSettings());
   }

   public <T extends LayoutElement> T addChild(T layoutelement, LayoutSettings layoutsettings) {
      this.children.add(new FrameLayout.ChildContainer(layoutelement, layoutsettings));
      return layoutelement;
   }

   public void visitChildren(Consumer<LayoutElement> consumer) {
      this.children.forEach((framelayout_childcontainer) -> consumer.accept(framelayout_childcontainer.child));
   }

   public static void centerInRectangle(LayoutElement layoutelement, int i, int j, int k, int l) {
      alignInRectangle(layoutelement, i, j, k, l, 0.5F, 0.5F);
   }

   public static void centerInRectangle(LayoutElement layoutelement, ScreenRectangle screenrectangle) {
      centerInRectangle(layoutelement, screenrectangle.position().x(), screenrectangle.position().y(), screenrectangle.width(), screenrectangle.height());
   }

   public static void alignInRectangle(LayoutElement layoutelement, ScreenRectangle screenrectangle, float f, float f1) {
      alignInRectangle(layoutelement, screenrectangle.left(), screenrectangle.top(), screenrectangle.width(), screenrectangle.height(), f, f1);
   }

   public static void alignInRectangle(LayoutElement layoutelement, int i, int j, int k, int l, float f, float f1) {
      alignInDimension(i, k, layoutelement.getWidth(), layoutelement::setX, f);
      alignInDimension(j, l, layoutelement.getHeight(), layoutelement::setY, f1);
   }

   public static void alignInDimension(int i, int j, int k, Consumer<Integer> consumer, float f) {
      int l = (int)Mth.lerp(f, 0.0F, (float)(j - k));
      consumer.accept(i + l);
   }

   static class ChildContainer extends AbstractLayout.AbstractChildWrapper {
      protected ChildContainer(LayoutElement layoutelement, LayoutSettings layoutsettings) {
         super(layoutelement, layoutsettings);
      }
   }
}
