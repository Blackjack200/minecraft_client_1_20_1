package net.minecraft.world.level;

import java.util.Optional;
import net.minecraft.core.BlockPos;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.material.FluidState;

public class EntityBasedExplosionDamageCalculator extends ExplosionDamageCalculator {
   private final Entity source;

   public EntityBasedExplosionDamageCalculator(Entity entity) {
      this.source = entity;
   }

   public Optional<Float> getBlockExplosionResistance(Explosion explosion, BlockGetter blockgetter, BlockPos blockpos, BlockState blockstate, FluidState fluidstate) {
      return super.getBlockExplosionResistance(explosion, blockgetter, blockpos, blockstate, fluidstate).map((ofloat) -> this.source.getBlockExplosionResistance(explosion, blockgetter, blockpos, blockstate, fluidstate, ofloat));
   }

   public boolean shouldBlockExplode(Explosion explosion, BlockGetter blockgetter, BlockPos blockpos, BlockState blockstate, float f) {
      return this.source.shouldBlockExplode(explosion, blockgetter, blockpos, blockstate, f);
   }
}
