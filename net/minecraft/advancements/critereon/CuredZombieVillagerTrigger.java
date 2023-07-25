package net.minecraft.advancements.critereon;

import com.google.gson.JsonObject;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.monster.Zombie;
import net.minecraft.world.entity.npc.Villager;
import net.minecraft.world.level.storage.loot.LootContext;

public class CuredZombieVillagerTrigger extends SimpleCriterionTrigger<CuredZombieVillagerTrigger.TriggerInstance> {
   static final ResourceLocation ID = new ResourceLocation("cured_zombie_villager");

   public ResourceLocation getId() {
      return ID;
   }

   public CuredZombieVillagerTrigger.TriggerInstance createInstance(JsonObject jsonobject, ContextAwarePredicate contextawarepredicate, DeserializationContext deserializationcontext) {
      ContextAwarePredicate contextawarepredicate1 = EntityPredicate.fromJson(jsonobject, "zombie", deserializationcontext);
      ContextAwarePredicate contextawarepredicate2 = EntityPredicate.fromJson(jsonobject, "villager", deserializationcontext);
      return new CuredZombieVillagerTrigger.TriggerInstance(contextawarepredicate, contextawarepredicate1, contextawarepredicate2);
   }

   public void trigger(ServerPlayer serverplayer, Zombie zombie, Villager villager) {
      LootContext lootcontext = EntityPredicate.createContext(serverplayer, zombie);
      LootContext lootcontext1 = EntityPredicate.createContext(serverplayer, villager);
      this.trigger(serverplayer, (curedzombievillagertrigger_triggerinstance) -> curedzombievillagertrigger_triggerinstance.matches(lootcontext, lootcontext1));
   }

   public static class TriggerInstance extends AbstractCriterionTriggerInstance {
      private final ContextAwarePredicate zombie;
      private final ContextAwarePredicate villager;

      public TriggerInstance(ContextAwarePredicate contextawarepredicate, ContextAwarePredicate contextawarepredicate1, ContextAwarePredicate contextawarepredicate2) {
         super(CuredZombieVillagerTrigger.ID, contextawarepredicate);
         this.zombie = contextawarepredicate1;
         this.villager = contextawarepredicate2;
      }

      public static CuredZombieVillagerTrigger.TriggerInstance curedZombieVillager() {
         return new CuredZombieVillagerTrigger.TriggerInstance(ContextAwarePredicate.ANY, ContextAwarePredicate.ANY, ContextAwarePredicate.ANY);
      }

      public boolean matches(LootContext lootcontext, LootContext lootcontext1) {
         if (!this.zombie.matches(lootcontext)) {
            return false;
         } else {
            return this.villager.matches(lootcontext1);
         }
      }

      public JsonObject serializeToJson(SerializationContext serializationcontext) {
         JsonObject jsonobject = super.serializeToJson(serializationcontext);
         jsonobject.add("zombie", this.zombie.toJson(serializationcontext));
         jsonobject.add("villager", this.villager.toJson(serializationcontext));
         return jsonobject;
      }
   }
}
