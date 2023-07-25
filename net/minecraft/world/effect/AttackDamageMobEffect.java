package net.minecraft.world.effect;

import net.minecraft.world.entity.ai.attributes.AttributeModifier;

public class AttackDamageMobEffect extends MobEffect {
   protected final double multiplier;

   protected AttackDamageMobEffect(MobEffectCategory mobeffectcategory, int i, double d0) {
      super(mobeffectcategory, i);
      this.multiplier = d0;
   }

   public double getAttributeModifierValue(int i, AttributeModifier attributemodifier) {
      return this.multiplier * (double)(i + 1);
   }
}
