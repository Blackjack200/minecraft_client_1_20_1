package net.minecraft.core;

import com.google.common.collect.Iterators;
import com.mojang.serialization.Codec;
import com.mojang.serialization.DataResult;
import java.util.Arrays;
import java.util.Collection;
import java.util.Comparator;
import java.util.Iterator;
import java.util.List;
import java.util.function.Predicate;
import java.util.stream.Stream;
import javax.annotation.Nullable;
import net.minecraft.Util;
import net.minecraft.util.ExtraCodecs;
import net.minecraft.util.Mth;
import net.minecraft.util.RandomSource;
import net.minecraft.util.StringRepresentable;
import net.minecraft.world.entity.Entity;
import org.joml.Matrix4f;
import org.joml.Quaternionf;
import org.joml.Vector3f;
import org.joml.Vector4f;

public enum Direction implements StringRepresentable {
   DOWN(0, 1, -1, "down", Direction.AxisDirection.NEGATIVE, Direction.Axis.Y, new Vec3i(0, -1, 0)),
   UP(1, 0, -1, "up", Direction.AxisDirection.POSITIVE, Direction.Axis.Y, new Vec3i(0, 1, 0)),
   NORTH(2, 3, 2, "north", Direction.AxisDirection.NEGATIVE, Direction.Axis.Z, new Vec3i(0, 0, -1)),
   SOUTH(3, 2, 0, "south", Direction.AxisDirection.POSITIVE, Direction.Axis.Z, new Vec3i(0, 0, 1)),
   WEST(4, 5, 1, "west", Direction.AxisDirection.NEGATIVE, Direction.Axis.X, new Vec3i(-1, 0, 0)),
   EAST(5, 4, 3, "east", Direction.AxisDirection.POSITIVE, Direction.Axis.X, new Vec3i(1, 0, 0));

   public static final StringRepresentable.EnumCodec<Direction> CODEC = StringRepresentable.fromEnum(Direction::values);
   public static final Codec<Direction> VERTICAL_CODEC = ExtraCodecs.validate(CODEC, Direction::verifyVertical);
   private final int data3d;
   private final int oppositeIndex;
   private final int data2d;
   private final String name;
   private final Direction.Axis axis;
   private final Direction.AxisDirection axisDirection;
   private final Vec3i normal;
   private static final Direction[] VALUES = values();
   private static final Direction[] BY_3D_DATA = Arrays.stream(VALUES).sorted(Comparator.comparingInt((direction) -> direction.data3d)).toArray((i) -> new Direction[i]);
   private static final Direction[] BY_2D_DATA = Arrays.stream(VALUES).filter((direction) -> direction.getAxis().isHorizontal()).sorted(Comparator.comparingInt((direction) -> direction.data2d)).toArray((i) -> new Direction[i]);

   private Direction(int i, int j, int k, String s, Direction.AxisDirection direction_axisdirection, Direction.Axis direction_axis, Vec3i vec3i) {
      this.data3d = i;
      this.data2d = k;
      this.oppositeIndex = j;
      this.name = s;
      this.axis = direction_axis;
      this.axisDirection = direction_axisdirection;
      this.normal = vec3i;
   }

   public static Direction[] orderedByNearest(Entity entity) {
      float f = entity.getViewXRot(1.0F) * ((float)Math.PI / 180F);
      float f1 = -entity.getViewYRot(1.0F) * ((float)Math.PI / 180F);
      float f2 = Mth.sin(f);
      float f3 = Mth.cos(f);
      float f4 = Mth.sin(f1);
      float f5 = Mth.cos(f1);
      boolean flag = f4 > 0.0F;
      boolean flag1 = f2 < 0.0F;
      boolean flag2 = f5 > 0.0F;
      float f6 = flag ? f4 : -f4;
      float f7 = flag1 ? -f2 : f2;
      float f8 = flag2 ? f5 : -f5;
      float f9 = f6 * f3;
      float f10 = f8 * f3;
      Direction direction = flag ? EAST : WEST;
      Direction direction1 = flag1 ? UP : DOWN;
      Direction direction2 = flag2 ? SOUTH : NORTH;
      if (f6 > f8) {
         if (f7 > f9) {
            return makeDirectionArray(direction1, direction, direction2);
         } else {
            return f10 > f7 ? makeDirectionArray(direction, direction2, direction1) : makeDirectionArray(direction, direction1, direction2);
         }
      } else if (f7 > f10) {
         return makeDirectionArray(direction1, direction2, direction);
      } else {
         return f9 > f7 ? makeDirectionArray(direction2, direction, direction1) : makeDirectionArray(direction2, direction1, direction);
      }
   }

   private static Direction[] makeDirectionArray(Direction direction, Direction direction1, Direction direction2) {
      return new Direction[]{direction, direction1, direction2, direction2.getOpposite(), direction1.getOpposite(), direction.getOpposite()};
   }

   public static Direction rotate(Matrix4f matrix4f, Direction direction) {
      Vec3i vec3i = direction.getNormal();
      Vector4f vector4f = matrix4f.transform(new Vector4f((float)vec3i.getX(), (float)vec3i.getY(), (float)vec3i.getZ(), 0.0F));
      return getNearest(vector4f.x(), vector4f.y(), vector4f.z());
   }

   public static Collection<Direction> allShuffled(RandomSource randomsource) {
      return Util.shuffledCopy(values(), randomsource);
   }

   public static Stream<Direction> stream() {
      return Stream.of(VALUES);
   }

   public Quaternionf getRotation() {
      Quaternionf var10000;
      switch (this) {
         case DOWN:
            var10000 = (new Quaternionf()).rotationX((float)Math.PI);
            break;
         case UP:
            var10000 = new Quaternionf();
            break;
         case NORTH:
            var10000 = (new Quaternionf()).rotationXYZ(((float)Math.PI / 2F), 0.0F, (float)Math.PI);
            break;
         case SOUTH:
            var10000 = (new Quaternionf()).rotationX(((float)Math.PI / 2F));
            break;
         case WEST:
            var10000 = (new Quaternionf()).rotationXYZ(((float)Math.PI / 2F), 0.0F, ((float)Math.PI / 2F));
            break;
         case EAST:
            var10000 = (new Quaternionf()).rotationXYZ(((float)Math.PI / 2F), 0.0F, (-(float)Math.PI / 2F));
            break;
         default:
            throw new IncompatibleClassChangeError();
      }

      return var10000;
   }

   public int get3DDataValue() {
      return this.data3d;
   }

   public int get2DDataValue() {
      return this.data2d;
   }

   public Direction.AxisDirection getAxisDirection() {
      return this.axisDirection;
   }

   public static Direction getFacingAxis(Entity entity, Direction.Axis direction_axis) {
      Direction var10000;
      switch (direction_axis) {
         case X:
            var10000 = EAST.isFacingAngle(entity.getViewYRot(1.0F)) ? EAST : WEST;
            break;
         case Z:
            var10000 = SOUTH.isFacingAngle(entity.getViewYRot(1.0F)) ? SOUTH : NORTH;
            break;
         case Y:
            var10000 = entity.getViewXRot(1.0F) < 0.0F ? UP : DOWN;
            break;
         default:
            throw new IncompatibleClassChangeError();
      }

      return var10000;
   }

   public Direction getOpposite() {
      return from3DDataValue(this.oppositeIndex);
   }

   public Direction getClockWise(Direction.Axis direction_axis) {
      Direction var10000;
      switch (direction_axis) {
         case X:
            var10000 = this != WEST && this != EAST ? this.getClockWiseX() : this;
            break;
         case Z:
            var10000 = this != NORTH && this != SOUTH ? this.getClockWiseZ() : this;
            break;
         case Y:
            var10000 = this != UP && this != DOWN ? this.getClockWise() : this;
            break;
         default:
            throw new IncompatibleClassChangeError();
      }

      return var10000;
   }

   public Direction getCounterClockWise(Direction.Axis direction_axis) {
      Direction var10000;
      switch (direction_axis) {
         case X:
            var10000 = this != WEST && this != EAST ? this.getCounterClockWiseX() : this;
            break;
         case Z:
            var10000 = this != NORTH && this != SOUTH ? this.getCounterClockWiseZ() : this;
            break;
         case Y:
            var10000 = this != UP && this != DOWN ? this.getCounterClockWise() : this;
            break;
         default:
            throw new IncompatibleClassChangeError();
      }

      return var10000;
   }

   public Direction getClockWise() {
      Direction var10000;
      switch (this) {
         case NORTH:
            var10000 = EAST;
            break;
         case SOUTH:
            var10000 = WEST;
            break;
         case WEST:
            var10000 = NORTH;
            break;
         case EAST:
            var10000 = SOUTH;
            break;
         default:
            throw new IllegalStateException("Unable to get Y-rotated facing of " + this);
      }

      return var10000;
   }

   private Direction getClockWiseX() {
      Direction var10000;
      switch (this) {
         case DOWN:
            var10000 = SOUTH;
            break;
         case UP:
            var10000 = NORTH;
            break;
         case NORTH:
            var10000 = DOWN;
            break;
         case SOUTH:
            var10000 = UP;
            break;
         default:
            throw new IllegalStateException("Unable to get X-rotated facing of " + this);
      }

      return var10000;
   }

   private Direction getCounterClockWiseX() {
      Direction var10000;
      switch (this) {
         case DOWN:
            var10000 = NORTH;
            break;
         case UP:
            var10000 = SOUTH;
            break;
         case NORTH:
            var10000 = UP;
            break;
         case SOUTH:
            var10000 = DOWN;
            break;
         default:
            throw new IllegalStateException("Unable to get X-rotated facing of " + this);
      }

      return var10000;
   }

   private Direction getClockWiseZ() {
      Direction var10000;
      switch (this) {
         case DOWN:
            var10000 = WEST;
            break;
         case UP:
            var10000 = EAST;
            break;
         case NORTH:
         case SOUTH:
         default:
            throw new IllegalStateException("Unable to get Z-rotated facing of " + this);
         case WEST:
            var10000 = UP;
            break;
         case EAST:
            var10000 = DOWN;
      }

      return var10000;
   }

   private Direction getCounterClockWiseZ() {
      Direction var10000;
      switch (this) {
         case DOWN:
            var10000 = EAST;
            break;
         case UP:
            var10000 = WEST;
            break;
         case NORTH:
         case SOUTH:
         default:
            throw new IllegalStateException("Unable to get Z-rotated facing of " + this);
         case WEST:
            var10000 = DOWN;
            break;
         case EAST:
            var10000 = UP;
      }

      return var10000;
   }

   public Direction getCounterClockWise() {
      Direction var10000;
      switch (this) {
         case NORTH:
            var10000 = WEST;
            break;
         case SOUTH:
            var10000 = EAST;
            break;
         case WEST:
            var10000 = SOUTH;
            break;
         case EAST:
            var10000 = NORTH;
            break;
         default:
            throw new IllegalStateException("Unable to get CCW facing of " + this);
      }

      return var10000;
   }

   public int getStepX() {
      return this.normal.getX();
   }

   public int getStepY() {
      return this.normal.getY();
   }

   public int getStepZ() {
      return this.normal.getZ();
   }

   public Vector3f step() {
      return new Vector3f((float)this.getStepX(), (float)this.getStepY(), (float)this.getStepZ());
   }

   public String getName() {
      return this.name;
   }

   public Direction.Axis getAxis() {
      return this.axis;
   }

   @Nullable
   public static Direction byName(@Nullable String s) {
      return CODEC.byName(s);
   }

   public static Direction from3DDataValue(int i) {
      return BY_3D_DATA[Mth.abs(i % BY_3D_DATA.length)];
   }

   public static Direction from2DDataValue(int i) {
      return BY_2D_DATA[Mth.abs(i % BY_2D_DATA.length)];
   }

   @Nullable
   public static Direction fromDelta(int i, int j, int k) {
      if (i == 0) {
         if (j == 0) {
            if (k > 0) {
               return SOUTH;
            }

            if (k < 0) {
               return NORTH;
            }
         } else if (k == 0) {
            if (j > 0) {
               return UP;
            }

            return DOWN;
         }
      } else if (j == 0 && k == 0) {
         if (i > 0) {
            return EAST;
         }

         return WEST;
      }

      return null;
   }

   public static Direction fromYRot(double d0) {
      return from2DDataValue(Mth.floor(d0 / 90.0D + 0.5D) & 3);
   }

   public static Direction fromAxisAndDirection(Direction.Axis direction_axis, Direction.AxisDirection direction_axisdirection) {
      Direction var10000;
      switch (direction_axis) {
         case X:
            var10000 = direction_axisdirection == Direction.AxisDirection.POSITIVE ? EAST : WEST;
            break;
         case Z:
            var10000 = direction_axisdirection == Direction.AxisDirection.POSITIVE ? SOUTH : NORTH;
            break;
         case Y:
            var10000 = direction_axisdirection == Direction.AxisDirection.POSITIVE ? UP : DOWN;
            break;
         default:
            throw new IncompatibleClassChangeError();
      }

      return var10000;
   }

   public float toYRot() {
      return (float)((this.data2d & 3) * 90);
   }

   public static Direction getRandom(RandomSource randomsource) {
      return Util.getRandom(VALUES, randomsource);
   }

   public static Direction getNearest(double d0, double d1, double d2) {
      return getNearest((float)d0, (float)d1, (float)d2);
   }

   public static Direction getNearest(float f, float f1, float f2) {
      Direction direction = NORTH;
      float f3 = Float.MIN_VALUE;

      for(Direction direction1 : VALUES) {
         float f4 = f * (float)direction1.normal.getX() + f1 * (float)direction1.normal.getY() + f2 * (float)direction1.normal.getZ();
         if (f4 > f3) {
            f3 = f4;
            direction = direction1;
         }
      }

      return direction;
   }

   public String toString() {
      return this.name;
   }

   public String getSerializedName() {
      return this.name;
   }

   private static DataResult<Direction> verifyVertical(Direction direction) {
      return direction.getAxis().isVertical() ? DataResult.success(direction) : DataResult.error(() -> "Expected a vertical direction");
   }

   public static Direction get(Direction.AxisDirection direction_axisdirection, Direction.Axis direction_axis) {
      for(Direction direction : VALUES) {
         if (direction.getAxisDirection() == direction_axisdirection && direction.getAxis() == direction_axis) {
            return direction;
         }
      }

      throw new IllegalArgumentException("No such direction: " + direction_axisdirection + " " + direction_axis);
   }

   public Vec3i getNormal() {
      return this.normal;
   }

   public boolean isFacingAngle(float f) {
      float f1 = f * ((float)Math.PI / 180F);
      float f2 = -Mth.sin(f1);
      float f3 = Mth.cos(f1);
      return (float)this.normal.getX() * f2 + (float)this.normal.getZ() * f3 > 0.0F;
   }

   public static enum Axis implements StringRepresentable, Predicate<Direction> {
      X("x") {
         public int choose(int i, int j, int k) {
            return i;
         }

         public double choose(double d0, double d1, double d2) {
            return d0;
         }
      },
      Y("y") {
         public int choose(int i, int j, int k) {
            return j;
         }

         public double choose(double d0, double d1, double d2) {
            return d1;
         }
      },
      Z("z") {
         public int choose(int i, int j, int k) {
            return k;
         }

         public double choose(double d0, double d1, double d2) {
            return d2;
         }
      };

      public static final Direction.Axis[] VALUES = values();
      public static final StringRepresentable.EnumCodec<Direction.Axis> CODEC = StringRepresentable.fromEnum(Direction.Axis::values);
      private final String name;

      Axis(String s) {
         this.name = s;
      }

      @Nullable
      public static Direction.Axis byName(String s) {
         return CODEC.byName(s);
      }

      public String getName() {
         return this.name;
      }

      public boolean isVertical() {
         return this == Y;
      }

      public boolean isHorizontal() {
         return this == X || this == Z;
      }

      public String toString() {
         return this.name;
      }

      public static Direction.Axis getRandom(RandomSource randomsource) {
         return Util.getRandom(VALUES, randomsource);
      }

      public boolean test(@Nullable Direction direction) {
         return direction != null && direction.getAxis() == this;
      }

      public Direction.Plane getPlane() {
         Direction.Plane var10000;
         switch (this) {
            case X:
            case Z:
               var10000 = Direction.Plane.HORIZONTAL;
               break;
            case Y:
               var10000 = Direction.Plane.VERTICAL;
               break;
            default:
               throw new IncompatibleClassChangeError();
         }

         return var10000;
      }

      public String getSerializedName() {
         return this.name;
      }

      public abstract int choose(int i, int j, int k);

      public abstract double choose(double d0, double d1, double d2);
   }

   public static enum AxisDirection {
      POSITIVE(1, "Towards positive"),
      NEGATIVE(-1, "Towards negative");

      private final int step;
      private final String name;

      private AxisDirection(int i, String s) {
         this.step = i;
         this.name = s;
      }

      public int getStep() {
         return this.step;
      }

      public String getName() {
         return this.name;
      }

      public String toString() {
         return this.name;
      }

      public Direction.AxisDirection opposite() {
         return this == POSITIVE ? NEGATIVE : POSITIVE;
      }
   }

   public static enum Plane implements Iterable<Direction>, Predicate<Direction> {
      HORIZONTAL(new Direction[]{Direction.NORTH, Direction.EAST, Direction.SOUTH, Direction.WEST}, new Direction.Axis[]{Direction.Axis.X, Direction.Axis.Z}),
      VERTICAL(new Direction[]{Direction.UP, Direction.DOWN}, new Direction.Axis[]{Direction.Axis.Y});

      private final Direction[] faces;
      private final Direction.Axis[] axis;

      private Plane(Direction[] adirection, Direction.Axis[] adirection_axis) {
         this.faces = adirection;
         this.axis = adirection_axis;
      }

      public Direction getRandomDirection(RandomSource randomsource) {
         return Util.getRandom(this.faces, randomsource);
      }

      public Direction.Axis getRandomAxis(RandomSource randomsource) {
         return Util.getRandom(this.axis, randomsource);
      }

      public boolean test(@Nullable Direction direction) {
         return direction != null && direction.getAxis().getPlane() == this;
      }

      public Iterator<Direction> iterator() {
         return Iterators.forArray(this.faces);
      }

      public Stream<Direction> stream() {
         return Arrays.stream(this.faces);
      }

      public List<Direction> shuffledCopy(RandomSource randomsource) {
         return Util.shuffledCopy(this.faces, randomsource);
      }
   }
}
