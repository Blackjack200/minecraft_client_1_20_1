package net.minecraft.advancements.critereon;

import com.google.gson.JsonObject;
import com.google.gson.JsonSyntaxException;
import javax.annotation.Nullable;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.util.GsonHelper;
import net.minecraft.world.item.alchemy.Potion;

public class BrewedPotionTrigger extends SimpleCriterionTrigger<BrewedPotionTrigger.TriggerInstance> {
   static final ResourceLocation ID = new ResourceLocation("brewed_potion");

   public ResourceLocation getId() {
      return ID;
   }

   public BrewedPotionTrigger.TriggerInstance createInstance(JsonObject jsonobject, ContextAwarePredicate contextawarepredicate, DeserializationContext deserializationcontext) {
      Potion potion = null;
      if (jsonobject.has("potion")) {
         ResourceLocation resourcelocation = new ResourceLocation(GsonHelper.getAsString(jsonobject, "potion"));
         potion = BuiltInRegistries.POTION.getOptional(resourcelocation).orElseThrow(() -> new JsonSyntaxException("Unknown potion '" + resourcelocation + "'"));
      }

      return new BrewedPotionTrigger.TriggerInstance(contextawarepredicate, potion);
   }

   public void trigger(ServerPlayer serverplayer, Potion potion) {
      this.trigger(serverplayer, (brewedpotiontrigger_triggerinstance) -> brewedpotiontrigger_triggerinstance.matches(potion));
   }

   public static class TriggerInstance extends AbstractCriterionTriggerInstance {
      @Nullable
      private final Potion potion;

      public TriggerInstance(ContextAwarePredicate contextawarepredicate, @Nullable Potion potion) {
         super(BrewedPotionTrigger.ID, contextawarepredicate);
         this.potion = potion;
      }

      public static BrewedPotionTrigger.TriggerInstance brewedPotion() {
         return new BrewedPotionTrigger.TriggerInstance(ContextAwarePredicate.ANY, (Potion)null);
      }

      public boolean matches(Potion potion) {
         return this.potion == null || this.potion == potion;
      }

      public JsonObject serializeToJson(SerializationContext serializationcontext) {
         JsonObject jsonobject = super.serializeToJson(serializationcontext);
         if (this.potion != null) {
            jsonobject.addProperty("potion", BuiltInRegistries.POTION.getKey(this.potion).toString());
         }

         return jsonobject;
      }
   }
}
