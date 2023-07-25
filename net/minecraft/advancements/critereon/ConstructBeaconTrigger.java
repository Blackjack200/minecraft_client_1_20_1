package net.minecraft.advancements.critereon;

import com.google.gson.JsonObject;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;

public class ConstructBeaconTrigger extends SimpleCriterionTrigger<ConstructBeaconTrigger.TriggerInstance> {
   static final ResourceLocation ID = new ResourceLocation("construct_beacon");

   public ResourceLocation getId() {
      return ID;
   }

   public ConstructBeaconTrigger.TriggerInstance createInstance(JsonObject jsonobject, ContextAwarePredicate contextawarepredicate, DeserializationContext deserializationcontext) {
      MinMaxBounds.Ints minmaxbounds_ints = MinMaxBounds.Ints.fromJson(jsonobject.get("level"));
      return new ConstructBeaconTrigger.TriggerInstance(contextawarepredicate, minmaxbounds_ints);
   }

   public void trigger(ServerPlayer serverplayer, int i) {
      this.trigger(serverplayer, (constructbeacontrigger_triggerinstance) -> constructbeacontrigger_triggerinstance.matches(i));
   }

   public static class TriggerInstance extends AbstractCriterionTriggerInstance {
      private final MinMaxBounds.Ints level;

      public TriggerInstance(ContextAwarePredicate contextawarepredicate, MinMaxBounds.Ints minmaxbounds_ints) {
         super(ConstructBeaconTrigger.ID, contextawarepredicate);
         this.level = minmaxbounds_ints;
      }

      public static ConstructBeaconTrigger.TriggerInstance constructedBeacon() {
         return new ConstructBeaconTrigger.TriggerInstance(ContextAwarePredicate.ANY, MinMaxBounds.Ints.ANY);
      }

      public static ConstructBeaconTrigger.TriggerInstance constructedBeacon(MinMaxBounds.Ints minmaxbounds_ints) {
         return new ConstructBeaconTrigger.TriggerInstance(ContextAwarePredicate.ANY, minmaxbounds_ints);
      }

      public boolean matches(int i) {
         return this.level.matches(i);
      }

      public JsonObject serializeToJson(SerializationContext serializationcontext) {
         JsonObject jsonobject = super.serializeToJson(serializationcontext);
         jsonobject.add("level", this.level.serializeToJson());
         return jsonobject;
      }
   }
}
