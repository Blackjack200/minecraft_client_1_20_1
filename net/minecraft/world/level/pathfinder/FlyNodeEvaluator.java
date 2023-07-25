package net.minecraft.world.level.pathfinder;

import it.unimi.dsi.fastutil.longs.Long2ObjectMap;
import it.unimi.dsi.fastutil.longs.Long2ObjectOpenHashMap;
import java.util.EnumSet;
import java.util.List;
import javax.annotation.Nullable;
import net.minecraft.core.BlockPos;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.PathNavigationRegion;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.AABB;

public class FlyNodeEvaluator extends WalkNodeEvaluator {
   private final Long2ObjectMap<BlockPathTypes> pathTypeByPosCache = new Long2ObjectOpenHashMap<>();
   private static final float SMALL_MOB_INFLATED_START_NODE_BOUNDING_BOX = 1.5F;
   private static final int MAX_START_NODE_CANDIDATES = 10;

   public void prepare(PathNavigationRegion pathnavigationregion, Mob mob) {
      super.prepare(pathnavigationregion, mob);
      this.pathTypeByPosCache.clear();
      mob.onPathfindingStart();
   }

   public void done() {
      this.mob.onPathfindingDone();
      this.pathTypeByPosCache.clear();
      super.done();
   }

   public Node getStart() {
      int i;
      if (this.canFloat() && this.mob.isInWater()) {
         i = this.mob.getBlockY();
         BlockPos.MutableBlockPos blockpos_mutableblockpos = new BlockPos.MutableBlockPos(this.mob.getX(), (double)i, this.mob.getZ());

         for(BlockState blockstate = this.level.getBlockState(blockpos_mutableblockpos); blockstate.is(Blocks.WATER); blockstate = this.level.getBlockState(blockpos_mutableblockpos)) {
            ++i;
            blockpos_mutableblockpos.set(this.mob.getX(), (double)i, this.mob.getZ());
         }
      } else {
         i = Mth.floor(this.mob.getY() + 0.5D);
      }

      BlockPos blockpos = BlockPos.containing(this.mob.getX(), (double)i, this.mob.getZ());
      if (!this.canStartAt(blockpos)) {
         for(BlockPos blockpos1 : this.iteratePathfindingStartNodeCandidatePositions(this.mob)) {
            if (this.canStartAt(blockpos1)) {
               return super.getStartNode(blockpos1);
            }
         }
      }

      return super.getStartNode(blockpos);
   }

   protected boolean canStartAt(BlockPos blockpos) {
      BlockPathTypes blockpathtypes = this.getBlockPathType(this.mob, blockpos);
      return this.mob.getPathfindingMalus(blockpathtypes) >= 0.0F;
   }

   public Target getGoal(double d0, double d1, double d2) {
      return this.getTargetFromNode(this.getNode(Mth.floor(d0), Mth.floor(d1), Mth.floor(d2)));
   }

   public int getNeighbors(Node[] anode, Node node) {
      int i = 0;
      Node node1 = this.findAcceptedNode(node.x, node.y, node.z + 1);
      if (this.isOpen(node1)) {
         anode[i++] = node1;
      }

      Node node2 = this.findAcceptedNode(node.x - 1, node.y, node.z);
      if (this.isOpen(node2)) {
         anode[i++] = node2;
      }

      Node node3 = this.findAcceptedNode(node.x + 1, node.y, node.z);
      if (this.isOpen(node3)) {
         anode[i++] = node3;
      }

      Node node4 = this.findAcceptedNode(node.x, node.y, node.z - 1);
      if (this.isOpen(node4)) {
         anode[i++] = node4;
      }

      Node node5 = this.findAcceptedNode(node.x, node.y + 1, node.z);
      if (this.isOpen(node5)) {
         anode[i++] = node5;
      }

      Node node6 = this.findAcceptedNode(node.x, node.y - 1, node.z);
      if (this.isOpen(node6)) {
         anode[i++] = node6;
      }

      Node node7 = this.findAcceptedNode(node.x, node.y + 1, node.z + 1);
      if (this.isOpen(node7) && this.hasMalus(node1) && this.hasMalus(node5)) {
         anode[i++] = node7;
      }

      Node node8 = this.findAcceptedNode(node.x - 1, node.y + 1, node.z);
      if (this.isOpen(node8) && this.hasMalus(node2) && this.hasMalus(node5)) {
         anode[i++] = node8;
      }

      Node node9 = this.findAcceptedNode(node.x + 1, node.y + 1, node.z);
      if (this.isOpen(node9) && this.hasMalus(node3) && this.hasMalus(node5)) {
         anode[i++] = node9;
      }

      Node node10 = this.findAcceptedNode(node.x, node.y + 1, node.z - 1);
      if (this.isOpen(node10) && this.hasMalus(node4) && this.hasMalus(node5)) {
         anode[i++] = node10;
      }

      Node node11 = this.findAcceptedNode(node.x, node.y - 1, node.z + 1);
      if (this.isOpen(node11) && this.hasMalus(node1) && this.hasMalus(node6)) {
         anode[i++] = node11;
      }

      Node node12 = this.findAcceptedNode(node.x - 1, node.y - 1, node.z);
      if (this.isOpen(node12) && this.hasMalus(node2) && this.hasMalus(node6)) {
         anode[i++] = node12;
      }

      Node node13 = this.findAcceptedNode(node.x + 1, node.y - 1, node.z);
      if (this.isOpen(node13) && this.hasMalus(node3) && this.hasMalus(node6)) {
         anode[i++] = node13;
      }

      Node node14 = this.findAcceptedNode(node.x, node.y - 1, node.z - 1);
      if (this.isOpen(node14) && this.hasMalus(node4) && this.hasMalus(node6)) {
         anode[i++] = node14;
      }

      Node node15 = this.findAcceptedNode(node.x + 1, node.y, node.z - 1);
      if (this.isOpen(node15) && this.hasMalus(node4) && this.hasMalus(node3)) {
         anode[i++] = node15;
      }

      Node node16 = this.findAcceptedNode(node.x + 1, node.y, node.z + 1);
      if (this.isOpen(node16) && this.hasMalus(node1) && this.hasMalus(node3)) {
         anode[i++] = node16;
      }

      Node node17 = this.findAcceptedNode(node.x - 1, node.y, node.z - 1);
      if (this.isOpen(node17) && this.hasMalus(node4) && this.hasMalus(node2)) {
         anode[i++] = node17;
      }

      Node node18 = this.findAcceptedNode(node.x - 1, node.y, node.z + 1);
      if (this.isOpen(node18) && this.hasMalus(node1) && this.hasMalus(node2)) {
         anode[i++] = node18;
      }

      Node node19 = this.findAcceptedNode(node.x + 1, node.y + 1, node.z - 1);
      if (this.isOpen(node19) && this.hasMalus(node15) && this.hasMalus(node4) && this.hasMalus(node3) && this.hasMalus(node5) && this.hasMalus(node10) && this.hasMalus(node9)) {
         anode[i++] = node19;
      }

      Node node20 = this.findAcceptedNode(node.x + 1, node.y + 1, node.z + 1);
      if (this.isOpen(node20) && this.hasMalus(node16) && this.hasMalus(node1) && this.hasMalus(node3) && this.hasMalus(node5) && this.hasMalus(node7) && this.hasMalus(node9)) {
         anode[i++] = node20;
      }

      Node node21 = this.findAcceptedNode(node.x - 1, node.y + 1, node.z - 1);
      if (this.isOpen(node21) && this.hasMalus(node17) && this.hasMalus(node4) && this.hasMalus(node2) && this.hasMalus(node5) && this.hasMalus(node10) && this.hasMalus(node8)) {
         anode[i++] = node21;
      }

      Node node22 = this.findAcceptedNode(node.x - 1, node.y + 1, node.z + 1);
      if (this.isOpen(node22) && this.hasMalus(node18) && this.hasMalus(node1) && this.hasMalus(node2) && this.hasMalus(node5) && this.hasMalus(node7) && this.hasMalus(node8)) {
         anode[i++] = node22;
      }

      Node node23 = this.findAcceptedNode(node.x + 1, node.y - 1, node.z - 1);
      if (this.isOpen(node23) && this.hasMalus(node15) && this.hasMalus(node4) && this.hasMalus(node3) && this.hasMalus(node6) && this.hasMalus(node14) && this.hasMalus(node13)) {
         anode[i++] = node23;
      }

      Node node24 = this.findAcceptedNode(node.x + 1, node.y - 1, node.z + 1);
      if (this.isOpen(node24) && this.hasMalus(node16) && this.hasMalus(node1) && this.hasMalus(node3) && this.hasMalus(node6) && this.hasMalus(node11) && this.hasMalus(node13)) {
         anode[i++] = node24;
      }

      Node node25 = this.findAcceptedNode(node.x - 1, node.y - 1, node.z - 1);
      if (this.isOpen(node25) && this.hasMalus(node17) && this.hasMalus(node4) && this.hasMalus(node2) && this.hasMalus(node6) && this.hasMalus(node14) && this.hasMalus(node12)) {
         anode[i++] = node25;
      }

      Node node26 = this.findAcceptedNode(node.x - 1, node.y - 1, node.z + 1);
      if (this.isOpen(node26) && this.hasMalus(node18) && this.hasMalus(node1) && this.hasMalus(node2) && this.hasMalus(node6) && this.hasMalus(node11) && this.hasMalus(node12)) {
         anode[i++] = node26;
      }

      return i;
   }

   private boolean hasMalus(@Nullable Node node) {
      return node != null && node.costMalus >= 0.0F;
   }

   private boolean isOpen(@Nullable Node node) {
      return node != null && !node.closed;
   }

   @Nullable
   protected Node findAcceptedNode(int i, int j, int k) {
      Node node = null;
      BlockPathTypes blockpathtypes = this.getCachedBlockPathType(i, j, k);
      float f = this.mob.getPathfindingMalus(blockpathtypes);
      if (f >= 0.0F) {
         node = this.getNode(i, j, k);
         node.type = blockpathtypes;
         node.costMalus = Math.max(node.costMalus, f);
         if (blockpathtypes == BlockPathTypes.WALKABLE) {
            ++node.costMalus;
         }
      }

      return node;
   }

   private BlockPathTypes getCachedBlockPathType(int i, int j, int k) {
      return this.pathTypeByPosCache.computeIfAbsent(BlockPos.asLong(i, j, k), (k1) -> this.getBlockPathType(this.level, i, j, k, this.mob));
   }

   public BlockPathTypes getBlockPathType(BlockGetter blockgetter, int i, int j, int k, Mob mob) {
      EnumSet<BlockPathTypes> enumset = EnumSet.noneOf(BlockPathTypes.class);
      BlockPathTypes blockpathtypes = BlockPathTypes.BLOCKED;
      BlockPos blockpos = mob.blockPosition();
      blockpathtypes = super.getBlockPathTypes(blockgetter, i, j, k, enumset, blockpathtypes, blockpos);
      if (enumset.contains(BlockPathTypes.FENCE)) {
         return BlockPathTypes.FENCE;
      } else {
         BlockPathTypes blockpathtypes1 = BlockPathTypes.BLOCKED;

         for(BlockPathTypes blockpathtypes2 : enumset) {
            if (mob.getPathfindingMalus(blockpathtypes2) < 0.0F) {
               return blockpathtypes2;
            }

            if (mob.getPathfindingMalus(blockpathtypes2) >= mob.getPathfindingMalus(blockpathtypes1)) {
               blockpathtypes1 = blockpathtypes2;
            }
         }

         return blockpathtypes == BlockPathTypes.OPEN && mob.getPathfindingMalus(blockpathtypes1) == 0.0F ? BlockPathTypes.OPEN : blockpathtypes1;
      }
   }

   public BlockPathTypes getBlockPathType(BlockGetter blockgetter, int i, int j, int k) {
      BlockPos.MutableBlockPos blockpos_mutableblockpos = new BlockPos.MutableBlockPos();
      BlockPathTypes blockpathtypes = getBlockPathTypeRaw(blockgetter, blockpos_mutableblockpos.set(i, j, k));
      if (blockpathtypes == BlockPathTypes.OPEN && j >= blockgetter.getMinBuildHeight() + 1) {
         BlockPathTypes blockpathtypes1 = getBlockPathTypeRaw(blockgetter, blockpos_mutableblockpos.set(i, j - 1, k));
         if (blockpathtypes1 != BlockPathTypes.DAMAGE_FIRE && blockpathtypes1 != BlockPathTypes.LAVA) {
            if (blockpathtypes1 == BlockPathTypes.DAMAGE_OTHER) {
               blockpathtypes = BlockPathTypes.DAMAGE_OTHER;
            } else if (blockpathtypes1 == BlockPathTypes.COCOA) {
               blockpathtypes = BlockPathTypes.COCOA;
            } else if (blockpathtypes1 == BlockPathTypes.FENCE) {
               if (!blockpos_mutableblockpos.equals(this.mob.blockPosition())) {
                  blockpathtypes = BlockPathTypes.FENCE;
               }
            } else {
               blockpathtypes = blockpathtypes1 != BlockPathTypes.WALKABLE && blockpathtypes1 != BlockPathTypes.OPEN && blockpathtypes1 != BlockPathTypes.WATER ? BlockPathTypes.WALKABLE : BlockPathTypes.OPEN;
            }
         } else {
            blockpathtypes = BlockPathTypes.DAMAGE_FIRE;
         }
      }

      if (blockpathtypes == BlockPathTypes.WALKABLE || blockpathtypes == BlockPathTypes.OPEN) {
         blockpathtypes = checkNeighbourBlocks(blockgetter, blockpos_mutableblockpos.set(i, j, k), blockpathtypes);
      }

      return blockpathtypes;
   }

   private Iterable<BlockPos> iteratePathfindingStartNodeCandidatePositions(Mob mob) {
      float f = 1.0F;
      AABB aabb = mob.getBoundingBox();
      boolean flag = aabb.getSize() < 1.0D;
      if (!flag) {
         return List.of(BlockPos.containing(aabb.minX, (double)mob.getBlockY(), aabb.minZ), BlockPos.containing(aabb.minX, (double)mob.getBlockY(), aabb.maxZ), BlockPos.containing(aabb.maxX, (double)mob.getBlockY(), aabb.minZ), BlockPos.containing(aabb.maxX, (double)mob.getBlockY(), aabb.maxZ));
      } else {
         double d0 = Math.max(0.0D, (1.5D - aabb.getZsize()) / 2.0D);
         double d1 = Math.max(0.0D, (1.5D - aabb.getXsize()) / 2.0D);
         double d2 = Math.max(0.0D, (1.5D - aabb.getYsize()) / 2.0D);
         AABB aabb1 = aabb.inflate(d1, d2, d0);
         return BlockPos.randomBetweenClosed(mob.getRandom(), 10, Mth.floor(aabb1.minX), Mth.floor(aabb1.minY), Mth.floor(aabb1.minZ), Mth.floor(aabb1.maxX), Mth.floor(aabb1.maxY), Mth.floor(aabb1.maxZ));
      }
   }
}
