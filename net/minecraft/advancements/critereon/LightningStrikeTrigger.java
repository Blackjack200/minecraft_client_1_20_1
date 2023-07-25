package net.minecraft.advancements.critereon;

import com.google.gson.JsonObject;
import java.util.List;
import java.util.stream.Collectors;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LightningBolt;
import net.minecraft.world.level.storage.loot.LootContext;

public class LightningStrikeTrigger extends SimpleCriterionTrigger<LightningStrikeTrigger.TriggerInstance> {
   static final ResourceLocation ID = new ResourceLocation("lightning_strike");

   public ResourceLocation getId() {
      return ID;
   }

   public LightningStrikeTrigger.TriggerInstance createInstance(JsonObject jsonobject, ContextAwarePredicate contextawarepredicate, DeserializationContext deserializationcontext) {
      ContextAwarePredicate contextawarepredicate1 = EntityPredicate.fromJson(jsonobject, "lightning", deserializationcontext);
      ContextAwarePredicate contextawarepredicate2 = EntityPredicate.fromJson(jsonobject, "bystander", deserializationcontext);
      return new LightningStrikeTrigger.TriggerInstance(contextawarepredicate, contextawarepredicate1, contextawarepredicate2);
   }

   public void trigger(ServerPlayer serverplayer, LightningBolt lightningbolt, List<Entity> list) {
      List<LootContext> list1 = list.stream().map((entity) -> EntityPredicate.createContext(serverplayer, entity)).collect(Collectors.toList());
      LootContext lootcontext = EntityPredicate.createContext(serverplayer, lightningbolt);
      this.trigger(serverplayer, (lightningstriketrigger_triggerinstance) -> lightningstriketrigger_triggerinstance.matches(lootcontext, list1));
   }

   public static class TriggerInstance extends AbstractCriterionTriggerInstance {
      private final ContextAwarePredicate lightning;
      private final ContextAwarePredicate bystander;

      public TriggerInstance(ContextAwarePredicate contextawarepredicate, ContextAwarePredicate contextawarepredicate1, ContextAwarePredicate contextawarepredicate2) {
         super(LightningStrikeTrigger.ID, contextawarepredicate);
         this.lightning = contextawarepredicate1;
         this.bystander = contextawarepredicate2;
      }

      public static LightningStrikeTrigger.TriggerInstance lighthingStrike(EntityPredicate entitypredicate, EntityPredicate entitypredicate1) {
         return new LightningStrikeTrigger.TriggerInstance(ContextAwarePredicate.ANY, EntityPredicate.wrap(entitypredicate), EntityPredicate.wrap(entitypredicate1));
      }

      public boolean matches(LootContext lootcontext, List<LootContext> list) {
         if (!this.lightning.matches(lootcontext)) {
            return false;
         } else {
            return this.bystander == ContextAwarePredicate.ANY || !list.stream().noneMatch(this.bystander::matches);
         }
      }

      public JsonObject serializeToJson(SerializationContext serializationcontext) {
         JsonObject jsonobject = super.serializeToJson(serializationcontext);
         jsonobject.add("lightning", this.lightning.toJson(serializationcontext));
         jsonobject.add("bystander", this.bystander.toJson(serializationcontext));
         return jsonobject;
      }
   }
}
