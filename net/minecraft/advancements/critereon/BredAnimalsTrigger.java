package net.minecraft.advancements.critereon;

import com.google.gson.JsonObject;
import javax.annotation.Nullable;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.AgeableMob;
import net.minecraft.world.entity.animal.Animal;
import net.minecraft.world.level.storage.loot.LootContext;

public class BredAnimalsTrigger extends SimpleCriterionTrigger<BredAnimalsTrigger.TriggerInstance> {
   static final ResourceLocation ID = new ResourceLocation("bred_animals");

   public ResourceLocation getId() {
      return ID;
   }

   public BredAnimalsTrigger.TriggerInstance createInstance(JsonObject jsonobject, ContextAwarePredicate contextawarepredicate, DeserializationContext deserializationcontext) {
      ContextAwarePredicate contextawarepredicate1 = EntityPredicate.fromJson(jsonobject, "parent", deserializationcontext);
      ContextAwarePredicate contextawarepredicate2 = EntityPredicate.fromJson(jsonobject, "partner", deserializationcontext);
      ContextAwarePredicate contextawarepredicate3 = EntityPredicate.fromJson(jsonobject, "child", deserializationcontext);
      return new BredAnimalsTrigger.TriggerInstance(contextawarepredicate, contextawarepredicate1, contextawarepredicate2, contextawarepredicate3);
   }

   public void trigger(ServerPlayer serverplayer, Animal animal, Animal animal1, @Nullable AgeableMob ageablemob) {
      LootContext lootcontext = EntityPredicate.createContext(serverplayer, animal);
      LootContext lootcontext1 = EntityPredicate.createContext(serverplayer, animal1);
      LootContext lootcontext2 = ageablemob != null ? EntityPredicate.createContext(serverplayer, ageablemob) : null;
      this.trigger(serverplayer, (bredanimalstrigger_triggerinstance) -> bredanimalstrigger_triggerinstance.matches(lootcontext, lootcontext1, lootcontext2));
   }

   public static class TriggerInstance extends AbstractCriterionTriggerInstance {
      private final ContextAwarePredicate parent;
      private final ContextAwarePredicate partner;
      private final ContextAwarePredicate child;

      public TriggerInstance(ContextAwarePredicate contextawarepredicate, ContextAwarePredicate contextawarepredicate1, ContextAwarePredicate contextawarepredicate2, ContextAwarePredicate contextawarepredicate3) {
         super(BredAnimalsTrigger.ID, contextawarepredicate);
         this.parent = contextawarepredicate1;
         this.partner = contextawarepredicate2;
         this.child = contextawarepredicate3;
      }

      public static BredAnimalsTrigger.TriggerInstance bredAnimals() {
         return new BredAnimalsTrigger.TriggerInstance(ContextAwarePredicate.ANY, ContextAwarePredicate.ANY, ContextAwarePredicate.ANY, ContextAwarePredicate.ANY);
      }

      public static BredAnimalsTrigger.TriggerInstance bredAnimals(EntityPredicate.Builder entitypredicate_builder) {
         return new BredAnimalsTrigger.TriggerInstance(ContextAwarePredicate.ANY, ContextAwarePredicate.ANY, ContextAwarePredicate.ANY, EntityPredicate.wrap(entitypredicate_builder.build()));
      }

      public static BredAnimalsTrigger.TriggerInstance bredAnimals(EntityPredicate entitypredicate, EntityPredicate entitypredicate1, EntityPredicate entitypredicate2) {
         return new BredAnimalsTrigger.TriggerInstance(ContextAwarePredicate.ANY, EntityPredicate.wrap(entitypredicate), EntityPredicate.wrap(entitypredicate1), EntityPredicate.wrap(entitypredicate2));
      }

      public boolean matches(LootContext lootcontext, LootContext lootcontext1, @Nullable LootContext lootcontext2) {
         if (this.child == ContextAwarePredicate.ANY || lootcontext2 != null && this.child.matches(lootcontext2)) {
            return this.parent.matches(lootcontext) && this.partner.matches(lootcontext1) || this.parent.matches(lootcontext1) && this.partner.matches(lootcontext);
         } else {
            return false;
         }
      }

      public JsonObject serializeToJson(SerializationContext serializationcontext) {
         JsonObject jsonobject = super.serializeToJson(serializationcontext);
         jsonobject.add("parent", this.parent.toJson(serializationcontext));
         jsonobject.add("partner", this.partner.toJson(serializationcontext));
         jsonobject.add("child", this.child.toJson(serializationcontext));
         return jsonobject;
      }
   }
}
