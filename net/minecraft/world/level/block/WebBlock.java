package net.minecraft.world.level.block;

import net.minecraft.core.BlockPos;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.Vec3;

public class WebBlock extends Block {
   public WebBlock(BlockBehaviour.Properties blockbehaviour_properties) {
      super(blockbehaviour_properties);
   }

   public void entityInside(BlockState blockstate, Level level, BlockPos blockpos, Entity entity) {
      entity.makeStuckInBlock(blockstate, new Vec3(0.25D, (double)0.05F, 0.25D));
   }
}
