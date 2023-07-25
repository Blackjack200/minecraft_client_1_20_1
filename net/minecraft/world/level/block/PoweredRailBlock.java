package net.minecraft.world.level.block;

import net.minecraft.core.BlockPos;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.block.state.properties.BooleanProperty;
import net.minecraft.world.level.block.state.properties.EnumProperty;
import net.minecraft.world.level.block.state.properties.Property;
import net.minecraft.world.level.block.state.properties.RailShape;

public class PoweredRailBlock extends BaseRailBlock {
   public static final EnumProperty<RailShape> SHAPE = BlockStateProperties.RAIL_SHAPE_STRAIGHT;
   public static final BooleanProperty POWERED = BlockStateProperties.POWERED;

   protected PoweredRailBlock(BlockBehaviour.Properties blockbehaviour_properties) {
      super(true, blockbehaviour_properties);
      this.registerDefaultState(this.stateDefinition.any().setValue(SHAPE, RailShape.NORTH_SOUTH).setValue(POWERED, Boolean.valueOf(false)).setValue(WATERLOGGED, Boolean.valueOf(false)));
   }

   protected boolean findPoweredRailSignal(Level level, BlockPos blockpos, BlockState blockstate, boolean flag, int i) {
      if (i >= 8) {
         return false;
      } else {
         int j = blockpos.getX();
         int k = blockpos.getY();
         int l = blockpos.getZ();
         boolean flag1 = true;
         RailShape railshape = blockstate.getValue(SHAPE);
         switch (railshape) {
            case NORTH_SOUTH:
               if (flag) {
                  ++l;
               } else {
                  --l;
               }
               break;
            case EAST_WEST:
               if (flag) {
                  --j;
               } else {
                  ++j;
               }
               break;
            case ASCENDING_EAST:
               if (flag) {
                  --j;
               } else {
                  ++j;
                  ++k;
                  flag1 = false;
               }

               railshape = RailShape.EAST_WEST;
               break;
            case ASCENDING_WEST:
               if (flag) {
                  --j;
                  ++k;
                  flag1 = false;
               } else {
                  ++j;
               }

               railshape = RailShape.EAST_WEST;
               break;
            case ASCENDING_NORTH:
               if (flag) {
                  ++l;
               } else {
                  --l;
                  ++k;
                  flag1 = false;
               }

               railshape = RailShape.NORTH_SOUTH;
               break;
            case ASCENDING_SOUTH:
               if (flag) {
                  ++l;
                  ++k;
                  flag1 = false;
               } else {
                  --l;
               }

               railshape = RailShape.NORTH_SOUTH;
         }

         if (this.isSameRailWithPower(level, new BlockPos(j, k, l), flag, i, railshape)) {
            return true;
         } else {
            return flag1 && this.isSameRailWithPower(level, new BlockPos(j, k - 1, l), flag, i, railshape);
         }
      }
   }

   protected boolean isSameRailWithPower(Level level, BlockPos blockpos, boolean flag, int i, RailShape railshape) {
      BlockState blockstate = level.getBlockState(blockpos);
      if (!blockstate.is(this)) {
         return false;
      } else {
         RailShape railshape1 = blockstate.getValue(SHAPE);
         if (railshape != RailShape.EAST_WEST || railshape1 != RailShape.NORTH_SOUTH && railshape1 != RailShape.ASCENDING_NORTH && railshape1 != RailShape.ASCENDING_SOUTH) {
            if (railshape != RailShape.NORTH_SOUTH || railshape1 != RailShape.EAST_WEST && railshape1 != RailShape.ASCENDING_EAST && railshape1 != RailShape.ASCENDING_WEST) {
               if (blockstate.getValue(POWERED)) {
                  return level.hasNeighborSignal(blockpos) ? true : this.findPoweredRailSignal(level, blockpos, blockstate, flag, i + 1);
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
   }

   protected void updateState(BlockState blockstate, Level level, BlockPos blockpos, Block block) {
      boolean flag = blockstate.getValue(POWERED);
      boolean flag1 = level.hasNeighborSignal(blockpos) || this.findPoweredRailSignal(level, blockpos, blockstate, true, 0) || this.findPoweredRailSignal(level, blockpos, blockstate, false, 0);
      if (flag1 != flag) {
         level.setBlock(blockpos, blockstate.setValue(POWERED, Boolean.valueOf(flag1)), 3);
         level.updateNeighborsAt(blockpos.below(), this);
         if (blockstate.getValue(SHAPE).isAscending()) {
            level.updateNeighborsAt(blockpos.above(), this);
         }
      }

   }

   public Property<RailShape> getShapeProperty() {
      return SHAPE;
   }

   public BlockState rotate(BlockState blockstate, Rotation rotation) {
      switch (rotation) {
         case CLOCKWISE_180:
            switch ((RailShape)blockstate.getValue(SHAPE)) {
               case ASCENDING_EAST:
                  return blockstate.setValue(SHAPE, RailShape.ASCENDING_WEST);
               case ASCENDING_WEST:
                  return blockstate.setValue(SHAPE, RailShape.ASCENDING_EAST);
               case ASCENDING_NORTH:
                  return blockstate.setValue(SHAPE, RailShape.ASCENDING_SOUTH);
               case ASCENDING_SOUTH:
                  return blockstate.setValue(SHAPE, RailShape.ASCENDING_NORTH);
               case SOUTH_EAST:
                  return blockstate.setValue(SHAPE, RailShape.NORTH_WEST);
               case SOUTH_WEST:
                  return blockstate.setValue(SHAPE, RailShape.NORTH_EAST);
               case NORTH_WEST:
                  return blockstate.setValue(SHAPE, RailShape.SOUTH_EAST);
               case NORTH_EAST:
                  return blockstate.setValue(SHAPE, RailShape.SOUTH_WEST);
            }
         case COUNTERCLOCKWISE_90:
            switch ((RailShape)blockstate.getValue(SHAPE)) {
               case NORTH_SOUTH:
                  return blockstate.setValue(SHAPE, RailShape.EAST_WEST);
               case EAST_WEST:
                  return blockstate.setValue(SHAPE, RailShape.NORTH_SOUTH);
               case ASCENDING_EAST:
                  return blockstate.setValue(SHAPE, RailShape.ASCENDING_NORTH);
               case ASCENDING_WEST:
                  return blockstate.setValue(SHAPE, RailShape.ASCENDING_SOUTH);
               case ASCENDING_NORTH:
                  return blockstate.setValue(SHAPE, RailShape.ASCENDING_WEST);
               case ASCENDING_SOUTH:
                  return blockstate.setValue(SHAPE, RailShape.ASCENDING_EAST);
               case SOUTH_EAST:
                  return blockstate.setValue(SHAPE, RailShape.NORTH_EAST);
               case SOUTH_WEST:
                  return blockstate.setValue(SHAPE, RailShape.SOUTH_EAST);
               case NORTH_WEST:
                  return blockstate.setValue(SHAPE, RailShape.SOUTH_WEST);
               case NORTH_EAST:
                  return blockstate.setValue(SHAPE, RailShape.NORTH_WEST);
            }
         case CLOCKWISE_90:
            switch ((RailShape)blockstate.getValue(SHAPE)) {
               case NORTH_SOUTH:
                  return blockstate.setValue(SHAPE, RailShape.EAST_WEST);
               case EAST_WEST:
                  return blockstate.setValue(SHAPE, RailShape.NORTH_SOUTH);
               case ASCENDING_EAST:
                  return blockstate.setValue(SHAPE, RailShape.ASCENDING_SOUTH);
               case ASCENDING_WEST:
                  return blockstate.setValue(SHAPE, RailShape.ASCENDING_NORTH);
               case ASCENDING_NORTH:
                  return blockstate.setValue(SHAPE, RailShape.ASCENDING_EAST);
               case ASCENDING_SOUTH:
                  return blockstate.setValue(SHAPE, RailShape.ASCENDING_WEST);
               case SOUTH_EAST:
                  return blockstate.setValue(SHAPE, RailShape.SOUTH_WEST);
               case SOUTH_WEST:
                  return blockstate.setValue(SHAPE, RailShape.NORTH_WEST);
               case NORTH_WEST:
                  return blockstate.setValue(SHAPE, RailShape.NORTH_EAST);
               case NORTH_EAST:
                  return blockstate.setValue(SHAPE, RailShape.SOUTH_EAST);
            }
         default:
            return blockstate;
      }
   }

   public BlockState mirror(BlockState blockstate, Mirror mirror) {
      RailShape railshape = blockstate.getValue(SHAPE);
      switch (mirror) {
         case LEFT_RIGHT:
            switch (railshape) {
               case ASCENDING_NORTH:
                  return blockstate.setValue(SHAPE, RailShape.ASCENDING_SOUTH);
               case ASCENDING_SOUTH:
                  return blockstate.setValue(SHAPE, RailShape.ASCENDING_NORTH);
               case SOUTH_EAST:
                  return blockstate.setValue(SHAPE, RailShape.NORTH_EAST);
               case SOUTH_WEST:
                  return blockstate.setValue(SHAPE, RailShape.NORTH_WEST);
               case NORTH_WEST:
                  return blockstate.setValue(SHAPE, RailShape.SOUTH_WEST);
               case NORTH_EAST:
                  return blockstate.setValue(SHAPE, RailShape.SOUTH_EAST);
               default:
                  return super.mirror(blockstate, mirror);
            }
         case FRONT_BACK:
            switch (railshape) {
               case ASCENDING_EAST:
                  return blockstate.setValue(SHAPE, RailShape.ASCENDING_WEST);
               case ASCENDING_WEST:
                  return blockstate.setValue(SHAPE, RailShape.ASCENDING_EAST);
               case ASCENDING_NORTH:
               case ASCENDING_SOUTH:
               default:
                  break;
               case SOUTH_EAST:
                  return blockstate.setValue(SHAPE, RailShape.SOUTH_WEST);
               case SOUTH_WEST:
                  return blockstate.setValue(SHAPE, RailShape.SOUTH_EAST);
               case NORTH_WEST:
                  return blockstate.setValue(SHAPE, RailShape.NORTH_EAST);
               case NORTH_EAST:
                  return blockstate.setValue(SHAPE, RailShape.NORTH_WEST);
            }
      }

      return super.mirror(blockstate, mirror);
   }

   protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> statedefinition_builder) {
      statedefinition_builder.add(SHAPE, POWERED, WATERLOGGED);
   }
}
