package net.minecraft.world.effect;

import com.google.common.collect.Maps;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;
import java.util.function.Supplier;
import javax.annotation.Nullable;
import net.minecraft.Util;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.attributes.Attribute;
import net.minecraft.world.entity.ai.attributes.AttributeInstance;
import net.minecraft.world.entity.ai.attributes.AttributeMap;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.player.Player;

public class MobEffect {
   private final Map<Attribute, AttributeModifier> attributeModifiers = Maps.newHashMap();
   private final MobEffectCategory category;
   private final int color;
   @Nullable
   private String descriptionId;
   private Supplier<MobEffectInstance.FactorData> factorDataFactory = () -> null;

   @Nullable
   public static MobEffect byId(int i) {
      return BuiltInRegistries.MOB_EFFECT.byId(i);
   }

   public static int getId(MobEffect mobeffect) {
      return BuiltInRegistries.MOB_EFFECT.getId(mobeffect);
   }

   public static int getIdFromNullable(@Nullable MobEffect mobeffect) {
      return BuiltInRegistries.MOB_EFFECT.getId(mobeffect);
   }

   protected MobEffect(MobEffectCategory mobeffectcategory, int i) {
      this.category = mobeffectcategory;
      this.color = i;
   }

   public Optional<MobEffectInstance.FactorData> createFactorData() {
      return Optional.ofNullable(this.factorDataFactory.get());
   }

   public void applyEffectTick(LivingEntity livingentity, int i) {
      if (this == MobEffects.REGENERATION) {
         if (livingentity.getHealth() < livingentity.getMaxHealth()) {
            livingentity.heal(1.0F);
         }
      } else if (this == MobEffects.POISON) {
         if (livingentity.getHealth() > 1.0F) {
            livingentity.hurt(livingentity.damageSources().magic(), 1.0F);
         }
      } else if (this == MobEffects.WITHER) {
         livingentity.hurt(livingentity.damageSources().wither(), 1.0F);
      } else if (this == MobEffects.HUNGER && livingentity instanceof Player) {
         ((Player)livingentity).causeFoodExhaustion(0.005F * (float)(i + 1));
      } else if (this == MobEffects.SATURATION && livingentity instanceof Player) {
         if (!livingentity.level().isClientSide) {
            ((Player)livingentity).getFoodData().eat(i + 1, 1.0F);
         }
      } else if ((this != MobEffects.HEAL || livingentity.isInvertedHealAndHarm()) && (this != MobEffects.HARM || !livingentity.isInvertedHealAndHarm())) {
         if (this == MobEffects.HARM && !livingentity.isInvertedHealAndHarm() || this == MobEffects.HEAL && livingentity.isInvertedHealAndHarm()) {
            livingentity.hurt(livingentity.damageSources().magic(), (float)(6 << i));
         }
      } else {
         livingentity.heal((float)Math.max(4 << i, 0));
      }

   }

   public void applyInstantenousEffect(@Nullable Entity entity, @Nullable Entity entity1, LivingEntity livingentity, int i, double d0) {
      if ((this != MobEffects.HEAL || livingentity.isInvertedHealAndHarm()) && (this != MobEffects.HARM || !livingentity.isInvertedHealAndHarm())) {
         if (this == MobEffects.HARM && !livingentity.isInvertedHealAndHarm() || this == MobEffects.HEAL && livingentity.isInvertedHealAndHarm()) {
            int k = (int)(d0 * (double)(6 << i) + 0.5D);
            if (entity == null) {
               livingentity.hurt(livingentity.damageSources().magic(), (float)k);
            } else {
               livingentity.hurt(livingentity.damageSources().indirectMagic(entity, entity1), (float)k);
            }
         } else {
            this.applyEffectTick(livingentity, i);
         }
      } else {
         int j = (int)(d0 * (double)(4 << i) + 0.5D);
         livingentity.heal((float)j);
      }

   }

   public boolean isDurationEffectTick(int i, int j) {
      if (this == MobEffects.REGENERATION) {
         int k = 50 >> j;
         if (k > 0) {
            return i % k == 0;
         } else {
            return true;
         }
      } else if (this == MobEffects.POISON) {
         int l = 25 >> j;
         if (l > 0) {
            return i % l == 0;
         } else {
            return true;
         }
      } else if (this == MobEffects.WITHER) {
         int i1 = 40 >> j;
         if (i1 > 0) {
            return i % i1 == 0;
         } else {
            return true;
         }
      } else {
         return this == MobEffects.HUNGER;
      }
   }

   public boolean isInstantenous() {
      return false;
   }

   protected String getOrCreateDescriptionId() {
      if (this.descriptionId == null) {
         this.descriptionId = Util.makeDescriptionId("effect", BuiltInRegistries.MOB_EFFECT.getKey(this));
      }

      return this.descriptionId;
   }

   public String getDescriptionId() {
      return this.getOrCreateDescriptionId();
   }

   public Component getDisplayName() {
      return Component.translatable(this.getDescriptionId());
   }

   public MobEffectCategory getCategory() {
      return this.category;
   }

   public int getColor() {
      return this.color;
   }

   public MobEffect addAttributeModifier(Attribute attribute, String s, double d0, AttributeModifier.Operation attributemodifier_operation) {
      AttributeModifier attributemodifier = new AttributeModifier(UUID.fromString(s), this::getDescriptionId, d0, attributemodifier_operation);
      this.attributeModifiers.put(attribute, attributemodifier);
      return this;
   }

   public MobEffect setFactorDataFactory(Supplier<MobEffectInstance.FactorData> supplier) {
      this.factorDataFactory = supplier;
      return this;
   }

   public Map<Attribute, AttributeModifier> getAttributeModifiers() {
      return this.attributeModifiers;
   }

   public void removeAttributeModifiers(LivingEntity livingentity, AttributeMap attributemap, int i) {
      for(Map.Entry<Attribute, AttributeModifier> map_entry : this.attributeModifiers.entrySet()) {
         AttributeInstance attributeinstance = attributemap.getInstance(map_entry.getKey());
         if (attributeinstance != null) {
            attributeinstance.removeModifier(map_entry.getValue());
         }
      }

   }

   public void addAttributeModifiers(LivingEntity livingentity, AttributeMap attributemap, int i) {
      for(Map.Entry<Attribute, AttributeModifier> map_entry : this.attributeModifiers.entrySet()) {
         AttributeInstance attributeinstance = attributemap.getInstance(map_entry.getKey());
         if (attributeinstance != null) {
            AttributeModifier attributemodifier = map_entry.getValue();
            attributeinstance.removeModifier(attributemodifier);
            attributeinstance.addPermanentModifier(new AttributeModifier(attributemodifier.getId(), this.getDescriptionId() + " " + i, this.getAttributeModifierValue(i, attributemodifier), attributemodifier.getOperation()));
         }
      }

   }

   public double getAttributeModifierValue(int i, AttributeModifier attributemodifier) {
      return attributemodifier.getAmount() * (double)(i + 1);
   }

   public boolean isBeneficial() {
      return this.category == MobEffectCategory.BENEFICIAL;
   }
}
