package net.minecraft.world.entity.ai.attributes;

import com.google.common.collect.Maps;
import com.google.common.collect.Multimap;
import com.google.common.collect.Sets;
import com.mojang.logging.LogUtils;
import java.util.Collection;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;
import javax.annotation.Nullable;
import net.minecraft.Util;
import net.minecraft.core.Holder;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.resources.ResourceLocation;
import org.slf4j.Logger;

public class AttributeMap {
   private static final Logger LOGGER = LogUtils.getLogger();
   private final Map<Attribute, AttributeInstance> attributes = Maps.newHashMap();
   private final Set<AttributeInstance> dirtyAttributes = Sets.newHashSet();
   private final AttributeSupplier supplier;

   public AttributeMap(AttributeSupplier attributesupplier) {
      this.supplier = attributesupplier;
   }

   private void onAttributeModified(AttributeInstance attributeinstance) {
      if (attributeinstance.getAttribute().isClientSyncable()) {
         this.dirtyAttributes.add(attributeinstance);
      }

   }

   public Set<AttributeInstance> getDirtyAttributes() {
      return this.dirtyAttributes;
   }

   public Collection<AttributeInstance> getSyncableAttributes() {
      return this.attributes.values().stream().filter((attributeinstance) -> attributeinstance.getAttribute().isClientSyncable()).collect(Collectors.toList());
   }

   @Nullable
   public AttributeInstance getInstance(Attribute attribute) {
      return this.attributes.computeIfAbsent(attribute, (attribute1) -> this.supplier.createInstance(this::onAttributeModified, attribute1));
   }

   @Nullable
   public AttributeInstance getInstance(Holder<Attribute> holder) {
      return this.getInstance(holder.value());
   }

   public boolean hasAttribute(Attribute attribute) {
      return this.attributes.get(attribute) != null || this.supplier.hasAttribute(attribute);
   }

   public boolean hasAttribute(Holder<Attribute> holder) {
      return this.hasAttribute(holder.value());
   }

   public boolean hasModifier(Attribute attribute, UUID uuid) {
      AttributeInstance attributeinstance = this.attributes.get(attribute);
      return attributeinstance != null ? attributeinstance.getModifier(uuid) != null : this.supplier.hasModifier(attribute, uuid);
   }

   public boolean hasModifier(Holder<Attribute> holder, UUID uuid) {
      return this.hasModifier(holder.value(), uuid);
   }

   public double getValue(Attribute attribute) {
      AttributeInstance attributeinstance = this.attributes.get(attribute);
      return attributeinstance != null ? attributeinstance.getValue() : this.supplier.getValue(attribute);
   }

   public double getBaseValue(Attribute attribute) {
      AttributeInstance attributeinstance = this.attributes.get(attribute);
      return attributeinstance != null ? attributeinstance.getBaseValue() : this.supplier.getBaseValue(attribute);
   }

   public double getModifierValue(Attribute attribute, UUID uuid) {
      AttributeInstance attributeinstance = this.attributes.get(attribute);
      return attributeinstance != null ? attributeinstance.getModifier(uuid).getAmount() : this.supplier.getModifierValue(attribute, uuid);
   }

   public double getModifierValue(Holder<Attribute> holder, UUID uuid) {
      return this.getModifierValue(holder.value(), uuid);
   }

   public void removeAttributeModifiers(Multimap<Attribute, AttributeModifier> multimap) {
      multimap.asMap().forEach((attribute, collection) -> {
         AttributeInstance attributeinstance = this.attributes.get(attribute);
         if (attributeinstance != null) {
            collection.forEach(attributeinstance::removeModifier);
         }

      });
   }

   public void addTransientAttributeModifiers(Multimap<Attribute, AttributeModifier> multimap) {
      multimap.forEach((attribute, attributemodifier) -> {
         AttributeInstance attributeinstance = this.getInstance(attribute);
         if (attributeinstance != null) {
            attributeinstance.removeModifier(attributemodifier);
            attributeinstance.addTransientModifier(attributemodifier);
         }

      });
   }

   public void assignValues(AttributeMap attributemap) {
      attributemap.attributes.values().forEach((attributeinstance) -> {
         AttributeInstance attributeinstance1 = this.getInstance(attributeinstance.getAttribute());
         if (attributeinstance1 != null) {
            attributeinstance1.replaceFrom(attributeinstance);
         }

      });
   }

   public ListTag save() {
      ListTag listtag = new ListTag();

      for(AttributeInstance attributeinstance : this.attributes.values()) {
         listtag.add(attributeinstance.save());
      }

      return listtag;
   }

   public void load(ListTag listtag) {
      for(int i = 0; i < listtag.size(); ++i) {
         CompoundTag compoundtag = listtag.getCompound(i);
         String s = compoundtag.getString("Name");
         Util.ifElse(BuiltInRegistries.ATTRIBUTE.getOptional(ResourceLocation.tryParse(s)), (attribute) -> {
            AttributeInstance attributeinstance = this.getInstance(attribute);
            if (attributeinstance != null) {
               attributeinstance.load(compoundtag);
            }

         }, () -> LOGGER.warn("Ignoring unknown attribute '{}'", (Object)s));
      }

   }
}
