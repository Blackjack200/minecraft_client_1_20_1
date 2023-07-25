package net.minecraft.advancements.critereon;

import com.google.gson.JsonObject;
import net.minecraft.advancements.CriterionTriggerInstance;
import net.minecraft.resources.ResourceLocation;

public abstract class AbstractCriterionTriggerInstance implements CriterionTriggerInstance {
   private final ResourceLocation criterion;
   private final ContextAwarePredicate player;

   public AbstractCriterionTriggerInstance(ResourceLocation resourcelocation, ContextAwarePredicate contextawarepredicate) {
      this.criterion = resourcelocation;
      this.player = contextawarepredicate;
   }

   public ResourceLocation getCriterion() {
      return this.criterion;
   }

   protected ContextAwarePredicate getPlayerPredicate() {
      return this.player;
   }

   public JsonObject serializeToJson(SerializationContext serializationcontext) {
      JsonObject jsonobject = new JsonObject();
      jsonobject.add("player", this.player.toJson(serializationcontext));
      return jsonobject;
   }

   public String toString() {
      return "AbstractCriterionInstance{criterion=" + this.criterion + "}";
   }
}
