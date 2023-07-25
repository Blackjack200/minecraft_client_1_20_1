package net.minecraft.world.entity.projectile;

import net.minecraft.core.BlockPos;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.world.Difficulty;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.boss.wither.WitherBoss;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Explosion;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.material.FluidState;
import net.minecraft.world.phys.EntityHitResult;
import net.minecraft.world.phys.HitResult;

public class WitherSkull extends AbstractHurtingProjectile {
   private static final EntityDataAccessor<Boolean> DATA_DANGEROUS = SynchedEntityData.defineId(WitherSkull.class, EntityDataSerializers.BOOLEAN);

   public WitherSkull(EntityType<? extends WitherSkull> entitytype, Level level) {
      super(entitytype, level);
   }

   public WitherSkull(Level level, LivingEntity livingentity, double d0, double d1, double d2) {
      super(EntityType.WITHER_SKULL, livingentity, d0, d1, d2, level);
   }

   protected float getInertia() {
      return this.isDangerous() ? 0.73F : super.getInertia();
   }

   public boolean isOnFire() {
      return false;
   }

   public float getBlockExplosionResistance(Explosion explosion, BlockGetter blockgetter, BlockPos blockpos, BlockState blockstate, FluidState fluidstate, float f) {
      return this.isDangerous() && WitherBoss.canDestroy(blockstate) ? Math.min(0.8F, f) : f;
   }

   protected void onHitEntity(EntityHitResult entityhitresult) {
      super.onHitEntity(entityhitresult);
      if (!this.level().isClientSide) {
         Entity entity = entityhitresult.getEntity();
         Entity entity1 = this.getOwner();
         boolean flag;
         if (entity1 instanceof LivingEntity) {
            LivingEntity livingentity = (LivingEntity)entity1;
            flag = entity.hurt(this.damageSources().witherSkull(this, livingentity), 8.0F);
            if (flag) {
               if (entity.isAlive()) {
                  this.doEnchantDamageEffects(livingentity, entity);
               } else {
                  livingentity.heal(5.0F);
               }
            }
         } else {
            flag = entity.hurt(this.damageSources().magic(), 5.0F);
         }

         if (flag && entity instanceof LivingEntity) {
            LivingEntity livingentity1 = (LivingEntity)entity;
            int i = 0;
            if (this.level().getDifficulty() == Difficulty.NORMAL) {
               i = 10;
            } else if (this.level().getDifficulty() == Difficulty.HARD) {
               i = 40;
            }

            if (i > 0) {
               livingentity1.addEffect(new MobEffectInstance(MobEffects.WITHER, 20 * i, 1), this.getEffectSource());
            }
         }

      }
   }

   protected void onHit(HitResult hitresult) {
      super.onHit(hitresult);
      if (!this.level().isClientSide) {
         this.level().explode(this, this.getX(), this.getY(), this.getZ(), 1.0F, false, Level.ExplosionInteraction.MOB);
         this.discard();
      }

   }

   public boolean isPickable() {
      return false;
   }

   public boolean hurt(DamageSource damagesource, float f) {
      return false;
   }

   protected void defineSynchedData() {
      this.entityData.define(DATA_DANGEROUS, false);
   }

   public boolean isDangerous() {
      return this.entityData.get(DATA_DANGEROUS);
   }

   public void setDangerous(boolean flag) {
      this.entityData.set(DATA_DANGEROUS, flag);
   }

   protected boolean shouldBurn() {
      return false;
   }
}
