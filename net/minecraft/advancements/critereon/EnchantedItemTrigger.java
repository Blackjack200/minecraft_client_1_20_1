package net.minecraft.advancements.critereon;

import com.google.gson.JsonObject;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.item.ItemStack;

public class EnchantedItemTrigger extends SimpleCriterionTrigger<EnchantedItemTrigger.TriggerInstance> {
   static final ResourceLocation ID = new ResourceLocation("enchanted_item");

   public ResourceLocation getId() {
      return ID;
   }

   public EnchantedItemTrigger.TriggerInstance createInstance(JsonObject jsonobject, ContextAwarePredicate contextawarepredicate, DeserializationContext deserializationcontext) {
      ItemPredicate itempredicate = ItemPredicate.fromJson(jsonobject.get("item"));
      MinMaxBounds.Ints minmaxbounds_ints = MinMaxBounds.Ints.fromJson(jsonobject.get("levels"));
      return new EnchantedItemTrigger.TriggerInstance(contextawarepredicate, itempredicate, minmaxbounds_ints);
   }

   public void trigger(ServerPlayer serverplayer, ItemStack itemstack, int i) {
      this.trigger(serverplayer, (enchanteditemtrigger_triggerinstance) -> enchanteditemtrigger_triggerinstance.matches(itemstack, i));
   }

   public static class TriggerInstance extends AbstractCriterionTriggerInstance {
      private final ItemPredicate item;
      private final MinMaxBounds.Ints levels;

      public TriggerInstance(ContextAwarePredicate contextawarepredicate, ItemPredicate itempredicate, MinMaxBounds.Ints minmaxbounds_ints) {
         super(EnchantedItemTrigger.ID, contextawarepredicate);
         this.item = itempredicate;
         this.levels = minmaxbounds_ints;
      }

      public static EnchantedItemTrigger.TriggerInstance enchantedItem() {
         return new EnchantedItemTrigger.TriggerInstance(ContextAwarePredicate.ANY, ItemPredicate.ANY, MinMaxBounds.Ints.ANY);
      }

      public boolean matches(ItemStack itemstack, int i) {
         if (!this.item.matches(itemstack)) {
            return false;
         } else {
            return this.levels.matches(i);
         }
      }

      public JsonObject serializeToJson(SerializationContext serializationcontext) {
         JsonObject jsonobject = super.serializeToJson(serializationcontext);
         jsonobject.add("item", this.item.serializeToJson());
         jsonobject.add("levels", this.levels.serializeToJson());
         return jsonobject;
      }
   }
}
