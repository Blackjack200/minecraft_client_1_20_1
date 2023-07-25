package net.minecraft.advancements.critereon;

import com.google.gson.JsonObject;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.storage.loot.LootContext;

public class PlayerInteractTrigger extends SimpleCriterionTrigger<PlayerInteractTrigger.TriggerInstance> {
   static final ResourceLocation ID = new ResourceLocation("player_interacted_with_entity");

   public ResourceLocation getId() {
      return ID;
   }

   protected PlayerInteractTrigger.TriggerInstance createInstance(JsonObject jsonobject, ContextAwarePredicate contextawarepredicate, DeserializationContext deserializationcontext) {
      ItemPredicate itempredicate = ItemPredicate.fromJson(jsonobject.get("item"));
      ContextAwarePredicate contextawarepredicate1 = EntityPredicate.fromJson(jsonobject, "entity", deserializationcontext);
      return new PlayerInteractTrigger.TriggerInstance(contextawarepredicate, itempredicate, contextawarepredicate1);
   }

   public void trigger(ServerPlayer serverplayer, ItemStack itemstack, Entity entity) {
      LootContext lootcontext = EntityPredicate.createContext(serverplayer, entity);
      this.trigger(serverplayer, (playerinteracttrigger_triggerinstance) -> playerinteracttrigger_triggerinstance.matches(itemstack, lootcontext));
   }

   public static class TriggerInstance extends AbstractCriterionTriggerInstance {
      private final ItemPredicate item;
      private final ContextAwarePredicate entity;

      public TriggerInstance(ContextAwarePredicate contextawarepredicate, ItemPredicate itempredicate, ContextAwarePredicate contextawarepredicate1) {
         super(PlayerInteractTrigger.ID, contextawarepredicate);
         this.item = itempredicate;
         this.entity = contextawarepredicate1;
      }

      public static PlayerInteractTrigger.TriggerInstance itemUsedOnEntity(ContextAwarePredicate contextawarepredicate, ItemPredicate.Builder itempredicate_builder, ContextAwarePredicate contextawarepredicate1) {
         return new PlayerInteractTrigger.TriggerInstance(contextawarepredicate, itempredicate_builder.build(), contextawarepredicate1);
      }

      public static PlayerInteractTrigger.TriggerInstance itemUsedOnEntity(ItemPredicate.Builder itempredicate_builder, ContextAwarePredicate contextawarepredicate) {
         return itemUsedOnEntity(ContextAwarePredicate.ANY, itempredicate_builder, contextawarepredicate);
      }

      public boolean matches(ItemStack itemstack, LootContext lootcontext) {
         return !this.item.matches(itemstack) ? false : this.entity.matches(lootcontext);
      }

      public JsonObject serializeToJson(SerializationContext serializationcontext) {
         JsonObject jsonobject = super.serializeToJson(serializationcontext);
         jsonobject.add("item", this.item.serializeToJson());
         jsonobject.add("entity", this.entity.toJson(serializationcontext));
         return jsonobject;
      }
   }
}
