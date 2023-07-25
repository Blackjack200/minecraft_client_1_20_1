package net.minecraft.world.level.pathfinder;

import it.unimi.dsi.fastutil.longs.Long2ObjectMap;
import it.unimi.dsi.fastutil.longs.Long2ObjectOpenHashMap;
import it.unimi.dsi.fastutil.objects.Object2BooleanMap;
import it.unimi.dsi.fastutil.objects.Object2BooleanOpenHashMap;
import java.util.EnumSet;
import javax.annotation.Nullable;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.tags.BlockTags;
import net.minecraft.tags.FluidTags;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.PathNavigationRegion;
import net.minecraft.world.level.block.BaseRailBlock;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.CampfireBlock;
import net.minecraft.world.level.block.DoorBlock;
import net.minecraft.world.level.block.FenceGateBlock;
import net.minecraft.world.level.block.LeavesBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.material.FluidState;
import net.minecraft.world.level.material.Fluids;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import net.minecraft.world.phys.shapes.VoxelShape;

public class WalkNodeEvaluator extends NodeEvaluator {
   public static final double SPACE_BETWEEN_WALL_POSTS = 0.5D;
   private static final double DEFAULT_MOB_JUMP_HEIGHT = 1.125D;
   private final Long2ObjectMap<BlockPathTypes> pathTypesByPosCache = new Long2ObjectOpenHashMap<>();
   private final Object2BooleanMap<AABB> collisionCache = new Object2BooleanOpenHashMap<>();

   public void prepare(PathNavigationRegion pathnavigationregion, Mob mob) {
      super.prepare(pathnavigationregion, mob);
      mob.onPathfindingStart();
   }

   public void done() {
      this.mob.onPathfindingDone();
      this.pathTypesByPosCache.clear();
      this.collisionCache.clear();
      super.done();
   }

   public Node getStart() {
      BlockPos.MutableBlockPos blockpos_mutableblockpos = new BlockPos.MutableBlockPos();
      int i = this.mob.getBlockY();
      BlockState blockstate = this.level.getBlockState(blockpos_mutableblockpos.set(this.mob.getX(), (double)i, this.mob.getZ()));
      if (!this.mob.canStandOnFluid(blockstate.getFluidState())) {
         if (this.canFloat() && this.mob.isInWater()) {
            while(true) {
               if (!blockstate.is(Blocks.WATER) && blockstate.getFluidState() != Fluids.WATER.getSource(false)) {
                  --i;
                  break;
               }

               ++i;
               blockstate = this.level.getBlockState(blockpos_mutableblockpos.set(this.mob.getX(), (double)i, this.mob.getZ()));
            }
         } else if (this.mob.onGround()) {
            i = Mth.floor(this.mob.getY() + 0.5D);
         } else {
            BlockPos blockpos;
            for(blockpos = this.mob.blockPosition(); (this.level.getBlockState(blockpos).isAir() || this.level.getBlockState(blockpos).isPathfindable(this.level, blockpos, PathComputationType.LAND)) && blockpos.getY() > this.mob.level().getMinBuildHeight(); blockpos = blockpos.below()) {
            }

            i = blockpos.above().getY();
         }
      } else {
         while(this.mob.canStandOnFluid(blockstate.getFluidState())) {
            ++i;
            blockstate = this.level.getBlockState(blockpos_mutableblockpos.set(this.mob.getX(), (double)i, this.mob.getZ()));
         }

         --i;
      }

      BlockPos blockpos1 = this.mob.blockPosition();
      if (!this.canStartAt(blockpos_mutableblockpos.set(blockpos1.getX(), i, blockpos1.getZ()))) {
         AABB aabb = this.mob.getBoundingBox();
         if (this.canStartAt(blockpos_mutableblockpos.set(aabb.minX, (double)i, aabb.minZ)) || this.canStartAt(blockpos_mutableblockpos.set(aabb.minX, (double)i, aabb.maxZ)) || this.canStartAt(blockpos_mutableblockpos.set(aabb.maxX, (double)i, aabb.minZ)) || this.canStartAt(blockpos_mutableblockpos.set(aabb.maxX, (double)i, aabb.maxZ))) {
            return this.getStartNode(blockpos_mutableblockpos);
         }
      }

      return this.getStartNode(new BlockPos(blockpos1.getX(), i, blockpos1.getZ()));
   }

   protected Node getStartNode(BlockPos blockpos) {
      Node node = this.getNode(blockpos);
      node.type = this.getBlockPathType(this.mob, node.asBlockPos());
      node.costMalus = this.mob.getPathfindingMalus(node.type);
      return node;
   }

   protected boolean canStartAt(BlockPos blockpos) {
      BlockPathTypes blockpathtypes = this.getBlockPathType(this.mob, blockpos);
      return blockpathtypes != BlockPathTypes.OPEN && this.mob.getPathfindingMalus(blockpathtypes) >= 0.0F;
   }

   public Target getGoal(double d0, double d1, double d2) {
      return this.getTargetFromNode(this.getNode(Mth.floor(d0), Mth.floor(d1), Mth.floor(d2)));
   }

   public int getNeighbors(Node[] anode, Node node) {
      int i = 0;
      int j = 0;
      BlockPathTypes blockpathtypes = this.getCachedBlockType(this.mob, node.x, node.y + 1, node.z);
      BlockPathTypes blockpathtypes1 = this.getCachedBlockType(this.mob, node.x, node.y, node.z);
      if (this.mob.getPathfindingMalus(blockpathtypes) >= 0.0F && blockpathtypes1 != BlockPathTypes.STICKY_HONEY) {
         j = Mth.floor(Math.max(1.0F, this.mob.maxUpStep()));
      }

      double d0 = this.getFloorLevel(new BlockPos(node.x, node.y, node.z));
      Node node1 = this.findAcceptedNode(node.x, node.y, node.z + 1, j, d0, Direction.SOUTH, blockpathtypes1);
      if (this.isNeighborValid(node1, node)) {
         anode[i++] = node1;
      }

      Node node2 = this.findAcceptedNode(node.x - 1, node.y, node.z, j, d0, Direction.WEST, blockpathtypes1);
      if (this.isNeighborValid(node2, node)) {
         anode[i++] = node2;
      }

      Node node3 = this.findAcceptedNode(node.x + 1, node.y, node.z, j, d0, Direction.EAST, blockpathtypes1);
      if (this.isNeighborValid(node3, node)) {
         anode[i++] = node3;
      }

      Node node4 = this.findAcceptedNode(node.x, node.y, node.z - 1, j, d0, Direction.NORTH, blockpathtypes1);
      if (this.isNeighborValid(node4, node)) {
         anode[i++] = node4;
      }

      Node node5 = this.findAcceptedNode(node.x - 1, node.y, node.z - 1, j, d0, Direction.NORTH, blockpathtypes1);
      if (this.isDiagonalValid(node, node2, node4, node5)) {
         anode[i++] = node5;
      }

      Node node6 = this.findAcceptedNode(node.x + 1, node.y, node.z - 1, j, d0, Direction.NORTH, blockpathtypes1);
      if (this.isDiagonalValid(node, node3, node4, node6)) {
         anode[i++] = node6;
      }

      Node node7 = this.findAcceptedNode(node.x - 1, node.y, node.z + 1, j, d0, Direction.SOUTH, blockpathtypes1);
      if (this.isDiagonalValid(node, node2, node1, node7)) {
         anode[i++] = node7;
      }

      Node node8 = this.findAcceptedNode(node.x + 1, node.y, node.z + 1, j, d0, Direction.SOUTH, blockpathtypes1);
      if (this.isDiagonalValid(node, node3, node1, node8)) {
         anode[i++] = node8;
      }

      return i;
   }

   protected boolean isNeighborValid(@Nullable Node node, Node node1) {
      return node != null && !node.closed && (node.costMalus >= 0.0F || node1.costMalus < 0.0F);
   }

   protected boolean isDiagonalValid(Node node, @Nullable Node node1, @Nullable Node node2, @Nullable Node node3) {
      if (node3 != null && node2 != null && node1 != null) {
         if (node3.closed) {
            return false;
         } else if (node2.y <= node.y && node1.y <= node.y) {
            if (node1.type != BlockPathTypes.WALKABLE_DOOR && node2.type != BlockPathTypes.WALKABLE_DOOR && node3.type != BlockPathTypes.WALKABLE_DOOR) {
               boolean flag = node2.type == BlockPathTypes.FENCE && node1.type == BlockPathTypes.FENCE && (double)this.mob.getBbWidth() < 0.5D;
               return node3.costMalus >= 0.0F && (node2.y < node.y || node2.costMalus >= 0.0F || flag) && (node1.y < node.y || node1.costMalus >= 0.0F || flag);
            } else {
               return false;
            }
         } else {
            return false;
         }
      } else {
         return false;
      }
   }

   private static boolean doesBlockHavePartialCollision(BlockPathTypes blockpathtypes) {
      return blockpathtypes == BlockPathTypes.FENCE || blockpathtypes == BlockPathTypes.DOOR_WOOD_CLOSED || blockpathtypes == BlockPathTypes.DOOR_IRON_CLOSED;
   }

   private boolean canReachWithoutCollision(Node node) {
      AABB aabb = this.mob.getBoundingBox();
      Vec3 vec3 = new Vec3((double)node.x - this.mob.getX() + aabb.getXsize() / 2.0D, (double)node.y - this.mob.getY() + aabb.getYsize() / 2.0D, (double)node.z - this.mob.getZ() + aabb.getZsize() / 2.0D);
      int i = Mth.ceil(vec3.length() / aabb.getSize());
      vec3 = vec3.scale((double)(1.0F / (float)i));

      for(int j = 1; j <= i; ++j) {
         aabb = aabb.move(vec3);
         if (this.hasCollisions(aabb)) {
            return false;
         }
      }

      return true;
   }

   protected double getFloorLevel(BlockPos blockpos) {
      return (this.canFloat() || this.isAmphibious()) && this.level.getFluidState(blockpos).is(FluidTags.WATER) ? (double)blockpos.getY() + 0.5D : getFloorLevel(this.level, blockpos);
   }

   public static double getFloorLevel(BlockGetter blockgetter, BlockPos blockpos) {
      BlockPos blockpos1 = blockpos.below();
      VoxelShape voxelshape = blockgetter.getBlockState(blockpos1).getCollisionShape(blockgetter, blockpos1);
      return (double)blockpos1.getY() + (voxelshape.isEmpty() ? 0.0D : voxelshape.max(Direction.Axis.Y));
   }

   protected boolean isAmphibious() {
      return false;
   }

   @Nullable
   protected Node findAcceptedNode(int i, int j, int k, int l, double d0, Direction direction, BlockPathTypes blockpathtypes) {
      Node node = null;
      BlockPos.MutableBlockPos blockpos_mutableblockpos = new BlockPos.MutableBlockPos();
      double d1 = this.getFloorLevel(blockpos_mutableblockpos.set(i, j, k));
      if (d1 - d0 > this.getMobJumpHeight()) {
         return null;
      } else {
         BlockPathTypes blockpathtypes1 = this.getCachedBlockType(this.mob, i, j, k);
         float f = this.mob.getPathfindingMalus(blockpathtypes1);
         double d2 = (double)this.mob.getBbWidth() / 2.0D;
         if (f >= 0.0F) {
            node = this.getNodeAndUpdateCostToMax(i, j, k, blockpathtypes1, f);
         }

         if (doesBlockHavePartialCollision(blockpathtypes) && node != null && node.costMalus >= 0.0F && !this.canReachWithoutCollision(node)) {
            node = null;
         }

         if (blockpathtypes1 != BlockPathTypes.WALKABLE && (!this.isAmphibious() || blockpathtypes1 != BlockPathTypes.WATER)) {
            if ((node == null || node.costMalus < 0.0F) && l > 0 && (blockpathtypes1 != BlockPathTypes.FENCE || this.canWalkOverFences()) && blockpathtypes1 != BlockPathTypes.UNPASSABLE_RAIL && blockpathtypes1 != BlockPathTypes.TRAPDOOR && blockpathtypes1 != BlockPathTypes.POWDER_SNOW) {
               node = this.findAcceptedNode(i, j + 1, k, l - 1, d0, direction, blockpathtypes);
               if (node != null && (node.type == BlockPathTypes.OPEN || node.type == BlockPathTypes.WALKABLE) && this.mob.getBbWidth() < 1.0F) {
                  double d3 = (double)(i - direction.getStepX()) + 0.5D;
                  double d4 = (double)(k - direction.getStepZ()) + 0.5D;
                  AABB aabb = new AABB(d3 - d2, this.getFloorLevel(blockpos_mutableblockpos.set(d3, (double)(j + 1), d4)) + 0.001D, d4 - d2, d3 + d2, (double)this.mob.getBbHeight() + this.getFloorLevel(blockpos_mutableblockpos.set((double)node.x, (double)node.y, (double)node.z)) - 0.002D, d4 + d2);
                  if (this.hasCollisions(aabb)) {
                     node = null;
                  }
               }
            }

            if (!this.isAmphibious() && blockpathtypes1 == BlockPathTypes.WATER && !this.canFloat()) {
               if (this.getCachedBlockType(this.mob, i, j - 1, k) != BlockPathTypes.WATER) {
                  return node;
               }

               while(j > this.mob.level().getMinBuildHeight()) {
                  --j;
                  blockpathtypes1 = this.getCachedBlockType(this.mob, i, j, k);
                  if (blockpathtypes1 != BlockPathTypes.WATER) {
                     return node;
                  }

                  node = this.getNodeAndUpdateCostToMax(i, j, k, blockpathtypes1, this.mob.getPathfindingMalus(blockpathtypes1));
               }
            }

            if (blockpathtypes1 == BlockPathTypes.OPEN) {
               int i1 = 0;
               int j1 = j;

               while(blockpathtypes1 == BlockPathTypes.OPEN) {
                  --j;
                  if (j < this.mob.level().getMinBuildHeight()) {
                     return this.getBlockedNode(i, j1, k);
                  }

                  if (i1++ >= this.mob.getMaxFallDistance()) {
                     return this.getBlockedNode(i, j, k);
                  }

                  blockpathtypes1 = this.getCachedBlockType(this.mob, i, j, k);
                  f = this.mob.getPathfindingMalus(blockpathtypes1);
                  if (blockpathtypes1 != BlockPathTypes.OPEN && f >= 0.0F) {
                     node = this.getNodeAndUpdateCostToMax(i, j, k, blockpathtypes1, f);
                     break;
                  }

                  if (f < 0.0F) {
                     return this.getBlockedNode(i, j, k);
                  }
               }
            }

            if (doesBlockHavePartialCollision(blockpathtypes1) && node == null) {
               node = this.getNode(i, j, k);
               node.closed = true;
               node.type = blockpathtypes1;
               node.costMalus = blockpathtypes1.getMalus();
            }

            return node;
         } else {
            return node;
         }
      }
   }

   private double getMobJumpHeight() {
      return Math.max(1.125D, (double)this.mob.maxUpStep());
   }

   private Node getNodeAndUpdateCostToMax(int i, int j, int k, BlockPathTypes blockpathtypes, float f) {
      Node node = this.getNode(i, j, k);
      node.type = blockpathtypes;
      node.costMalus = Math.max(node.costMalus, f);
      return node;
   }

   private Node getBlockedNode(int i, int j, int k) {
      Node node = this.getNode(i, j, k);
      node.type = BlockPathTypes.BLOCKED;
      node.costMalus = -1.0F;
      return node;
   }

   private boolean hasCollisions(AABB aabb) {
      return this.collisionCache.computeIfAbsent(aabb, (object) -> !this.level.noCollision(this.mob, aabb));
   }

   public BlockPathTypes getBlockPathType(BlockGetter blockgetter, int i, int j, int k, Mob mob) {
      EnumSet<BlockPathTypes> enumset = EnumSet.noneOf(BlockPathTypes.class);
      BlockPathTypes blockpathtypes = BlockPathTypes.BLOCKED;
      blockpathtypes = this.getBlockPathTypes(blockgetter, i, j, k, enumset, blockpathtypes, mob.blockPosition());
      if (enumset.contains(BlockPathTypes.FENCE)) {
         return BlockPathTypes.FENCE;
      } else if (enumset.contains(BlockPathTypes.UNPASSABLE_RAIL)) {
         return BlockPathTypes.UNPASSABLE_RAIL;
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

         return blockpathtypes == BlockPathTypes.OPEN && mob.getPathfindingMalus(blockpathtypes1) == 0.0F && this.entityWidth <= 1 ? BlockPathTypes.OPEN : blockpathtypes1;
      }
   }

   public BlockPathTypes getBlockPathTypes(BlockGetter blockgetter, int i, int j, int k, EnumSet<BlockPathTypes> enumset, BlockPathTypes blockpathtypes, BlockPos blockpos) {
      for(int l = 0; l < this.entityWidth; ++l) {
         for(int i1 = 0; i1 < this.entityHeight; ++i1) {
            for(int j1 = 0; j1 < this.entityDepth; ++j1) {
               int k1 = l + i;
               int l1 = i1 + j;
               int i2 = j1 + k;
               BlockPathTypes blockpathtypes1 = this.getBlockPathType(blockgetter, k1, l1, i2);
               blockpathtypes1 = this.evaluateBlockPathType(blockgetter, blockpos, blockpathtypes1);
               if (l == 0 && i1 == 0 && j1 == 0) {
                  blockpathtypes = blockpathtypes1;
               }

               enumset.add(blockpathtypes1);
            }
         }
      }

      return blockpathtypes;
   }

   protected BlockPathTypes evaluateBlockPathType(BlockGetter blockgetter, BlockPos blockpos, BlockPathTypes blockpathtypes) {
      boolean flag = this.canPassDoors();
      if (blockpathtypes == BlockPathTypes.DOOR_WOOD_CLOSED && this.canOpenDoors() && flag) {
         blockpathtypes = BlockPathTypes.WALKABLE_DOOR;
      }

      if (blockpathtypes == BlockPathTypes.DOOR_OPEN && !flag) {
         blockpathtypes = BlockPathTypes.BLOCKED;
      }

      if (blockpathtypes == BlockPathTypes.RAIL && !(blockgetter.getBlockState(blockpos).getBlock() instanceof BaseRailBlock) && !(blockgetter.getBlockState(blockpos.below()).getBlock() instanceof BaseRailBlock)) {
         blockpathtypes = BlockPathTypes.UNPASSABLE_RAIL;
      }

      return blockpathtypes;
   }

   protected BlockPathTypes getBlockPathType(Mob mob, BlockPos blockpos) {
      return this.getCachedBlockType(mob, blockpos.getX(), blockpos.getY(), blockpos.getZ());
   }

   protected BlockPathTypes getCachedBlockType(Mob mob, int i, int j, int k) {
      return this.pathTypesByPosCache.computeIfAbsent(BlockPos.asLong(i, j, k), (k1) -> this.getBlockPathType(this.level, i, j, k, mob));
   }

   public BlockPathTypes getBlockPathType(BlockGetter blockgetter, int i, int j, int k) {
      return getBlockPathTypeStatic(blockgetter, new BlockPos.MutableBlockPos(i, j, k));
   }

   public static BlockPathTypes getBlockPathTypeStatic(BlockGetter blockgetter, BlockPos.MutableBlockPos blockpos_mutableblockpos) {
      int i = blockpos_mutableblockpos.getX();
      int j = blockpos_mutableblockpos.getY();
      int k = blockpos_mutableblockpos.getZ();
      BlockPathTypes blockpathtypes = getBlockPathTypeRaw(blockgetter, blockpos_mutableblockpos);
      if (blockpathtypes == BlockPathTypes.OPEN && j >= blockgetter.getMinBuildHeight() + 1) {
         BlockPathTypes blockpathtypes1 = getBlockPathTypeRaw(blockgetter, blockpos_mutableblockpos.set(i, j - 1, k));
         blockpathtypes = blockpathtypes1 != BlockPathTypes.WALKABLE && blockpathtypes1 != BlockPathTypes.OPEN && blockpathtypes1 != BlockPathTypes.WATER && blockpathtypes1 != BlockPathTypes.LAVA ? BlockPathTypes.WALKABLE : BlockPathTypes.OPEN;
         if (blockpathtypes1 == BlockPathTypes.DAMAGE_FIRE) {
            blockpathtypes = BlockPathTypes.DAMAGE_FIRE;
         }

         if (blockpathtypes1 == BlockPathTypes.DAMAGE_OTHER) {
            blockpathtypes = BlockPathTypes.DAMAGE_OTHER;
         }

         if (blockpathtypes1 == BlockPathTypes.STICKY_HONEY) {
            blockpathtypes = BlockPathTypes.STICKY_HONEY;
         }

         if (blockpathtypes1 == BlockPathTypes.POWDER_SNOW) {
            blockpathtypes = BlockPathTypes.DANGER_POWDER_SNOW;
         }

         if (blockpathtypes1 == BlockPathTypes.DAMAGE_CAUTIOUS) {
            blockpathtypes = BlockPathTypes.DAMAGE_CAUTIOUS;
         }
      }

      if (blockpathtypes == BlockPathTypes.WALKABLE) {
         blockpathtypes = checkNeighbourBlocks(blockgetter, blockpos_mutableblockpos.set(i, j, k), blockpathtypes);
      }

      return blockpathtypes;
   }

   public static BlockPathTypes checkNeighbourBlocks(BlockGetter blockgetter, BlockPos.MutableBlockPos blockpos_mutableblockpos, BlockPathTypes blockpathtypes) {
      int i = blockpos_mutableblockpos.getX();
      int j = blockpos_mutableblockpos.getY();
      int k = blockpos_mutableblockpos.getZ();

      for(int l = -1; l <= 1; ++l) {
         for(int i1 = -1; i1 <= 1; ++i1) {
            for(int j1 = -1; j1 <= 1; ++j1) {
               if (l != 0 || j1 != 0) {
                  blockpos_mutableblockpos.set(i + l, j + i1, k + j1);
                  BlockState blockstate = blockgetter.getBlockState(blockpos_mutableblockpos);
                  if (blockstate.is(Blocks.CACTUS) || blockstate.is(Blocks.SWEET_BERRY_BUSH)) {
                     return BlockPathTypes.DANGER_OTHER;
                  }

                  if (isBurningBlock(blockstate)) {
                     return BlockPathTypes.DANGER_FIRE;
                  }

                  if (blockgetter.getFluidState(blockpos_mutableblockpos).is(FluidTags.WATER)) {
                     return BlockPathTypes.WATER_BORDER;
                  }

                  if (blockstate.is(Blocks.WITHER_ROSE) || blockstate.is(Blocks.POINTED_DRIPSTONE)) {
                     return BlockPathTypes.DAMAGE_CAUTIOUS;
                  }
               }
            }
         }
      }

      return blockpathtypes;
   }

   protected static BlockPathTypes getBlockPathTypeRaw(BlockGetter blockgetter, BlockPos blockpos) {
      BlockState blockstate = blockgetter.getBlockState(blockpos);
      Block block = blockstate.getBlock();
      if (blockstate.isAir()) {
         return BlockPathTypes.OPEN;
      } else if (!blockstate.is(BlockTags.TRAPDOORS) && !blockstate.is(Blocks.LILY_PAD) && !blockstate.is(Blocks.BIG_DRIPLEAF)) {
         if (blockstate.is(Blocks.POWDER_SNOW)) {
            return BlockPathTypes.POWDER_SNOW;
         } else if (!blockstate.is(Blocks.CACTUS) && !blockstate.is(Blocks.SWEET_BERRY_BUSH)) {
            if (blockstate.is(Blocks.HONEY_BLOCK)) {
               return BlockPathTypes.STICKY_HONEY;
            } else if (blockstate.is(Blocks.COCOA)) {
               return BlockPathTypes.COCOA;
            } else if (!blockstate.is(Blocks.WITHER_ROSE) && !blockstate.is(Blocks.POINTED_DRIPSTONE)) {
               FluidState fluidstate = blockgetter.getFluidState(blockpos);
               if (fluidstate.is(FluidTags.LAVA)) {
                  return BlockPathTypes.LAVA;
               } else if (isBurningBlock(blockstate)) {
                  return BlockPathTypes.DAMAGE_FIRE;
               } else if (block instanceof DoorBlock) {
                  DoorBlock doorblock = (DoorBlock)block;
                  if (blockstate.getValue(DoorBlock.OPEN)) {
                     return BlockPathTypes.DOOR_OPEN;
                  } else {
                     return doorblock.type().canOpenByHand() ? BlockPathTypes.DOOR_WOOD_CLOSED : BlockPathTypes.DOOR_IRON_CLOSED;
                  }
               } else if (block instanceof BaseRailBlock) {
                  return BlockPathTypes.RAIL;
               } else if (block instanceof LeavesBlock) {
                  return BlockPathTypes.LEAVES;
               } else if (!blockstate.is(BlockTags.FENCES) && !blockstate.is(BlockTags.WALLS) && (!(block instanceof FenceGateBlock) || blockstate.getValue(FenceGateBlock.OPEN))) {
                  if (!blockstate.isPathfindable(blockgetter, blockpos, PathComputationType.LAND)) {
                     return BlockPathTypes.BLOCKED;
                  } else {
                     return fluidstate.is(FluidTags.WATER) ? BlockPathTypes.WATER : BlockPathTypes.OPEN;
                  }
               } else {
                  return BlockPathTypes.FENCE;
               }
            } else {
               return BlockPathTypes.DAMAGE_CAUTIOUS;
            }
         } else {
            return BlockPathTypes.DAMAGE_OTHER;
         }
      } else {
         return BlockPathTypes.TRAPDOOR;
      }
   }

   public static boolean isBurningBlock(BlockState blockstate) {
      return blockstate.is(BlockTags.FIRE) || blockstate.is(Blocks.LAVA) || blockstate.is(Blocks.MAGMA_BLOCK) || CampfireBlock.isLitCampfire(blockstate) || blockstate.is(Blocks.LAVA_CAULDRON);
   }
}
