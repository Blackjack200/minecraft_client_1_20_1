package net.minecraft.advancements.critereon;

import com.google.gson.JsonObject;
import javax.annotation.Nullable;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.level.storage.loot.LootContext;

public class EffectsChangedTrigger extends SimpleCriterionTrigger<EffectsChangedTrigger.TriggerInstance> {
   static final ResourceLocation ID = new ResourceLocation("effects_changed");

   public ResourceLocation getId() {
      return ID;
   }

   public EffectsChangedTrigger.TriggerInstance createInstance(JsonObject jsonobject, ContextAwarePredicate contextawarepredicate, DeserializationContext deserializationcontext) {
      MobEffectsPredicate mobeffectspredicate = MobEffectsPredicate.fromJson(jsonobject.get("effects"));
      ContextAwarePredicate contextawarepredicate1 = EntityPredicate.fromJson(jsonobject, "source", deserializationcontext);
      return new EffectsChangedTrigger.TriggerInstance(contextawarepredicate, mobeffectspredicate, contextawarepredicate1);
   }

   public void trigger(ServerPlayer serverplayer, @Nullable Entity entity) {
      LootContext lootcontext = entity != null ? EntityPredicate.createContext(serverplayer, entity) : null;
      this.trigger(serverplayer, (effectschangedtrigger_triggerinstance) -> effectschangedtrigger_triggerinstance.matches(serverplayer, lootcontext));
   }

   public static class TriggerInstance extends AbstractCriterionTriggerInstance {
      private final MobEffectsPredicate effects;
      private final ContextAwarePredicate source;

      public TriggerInstance(ContextAwarePredicate contextawarepredicate, MobEffectsPredicate mobeffectspredicate, ContextAwarePredicate contextawarepredicate1) {
         super(EffectsChangedTrigger.ID, contextawarepredicate);
         this.effects = mobeffectspredicate;
         this.source = contextawarepredicate1;
      }

      public static EffectsChangedTrigger.TriggerInstance hasEffects(MobEffectsPredicate mobeffectspredicate) {
         return new EffectsChangedTrigger.TriggerInstance(ContextAwarePredicate.ANY, mobeffectspredicate, ContextAwarePredicate.ANY);
      }

      public static EffectsChangedTrigger.TriggerInstance gotEffectsFrom(EntityPredicate entitypredicate) {
         return new EffectsChangedTrigger.TriggerInstance(ContextAwarePredicate.ANY, MobEffectsPredicate.ANY, EntityPredicate.wrap(entitypredicate));
      }

      public boolean matches(ServerPlayer serverplayer, @Nullable LootContext lootcontext) {
         if (!this.effects.matches((LivingEntity)serverplayer)) {
            return false;
         } else {
            return this.source == ContextAwarePredicate.ANY || lootcontext != null && this.source.matches(lootcontext);
         }
      }

      public JsonObject serializeToJson(SerializationContext serializationcontext) {
         JsonObject jsonobject = super.serializeToJson(serializationcontext);
         jsonobject.add("effects", this.effects.serializeToJson());
         jsonobject.add("source", this.source.toJson(serializationcontext));
         return jsonobject;
      }
   }
}
