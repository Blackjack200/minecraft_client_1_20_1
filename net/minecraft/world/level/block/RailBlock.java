package net.minecraft.world.level.block;

import net.minecraft.core.BlockPos;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.block.state.properties.EnumProperty;
import net.minecraft.world.level.block.state.properties.Property;
import net.minecraft.world.level.block.state.properties.RailShape;

public class RailBlock extends BaseRailBlock {
   public static final EnumProperty<RailShape> SHAPE = BlockStateProperties.RAIL_SHAPE;

   protected RailBlock(BlockBehaviour.Properties blockbehaviour_properties) {
      super(false, blockbehaviour_properties);
      this.registerDefaultState(this.stateDefinition.any().setValue(SHAPE, RailShape.NORTH_SOUTH).setValue(WATERLOGGED, Boolean.valueOf(false)));
   }

   protected void updateState(BlockState blockstate, Level level, BlockPos blockpos, Block block) {
      if (block.defaultBlockState().isSignalSource() && (new RailState(level, blockpos, blockstate)).countPotentialConnections() == 3) {
         this.updateDir(level, blockpos, blockstate, false);
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
               case NORTH_SOUTH:
                  return blockstate.setValue(SHAPE, RailShape.EAST_WEST);
               case EAST_WEST:
                  return blockstate.setValue(SHAPE, RailShape.NORTH_SOUTH);
            }
         case CLOCKWISE_90:
            switch ((RailShape)blockstate.getValue(SHAPE)) {
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
               case NORTH_SOUTH:
                  return blockstate.setValue(SHAPE, RailShape.EAST_WEST);
               case EAST_WEST:
                  return blockstate.setValue(SHAPE, RailShape.NORTH_SOUTH);
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
      statedefinition_builder.add(SHAPE, WATERLOGGED);
   }
}
