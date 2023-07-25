package net.minecraft.world.level.block.state.properties;

import net.minecraft.util.StringRepresentable;

public enum ChestType implements StringRepresentable {
   SINGLE("single"),
   LEFT("left"),
   RIGHT("right");

   private final String name;

   private ChestType(String s) {
      this.name = s;
   }

   public String getSerializedName() {
      return this.name;
   }

   public ChestType getOpposite() {
      ChestType var10000;
      switch (this) {
         case SINGLE:
            var10000 = SINGLE;
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
}
