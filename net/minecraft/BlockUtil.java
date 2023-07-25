package net.minecraft;

import com.google.common.annotations.VisibleForTesting;
import com.mojang.datafixers.util.Pair;
import it.unimi.dsi.fastutil.ints.IntArrayList;
import it.unimi.dsi.fastutil.ints.IntStack;
import java.util.Optional;
import java.util.function.Predicate;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;

public class BlockUtil {
   public static BlockUtil.FoundRectangle getLargestRectangleAround(BlockPos blockpos, Direction.Axis direction_axis, int i, Direction.Axis direction_axis1, int j, Predicate<BlockPos> predicate) {
      BlockPos.MutableBlockPos blockpos_mutableblockpos = blockpos.mutable();
      Direction direction = Direction.get(Direction.AxisDirection.NEGATIVE, direction_axis);
      Direction direction1 = direction.getOpposite();
      Direction direction2 = Direction.get(Direction.AxisDirection.NEGATIVE, direction_axis1);
      Direction direction3 = direction2.getOpposite();
      int k = getLimit(predicate, blockpos_mutableblockpos.set(blockpos), direction, i);
      int l = getLimit(predicate, blockpos_mutableblockpos.set(blockpos), direction1, i);
      int i1 = k;
      BlockUtil.IntBounds[] ablockutil_intbounds = new BlockUtil.IntBounds[k + 1 + l];
      ablockutil_intbounds[k] = new BlockUtil.IntBounds(getLimit(predicate, blockpos_mutableblockpos.set(blockpos), direction2, j), getLimit(predicate, blockpos_mutableblockpos.set(blockpos), direction3, j));
      int j1 = ablockutil_intbounds[k].min;

      for(int k1 = 1; k1 <= k; ++k1) {
         BlockUtil.IntBounds blockutil_intbounds = ablockutil_intbounds[i1 - (k1 - 1)];
         ablockutil_intbounds[i1 - k1] = new BlockUtil.IntBounds(getLimit(predicate, blockpos_mutableblockpos.set(blockpos).move(direction, k1), direction2, blockutil_intbounds.min), getLimit(predicate, blockpos_mutableblockpos.set(blockpos).move(direction, k1), direction3, blockutil_intbounds.max));
      }

      for(int l1 = 1; l1 <= l; ++l1) {
         BlockUtil.IntBounds blockutil_intbounds1 = ablockutil_intbounds[i1 + l1 - 1];
         ablockutil_intbounds[i1 + l1] = new BlockUtil.IntBounds(getLimit(predicate, blockpos_mutableblockpos.set(blockpos).move(direction1, l1), direction2, blockutil_intbounds1.min), getLimit(predicate, blockpos_mutableblockpos.set(blockpos).move(direction1, l1), direction3, blockutil_intbounds1.max));
      }

      int i2 = 0;
      int j2 = 0;
      int k2 = 0;
      int l2 = 0;
      int[] aint = new int[ablockutil_intbounds.length];

      for(int i3 = j1; i3 >= 0; --i3) {
         for(int j3 = 0; j3 < ablockutil_intbounds.length; ++j3) {
            BlockUtil.IntBounds blockutil_intbounds2 = ablockutil_intbounds[j3];
            int k3 = j1 - blockutil_intbounds2.min;
            int l3 = j1 + blockutil_intbounds2.max;
            aint[j3] = i3 >= k3 && i3 <= l3 ? l3 + 1 - i3 : 0;
         }

         Pair<BlockUtil.IntBounds, Integer> pair = getMaxRectangleLocation(aint);
         BlockUtil.IntBounds blockutil_intbounds3 = pair.getFirst();
         int i4 = 1 + blockutil_intbounds3.max - blockutil_intbounds3.min;
         int j4 = pair.getSecond();
         if (i4 * j4 > k2 * l2) {
            i2 = blockutil_intbounds3.min;
            j2 = i3;
            k2 = i4;
            l2 = j4;
         }
      }

      return new BlockUtil.FoundRectangle(blockpos.relative(direction_axis, i2 - i1).relative(direction_axis1, j2 - j1), k2, l2);
   }

   private static int getLimit(Predicate<BlockPos> predicate, BlockPos.MutableBlockPos blockpos_mutableblockpos, Direction direction, int i) {
      int j;
      for(j = 0; j < i && predicate.test(blockpos_mutableblockpos.move(direction)); ++j) {
      }

      return j;
   }

   @VisibleForTesting
   static Pair<BlockUtil.IntBounds, Integer> getMaxRectangleLocation(int[] aint) {
      int i = 0;
      int j = 0;
      int k = 0;
      IntStack intstack = new IntArrayList();
      intstack.push(0);

      for(int l = 1; l <= aint.length; ++l) {
         int i1 = l == aint.length ? 0 : aint[l];

         while(!intstack.isEmpty()) {
            int j1 = aint[intstack.topInt()];
            if (i1 >= j1) {
               intstack.push(l);
               break;
            }

            intstack.popInt();
            int k1 = intstack.isEmpty() ? 0 : intstack.topInt() + 1;
            if (j1 * (l - k1) > k * (j - i)) {
               j = l;
               i = k1;
               k = j1;
            }
         }

         if (intstack.isEmpty()) {
            intstack.push(l);
         }
      }

      return new Pair<>(new BlockUtil.IntBounds(i, j - 1), k);
   }

   public static Optional<BlockPos> getTopConnectedBlock(BlockGetter blockgetter, BlockPos blockpos, Block block, Direction direction, Block block1) {
      BlockPos.MutableBlockPos blockpos_mutableblockpos = blockpos.mutable();

      BlockState blockstate;
      do {
         blockpos_mutableblockpos.move(direction);
         blockstate = blockgetter.getBlockState(blockpos_mutableblockpos);
      } while(blockstate.is(block));

      return blockstate.is(block1) ? Optional.of(blockpos_mutableblockpos) : Optional.empty();
   }

   public static class FoundRectangle {
      public final BlockPos minCorner;
      public final int axis1Size;
      public final int axis2Size;

      public FoundRectangle(BlockPos blockpos, int i, int j) {
         this.minCorner = blockpos;
         this.axis1Size = i;
         this.axis2Size = j;
      }
   }

   public static class IntBounds {
      public final int min;
      public final int max;

      public IntBounds(int i, int j) {
         this.min = i;
         this.max = j;
      }

      public String toString() {
         return "IntBounds{min=" + this.min + ", max=" + this.max + "}";
      }
   }
}
