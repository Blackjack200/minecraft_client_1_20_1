package net.minecraft.world.phys;

import java.util.Optional;
import javax.annotation.Nullable;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.util.Mth;
import net.minecraft.world.level.levelgen.structure.BoundingBox;

public class AABB {
   private static final double EPSILON = 1.0E-7D;
   public final double minX;
   public final double minY;
   public final double minZ;
   public final double maxX;
   public final double maxY;
   public final double maxZ;

   public AABB(double d0, double d1, double d2, double d3, double d4, double d5) {
      this.minX = Math.min(d0, d3);
      this.minY = Math.min(d1, d4);
      this.minZ = Math.min(d2, d5);
      this.maxX = Math.max(d0, d3);
      this.maxY = Math.max(d1, d4);
      this.maxZ = Math.max(d2, d5);
   }

   public AABB(BlockPos blockpos) {
      this((double)blockpos.getX(), (double)blockpos.getY(), (double)blockpos.getZ(), (double)(blockpos.getX() + 1), (double)(blockpos.getY() + 1), (double)(blockpos.getZ() + 1));
   }

   public AABB(BlockPos blockpos, BlockPos blockpos1) {
      this((double)blockpos.getX(), (double)blockpos.getY(), (double)blockpos.getZ(), (double)blockpos1.getX(), (double)blockpos1.getY(), (double)blockpos1.getZ());
   }

   public AABB(Vec3 vec3, Vec3 vec31) {
      this(vec3.x, vec3.y, vec3.z, vec31.x, vec31.y, vec31.z);
   }

   public static AABB of(BoundingBox boundingbox) {
      return new AABB((double)boundingbox.minX(), (double)boundingbox.minY(), (double)boundingbox.minZ(), (double)(boundingbox.maxX() + 1), (double)(boundingbox.maxY() + 1), (double)(boundingbox.maxZ() + 1));
   }

   public static AABB unitCubeFromLowerCorner(Vec3 vec3) {
      return new AABB(vec3.x, vec3.y, vec3.z, vec3.x + 1.0D, vec3.y + 1.0D, vec3.z + 1.0D);
   }

   public AABB setMinX(double d0) {
      return new AABB(d0, this.minY, this.minZ, this.maxX, this.maxY, this.maxZ);
   }

   public AABB setMinY(double d0) {
      return new AABB(this.minX, d0, this.minZ, this.maxX, this.maxY, this.maxZ);
   }

   public AABB setMinZ(double d0) {
      return new AABB(this.minX, this.minY, d0, this.maxX, this.maxY, this.maxZ);
   }

   public AABB setMaxX(double d0) {
      return new AABB(this.minX, this.minY, this.minZ, d0, this.maxY, this.maxZ);
   }

   public AABB setMaxY(double d0) {
      return new AABB(this.minX, this.minY, this.minZ, this.maxX, d0, this.maxZ);
   }

   public AABB setMaxZ(double d0) {
      return new AABB(this.minX, this.minY, this.minZ, this.maxX, this.maxY, d0);
   }

   public double min(Direction.Axis direction_axis) {
      return direction_axis.choose(this.minX, this.minY, this.minZ);
   }

   public double max(Direction.Axis direction_axis) {
      return direction_axis.choose(this.maxX, this.maxY, this.maxZ);
   }

   public boolean equals(Object object) {
      if (this == object) {
         return true;
      } else if (!(object instanceof AABB)) {
         return false;
      } else {
         AABB aabb = (AABB)object;
         if (Double.compare(aabb.minX, this.minX) != 0) {
            return false;
         } else if (Double.compare(aabb.minY, this.minY) != 0) {
            return false;
         } else if (Double.compare(aabb.minZ, this.minZ) != 0) {
            return false;
         } else if (Double.compare(aabb.maxX, this.maxX) != 0) {
            return false;
         } else if (Double.compare(aabb.maxY, this.maxY) != 0) {
            return false;
         } else {
            return Double.compare(aabb.maxZ, this.maxZ) == 0;
         }
      }
   }

   public int hashCode() {
      long i = Double.doubleToLongBits(this.minX);
      int j = (int)(i ^ i >>> 32);
      i = Double.doubleToLongBits(this.minY);
      j = 31 * j + (int)(i ^ i >>> 32);
      i = Double.doubleToLongBits(this.minZ);
      j = 31 * j + (int)(i ^ i >>> 32);
      i = Double.doubleToLongBits(this.maxX);
      j = 31 * j + (int)(i ^ i >>> 32);
      i = Double.doubleToLongBits(this.maxY);
      j = 31 * j + (int)(i ^ i >>> 32);
      i = Double.doubleToLongBits(this.maxZ);
      return 31 * j + (int)(i ^ i >>> 32);
   }

   public AABB contract(double d0, double d1, double d2) {
      double d3 = this.minX;
      double d4 = this.minY;
      double d5 = this.minZ;
      double d6 = this.maxX;
      double d7 = this.maxY;
      double d8 = this.maxZ;
      if (d0 < 0.0D) {
         d3 -= d0;
      } else if (d0 > 0.0D) {
         d6 -= d0;
      }

      if (d1 < 0.0D) {
         d4 -= d1;
      } else if (d1 > 0.0D) {
         d7 -= d1;
      }

      if (d2 < 0.0D) {
         d5 -= d2;
      } else if (d2 > 0.0D) {
         d8 -= d2;
      }

      return new AABB(d3, d4, d5, d6, d7, d8);
   }

   public AABB expandTowards(Vec3 vec3) {
      return this.expandTowards(vec3.x, vec3.y, vec3.z);
   }

   public AABB expandTowards(double d0, double d1, double d2) {
      double d3 = this.minX;
      double d4 = this.minY;
      double d5 = this.minZ;
      double d6 = this.maxX;
      double d7 = this.maxY;
      double d8 = this.maxZ;
      if (d0 < 0.0D) {
         d3 += d0;
      } else if (d0 > 0.0D) {
         d6 += d0;
      }

      if (d1 < 0.0D) {
         d4 += d1;
      } else if (d1 > 0.0D) {
         d7 += d1;
      }

      if (d2 < 0.0D) {
         d5 += d2;
      } else if (d2 > 0.0D) {
         d8 += d2;
      }

      return new AABB(d3, d4, d5, d6, d7, d8);
   }

   public AABB inflate(double d0, double d1, double d2) {
      double d3 = this.minX - d0;
      double d4 = this.minY - d1;
      double d5 = this.minZ - d2;
      double d6 = this.maxX + d0;
      double d7 = this.maxY + d1;
      double d8 = this.maxZ + d2;
      return new AABB(d3, d4, d5, d6, d7, d8);
   }

   public AABB inflate(double d0) {
      return this.inflate(d0, d0, d0);
   }

   public AABB intersect(AABB aabb) {
      double d0 = Math.max(this.minX, aabb.minX);
      double d1 = Math.max(this.minY, aabb.minY);
      double d2 = Math.max(this.minZ, aabb.minZ);
      double d3 = Math.min(this.maxX, aabb.maxX);
      double d4 = Math.min(this.maxY, aabb.maxY);
      double d5 = Math.min(this.maxZ, aabb.maxZ);
      return new AABB(d0, d1, d2, d3, d4, d5);
   }

   public AABB minmax(AABB aabb) {
      double d0 = Math.min(this.minX, aabb.minX);
      double d1 = Math.min(this.minY, aabb.minY);
      double d2 = Math.min(this.minZ, aabb.minZ);
      double d3 = Math.max(this.maxX, aabb.maxX);
      double d4 = Math.max(this.maxY, aabb.maxY);
      double d5 = Math.max(this.maxZ, aabb.maxZ);
      return new AABB(d0, d1, d2, d3, d4, d5);
   }

   public AABB move(double d0, double d1, double d2) {
      return new AABB(this.minX + d0, this.minY + d1, this.minZ + d2, this.maxX + d0, this.maxY + d1, this.maxZ + d2);
   }

   public AABB move(BlockPos blockpos) {
      return new AABB(this.minX + (double)blockpos.getX(), this.minY + (double)blockpos.getY(), this.minZ + (double)blockpos.getZ(), this.maxX + (double)blockpos.getX(), this.maxY + (double)blockpos.getY(), this.maxZ + (double)blockpos.getZ());
   }

   public AABB move(Vec3 vec3) {
      return this.move(vec3.x, vec3.y, vec3.z);
   }

   public boolean intersects(AABB aabb) {
      return this.intersects(aabb.minX, aabb.minY, aabb.minZ, aabb.maxX, aabb.maxY, aabb.maxZ);
   }

   public boolean intersects(double d0, double d1, double d2, double d3, double d4, double d5) {
      return this.minX < d3 && this.maxX > d0 && this.minY < d4 && this.maxY > d1 && this.minZ < d5 && this.maxZ > d2;
   }

   public boolean intersects(Vec3 vec3, Vec3 vec31) {
      return this.intersects(Math.min(vec3.x, vec31.x), Math.min(vec3.y, vec31.y), Math.min(vec3.z, vec31.z), Math.max(vec3.x, vec31.x), Math.max(vec3.y, vec31.y), Math.max(vec3.z, vec31.z));
   }

   public boolean contains(Vec3 vec3) {
      return this.contains(vec3.x, vec3.y, vec3.z);
   }

   public boolean contains(double d0, double d1, double d2) {
      return d0 >= this.minX && d0 < this.maxX && d1 >= this.minY && d1 < this.maxY && d2 >= this.minZ && d2 < this.maxZ;
   }

   public double getSize() {
      double d0 = this.getXsize();
      double d1 = this.getYsize();
      double d2 = this.getZsize();
      return (d0 + d1 + d2) / 3.0D;
   }

   public double getXsize() {
      return this.maxX - this.minX;
   }

   public double getYsize() {
      return this.maxY - this.minY;
   }

   public double getZsize() {
      return this.maxZ - this.minZ;
   }

   public AABB deflate(double d0, double d1, double d2) {
      return this.inflate(-d0, -d1, -d2);
   }

   public AABB deflate(double d0) {
      return this.inflate(-d0);
   }

   public Optional<Vec3> clip(Vec3 vec3, Vec3 vec31) {
      double[] adouble = new double[]{1.0D};
      double d0 = vec31.x - vec3.x;
      double d1 = vec31.y - vec3.y;
      double d2 = vec31.z - vec3.z;
      Direction direction = getDirection(this, vec3, adouble, (Direction)null, d0, d1, d2);
      if (direction == null) {
         return Optional.empty();
      } else {
         double d3 = adouble[0];
         return Optional.of(vec3.add(d3 * d0, d3 * d1, d3 * d2));
      }
   }

   @Nullable
   public static BlockHitResult clip(Iterable<AABB> iterable, Vec3 vec3, Vec3 vec31, BlockPos blockpos) {
      double[] adouble = new double[]{1.0D};
      Direction direction = null;
      double d0 = vec31.x - vec3.x;
      double d1 = vec31.y - vec3.y;
      double d2 = vec31.z - vec3.z;

      for(AABB aabb : iterable) {
         direction = getDirection(aabb.move(blockpos), vec3, adouble, direction, d0, d1, d2);
      }

      if (direction == null) {
         return null;
      } else {
         double d3 = adouble[0];
         return new BlockHitResult(vec3.add(d3 * d0, d3 * d1, d3 * d2), direction, blockpos, false);
      }
   }

   @Nullable
   private static Direction getDirection(AABB aabb, Vec3 vec3, double[] adouble, @Nullable Direction direction, double d0, double d1, double d2) {
      if (d0 > 1.0E-7D) {
         direction = clipPoint(adouble, direction, d0, d1, d2, aabb.minX, aabb.minY, aabb.maxY, aabb.minZ, aabb.maxZ, Direction.WEST, vec3.x, vec3.y, vec3.z);
      } else if (d0 < -1.0E-7D) {
         direction = clipPoint(adouble, direction, d0, d1, d2, aabb.maxX, aabb.minY, aabb.maxY, aabb.minZ, aabb.maxZ, Direction.EAST, vec3.x, vec3.y, vec3.z);
      }

      if (d1 > 1.0E-7D) {
         direction = clipPoint(adouble, direction, d1, d2, d0, aabb.minY, aabb.minZ, aabb.maxZ, aabb.minX, aabb.maxX, Direction.DOWN, vec3.y, vec3.z, vec3.x);
      } else if (d1 < -1.0E-7D) {
         direction = clipPoint(adouble, direction, d1, d2, d0, aabb.maxY, aabb.minZ, aabb.maxZ, aabb.minX, aabb.maxX, Direction.UP, vec3.y, vec3.z, vec3.x);
      }

      if (d2 > 1.0E-7D) {
         direction = clipPoint(adouble, direction, d2, d0, d1, aabb.minZ, aabb.minX, aabb.maxX, aabb.minY, aabb.maxY, Direction.NORTH, vec3.z, vec3.x, vec3.y);
      } else if (d2 < -1.0E-7D) {
         direction = clipPoint(adouble, direction, d2, d0, d1, aabb.maxZ, aabb.minX, aabb.maxX, aabb.minY, aabb.maxY, Direction.SOUTH, vec3.z, vec3.x, vec3.y);
      }

      return direction;
   }

   @Nullable
   private static Direction clipPoint(double[] adouble, @Nullable Direction direction, double d0, double d1, double d2, double d3, double d4, double d5, double d6, double d7, Direction direction1, double d8, double d9, double d10) {
      double d11 = (d3 - d8) / d0;
      double d12 = d9 + d11 * d1;
      double d13 = d10 + d11 * d2;
      if (0.0D < d11 && d11 < adouble[0] && d4 - 1.0E-7D < d12 && d12 < d5 + 1.0E-7D && d6 - 1.0E-7D < d13 && d13 < d7 + 1.0E-7D) {
         adouble[0] = d11;
         return direction1;
      } else {
         return direction;
      }
   }

   public double distanceToSqr(Vec3 vec3) {
      double d0 = Math.max(Math.max(this.minX - vec3.x, vec3.x - this.maxX), 0.0D);
      double d1 = Math.max(Math.max(this.minY - vec3.y, vec3.y - this.maxY), 0.0D);
      double d2 = Math.max(Math.max(this.minZ - vec3.z, vec3.z - this.maxZ), 0.0D);
      return Mth.lengthSquared(d0, d1, d2);
   }

   public String toString() {
      return "AABB[" + this.minX + ", " + this.minY + ", " + this.minZ + "] -> [" + this.maxX + ", " + this.maxY + ", " + this.maxZ + "]";
   }

   public boolean hasNaN() {
      return Double.isNaN(this.minX) || Double.isNaN(this.minY) || Double.isNaN(this.minZ) || Double.isNaN(this.maxX) || Double.isNaN(this.maxY) || Double.isNaN(this.maxZ);
   }

   public Vec3 getCenter() {
      return new Vec3(Mth.lerp(0.5D, this.minX, this.maxX), Mth.lerp(0.5D, this.minY, this.maxY), Mth.lerp(0.5D, this.minZ, this.maxZ));
   }

   public static AABB ofSize(Vec3 vec3, double d0, double d1, double d2) {
      return new AABB(vec3.x - d0 / 2.0D, vec3.y - d1 / 2.0D, vec3.z - d2 / 2.0D, vec3.x + d0 / 2.0D, vec3.y + d1 / 2.0D, vec3.z + d2 / 2.0D);
   }
}
