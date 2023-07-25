package com.mojang.blaze3d.shaders;

public enum FogShape {
   SPHERE(0),
   CYLINDER(1);

   private final int index;

   private FogShape(int i) {
      this.index = i;
   }

   public int getIndex() {
      return this.index;
   }
}
