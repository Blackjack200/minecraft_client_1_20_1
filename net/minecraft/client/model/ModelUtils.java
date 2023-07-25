package net.minecraft.client.model;

public class ModelUtils {
   public static float rotlerpRad(float f, float f1, float f2) {
      float f3;
      for(f3 = f1 - f; f3 < -(float)Math.PI; f3 += ((float)Math.PI * 2F)) {
      }

      while(f3 >= (float)Math.PI) {
         f3 -= ((float)Math.PI * 2F);
      }

      return f + f2 * f3;
   }
}
