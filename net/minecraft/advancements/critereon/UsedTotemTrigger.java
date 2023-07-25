package net.minecraft.advancements.critereon;

import com.google.gson.JsonObject;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.ItemLike;

public class UsedTotemTrigger extends SimpleCriterionTrigger<UsedTotemTrigger.TriggerInstance> {
   static final ResourceLocation ID = new ResourceLocation("used_totem");

   public ResourceLocation getId() {
      return ID;
   }

   public UsedTotemTrigger.TriggerInstance createInstance(JsonObject jsonobject, ContextAwarePredicate contextawarepredicate, DeserializationContext deserializationcontext) {
      ItemPredicate itempredicate = ItemPredicate.fromJson(jsonobject.get("item"));
      return new UsedTotemTrigger.TriggerInstance(contextawarepredicate, itempredicate);
   }

   public void trigger(ServerPlayer serverplayer, ItemStack itemstack) {
      this.trigger(serverplayer, (usedtotemtrigger_triggerinstance) -> usedtotemtrigger_triggerinstance.matches(itemstack));
   }

   public static class TriggerInstance extends AbstractCriterionTriggerInstance {
      private final ItemPredicate item;

      public TriggerInstance(ContextAwarePredicate contextawarepredicate, ItemPredicate itempredicate) {
         super(UsedTotemTrigger.ID, contextawarepredicate);
         this.item = itempredicate;
      }

      public static UsedTotemTrigger.TriggerInstance usedTotem(ItemPredicate itempredicate) {
         return new UsedTotemTrigger.TriggerInstance(ContextAwarePredicate.ANY, itempredicate);
      }

      public static UsedTotemTrigger.TriggerInstance usedTotem(ItemLike itemlike) {
         return new UsedTotemTrigger.TriggerInstance(ContextAwarePredicate.ANY, ItemPredicate.Builder.item().of(itemlike).build());
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
