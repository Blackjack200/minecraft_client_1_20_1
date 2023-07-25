package net.minecraft.world.effect;

import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.attributes.AttributeMap;

public class HealthBoostMobEffect extends MobEffect {
   public HealthBoostMobEffect(MobEffectCategory mobeffectcategory, int i) {
      super(mobeffectcategory, i);
   }

   public void removeAttributeModifiers(LivingEntity livingentity, AttributeMap attributemap, int i) {
      super.removeAttributeModifiers(livingentity, attributemap, i);
      if (livingentity.getHealth() > livingentity.getMaxHealth()) {
         livingentity.setHealth(livingentity.getMaxHealth());
      }

   }
}
