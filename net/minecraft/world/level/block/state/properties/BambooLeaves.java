package net.minecraft.world.level.block.state.properties;

import net.minecraft.util.StringRepresentable;

public enum BambooLeaves implements StringRepresentable {
   NONE("none"),
   SMALL("small"),
   LARGE("large");

   private final String name;

   private BambooLeaves(String s) {
      this.name = s;
   }

   public String toString() {
      return this.name;
   }

   public String getSerializedName() {
      return this.name;
   }
}
