package net.minecraft.advancements.critereon;

import com.google.gson.JsonObject;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.util.GsonHelper;
import net.minecraft.world.item.crafting.Recipe;

public class RecipeUnlockedTrigger extends SimpleCriterionTrigger<RecipeUnlockedTrigger.TriggerInstance> {
   static final ResourceLocation ID = new ResourceLocation("recipe_unlocked");

   public ResourceLocation getId() {
      return ID;
   }

   public RecipeUnlockedTrigger.TriggerInstance createInstance(JsonObject jsonobject, ContextAwarePredicate contextawarepredicate, DeserializationContext deserializationcontext) {
      ResourceLocation resourcelocation = new ResourceLocation(GsonHelper.getAsString(jsonobject, "recipe"));
      return new RecipeUnlockedTrigger.TriggerInstance(contextawarepredicate, resourcelocation);
   }

   public void trigger(ServerPlayer serverplayer, Recipe<?> recipe) {
      this.trigger(serverplayer, (recipeunlockedtrigger_triggerinstance) -> recipeunlockedtrigger_triggerinstance.matches(recipe));
   }

   public static RecipeUnlockedTrigger.TriggerInstance unlocked(ResourceLocation resourcelocation) {
      return new RecipeUnlockedTrigger.TriggerInstance(ContextAwarePredicate.ANY, resourcelocation);
   }

   public static class TriggerInstance extends AbstractCriterionTriggerInstance {
      private final ResourceLocation recipe;

      public TriggerInstance(ContextAwarePredicate contextawarepredicate, ResourceLocation resourcelocation) {
         super(RecipeUnlockedTrigger.ID, contextawarepredicate);
         this.recipe = resourcelocation;
      }

      public JsonObject serializeToJson(SerializationContext serializationcontext) {
         JsonObject jsonobject = super.serializeToJson(serializationcontext);
         jsonobject.addProperty("recipe", this.recipe.toString());
         return jsonobject;
      }

      public boolean matches(Recipe<?> recipe) {
         return this.recipe.equals(recipe.getId());
      }
   }
}
