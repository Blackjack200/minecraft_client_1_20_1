package net.minecraft.world.level.block.piston;

import com.google.common.collect.Lists;
import java.util.List;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.material.PushReaction;

public class PistonStructureResolver {
   public static final int MAX_PUSH_DEPTH = 12;
   private final Level level;
   private final BlockPos pistonPos;
   private final boolean extending;
   private final BlockPos startPos;
   private final Direction pushDirection;
   private final List<BlockPos> toPush = Lists.newArrayList();
   private final List<BlockPos> toDestroy = Lists.newArrayList();
   private final Direction pistonDirection;

   public PistonStructureResolver(Level level, BlockPos blockpos, Direction direction, boolean flag) {
      this.level = level;
      this.pistonPos = blockpos;
      this.pistonDirection = direction;
      this.extending = flag;
      if (flag) {
         this.pushDirection = direction;
         this.startPos = blockpos.relative(direction);
      } else {
         this.pushDirection = direction.getOpposite();
         this.startPos = blockpos.relative(direction, 2);
      }

   }

   public boolean resolve() {
      this.toPush.clear();
      this.toDestroy.clear();
      BlockState blockstate = this.level.getBlockState(this.startPos);
      if (!PistonBaseBlock.isPushable(blockstate, this.level, this.startPos, this.pushDirection, false, this.pistonDirection)) {
         if (this.extending && blockstate.getPistonPushReaction() == PushReaction.DESTROY) {
            this.toDestroy.add(this.startPos);
            return true;
         } else {
            return false;
         }
      } else if (!this.addBlockLine(this.startPos, this.pushDirection)) {
         return false;
      } else {
         for(int i = 0; i < this.toPush.size(); ++i) {
            BlockPos blockpos = this.toPush.get(i);
            if (isSticky(this.level.getBlockState(blockpos)) && !this.addBranchingBlocks(blockpos)) {
               return false;
            }
         }

         return true;
      }
   }

   private static boolean isSticky(BlockState blockstate) {
      return blockstate.is(Blocks.SLIME_BLOCK) || blockstate.is(Blocks.HONEY_BLOCK);
   }

   private static boolean canStickToEachOther(BlockState blockstate, BlockState blockstate1) {
      if (blockstate.is(Blocks.HONEY_BLOCK) && blockstate1.is(Blocks.SLIME_BLOCK)) {
         return false;
      } else if (blockstate.is(Blocks.SLIME_BLOCK) && blockstate1.is(Blocks.HONEY_BLOCK)) {
         return false;
      } else {
         return isSticky(blockstate) || isSticky(blockstate1);
      }
   }

   private boolean addBlockLine(BlockPos blockpos, Direction direction) {
      BlockState blockstate = this.level.getBlockState(blockpos);
      if (blockstate.isAir()) {
         return true;
      } else if (!PistonBaseBlock.isPushable(blockstate, this.level, blockpos, this.pushDirection, false, direction)) {
         return true;
      } else if (blockpos.equals(this.pistonPos)) {
         return true;
      } else if (this.toPush.contains(blockpos)) {
         return true;
      } else {
         int i = 1;
         if (i + this.toPush.size() > 12) {
            return false;
         } else {
            while(isSticky(blockstate)) {
               BlockPos blockpos1 = blockpos.relative(this.pushDirection.getOpposite(), i);
               BlockState blockstate1 = blockstate;
               blockstate = this.level.getBlockState(blockpos1);
               if (blockstate.isAir() || !canStickToEachOther(blockstate1, blockstate) || !PistonBaseBlock.isPushable(blockstate, this.level, blockpos1, this.pushDirection, false, this.pushDirection.getOpposite()) || blockpos1.equals(this.pistonPos)) {
                  break;
               }

               ++i;
               if (i + this.toPush.size() > 12) {
                  return false;
               }
            }

            int j = 0;

            for(int k = i - 1; k >= 0; --k) {
               this.toPush.add(blockpos.relative(this.pushDirection.getOpposite(), k));
               ++j;
            }

            int l = 1;

            while(true) {
               BlockPos blockpos2 = blockpos.relative(this.pushDirection, l);
               int i1 = this.toPush.indexOf(blockpos2);
               if (i1 > -1) {
                  this.reorderListAtCollision(j, i1);

                  for(int j1 = 0; j1 <= i1 + j; ++j1) {
                     BlockPos blockpos3 = this.toPush.get(j1);
                     if (isSticky(this.level.getBlockState(blockpos3)) && !this.addBranchingBlocks(blockpos3)) {
                        return false;
                     }
                  }

                  return true;
               }

               blockstate = this.level.getBlockState(blockpos2);
               if (blockstate.isAir()) {
                  return true;
               }

               if (!PistonBaseBlock.isPushable(blockstate, this.level, blockpos2, this.pushDirection, true, this.pushDirection) || blockpos2.equals(this.pistonPos)) {
                  return false;
               }

               if (blockstate.getPistonPushReaction() == PushReaction.DESTROY) {
                  this.toDestroy.add(blockpos2);
                  return true;
               }

               if (this.toPush.size() >= 12) {
                  return false;
               }

               this.toPush.add(blockpos2);
               ++j;
               ++l;
            }
         }
      }
   }

   private void reorderListAtCollision(int i, int j) {
      List<BlockPos> list = Lists.newArrayList();
      List<BlockPos> list1 = Lists.newArrayList();
      List<BlockPos> list2 = Lists.newArrayList();
      list.addAll(this.toPush.subList(0, j));
      list1.addAll(this.toPush.subList(this.toPush.size() - i, this.toPush.size()));
      list2.addAll(this.toPush.subList(j, this.toPush.size() - i));
      this.toPush.clear();
      this.toPush.addAll(list);
      this.toPush.addAll(list1);
      this.toPush.addAll(list2);
   }

   private boolean addBranchingBlocks(BlockPos blockpos) {
      BlockState blockstate = this.level.getBlockState(blockpos);

      for(Direction direction : Direction.values()) {
         if (direction.getAxis() != this.pushDirection.getAxis()) {
            BlockPos blockpos1 = blockpos.relative(direction);
            BlockState blockstate1 = this.level.getBlockState(blockpos1);
            if (canStickToEachOther(blockstate1, blockstate) && !this.addBlockLine(blockpos1, direction)) {
               return false;
            }
         }
      }

      return true;
   }

   public Direction getPushDirection() {
      return this.pushDirection;
   }

   public List<BlockPos> getToPush() {
      return this.toPush;
   }

   public List<BlockPos> getToDestroy() {
      return this.toDestroy;
   }
}
