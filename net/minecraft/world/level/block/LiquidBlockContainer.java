package net.minecraft.world.level.block;

import net.minecraft.core.BlockPos;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.material.Fluid;
import net.minecraft.world.level.material.FluidState;

public interface LiquidBlockContainer {
   boolean canPlaceLiquid(BlockGetter blockgetter, BlockPos blockpos, BlockState blockstate, Fluid fluid);

   boolean placeLiquid(LevelAccessor levelaccessor, BlockPos blockpos, BlockState blockstate, FluidState fluidstate);
}
