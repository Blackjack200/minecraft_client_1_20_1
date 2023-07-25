package net.minecraft.advancements.critereon;

import com.google.gson.JsonObject;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.ItemLike;

public class ShotCrossbowTrigger extends SimpleCriterionTrigger<ShotCrossbowTrigger.TriggerInstance> {
   static final ResourceLocation ID = new ResourceLocation("shot_crossbow");

   public ResourceLocation getId() {
      return ID;
   }

   public ShotCrossbowTrigger.TriggerInstance createInstance(JsonObject jsonobject, ContextAwarePredicate contextawarepredicate, DeserializationContext deserializationcontext) {
      ItemPredicate itempredicate = ItemPredicate.fromJson(jsonobject.get("item"));
      return new ShotCrossbowTrigger.TriggerInstance(contextawarepredicate, itempredicate);
   }

   public void trigger(ServerPlayer serverplayer, ItemStack itemstack) {
      this.trigger(serverplayer, (shotcrossbowtrigger_triggerinstance) -> shotcrossbowtrigger_triggerinstance.matches(itemstack));
   }

   public static class TriggerInstance extends AbstractCriterionTriggerInstance {
      private final ItemPredicate item;

      public TriggerInstance(ContextAwarePredicate contextawarepredicate, ItemPredicate itempredicate) {
         super(ShotCrossbowTrigger.ID, contextawarepredicate);
         this.item = itempredicate;
      }

      public static ShotCrossbowTrigger.TriggerInstance shotCrossbow(ItemPredicate itempredicate) {
         return new ShotCrossbowTrigger.TriggerInstance(ContextAwarePredicate.ANY, itempredicate);
      }

      public static ShotCrossbowTrigger.TriggerInstance shotCrossbow(ItemLike itemlike) {
         return new ShotCrossbowTrigger.TriggerInstance(ContextAwarePredicate.ANY, ItemPredicate.Builder.item().of(itemlike).build());
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
