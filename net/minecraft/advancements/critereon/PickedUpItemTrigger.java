package net.minecraft.advancements.critereon;

import com.google.gson.JsonObject;
import javax.annotation.Nullable;
import net.minecraft.advancements.CriteriaTriggers;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.storage.loot.LootContext;

public class PickedUpItemTrigger extends SimpleCriterionTrigger<PickedUpItemTrigger.TriggerInstance> {
   private final ResourceLocation id;

   public PickedUpItemTrigger(ResourceLocation resourcelocation) {
      this.id = resourcelocation;
   }

   public ResourceLocation getId() {
      return this.id;
   }

   protected PickedUpItemTrigger.TriggerInstance createInstance(JsonObject jsonobject, ContextAwarePredicate contextawarepredicate, DeserializationContext deserializationcontext) {
      ItemPredicate itempredicate = ItemPredicate.fromJson(jsonobject.get("item"));
      ContextAwarePredicate contextawarepredicate1 = EntityPredicate.fromJson(jsonobject, "entity", deserializationcontext);
      return new PickedUpItemTrigger.TriggerInstance(this.id, contextawarepredicate, itempredicate, contextawarepredicate1);
   }

   public void trigger(ServerPlayer serverplayer, ItemStack itemstack, @Nullable Entity entity) {
      LootContext lootcontext = EntityPredicate.createContext(serverplayer, entity);
      this.trigger(serverplayer, (pickedupitemtrigger_triggerinstance) -> pickedupitemtrigger_triggerinstance.matches(serverplayer, itemstack, lootcontext));
   }

   public static class TriggerInstance extends AbstractCriterionTriggerInstance {
      private final ItemPredicate item;
      private final ContextAwarePredicate entity;

      public TriggerInstance(ResourceLocation resourcelocation, ContextAwarePredicate contextawarepredicate, ItemPredicate itempredicate, ContextAwarePredicate contextawarepredicate1) {
         super(resourcelocation, contextawarepredicate);
         this.item = itempredicate;
         this.entity = contextawarepredicate1;
      }

      public static PickedUpItemTrigger.TriggerInstance thrownItemPickedUpByEntity(ContextAwarePredicate contextawarepredicate, ItemPredicate itempredicate, ContextAwarePredicate contextawarepredicate1) {
         return new PickedUpItemTrigger.TriggerInstance(CriteriaTriggers.THROWN_ITEM_PICKED_UP_BY_ENTITY.getId(), contextawarepredicate, itempredicate, contextawarepredicate1);
      }

      public static PickedUpItemTrigger.TriggerInstance thrownItemPickedUpByPlayer(ContextAwarePredicate contextawarepredicate, ItemPredicate itempredicate, ContextAwarePredicate contextawarepredicate1) {
         return new PickedUpItemTrigger.TriggerInstance(CriteriaTriggers.THROWN_ITEM_PICKED_UP_BY_PLAYER.getId(), contextawarepredicate, itempredicate, contextawarepredicate1);
      }

      public boolean matches(ServerPlayer serverplayer, ItemStack itemstack, LootContext lootcontext) {
         if (!this.item.matches(itemstack)) {
            return false;
         } else {
            return this.entity.matches(lootcontext);
         }
      }

      public JsonObject serializeToJson(SerializationContext serializationcontext) {
         JsonObject jsonobject = super.serializeToJson(serializationcontext);
         jsonobject.add("item", this.item.serializeToJson());
         jsonobject.add("entity", this.entity.toJson(serializationcontext));
         return jsonobject;
      }
   }
}
