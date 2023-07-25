package net.minecraft.world.level.redstone;

import com.mojang.logging.LogUtils;
import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.List;
import javax.annotation.Nullable;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import org.slf4j.Logger;

public class CollectingNeighborUpdater implements NeighborUpdater {
   private static final Logger LOGGER = LogUtils.getLogger();
   private final Level level;
   private final int maxChainedNeighborUpdates;
   private final ArrayDeque<CollectingNeighborUpdater.NeighborUpdates> stack = new ArrayDeque<>();
   private final List<CollectingNeighborUpdater.NeighborUpdates> addedThisLayer = new ArrayList<>();
   private int count = 0;

   public CollectingNeighborUpdater(Level level, int i) {
      this.level = level;
      this.maxChainedNeighborUpdates = i;
   }

   public void shapeUpdate(Direction direction, BlockState blockstate, BlockPos blockpos, BlockPos blockpos1, int i, int j) {
      this.addAndRun(blockpos, new CollectingNeighborUpdater.ShapeUpdate(direction, blockstate, blockpos.immutable(), blockpos1.immutable(), i, j));
   }

   public void neighborChanged(BlockPos blockpos, Block block, BlockPos blockpos1) {
      this.addAndRun(blockpos, new CollectingNeighborUpdater.SimpleNeighborUpdate(blockpos, block, blockpos1.immutable()));
   }

   public void neighborChanged(BlockState blockstate, BlockPos blockpos, Block block, BlockPos blockpos1, boolean flag) {
      this.addAndRun(blockpos, new CollectingNeighborUpdater.FullNeighborUpdate(blockstate, blockpos.immutable(), block, blockpos1.immutable(), flag));
   }

   public void updateNeighborsAtExceptFromFacing(BlockPos blockpos, Block block, @Nullable Direction direction) {
      this.addAndRun(blockpos, new CollectingNeighborUpdater.MultiNeighborUpdate(blockpos.immutable(), block, direction));
   }

   private void addAndRun(BlockPos blockpos, CollectingNeighborUpdater.NeighborUpdates collectingneighborupdater_neighborupdates) {
      boolean flag = this.count > 0;
      boolean flag1 = this.maxChainedNeighborUpdates >= 0 && this.count >= this.maxChainedNeighborUpdates;
      ++this.count;
      if (!flag1) {
         if (flag) {
            this.addedThisLayer.add(collectingneighborupdater_neighborupdates);
         } else {
            this.stack.push(collectingneighborupdater_neighborupdates);
         }
      } else if (this.count - 1 == this.maxChainedNeighborUpdates) {
         LOGGER.error("Too many chained neighbor updates. Skipping the rest. First skipped position: " + blockpos.toShortString());
      }

      if (!flag) {
         this.runUpdates();
      }

   }

   private void runUpdates() {
      try {
         while(!this.stack.isEmpty() || !this.addedThisLayer.isEmpty()) {
            for(int i = this.addedThisLayer.size() - 1; i >= 0; --i) {
               this.stack.push(this.addedThisLayer.get(i));
            }

            this.addedThisLayer.clear();
            CollectingNeighborUpdater.NeighborUpdates collectingneighborupdater_neighborupdates = this.stack.peek();

            while(this.addedThisLayer.isEmpty()) {
               if (!collectingneighborupdater_neighborupdates.runNext(this.level)) {
                  this.stack.pop();
                  break;
               }
            }
         }
      } finally {
         this.stack.clear();
         this.addedThisLayer.clear();
         this.count = 0;
      }

   }

   static record FullNeighborUpdate(BlockState state, BlockPos pos, Block block, BlockPos neighborPos, boolean movedByPiston) implements CollectingNeighborUpdater.NeighborUpdates {
      public boolean runNext(Level level) {
         NeighborUpdater.executeUpdate(level, this.state, this.pos, this.block, this.neighborPos, this.movedByPiston);
         return false;
      }
   }

   static final class MultiNeighborUpdate implements CollectingNeighborUpdater.NeighborUpdates {
      private final BlockPos sourcePos;
      private final Block sourceBlock;
      @Nullable
      private final Direction skipDirection;
      private int idx = 0;

      MultiNeighborUpdate(BlockPos blockpos, Block block, @Nullable Direction direction) {
         this.sourcePos = blockpos;
         this.sourceBlock = block;
         this.skipDirection = direction;
         if (NeighborUpdater.UPDATE_ORDER[this.idx] == direction) {
            ++this.idx;
         }

      }

      public boolean runNext(Level level) {
         BlockPos blockpos = this.sourcePos.relative(NeighborUpdater.UPDATE_ORDER[this.idx++]);
         BlockState blockstate = level.getBlockState(blockpos);
         blockstate.neighborChanged(level, blockpos, this.sourceBlock, this.sourcePos, false);
         if (this.idx < NeighborUpdater.UPDATE_ORDER.length && NeighborUpdater.UPDATE_ORDER[this.idx] == this.skipDirection) {
            ++this.idx;
         }

         return this.idx < NeighborUpdater.UPDATE_ORDER.length;
      }
   }

   interface NeighborUpdates {
      boolean runNext(Level level);
   }

   static record ShapeUpdate(Direction direction, BlockState state, BlockPos pos, BlockPos neighborPos, int updateFlags, int updateLimit) implements CollectingNeighborUpdater.NeighborUpdates {
      public boolean runNext(Level level) {
         NeighborUpdater.executeShapeUpdate(level, this.direction, this.state, this.pos, this.neighborPos, this.updateFlags, this.updateLimit);
         return false;
      }
   }

   static record SimpleNeighborUpdate(BlockPos pos, Block block, BlockPos neighborPos) implements CollectingNeighborUpdater.NeighborUpdates {
      public boolean runNext(Level level) {
         BlockState blockstate = level.getBlockState(this.pos);
         NeighborUpdater.executeUpdate(level, blockstate, this.pos, this.block, this.neighborPos, false);
         return false;
      }
   }
}
