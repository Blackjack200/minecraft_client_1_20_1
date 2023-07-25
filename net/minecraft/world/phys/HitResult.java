package net.minecraft.world.phys;

import net.minecraft.world.entity.Entity;

public abstract class HitResult {
   protected final Vec3 location;

   protected HitResult(Vec3 vec3) {
      this.location = vec3;
   }

   public double distanceTo(Entity entity) {
      double d0 = this.location.x - entity.getX();
      double d1 = this.location.y - entity.getY();
      double d2 = this.location.z - entity.getZ();
      return d0 * d0 + d1 * d1 + d2 * d2;
   }

   public abstract HitResult.Type getType();

   public Vec3 getLocation() {
      return this.location;
   }

   public static enum Type {
      MISS,
      BLOCK,
      ENTITY;
   }
}
