package net.minecraft.world.entity;

import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;

public class EntityDimensions {
   public final float width;
   public final float height;
   public final boolean fixed;

   public EntityDimensions(float f, float f1, boolean flag) {
      this.width = f;
      this.height = f1;
      this.fixed = flag;
   }

   public AABB makeBoundingBox(Vec3 vec3) {
      return this.makeBoundingBox(vec3.x, vec3.y, vec3.z);
   }

   public AABB makeBoundingBox(double d0, double d1, double d2) {
      float f = this.width / 2.0F;
      float f1 = this.height;
      return new AABB(d0 - (double)f, d1, d2 - (double)f, d0 + (double)f, d1 + (double)f1, d2 + (double)f);
   }

   public EntityDimensions scale(float f) {
      return this.scale(f, f);
   }

   public EntityDimensions scale(float f, float f1) {
      return !this.fixed && (f != 1.0F || f1 != 1.0F) ? scalable(this.width * f, this.height * f1) : this;
   }

   public static EntityDimensions scalable(float f, float f1) {
      return new EntityDimensions(f, f1, false);
   }

   public static EntityDimensions fixed(float f, float f1) {
      return new EntityDimensions(f, f1, true);
   }

   public String toString() {
      return "EntityDimensions w=" + this.width + ", h=" + this.height + ", fixed=" + this.fixed;
   }
}
