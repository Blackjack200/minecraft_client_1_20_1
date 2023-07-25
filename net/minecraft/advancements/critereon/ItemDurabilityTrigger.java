package net.minecraft.advancements.critereon;

import com.google.gson.JsonObject;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.item.ItemStack;

public class ItemDurabilityTrigger extends SimpleCriterionTrigger<ItemDurabilityTrigger.TriggerInstance> {
   static final ResourceLocation ID = new ResourceLocation("item_durability_changed");

   public ResourceLocation getId() {
      return ID;
   }

   public ItemDurabilityTrigger.TriggerInstance createInstance(JsonObject jsonobject, ContextAwarePredicate contextawarepredicate, DeserializationContext deserializationcontext) {
      ItemPredicate itempredicate = ItemPredicate.fromJson(jsonobject.get("item"));
      MinMaxBounds.Ints minmaxbounds_ints = MinMaxBounds.Ints.fromJson(jsonobject.get("durability"));
      MinMaxBounds.Ints minmaxbounds_ints1 = MinMaxBounds.Ints.fromJson(jsonobject.get("delta"));
      return new ItemDurabilityTrigger.TriggerInstance(contextawarepredicate, itempredicate, minmaxbounds_ints, minmaxbounds_ints1);
   }

   public void trigger(ServerPlayer serverplayer, ItemStack itemstack, int i) {
      this.trigger(serverplayer, (itemdurabilitytrigger_triggerinstance) -> itemdurabilitytrigger_triggerinstance.matches(itemstack, i));
   }

   public static class TriggerInstance extends AbstractCriterionTriggerInstance {
      private final ItemPredicate item;
      private final MinMaxBounds.Ints durability;
      private final MinMaxBounds.Ints delta;

      public TriggerInstance(ContextAwarePredicate contextawarepredicate, ItemPredicate itempredicate, MinMaxBounds.Ints minmaxbounds_ints, MinMaxBounds.Ints minmaxbounds_ints1) {
         super(ItemDurabilityTrigger.ID, contextawarepredicate);
         this.item = itempredicate;
         this.durability = minmaxbounds_ints;
         this.delta = minmaxbounds_ints1;
      }

      public static ItemDurabilityTrigger.TriggerInstance changedDurability(ItemPredicate itempredicate, MinMaxBounds.Ints minmaxbounds_ints) {
         return changedDurability(ContextAwarePredicate.ANY, itempredicate, minmaxbounds_ints);
      }

      public static ItemDurabilityTrigger.TriggerInstance changedDurability(ContextAwarePredicate contextawarepredicate, ItemPredicate itempredicate, MinMaxBounds.Ints minmaxbounds_ints) {
         return new ItemDurabilityTrigger.TriggerInstance(contextawarepredicate, itempredicate, minmaxbounds_ints, MinMaxBounds.Ints.ANY);
      }

      public boolean matches(ItemStack itemstack, int i) {
         if (!this.item.matches(itemstack)) {
            return false;
         } else if (!this.durability.matches(itemstack.getMaxDamage() - i)) {
            return false;
         } else {
            return this.delta.matches(itemstack.getDamageValue() - i);
         }
      }

      public JsonObject serializeToJson(SerializationContext serializationcontext) {
         JsonObject jsonobject = super.serializeToJson(serializationcontext);
         jsonobject.add("item", this.item.serializeToJson());
         jsonobject.add("durability", this.durability.serializeToJson());
         jsonobject.add("delta", this.delta.serializeToJson());
         return jsonobject;
      }
   }
}
