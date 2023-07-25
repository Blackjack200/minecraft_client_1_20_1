package net.minecraft.client.model.geom.builders;

public class UVPair {
   private final float u;
   private final float v;

   public UVPair(float f, float f1) {
      this.u = f;
      this.v = f1;
   }

   public float u() {
      return this.u;
   }

   public float v() {
      return this.v;
   }

   public String toString() {
      return "(" + this.u + "," + this.v + ")";
   }
}
