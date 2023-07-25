package net.minecraft.world.entity.monster.hoglin;

import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.phys.Vec3;

public interface HoglinBase {
   int ATTACK_ANIMATION_DURATION = 10;

   int getAttackAnimationRemainingTicks();

   static boolean hurtAndThrowTarget(LivingEntity livingentity, LivingEntity livingentity1) {
      float f = (float)livingentity.getAttributeValue(Attributes.ATTACK_DAMAGE);
      float f1;
      if (!livingentity.isBaby() && (int)f > 0) {
         f1 = f / 2.0F + (float)livingentity.level().random.nextInt((int)f);
      } else {
         f1 = f;
      }

      boolean flag = livingentity1.hurt(livingentity.damageSources().mobAttack(livingentity), f1);
      if (flag) {
         livingentity.doEnchantDamageEffects(livingentity, livingentity1);
         if (!livingentity.isBaby()) {
            throwTarget(livingentity, livingentity1);
         }
      }

      return flag;
   }

   static void throwTarget(LivingEntity livingentity, LivingEntity livingentity1) {
      double d0 = livingentity.getAttributeValue(Attributes.ATTACK_KNOCKBACK);
      double d1 = livingentity1.getAttributeValue(Attributes.KNOCKBACK_RESISTANCE);
      double d2 = d0 - d1;
      if (!(d2 <= 0.0D)) {
         double d3 = livingentity1.getX() - livingentity.getX();
         double d4 = livingentity1.getZ() - livingentity.getZ();
         float f = (float)(livingentity.level().random.nextInt(21) - 10);
         double d5 = d2 * (double)(livingentity.level().random.nextFloat() * 0.5F + 0.2F);
         Vec3 vec3 = (new Vec3(d3, 0.0D, d4)).normalize().scale(d5).yRot(f);
         double d6 = d2 * (double)livingentity.level().random.nextFloat() * 0.5D;
         livingentity1.push(vec3.x, d6, vec3.z);
         livingentity1.hurtMarked = true;
      }
   }
}
