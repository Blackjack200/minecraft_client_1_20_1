package net.minecraft.world.level.block.state.properties;

import net.minecraft.util.StringRepresentable;

public enum Tilt implements StringRepresentable {
   NONE("none", true),
   UNSTABLE("unstable", false),
   PARTIAL("partial", true),
   FULL("full", true);

   private final String name;
   private final boolean causesVibration;

   private Tilt(String s, boolean flag) {
      this.name = s;
      this.causesVibration = flag;
   }

   public String getSerializedName() {
      return this.name;
   }

   public boolean causesVibration() {
      return this.causesVibration;
   }
}
