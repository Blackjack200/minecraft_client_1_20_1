package net.minecraft.advancements.critereon;

import com.google.gson.JsonObject;
import java.util.Collection;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.entity.projectile.FishingHook;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.storage.loot.LootContext;
import net.minecraft.world.level.storage.loot.parameters.LootContextParams;

public class FishingRodHookedTrigger extends SimpleCriterionTrigger<FishingRodHookedTrigger.TriggerInstance> {
   static final ResourceLocation ID = new ResourceLocation("fishing_rod_hooked");

   public ResourceLocation getId() {
      return ID;
   }

   public FishingRodHookedTrigger.TriggerInstance createInstance(JsonObject jsonobject, ContextAwarePredicate contextawarepredicate, DeserializationContext deserializationcontext) {
      ItemPredicate itempredicate = ItemPredicate.fromJson(jsonobject.get("rod"));
      ContextAwarePredicate contextawarepredicate1 = EntityPredicate.fromJson(jsonobject, "entity", deserializationcontext);
      ItemPredicate itempredicate1 = ItemPredicate.fromJson(jsonobject.get("item"));
      return new FishingRodHookedTrigger.TriggerInstance(contextawarepredicate, itempredicate, contextawarepredicate1, itempredicate1);
   }

   public void trigger(ServerPlayer serverplayer, ItemStack itemstack, FishingHook fishinghook, Collection<ItemStack> collection) {
      LootContext lootcontext = EntityPredicate.createContext(serverplayer, (Entity)(fishinghook.getHookedIn() != null ? fishinghook.getHookedIn() : fishinghook));
      this.trigger(serverplayer, (fishingrodhookedtrigger_triggerinstance) -> fishingrodhookedtrigger_triggerinstance.matches(itemstack, lootcontext, collection));
   }

   public static class TriggerInstance extends AbstractCriterionTriggerInstance {
      private final ItemPredicate rod;
      private final ContextAwarePredicate entity;
      private final ItemPredicate item;

      public TriggerInstance(ContextAwarePredicate contextawarepredicate, ItemPredicate itempredicate, ContextAwarePredicate contextawarepredicate1, ItemPredicate itempredicate1) {
         super(FishingRodHookedTrigger.ID, contextawarepredicate);
         this.rod = itempredicate;
         this.entity = contextawarepredicate1;
         this.item = itempredicate1;
      }

      public static FishingRodHookedTrigger.TriggerInstance fishedItem(ItemPredicate itempredicate, EntityPredicate entitypredicate, ItemPredicate itempredicate1) {
         return new FishingRodHookedTrigger.TriggerInstance(ContextAwarePredicate.ANY, itempredicate, EntityPredicate.wrap(entitypredicate), itempredicate1);
      }

      public boolean matches(ItemStack itemstack, LootContext lootcontext, Collection<ItemStack> collection) {
         if (!this.rod.matches(itemstack)) {
            return false;
         } else if (!this.entity.matches(lootcontext)) {
            return false;
         } else {
            if (this.item != ItemPredicate.ANY) {
               boolean flag = false;
               Entity entity = lootcontext.getParamOrNull(LootContextParams.THIS_ENTITY);
               if (entity instanceof ItemEntity) {
                  ItemEntity itementity = (ItemEntity)entity;
                  if (this.item.matches(itementity.getItem())) {
                     flag = true;
                  }
               }

               for(ItemStack itemstack1 : collection) {
                  if (this.item.matches(itemstack1)) {
                     flag = true;
                     break;
                  }
               }

               if (!flag) {
                  return false;
               }
            }

            return true;
         }
      }

      public JsonObject serializeToJson(SerializationContext serializationcontext) {
         JsonObject jsonobject = super.serializeToJson(serializationcontext);
         jsonobject.add("rod", this.rod.serializeToJson());
         jsonobject.add("entity", this.entity.toJson(serializationcontext));
         jsonobject.add("item", this.item.serializeToJson());
         return jsonobject;
      }
   }
}
