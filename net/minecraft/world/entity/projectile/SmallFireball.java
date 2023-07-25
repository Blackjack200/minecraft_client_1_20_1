package net.minecraft.world.entity.projectile;

import net.minecraft.core.BlockPos;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.level.GameRules;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.BaseFireBlock;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.EntityHitResult;
import net.minecraft.world.phys.HitResult;

public class SmallFireball extends Fireball {
   public SmallFireball(EntityType<? extends SmallFireball> entitytype, Level level) {
      super(entitytype, level);
   }

   public SmallFireball(Level level, LivingEntity livingentity, double d0, double d1, double d2) {
      super(EntityType.SMALL_FIREBALL, livingentity, d0, d1, d2, level);
   }

   public SmallFireball(Level level, double d0, double d1, double d2, double d3, double d4, double d5) {
      super(EntityType.SMALL_FIREBALL, d0, d1, d2, d3, d4, d5, level);
   }

   protected void onHitEntity(EntityHitResult entityhitresult) {
      super.onHitEntity(entityhitresult);
      if (!this.level().isClientSide) {
         Entity entity = entityhitresult.getEntity();
         Entity entity1 = this.getOwner();
         int i = entity.getRemainingFireTicks();
         entity.setSecondsOnFire(5);
         if (!entity.hurt(this.damageSources().fireball(this, entity1), 5.0F)) {
            entity.setRemainingFireTicks(i);
         } else if (entity1 instanceof LivingEntity) {
            this.doEnchantDamageEffects((LivingEntity)entity1, entity);
         }

      }
   }

   protected void onHitBlock(BlockHitResult blockhitresult) {
      super.onHitBlock(blockhitresult);
      if (!this.level().isClientSide) {
         Entity entity = this.getOwner();
         if (!(entity instanceof Mob) || this.level().getGameRules().getBoolean(GameRules.RULE_MOBGRIEFING)) {
            BlockPos blockpos = blockhitresult.getBlockPos().relative(blockhitresult.getDirection());
            if (this.level().isEmptyBlock(blockpos)) {
               this.level().setBlockAndUpdate(blockpos, BaseFireBlock.getState(this.level(), blockpos));
            }
         }

      }
   }

   protected void onHit(HitResult hitresult) {
      super.onHit(hitresult);
      if (!this.level().isClientSide) {
         this.discard();
      }

   }

   public boolean isPickable() {
      return false;
   }

   public boolean hurt(DamageSource damagesource, float f) {
      return false;
   }
}
