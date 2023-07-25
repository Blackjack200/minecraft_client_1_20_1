package net.minecraft.world.entity.projectile;

import net.minecraft.core.particles.ItemParticleOption;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.animal.Chicken;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.EntityHitResult;
import net.minecraft.world.phys.HitResult;

public class ThrownEgg extends ThrowableItemProjectile {
   public ThrownEgg(EntityType<? extends ThrownEgg> entitytype, Level level) {
      super(entitytype, level);
   }

   public ThrownEgg(Level level, LivingEntity livingentity) {
      super(EntityType.EGG, livingentity, level);
   }

   public ThrownEgg(Level level, double d0, double d1, double d2) {
      super(EntityType.EGG, d0, d1, d2, level);
   }

   public void handleEntityEvent(byte b0) {
      if (b0 == 3) {
         double d0 = 0.08D;

         for(int i = 0; i < 8; ++i) {
            this.level().addParticle(new ItemParticleOption(ParticleTypes.ITEM, this.getItem()), this.getX(), this.getY(), this.getZ(), ((double)this.random.nextFloat() - 0.5D) * 0.08D, ((double)this.random.nextFloat() - 0.5D) * 0.08D, ((double)this.random.nextFloat() - 0.5D) * 0.08D);
         }
      }

   }

   protected void onHitEntity(EntityHitResult entityhitresult) {
      super.onHitEntity(entityhitresult);
      entityhitresult.getEntity().hurt(this.damageSources().thrown(this, this.getOwner()), 0.0F);
   }

   protected void onHit(HitResult hitresult) {
      super.onHit(hitresult);
      if (!this.level().isClientSide) {
         if (this.random.nextInt(8) == 0) {
            int i = 1;
            if (this.random.nextInt(32) == 0) {
               i = 4;
            }

            for(int j = 0; j < i; ++j) {
               Chicken chicken = EntityType.CHICKEN.create(this.level());
               if (chicken != null) {
                  chicken.setAge(-24000);
                  chicken.moveTo(this.getX(), this.getY(), this.getZ(), this.getYRot(), 0.0F);
                  this.level().addFreshEntity(chicken);
               }
            }
         }

         this.level().broadcastEntityEvent(this, (byte)3);
         this.discard();
      }

   }

   protected Item getDefaultItem() {
      return Items.EGG;
   }
}
