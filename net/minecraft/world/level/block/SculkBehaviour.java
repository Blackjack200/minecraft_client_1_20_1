package net.minecraft.world.level.block;

import java.util.Collection;
import javax.annotation.Nullable;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.material.Fluids;

public interface SculkBehaviour {
   SculkBehaviour DEFAULT = new SculkBehaviour() {
      public boolean attemptSpreadVein(LevelAccessor levelaccessor, BlockPos blockpos, BlockState blockstate, @Nullable Collection<Direction> collection, boolean flag) {
         if (collection == null) {
            return ((SculkVeinBlock)Blocks.SCULK_VEIN).getSameSpaceSpreader().spreadAll(levelaccessor.getBlockState(blockpos), levelaccessor, blockpos, flag) > 0L;
         } else if (!collection.isEmpty()) {
            return !blockstate.isAir() && !blockstate.getFluidState().is(Fluids.WATER) ? false : SculkVeinBlock.regrow(levelaccessor, blockpos, blockstate, collection);
         } else {
            return SculkBehaviour.super.attemptSpreadVein(levelaccessor, blockpos, blockstate, collection, flag);
         }
      }

      public int attemptUseCharge(SculkSpreader.ChargeCursor sculkspreader_chargecursor, LevelAccessor levelaccessor, BlockPos blockpos, RandomSource randomsource, SculkSpreader sculkspreader, boolean flag) {
         return sculkspreader_chargecursor.getDecayDelay() > 0 ? sculkspreader_chargecursor.getCharge() : 0;
      }

      public int updateDecayDelay(int i) {
         return Math.max(i - 1, 0);
      }
   };

   default byte getSculkSpreadDelay() {
      return 1;
   }

   default void onDischarged(LevelAccessor levelaccessor, BlockState blockstate, BlockPos blockpos, RandomSource randomsource) {
   }

   default boolean depositCharge(LevelAccessor levelaccessor, BlockPos blockpos, RandomSource randomsource) {
      return false;
   }

   default boolean attemptSpreadVein(LevelAccessor levelaccessor, BlockPos blockpos, BlockState blockstate, @Nullable Collection<Direction> collection, boolean flag) {
      return ((MultifaceBlock)Blocks.SCULK_VEIN).getSpreader().spreadAll(blockstate, levelaccessor, blockpos, flag) > 0L;
   }

   default boolean canChangeBlockStateOnSpread() {
      return true;
   }

   default int updateDecayDelay(int i) {
      return 1;
   }

   int attemptUseCharge(SculkSpreader.ChargeCursor sculkspreader_chargecursor, LevelAccessor levelaccessor, BlockPos blockpos, RandomSource randomsource, SculkSpreader sculkspreader, boolean flag);
}
