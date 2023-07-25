package net.minecraft.advancements.critereon;

import com.google.gson.JsonObject;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.animal.Animal;
import net.minecraft.world.level.storage.loot.LootContext;

public class TameAnimalTrigger extends SimpleCriterionTrigger<TameAnimalTrigger.TriggerInstance> {
   static final ResourceLocation ID = new ResourceLocation("tame_animal");

   public ResourceLocation getId() {
      return ID;
   }

   public TameAnimalTrigger.TriggerInstance createInstance(JsonObject jsonobject, ContextAwarePredicate contextawarepredicate, DeserializationContext deserializationcontext) {
      ContextAwarePredicate contextawarepredicate1 = EntityPredicate.fromJson(jsonobject, "entity", deserializationcontext);
      return new TameAnimalTrigger.TriggerInstance(contextawarepredicate, contextawarepredicate1);
   }

   public void trigger(ServerPlayer serverplayer, Animal animal) {
      LootContext lootcontext = EntityPredicate.createContext(serverplayer, animal);
      this.trigger(serverplayer, (tameanimaltrigger_triggerinstance) -> tameanimaltrigger_triggerinstance.matches(lootcontext));
   }

   public static class TriggerInstance extends AbstractCriterionTriggerInstance {
      private final ContextAwarePredicate entity;

      public TriggerInstance(ContextAwarePredicate contextawarepredicate, ContextAwarePredicate contextawarepredicate1) {
         super(TameAnimalTrigger.ID, contextawarepredicate);
         this.entity = contextawarepredicate1;
      }

      public static TameAnimalTrigger.TriggerInstance tamedAnimal() {
         return new TameAnimalTrigger.TriggerInstance(ContextAwarePredicate.ANY, ContextAwarePredicate.ANY);
      }

      public static TameAnimalTrigger.TriggerInstance tamedAnimal(EntityPredicate entitypredicate) {
         return new TameAnimalTrigger.TriggerInstance(ContextAwarePredicate.ANY, EntityPredicate.wrap(entitypredicate));
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
