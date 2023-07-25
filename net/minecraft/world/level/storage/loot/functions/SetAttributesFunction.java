package net.minecraft.world.level.storage.loot.functions;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableSet;
import com.google.common.collect.Lists;
import com.google.gson.JsonArray;
import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonPrimitive;
import com.google.gson.JsonSerializationContext;
import com.google.gson.JsonSyntaxException;
import java.util.EnumSet;
import java.util.List;
import java.util.Set;
import java.util.UUID;
import javax.annotation.Nullable;
import net.minecraft.Util;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.GsonHelper;
import net.minecraft.util.RandomSource;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.ai.attributes.Attribute;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.storage.loot.LootContext;
import net.minecraft.world.level.storage.loot.parameters.LootContextParam;
import net.minecraft.world.level.storage.loot.predicates.LootItemCondition;
import net.minecraft.world.level.storage.loot.providers.number.NumberProvider;

public class SetAttributesFunction extends LootItemConditionalFunction {
   final List<SetAttributesFunction.Modifier> modifiers;

   SetAttributesFunction(LootItemCondition[] alootitemcondition, List<SetAttributesFunction.Modifier> list) {
      super(alootitemcondition);
      this.modifiers = ImmutableList.copyOf(list);
   }

   public LootItemFunctionType getType() {
      return LootItemFunctions.SET_ATTRIBUTES;
   }

   public Set<LootContextParam<?>> getReferencedContextParams() {
      return this.modifiers.stream().flatMap((setattributesfunction_modifier) -> setattributesfunction_modifier.amount.getReferencedContextParams().stream()).collect(ImmutableSet.toImmutableSet());
   }

   public ItemStack run(ItemStack itemstack, LootContext lootcontext) {
      RandomSource randomsource = lootcontext.getRandom();

      for(SetAttributesFunction.Modifier setattributesfunction_modifier : this.modifiers) {
         UUID uuid = setattributesfunction_modifier.id;
         if (uuid == null) {
            uuid = UUID.randomUUID();
         }

         EquipmentSlot equipmentslot = Util.getRandom(setattributesfunction_modifier.slots, randomsource);
         itemstack.addAttributeModifier(setattributesfunction_modifier.attribute, new AttributeModifier(uuid, setattributesfunction_modifier.name, (double)setattributesfunction_modifier.amount.getFloat(lootcontext), setattributesfunction_modifier.operation), equipmentslot);
      }

      return itemstack;
   }

   public static SetAttributesFunction.ModifierBuilder modifier(String s, Attribute attribute, AttributeModifier.Operation attributemodifier_operation, NumberProvider numberprovider) {
      return new SetAttributesFunction.ModifierBuilder(s, attribute, attributemodifier_operation, numberprovider);
   }

   public static SetAttributesFunction.Builder setAttributes() {
      return new SetAttributesFunction.Builder();
   }

   public static class Builder extends LootItemConditionalFunction.Builder<SetAttributesFunction.Builder> {
      private final List<SetAttributesFunction.Modifier> modifiers = Lists.newArrayList();

      protected SetAttributesFunction.Builder getThis() {
         return this;
      }

      public SetAttributesFunction.Builder withModifier(SetAttributesFunction.ModifierBuilder setattributesfunction_modifierbuilder) {
         this.modifiers.add(setattributesfunction_modifierbuilder.build());
         return this;
      }

      public LootItemFunction build() {
         return new SetAttributesFunction(this.getConditions(), this.modifiers);
      }
   }

   static class Modifier {
      final String name;
      final Attribute attribute;
      final AttributeModifier.Operation operation;
      final NumberProvider amount;
      @Nullable
      final UUID id;
      final EquipmentSlot[] slots;

      Modifier(String s, Attribute attribute, AttributeModifier.Operation attributemodifier_operation, NumberProvider numberprovider, EquipmentSlot[] aequipmentslot, @Nullable UUID uuid) {
         this.name = s;
         this.attribute = attribute;
         this.operation = attributemodifier_operation;
         this.amount = numberprovider;
         this.id = uuid;
         this.slots = aequipmentslot;
      }

      public JsonObject serialize(JsonSerializationContext jsonserializationcontext) {
         JsonObject jsonobject = new JsonObject();
         jsonobject.addProperty("name", this.name);
         jsonobject.addProperty("attribute", BuiltInRegistries.ATTRIBUTE.getKey(this.attribute).toString());
         jsonobject.addProperty("operation", operationToString(this.operation));
         jsonobject.add("amount", jsonserializationcontext.serialize(this.amount));
         if (this.id != null) {
            jsonobject.addProperty("id", this.id.toString());
         }

         if (this.slots.length == 1) {
            jsonobject.addProperty("slot", this.slots[0].getName());
         } else {
            JsonArray jsonarray = new JsonArray();

            for(EquipmentSlot equipmentslot : this.slots) {
               jsonarray.add(new JsonPrimitive(equipmentslot.getName()));
            }

            jsonobject.add("slot", jsonarray);
         }

         return jsonobject;
      }

      public static SetAttributesFunction.Modifier deserialize(JsonObject jsonobject, JsonDeserializationContext jsondeserializationcontext) {
         String s = GsonHelper.getAsString(jsonobject, "name");
         ResourceLocation resourcelocation = new ResourceLocation(GsonHelper.getAsString(jsonobject, "attribute"));
         Attribute attribute = BuiltInRegistries.ATTRIBUTE.get(resourcelocation);
         if (attribute == null) {
            throw new JsonSyntaxException("Unknown attribute: " + resourcelocation);
         } else {
            AttributeModifier.Operation attributemodifier_operation = operationFromString(GsonHelper.getAsString(jsonobject, "operation"));
            NumberProvider numberprovider = GsonHelper.getAsObject(jsonobject, "amount", jsondeserializationcontext, NumberProvider.class);
            UUID uuid = null;
            EquipmentSlot[] aequipmentslot;
            if (GsonHelper.isStringValue(jsonobject, "slot")) {
               aequipmentslot = new EquipmentSlot[]{EquipmentSlot.byName(GsonHelper.getAsString(jsonobject, "slot"))};
            } else {
               if (!GsonHelper.isArrayNode(jsonobject, "slot")) {
                  throw new JsonSyntaxException("Invalid or missing attribute modifier slot; must be either string or array of strings.");
               }

               JsonArray jsonarray = GsonHelper.getAsJsonArray(jsonobject, "slot");
               aequipmentslot = new EquipmentSlot[jsonarray.size()];
               int i = 0;

               for(JsonElement jsonelement : jsonarray) {
                  aequipmentslot[i++] = EquipmentSlot.byName(GsonHelper.convertToString(jsonelement, "slot"));
               }

               if (aequipmentslot.length == 0) {
                  throw new JsonSyntaxException("Invalid attribute modifier slot; must contain at least one entry.");
               }
            }

            if (jsonobject.has("id")) {
               String s1 = GsonHelper.getAsString(jsonobject, "id");

               try {
                  uuid = UUID.fromString(s1);
               } catch (IllegalArgumentException var13) {
                  throw new JsonSyntaxException("Invalid attribute modifier id '" + s1 + "' (must be UUID format, with dashes)");
               }
            }

            return new SetAttributesFunction.Modifier(s, attribute, attributemodifier_operation, numberprovider, aequipmentslot, uuid);
         }
      }

      private static String operationToString(AttributeModifier.Operation attributemodifier_operation) {
         switch (attributemodifier_operation) {
            case ADDITION:
               return "addition";
            case MULTIPLY_BASE:
               return "multiply_base";
            case MULTIPLY_TOTAL:
               return "multiply_total";
            default:
               throw new IllegalArgumentException("Unknown operation " + attributemodifier_operation);
         }
      }

      private static AttributeModifier.Operation operationFromString(String s) {
         switch (s) {
            case "addition":
               return AttributeModifier.Operation.ADDITION;
            case "multiply_base":
               return AttributeModifier.Operation.MULTIPLY_BASE;
            case "multiply_total":
               return AttributeModifier.Operation.MULTIPLY_TOTAL;
            default:
               throw new JsonSyntaxException("Unknown attribute modifier operation " + s);
         }
      }
   }

   public static class ModifierBuilder {
      private final String name;
      private final Attribute attribute;
      private final AttributeModifier.Operation operation;
      private final NumberProvider amount;
      @Nullable
      private UUID id;
      private final Set<EquipmentSlot> slots = EnumSet.noneOf(EquipmentSlot.class);

      public ModifierBuilder(String s, Attribute attribute, AttributeModifier.Operation attributemodifier_operation, NumberProvider numberprovider) {
         this.name = s;
         this.attribute = attribute;
         this.operation = attributemodifier_operation;
         this.amount = numberprovider;
      }

      public SetAttributesFunction.ModifierBuilder forSlot(EquipmentSlot equipmentslot) {
         this.slots.add(equipmentslot);
         return this;
      }

      public SetAttributesFunction.ModifierBuilder withUuid(UUID uuid) {
         this.id = uuid;
         return this;
      }

      public SetAttributesFunction.Modifier build() {
         return new SetAttributesFunction.Modifier(this.name, this.attribute, this.operation, this.amount, this.slots.toArray(new EquipmentSlot[0]), this.id);
      }
   }

   public static class Serializer extends LootItemConditionalFunction.Serializer<SetAttributesFunction> {
      public void serialize(JsonObject jsonobject, SetAttributesFunction setattributesfunction, JsonSerializationContext jsonserializationcontext) {
         super.serialize(jsonobject, setattributesfunction, jsonserializationcontext);
         JsonArray jsonarray = new JsonArray();

         for(SetAttributesFunction.Modifier setattributesfunction_modifier : setattributesfunction.modifiers) {
            jsonarray.add(setattributesfunction_modifier.serialize(jsonserializationcontext));
         }

         jsonobject.add("modifiers", jsonarray);
      }

      public SetAttributesFunction deserialize(JsonObject jsonobject, JsonDeserializationContext jsondeserializationcontext, LootItemCondition[] alootitemcondition) {
         JsonArray jsonarray = GsonHelper.getAsJsonArray(jsonobject, "modifiers");
         List<SetAttributesFunction.Modifier> list = Lists.newArrayListWithExpectedSize(jsonarray.size());

         for(JsonElement jsonelement : jsonarray) {
            list.add(SetAttributesFunction.Modifier.deserialize(GsonHelper.convertToJsonObject(jsonelement, "modifier"), jsondeserializationcontext));
         }

         if (list.isEmpty()) {
            throw new JsonSyntaxException("Invalid attribute modifiers array; cannot be empty");
         } else {
            return new SetAttributesFunction(alootitemcondition, list);
         }
      }
   }
}
