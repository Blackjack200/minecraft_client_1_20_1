package net.minecraft.world.level.block;

import net.minecraft.core.BlockPos;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.BlockState;

public class SandBlock extends FallingBlock {
   private final int dustColor;

   public SandBlock(int i, BlockBehaviour.Properties blockbehaviour_properties) {
      super(blockbehaviour_properties);
      this.dustColor = i;
   }

   public int getDustColor(BlockState blockstate, BlockGetter blockgetter, BlockPos blockpos) {
      return this.dustColor;
   }
}
