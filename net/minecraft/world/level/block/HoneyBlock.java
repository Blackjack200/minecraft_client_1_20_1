package net.minecraft.world.level.block;

import net.minecraft.advancements.CriteriaTriggers;
import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.BlockParticleOption;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.item.PrimedTnt;
import net.minecraft.world.entity.vehicle.AbstractMinecart;
import net.minecraft.world.entity.vehicle.Boat;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.Vec3;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.VoxelShape;

public class HoneyBlock extends HalfTransparentBlock {
   private static final double SLIDE_STARTS_WHEN_VERTICAL_SPEED_IS_AT_LEAST = 0.13D;
   private static final double MIN_FALL_SPEED_TO_BE_CONSIDERED_SLIDING = 0.08D;
   private static final double THROTTLE_SLIDE_SPEED_TO = 0.05D;
   private static final int SLIDE_ADVANCEMENT_CHECK_INTERVAL = 20;
   protected static final VoxelShape SHAPE = Block.box(1.0D, 0.0D, 1.0D, 15.0D, 15.0D, 15.0D);

   public HoneyBlock(BlockBehaviour.Properties blockbehaviour_properties) {
      super(blockbehaviour_properties);
   }

   private static boolean doesEntityDoHoneyBlockSlideEffects(Entity entity) {
      return entity instanceof LivingEntity || entity instanceof AbstractMinecart || entity instanceof PrimedTnt || entity instanceof Boat;
   }

   public VoxelShape getCollisionShape(BlockState blockstate, BlockGetter blockgetter, BlockPos blockpos, CollisionContext collisioncontext) {
      return SHAPE;
   }

   public void fallOn(Level level, BlockState blockstate, BlockPos blockpos, Entity entity, float f) {
      entity.playSound(SoundEvents.HONEY_BLOCK_SLIDE, 1.0F, 1.0F);
      if (!level.isClientSide) {
         level.broadcastEntityEvent(entity, (byte)54);
      }

      if (entity.causeFallDamage(f, 0.2F, level.damageSources().fall())) {
         entity.playSound(this.soundType.getFallSound(), this.soundType.getVolume() * 0.5F, this.soundType.getPitch() * 0.75F);
      }

   }

   public void entityInside(BlockState blockstate, Level level, BlockPos blockpos, Entity entity) {
      if (this.isSlidingDown(blockpos, entity)) {
         this.maybeDoSlideAchievement(entity, blockpos);
         this.doSlideMovement(entity);
         this.maybeDoSlideEffects(level, entity);
      }

      super.entityInside(blockstate, level, blockpos, entity);
   }

   private boolean isSlidingDown(BlockPos blockpos, Entity entity) {
      if (entity.onGround()) {
         return false;
      } else if (entity.getY() > (double)blockpos.getY() + 0.9375D - 1.0E-7D) {
         return false;
      } else if (entity.getDeltaMovement().y >= -0.08D) {
         return false;
      } else {
         double d0 = Math.abs((double)blockpos.getX() + 0.5D - entity.getX());
         double d1 = Math.abs((double)blockpos.getZ() + 0.5D - entity.getZ());
         double d2 = 0.4375D + (double)(entity.getBbWidth() / 2.0F);
         return d0 + 1.0E-7D > d2 || d1 + 1.0E-7D > d2;
      }
   }

   private void maybeDoSlideAchievement(Entity entity, BlockPos blockpos) {
      if (entity instanceof ServerPlayer && entity.level().getGameTime() % 20L == 0L) {
         CriteriaTriggers.HONEY_BLOCK_SLIDE.trigger((ServerPlayer)entity, entity.level().getBlockState(blockpos));
      }

   }

   private void doSlideMovement(Entity entity) {
      Vec3 vec3 = entity.getDeltaMovement();
      if (vec3.y < -0.13D) {
         double d0 = -0.05D / vec3.y;
         entity.setDeltaMovement(new Vec3(vec3.x * d0, -0.05D, vec3.z * d0));
      } else {
         entity.setDeltaMovement(new Vec3(vec3.x, -0.05D, vec3.z));
      }

      entity.resetFallDistance();
   }

   private void maybeDoSlideEffects(Level level, Entity entity) {
      if (doesEntityDoHoneyBlockSlideEffects(entity)) {
         if (level.random.nextInt(5) == 0) {
            entity.playSound(SoundEvents.HONEY_BLOCK_SLIDE, 1.0F, 1.0F);
         }

         if (!level.isClientSide && level.random.nextInt(5) == 0) {
            level.broadcastEntityEvent(entity, (byte)53);
         }
      }

   }

   public static void showSlideParticles(Entity entity) {
      showParticles(entity, 5);
   }

   public static void showJumpParticles(Entity entity) {
      showParticles(entity, 10);
   }

   private static void showParticles(Entity entity, int i) {
      if (entity.level().isClientSide) {
         BlockState blockstate = Blocks.HONEY_BLOCK.defaultBlockState();

         for(int j = 0; j < i; ++j) {
            entity.level().addParticle(new BlockParticleOption(ParticleTypes.BLOCK, blockstate), entity.getX(), entity.getY(), entity.getZ(), 0.0D, 0.0D, 0.0D);
         }

      }
   }
}
