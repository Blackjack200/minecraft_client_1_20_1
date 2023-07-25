package net.minecraft.world.entity.projectile;

import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.TheEndGatewayBlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.Vec3;

public abstract class ThrowableProjectile extends Projectile {
   protected ThrowableProjectile(EntityType<? extends ThrowableProjectile> entitytype, Level level) {
      super(entitytype, level);
   }

   protected ThrowableProjectile(EntityType<? extends ThrowableProjectile> entitytype, double d0, double d1, double d2, Level level) {
      this(entitytype, level);
      this.setPos(d0, d1, d2);
   }

   protected ThrowableProjectile(EntityType<? extends ThrowableProjectile> entitytype, LivingEntity livingentity, Level level) {
      this(entitytype, livingentity.getX(), livingentity.getEyeY() - (double)0.1F, livingentity.getZ(), level);
      this.setOwner(livingentity);
   }

   public boolean shouldRenderAtSqrDistance(double d0) {
      double d1 = this.getBoundingBox().getSize() * 4.0D;
      if (Double.isNaN(d1)) {
         d1 = 4.0D;
      }

      d1 *= 64.0D;
      return d0 < d1 * d1;
   }

   public void tick() {
      super.tick();
      HitResult hitresult = ProjectileUtil.getHitResultOnMoveVector(this, this::canHitEntity);
      boolean flag = false;
      if (hitresult.getType() == HitResult.Type.BLOCK) {
         BlockPos blockpos = ((BlockHitResult)hitresult).getBlockPos();
         BlockState blockstate = this.level().getBlockState(blockpos);
         if (blockstate.is(Blocks.NETHER_PORTAL)) {
            this.handleInsidePortal(blockpos);
            flag = true;
         } else if (blockstate.is(Blocks.END_GATEWAY)) {
            BlockEntity blockentity = this.level().getBlockEntity(blockpos);
            if (blockentity instanceof TheEndGatewayBlockEntity && TheEndGatewayBlockEntity.canEntityTeleport(this)) {
               TheEndGatewayBlockEntity.teleportEntity(this.level(), blockpos, blockstate, this, (TheEndGatewayBlockEntity)blockentity);
            }

            flag = true;
         }
      }

      if (hitresult.getType() != HitResult.Type.MISS && !flag) {
         this.onHit(hitresult);
      }

      this.checkInsideBlocks();
      Vec3 vec3 = this.getDeltaMovement();
      double d0 = this.getX() + vec3.x;
      double d1 = this.getY() + vec3.y;
      double d2 = this.getZ() + vec3.z;
      this.updateRotation();
      float f1;
      if (this.isInWater()) {
         for(int i = 0; i < 4; ++i) {
            float f = 0.25F;
            this.level().addParticle(ParticleTypes.BUBBLE, d0 - vec3.x * 0.25D, d1 - vec3.y * 0.25D, d2 - vec3.z * 0.25D, vec3.x, vec3.y, vec3.z);
         }

         f1 = 0.8F;
      } else {
         f1 = 0.99F;
      }

      this.setDeltaMovement(vec3.scale((double)f1));
      if (!this.isNoGravity()) {
         Vec3 vec31 = this.getDeltaMovement();
         this.setDeltaMovement(vec31.x, vec31.y - (double)this.getGravity(), vec31.z);
      }

      this.setPos(d0, d1, d2);
   }

   protected float getGravity() {
      return 0.03F;
   }
}
