package net.minecraft.world.level.block;

import net.minecraft.core.Direction;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.block.state.properties.EnumProperty;

public class RotatedPillarBlock extends Block {
   public static final EnumProperty<Direction.Axis> AXIS = BlockStateProperties.AXIS;

   public RotatedPillarBlock(BlockBehaviour.Properties blockbehaviour_properties) {
      super(blockbehaviour_properties);
      this.registerDefaultState(this.defaultBlockState().setValue(AXIS, Direction.Axis.Y));
   }

   public BlockState rotate(BlockState blockstate, Rotation rotation) {
      return rotatePillar(blockstate, rotation);
   }

   public static BlockState rotatePillar(BlockState blockstate, Rotation rotation) {
      switch (rotation) {
         case COUNTERCLOCKWISE_90:
         case CLOCKWISE_90:
            switch ((Direction.Axis)blockstate.getValue(AXIS)) {
               case X:
                  return blockstate.setValue(AXIS, Direction.Axis.Z);
               case Z:
                  return blockstate.setValue(AXIS, Direction.Axis.X);
               default:
                  return blockstate;
            }
         default:
            return blockstate;
      }
   }

   protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> statedefinition_builder) {
      statedefinition_builder.add(AXIS);
   }

   public BlockState getStateForPlacement(BlockPlaceContext blockplacecontext) {
      return this.defaultBlockState().setValue(AXIS, blockplacecontext.getClickedFace().getAxis());
   }
}
