package net.minecraft.world.entity.projectile;

import com.google.common.base.MoreObjects;
import com.google.common.collect.Lists;
import java.util.List;
import java.util.UUID;
import javax.annotation.Nullable;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.protocol.game.ClientboundAddEntityPacket;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.util.Mth;
import net.minecraft.world.Difficulty;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.gameevent.GameEvent;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.EntityHitResult;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.Vec3;

public class ShulkerBullet extends Projectile {
   private static final double SPEED = 0.15D;
   @Nullable
   private Entity finalTarget;
   @Nullable
   private Direction currentMoveDirection;
   private int flightSteps;
   private double targetDeltaX;
   private double targetDeltaY;
   private double targetDeltaZ;
   @Nullable
   private UUID targetId;

   public ShulkerBullet(EntityType<? extends ShulkerBullet> entitytype, Level level) {
      super(entitytype, level);
      this.noPhysics = true;
   }

   public ShulkerBullet(Level level, LivingEntity livingentity, Entity entity, Direction.Axis direction_axis) {
      this(EntityType.SHULKER_BULLET, level);
      this.setOwner(livingentity);
      BlockPos blockpos = livingentity.blockPosition();
      double d0 = (double)blockpos.getX() + 0.5D;
      double d1 = (double)blockpos.getY() + 0.5D;
      double d2 = (double)blockpos.getZ() + 0.5D;
      this.moveTo(d0, d1, d2, this.getYRot(), this.getXRot());
      this.finalTarget = entity;
      this.currentMoveDirection = Direction.UP;
      this.selectNextMoveDirection(direction_axis);
   }

   public SoundSource getSoundSource() {
      return SoundSource.HOSTILE;
   }

   protected void addAdditionalSaveData(CompoundTag compoundtag) {
      super.addAdditionalSaveData(compoundtag);
      if (this.finalTarget != null) {
         compoundtag.putUUID("Target", this.finalTarget.getUUID());
      }

      if (this.currentMoveDirection != null) {
         compoundtag.putInt("Dir", this.currentMoveDirection.get3DDataValue());
      }

      compoundtag.putInt("Steps", this.flightSteps);
      compoundtag.putDouble("TXD", this.targetDeltaX);
      compoundtag.putDouble("TYD", this.targetDeltaY);
      compoundtag.putDouble("TZD", this.targetDeltaZ);
   }

   protected void readAdditionalSaveData(CompoundTag compoundtag) {
      super.readAdditionalSaveData(compoundtag);
      this.flightSteps = compoundtag.getInt("Steps");
      this.targetDeltaX = compoundtag.getDouble("TXD");
      this.targetDeltaY = compoundtag.getDouble("TYD");
      this.targetDeltaZ = compoundtag.getDouble("TZD");
      if (compoundtag.contains("Dir", 99)) {
         this.currentMoveDirection = Direction.from3DDataValue(compoundtag.getInt("Dir"));
      }

      if (compoundtag.hasUUID("Target")) {
         this.targetId = compoundtag.getUUID("Target");
      }

   }

   protected void defineSynchedData() {
   }

   @Nullable
   private Direction getMoveDirection() {
      return this.currentMoveDirection;
   }

   private void setMoveDirection(@Nullable Direction direction) {
      this.currentMoveDirection = direction;
   }

   private void selectNextMoveDirection(@Nullable Direction.Axis direction_axis) {
      double d0 = 0.5D;
      BlockPos blockpos;
      if (this.finalTarget == null) {
         blockpos = this.blockPosition().below();
      } else {
         d0 = (double)this.finalTarget.getBbHeight() * 0.5D;
         blockpos = BlockPos.containing(this.finalTarget.getX(), this.finalTarget.getY() + d0, this.finalTarget.getZ());
      }

      double d1 = (double)blockpos.getX() + 0.5D;
      double d2 = (double)blockpos.getY() + d0;
      double d3 = (double)blockpos.getZ() + 0.5D;
      Direction direction = null;
      if (!blockpos.closerToCenterThan(this.position(), 2.0D)) {
         BlockPos blockpos2 = this.blockPosition();
         List<Direction> list = Lists.newArrayList();
         if (direction_axis != Direction.Axis.X) {
            if (blockpos2.getX() < blockpos.getX() && this.level().isEmptyBlock(blockpos2.east())) {
               list.add(Direction.EAST);
            } else if (blockpos2.getX() > blockpos.getX() && this.level().isEmptyBlock(blockpos2.west())) {
               list.add(Direction.WEST);
            }
         }

         if (direction_axis != Direction.Axis.Y) {
            if (blockpos2.getY() < blockpos.getY() && this.level().isEmptyBlock(blockpos2.above())) {
               list.add(Direction.UP);
            } else if (blockpos2.getY() > blockpos.getY() && this.level().isEmptyBlock(blockpos2.below())) {
               list.add(Direction.DOWN);
            }
         }

         if (direction_axis != Direction.Axis.Z) {
            if (blockpos2.getZ() < blockpos.getZ() && this.level().isEmptyBlock(blockpos2.south())) {
               list.add(Direction.SOUTH);
            } else if (blockpos2.getZ() > blockpos.getZ() && this.level().isEmptyBlock(blockpos2.north())) {
               list.add(Direction.NORTH);
            }
         }

         direction = Direction.getRandom(this.random);
         if (list.isEmpty()) {
            for(int i = 5; !this.level().isEmptyBlock(blockpos2.relative(direction)) && i > 0; --i) {
               direction = Direction.getRandom(this.random);
            }
         } else {
            direction = list.get(this.random.nextInt(list.size()));
         }

         d1 = this.getX() + (double)direction.getStepX();
         d2 = this.getY() + (double)direction.getStepY();
         d3 = this.getZ() + (double)direction.getStepZ();
      }

      this.setMoveDirection(direction);
      double d4 = d1 - this.getX();
      double d5 = d2 - this.getY();
      double d6 = d3 - this.getZ();
      double d7 = Math.sqrt(d4 * d4 + d5 * d5 + d6 * d6);
      if (d7 == 0.0D) {
         this.targetDeltaX = 0.0D;
         this.targetDeltaY = 0.0D;
         this.targetDeltaZ = 0.0D;
      } else {
         this.targetDeltaX = d4 / d7 * 0.15D;
         this.targetDeltaY = d5 / d7 * 0.15D;
         this.targetDeltaZ = d6 / d7 * 0.15D;
      }

      this.hasImpulse = true;
      this.flightSteps = 10 + this.random.nextInt(5) * 10;
   }

   public void checkDespawn() {
      if (this.level().getDifficulty() == Difficulty.PEACEFUL) {
         this.discard();
      }

   }

   public void tick() {
      super.tick();
      if (!this.level().isClientSide) {
         if (this.finalTarget == null && this.targetId != null) {
            this.finalTarget = ((ServerLevel)this.level()).getEntity(this.targetId);
            if (this.finalTarget == null) {
               this.targetId = null;
            }
         }

         if (this.finalTarget == null || !this.finalTarget.isAlive() || this.finalTarget instanceof Player && this.finalTarget.isSpectator()) {
            if (!this.isNoGravity()) {
               this.setDeltaMovement(this.getDeltaMovement().add(0.0D, -0.04D, 0.0D));
            }
         } else {
            this.targetDeltaX = Mth.clamp(this.targetDeltaX * 1.025D, -1.0D, 1.0D);
            this.targetDeltaY = Mth.clamp(this.targetDeltaY * 1.025D, -1.0D, 1.0D);
            this.targetDeltaZ = Mth.clamp(this.targetDeltaZ * 1.025D, -1.0D, 1.0D);
            Vec3 vec3 = this.getDeltaMovement();
            this.setDeltaMovement(vec3.add((this.targetDeltaX - vec3.x) * 0.2D, (this.targetDeltaY - vec3.y) * 0.2D, (this.targetDeltaZ - vec3.z) * 0.2D));
         }

         HitResult hitresult = ProjectileUtil.getHitResultOnMoveVector(this, this::canHitEntity);
         if (hitresult.getType() != HitResult.Type.MISS) {
            this.onHit(hitresult);
         }
      }

      this.checkInsideBlocks();
      Vec3 vec31 = this.getDeltaMovement();
      this.setPos(this.getX() + vec31.x, this.getY() + vec31.y, this.getZ() + vec31.z);
      ProjectileUtil.rotateTowardsMovement(this, 0.5F);
      if (this.level().isClientSide) {
         this.level().addParticle(ParticleTypes.END_ROD, this.getX() - vec31.x, this.getY() - vec31.y + 0.15D, this.getZ() - vec31.z, 0.0D, 0.0D, 0.0D);
      } else if (this.finalTarget != null && !this.finalTarget.isRemoved()) {
         if (this.flightSteps > 0) {
            --this.flightSteps;
            if (this.flightSteps == 0) {
               this.selectNextMoveDirection(this.currentMoveDirection == null ? null : this.currentMoveDirection.getAxis());
            }
         }

         if (this.currentMoveDirection != null) {
            BlockPos blockpos = this.blockPosition();
            Direction.Axis direction_axis = this.currentMoveDirection.getAxis();
            if (this.level().loadedAndEntityCanStandOn(blockpos.relative(this.currentMoveDirection), this)) {
               this.selectNextMoveDirection(direction_axis);
            } else {
               BlockPos blockpos1 = this.finalTarget.blockPosition();
               if (direction_axis == Direction.Axis.X && blockpos.getX() == blockpos1.getX() || direction_axis == Direction.Axis.Z && blockpos.getZ() == blockpos1.getZ() || direction_axis == Direction.Axis.Y && blockpos.getY() == blockpos1.getY()) {
                  this.selectNextMoveDirection(direction_axis);
               }
            }
         }
      }

   }

   protected boolean canHitEntity(Entity entity) {
      return super.canHitEntity(entity) && !entity.noPhysics;
   }

   public boolean isOnFire() {
      return false;
   }

   public boolean shouldRenderAtSqrDistance(double d0) {
      return d0 < 16384.0D;
   }

   public float getLightLevelDependentMagicValue() {
      return 1.0F;
   }

   protected void onHitEntity(EntityHitResult entityhitresult) {
      super.onHitEntity(entityhitresult);
      Entity entity = entityhitresult.getEntity();
      Entity entity1 = this.getOwner();
      LivingEntity livingentity = entity1 instanceof LivingEntity ? (LivingEntity)entity1 : null;
      boolean flag = entity.hurt(this.damageSources().mobProjectile(this, livingentity), 4.0F);
      if (flag) {
         this.doEnchantDamageEffects(livingentity, entity);
         if (entity instanceof LivingEntity) {
            LivingEntity livingentity1 = (LivingEntity)entity;
            livingentity1.addEffect(new MobEffectInstance(MobEffects.LEVITATION, 200), MoreObjects.firstNonNull(entity1, this));
         }
      }

   }

   protected void onHitBlock(BlockHitResult blockhitresult) {
      super.onHitBlock(blockhitresult);
      ((ServerLevel)this.level()).sendParticles(ParticleTypes.EXPLOSION, this.getX(), this.getY(), this.getZ(), 2, 0.2D, 0.2D, 0.2D, 0.0D);
      this.playSound(SoundEvents.SHULKER_BULLET_HIT, 1.0F, 1.0F);
   }

   private void destroy() {
      this.discard();
      this.level().gameEvent(GameEvent.ENTITY_DAMAGE, this.position(), GameEvent.Context.of(this));
   }

   protected void onHit(HitResult hitresult) {
      super.onHit(hitresult);
      this.destroy();
   }

   public boolean isPickable() {
      return true;
   }

   public boolean hurt(DamageSource damagesource, float f) {
      if (!this.level().isClientSide) {
         this.playSound(SoundEvents.SHULKER_BULLET_HURT, 1.0F, 1.0F);
         ((ServerLevel)this.level()).sendParticles(ParticleTypes.CRIT, this.getX(), this.getY(), this.getZ(), 15, 0.2D, 0.2D, 0.2D, 0.0D);
         this.destroy();
      }

      return true;
   }

   public void recreateFromPacket(ClientboundAddEntityPacket clientboundaddentitypacket) {
      super.recreateFromPacket(clientboundaddentitypacket);
      double d0 = clientboundaddentitypacket.getXa();
      double d1 = clientboundaddentitypacket.getYa();
      double d2 = clientboundaddentitypacket.getZa();
      this.setDeltaMovement(d0, d1, d2);
   }
}
