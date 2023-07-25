package net.minecraft.advancements.critereon;

import com.google.gson.JsonObject;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.damagesource.DamageSource;

public class EntityHurtPlayerTrigger extends SimpleCriterionTrigger<EntityHurtPlayerTrigger.TriggerInstance> {
   static final ResourceLocation ID = new ResourceLocation("entity_hurt_player");

   public ResourceLocation getId() {
      return ID;
   }

   public EntityHurtPlayerTrigger.TriggerInstance createInstance(JsonObject jsonobject, ContextAwarePredicate contextawarepredicate, DeserializationContext deserializationcontext) {
      DamagePredicate damagepredicate = DamagePredicate.fromJson(jsonobject.get("damage"));
      return new EntityHurtPlayerTrigger.TriggerInstance(contextawarepredicate, damagepredicate);
   }

   public void trigger(ServerPlayer serverplayer, DamageSource damagesource, float f, float f1, boolean flag) {
      this.trigger(serverplayer, (entityhurtplayertrigger_triggerinstance) -> entityhurtplayertrigger_triggerinstance.matches(serverplayer, damagesource, f, f1, flag));
   }

   public static class TriggerInstance extends AbstractCriterionTriggerInstance {
      private final DamagePredicate damage;

      public TriggerInstance(ContextAwarePredicate contextawarepredicate, DamagePredicate damagepredicate) {
         super(EntityHurtPlayerTrigger.ID, contextawarepredicate);
         this.damage = damagepredicate;
      }

      public static EntityHurtPlayerTrigger.TriggerInstance entityHurtPlayer() {
         return new EntityHurtPlayerTrigger.TriggerInstance(ContextAwarePredicate.ANY, DamagePredicate.ANY);
      }

      public static EntityHurtPlayerTrigger.TriggerInstance entityHurtPlayer(DamagePredicate damagepredicate) {
         return new EntityHurtPlayerTrigger.TriggerInstance(ContextAwarePredicate.ANY, damagepredicate);
      }

      public static EntityHurtPlayerTrigger.TriggerInstance entityHurtPlayer(DamagePredicate.Builder damagepredicate_builder) {
         return new EntityHurtPlayerTrigger.TriggerInstance(ContextAwarePredicate.ANY, damagepredicate_builder.build());
      }

      public boolean matches(ServerPlayer serverplayer, DamageSource damagesource, float f, float f1, boolean flag) {
         return this.damage.matches(serverplayer, damagesource, f, f1, flag);
      }

      public JsonObject serializeToJson(SerializationContext serializationcontext) {
         JsonObject jsonobject = super.serializeToJson(serializationcontext);
         jsonobject.add("damage", this.damage.serializeToJson());
         return jsonobject;
      }
   }
}
