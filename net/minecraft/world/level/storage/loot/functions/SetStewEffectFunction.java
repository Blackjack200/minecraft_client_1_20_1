package net.minecraft.world.level.storage.loot.functions;

import com.google.common.collect.ImmutableMap;
import com.google.common.collect.ImmutableSet;
import com.google.common.collect.Iterables;
import com.google.common.collect.Maps;
import com.google.gson.JsonArray;
import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonPrimitive;
import com.google.gson.JsonSerializationContext;
import com.google.gson.JsonSyntaxException;
import java.util.Map;
import java.util.Set;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.GsonHelper;
import net.minecraft.util.RandomSource;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.SuspiciousStewItem;
import net.minecraft.world.level.storage.loot.LootContext;
import net.minecraft.world.level.storage.loot.parameters.LootContextParam;
import net.minecraft.world.level.storage.loot.predicates.LootItemCondition;
import net.minecraft.world.level.storage.loot.providers.number.NumberProvider;

public class SetStewEffectFunction extends LootItemConditionalFunction {
   final Map<MobEffect, NumberProvider> effectDurationMap;

   SetStewEffectFunction(LootItemCondition[] alootitemcondition, Map<MobEffect, NumberProvider> map) {
      super(alootitemcondition);
      this.effectDurationMap = ImmutableMap.copyOf(map);
   }

   public LootItemFunctionType getType() {
      return LootItemFunctions.SET_STEW_EFFECT;
   }

   public Set<LootContextParam<?>> getReferencedContextParams() {
      return this.effectDurationMap.values().stream().flatMap((numberprovider) -> numberprovider.getReferencedContextParams().stream()).collect(ImmutableSet.toImmutableSet());
   }

   public ItemStack run(ItemStack itemstack, LootContext lootcontext) {
      if (itemstack.is(Items.SUSPICIOUS_STEW) && !this.effectDurationMap.isEmpty()) {
         RandomSource randomsource = lootcontext.getRandom();
         int i = randomsource.nextInt(this.effectDurationMap.size());
         Map.Entry<MobEffect, NumberProvider> map_entry = Iterables.get(this.effectDurationMap.entrySet(), i);
         MobEffect mobeffect = map_entry.getKey();
         int j = map_entry.getValue().getInt(lootcontext);
         if (!mobeffect.isInstantenous()) {
            j *= 20;
         }

         SuspiciousStewItem.saveMobEffect(itemstack, mobeffect, j);
         return itemstack;
      } else {
         return itemstack;
      }
   }

   public static SetStewEffectFunction.Builder stewEffect() {
      return new SetStewEffectFunction.Builder();
   }

   public static class Builder extends LootItemConditionalFunction.Builder<SetStewEffectFunction.Builder> {
      private final Map<MobEffect, NumberProvider> effectDurationMap = Maps.newLinkedHashMap();

      protected SetStewEffectFunction.Builder getThis() {
         return this;
      }

      public SetStewEffectFunction.Builder withEffect(MobEffect mobeffect, NumberProvider numberprovider) {
         this.effectDurationMap.put(mobeffect, numberprovider);
         return this;
      }

      public LootItemFunction build() {
         return new SetStewEffectFunction(this.getConditions(), this.effectDurationMap);
      }
   }

   public static class Serializer extends LootItemConditionalFunction.Serializer<SetStewEffectFunction> {
      public void serialize(JsonObject jsonobject, SetStewEffectFunction setsteweffectfunction, JsonSerializationContext jsonserializationcontext) {
         super.serialize(jsonobject, setsteweffectfunction, jsonserializationcontext);
         if (!setsteweffectfunction.effectDurationMap.isEmpty()) {
            JsonArray jsonarray = new JsonArray();

            for(MobEffect mobeffect : setsteweffectfunction.effectDurationMap.keySet()) {
               JsonObject jsonobject1 = new JsonObject();
               ResourceLocation resourcelocation = BuiltInRegistries.MOB_EFFECT.getKey(mobeffect);
               if (resourcelocation == null) {
                  throw new IllegalArgumentException("Don't know how to serialize mob effect " + mobeffect);
               }

               jsonobject1.add("type", new JsonPrimitive(resourcelocation.toString()));
               jsonobject1.add("duration", jsonserializationcontext.serialize(setsteweffectfunction.effectDurationMap.get(mobeffect)));
               jsonarray.add(jsonobject1);
            }

            jsonobject.add("effects", jsonarray);
         }

      }

      public SetStewEffectFunction deserialize(JsonObject jsonobject, JsonDeserializationContext jsondeserializationcontext, LootItemCondition[] alootitemcondition) {
         Map<MobEffect, NumberProvider> map = Maps.newLinkedHashMap();
         if (jsonobject.has("effects")) {
            for(JsonElement jsonelement : GsonHelper.getAsJsonArray(jsonobject, "effects")) {
               String s = GsonHelper.getAsString(jsonelement.getAsJsonObject(), "type");
               MobEffect mobeffect = BuiltInRegistries.MOB_EFFECT.getOptional(new ResourceLocation(s)).orElseThrow(() -> new JsonSyntaxException("Unknown mob effect '" + s + "'"));
               NumberProvider numberprovider = GsonHelper.getAsObject(jsonelement.getAsJsonObject(), "duration", jsondeserializationcontext, NumberProvider.class);
               map.put(mobeffect, numberprovider);
            }
         }

         return new SetStewEffectFunction(alootitemcondition, map);
      }
   }
}
