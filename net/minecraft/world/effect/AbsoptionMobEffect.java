package net.minecraft.world.effect;

import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.attributes.AttributeMap;

public class AbsoptionMobEffect extends MobEffect {
   protected AbsoptionMobEffect(MobEffectCategory mobeffectcategory, int i) {
      super(mobeffectcategory, i);
   }

   public void removeAttributeModifiers(LivingEntity livingentity, AttributeMap attributemap, int i) {
      livingentity.setAbsorptionAmount(livingentity.getAbsorptionAmount() - (float)(4 * (i + 1)));
      super.removeAttributeModifiers(livingentity, attributemap, i);
   }

   public void addAttributeModifiers(LivingEntity livingentity, AttributeMap attributemap, int i) {
      livingentity.setAbsorptionAmount(livingentity.getAbsorptionAmount() + (float)(4 * (i + 1)));
      super.addAttributeModifiers(livingentity, attributemap, i);
   }
}
