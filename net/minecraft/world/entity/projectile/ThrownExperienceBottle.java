package net.minecraft.world.entity.projectile;

import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.ExperienceOrb;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.alchemy.PotionUtils;
import net.minecraft.world.item.alchemy.Potions;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.HitResult;

public class ThrownExperienceBottle extends ThrowableItemProjectile {
   public ThrownExperienceBottle(EntityType<? extends ThrownExperienceBottle> entitytype, Level level) {
      super(entitytype, level);
   }

   public ThrownExperienceBottle(Level level, LivingEntity livingentity) {
      super(EntityType.EXPERIENCE_BOTTLE, livingentity, level);
   }

   public ThrownExperienceBottle(Level level, double d0, double d1, double d2) {
      super(EntityType.EXPERIENCE_BOTTLE, d0, d1, d2, level);
   }

   protected Item getDefaultItem() {
      return Items.EXPERIENCE_BOTTLE;
   }

   protected float getGravity() {
      return 0.07F;
   }

   protected void onHit(HitResult hitresult) {
      super.onHit(hitresult);
      if (this.level() instanceof ServerLevel) {
         this.level().levelEvent(2002, this.blockPosition(), PotionUtils.getColor(Potions.WATER));
         int i = 3 + this.level().random.nextInt(5) + this.level().random.nextInt(5);
         ExperienceOrb.award((ServerLevel)this.level(), this.position(), i);
         this.discard();
      }

   }
}
