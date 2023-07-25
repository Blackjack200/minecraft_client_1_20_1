package net.minecraft.advancements.critereon;

import com.google.gson.JsonObject;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;

public class StartRidingTrigger extends SimpleCriterionTrigger<StartRidingTrigger.TriggerInstance> {
   static final ResourceLocation ID = new ResourceLocation("started_riding");

   public ResourceLocation getId() {
      return ID;
   }

   public StartRidingTrigger.TriggerInstance createInstance(JsonObject jsonobject, ContextAwarePredicate contextawarepredicate, DeserializationContext deserializationcontext) {
      return new StartRidingTrigger.TriggerInstance(contextawarepredicate);
   }

   public void trigger(ServerPlayer serverplayer) {
      this.trigger(serverplayer, (startridingtrigger_triggerinstance) -> true);
   }

   public static class TriggerInstance extends AbstractCriterionTriggerInstance {
      public TriggerInstance(ContextAwarePredicate contextawarepredicate) {
         super(StartRidingTrigger.ID, contextawarepredicate);
      }

      public static StartRidingTrigger.TriggerInstance playerStartsRiding(EntityPredicate.Builder entitypredicate_builder) {
         return new StartRidingTrigger.TriggerInstance(EntityPredicate.wrap(entitypredicate_builder.build()));
      }
   }
}
