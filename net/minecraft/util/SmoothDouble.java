package net.minecraft.util;

public class SmoothDouble {
   private double targetValue;
   private double remainingValue;
   private double lastAmount;

   public double getNewDeltaValue(double d0, double d1) {
      this.targetValue += d0;
      double d2 = this.targetValue - this.remainingValue;
      double d3 = Mth.lerp(0.5D, this.lastAmount, d2);
      double d4 = Math.signum(d2);
      if (d4 * d2 > d4 * this.lastAmount) {
         d2 = d3;
      }

      this.lastAmount = d3;
      this.remainingValue += d2 * d1;
      return d2 * d1;
   }

   public void reset() {
      this.targetValue = 0.0D;
      this.remainingValue = 0.0D;
      this.lastAmount = 0.0D;
   }
}
