package net.minecraft.core;

import net.minecraft.nbt.FloatTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.util.Mth;

public class Rotations {
   protected final float x;
   protected final float y;
   protected final float z;

   public Rotations(float f, float f1, float f2) {
      this.x = !Float.isInfinite(f) && !Float.isNaN(f) ? f % 360.0F : 0.0F;
      this.y = !Float.isInfinite(f1) && !Float.isNaN(f1) ? f1 % 360.0F : 0.0F;
      this.z = !Float.isInfinite(f2) && !Float.isNaN(f2) ? f2 % 360.0F : 0.0F;
   }

   public Rotations(ListTag listtag) {
      this(listtag.getFloat(0), listtag.getFloat(1), listtag.getFloat(2));
   }

   public ListTag save() {
      ListTag listtag = new ListTag();
      listtag.add(FloatTag.valueOf(this.x));
      listtag.add(FloatTag.valueOf(this.y));
      listtag.add(FloatTag.valueOf(this.z));
      return listtag;
   }

   public boolean equals(Object object) {
      if (!(object instanceof Rotations rotations)) {
         return false;
      } else {
         return this.x == rotations.x && this.y == rotations.y && this.z == rotations.z;
      }
   }

   public float getX() {
      return this.x;
   }

   public float getY() {
      return this.y;
   }

   public float getZ() {
      return this.z;
   }

   public float getWrappedX() {
      return Mth.wrapDegrees(this.x);
   }

   public float getWrappedY() {
      return Mth.wrapDegrees(this.y);
   }

   public float getWrappedZ() {
      return Mth.wrapDegrees(this.z);
   }
}
