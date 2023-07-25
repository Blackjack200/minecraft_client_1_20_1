package net.minecraft.advancements.critereon;

import com.google.gson.JsonObject;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.item.ItemStack;

public class FilledBucketTrigger extends SimpleCriterionTrigger<FilledBucketTrigger.TriggerInstance> {
   static final ResourceLocation ID = new ResourceLocation("filled_bucket");

   public ResourceLocation getId() {
      return ID;
   }

   public FilledBucketTrigger.TriggerInstance createInstance(JsonObject jsonobject, ContextAwarePredicate contextawarepredicate, DeserializationContext deserializationcontext) {
      ItemPredicate itempredicate = ItemPredicate.fromJson(jsonobject.get("item"));
      return new FilledBucketTrigger.TriggerInstance(contextawarepredicate, itempredicate);
   }

   public void trigger(ServerPlayer serverplayer, ItemStack itemstack) {
      this.trigger(serverplayer, (filledbuckettrigger_triggerinstance) -> filledbuckettrigger_triggerinstance.matches(itemstack));
   }

   public static class TriggerInstance extends AbstractCriterionTriggerInstance {
      private final ItemPredicate item;

      public TriggerInstance(ContextAwarePredicate contextawarepredicate, ItemPredicate itempredicate) {
         super(FilledBucketTrigger.ID, contextawarepredicate);
         this.item = itempredicate;
      }

      public static FilledBucketTrigger.TriggerInstance filledBucket(ItemPredicate itempredicate) {
         return new FilledBucketTrigger.TriggerInstance(ContextAwarePredicate.ANY, itempredicate);
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
