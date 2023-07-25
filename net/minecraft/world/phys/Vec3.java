package net.minecraft.world.phys;

import com.mojang.serialization.Codec;
import java.util.EnumSet;
import java.util.List;
import net.minecraft.Util;
import net.minecraft.core.Direction;
import net.minecraft.core.Position;
import net.minecraft.core.Vec3i;
import net.minecraft.util.Mth;
import net.minecraft.util.RandomSource;
import org.joml.Vector3f;

public class Vec3 implements Position {
   public static final Codec<Vec3> CODEC = Codec.DOUBLE.listOf().comapFlatMap((list) -> Util.fixedSize(list, 3).map((list1) -> new Vec3(list1.get(0), list1.get(1), list1.get(2))), (vec3) -> List.of(vec3.x(), vec3.y(), vec3.z()));
   public static final Vec3 ZERO = new Vec3(0.0D, 0.0D, 0.0D);
   public final double x;
   public final double y;
   public final double z;

   public static Vec3 fromRGB24(int i) {
      double d0 = (double)(i >> 16 & 255) / 255.0D;
      double d1 = (double)(i >> 8 & 255) / 255.0D;
      double d2 = (double)(i & 255) / 255.0D;
      return new Vec3(d0, d1, d2);
   }

   public static Vec3 atLowerCornerOf(Vec3i vec3i) {
      return new Vec3((double)vec3i.getX(), (double)vec3i.getY(), (double)vec3i.getZ());
   }

   public static Vec3 atLowerCornerWithOffset(Vec3i vec3i, double d0, double d1, double d2) {
      return new Vec3((double)vec3i.getX() + d0, (double)vec3i.getY() + d1, (double)vec3i.getZ() + d2);
   }

   public static Vec3 atCenterOf(Vec3i vec3i) {
      return atLowerCornerWithOffset(vec3i, 0.5D, 0.5D, 0.5D);
   }

   public static Vec3 atBottomCenterOf(Vec3i vec3i) {
      return atLowerCornerWithOffset(vec3i, 0.5D, 0.0D, 0.5D);
   }

   public static Vec3 upFromBottomCenterOf(Vec3i vec3i, double d0) {
      return atLowerCornerWithOffset(vec3i, 0.5D, d0, 0.5D);
   }

   public Vec3(double d0, double d1, double d2) {
      this.x = d0;
      this.y = d1;
      this.z = d2;
   }

   public Vec3(Vector3f vector3f) {
      this((double)vector3f.x(), (double)vector3f.y(), (double)vector3f.z());
   }

   public Vec3 vectorTo(Vec3 vec3) {
      return new Vec3(vec3.x - this.x, vec3.y - this.y, vec3.z - this.z);
   }

   public Vec3 normalize() {
      double d0 = Math.sqrt(this.x * this.x + this.y * this.y + this.z * this.z);
      return d0 < 1.0E-4D ? ZERO : new Vec3(this.x / d0, this.y / d0, this.z / d0);
   }

   public double dot(Vec3 vec3) {
      return this.x * vec3.x + this.y * vec3.y + this.z * vec3.z;
   }

   public Vec3 cross(Vec3 vec3) {
      return new Vec3(this.y * vec3.z - this.z * vec3.y, this.z * vec3.x - this.x * vec3.z, this.x * vec3.y - this.y * vec3.x);
   }

   public Vec3 subtract(Vec3 vec3) {
      return this.subtract(vec3.x, vec3.y, vec3.z);
   }

   public Vec3 subtract(double d0, double d1, double d2) {
      return this.add(-d0, -d1, -d2);
   }

   public Vec3 add(Vec3 vec3) {
      return this.add(vec3.x, vec3.y, vec3.z);
   }

   public Vec3 add(double d0, double d1, double d2) {
      return new Vec3(this.x + d0, this.y + d1, this.z + d2);
   }

   public boolean closerThan(Position position, double d0) {
      return this.distanceToSqr(position.x(), position.y(), position.z()) < d0 * d0;
   }

   public double distanceTo(Vec3 vec3) {
      double d0 = vec3.x - this.x;
      double d1 = vec3.y - this.y;
      double d2 = vec3.z - this.z;
      return Math.sqrt(d0 * d0 + d1 * d1 + d2 * d2);
   }

   public double distanceToSqr(Vec3 vec3) {
      double d0 = vec3.x - this.x;
      double d1 = vec3.y - this.y;
      double d2 = vec3.z - this.z;
      return d0 * d0 + d1 * d1 + d2 * d2;
   }

   public double distanceToSqr(double d0, double d1, double d2) {
      double d3 = d0 - this.x;
      double d4 = d1 - this.y;
      double d5 = d2 - this.z;
      return d3 * d3 + d4 * d4 + d5 * d5;
   }

   public Vec3 scale(double d0) {
      return this.multiply(d0, d0, d0);
   }

   public Vec3 reverse() {
      return this.scale(-1.0D);
   }

   public Vec3 multiply(Vec3 vec3) {
      return this.multiply(vec3.x, vec3.y, vec3.z);
   }

   public Vec3 multiply(double d0, double d1, double d2) {
      return new Vec3(this.x * d0, this.y * d1, this.z * d2);
   }

   public Vec3 offsetRandom(RandomSource randomsource, float f) {
      return this.add((double)((randomsource.nextFloat() - 0.5F) * f), (double)((randomsource.nextFloat() - 0.5F) * f), (double)((randomsource.nextFloat() - 0.5F) * f));
   }

   public double length() {
      return Math.sqrt(this.x * this.x + this.y * this.y + this.z * this.z);
   }

   public double lengthSqr() {
      return this.x * this.x + this.y * this.y + this.z * this.z;
   }

   public double horizontalDistance() {
      return Math.sqrt(this.x * this.x + this.z * this.z);
   }

   public double horizontalDistanceSqr() {
      return this.x * this.x + this.z * this.z;
   }

   public boolean equals(Object object) {
      if (this == object) {
         return true;
      } else if (!(object instanceof Vec3)) {
         return false;
      } else {
         Vec3 vec3 = (Vec3)object;
         if (Double.compare(vec3.x, this.x) != 0) {
            return false;
         } else if (Double.compare(vec3.y, this.y) != 0) {
            return false;
         } else {
            return Double.compare(vec3.z, this.z) == 0;
         }
      }
   }

   public int hashCode() {
      long i = Double.doubleToLongBits(this.x);
      int j = (int)(i ^ i >>> 32);
      i = Double.doubleToLongBits(this.y);
      j = 31 * j + (int)(i ^ i >>> 32);
      i = Double.doubleToLongBits(this.z);
      return 31 * j + (int)(i ^ i >>> 32);
   }

   public String toString() {
      return "(" + this.x + ", " + this.y + ", " + this.z + ")";
   }

   public Vec3 lerp(Vec3 vec3, double d0) {
      return new Vec3(Mth.lerp(d0, this.x, vec3.x), Mth.lerp(d0, this.y, vec3.y), Mth.lerp(d0, this.z, vec3.z));
   }

   public Vec3 xRot(float f) {
      float f1 = Mth.cos(f);
      float f2 = Mth.sin(f);
      double d0 = this.x;
      double d1 = this.y * (double)f1 + this.z * (double)f2;
      double d2 = this.z * (double)f1 - this.y * (double)f2;
      return new Vec3(d0, d1, d2);
   }

   public Vec3 yRot(float f) {
      float f1 = Mth.cos(f);
      float f2 = Mth.sin(f);
      double d0 = this.x * (double)f1 + this.z * (double)f2;
      double d1 = this.y;
      double d2 = this.z * (double)f1 - this.x * (double)f2;
      return new Vec3(d0, d1, d2);
   }

   public Vec3 zRot(float f) {
      float f1 = Mth.cos(f);
      float f2 = Mth.sin(f);
      double d0 = this.x * (double)f1 + this.y * (double)f2;
      double d1 = this.y * (double)f1 - this.x * (double)f2;
      double d2 = this.z;
      return new Vec3(d0, d1, d2);
   }

   public static Vec3 directionFromRotation(Vec2 vec2) {
      return directionFromRotation(vec2.x, vec2.y);
   }

   public static Vec3 directionFromRotation(float f, float f1) {
      float f2 = Mth.cos(-f1 * ((float)Math.PI / 180F) - (float)Math.PI);
      float f3 = Mth.sin(-f1 * ((float)Math.PI / 180F) - (float)Math.PI);
      float f4 = -Mth.cos(-f * ((float)Math.PI / 180F));
      float f5 = Mth.sin(-f * ((float)Math.PI / 180F));
      return new Vec3((double)(f3 * f4), (double)f5, (double)(f2 * f4));
   }

   public Vec3 align(EnumSet<Direction.Axis> enumset) {
      double d0 = enumset.contains(Direction.Axis.X) ? (double)Mth.floor(this.x) : this.x;
      double d1 = enumset.contains(Direction.Axis.Y) ? (double)Mth.floor(this.y) : this.y;
      double d2 = enumset.contains(Direction.Axis.Z) ? (double)Mth.floor(this.z) : this.z;
      return new Vec3(d0, d1, d2);
   }

   public double get(Direction.Axis direction_axis) {
      return direction_axis.choose(this.x, this.y, this.z);
   }

   public Vec3 with(Direction.Axis direction_axis, double d0) {
      double d1 = direction_axis == Direction.Axis.X ? d0 : this.x;
      double d2 = direction_axis == Direction.Axis.Y ? d0 : this.y;
      double d3 = direction_axis == Direction.Axis.Z ? d0 : this.z;
      return new Vec3(d1, d2, d3);
   }

   public Vec3 relative(Direction direction, double d0) {
      Vec3i vec3i = direction.getNormal();
      return new Vec3(this.x + d0 * (double)vec3i.getX(), this.y + d0 * (double)vec3i.getY(), this.z + d0 * (double)vec3i.getZ());
   }

   public final double x() {
      return this.x;
   }

   public final double y() {
      return this.y;
   }

   public final double z() {
      return this.z;
   }

   public Vector3f toVector3f() {
      return new Vector3f((float)this.x, (float)this.y, (float)this.z);
   }
}
