package net.minecraft.core;

public enum AxisCycle {
   NONE {
      public int cycle(int i, int j, int k, Direction.Axis direction_axis) {
         return direction_axis.choose(i, j, k);
      }

      public double cycle(double d0, double d1, double d2, Direction.Axis direction_axis) {
         return direction_axis.choose(d0, d1, d2);
      }

      public Direction.Axis cycle(Direction.Axis direction_axis) {
         return direction_axis;
      }

      public AxisCycle inverse() {
         return this;
      }
   },
   FORWARD {
      public int cycle(int i, int j, int k, Direction.Axis direction_axis) {
         return direction_axis.choose(k, i, j);
      }

      public double cycle(double d0, double d1, double d2, Direction.Axis direction_axis) {
         return direction_axis.choose(d2, d0, d1);
      }

      public Direction.Axis cycle(Direction.Axis direction_axis) {
         return d[Math.floorMod(direction_axis.ordinal() + 1, 3)];
      }

      public AxisCycle inverse() {
         return c;
      }
   },
   BACKWARD {
      public int cycle(int i, int j, int k, Direction.Axis direction_axis) {
         return direction_axis.choose(j, k, i);
      }

      public double cycle(double d0, double d1, double d2, Direction.Axis direction_axis) {
         return direction_axis.choose(d1, d2, d0);
      }

      public Direction.Axis cycle(Direction.Axis direction_axis) {
         return d[Math.floorMod(direction_axis.ordinal() - 1, 3)];
      }

      public AxisCycle inverse() {
         return b;
      }
   };

   public static final Direction.Axis[] AXIS_VALUES = Direction.Axis.values();
   public static final AxisCycle[] VALUES = values();

   public abstract int cycle(int i, int j, int k, Direction.Axis direction_axis);

   public abstract double cycle(double d0, double d1, double d2, Direction.Axis direction_axis);

   public abstract Direction.Axis cycle(Direction.Axis direction_axis);

   public abstract AxisCycle inverse();

   public static AxisCycle between(Direction.Axis direction_axis, Direction.Axis direction_axis1) {
      return VALUES[Math.floorMod(direction_axis1.ordinal() - direction_axis.ordinal(), 3)];
   }
}
