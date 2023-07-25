package net.minecraft.world.level.block;

import net.minecraft.core.BlockPos;
import net.minecraft.core.cauldron.CauldronInteraction;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.BlockState;

public class LavaCauldronBlock extends AbstractCauldronBlock {
   public LavaCauldronBlock(BlockBehaviour.Properties blockbehaviour_properties) {
      super(blockbehaviour_properties, CauldronInteraction.LAVA);
   }

   protected double getContentHeight(BlockState blockstate) {
      return 0.9375D;
   }

   public boolean isFull(BlockState blockstate) {
      return true;
   }

   public void entityInside(BlockState blockstate, Level level, BlockPos blockpos, Entity entity) {
      if (this.isEntityInsideContent(blockstate, blockpos, entity)) {
         entity.lavaHurt();
      }

   }

   public int getAnalogOutputSignal(BlockState blockstate, Level level, BlockPos blockpos) {
      return 3;
   }
}
