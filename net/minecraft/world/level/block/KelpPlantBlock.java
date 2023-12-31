package net.minecraft.world.level.block;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.material.Fluid;
import net.minecraft.world.level.material.FluidState;
import net.minecraft.world.level.material.Fluids;
import net.minecraft.world.phys.shapes.Shapes;

public class KelpPlantBlock extends GrowingPlantBodyBlock implements LiquidBlockContainer {
   protected KelpPlantBlock(BlockBehaviour.Properties blockbehaviour_properties) {
      super(blockbehaviour_properties, Direction.UP, Shapes.block(), true);
   }

   protected GrowingPlantHeadBlock getHeadBlock() {
      return (GrowingPlantHeadBlock)Blocks.KELP;
   }

   public FluidState getFluidState(BlockState blockstate) {
      return Fluids.WATER.getSource(false);
   }

   protected boolean canAttachTo(BlockState blockstate) {
      return this.getHeadBlock().canAttachTo(blockstate);
   }

   public boolean canPlaceLiquid(BlockGetter blockgetter, BlockPos blockpos, BlockState blockstate, Fluid fluid) {
      return false;
   }

   public boolean placeLiquid(LevelAccessor levelaccessor, BlockPos blockpos, BlockState blockstate, FluidState fluidstate) {
      return false;
   }
}
