package net.minecraft.advancements.critereon;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.util.GsonHelper;
import net.minecraft.world.item.ItemStack;

public class RecipeCraftedTrigger extends SimpleCriterionTrigger<RecipeCraftedTrigger.TriggerInstance> {
   static final ResourceLocation ID = new ResourceLocation("recipe_crafted");

   public ResourceLocation getId() {
      return ID;
   }

   protected RecipeCraftedTrigger.TriggerInstance createInstance(JsonObject jsonobject, ContextAwarePredicate contextawarepredicate, DeserializationContext deserializationcontext) {
      ResourceLocation resourcelocation = new ResourceLocation(GsonHelper.getAsString(jsonobject, "recipe_id"));
      ItemPredicate[] aitempredicate = ItemPredicate.fromJsonArray(jsonobject.get("ingredients"));
      return new RecipeCraftedTrigger.TriggerInstance(contextawarepredicate, resourcelocation, List.of(aitempredicate));
   }

   public void trigger(ServerPlayer serverplayer, ResourceLocation resourcelocation, List<ItemStack> list) {
      this.trigger(serverplayer, (recipecraftedtrigger_triggerinstance) -> recipecraftedtrigger_triggerinstance.matches(resourcelocation, list));
   }

   public static class TriggerInstance extends AbstractCriterionTriggerInstance {
      private final ResourceLocation recipeId;
      private final List<ItemPredicate> predicates;

      public TriggerInstance(ContextAwarePredicate contextawarepredicate, ResourceLocation resourcelocation, List<ItemPredicate> list) {
         super(RecipeCraftedTrigger.ID, contextawarepredicate);
         this.recipeId = resourcelocation;
         this.predicates = list;
      }

      public static RecipeCraftedTrigger.TriggerInstance craftedItem(ResourceLocation resourcelocation, List<ItemPredicate> list) {
         return new RecipeCraftedTrigger.TriggerInstance(ContextAwarePredicate.ANY, resourcelocation, list);
      }

      public static RecipeCraftedTrigger.TriggerInstance craftedItem(ResourceLocation resourcelocation) {
         return new RecipeCraftedTrigger.TriggerInstance(ContextAwarePredicate.ANY, resourcelocation, List.of());
      }

      boolean matches(ResourceLocation resourcelocation, List<ItemStack> list) {
         if (!resourcelocation.equals(this.recipeId)) {
            return false;
         } else {
            List<ItemStack> list1 = new ArrayList<>(list);

            for(ItemPredicate itempredicate : this.predicates) {
               boolean flag = false;
               Iterator<ItemStack> iterator = list1.iterator();

               while(iterator.hasNext()) {
                  if (itempredicate.matches(iterator.next())) {
                     iterator.remove();
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
         jsonobject.addProperty("recipe_id", this.recipeId.toString());
         if (this.predicates.size() > 0) {
            JsonArray jsonarray = new JsonArray();

            for(ItemPredicate itempredicate : this.predicates) {
               jsonarray.add(itempredicate.serializeToJson());
            }

            jsonobject.add("ingredients", jsonarray);
         }

         return jsonobject;
      }
   }
}
