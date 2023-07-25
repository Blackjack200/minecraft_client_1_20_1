package net.minecraft.advancements.critereon;

import com.google.gson.JsonObject;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.level.storage.loot.LootContext;

public class PlayerHurtEntityTrigger extends SimpleCriterionTrigger<PlayerHurtEntityTrigger.TriggerInstance> {
   static final ResourceLocation ID = new ResourceLocation("player_hurt_entity");

   public ResourceLocation getId() {
      return ID;
   }

   public PlayerHurtEntityTrigger.TriggerInstance createInstance(JsonObject jsonobject, ContextAwarePredicate contextawarepredicate, DeserializationContext deserializationcontext) {
      DamagePredicate damagepredicate = DamagePredicate.fromJson(jsonobject.get("damage"));
      ContextAwarePredicate contextawarepredicate1 = EntityPredicate.fromJson(jsonobject, "entity", deserializationcontext);
      return new PlayerHurtEntityTrigger.TriggerInstance(contextawarepredicate, damagepredicate, contextawarepredicate1);
   }

   public void trigger(ServerPlayer serverplayer, Entity entity, DamageSource damagesource, float f, float f1, boolean flag) {
      LootContext lootcontext = EntityPredicate.createContext(serverplayer, entity);
      this.trigger(serverplayer, (playerhurtentitytrigger_triggerinstance) -> playerhurtentitytrigger_triggerinstance.matches(serverplayer, lootcontext, damagesource, f, f1, flag));
   }

   public static class TriggerInstance extends AbstractCriterionTriggerInstance {
      private final DamagePredicate damage;
      private final ContextAwarePredicate entity;

      public TriggerInstance(ContextAwarePredicate contextawarepredicate, DamagePredicate damagepredicate, ContextAwarePredicate contextawarepredicate1) {
         super(PlayerHurtEntityTrigger.ID, contextawarepredicate);
         this.damage = damagepredicate;
         this.entity = contextawarepredicate1;
      }

      public static PlayerHurtEntityTrigger.TriggerInstance playerHurtEntity() {
         return new PlayerHurtEntityTrigger.TriggerInstance(ContextAwarePredicate.ANY, DamagePredicate.ANY, ContextAwarePredicate.ANY);
      }

      public static PlayerHurtEntityTrigger.TriggerInstance playerHurtEntity(DamagePredicate damagepredicate) {
         return new PlayerHurtEntityTrigger.TriggerInstance(ContextAwarePredicate.ANY, damagepredicate, ContextAwarePredicate.ANY);
      }

      public static PlayerHurtEntityTrigger.TriggerInstance playerHurtEntity(DamagePredicate.Builder damagepredicate_builder) {
         return new PlayerHurtEntityTrigger.TriggerInstance(ContextAwarePredicate.ANY, damagepredicate_builder.build(), ContextAwarePredicate.ANY);
      }

      public static PlayerHurtEntityTrigger.TriggerInstance playerHurtEntity(EntityPredicate entitypredicate) {
         return new PlayerHurtEntityTrigger.TriggerInstance(ContextAwarePredicate.ANY, DamagePredicate.ANY, EntityPredicate.wrap(entitypredicate));
      }

      public static PlayerHurtEntityTrigger.TriggerInstance playerHurtEntity(DamagePredicate damagepredicate, EntityPredicate entitypredicate) {
         return new PlayerHurtEntityTrigger.TriggerInstance(ContextAwarePredicate.ANY, damagepredicate, EntityPredicate.wrap(entitypredicate));
      }

      public static PlayerHurtEntityTrigger.TriggerInstance playerHurtEntity(DamagePredicate.Builder damagepredicate_builder, EntityPredicate entitypredicate) {
         return new PlayerHurtEntityTrigger.TriggerInstance(ContextAwarePredicate.ANY, damagepredicate_builder.build(), EntityPredicate.wrap(entitypredicate));
      }

      public boolean matches(ServerPlayer serverplayer, LootContext lootcontext, DamageSource damagesource, float f, float f1, boolean flag) {
         if (!this.damage.matches(serverplayer, damagesource, f, f1, flag)) {
            return false;
         } else {
            return this.entity.matches(lootcontext);
         }
      }

      public JsonObject serializeToJson(SerializationContext serializationcontext) {
         JsonObject jsonobject = super.serializeToJson(serializationcontext);
         jsonobject.add("damage", this.damage.serializeToJson());
         jsonobject.add("entity", this.entity.toJson(serializationcontext));
         return jsonobject;
      }
   }
}
