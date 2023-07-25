package net.minecraft.world.level;

import com.google.common.collect.Lists;
import java.util.List;
import net.minecraft.core.BlockPos;

public class PotentialCalculator {
   private final List<PotentialCalculator.PointCharge> charges = Lists.newArrayList();

   public void addCharge(BlockPos blockpos, double d0) {
      if (d0 != 0.0D) {
         this.charges.add(new PotentialCalculator.PointCharge(blockpos, d0));
      }

   }

   public double getPotentialEnergyChange(BlockPos blockpos, double d0) {
      if (d0 == 0.0D) {
         return 0.0D;
      } else {
         double d1 = 0.0D;

         for(PotentialCalculator.PointCharge potentialcalculator_pointcharge : this.charges) {
            d1 += potentialcalculator_pointcharge.getPotentialChange(blockpos);
         }

         return d1 * d0;
      }
   }

   static class PointCharge {
      private final BlockPos pos;
      private final double charge;

      public PointCharge(BlockPos blockpos, double d0) {
         this.pos = blockpos;
         this.charge = d0;
      }

      public double getPotentialChange(BlockPos blockpos) {
         double d0 = this.pos.distSqr(blockpos);
         return d0 == 0.0D ? Double.POSITIVE_INFINITY : this.charge / Math.sqrt(d0);
      }
   }
}
