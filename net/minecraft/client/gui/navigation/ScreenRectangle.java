package net.minecraft.client.gui.navigation;

import javax.annotation.Nullable;

public record ScreenRectangle(ScreenPosition position, int width, int height) {
   private static final ScreenRectangle EMPTY = new ScreenRectangle(0, 0, 0, 0);

   public ScreenRectangle(int i, int j, int k, int l) {
      this(new ScreenPosition(i, j), k, l);
   }

   public static ScreenRectangle empty() {
      return EMPTY;
   }

   public static ScreenRectangle of(ScreenAxis screenaxis, int i, int j, int k, int l) {
      ScreenRectangle var10000;
      switch (screenaxis) {
         case HORIZONTAL:
            var10000 = new ScreenRectangle(i, j, k, l);
            break;
         case VERTICAL:
            var10000 = new ScreenRectangle(j, i, l, k);
            break;
         default:
            throw new IncompatibleClassChangeError();
      }

      return var10000;
   }

   public ScreenRectangle step(ScreenDirection screendirection) {
      return new ScreenRectangle(this.position.step(screendirection), this.width, this.height);
   }

   public int getLength(ScreenAxis screenaxis) {
      int var10000;
      switch (screenaxis) {
         case HORIZONTAL:
            var10000 = this.width;
            break;
         case VERTICAL:
            var10000 = this.height;
            break;
         default:
            throw new IncompatibleClassChangeError();
      }

      return var10000;
   }

   public int getBoundInDirection(ScreenDirection screendirection) {
      ScreenAxis screenaxis = screendirection.getAxis();
      return screendirection.isPositive() ? this.position.getCoordinate(screenaxis) + this.getLength(screenaxis) - 1 : this.position.getCoordinate(screenaxis);
   }

   public ScreenRectangle getBorder(ScreenDirection screendirection) {
      int i = this.getBoundInDirection(screendirection);
      ScreenAxis screenaxis = screendirection.getAxis().orthogonal();
      int j = this.getBoundInDirection(screenaxis.getNegative());
      int k = this.getLength(screenaxis);
      return of(screendirection.getAxis(), i, j, 1, k).step(screendirection);
   }

   public boolean overlaps(ScreenRectangle screenrectangle) {
      return this.overlapsInAxis(screenrectangle, ScreenAxis.HORIZONTAL) && this.overlapsInAxis(screenrectangle, ScreenAxis.VERTICAL);
   }

   public boolean overlapsInAxis(ScreenRectangle screenrectangle, ScreenAxis screenaxis) {
      int i = this.getBoundInDirection(screenaxis.getNegative());
      int j = screenrectangle.getBoundInDirection(screenaxis.getNegative());
      int k = this.getBoundInDirection(screenaxis.getPositive());
      int l = screenrectangle.getBoundInDirection(screenaxis.getPositive());
      return Math.max(i, j) <= Math.min(k, l);
   }

   public int getCenterInAxis(ScreenAxis screenaxis) {
      return (this.getBoundInDirection(screenaxis.getPositive()) + this.getBoundInDirection(screenaxis.getNegative())) / 2;
   }

   @Nullable
   public ScreenRectangle intersection(ScreenRectangle screenrectangle) {
      int i = Math.max(this.left(), screenrectangle.left());
      int j = Math.max(this.top(), screenrectangle.top());
      int k = Math.min(this.right(), screenrectangle.right());
      int l = Math.min(this.bottom(), screenrectangle.bottom());
      return i < k && j < l ? new ScreenRectangle(i, j, k - i, l - j) : null;
   }

   public int top() {
      return this.position.y();
   }

   public int bottom() {
      return this.position.y() + this.height;
   }

   public int left() {
      return this.position.x();
   }

   public int right() {
      return this.position.x() + this.width;
   }
}
