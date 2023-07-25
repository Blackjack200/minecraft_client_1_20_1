package net.minecraft.advancements.critereon;

import com.google.gson.JsonObject;
import net.minecraft.advancements.CriteriaTriggers;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.phys.Vec3;

public class DistanceTrigger extends SimpleCriterionTrigger<DistanceTrigger.TriggerInstance> {
   final ResourceLocation id;

   public DistanceTrigger(ResourceLocation resourcelocation) {
      this.id = resourcelocation;
   }

   public ResourceLocation getId() {
      return this.id;
   }

   public DistanceTrigger.TriggerInstance createInstance(JsonObject jsonobject, ContextAwarePredicate contextawarepredicate, DeserializationContext deserializationcontext) {
      LocationPredicate locationpredicate = LocationPredicate.fromJson(jsonobject.get("start_position"));
      DistancePredicate distancepredicate = DistancePredicate.fromJson(jsonobject.get("distance"));
      return new DistanceTrigger.TriggerInstance(this.id, contextawarepredicate, locationpredicate, distancepredicate);
   }

   public void trigger(ServerPlayer serverplayer, Vec3 vec3) {
      Vec3 vec31 = serverplayer.position();
      this.trigger(serverplayer, (distancetrigger_triggerinstance) -> distancetrigger_triggerinstance.matches(serverplayer.serverLevel(), vec3, vec31));
   }

   public static class TriggerInstance extends AbstractCriterionTriggerInstance {
      private final LocationPredicate startPosition;
      private final DistancePredicate distance;

      public TriggerInstance(ResourceLocation resourcelocation, ContextAwarePredicate contextawarepredicate, LocationPredicate locationpredicate, DistancePredicate distancepredicate) {
         super(resourcelocation, contextawarepredicate);
         this.startPosition = locationpredicate;
         this.distance = distancepredicate;
      }

      public static DistanceTrigger.TriggerInstance fallFromHeight(EntityPredicate.Builder entitypredicate_builder, DistancePredicate distancepredicate, LocationPredicate locationpredicate) {
         return new DistanceTrigger.TriggerInstance(CriteriaTriggers.FALL_FROM_HEIGHT.id, EntityPredicate.wrap(entitypredicate_builder.build()), locationpredicate, distancepredicate);
      }

      public static DistanceTrigger.TriggerInstance rideEntityInLava(EntityPredicate.Builder entitypredicate_builder, DistancePredicate distancepredicate) {
         return new DistanceTrigger.TriggerInstance(CriteriaTriggers.RIDE_ENTITY_IN_LAVA_TRIGGER.id, EntityPredicate.wrap(entitypredicate_builder.build()), LocationPredicate.ANY, distancepredicate);
      }

      public static DistanceTrigger.TriggerInstance travelledThroughNether(DistancePredicate distancepredicate) {
         return new DistanceTrigger.TriggerInstance(CriteriaTriggers.NETHER_TRAVEL.id, ContextAwarePredicate.ANY, LocationPredicate.ANY, distancepredicate);
      }

      public JsonObject serializeToJson(SerializationContext serializationcontext) {
         JsonObject jsonobject = super.serializeToJson(serializationcontext);
         jsonobject.add("start_position", this.startPosition.serializeToJson());
         jsonobject.add("distance", this.distance.serializeToJson());
         return jsonobject;
      }

      public boolean matches(ServerLevel serverlevel, Vec3 vec3, Vec3 vec31) {
         if (!this.startPosition.matches(serverlevel, vec3.x, vec3.y, vec3.z)) {
            return false;
         } else {
            return this.distance.matches(vec3.x, vec3.y, vec3.z, vec31.x, vec31.y, vec31.z);
         }
      }
   }
}
