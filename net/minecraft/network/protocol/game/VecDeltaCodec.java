package net.minecraft.network.protocol.game;

import net.minecraft.world.phys.Vec3;
import org.jetbrains.annotations.VisibleForTesting;

public class VecDeltaCodec {
   private static final double TRUNCATION_STEPS = 4096.0D;
   private Vec3 base = Vec3.ZERO;

   @VisibleForTesting
   static long encode(double d0) {
      return Math.round(d0 * 4096.0D);
   }

   @VisibleForTesting
   static double decode(long i) {
      return (double)i / 4096.0D;
   }

   public Vec3 decode(long i, long j, long k) {
      if (i == 0L && j == 0L && k == 0L) {
         return this.base;
      } else {
         double d0 = i == 0L ? this.base.x : decode(encode(this.base.x) + i);
         double d1 = j == 0L ? this.base.y : decode(encode(this.base.y) + j);
         double d2 = k == 0L ? this.base.z : decode(encode(this.base.z) + k);
         return new Vec3(d0, d1, d2);
      }
   }

   public long encodeX(Vec3 vec3) {
      return encode(vec3.x) - encode(this.base.x);
   }

   public long encodeY(Vec3 vec3) {
      return encode(vec3.y) - encode(this.base.y);
   }

   public long encodeZ(Vec3 vec3) {
      return encode(vec3.z) - encode(this.base.z);
   }

   public Vec3 delta(Vec3 vec3) {
      return vec3.subtract(this.base);
   }

   public void setBase(Vec3 vec3) {
      this.base = vec3;
   }
}
