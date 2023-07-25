package net.minecraft.world.entity.projectile;

import net.minecraft.core.particles.ParticleOptions;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.game.ClientGamePacketListener;
import net.minecraft.network.protocol.game.ClientboundAddEntityPacket;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.Vec3;

public abstract class AbstractHurtingProjectile extends Projectile {
   public double xPower;
   public double yPower;
   public double zPower;

   protected AbstractHurtingProjectile(EntityType<? extends AbstractHurtingProjectile> entitytype, Level level) {
      super(entitytype, level);
   }

   public AbstractHurtingProjectile(EntityType<? extends AbstractHurtingProjectile> entitytype, double d0, double d1, double d2, double d3, double d4, double d5, Level level) {
      this(entitytype, level);
      this.moveTo(d0, d1, d2, this.getYRot(), this.getXRot());
      this.reapplyPosition();
      double d6 = Math.sqrt(d3 * d3 + d4 * d4 + d5 * d5);
      if (d6 != 0.0D) {
         this.xPower = d3 / d6 * 0.1D;
         this.yPower = d4 / d6 * 0.1D;
         this.zPower = d5 / d6 * 0.1D;
      }

   }

   public AbstractHurtingProjectile(EntityType<? extends AbstractHurtingProjectile> entitytype, LivingEntity livingentity, double d0, double d1, double d2, Level level) {
      this(entitytype, livingentity.getX(), livingentity.getY(), livingentity.getZ(), d0, d1, d2, level);
      this.setOwner(livingentity);
      this.setRot(livingentity.getYRot(), livingentity.getXRot());
   }

   protected void defineSynchedData() {
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
      Entity entity = this.getOwner();
      if (this.level().isClientSide || (entity == null || !entity.isRemoved()) && this.level().hasChunkAt(this.blockPosition())) {
         super.tick();
         if (this.shouldBurn()) {
            this.setSecondsOnFire(1);
         }

         HitResult hitresult = ProjectileUtil.getHitResultOnMoveVector(this, this::canHitEntity);
         if (hitresult.getType() != HitResult.Type.MISS) {
            this.onHit(hitresult);
         }

         this.checkInsideBlocks();
         Vec3 vec3 = this.getDeltaMovement();
         double d0 = this.getX() + vec3.x;
         double d1 = this.getY() + vec3.y;
         double d2 = this.getZ() + vec3.z;
         ProjectileUtil.rotateTowardsMovement(this, 0.2F);
         float f = this.getInertia();
         if (this.isInWater()) {
            for(int i = 0; i < 4; ++i) {
               float f1 = 0.25F;
               this.level().addParticle(ParticleTypes.BUBBLE, d0 - vec3.x * 0.25D, d1 - vec3.y * 0.25D, d2 - vec3.z * 0.25D, vec3.x, vec3.y, vec3.z);
            }

            f = 0.8F;
         }

         this.setDeltaMovement(vec3.add(this.xPower, this.yPower, this.zPower).scale((double)f));
         this.level().addParticle(this.getTrailParticle(), d0, d1 + 0.5D, d2, 0.0D, 0.0D, 0.0D);
         this.setPos(d0, d1, d2);
      } else {
         this.discard();
      }
   }

   protected boolean canHitEntity(Entity entity) {
      return super.canHitEntity(entity) && !entity.noPhysics;
   }

   protected boolean shouldBurn() {
      return true;
   }

   protected ParticleOptions getTrailParticle() {
      return ParticleTypes.SMOKE;
   }

   protected float getInertia() {
      return 0.95F;
   }

   public void addAdditionalSaveData(CompoundTag compoundtag) {
      super.addAdditionalSaveData(compoundtag);
      compoundtag.put("power", this.newDoubleList(new double[]{this.xPower, this.yPower, this.zPower}));
   }

   public void readAdditionalSaveData(CompoundTag compoundtag) {
      super.readAdditionalSaveData(compoundtag);
      if (compoundtag.contains("power", 9)) {
         ListTag listtag = compoundtag.getList("power", 6);
         if (listtag.size() == 3) {
            this.xPower = listtag.getDouble(0);
            this.yPower = listtag.getDouble(1);
            this.zPower = listtag.getDouble(2);
         }
      }

   }

   public boolean isPickable() {
      return true;
   }

   public float getPickRadius() {
      return 1.0F;
   }

   public boolean hurt(DamageSource damagesource, float f) {
      if (this.isInvulnerableTo(damagesource)) {
         return false;
      } else {
         this.markHurt();
         Entity entity = damagesource.getEntity();
         if (entity != null) {
            if (!this.level().isClientSide) {
               Vec3 vec3 = entity.getLookAngle();
               this.setDeltaMovement(vec3);
               this.xPower = vec3.x * 0.1D;
               this.yPower = vec3.y * 0.1D;
               this.zPower = vec3.z * 0.1D;
               this.setOwner(entity);
            }

            return true;
         } else {
            return false;
         }
      }
   }

   public float getLightLevelDependentMagicValue() {
      return 1.0F;
   }

   public Packet<ClientGamePacketListener> getAddEntityPacket() {
      Entity entity = this.getOwner();
      int i = entity == null ? 0 : entity.getId();
      return new ClientboundAddEntityPacket(this.getId(), this.getUUID(), this.getX(), this.getY(), this.getZ(), this.getXRot(), this.getYRot(), this.getType(), i, new Vec3(this.xPower, this.yPower, this.zPower), 0.0D);
   }

   public void recreateFromPacket(ClientboundAddEntityPacket clientboundaddentitypacket) {
      super.recreateFromPacket(clientboundaddentitypacket);
      double d0 = clientboundaddentitypacket.getXa();
      double d1 = clientboundaddentitypacket.getYa();
      double d2 = clientboundaddentitypacket.getZa();
      double d3 = Math.sqrt(d0 * d0 + d1 * d1 + d2 * d2);
      if (d3 != 0.0D) {
         this.xPower = d0 / d3 * 0.1D;
         this.yPower = d1 / d3 * 0.1D;
         this.zPower = d2 / d3 * 0.1D;
      }

   }
}
