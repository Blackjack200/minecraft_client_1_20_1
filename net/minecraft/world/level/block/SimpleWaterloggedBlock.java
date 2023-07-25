package net.minecraft.world.level.block;

import java.util.Optional;
import net.minecraft.core.BlockPos;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.material.Fluid;
import net.minecraft.world.level.material.FluidState;
import net.minecraft.world.level.material.Fluids;

public interface SimpleWaterloggedBlock extends BucketPickup, LiquidBlockContainer {
   default boolean canPlaceLiquid(BlockGetter blockgetter, BlockPos blockpos, BlockState blockstate, Fluid fluid) {
      return fluid == Fluids.WATER;
   }

   default boolean placeLiquid(LevelAccessor levelaccessor, BlockPos blockpos, BlockState blockstate, FluidState fluidstate) {
      if (!blockstate.getValue(BlockStateProperties.WATERLOGGED) && fluidstate.getType() == Fluids.WATER) {
         if (!levelaccessor.isClientSide()) {
            levelaccessor.setBlock(blockpos, blockstate.setValue(BlockStateProperties.WATERLOGGED, Boolean.valueOf(true)), 3);
            levelaccessor.scheduleTick(blockpos, fluidstate.getType(), fluidstate.getType().getTickDelay(levelaccessor));
         }

         return true;
      } else {
         return false;
      }
   }

   default ItemStack pickupBlock(LevelAccessor levelaccessor, BlockPos blockpos, BlockState blockstate) {
      if (blockstate.getValue(BlockStateProperties.WATERLOGGED)) {
         levelaccessor.setBlock(blockpos, blockstate.setValue(BlockStateProperties.WATERLOGGED, Boolean.valueOf(false)), 3);
         if (!blockstate.canSurvive(levelaccessor, blockpos)) {
            levelaccessor.destroyBlock(blockpos, true);
         }

         return new ItemStack(Items.WATER_BUCKET);
      } else {
         return ItemStack.EMPTY;
      }
   }

   default Optional<SoundEvent> getPickupSound() {
      return Fluids.WATER.getPickupSound();
   }
}
