package net.minecraft.world.level.pathfinder;

import javax.annotation.Nullable;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.PathNavigationRegion;

public class AmphibiousNodeEvaluator extends WalkNodeEvaluator {
   private final boolean prefersShallowSwimming;
   private float oldWalkableCost;
   private float oldWaterBorderCost;

   public AmphibiousNodeEvaluator(boolean flag) {
      this.prefersShallowSwimming = flag;
   }

   public void prepare(PathNavigationRegion pathnavigationregion, Mob mob) {
      super.prepare(pathnavigationregion, mob);
      mob.setPathfindingMalus(BlockPathTypes.WATER, 0.0F);
      this.oldWalkableCost = mob.getPathfindingMalus(BlockPathTypes.WALKABLE);
      mob.setPathfindingMalus(BlockPathTypes.WALKABLE, 6.0F);
      this.oldWaterBorderCost = mob.getPathfindingMalus(BlockPathTypes.WATER_BORDER);
      mob.setPathfindingMalus(BlockPathTypes.WATER_BORDER, 4.0F);
   }

   public void done() {
      this.mob.setPathfindingMalus(BlockPathTypes.WALKABLE, this.oldWalkableCost);
      this.mob.setPathfindingMalus(BlockPathTypes.WATER_BORDER, this.oldWaterBorderCost);
      super.done();
   }

   public Node getStart() {
      return !this.mob.isInWater() ? super.getStart() : this.getStartNode(new BlockPos(Mth.floor(this.mob.getBoundingBox().minX), Mth.floor(this.mob.getBoundingBox().minY + 0.5D), Mth.floor(this.mob.getBoundingBox().minZ)));
   }

   public Target getGoal(double d0, double d1, double d2) {
      return this.getTargetFromNode(this.getNode(Mth.floor(d0), Mth.floor(d1 + 0.5D), Mth.floor(d2)));
   }

   public int getNeighbors(Node[] anode, Node node) {
      int i = super.getNeighbors(anode, node);
      BlockPathTypes blockpathtypes = this.getCachedBlockType(this.mob, node.x, node.y + 1, node.z);
      BlockPathTypes blockpathtypes1 = this.getCachedBlockType(this.mob, node.x, node.y, node.z);
      int j;
      if (this.mob.getPathfindingMalus(blockpathtypes) >= 0.0F && blockpathtypes1 != BlockPathTypes.STICKY_HONEY) {
         j = Mth.floor(Math.max(1.0F, this.mob.maxUpStep()));
      } else {
         j = 0;
      }

      double d0 = this.getFloorLevel(new BlockPos(node.x, node.y, node.z));
      Node node1 = this.findAcceptedNode(node.x, node.y + 1, node.z, Math.max(0, j - 1), d0, Direction.UP, blockpathtypes1);
      Node node2 = this.findAcceptedNode(node.x, node.y - 1, node.z, j, d0, Direction.DOWN, blockpathtypes1);
      if (this.isVerticalNeighborValid(node1, node)) {
         anode[i++] = node1;
      }

      if (this.isVerticalNeighborValid(node2, node) && blockpathtypes1 != BlockPathTypes.TRAPDOOR) {
         anode[i++] = node2;
      }

      for(int l = 0; l < i; ++l) {
         Node node3 = anode[l];
         if (node3.type == BlockPathTypes.WATER && this.prefersShallowSwimming && node3.y < this.mob.level().getSeaLevel() - 10) {
            ++node3.costMalus;
         }
      }

      return i;
   }

   private boolean isVerticalNeighborValid(@Nullable Node node, Node node1) {
      return this.isNeighborValid(node, node1) && node.type == BlockPathTypes.WATER;
   }

   protected boolean isAmphibious() {
      return true;
   }

   public BlockPathTypes getBlockPathType(BlockGetter blockgetter, int i, int j, int k) {
      BlockPos.MutableBlockPos blockpos_mutableblockpos = new BlockPos.MutableBlockPos();
      BlockPathTypes blockpathtypes = getBlockPathTypeRaw(blockgetter, blockpos_mutableblockpos.set(i, j, k));
      if (blockpathtypes == BlockPathTypes.WATER) {
         for(Direction direction : Direction.values()) {
            BlockPathTypes blockpathtypes1 = getBlockPathTypeRaw(blockgetter, blockpos_mutableblockpos.set(i, j, k).move(direction));
            if (blockpathtypes1 == BlockPathTypes.BLOCKED) {
               return BlockPathTypes.WATER_BORDER;
            }
         }

         return BlockPathTypes.WATER;
      } else {
         return getBlockPathTypeStatic(blockgetter, blockpos_mutableblockpos);
      }
   }
}
