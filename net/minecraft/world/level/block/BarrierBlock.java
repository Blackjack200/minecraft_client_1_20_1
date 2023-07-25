package net.minecraft.world.level.block;

import net.minecraft.core.BlockPos;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.BlockState;

public class BarrierBlock extends Block {
   protected BarrierBlock(BlockBehaviour.Properties blockbehaviour_properties) {
      super(blockbehaviour_properties);
   }

   public boolean propagatesSkylightDown(BlockState blockstate, BlockGetter blockgetter, BlockPos blockpos) {
      return true;
   }

   public RenderShape getRenderShape(BlockState blockstate) {
      return RenderShape.INVISIBLE;
   }

   public float getShadeBrightness(BlockState blockstate, BlockGetter blockgetter, BlockPos blockpos) {
      return 1.0F;
   }
}
