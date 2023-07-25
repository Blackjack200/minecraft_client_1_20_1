package net.minecraft.advancements.critereon;

import com.google.gson.JsonObject;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.level.storage.loot.LootContext;
import net.minecraft.world.phys.Vec3;

public class TargetBlockTrigger extends SimpleCriterionTrigger<TargetBlockTrigger.TriggerInstance> {
   static final ResourceLocation ID = new ResourceLocation("target_hit");

   public ResourceLocation getId() {
      return ID;
   }

   public TargetBlockTrigger.TriggerInstance createInstance(JsonObject jsonobject, ContextAwarePredicate contextawarepredicate, DeserializationContext deserializationcontext) {
      MinMaxBounds.Ints minmaxbounds_ints = MinMaxBounds.Ints.fromJson(jsonobject.get("signal_strength"));
      ContextAwarePredicate contextawarepredicate1 = EntityPredicate.fromJson(jsonobject, "projectile", deserializationcontext);
      return new TargetBlockTrigger.TriggerInstance(contextawarepredicate, minmaxbounds_ints, contextawarepredicate1);
   }

   public void trigger(ServerPlayer serverplayer, Entity entity, Vec3 vec3, int i) {
      LootContext lootcontext = EntityPredicate.createContext(serverplayer, entity);
      this.trigger(serverplayer, (targetblocktrigger_triggerinstance) -> targetblocktrigger_triggerinstance.matches(lootcontext, vec3, i));
   }

   public static class TriggerInstance extends AbstractCriterionTriggerInstance {
      private final MinMaxBounds.Ints signalStrength;
      private final ContextAwarePredicate projectile;

      public TriggerInstance(ContextAwarePredicate contextawarepredicate, MinMaxBounds.Ints minmaxbounds_ints, ContextAwarePredicate contextawarepredicate1) {
         super(TargetBlockTrigger.ID, contextawarepredicate);
         this.signalStrength = minmaxbounds_ints;
         this.projectile = contextawarepredicate1;
      }

      public static TargetBlockTrigger.TriggerInstance targetHit(MinMaxBounds.Ints minmaxbounds_ints, ContextAwarePredicate contextawarepredicate) {
         return new TargetBlockTrigger.TriggerInstance(ContextAwarePredicate.ANY, minmaxbounds_ints, contextawarepredicate);
      }

      public JsonObject serializeToJson(SerializationContext serializationcontext) {
         JsonObject jsonobject = super.serializeToJson(serializationcontext);
         jsonobject.add("signal_strength", this.signalStrength.serializeToJson());
         jsonobject.add("projectile", this.projectile.toJson(serializationcontext));
         return jsonobject;
      }

      public boolean matches(LootContext lootcontext, Vec3 vec3, int i) {
         if (!this.signalStrength.matches(i)) {
            return false;
         } else {
            return this.projectile.matches(lootcontext);
         }
      }
   }
}
