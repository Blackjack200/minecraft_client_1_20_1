package net.minecraft.world.level.block;

import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.util.RandomSource;
import net.minecraft.world.Difficulty;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.Vec3;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.VoxelShape;

public class WitherRoseBlock extends FlowerBlock {
   public WitherRoseBlock(MobEffect mobeffect, BlockBehaviour.Properties blockbehaviour_properties) {
      super(mobeffect, 8, blockbehaviour_properties);
   }

   protected boolean mayPlaceOn(BlockState blockstate, BlockGetter blockgetter, BlockPos blockpos) {
      return super.mayPlaceOn(blockstate, blockgetter, blockpos) || blockstate.is(Blocks.NETHERRACK) || blockstate.is(Blocks.SOUL_SAND) || blockstate.is(Blocks.SOUL_SOIL);
   }

   public void animateTick(BlockState blockstate, Level level, BlockPos blockpos, RandomSource randomsource) {
      VoxelShape voxelshape = this.getShape(blockstate, level, blockpos, CollisionContext.empty());
      Vec3 vec3 = voxelshape.bounds().getCenter();
      double d0 = (double)blockpos.getX() + vec3.x;
      double d1 = (double)blockpos.getZ() + vec3.z;

      for(int i = 0; i < 3; ++i) {
         if (randomsource.nextBoolean()) {
            level.addParticle(ParticleTypes.SMOKE, d0 + randomsource.nextDouble() / 5.0D, (double)blockpos.getY() + (0.5D - randomsource.nextDouble()), d1 + randomsource.nextDouble() / 5.0D, 0.0D, 0.0D, 0.0D);
         }
      }

   }

   public void entityInside(BlockState blockstate, Level level, BlockPos blockpos, Entity entity) {
      if (!level.isClientSide && level.getDifficulty() != Difficulty.PEACEFUL) {
         if (entity instanceof LivingEntity) {
            LivingEntity livingentity = (LivingEntity)entity;
            if (!livingentity.isInvulnerableTo(level.damageSources().wither())) {
               livingentity.addEffect(new MobEffectInstance(MobEffects.WITHER, 40));
            }
         }

      }
   }
}
