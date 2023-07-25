package net.minecraft.world.level.block;

import net.minecraft.core.Direction;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;

public class InfestedRotatedPillarBlock extends InfestedBlock {
   public InfestedRotatedPillarBlock(Block block, BlockBehaviour.Properties blockbehaviour_properties) {
      super(block, blockbehaviour_properties);
      this.registerDefaultState(this.defaultBlockState().setValue(RotatedPillarBlock.AXIS, Direction.Axis.Y));
   }

   public BlockState rotate(BlockState blockstate, Rotation rotation) {
      return RotatedPillarBlock.rotatePillar(blockstate, rotation);
   }

   protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> statedefinition_builder) {
      statedefinition_builder.add(RotatedPillarBlock.AXIS);
   }

   public BlockState getStateForPlacement(BlockPlaceContext blockplacecontext) {
      return this.defaultBlockState().setValue(RotatedPillarBlock.AXIS, blockplacecontext.getClickedFace().getAxis());
   }
}
