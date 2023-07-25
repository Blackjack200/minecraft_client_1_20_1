package net.minecraft.client.gui.navigation;

import it.unimi.dsi.fastutil.ints.IntComparator;

public enum ScreenDirection {
   UP,
   DOWN,
   LEFT,
   RIGHT;

   private final IntComparator coordinateValueComparator = (i, j) -> i == j ? 0 : (this.isBefore(i, j) ? -1 : 1);

   public ScreenAxis getAxis() {
      ScreenAxis var10000;
      switch (this) {
         case UP:
         case DOWN:
            var10000 = ScreenAxis.VERTICAL;
            break;
         case LEFT:
         case RIGHT:
            var10000 = ScreenAxis.HORIZONTAL;
            break;
         default:
            throw new IncompatibleClassChangeError();
      }

      return var10000;
   }

   public ScreenDirection getOpposite() {
      ScreenDirection var10000;
      switch (this) {
         case UP:
            var10000 = DOWN;
            break;
         case DOWN:
            var10000 = UP;
            break;
         case LEFT:
            var10000 = RIGHT;
            break;
         case RIGHT:
            var10000 = LEFT;
            break;
         default:
            throw new IncompatibleClassChangeError();
      }

      return var10000;
   }

   public boolean isPositive() {
      boolean var10000;
      switch (this) {
         case UP:
         case LEFT:
            var10000 = false;
            break;
         case DOWN:
         case RIGHT:
            var10000 = true;
            break;
         default:
            throw new IncompatibleClassChangeError();
      }

      return var10000;
   }

   public boolean isAfter(int i, int j) {
      if (this.isPositive()) {
         return i > j;
      } else {
         return j > i;
      }
   }

   public boolean isBefore(int i, int j) {
      if (this.isPositive()) {
         return i < j;
      } else {
         return j < i;
      }
   }

   public IntComparator coordinateValueComparator() {
      return this.coordinateValueComparator;
   }
}
