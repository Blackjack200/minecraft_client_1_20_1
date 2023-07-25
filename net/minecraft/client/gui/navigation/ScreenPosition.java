package net.minecraft.client.gui.navigation;

public record ScreenPosition(int x, int y) {
   public static ScreenPosition of(ScreenAxis screenaxis, int i, int j) {
      ScreenPosition var10000;
      switch (screenaxis) {
         case HORIZONTAL:
            var10000 = new ScreenPosition(i, j);
            break;
         case VERTICAL:
            var10000 = new ScreenPosition(j, i);
            break;
         default:
            throw new IncompatibleClassChangeError();
      }

      return var10000;
   }

   public ScreenPosition step(ScreenDirection screendirection) {
      ScreenPosition var10000;
      switch (screendirection) {
         case DOWN:
            var10000 = new ScreenPosition(this.x, this.y + 1);
            break;
         case UP:
            var10000 = new ScreenPosition(this.x, this.y - 1);
            break;
         case LEFT:
            var10000 = new ScreenPosition(this.x - 1, this.y);
            break;
         case RIGHT:
            var10000 = new ScreenPosition(this.x + 1, this.y);
            break;
         default:
            throw new IncompatibleClassChangeError();
      }

      return var10000;
   }

   public int getCoordinate(ScreenAxis screenaxis) {
      int var10000;
      switch (screenaxis) {
         case HORIZONTAL:
            var10000 = this.x;
            break;
         case VERTICAL:
            var10000 = this.y;
            break;
         default:
            throw new IncompatibleClassChangeError();
      }

      return var10000;
   }
}
