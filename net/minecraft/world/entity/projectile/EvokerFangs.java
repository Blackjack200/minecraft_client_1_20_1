package net.minecraft.world.entity.projectile;

import java.util.UUID;
import javax.annotation.Nullable;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.TraceableEntity;
import net.minecraft.world.level.Level;

public class EvokerFangs extends Entity implements TraceableEntity {
   public static final int ATTACK_DURATION = 20;
   public static final int LIFE_OFFSET = 2;
   public static final int ATTACK_TRIGGER_TICKS = 14;
   private int warmupDelayTicks;
   private boolean sentSpikeEvent;
   private int lifeTicks = 22;
   private boolean clientSideAttackStarted;
   @Nullable
   private LivingEntity owner;
   @Nullable
   private UUID ownerUUID;

   public EvokerFangs(EntityType<? extends EvokerFangs> entitytype, Level level) {
      super(entitytype, level);
   }

   public EvokerFangs(Level level, double d0, double d1, double d2, float f, int i, LivingEntity livingentity) {
      this(EntityType.EVOKER_FANGS, level);
      this.warmupDelayTicks = i;
      this.setOwner(livingentity);
      this.setYRot(f * (180F / (float)Math.PI));
      this.setPos(d0, d1, d2);
   }

   protected void defineSynchedData() {
   }

   public void setOwner(@Nullable LivingEntity livingentity) {
      this.owner = livingentity;
      this.ownerUUID = livingentity == null ? null : livingentity.getUUID();
   }

   @Nullable
   public LivingEntity getOwner() {
      if (this.owner == null && this.ownerUUID != null && this.level() instanceof ServerLevel) {
         Entity entity = ((ServerLevel)this.level()).getEntity(this.ownerUUID);
         if (entity instanceof LivingEntity) {
            this.owner = (LivingEntity)entity;
         }
      }

      return this.owner;
   }

   protected void readAdditionalSaveData(CompoundTag compoundtag) {
      this.warmupDelayTicks = compoundtag.getInt("Warmup");
      if (compoundtag.hasUUID("Owner")) {
         this.ownerUUID = compoundtag.getUUID("Owner");
      }

   }

   protected void addAdditionalSaveData(CompoundTag compoundtag) {
      compoundtag.putInt("Warmup", this.warmupDelayTicks);
      if (this.ownerUUID != null) {
         compoundtag.putUUID("Owner", this.ownerUUID);
      }

   }

   public void tick() {
      super.tick();
      if (this.level().isClientSide) {
         if (this.clientSideAttackStarted) {
            --this.lifeTicks;
            if (this.lifeTicks == 14) {
               for(int i = 0; i < 12; ++i) {
                  double d0 = this.getX() + (this.random.nextDouble() * 2.0D - 1.0D) * (double)this.getBbWidth() * 0.5D;
                  double d1 = this.getY() + 0.05D + this.random.nextDouble();
                  double d2 = this.getZ() + (this.random.nextDouble() * 2.0D - 1.0D) * (double)this.getBbWidth() * 0.5D;
                  double d3 = (this.random.nextDouble() * 2.0D - 1.0D) * 0.3D;
                  double d4 = 0.3D + this.random.nextDouble() * 0.3D;
                  double d5 = (this.random.nextDouble() * 2.0D - 1.0D) * 0.3D;
                  this.level().addParticle(ParticleTypes.CRIT, d0, d1 + 1.0D, d2, d3, d4, d5);
               }
            }
         }
      } else if (--this.warmupDelayTicks < 0) {
         if (this.warmupDelayTicks == -8) {
            for(LivingEntity livingentity : this.level().getEntitiesOfClass(LivingEntity.class, this.getBoundingBox().inflate(0.2D, 0.0D, 0.2D))) {
               this.dealDamageTo(livingentity);
            }
         }

         if (!this.sentSpikeEvent) {
            this.level().broadcastEntityEvent(this, (byte)4);
            this.sentSpikeEvent = true;
         }

         if (--this.lifeTicks < 0) {
            this.discard();
         }
      }

   }

   private void dealDamageTo(LivingEntity livingentity) {
      LivingEntity livingentity1 = this.getOwner();
      if (livingentity.isAlive() && !livingentity.isInvulnerable() && livingentity != livingentity1) {
         if (livingentity1 == null) {
            livingentity.hurt(this.damageSources().magic(), 6.0F);
         } else {
            if (livingentity1.isAlliedTo(livingentity)) {
               return;
            }

            livingentity.hurt(this.damageSources().indirectMagic(this, livingentity1), 6.0F);
         }

      }
   }

   public void handleEntityEvent(byte b0) {
      super.handleEntityEvent(b0);
      if (b0 == 4) {
         this.clientSideAttackStarted = true;
         if (!this.isSilent()) {
            this.level().playLocalSound(this.getX(), this.getY(), this.getZ(), SoundEvents.EVOKER_FANGS_ATTACK, this.getSoundSource(), 1.0F, this.random.nextFloat() * 0.2F + 0.85F, false);
         }
      }

   }

   public float getAnimationProgress(float f) {
      if (!this.clientSideAttackStarted) {
         return 0.0F;
      } else {
         int i = this.lifeTicks - 2;
         return i <= 0 ? 1.0F : 1.0F - ((float)i - f) / 20.0F;
      }
   }
}
