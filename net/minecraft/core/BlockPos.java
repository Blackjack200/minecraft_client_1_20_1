package net.minecraft.core;

import com.google.common.collect.AbstractIterator;
import com.mojang.logging.LogUtils;
import com.mojang.serialization.Codec;
import it.unimi.dsi.fastutil.longs.LongOpenHashSet;
import it.unimi.dsi.fastutil.longs.LongSet;
import java.util.ArrayDeque;
import java.util.Optional;
import java.util.Queue;
import java.util.function.BiConsumer;
import java.util.function.Consumer;
import java.util.function.Predicate;
import java.util.stream.IntStream;
import java.util.stream.Stream;
import java.util.stream.StreamSupport;
import javax.annotation.concurrent.Immutable;
import net.minecraft.Util;
import net.minecraft.util.Mth;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.block.Rotation;
import net.minecraft.world.level.levelgen.structure.BoundingBox;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import org.apache.commons.lang3.Validate;
import org.apache.commons.lang3.tuple.Pair;
import org.slf4j.Logger;

@Immutable
public class BlockPos extends Vec3i {
   public static final Codec<BlockPos> CODEC = Codec.INT_STREAM.comapFlatMap((intstream) -> Util.fixedSize(intstream, 3).map((aint) -> new BlockPos(aint[0], aint[1], aint[2])), (blockpos) -> IntStream.of(blockpos.getX(), blockpos.getY(), blockpos.getZ())).stable();
   private static final Logger LOGGER = LogUtils.getLogger();
   public static final BlockPos ZERO = new BlockPos(0, 0, 0);
   private static final int PACKED_X_LENGTH = 1 + Mth.log2(Mth.smallestEncompassingPowerOfTwo(30000000));
   private static final int PACKED_Z_LENGTH = PACKED_X_LENGTH;
   public static final int PACKED_Y_LENGTH = 64 - PACKED_X_LENGTH - PACKED_Z_LENGTH;
   private static final long PACKED_X_MASK = (1L << PACKED_X_LENGTH) - 1L;
   private static final long PACKED_Y_MASK = (1L << PACKED_Y_LENGTH) - 1L;
   private static final long PACKED_Z_MASK = (1L << PACKED_Z_LENGTH) - 1L;
   private static final int Y_OFFSET = 0;
   private static final int Z_OFFSET = PACKED_Y_LENGTH;
   private static final int X_OFFSET = PACKED_Y_LENGTH + PACKED_Z_LENGTH;

   public BlockPos(int i, int j, int k) {
      super(i, j, k);
   }

   public BlockPos(Vec3i vec3i) {
      this(vec3i.getX(), vec3i.getY(), vec3i.getZ());
   }

   public static long offset(long i, Direction direction) {
      return offset(i, direction.getStepX(), direction.getStepY(), direction.getStepZ());
   }

   public static long offset(long i, int j, int k, int l) {
      return asLong(getX(i) + j, getY(i) + k, getZ(i) + l);
   }

   public static int getX(long i) {
      return (int)(i << 64 - X_OFFSET - PACKED_X_LENGTH >> 64 - PACKED_X_LENGTH);
   }

   public static int getY(long i) {
      return (int)(i << 64 - PACKED_Y_LENGTH >> 64 - PACKED_Y_LENGTH);
   }

   public static int getZ(long i) {
      return (int)(i << 64 - Z_OFFSET - PACKED_Z_LENGTH >> 64 - PACKED_Z_LENGTH);
   }

   public static BlockPos of(long i) {
      return new BlockPos(getX(i), getY(i), getZ(i));
   }

   public static BlockPos containing(double d0, double d1, double d2) {
      return new BlockPos(Mth.floor(d0), Mth.floor(d1), Mth.floor(d2));
   }

   public static BlockPos containing(Position position) {
      return containing(position.x(), position.y(), position.z());
   }

   public long asLong() {
      return asLong(this.getX(), this.getY(), this.getZ());
   }

   public static long asLong(int i, int j, int k) {
      long l = 0L;
      l |= ((long)i & PACKED_X_MASK) << X_OFFSET;
      l |= ((long)j & PACKED_Y_MASK) << 0;
      return l | ((long)k & PACKED_Z_MASK) << Z_OFFSET;
   }

   public static long getFlatIndex(long i) {
      return i & -16L;
   }

   public BlockPos offset(int i, int j, int k) {
      return i == 0 && j == 0 && k == 0 ? this : new BlockPos(this.getX() + i, this.getY() + j, this.getZ() + k);
   }

   public Vec3 getCenter() {
      return Vec3.atCenterOf(this);
   }

   public BlockPos offset(Vec3i vec3i) {
      return this.offset(vec3i.getX(), vec3i.getY(), vec3i.getZ());
   }

   public BlockPos subtract(Vec3i vec3i) {
      return this.offset(-vec3i.getX(), -vec3i.getY(), -vec3i.getZ());
   }

   public BlockPos multiply(int i) {
      if (i == 1) {
         return this;
      } else {
         return i == 0 ? ZERO : new BlockPos(this.getX() * i, this.getY() * i, this.getZ() * i);
      }
   }

   public BlockPos above() {
      return this.relative(Direction.UP);
   }

   public BlockPos above(int i) {
      return this.relative(Direction.UP, i);
   }

   public BlockPos below() {
      return this.relative(Direction.DOWN);
   }

   public BlockPos below(int i) {
      return this.relative(Direction.DOWN, i);
   }

   public BlockPos north() {
      return this.relative(Direction.NORTH);
   }

   public BlockPos north(int i) {
      return this.relative(Direction.NORTH, i);
   }

   public BlockPos south() {
      return this.relative(Direction.SOUTH);
   }

   public BlockPos south(int i) {
      return this.relative(Direction.SOUTH, i);
   }

   public BlockPos west() {
      return this.relative(Direction.WEST);
   }

   public BlockPos west(int i) {
      return this.relative(Direction.WEST, i);
   }

   public BlockPos east() {
      return this.relative(Direction.EAST);
   }

   public BlockPos east(int i) {
      return this.relative(Direction.EAST, i);
   }

   public BlockPos relative(Direction direction) {
      return new BlockPos(this.getX() + direction.getStepX(), this.getY() + direction.getStepY(), this.getZ() + direction.getStepZ());
   }

   public BlockPos relative(Direction direction, int i) {
      return i == 0 ? this : new BlockPos(this.getX() + direction.getStepX() * i, this.getY() + direction.getStepY() * i, this.getZ() + direction.getStepZ() * i);
   }

   public BlockPos relative(Direction.Axis direction_axis, int i) {
      if (i == 0) {
         return this;
      } else {
         int j = direction_axis == Direction.Axis.X ? i : 0;
         int k = direction_axis == Direction.Axis.Y ? i : 0;
         int l = direction_axis == Direction.Axis.Z ? i : 0;
         return new BlockPos(this.getX() + j, this.getY() + k, this.getZ() + l);
      }
   }

   public BlockPos rotate(Rotation rotation) {
      switch (rotation) {
         case NONE:
         default:
            return this;
         case CLOCKWISE_90:
            return new BlockPos(-this.getZ(), this.getY(), this.getX());
         case CLOCKWISE_180:
            return new BlockPos(-this.getX(), this.getY(), -this.getZ());
         case COUNTERCLOCKWISE_90:
            return new BlockPos(this.getZ(), this.getY(), -this.getX());
      }
   }

   public BlockPos cross(Vec3i vec3i) {
      return new BlockPos(this.getY() * vec3i.getZ() - this.getZ() * vec3i.getY(), this.getZ() * vec3i.getX() - this.getX() * vec3i.getZ(), this.getX() * vec3i.getY() - this.getY() * vec3i.getX());
   }

   public BlockPos atY(int i) {
      return new BlockPos(this.getX(), i, this.getZ());
   }

   public BlockPos immutable() {
      return this;
   }

   public BlockPos.MutableBlockPos mutable() {
      return new BlockPos.MutableBlockPos(this.getX(), this.getY(), this.getZ());
   }

   public static Iterable<BlockPos> randomInCube(RandomSource randomsource, int i, BlockPos blockpos, int j) {
      return randomBetweenClosed(randomsource, i, blockpos.getX() - j, blockpos.getY() - j, blockpos.getZ() - j, blockpos.getX() + j, blockpos.getY() + j, blockpos.getZ() + j);
   }

   /** @deprecated */
   @Deprecated
   public static Stream<BlockPos> squareOutSouthEast(BlockPos blockpos) {
      return Stream.of(blockpos, blockpos.south(), blockpos.east(), blockpos.south().east());
   }

   public static Iterable<BlockPos> randomBetweenClosed(RandomSource randomsource, int i, int j, int k, int l, int i1, int j1, int k1) {
      int l1 = i1 - j + 1;
      int i2 = j1 - k + 1;
      int j2 = k1 - l + 1;
      return () -> new AbstractIterator<BlockPos>() {
            final BlockPos.MutableBlockPos nextPos = new BlockPos.MutableBlockPos();
            int counter = i;

            protected BlockPos computeNext() {
               if (this.counter <= 0) {
                  return this.endOfData();
               } else {
                  BlockPos blockpos = this.nextPos.set(j + randomsource.nextInt(l1), k + randomsource.nextInt(i2), l + randomsource.nextInt(j2));
                  --this.counter;
                  return blockpos;
               }
            }
         };
   }

   public static Iterable<BlockPos> withinManhattan(BlockPos blockpos, int i, int j, int k) {
      int l = i + j + k;
      int i1 = blockpos.getX();
      int j1 = blockpos.getY();
      int k1 = blockpos.getZ();
      return () -> new AbstractIterator<BlockPos>() {
            private final BlockPos.MutableBlockPos cursor = new BlockPos.MutableBlockPos();
            private int currentDepth;
            private int maxX;
            private int maxY;
            private int x;
            private int y;
            private boolean zMirror;

            protected BlockPos computeNext() {
               if (this.zMirror) {
                  this.zMirror = false;
                  this.cursor.setZ(k1 - (this.cursor.getZ() - k1));
                  return this.cursor;
               } else {
                  BlockPos blockpos;
                  for(blockpos = null; blockpos == null; ++this.y) {
                     if (this.y > this.maxY) {
                        ++this.x;
                        if (this.x > this.maxX) {
                           ++this.currentDepth;
                           if (this.currentDepth > l) {
                              return this.endOfData();
                           }

                           this.maxX = Math.min(i, this.currentDepth);
                           this.x = -this.maxX;
                        }

                        this.maxY = Math.min(j, this.currentDepth - Math.abs(this.x));
                        this.y = -this.maxY;
                     }

                     int i = this.x;
                     int j = this.y;
                     int k = this.currentDepth - Math.abs(i) - Math.abs(j);
                     if (k <= k) {
                        this.zMirror = k != 0;
                        blockpos = this.cursor.set(i1 + i, j1 + j, k1 + k);
                     }
                  }

                  return blockpos;
               }
            }
         };
   }

   public static Optional<BlockPos> findClosestMatch(BlockPos blockpos, int i, int j, Predicate<BlockPos> predicate) {
      for(BlockPos blockpos1 : withinManhattan(blockpos, i, j, i)) {
         if (predicate.test(blockpos1)) {
            return Optional.of(blockpos1);
         }
      }

      return Optional.empty();
   }

   public static Stream<BlockPos> withinManhattanStream(BlockPos blockpos, int i, int j, int k) {
      return StreamSupport.stream(withinManhattan(blockpos, i, j, k).spliterator(), false);
   }

   public static Iterable<BlockPos> betweenClosed(BlockPos blockpos, BlockPos blockpos1) {
      return betweenClosed(Math.min(blockpos.getX(), blockpos1.getX()), Math.min(blockpos.getY(), blockpos1.getY()), Math.min(blockpos.getZ(), blockpos1.getZ()), Math.max(blockpos.getX(), blockpos1.getX()), Math.max(blockpos.getY(), blockpos1.getY()), Math.max(blockpos.getZ(), blockpos1.getZ()));
   }

   public static Stream<BlockPos> betweenClosedStream(BlockPos blockpos, BlockPos blockpos1) {
      return StreamSupport.stream(betweenClosed(blockpos, blockpos1).spliterator(), false);
   }

   public static Stream<BlockPos> betweenClosedStream(BoundingBox boundingbox) {
      return betweenClosedStream(Math.min(boundingbox.minX(), boundingbox.maxX()), Math.min(boundingbox.minY(), boundingbox.maxY()), Math.min(boundingbox.minZ(), boundingbox.maxZ()), Math.max(boundingbox.minX(), boundingbox.maxX()), Math.max(boundingbox.minY(), boundingbox.maxY()), Math.max(boundingbox.minZ(), boundingbox.maxZ()));
   }

   public static Stream<BlockPos> betweenClosedStream(AABB aabb) {
      return betweenClosedStream(Mth.floor(aabb.minX), Mth.floor(aabb.minY), Mth.floor(aabb.minZ), Mth.floor(aabb.maxX), Mth.floor(aabb.maxY), Mth.floor(aabb.maxZ));
   }

   public static Stream<BlockPos> betweenClosedStream(int i, int j, int k, int l, int i1, int j1) {
      return StreamSupport.stream(betweenClosed(i, j, k, l, i1, j1).spliterator(), false);
   }

   public static Iterable<BlockPos> betweenClosed(int i, int j, int k, int l, int i1, int j1) {
      int k1 = l - i + 1;
      int l1 = i1 - j + 1;
      int i2 = j1 - k + 1;
      int j2 = k1 * l1 * i2;
      return () -> new AbstractIterator<BlockPos>() {
            private final BlockPos.MutableBlockPos cursor = new BlockPos.MutableBlockPos();
            private int index;

            protected BlockPos computeNext() {
               if (this.index == j2) {
                  return this.endOfData();
               } else {
                  int i = this.index % k1;
                  int j = this.index / k1;
                  int k = j % l1;
                  int l = j / l1;
                  ++this.index;
                  return this.cursor.set(i + i, j + k, k + l);
               }
            }
         };
   }

   public static Iterable<BlockPos.MutableBlockPos> spiralAround(BlockPos blockpos, int i, Direction direction, Direction direction1) {
      Validate.validState(direction.getAxis() != direction1.getAxis(), "The two directions cannot be on the same axis");
      return () -> new AbstractIterator<BlockPos.MutableBlockPos>() {
            private final Direction[] directions = new Direction[]{direction, direction1, direction.getOpposite(), direction1.getOpposite()};
            private final BlockPos.MutableBlockPos cursor = blockpos.mutable().move(direction1);
            private final int legs = 4 * i;
            private int leg = -1;
            private int legSize;
            private int legIndex;
            private int lastX = this.cursor.getX();
            private int lastY = this.cursor.getY();
            private int lastZ = this.cursor.getZ();

            protected BlockPos.MutableBlockPos computeNext() {
               this.cursor.set(this.lastX, this.lastY, this.lastZ).move(this.directions[(this.leg + 4) % 4]);
               this.lastX = this.cursor.getX();
               this.lastY = this.cursor.getY();
               this.lastZ = this.cursor.getZ();
               if (this.legIndex >= this.legSize) {
                  if (this.leg >= this.legs) {
                     return this.endOfData();
                  }

                  ++this.leg;
                  this.legIndex = 0;
                  this.legSize = this.leg / 2 + 1;
               }

               ++this.legIndex;
               return this.cursor;
            }
         };
   }

   public static int breadthFirstTraversal(BlockPos blockpos, int i, int j, BiConsumer<BlockPos, Consumer<BlockPos>> biconsumer, Predicate<BlockPos> predicate) {
      Queue<Pair<BlockPos, Integer>> queue = new ArrayDeque<>();
      LongSet longset = new LongOpenHashSet();
      queue.add(Pair.of(blockpos, 0));
      int k = 0;

      while(!queue.isEmpty()) {
         Pair<BlockPos, Integer> pair = queue.poll();
         BlockPos blockpos1 = pair.getLeft();
         int l = pair.getRight();
         long i1 = blockpos1.asLong();
         if (longset.add(i1) && predicate.test(blockpos1)) {
            ++k;
            if (k >= j) {
               return k;
            }

            if (l < i) {
               biconsumer.accept(blockpos1, (blockpos2) -> queue.add(Pair.of(blockpos2, l + 1)));
            }
         }
      }

      return k;
   }

   public static class MutableBlockPos extends BlockPos {
      public MutableBlockPos() {
         this(0, 0, 0);
      }

      public MutableBlockPos(int i, int j, int k) {
         super(i, j, k);
      }

      public MutableBlockPos(double d0, double d1, double d2) {
         this(Mth.floor(d0), Mth.floor(d1), Mth.floor(d2));
      }

      public BlockPos offset(int i, int j, int k) {
         return super.offset(i, j, k).immutable();
      }

      public BlockPos multiply(int i) {
         return super.multiply(i).immutable();
      }

      public BlockPos relative(Direction direction, int i) {
         return super.relative(direction, i).immutable();
      }

      public BlockPos relative(Direction.Axis direction_axis, int i) {
         return super.relative(direction_axis, i).immutable();
      }

      public BlockPos rotate(Rotation rotation) {
         return super.rotate(rotation).immutable();
      }

      public BlockPos.MutableBlockPos set(int i, int j, int k) {
         this.setX(i);
         this.setY(j);
         this.setZ(k);
         return this;
      }

      public BlockPos.MutableBlockPos set(double d0, double d1, double d2) {
         return this.set(Mth.floor(d0), Mth.floor(d1), Mth.floor(d2));
      }

      public BlockPos.MutableBlockPos set(Vec3i vec3i) {
         return this.set(vec3i.getX(), vec3i.getY(), vec3i.getZ());
      }

      public BlockPos.MutableBlockPos set(long i) {
         return this.set(getX(i), getY(i), getZ(i));
      }

      public BlockPos.MutableBlockPos set(AxisCycle axiscycle, int i, int j, int k) {
         return this.set(axiscycle.cycle(i, j, k, Direction.Axis.X), axiscycle.cycle(i, j, k, Direction.Axis.Y), axiscycle.cycle(i, j, k, Direction.Axis.Z));
      }

      public BlockPos.MutableBlockPos setWithOffset(Vec3i vec3i, Direction direction) {
         return this.set(vec3i.getX() + direction.getStepX(), vec3i.getY() + direction.getStepY(), vec3i.getZ() + direction.getStepZ());
      }

      public BlockPos.MutableBlockPos setWithOffset(Vec3i vec3i, int i, int j, int k) {
         return this.set(vec3i.getX() + i, vec3i.getY() + j, vec3i.getZ() + k);
      }

      public BlockPos.MutableBlockPos setWithOffset(Vec3i vec3i, Vec3i vec3i1) {
         return this.set(vec3i.getX() + vec3i1.getX(), vec3i.getY() + vec3i1.getY(), vec3i.getZ() + vec3i1.getZ());
      }

      public BlockPos.MutableBlockPos move(Direction direction) {
         return this.move(direction, 1);
      }

      public BlockPos.MutableBlockPos move(Direction direction, int i) {
         return this.set(this.getX() + direction.getStepX() * i, this.getY() + direction.getStepY() * i, this.getZ() + direction.getStepZ() * i);
      }

      public BlockPos.MutableBlockPos move(int i, int j, int k) {
         return this.set(this.getX() + i, this.getY() + j, this.getZ() + k);
      }

      public BlockPos.MutableBlockPos move(Vec3i vec3i) {
         return this.set(this.getX() + vec3i.getX(), this.getY() + vec3i.getY(), this.getZ() + vec3i.getZ());
      }

      public BlockPos.MutableBlockPos clamp(Direction.Axis direction_axis, int i, int j) {
         switch (direction_axis) {
            case X:
               return this.set(Mth.clamp(this.getX(), i, j), this.getY(), this.getZ());
            case Y:
               return this.set(this.getX(), Mth.clamp(this.getY(), i, j), this.getZ());
            case Z:
               return this.set(this.getX(), this.getY(), Mth.clamp(this.getZ(), i, j));
            default:
               throw new IllegalStateException("Unable to clamp axis " + direction_axis);
         }
      }

      public BlockPos.MutableBlockPos setX(int i) {
         super.setX(i);
         return this;
      }

      public BlockPos.MutableBlockPos setY(int i) {
         super.setY(i);
         return this;
      }

      public BlockPos.MutableBlockPos setZ(int i) {
         super.setZ(i);
         return this;
      }

      public BlockPos immutable() {
         return new BlockPos(this);
      }
   }
}
