package net.minecraft.world.level;

public class FoliageColor {
   private static int[] pixels = new int[65536];

   public static void init(int[] aint) {
      pixels = aint;
   }

   public static int get(double d0, double d1) {
      d1 *= d0;
      int i = (int)((1.0D - d0) * 255.0D);
      int j = (int)((1.0D - d1) * 255.0D);
      int k = j << 8 | i;
      return k >= pixels.length ? getDefaultColor() : pixels[k];
   }

   public static int getEvergreenColor() {
      return 6396257;
   }

   public static int getBirchColor() {
      return 8431445;
   }

   public static int getDefaultColor() {
      return 4764952;
   }

   public static int getMangroveColor() {
      return 9619016;
   }
}
