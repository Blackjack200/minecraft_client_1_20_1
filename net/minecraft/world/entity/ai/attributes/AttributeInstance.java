package net.minecraft.world.entity.ai.attributes;

import com.google.common.collect.ImmutableSet;
import com.google.common.collect.Maps;
import com.google.common.collect.Sets;
import it.unimi.dsi.fastutil.objects.Object2ObjectArrayMap;
import it.unimi.dsi.fastutil.objects.ObjectArraySet;
import java.util.Collection;
import java.util.Collections;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.function.Consumer;
import javax.annotation.Nullable;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;

public class AttributeInstance {
   private final Attribute attribute;
   private final Map<AttributeModifier.Operation, Set<AttributeModifier>> modifiersByOperation = Maps.newEnumMap(AttributeModifier.Operation.class);
   private final Map<UUID, AttributeModifier> modifierById = new Object2ObjectArrayMap<>();
   private final Set<AttributeModifier> permanentModifiers = new ObjectArraySet<>();
   private double baseValue;
   private boolean dirty = true;
   private double cachedValue;
   private final Consumer<AttributeInstance> onDirty;

   public AttributeInstance(Attribute attribute, Consumer<AttributeInstance> consumer) {
      this.attribute = attribute;
      this.onDirty = consumer;
      this.baseValue = attribute.getDefaultValue();
   }

   public Attribute getAttribute() {
      return this.attribute;
   }

   public double getBaseValue() {
      return this.baseValue;
   }

   public void setBaseValue(double d0) {
      if (d0 != this.baseValue) {
         this.baseValue = d0;
         this.setDirty();
      }
   }

   public Set<AttributeModifier> getModifiers(AttributeModifier.Operation attributemodifier_operation) {
      return this.modifiersByOperation.computeIfAbsent(attributemodifier_operation, (attributemodifier_operation1) -> Sets.newHashSet());
   }

   public Set<AttributeModifier> getModifiers() {
      return ImmutableSet.copyOf(this.modifierById.values());
   }

   @Nullable
   public AttributeModifier getModifier(UUID uuid) {
      return this.modifierById.get(uuid);
   }

   public boolean hasModifier(AttributeModifier attributemodifier) {
      return this.modifierById.get(attributemodifier.getId()) != null;
   }

   private void addModifier(AttributeModifier attributemodifier) {
      AttributeModifier attributemodifier1 = this.modifierById.putIfAbsent(attributemodifier.getId(), attributemodifier);
      if (attributemodifier1 != null) {
         throw new IllegalArgumentException("Modifier is already applied on this attribute!");
      } else {
         this.getModifiers(attributemodifier.getOperation()).add(attributemodifier);
         this.setDirty();
      }
   }

   public void addTransientModifier(AttributeModifier attributemodifier) {
      this.addModifier(attributemodifier);
   }

   public void addPermanentModifier(AttributeModifier attributemodifier) {
      this.addModifier(attributemodifier);
      this.permanentModifiers.add(attributemodifier);
   }

   protected void setDirty() {
      this.dirty = true;
      this.onDirty.accept(this);
   }

   public void removeModifier(AttributeModifier attributemodifier) {
      this.getModifiers(attributemodifier.getOperation()).remove(attributemodifier);
      this.modifierById.remove(attributemodifier.getId());
      this.permanentModifiers.remove(attributemodifier);
      this.setDirty();
   }

   public void removeModifier(UUID uuid) {
      AttributeModifier attributemodifier = this.getModifier(uuid);
      if (attributemodifier != null) {
         this.removeModifier(attributemodifier);
      }

   }

   public boolean removePermanentModifier(UUID uuid) {
      AttributeModifier attributemodifier = this.getModifier(uuid);
      if (attributemodifier != null && this.permanentModifiers.contains(attributemodifier)) {
         this.removeModifier(attributemodifier);
         return true;
      } else {
         return false;
      }
   }

   public void removeModifiers() {
      for(AttributeModifier attributemodifier : this.getModifiers()) {
         this.removeModifier(attributemodifier);
      }

   }

   public double getValue() {
      if (this.dirty) {
         this.cachedValue = this.calculateValue();
         this.dirty = false;
      }

      return this.cachedValue;
   }

   private double calculateValue() {
      double d0 = this.getBaseValue();

      for(AttributeModifier attributemodifier : this.getModifiersOrEmpty(AttributeModifier.Operation.ADDITION)) {
         d0 += attributemodifier.getAmount();
      }

      double d1 = d0;

      for(AttributeModifier attributemodifier1 : this.getModifiersOrEmpty(AttributeModifier.Operation.MULTIPLY_BASE)) {
         d1 += d0 * attributemodifier1.getAmount();
      }

      for(AttributeModifier attributemodifier2 : this.getModifiersOrEmpty(AttributeModifier.Operation.MULTIPLY_TOTAL)) {
         d1 *= 1.0D + attributemodifier2.getAmount();
      }

      return this.attribute.sanitizeValue(d1);
   }

   private Collection<AttributeModifier> getModifiersOrEmpty(AttributeModifier.Operation attributemodifier_operation) {
      return this.modifiersByOperation.getOrDefault(attributemodifier_operation, Collections.emptySet());
   }

   public void replaceFrom(AttributeInstance attributeinstance) {
      this.baseValue = attributeinstance.baseValue;
      this.modifierById.clear();
      this.modifierById.putAll(attributeinstance.modifierById);
      this.permanentModifiers.clear();
      this.permanentModifiers.addAll(attributeinstance.permanentModifiers);
      this.modifiersByOperation.clear();
      attributeinstance.modifiersByOperation.forEach((attributemodifier_operation, set) -> this.getModifiers(attributemodifier_operation).addAll(set));
      this.setDirty();
   }

   public CompoundTag save() {
      CompoundTag compoundtag = new CompoundTag();
      compoundtag.putString("Name", BuiltInRegistries.ATTRIBUTE.getKey(this.attribute).toString());
      compoundtag.putDouble("Base", this.baseValue);
      if (!this.permanentModifiers.isEmpty()) {
         ListTag listtag = new ListTag();

         for(AttributeModifier attributemodifier : this.permanentModifiers) {
            listtag.add(attributemodifier.save());
         }

         compoundtag.put("Modifiers", listtag);
      }

      return compoundtag;
   }

   public void load(CompoundTag compoundtag) {
      this.baseValue = compoundtag.getDouble("Base");
      if (compoundtag.contains("Modifiers", 9)) {
         ListTag listtag = compoundtag.getList("Modifiers", 10);

         for(int i = 0; i < listtag.size(); ++i) {
            AttributeModifier attributemodifier = AttributeModifier.load(listtag.getCompound(i));
            if (attributemodifier != null) {
               this.modifierById.put(attributemodifier.getId(), attributemodifier);
               this.getModifiers(attributemodifier.getOperation()).add(attributemodifier);
               this.permanentModifiers.add(attributemodifier);
            }
         }
      }

      this.setDirty();
   }
}
