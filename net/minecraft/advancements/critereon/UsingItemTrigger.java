package net.minecraft.advancements.critereon;

import com.google.gson.JsonObject;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.item.ItemStack;

public class UsingItemTrigger extends SimpleCriterionTrigger<UsingItemTrigger.TriggerInstance> {
   static final ResourceLocation ID = new ResourceLocation("using_item");

   public ResourceLocation getId() {
      return ID;
   }

   public UsingItemTrigger.TriggerInstance createInstance(JsonObject jsonobject, ContextAwarePredicate contextawarepredicate, DeserializationContext deserializationcontext) {
      ItemPredicate itempredicate = ItemPredicate.fromJson(jsonobject.get("item"));
      return new UsingItemTrigger.TriggerInstance(contextawarepredicate, itempredicate);
   }

   public void trigger(ServerPlayer serverplayer, ItemStack itemstack) {
      this.trigger(serverplayer, (usingitemtrigger_triggerinstance) -> usingitemtrigger_triggerinstance.matches(itemstack));
   }

   public static class TriggerInstance extends AbstractCriterionTriggerInstance {
      private final ItemPredicate item;

      public TriggerInstance(ContextAwarePredicate contextawarepredicate, ItemPredicate itempredicate) {
         super(UsingItemTrigger.ID, contextawarepredicate);
         this.item = itempredicate;
      }

      public static UsingItemTrigger.TriggerInstance lookingAt(EntityPredicate.Builder entitypredicate_builder, ItemPredicate.Builder itempredicate_builder) {
         return new UsingItemTrigger.TriggerInstance(EntityPredicate.wrap(entitypredicate_builder.build()), itempredicate_builder.build());
      }

      public boolean matches(ItemStack itemstack) {
         return this.item.matches(itemstack);
      }

      public JsonObject serializeToJson(SerializationContext serializationcontext) {
         JsonObject jsonobject = super.serializeToJson(serializationcontext);
         jsonobject.add("item", this.item.serializeToJson());
         return jsonobject;
      }
   }
}
