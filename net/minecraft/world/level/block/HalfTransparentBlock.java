package net.minecraft.world.level.block;

import net.minecraft.core.Direction;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.BlockState;

public class HalfTransparentBlock extends Block {
   protected HalfTransparentBlock(BlockBehaviour.Properties blockbehaviour_properties) {
      super(blockbehaviour_properties);
   }

   public boolean skipRendering(BlockState blockstate, BlockState blockstate1, Direction direction) {
      return blockstate1.is(this) ? true : super.skipRendering(blockstate, blockstate1, direction);
   }
}
