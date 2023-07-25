package net.minecraft.world.entity.projectile;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.level.GameRules;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.EntityHitResult;
import net.minecraft.world.phys.HitResult;

public class LargeFireball extends Fireball {
   private int explosionPower = 1;

   public LargeFireball(EntityType<? extends LargeFireball> entitytype, Level level) {
      super(entitytype, level);
   }

   public LargeFireball(Level level, LivingEntity livingentity, double d0, double d1, double d2, int i) {
      super(EntityType.FIREBALL, livingentity, d0, d1, d2, level);
      this.explosionPower = i;
   }

   protected void onHit(HitResult hitresult) {
      super.onHit(hitresult);
      if (!this.level().isClientSide) {
         boolean flag = this.level().getGameRules().getBoolean(GameRules.RULE_MOBGRIEFING);
         this.level().explode(this, this.getX(), this.getY(), this.getZ(), (float)this.explosionPower, flag, Level.ExplosionInteraction.MOB);
         this.discard();
      }

   }

   protected void onHitEntity(EntityHitResult entityhitresult) {
      super.onHitEntity(entityhitresult);
      if (!this.level().isClientSide) {
         Entity entity = entityhitresult.getEntity();
         Entity entity1 = this.getOwner();
         entity.hurt(this.damageSources().fireball(this, entity1), 6.0F);
         if (entity1 instanceof LivingEntity) {
            this.doEnchantDamageEffects((LivingEntity)entity1, entity);
         }

      }
   }

   public void addAdditionalSaveData(CompoundTag compoundtag) {
      super.addAdditionalSaveData(compoundtag);
      compoundtag.putByte("ExplosionPower", (byte)this.explosionPower);
   }

   public void readAdditionalSaveData(CompoundTag compoundtag) {
      super.readAdditionalSaveData(compoundtag);
      if (compoundtag.contains("ExplosionPower", 99)) {
         this.explosionPower = compoundtag.getByte("ExplosionPower");
      }

   }
}
