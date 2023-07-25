package net.minecraft.advancements.critereon;

import com.google.gson.JsonObject;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.level.storage.loot.LootContext;

public class SummonedEntityTrigger extends SimpleCriterionTrigger<SummonedEntityTrigger.TriggerInstance> {
   static final ResourceLocation ID = new ResourceLocation("summoned_entity");

   public ResourceLocation getId() {
      return ID;
   }

   public SummonedEntityTrigger.TriggerInstance createInstance(JsonObject jsonobject, ContextAwarePredicate contextawarepredicate, DeserializationContext deserializationcontext) {
      ContextAwarePredicate contextawarepredicate1 = EntityPredicate.fromJson(jsonobject, "entity", deserializationcontext);
      return new SummonedEntityTrigger.TriggerInstance(contextawarepredicate, contextawarepredicate1);
   }

   public void trigger(ServerPlayer serverplayer, Entity entity) {
      LootContext lootcontext = EntityPredicate.createContext(serverplayer, entity);
      this.trigger(serverplayer, (summonedentitytrigger_triggerinstance) -> summonedentitytrigger_triggerinstance.matches(lootcontext));
   }

   public static class TriggerInstance extends AbstractCriterionTriggerInstance {
      private final ContextAwarePredicate entity;

      public TriggerInstance(ContextAwarePredicate contextawarepredicate, ContextAwarePredicate contextawarepredicate1) {
         super(SummonedEntityTrigger.ID, contextawarepredicate);
         this.entity = contextawarepredicate1;
      }

      public static SummonedEntityTrigger.TriggerInstance summonedEntity(EntityPredicate.Builder entitypredicate_builder) {
         return new SummonedEntityTrigger.TriggerInstance(ContextAwarePredicate.ANY, EntityPredicate.wrap(entitypredicate_builder.build()));
      }

      public boolean matches(LootContext lootcontext) {
         return this.entity.matches(lootcontext);
      }

      public JsonObject serializeToJson(SerializationContext serializationcontext) {
         JsonObject jsonobject = super.serializeToJson(serializationcontext);
         jsonobject.add("entity", this.entity.toJson(serializationcontext));
         return jsonobject;
      }
   }
}
