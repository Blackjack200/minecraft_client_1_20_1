package net.minecraft.world.level;

import java.util.Optional;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.material.FluidState;

public class ExplosionDamageCalculator {
   public Optional<Float> getBlockExplosionResistance(Explosion explosion, BlockGetter blockgetter, BlockPos blockpos, BlockState blockstate, FluidState fluidstate) {
      return blockstate.isAir() && fluidstate.isEmpty() ? Optional.empty() : Optional.of(Math.max(blockstate.getBlock().getExplosionResistance(), fluidstate.getExplosionResistance()));
   }

   public boolean shouldBlockExplode(Explosion explosion, BlockGetter blockgetter, BlockPos blockpos, BlockState blockstate, float f) {
      return true;
   }
}
