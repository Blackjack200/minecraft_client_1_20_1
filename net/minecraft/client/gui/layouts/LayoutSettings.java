package net.minecraft.client.gui.layouts;

public interface LayoutSettings {
   LayoutSettings padding(int i);

   LayoutSettings padding(int i, int j);

   LayoutSettings padding(int i, int j, int k, int l);

   LayoutSettings paddingLeft(int i);

   LayoutSettings paddingTop(int i);

   LayoutSettings paddingRight(int i);

   LayoutSettings paddingBottom(int i);

   LayoutSettings paddingHorizontal(int i);

   LayoutSettings paddingVertical(int i);

   LayoutSettings align(float f, float f1);

   LayoutSettings alignHorizontally(float f);

   LayoutSettings alignVertically(float f);

   default LayoutSettings alignHorizontallyLeft() {
      return this.alignHorizontally(0.0F);
   }

   default LayoutSettings alignHorizontallyCenter() {
      return this.alignHorizontally(0.5F);
   }

   default LayoutSettings alignHorizontallyRight() {
      return this.alignHorizontally(1.0F);
   }

   default LayoutSettings alignVerticallyTop() {
      return this.alignVertically(0.0F);
   }

   default LayoutSettings alignVerticallyMiddle() {
      return this.alignVertically(0.5F);
   }

   default LayoutSettings alignVerticallyBottom() {
      return this.alignVertically(1.0F);
   }

   LayoutSettings copy();

   LayoutSettings.LayoutSettingsImpl getExposed();

   static LayoutSettings defaults() {
      return new LayoutSettings.LayoutSettingsImpl();
   }

   public static class LayoutSettingsImpl implements LayoutSettings {
      public int paddingLeft;
      public int paddingTop;
      public int paddingRight;
      public int paddingBottom;
      public float xAlignment;
      public float yAlignment;

      public LayoutSettingsImpl() {
      }

      public LayoutSettingsImpl(LayoutSettings.LayoutSettingsImpl layoutsettings_layoutsettingsimpl) {
         this.paddingLeft = layoutsettings_layoutsettingsimpl.paddingLeft;
         this.paddingTop = layoutsettings_layoutsettingsimpl.paddingTop;
         this.paddingRight = layoutsettings_layoutsettingsimpl.paddingRight;
         this.paddingBottom = layoutsettings_layoutsettingsimpl.paddingBottom;
         this.xAlignment = layoutsettings_layoutsettingsimpl.xAlignment;
         this.yAlignment = layoutsettings_layoutsettingsimpl.yAlignment;
      }

      public LayoutSettings.LayoutSettingsImpl padding(int i) {
         return this.padding(i, i);
      }

      public LayoutSettings.LayoutSettingsImpl padding(int i, int j) {
         return this.paddingHorizontal(i).paddingVertical(j);
      }

      public LayoutSettings.LayoutSettingsImpl padding(int i, int j, int k, int l) {
         return this.paddingLeft(i).paddingRight(k).paddingTop(j).paddingBottom(l);
      }

      public LayoutSettings.LayoutSettingsImpl paddingLeft(int i) {
         this.paddingLeft = i;
         return this;
      }

      public LayoutSettings.LayoutSettingsImpl paddingTop(int i) {
         this.paddingTop = i;
         return this;
      }

      public LayoutSettings.LayoutSettingsImpl paddingRight(int i) {
         this.paddingRight = i;
         return this;
      }

      public LayoutSettings.LayoutSettingsImpl paddingBottom(int i) {
         this.paddingBottom = i;
         return this;
      }

      public LayoutSettings.LayoutSettingsImpl paddingHorizontal(int i) {
         return this.paddingLeft(i).paddingRight(i);
      }

      public LayoutSettings.LayoutSettingsImpl paddingVertical(int i) {
         return this.paddingTop(i).paddingBottom(i);
      }

      public LayoutSettings.LayoutSettingsImpl align(float f, float f1) {
         this.xAlignment = f;
         this.yAlignment = f1;
         return this;
      }

      public LayoutSettings.LayoutSettingsImpl alignHorizontally(float f) {
         this.xAlignment = f;
         return this;
      }

      public LayoutSettings.LayoutSettingsImpl alignVertically(float f) {
         this.yAlignment = f;
         return this;
      }

      public LayoutSettings.LayoutSettingsImpl copy() {
         return new LayoutSettings.LayoutSettingsImpl(this);
      }

      public LayoutSettings.LayoutSettingsImpl getExposed() {
         return this;
      }
   }
}
