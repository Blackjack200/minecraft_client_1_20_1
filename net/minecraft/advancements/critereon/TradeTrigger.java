package net.minecraft.advancements.critereon;

import com.google.gson.JsonObject;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.npc.AbstractVillager;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.storage.loot.LootContext;

public class TradeTrigger extends SimpleCriterionTrigger<TradeTrigger.TriggerInstance> {
   static final ResourceLocation ID = new ResourceLocation("villager_trade");

   public ResourceLocation getId() {
      return ID;
   }

   public TradeTrigger.TriggerInstance createInstance(JsonObject jsonobject, ContextAwarePredicate contextawarepredicate, DeserializationContext deserializationcontext) {
      ContextAwarePredicate contextawarepredicate1 = EntityPredicate.fromJson(jsonobject, "villager", deserializationcontext);
      ItemPredicate itempredicate = ItemPredicate.fromJson(jsonobject.get("item"));
      return new TradeTrigger.TriggerInstance(contextawarepredicate, contextawarepredicate1, itempredicate);
   }

   public void trigger(ServerPlayer serverplayer, AbstractVillager abstractvillager, ItemStack itemstack) {
      LootContext lootcontext = EntityPredicate.createContext(serverplayer, abstractvillager);
      this.trigger(serverplayer, (tradetrigger_triggerinstance) -> tradetrigger_triggerinstance.matches(lootcontext, itemstack));
   }

   public static class TriggerInstance extends AbstractCriterionTriggerInstance {
      private final ContextAwarePredicate villager;
      private final ItemPredicate item;

      public TriggerInstance(ContextAwarePredicate contextawarepredicate, ContextAwarePredicate contextawarepredicate1, ItemPredicate itempredicate) {
         super(TradeTrigger.ID, contextawarepredicate);
         this.villager = contextawarepredicate1;
         this.item = itempredicate;
      }

      public static TradeTrigger.TriggerInstance tradedWithVillager() {
         return new TradeTrigger.TriggerInstance(ContextAwarePredicate.ANY, ContextAwarePredicate.ANY, ItemPredicate.ANY);
      }

      public static TradeTrigger.TriggerInstance tradedWithVillager(EntityPredicate.Builder entitypredicate_builder) {
         return new TradeTrigger.TriggerInstance(EntityPredicate.wrap(entitypredicate_builder.build()), ContextAwarePredicate.ANY, ItemPredicate.ANY);
      }

      public boolean matches(LootContext lootcontext, ItemStack itemstack) {
         if (!this.villager.matches(lootcontext)) {
            return false;
         } else {
            return this.item.matches(itemstack);
         }
      }

      public JsonObject serializeToJson(SerializationContext serializationcontext) {
         JsonObject jsonobject = super.serializeToJson(serializationcontext);
         jsonobject.add("item", this.item.serializeToJson());
         jsonobject.add("villager", this.villager.toJson(serializationcontext));
         return jsonobject;
      }
   }
}
