package net.minecraft.world.entity.ai.attributes;

import com.google.common.collect.ImmutableMap;
import com.google.common.collect.Maps;
import java.util.Map;
import java.util.UUID;
import java.util.function.Consumer;
import javax.annotation.Nullable;
import net.minecraft.core.registries.BuiltInRegistries;

public class AttributeSupplier {
   private final Map<Attribute, AttributeInstance> instances;

   public AttributeSupplier(Map<Attribute, AttributeInstance> map) {
      this.instances = ImmutableMap.copyOf(map);
   }

   private AttributeInstance getAttributeInstance(Attribute attribute) {
      AttributeInstance attributeinstance = this.instances.get(attribute);
      if (attributeinstance == null) {
         throw new IllegalArgumentException("Can't find attribute " + BuiltInRegistries.ATTRIBUTE.getKey(attribute));
      } else {
         return attributeinstance;
      }
   }

   public double getValue(Attribute attribute) {
      return this.getAttributeInstance(attribute).getValue();
   }

   public double getBaseValue(Attribute attribute) {
      return this.getAttributeInstance(attribute).getBaseValue();
   }

   public double getModifierValue(Attribute attribute, UUID uuid) {
      AttributeModifier attributemodifier = this.getAttributeInstance(attribute).getModifier(uuid);
      if (attributemodifier == null) {
         throw new IllegalArgumentException("Can't find modifier " + uuid + " on attribute " + BuiltInRegistries.ATTRIBUTE.getKey(attribute));
      } else {
         return attributemodifier.getAmount();
      }
   }

   @Nullable
   public AttributeInstance createInstance(Consumer<AttributeInstance> consumer, Attribute attribute) {
      AttributeInstance attributeinstance = this.instances.get(attribute);
      if (attributeinstance == null) {
         return null;
      } else {
         AttributeInstance attributeinstance1 = new AttributeInstance(attribute, consumer);
         attributeinstance1.replaceFrom(attributeinstance);
         return attributeinstance1;
      }
   }

   public static AttributeSupplier.Builder builder() {
      return new AttributeSupplier.Builder();
   }

   public boolean hasAttribute(Attribute attribute) {
      return this.instances.containsKey(attribute);
   }

   public boolean hasModifier(Attribute attribute, UUID uuid) {
      AttributeInstance attributeinstance = this.instances.get(attribute);
      return attributeinstance != null && attributeinstance.getModifier(uuid) != null;
   }

   public static class Builder {
      private final Map<Attribute, AttributeInstance> builder = Maps.newHashMap();
      private boolean instanceFrozen;

      private AttributeInstance create(Attribute attribute) {
         AttributeInstance attributeinstance = new AttributeInstance(attribute, (attributeinstance1) -> {
            if (this.instanceFrozen) {
               throw new UnsupportedOperationException("Tried to change value for default attribute instance: " + BuiltInRegistries.ATTRIBUTE.getKey(attribute));
            }
         });
         this.builder.put(attribute, attributeinstance);
         return attributeinstance;
      }

      public AttributeSupplier.Builder add(Attribute attribute) {
         this.create(attribute);
         return this;
      }

      public AttributeSupplier.Builder add(Attribute attribute, double d0) {
         AttributeInstance attributeinstance = this.create(attribute);
         attributeinstance.setBaseValue(d0);
         return this;
      }

      public AttributeSupplier build() {
         this.instanceFrozen = true;
         return new AttributeSupplier(this.builder);
      }
   }
}
