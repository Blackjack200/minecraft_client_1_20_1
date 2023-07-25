package net.minecraft.world.level.block;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.BlockState;

public class HayBlock extends RotatedPillarBlock {
   public HayBlock(BlockBehaviour.Properties blockbehaviour_properties) {
      super(blockbehaviour_properties);
      this.registerDefaultState(this.stateDefinition.any().setValue(AXIS, Direction.Axis.Y));
   }

   public void fallOn(Level level, BlockState blockstate, BlockPos blockpos, Entity entity, float f) {
      entity.causeFallDamage(f, 0.2F, level.damageSources().fall());
   }
}
