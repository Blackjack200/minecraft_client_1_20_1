package net.minecraft.client.gui.layouts;

import com.mojang.math.Divisor;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.function.Consumer;

public class LinearLayout extends AbstractLayout {
   private final LinearLayout.Orientation orientation;
   private final List<LinearLayout.ChildContainer> children = new ArrayList<>();
   private final LayoutSettings defaultChildLayoutSettings = LayoutSettings.defaults();

   public LinearLayout(int i, int j, LinearLayout.Orientation linearlayout_orientation) {
      this(0, 0, i, j, linearlayout_orientation);
   }

   public LinearLayout(int i, int j, int k, int l, LinearLayout.Orientation linearlayout_orientation) {
      super(i, j, k, l);
      this.orientation = linearlayout_orientation;
   }

   public void arrangeElements() {
      super.arrangeElements();
      if (!this.children.isEmpty()) {
         int i = 0;
         int j = this.orientation.getSecondaryLength(this);

         for(LinearLayout.ChildContainer linearlayout_childcontainer : this.children) {
            i += this.orientation.getPrimaryLength(linearlayout_childcontainer);
            j = Math.max(j, this.orientation.getSecondaryLength(linearlayout_childcontainer));
         }

         int k = this.orientation.getPrimaryLength(this) - i;
         int l = this.orientation.getPrimaryPosition(this);
         Iterator<LinearLayout.ChildContainer> iterator = this.children.iterator();
         LinearLayout.ChildContainer linearlayout_childcontainer1 = iterator.next();
         this.orientation.setPrimaryPosition(linearlayout_childcontainer1, l);
         l += this.orientation.getPrimaryLength(linearlayout_childcontainer1);
         LinearLayout.ChildContainer linearlayout_childcontainer2;
         if (this.children.size() >= 2) {
            for(Divisor divisor = new Divisor(k, this.children.size() - 1); divisor.hasNext(); l += this.orientation.getPrimaryLength(linearlayout_childcontainer2)) {
               l += divisor.nextInt();
               linearlayout_childcontainer2 = iterator.next();
               this.orientation.setPrimaryPosition(linearlayout_childcontainer2, l);
            }
         }

         int i1 = this.orientation.getSecondaryPosition(this);

         for(LinearLayout.ChildContainer linearlayout_childcontainer3 : this.children) {
            this.orientation.setSecondaryPosition(linearlayout_childcontainer3, i1, j);
         }

         switch (this.orientation) {
            case HORIZONTAL:
               this.height = j;
               break;
            case VERTICAL:
               this.width = j;
         }

      }
   }

   public void visitChildren(Consumer<LayoutElement> consumer) {
      this.children.forEach((linearlayout_childcontainer) -> consumer.accept(linearlayout_childcontainer.child));
   }

   public LayoutSettings newChildLayoutSettings() {
      return this.defaultChildLayoutSettings.copy();
   }

   public LayoutSettings defaultChildLayoutSetting() {
      return this.defaultChildLayoutSettings;
   }

   public <T extends LayoutElement> T addChild(T layoutelement) {
      return this.addChild(layoutelement, this.newChildLayoutSettings());
   }

   public <T extends LayoutElement> T addChild(T layoutelement, LayoutSettings layoutsettings) {
      this.children.add(new LinearLayout.ChildContainer(layoutelement, layoutsettings));
      return layoutelement;
   }

   static class ChildContainer extends AbstractLayout.AbstractChildWrapper {
      protected ChildContainer(LayoutElement layoutelement, LayoutSettings layoutsettings) {
         super(layoutelement, layoutsettings);
      }
   }

   public static enum Orientation {
      HORIZONTAL,
      VERTICAL;

      int getPrimaryLength(LayoutElement layoutelement) {
         int var10000;
         switch (this) {
            case HORIZONTAL:
               var10000 = layoutelement.getWidth();
               break;
            case VERTICAL:
               var10000 = layoutelement.getHeight();
               break;
            default:
               throw new IncompatibleClassChangeError();
         }

         return var10000;
      }

      int getPrimaryLength(LinearLayout.ChildContainer linearlayout_childcontainer) {
         int var10000;
         switch (this) {
            case HORIZONTAL:
               var10000 = linearlayout_childcontainer.getWidth();
               break;
            case VERTICAL:
               var10000 = linearlayout_childcontainer.getHeight();
               break;
            default:
               throw new IncompatibleClassChangeError();
         }

         return var10000;
      }

      int getSecondaryLength(LayoutElement layoutelement) {
         int var10000;
         switch (this) {
            case HORIZONTAL:
               var10000 = layoutelement.getHeight();
               break;
            case VERTICAL:
               var10000 = layoutelement.getWidth();
               break;
            default:
               throw new IncompatibleClassChangeError();
         }

         return var10000;
      }

      int getSecondaryLength(LinearLayout.ChildContainer linearlayout_childcontainer) {
         int var10000;
         switch (this) {
            case HORIZONTAL:
               var10000 = linearlayout_childcontainer.getHeight();
               break;
            case VERTICAL:
               var10000 = linearlayout_childcontainer.getWidth();
               break;
            default:
               throw new IncompatibleClassChangeError();
         }

         return var10000;
      }

      void setPrimaryPosition(LinearLayout.ChildContainer linearlayout_childcontainer, int i) {
         switch (this) {
            case HORIZONTAL:
               linearlayout_childcontainer.setX(i, linearlayout_childcontainer.getWidth());
               break;
            case VERTICAL:
               linearlayout_childcontainer.setY(i, linearlayout_childcontainer.getHeight());
         }

      }

      void setSecondaryPosition(LinearLayout.ChildContainer linearlayout_childcontainer, int i, int j) {
         switch (this) {
            case HORIZONTAL:
               linearlayout_childcontainer.setY(i, j);
               break;
            case VERTICAL:
               linearlayout_childcontainer.setX(i, j);
         }

      }

      int getPrimaryPosition(LayoutElement layoutelement) {
         int var10000;
         switch (this) {
            case HORIZONTAL:
               var10000 = layoutelement.getX();
               break;
            case VERTICAL:
               var10000 = layoutelement.getY();
               break;
            default:
               throw new IncompatibleClassChangeError();
         }

         return var10000;
      }

      int getSecondaryPosition(LayoutElement layoutelement) {
         int var10000;
         switch (this) {
            case HORIZONTAL:
               var10000 = layoutelement.getY();
               break;
            case VERTICAL:
               var10000 = layoutelement.getX();
               break;
            default:
               throw new IncompatibleClassChangeError();
         }

         return var10000;
      }
   }
}
