package net.minecraft.advancements.critereon;

import com.google.gson.JsonObject;
import net.minecraft.advancements.CriterionTrigger;
import net.minecraft.advancements.CriterionTriggerInstance;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.PlayerAdvancements;

public class ImpossibleTrigger implements CriterionTrigger<ImpossibleTrigger.TriggerInstance> {
   static final ResourceLocation ID = new ResourceLocation("impossible");

   public ResourceLocation getId() {
      return ID;
   }

   public void addPlayerListener(PlayerAdvancements playeradvancements, CriterionTrigger.Listener<ImpossibleTrigger.TriggerInstance> criteriontrigger_listener) {
   }

   public void removePlayerListener(PlayerAdvancements playeradvancements, CriterionTrigger.Listener<ImpossibleTrigger.TriggerInstance> criteriontrigger_listener) {
   }

   public void removePlayerListeners(PlayerAdvancements playeradvancements) {
   }

   public ImpossibleTrigger.TriggerInstance createInstance(JsonObject jsonobject, DeserializationContext deserializationcontext) {
      return new ImpossibleTrigger.TriggerInstance();
   }

   public static class TriggerInstance implements CriterionTriggerInstance {
      public ResourceLocation getCriterion() {
         return ImpossibleTrigger.ID;
      }

      public JsonObject serializeToJson(SerializationContext serializationcontext) {
         return new JsonObject();
      }
   }
}
