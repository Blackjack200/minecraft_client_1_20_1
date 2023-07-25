package net.minecraft.world.level.pathfinder;

import com.google.common.collect.Maps;
import it.unimi.dsi.fastutil.longs.Long2ObjectMap;
import it.unimi.dsi.fastutil.longs.Long2ObjectOpenHashMap;
import java.util.Map;
import javax.annotation.Nullable;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.tags.FluidTags;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.PathNavigationRegion;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.material.FluidState;

public class SwimNodeEvaluator extends NodeEvaluator {
   private final boolean allowBreaching;
   private final Long2ObjectMap<BlockPathTypes> pathTypesByPosCache = new Long2ObjectOpenHashMap<>();

   public SwimNodeEvaluator(boolean flag) {
      this.allowBreaching = flag;
   }

   public void prepare(PathNavigationRegion pathnavigationregion, Mob mob) {
      super.prepare(pathnavigationregion, mob);
      this.pathTypesByPosCache.clear();
   }

   public void done() {
      super.done();
      this.pathTypesByPosCache.clear();
   }

   public Node getStart() {
      return this.getNode(Mth.floor(this.mob.getBoundingBox().minX), Mth.floor(this.mob.getBoundingBox().minY + 0.5D), Mth.floor(this.mob.getBoundingBox().minZ));
   }

   public Target getGoal(double d0, double d1, double d2) {
      return this.getTargetFromNode(this.getNode(Mth.floor(d0), Mth.floor(d1), Mth.floor(d2)));
   }

   public int getNeighbors(Node[] anode, Node node) {
      int i = 0;
      Map<Direction, Node> map = Maps.newEnumMap(Direction.class);

      for(Direction direction : Direction.values()) {
         Node node1 = this.findAcceptedNode(node.x + direction.getStepX(), node.y + direction.getStepY(), node.z + direction.getStepZ());
         map.put(direction, node1);
         if (this.isNodeValid(node1)) {
            anode[i++] = node1;
         }
      }

      for(Direction direction1 : Direction.Plane.HORIZONTAL) {
         Direction direction2 = direction1.getClockWise();
         Node node2 = this.findAcceptedNode(node.x + direction1.getStepX() + direction2.getStepX(), node.y, node.z + direction1.getStepZ() + direction2.getStepZ());
         if (this.isDiagonalNodeValid(node2, map.get(direction1), map.get(direction2))) {
            anode[i++] = node2;
         }
      }

      return i;
   }

   protected boolean isNodeValid(@Nullable Node node) {
      return node != null && !node.closed;
   }

   protected boolean isDiagonalNodeValid(@Nullable Node node, @Nullable Node node1, @Nullable Node node2) {
      return this.isNodeValid(node) && node1 != null && node1.costMalus >= 0.0F && node2 != null && node2.costMalus >= 0.0F;
   }

   @Nullable
   protected Node findAcceptedNode(int i, int j, int k) {
      Node node = null;
      BlockPathTypes blockpathtypes = this.getCachedBlockType(i, j, k);
      if (this.allowBreaching && blockpathtypes == BlockPathTypes.BREACH || blockpathtypes == BlockPathTypes.WATER) {
         float f = this.mob.getPathfindingMalus(blockpathtypes);
         if (f >= 0.0F) {
            node = this.getNode(i, j, k);
            node.type = blockpathtypes;
            node.costMalus = Math.max(node.costMalus, f);
            if (this.level.getFluidState(new BlockPos(i, j, k)).isEmpty()) {
               node.costMalus += 8.0F;
            }
         }
      }

      return node;
   }

   protected BlockPathTypes getCachedBlockType(int i, int j, int k) {
      return this.pathTypesByPosCache.computeIfAbsent(BlockPos.asLong(i, j, k), (k1) -> this.getBlockPathType(this.level, i, j, k));
   }

   public BlockPathTypes getBlockPathType(BlockGetter blockgetter, int i, int j, int k) {
      return this.getBlockPathType(blockgetter, i, j, k, this.mob);
   }

   public BlockPathTypes getBlockPathType(BlockGetter blockgetter, int i, int j, int k, Mob mob) {
      BlockPos.MutableBlockPos blockpos_mutableblockpos = new BlockPos.MutableBlockPos();

      for(int l = i; l < i + this.entityWidth; ++l) {
         for(int i1 = j; i1 < j + this.entityHeight; ++i1) {
            for(int j1 = k; j1 < k + this.entityDepth; ++j1) {
               FluidState fluidstate = blockgetter.getFluidState(blockpos_mutableblockpos.set(l, i1, j1));
               BlockState blockstate = blockgetter.getBlockState(blockpos_mutableblockpos.set(l, i1, j1));
               if (fluidstate.isEmpty() && blockstate.isPathfindable(blockgetter, blockpos_mutableblockpos.below(), PathComputationType.WATER) && blockstate.isAir()) {
                  return BlockPathTypes.BREACH;
               }

               if (!fluidstate.is(FluidTags.WATER)) {
                  return BlockPathTypes.BLOCKED;
               }
            }
         }
      }

      BlockState blockstate1 = blockgetter.getBlockState(blockpos_mutableblockpos);
      return blockstate1.isPathfindable(blockgetter, blockpos_mutableblockpos, PathComputationType.WATER) ? BlockPathTypes.WATER : BlockPathTypes.BLOCKED;
   }
}
