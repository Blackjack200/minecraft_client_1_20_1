package net.minecraft.world.entity.ai.attributes;

import net.minecraft.util.Mth;

public class RangedAttribute extends Attribute {
   private final double minValue;
   private final double maxValue;

   public RangedAttribute(String s, double d0, double d1, double d2) {
      super(s, d0);
      this.minValue = d1;
      this.maxValue = d2;
      if (d1 > d2) {
         throw new IllegalArgumentException("Minimum value cannot be bigger than maximum value!");
      } else if (d0 < d1) {
         throw new IllegalArgumentException("Default value cannot be lower than minimum value!");
      } else if (d0 > d2) {
         throw new IllegalArgumentException("Default value cannot be bigger than maximum value!");
      }
   }

   public double getMinValue() {
      return this.minValue;
   }

   public double getMaxValue() {
      return this.maxValue;
   }

   public double sanitizeValue(double d0) {
      return Double.isNaN(d0) ? this.minValue : Mth.clamp(d0, this.minValue, this.maxValue);
   }
}
