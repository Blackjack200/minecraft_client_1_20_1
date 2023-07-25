package net.minecraft.world.level.block;

import com.google.common.collect.Lists;
import java.util.List;
import javax.annotation.Nullable;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.RailShape;

public class RailState {
   private final Level level;
   private final BlockPos pos;
   private final BaseRailBlock block;
   private BlockState state;
   private final boolean isStraight;
   private final List<BlockPos> connections = Lists.newArrayList();

   public RailState(Level level, BlockPos blockpos, BlockState blockstate) {
      this.level = level;
      this.pos = blockpos;
      this.state = blockstate;
      this.block = (BaseRailBlock)blockstate.getBlock();
      RailShape railshape = blockstate.getValue(this.block.getShapeProperty());
      this.isStraight = this.block.isStraight();
      this.updateConnections(railshape);
   }

   public List<BlockPos> getConnections() {
      return this.connections;
   }

   private void updateConnections(RailShape railshape) {
      this.connections.clear();
      switch (railshape) {
         case NORTH_SOUTH:
            this.connections.add(this.pos.north());
            this.connections.add(this.pos.south());
            break;
         case EAST_WEST:
            this.connections.add(this.pos.west());
            this.connections.add(this.pos.east());
            break;
         case ASCENDING_EAST:
            this.connections.add(this.pos.west());
            this.connections.add(this.pos.east().above());
            break;
         case ASCENDING_WEST:
            this.connections.add(this.pos.west().above());
            this.connections.add(this.pos.east());
            break;
         case ASCENDING_NORTH:
            this.connections.add(this.pos.north().above());
            this.connections.add(this.pos.south());
            break;
         case ASCENDING_SOUTH:
            this.connections.add(this.pos.north());
            this.connections.add(this.pos.south().above());
            break;
         case SOUTH_EAST:
            this.connections.add(this.pos.east());
            this.connections.add(this.pos.south());
            break;
         case SOUTH_WEST:
            this.connections.add(this.pos.west());
            this.connections.add(this.pos.south());
            break;
         case NORTH_WEST:
            this.connections.add(this.pos.west());
            this.connections.add(this.pos.north());
            break;
         case NORTH_EAST:
            this.connections.add(this.pos.east());
            this.connections.add(this.pos.north());
      }

   }

   private void removeSoftConnections() {
      for(int i = 0; i < this.connections.size(); ++i) {
         RailState railstate = this.getRail(this.connections.get(i));
         if (railstate != null && railstate.connectsTo(this)) {
            this.connections.set(i, railstate.pos);
         } else {
            this.connections.remove(i--);
         }
      }

   }

   private boolean hasRail(BlockPos blockpos) {
      return BaseRailBlock.isRail(this.level, blockpos) || BaseRailBlock.isRail(this.level, blockpos.above()) || BaseRailBlock.isRail(this.level, blockpos.below());
   }

   @Nullable
   private RailState getRail(BlockPos blockpos) {
      BlockState blockstate = this.level.getBlockState(blockpos);
      if (BaseRailBlock.isRail(blockstate)) {
         return new RailState(this.level, blockpos, blockstate);
      } else {
         BlockPos blockpos1 = blockpos.above();
         blockstate = this.level.getBlockState(blockpos1);
         if (BaseRailBlock.isRail(blockstate)) {
            return new RailState(this.level, blockpos1, blockstate);
         } else {
            blockpos1 = blockpos.below();
            blockstate = this.level.getBlockState(blockpos1);
            return BaseRailBlock.isRail(blockstate) ? new RailState(this.level, blockpos1, blockstate) : null;
         }
      }
   }

   private boolean connectsTo(RailState railstate) {
      return this.hasConnection(railstate.pos);
   }

   private boolean hasConnection(BlockPos blockpos) {
      for(int i = 0; i < this.connections.size(); ++i) {
         BlockPos blockpos1 = this.connections.get(i);
         if (blockpos1.getX() == blockpos.getX() && blockpos1.getZ() == blockpos.getZ()) {
            return true;
         }
      }

      return false;
   }

   protected int countPotentialConnections() {
      int i = 0;

      for(Direction direction : Direction.Plane.HORIZONTAL) {
         if (this.hasRail(this.pos.relative(direction))) {
            ++i;
         }
      }

      return i;
   }

   private boolean canConnectTo(RailState railstate) {
      return this.connectsTo(railstate) || this.connections.size() != 2;
   }

   private void connectTo(RailState railstate) {
      this.connections.add(railstate.pos);
      BlockPos blockpos = this.pos.north();
      BlockPos blockpos1 = this.pos.south();
      BlockPos blockpos2 = this.pos.west();
      BlockPos blockpos3 = this.pos.east();
      boolean flag = this.hasConnection(blockpos);
      boolean flag1 = this.hasConnection(blockpos1);
      boolean flag2 = this.hasConnection(blockpos2);
      boolean flag3 = this.hasConnection(blockpos3);
      RailShape railshape = null;
      if (flag || flag1) {
         railshape = RailShape.NORTH_SOUTH;
      }

      if (flag2 || flag3) {
         railshape = RailShape.EAST_WEST;
      }

      if (!this.isStraight) {
         if (flag1 && flag3 && !flag && !flag2) {
            railshape = RailShape.SOUTH_EAST;
         }

         if (flag1 && flag2 && !flag && !flag3) {
            railshape = RailShape.SOUTH_WEST;
         }

         if (flag && flag2 && !flag1 && !flag3) {
            railshape = RailShape.NORTH_WEST;
         }

         if (flag && flag3 && !flag1 && !flag2) {
            railshape = RailShape.NORTH_EAST;
         }
      }

      if (railshape == RailShape.NORTH_SOUTH) {
         if (BaseRailBlock.isRail(this.level, blockpos.above())) {
            railshape = RailShape.ASCENDING_NORTH;
         }

         if (BaseRailBlock.isRail(this.level, blockpos1.above())) {
            railshape = RailShape.ASCENDING_SOUTH;
         }
      }

      if (railshape == RailShape.EAST_WEST) {
         if (BaseRailBlock.isRail(this.level, blockpos3.above())) {
            railshape = RailShape.ASCENDING_EAST;
         }

         if (BaseRailBlock.isRail(this.level, blockpos2.above())) {
            railshape = RailShape.ASCENDING_WEST;
         }
      }

      if (railshape == null) {
         railshape = RailShape.NORTH_SOUTH;
      }

      this.state = this.state.setValue(this.block.getShapeProperty(), railshape);
      this.level.setBlock(this.pos, this.state, 3);
   }

   private boolean hasNeighborRail(BlockPos blockpos) {
      RailState railstate = this.getRail(blockpos);
      if (railstate == null) {
         return false;
      } else {
         railstate.removeSoftConnections();
         return railstate.canConnectTo(this);
      }
   }

   public RailState place(boolean flag, boolean flag1, RailShape railshape) {
      BlockPos blockpos = this.pos.north();
      BlockPos blockpos1 = this.pos.south();
      BlockPos blockpos2 = this.pos.west();
      BlockPos blockpos3 = this.pos.east();
      boolean flag2 = this.hasNeighborRail(blockpos);
      boolean flag3 = this.hasNeighborRail(blockpos1);
      boolean flag4 = this.hasNeighborRail(blockpos2);
      boolean flag5 = this.hasNeighborRail(blockpos3);
      RailShape railshape1 = null;
      boolean flag6 = flag2 || flag3;
      boolean flag7 = flag4 || flag5;
      if (flag6 && !flag7) {
         railshape1 = RailShape.NORTH_SOUTH;
      }

      if (flag7 && !flag6) {
         railshape1 = RailShape.EAST_WEST;
      }

      boolean flag8 = flag3 && flag5;
      boolean flag9 = flag3 && flag4;
      boolean flag10 = flag2 && flag5;
      boolean flag11 = flag2 && flag4;
      if (!this.isStraight) {
         if (flag8 && !flag2 && !flag4) {
            railshape1 = RailShape.SOUTH_EAST;
         }

         if (flag9 && !flag2 && !flag5) {
            railshape1 = RailShape.SOUTH_WEST;
         }

         if (flag11 && !flag3 && !flag5) {
            railshape1 = RailShape.NORTH_WEST;
         }

         if (flag10 && !flag3 && !flag4) {
            railshape1 = RailShape.NORTH_EAST;
         }
      }

      if (railshape1 == null) {
         if (flag6 && flag7) {
            railshape1 = railshape;
         } else if (flag6) {
            railshape1 = RailShape.NORTH_SOUTH;
         } else if (flag7) {
            railshape1 = RailShape.EAST_WEST;
         }

         if (!this.isStraight) {
            if (flag) {
               if (flag8) {
                  railshape1 = RailShape.SOUTH_EAST;
               }

               if (flag9) {
                  railshape1 = RailShape.SOUTH_WEST;
               }

               if (flag10) {
                  railshape1 = RailShape.NORTH_EAST;
               }

               if (flag11) {
                  railshape1 = RailShape.NORTH_WEST;
               }
            } else {
               if (flag11) {
                  railshape1 = RailShape.NORTH_WEST;
               }

               if (flag10) {
                  railshape1 = RailShape.NORTH_EAST;
               }

               if (flag9) {
                  railshape1 = RailShape.SOUTH_WEST;
               }

               if (flag8) {
                  railshape1 = RailShape.SOUTH_EAST;
               }
            }
         }
      }

      if (railshape1 == RailShape.NORTH_SOUTH) {
         if (BaseRailBlock.isRail(this.level, blockpos.above())) {
            railshape1 = RailShape.ASCENDING_NORTH;
         }

         if (BaseRailBlock.isRail(this.level, blockpos1.above())) {
            railshape1 = RailShape.ASCENDING_SOUTH;
         }
      }

      if (railshape1 == RailShape.EAST_WEST) {
         if (BaseRailBlock.isRail(this.level, blockpos3.above())) {
            railshape1 = RailShape.ASCENDING_EAST;
         }

         if (BaseRailBlock.isRail(this.level, blockpos2.above())) {
            railshape1 = RailShape.ASCENDING_WEST;
         }
      }

      if (railshape1 == null) {
         railshape1 = railshape;
      }

      this.updateConnections(railshape1);
      this.state = this.state.setValue(this.block.getShapeProperty(), railshape1);
      if (flag1 || this.level.getBlockState(this.pos) != this.state) {
         this.level.setBlock(this.pos, this.state, 3);

         for(int i = 0; i < this.connections.size(); ++i) {
            RailState railstate = this.getRail(this.connections.get(i));
            if (railstate != null) {
               railstate.removeSoftConnections();
               if (railstate.canConnectTo(this)) {
                  railstate.connectTo(this);
               }
            }
         }
      }

      return this;
   }

   public BlockState getState() {
      return this.state;
   }
}
