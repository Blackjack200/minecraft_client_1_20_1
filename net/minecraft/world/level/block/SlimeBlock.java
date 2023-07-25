package net.minecraft.world.level.block;

import net.minecraft.core.BlockPos;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.Vec3;

public class SlimeBlock extends HalfTransparentBlock {
   public SlimeBlock(BlockBehaviour.Properties blockbehaviour_properties) {
      super(blockbehaviour_properties);
   }

   public void fallOn(Level level, BlockState blockstate, BlockPos blockpos, Entity entity, float f) {
      if (entity.isSuppressingBounce()) {
         super.fallOn(level, blockstate, blockpos, entity, f);
      } else {
         entity.causeFallDamage(f, 0.0F, level.damageSources().fall());
      }

   }

   public void updateEntityAfterFallOn(BlockGetter blockgetter, Entity entity) {
      if (entity.isSuppressingBounce()) {
         super.updateEntityAfterFallOn(blockgetter, entity);
      } else {
         this.bounceUp(entity);
      }

   }

   private void bounceUp(Entity entity) {
      Vec3 vec3 = entity.getDeltaMovement();
      if (vec3.y < 0.0D) {
         double d0 = entity instanceof LivingEntity ? 1.0D : 0.8D;
         entity.setDeltaMovement(vec3.x, -vec3.y * d0, vec3.z);
      }

   }

   public void stepOn(Level level, BlockPos blockpos, BlockState blockstate, Entity entity) {
      double d0 = Math.abs(entity.getDeltaMovement().y);
      if (d0 < 0.1D && !entity.isSteppingCarefully()) {
         double d1 = 0.4D + d0 * 0.2D;
         entity.setDeltaMovement(entity.getDeltaMovement().multiply(d1, 1.0D, d1));
      }

      super.stepOn(level, blockpos, blockstate, entity);
   }
}
