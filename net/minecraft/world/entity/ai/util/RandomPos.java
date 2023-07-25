package net.minecraft.world.entity.ai.util;

import com.google.common.annotations.VisibleForTesting;
import java.util.function.Predicate;
import java.util.function.Supplier;
import java.util.function.ToDoubleFunction;
import javax.annotation.Nullable;
import net.minecraft.core.BlockPos;
import net.minecraft.util.Mth;
import net.minecraft.util.RandomSource;
import net.minecraft.world.entity.PathfinderMob;
import net.minecraft.world.phys.Vec3;

public class RandomPos {
   private static final int RANDOM_POS_ATTEMPTS = 10;

   public static BlockPos generateRandomDirection(RandomSource randomsource, int i, int j) {
      int k = randomsource.nextInt(2 * i + 1) - i;
      int l = randomsource.nextInt(2 * j + 1) - j;
      int i1 = randomsource.nextInt(2 * i + 1) - i;
      return new BlockPos(k, l, i1);
   }

   @Nullable
   public static BlockPos generateRandomDirectionWithinRadians(RandomSource randomsource, int i, int j, int k, double d0, double d1, double d2) {
      double d3 = Mth.atan2(d1, d0) - (double)((float)Math.PI / 2F);
      double d4 = d3 + (double)(2.0F * randomsource.nextFloat() - 1.0F) * d2;
      double d5 = Math.sqrt(randomsource.nextDouble()) * (double)Mth.SQRT_OF_TWO * (double)i;
      double d6 = -d5 * Math.sin(d4);
      double d7 = d5 * Math.cos(d4);
      if (!(Math.abs(d6) > (double)i) && !(Math.abs(d7) > (double)i)) {
         int l = randomsource.nextInt(2 * j + 1) - j + k;
         return BlockPos.containing(d6, (double)l, d7);
      } else {
         return null;
      }
   }

   @VisibleForTesting
   public static BlockPos moveUpOutOfSolid(BlockPos blockpos, int i, Predicate<BlockPos> predicate) {
      if (!predicate.test(blockpos)) {
         return blockpos;
      } else {
         BlockPos blockpos1;
         for(blockpos1 = blockpos.above(); blockpos1.getY() < i && predicate.test(blockpos1); blockpos1 = blockpos1.above()) {
         }

         return blockpos1;
      }
   }

   @VisibleForTesting
   public static BlockPos moveUpToAboveSolid(BlockPos blockpos, int i, int j, Predicate<BlockPos> predicate) {
      if (i < 0) {
         throw new IllegalArgumentException("aboveSolidAmount was " + i + ", expected >= 0");
      } else if (!predicate.test(blockpos)) {
         return blockpos;
      } else {
         BlockPos blockpos1;
         for(blockpos1 = blockpos.above(); blockpos1.getY() < j && predicate.test(blockpos1); blockpos1 = blockpos1.above()) {
         }

         BlockPos blockpos2;
         BlockPos blockpos3;
         for(blockpos2 = blockpos1; blockpos2.getY() < j && blockpos2.getY() - blockpos1.getY() < i; blockpos2 = blockpos3) {
            blockpos3 = blockpos2.above();
            if (predicate.test(blockpos3)) {
               break;
            }
         }

         return blockpos2;
      }
   }

   @Nullable
   public static Vec3 generateRandomPos(PathfinderMob pathfindermob, Supplier<BlockPos> supplier) {
      return generateRandomPos(supplier, pathfindermob::getWalkTargetValue);
   }

   @Nullable
   public static Vec3 generateRandomPos(Supplier<BlockPos> supplier, ToDoubleFunction<BlockPos> todoublefunction) {
      double d0 = Double.NEGATIVE_INFINITY;
      BlockPos blockpos = null;

      for(int i = 0; i < 10; ++i) {
         BlockPos blockpos1 = supplier.get();
         if (blockpos1 != null) {
            double d1 = todoublefunction.applyAsDouble(blockpos1);
            if (d1 > d0) {
               d0 = d1;
               blockpos = blockpos1;
            }
         }
      }

      return blockpos != null ? Vec3.atBottomCenterOf(blockpos) : null;
   }

   public static BlockPos generateRandomPosTowardDirection(PathfinderMob pathfindermob, int i, RandomSource randomsource, BlockPos blockpos) {
      int j = blockpos.getX();
      int k = blockpos.getZ();
      if (pathfindermob.hasRestriction() && i > 1) {
         BlockPos blockpos1 = pathfindermob.getRestrictCenter();
         if (pathfindermob.getX() > (double)blockpos1.getX()) {
            j -= randomsource.nextInt(i / 2);
         } else {
            j += randomsource.nextInt(i / 2);
         }

         if (pathfindermob.getZ() > (double)blockpos1.getZ()) {
            k -= randomsource.nextInt(i / 2);
         } else {
            k += randomsource.nextInt(i / 2);
         }
      }

      return BlockPos.containing((double)j + pathfindermob.getX(), (double)blockpos.getY() + pathfindermob.getY(), (double)k + pathfindermob.getZ());
   }
}
