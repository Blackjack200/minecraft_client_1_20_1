package net.minecraft.world.level.block;

import net.minecraft.core.BlockPos;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.Vec3;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.VoxelShape;

public class FlowerBlock extends BushBlock implements SuspiciousEffectHolder {
   protected static final float AABB_OFFSET = 3.0F;
   protected static final VoxelShape SHAPE = Block.box(5.0D, 0.0D, 5.0D, 11.0D, 10.0D, 11.0D);
   private final MobEffect suspiciousStewEffect;
   private final int effectDuration;

   public FlowerBlock(MobEffect mobeffect, int i, BlockBehaviour.Properties blockbehaviour_properties) {
      super(blockbehaviour_properties);
      this.suspiciousStewEffect = mobeffect;
      if (mobeffect.isInstantenous()) {
         this.effectDuration = i;
      } else {
         this.effectDuration = i * 20;
      }

   }

   public VoxelShape getShape(BlockState blockstate, BlockGetter blockgetter, BlockPos blockpos, CollisionContext collisioncontext) {
      Vec3 vec3 = blockstate.getOffset(blockgetter, blockpos);
      return SHAPE.move(vec3.x, vec3.y, vec3.z);
   }

   public MobEffect getSuspiciousEffect() {
      return this.suspiciousStewEffect;
   }

   public int getEffectDuration() {
      return this.effectDuration;
   }
}
