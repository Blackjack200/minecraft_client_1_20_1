package net.minecraft.advancements.critereon;

import com.google.gson.JsonObject;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.phys.Vec3;

public class LevitationTrigger extends SimpleCriterionTrigger<LevitationTrigger.TriggerInstance> {
   static final ResourceLocation ID = new ResourceLocation("levitation");

   public ResourceLocation getId() {
      return ID;
   }

   public LevitationTrigger.TriggerInstance createInstance(JsonObject jsonobject, ContextAwarePredicate contextawarepredicate, DeserializationContext deserializationcontext) {
      DistancePredicate distancepredicate = DistancePredicate.fromJson(jsonobject.get("distance"));
      MinMaxBounds.Ints minmaxbounds_ints = MinMaxBounds.Ints.fromJson(jsonobject.get("duration"));
      return new LevitationTrigger.TriggerInstance(contextawarepredicate, distancepredicate, minmaxbounds_ints);
   }

   public void trigger(ServerPlayer serverplayer, Vec3 vec3, int i) {
      this.trigger(serverplayer, (levitationtrigger_triggerinstance) -> levitationtrigger_triggerinstance.matches(serverplayer, vec3, i));
   }

   public static class TriggerInstance extends AbstractCriterionTriggerInstance {
      private final DistancePredicate distance;
      private final MinMaxBounds.Ints duration;

      public TriggerInstance(ContextAwarePredicate contextawarepredicate, DistancePredicate distancepredicate, MinMaxBounds.Ints minmaxbounds_ints) {
         super(LevitationTrigger.ID, contextawarepredicate);
         this.distance = distancepredicate;
         this.duration = minmaxbounds_ints;
      }

      public static LevitationTrigger.TriggerInstance levitated(DistancePredicate distancepredicate) {
         return new LevitationTrigger.TriggerInstance(ContextAwarePredicate.ANY, distancepredicate, MinMaxBounds.Ints.ANY);
      }

      public boolean matches(ServerPlayer serverplayer, Vec3 vec3, int i) {
         if (!this.distance.matches(vec3.x, vec3.y, vec3.z, serverplayer.getX(), serverplayer.getY(), serverplayer.getZ())) {
            return false;
         } else {
            return this.duration.matches(i);
         }
      }

      public JsonObject serializeToJson(SerializationContext serializationcontext) {
         JsonObject jsonobject = super.serializeToJson(serializationcontext);
         jsonobject.add("distance", this.distance.serializeToJson());
         jsonobject.add("duration", this.duration.serializeToJson());
         return jsonobject;
      }
   }
}
