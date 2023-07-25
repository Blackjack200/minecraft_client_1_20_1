package net.minecraft.world.level.levelgen;

import java.util.Optional;
import java.util.OptionalInt;
import java.util.function.Predicate;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.level.LevelSimulatedReader;
import net.minecraft.world.level.block.state.BlockState;

public abstract class Column {
   public static Column.Range around(int i, int j) {
      return new Column.Range(i - 1, j + 1);
   }

   public static Column.Range inside(int i, int j) {
      return new Column.Range(i, j);
   }

   public static Column below(int i) {
      return new Column.Ray(i, false);
   }

   public static Column fromHighest(int i) {
      return new Column.Ray(i + 1, false);
   }

   public static Column above(int i) {
      return new Column.Ray(i, true);
   }

   public static Column fromLowest(int i) {
      return new Column.Ray(i - 1, true);
   }

   public static Column line() {
      return Column.Line.INSTANCE;
   }

   public static Column create(OptionalInt optionalint, OptionalInt optionalint1) {
      if (optionalint.isPresent() && optionalint1.isPresent()) {
         return inside(optionalint.getAsInt(), optionalint1.getAsInt());
      } else if (optionalint.isPresent()) {
         return above(optionalint.getAsInt());
      } else {
         return optionalint1.isPresent() ? below(optionalint1.getAsInt()) : line();
      }
   }

   public abstract OptionalInt getCeiling();

   public abstract OptionalInt getFloor();

   public abstract OptionalInt getHeight();

   public Column withFloor(OptionalInt optionalint) {
      return create(optionalint, this.getCeiling());
   }

   public Column withCeiling(OptionalInt optionalint) {
      return create(this.getFloor(), optionalint);
   }

   public static Optional<Column> scan(LevelSimulatedReader levelsimulatedreader, BlockPos blockpos, int i, Predicate<BlockState> predicate, Predicate<BlockState> predicate1) {
      BlockPos.MutableBlockPos blockpos_mutableblockpos = blockpos.mutable();
      if (!levelsimulatedreader.isStateAtPosition(blockpos, predicate)) {
         return Optional.empty();
      } else {
         int j = blockpos.getY();
         OptionalInt optionalint = scanDirection(levelsimulatedreader, i, predicate, predicate1, blockpos_mutableblockpos, j, Direction.UP);
         OptionalInt optionalint1 = scanDirection(levelsimulatedreader, i, predicate, predicate1, blockpos_mutableblockpos, j, Direction.DOWN);
         return Optional.of(create(optionalint1, optionalint));
      }
   }

   private static OptionalInt scanDirection(LevelSimulatedReader levelsimulatedreader, int i, Predicate<BlockState> predicate, Predicate<BlockState> predicate1, BlockPos.MutableBlockPos blockpos_mutableblockpos, int j, Direction direction) {
      blockpos_mutableblockpos.setY(j);

      for(int k = 1; k < i && levelsimulatedreader.isStateAtPosition(blockpos_mutableblockpos, predicate); ++k) {
         blockpos_mutableblockpos.move(direction);
      }

      return levelsimulatedreader.isStateAtPosition(blockpos_mutableblockpos, predicate1) ? OptionalInt.of(blockpos_mutableblockpos.getY()) : OptionalInt.empty();
   }

   public static final class Line extends Column {
      static final Column.Line INSTANCE = new Column.Line();

      private Line() {
      }

      public OptionalInt getCeiling() {
         return OptionalInt.empty();
      }

      public OptionalInt getFloor() {
         return OptionalInt.empty();
      }

      public OptionalInt getHeight() {
         return OptionalInt.empty();
      }

      public String toString() {
         return "C(-)";
      }
   }

   public static final class Range extends Column {
      private final int floor;
      private final int ceiling;

      protected Range(int i, int j) {
         this.floor = i;
         this.ceiling = j;
         if (this.height() < 0) {
            throw new IllegalArgumentException("Column of negative height: " + this);
         }
      }

      public OptionalInt getCeiling() {
         return OptionalInt.of(this.ceiling);
      }

      public OptionalInt getFloor() {
         return OptionalInt.of(this.floor);
      }

      public OptionalInt getHeight() {
         return OptionalInt.of(this.height());
      }

      public int ceiling() {
         return this.ceiling;
      }

      public int floor() {
         return this.floor;
      }

      public int height() {
         return this.ceiling - this.floor - 1;
      }

      public String toString() {
         return "C(" + this.ceiling + "-" + this.floor + ")";
      }
   }

   public static final class Ray extends Column {
      private final int edge;
      private final boolean pointingUp;

      public Ray(int i, boolean flag) {
         this.edge = i;
         this.pointingUp = flag;
      }

      public OptionalInt getCeiling() {
         return this.pointingUp ? OptionalInt.empty() : OptionalInt.of(this.edge);
      }

      public OptionalInt getFloor() {
         return this.pointingUp ? OptionalInt.of(this.edge) : OptionalInt.empty();
      }

      public OptionalInt getHeight() {
         return OptionalInt.empty();
      }

      public String toString() {
         return this.pointingUp ? "C(" + this.edge + "-)" : "C(-" + this.edge + ")";
      }
   }
}
