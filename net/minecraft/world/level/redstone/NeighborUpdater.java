package net.minecraft.world.level.redstone;

import java.util.Locale;
import javax.annotation.Nullable;
import net.minecraft.CrashReport;
import net.minecraft.CrashReportCategory;
import net.minecraft.ReportedException;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;

public interface NeighborUpdater {
   Direction[] UPDATE_ORDER = new Direction[]{Direction.WEST, Direction.EAST, Direction.DOWN, Direction.UP, Direction.NORTH, Direction.SOUTH};

   void shapeUpdate(Direction direction, BlockState blockstate, BlockPos blockpos, BlockPos blockpos1, int i, int j);

   void neighborChanged(BlockPos blockpos, Block block, BlockPos blockpos1);

   void neighborChanged(BlockState blockstate, BlockPos blockpos, Block block, BlockPos blockpos1, boolean flag);

   default void updateNeighborsAtExceptFromFacing(BlockPos blockpos, Block block, @Nullable Direction direction) {
      for(Direction direction1 : UPDATE_ORDER) {
         if (direction1 != direction) {
            this.neighborChanged(blockpos.relative(direction1), block, blockpos);
         }
      }

   }

   static void executeShapeUpdate(LevelAccessor levelaccessor, Direction direction, BlockState blockstate, BlockPos blockpos, BlockPos blockpos1, int i, int j) {
      BlockState blockstate1 = levelaccessor.getBlockState(blockpos);
      BlockState blockstate2 = blockstate1.updateShape(direction, blockstate, levelaccessor, blockpos, blockpos1);
      Block.updateOrDestroy(blockstate1, blockstate2, levelaccessor, blockpos, i, j);
   }

   static void executeUpdate(Level level, BlockState blockstate, BlockPos blockpos, Block block, BlockPos blockpos1, boolean flag) {
      try {
         blockstate.neighborChanged(level, blockpos, block, blockpos1, flag);
      } catch (Throwable var9) {
         CrashReport crashreport = CrashReport.forThrowable(var9, "Exception while updating neighbours");
         CrashReportCategory crashreportcategory = crashreport.addCategory("Block being updated");
         crashreportcategory.setDetail("Source block type", () -> {
            try {
               return String.format(Locale.ROOT, "ID #%s (%s // %s)", BuiltInRegistries.BLOCK.getKey(block), block.getDescriptionId(), block.getClass().getCanonicalName());
            } catch (Throwable var2) {
               return "ID #" + BuiltInRegistries.BLOCK.getKey(block);
            }
         });
         CrashReportCategory.populateBlockDetails(crashreportcategory, level, blockpos, blockstate);
         throw new ReportedException(crashreport);
      }
   }
}
