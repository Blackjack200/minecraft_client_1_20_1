package net.minecraft.world.entity.boss.enderdragon.phases;

import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.boss.enderdragon.EnderDragon;
import net.minecraft.world.entity.projectile.AbstractArrow;

public abstract class AbstractDragonSittingPhase extends AbstractDragonPhaseInstance {
   public AbstractDragonSittingPhase(EnderDragon enderdragon) {
      super(enderdragon);
   }

   public boolean isSitting() {
      return true;
   }

   public float onHurt(DamageSource damagesource, float f) {
      if (damagesource.getDirectEntity() instanceof AbstractArrow) {
         damagesource.getDirectEntity().setSecondsOnFire(1);
         return 0.0F;
      } else {
         return super.onHurt(damagesource, f);
      }
   }
}
