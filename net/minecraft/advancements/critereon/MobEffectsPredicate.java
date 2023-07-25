package net.minecraft.advancements.critereon;

import com.google.common.collect.Maps;
import com.google.gson.JsonElement;
import com.google.gson.JsonNull;
import com.google.gson.JsonObject;
import com.google.gson.JsonSyntaxException;
import java.util.Collections;
import java.util.Map;
import javax.annotation.Nullable;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.GsonHelper;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;

public class MobEffectsPredicate {
   public static final MobEffectsPredicate ANY = new MobEffectsPredicate(Collections.emptyMap());
   private final Map<MobEffect, MobEffectsPredicate.MobEffectInstancePredicate> effects;

   public MobEffectsPredicate(Map<MobEffect, MobEffectsPredicate.MobEffectInstancePredicate> map) {
      this.effects = map;
   }

   public static MobEffectsPredicate effects() {
      return new MobEffectsPredicate(Maps.newLinkedHashMap());
   }

   public MobEffectsPredicate and(MobEffect mobeffect) {
      this.effects.put(mobeffect, new MobEffectsPredicate.MobEffectInstancePredicate());
      return this;
   }

   public MobEffectsPredicate and(MobEffect mobeffect, MobEffectsPredicate.MobEffectInstancePredicate mobeffectspredicate_mobeffectinstancepredicate) {
      this.effects.put(mobeffect, mobeffectspredicate_mobeffectinstancepredicate);
      return this;
   }

   public boolean matches(Entity entity) {
      if (this == ANY) {
         return true;
      } else {
         return entity instanceof LivingEntity ? this.matches(((LivingEntity)entity).getActiveEffectsMap()) : false;
      }
   }

   public boolean matches(LivingEntity livingentity) {
      return this == ANY ? true : this.matches(livingentity.getActiveEffectsMap());
   }

   public boolean matches(Map<MobEffect, MobEffectInstance> map) {
      if (this == ANY) {
         return true;
      } else {
         for(Map.Entry<MobEffect, MobEffectsPredicate.MobEffectInstancePredicate> map_entry : this.effects.entrySet()) {
            MobEffectInstance mobeffectinstance = map.get(map_entry.getKey());
            if (!map_entry.getValue().matches(mobeffectinstance)) {
               return false;
            }
         }

         return true;
      }
   }

   public static MobEffectsPredicate fromJson(@Nullable JsonElement jsonelement) {
      if (jsonelement != null && !jsonelement.isJsonNull()) {
         JsonObject jsonobject = GsonHelper.convertToJsonObject(jsonelement, "effects");
         Map<MobEffect, MobEffectsPredicate.MobEffectInstancePredicate> map = Maps.newLinkedHashMap();

         for(Map.Entry<String, JsonElement> map_entry : jsonobject.entrySet()) {
            ResourceLocation resourcelocation = new ResourceLocation(map_entry.getKey());
            MobEffect mobeffect = BuiltInRegistries.MOB_EFFECT.getOptional(resourcelocation).orElseThrow(() -> new JsonSyntaxException("Unknown effect '" + resourcelocation + "'"));
            MobEffectsPredicate.MobEffectInstancePredicate mobeffectspredicate_mobeffectinstancepredicate = MobEffectsPredicate.MobEffectInstancePredicate.fromJson(GsonHelper.convertToJsonObject(map_entry.getValue(), map_entry.getKey()));
            map.put(mobeffect, mobeffectspredicate_mobeffectinstancepredicate);
         }

         return new MobEffectsPredicate(map);
      } else {
         return ANY;
      }
   }

   public JsonElement serializeToJson() {
      if (this == ANY) {
         return JsonNull.INSTANCE;
      } else {
         JsonObject jsonobject = new JsonObject();

         for(Map.Entry<MobEffect, MobEffectsPredicate.MobEffectInstancePredicate> map_entry : this.effects.entrySet()) {
            jsonobject.add(BuiltInRegistries.MOB_EFFECT.getKey(map_entry.getKey()).toString(), map_entry.getValue().serializeToJson());
         }

         return jsonobject;
      }
   }

   public static class MobEffectInstancePredicate {
      private final MinMaxBounds.Ints amplifier;
      private final MinMaxBounds.Ints duration;
      @Nullable
      private final Boolean ambient;
      @Nullable
      private final Boolean visible;

      public MobEffectInstancePredicate(MinMaxBounds.Ints minmaxbounds_ints, MinMaxBounds.Ints minmaxbounds_ints1, @Nullable Boolean obool, @Nullable Boolean obool1) {
         this.amplifier = minmaxbounds_ints;
         this.duration = minmaxbounds_ints1;
         this.ambient = obool;
         this.visible = obool1;
      }

      public MobEffectInstancePredicate() {
         this(MinMaxBounds.Ints.ANY, MinMaxBounds.Ints.ANY, (Boolean)null, (Boolean)null);
      }

      public boolean matches(@Nullable MobEffectInstance mobeffectinstance) {
         if (mobeffectinstance == null) {
            return false;
         } else if (!this.amplifier.matches(mobeffectinstance.getAmplifier())) {
            return false;
         } else if (!this.duration.matches(mobeffectinstance.getDuration())) {
            return false;
         } else if (this.ambient != null && this.ambient != mobeffectinstance.isAmbient()) {
            return false;
         } else {
            return this.visible == null || this.visible == mobeffectinstance.isVisible();
         }
      }

      public JsonElement serializeToJson() {
         JsonObject jsonobject = new JsonObject();
         jsonobject.add("amplifier", this.amplifier.serializeToJson());
         jsonobject.add("duration", this.duration.serializeToJson());
         jsonobject.addProperty("ambient", this.ambient);
         jsonobject.addProperty("visible", this.visible);
         return jsonobject;
      }

      public static MobEffectsPredicate.MobEffectInstancePredicate fromJson(JsonObject jsonobject) {
         MinMaxBounds.Ints minmaxbounds_ints = MinMaxBounds.Ints.fromJson(jsonobject.get("amplifier"));
         MinMaxBounds.Ints minmaxbounds_ints1 = MinMaxBounds.Ints.fromJson(jsonobject.get("duration"));
         Boolean obool = jsonobject.has("ambient") ? GsonHelper.getAsBoolean(jsonobject, "ambient") : null;
         Boolean obool1 = jsonobject.has("visible") ? GsonHelper.getAsBoolean(jsonobject, "visible") : null;
         return new MobEffectsPredicate.MobEffectInstancePredicate(minmaxbounds_ints, minmaxbounds_ints1, obool, obool1);
      }
   }
}
