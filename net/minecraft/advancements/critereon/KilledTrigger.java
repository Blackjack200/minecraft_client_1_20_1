package net.minecraft.advancements.critereon;

import com.google.gson.JsonObject;
import net.minecraft.advancements.CriteriaTriggers;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.level.storage.loot.LootContext;

public class KilledTrigger extends SimpleCriterionTrigger<KilledTrigger.TriggerInstance> {
   final ResourceLocation id;

   public KilledTrigger(ResourceLocation resourcelocation) {
      this.id = resourcelocation;
   }

   public ResourceLocation getId() {
      return this.id;
   }

   public KilledTrigger.TriggerInstance createInstance(JsonObject jsonobject, ContextAwarePredicate contextawarepredicate, DeserializationContext deserializationcontext) {
      return new KilledTrigger.TriggerInstance(this.id, contextawarepredicate, EntityPredicate.fromJson(jsonobject, "entity", deserializationcontext), DamageSourcePredicate.fromJson(jsonobject.get("killing_blow")));
   }

   public void trigger(ServerPlayer serverplayer, Entity entity, DamageSource damagesource) {
      LootContext lootcontext = EntityPredicate.createContext(serverplayer, entity);
      this.trigger(serverplayer, (killedtrigger_triggerinstance) -> killedtrigger_triggerinstance.matches(serverplayer, lootcontext, damagesource));
   }

   public static class TriggerInstance extends AbstractCriterionTriggerInstance {
      private final ContextAwarePredicate entityPredicate;
      private final DamageSourcePredicate killingBlow;

      public TriggerInstance(ResourceLocation resourcelocation, ContextAwarePredicate contextawarepredicate, ContextAwarePredicate contextawarepredicate1, DamageSourcePredicate damagesourcepredicate) {
         super(resourcelocation, contextawarepredicate);
         this.entityPredicate = contextawarepredicate1;
         this.killingBlow = damagesourcepredicate;
      }

      public static KilledTrigger.TriggerInstance playerKilledEntity(EntityPredicate entitypredicate) {
         return new KilledTrigger.TriggerInstance(CriteriaTriggers.PLAYER_KILLED_ENTITY.id, ContextAwarePredicate.ANY, EntityPredicate.wrap(entitypredicate), DamageSourcePredicate.ANY);
      }

      public static KilledTrigger.TriggerInstance playerKilledEntity(EntityPredicate.Builder entitypredicate_builder) {
         return new KilledTrigger.TriggerInstance(CriteriaTriggers.PLAYER_KILLED_ENTITY.id, ContextAwarePredicate.ANY, EntityPredicate.wrap(entitypredicate_builder.build()), DamageSourcePredicate.ANY);
      }

      public static KilledTrigger.TriggerInstance playerKilledEntity() {
         return new KilledTrigger.TriggerInstance(CriteriaTriggers.PLAYER_KILLED_ENTITY.id, ContextAwarePredicate.ANY, ContextAwarePredicate.ANY, DamageSourcePredicate.ANY);
      }

      public static KilledTrigger.TriggerInstance playerKilledEntity(EntityPredicate entitypredicate, DamageSourcePredicate damagesourcepredicate) {
         return new KilledTrigger.TriggerInstance(CriteriaTriggers.PLAYER_KILLED_ENTITY.id, ContextAwarePredicate.ANY, EntityPredicate.wrap(entitypredicate), damagesourcepredicate);
      }

      public static KilledTrigger.TriggerInstance playerKilledEntity(EntityPredicate.Builder entitypredicate_builder, DamageSourcePredicate damagesourcepredicate) {
         return new KilledTrigger.TriggerInstance(CriteriaTriggers.PLAYER_KILLED_ENTITY.id, ContextAwarePredicate.ANY, EntityPredicate.wrap(entitypredicate_builder.build()), damagesourcepredicate);
      }

      public static KilledTrigger.TriggerInstance playerKilledEntity(EntityPredicate entitypredicate, DamageSourcePredicate.Builder damagesourcepredicate_builder) {
         return new KilledTrigger.TriggerInstance(CriteriaTriggers.PLAYER_KILLED_ENTITY.id, ContextAwarePredicate.ANY, EntityPredicate.wrap(entitypredicate), damagesourcepredicate_builder.build());
      }

      public static KilledTrigger.TriggerInstance playerKilledEntity(EntityPredicate.Builder entitypredicate_builder, DamageSourcePredicate.Builder damagesourcepredicate_builder) {
         return new KilledTrigger.TriggerInstance(CriteriaTriggers.PLAYER_KILLED_ENTITY.id, ContextAwarePredicate.ANY, EntityPredicate.wrap(entitypredicate_builder.build()), damagesourcepredicate_builder.build());
      }

      public static KilledTrigger.TriggerInstance playerKilledEntityNearSculkCatalyst() {
         return new KilledTrigger.TriggerInstance(CriteriaTriggers.KILL_MOB_NEAR_SCULK_CATALYST.id, ContextAwarePredicate.ANY, ContextAwarePredicate.ANY, DamageSourcePredicate.ANY);
      }

      public static KilledTrigger.TriggerInstance entityKilledPlayer(EntityPredicate entitypredicate) {
         return new KilledTrigger.TriggerInstance(CriteriaTriggers.ENTITY_KILLED_PLAYER.id, ContextAwarePredicate.ANY, EntityPredicate.wrap(entitypredicate), DamageSourcePredicate.ANY);
      }

      public static KilledTrigger.TriggerInstance entityKilledPlayer(EntityPredicate.Builder entitypredicate_builder) {
         return new KilledTrigger.TriggerInstance(CriteriaTriggers.ENTITY_KILLED_PLAYER.id, ContextAwarePredicate.ANY, EntityPredicate.wrap(entitypredicate_builder.build()), DamageSourcePredicate.ANY);
      }

      public static KilledTrigger.TriggerInstance entityKilledPlayer() {
         return new KilledTrigger.TriggerInstance(CriteriaTriggers.ENTITY_KILLED_PLAYER.id, ContextAwarePredicate.ANY, ContextAwarePredicate.ANY, DamageSourcePredicate.ANY);
      }

      public static KilledTrigger.TriggerInstance entityKilledPlayer(EntityPredicate entitypredicate, DamageSourcePredicate damagesourcepredicate) {
         return new KilledTrigger.TriggerInstance(CriteriaTriggers.ENTITY_KILLED_PLAYER.id, ContextAwarePredicate.ANY, EntityPredicate.wrap(entitypredicate), damagesourcepredicate);
      }

      public static KilledTrigger.TriggerInstance entityKilledPlayer(EntityPredicate.Builder entitypredicate_builder, DamageSourcePredicate damagesourcepredicate) {
         return new KilledTrigger.TriggerInstance(CriteriaTriggers.ENTITY_KILLED_PLAYER.id, ContextAwarePredicate.ANY, EntityPredicate.wrap(entitypredicate_builder.build()), damagesourcepredicate);
      }

      public static KilledTrigger.TriggerInstance entityKilledPlayer(EntityPredicate entitypredicate, DamageSourcePredicate.Builder damagesourcepredicate_builder) {
         return new KilledTrigger.TriggerInstance(CriteriaTriggers.ENTITY_KILLED_PLAYER.id, ContextAwarePredicate.ANY, EntityPredicate.wrap(entitypredicate), damagesourcepredicate_builder.build());
      }

      public static KilledTrigger.TriggerInstance entityKilledPlayer(EntityPredicate.Builder entitypredicate_builder, DamageSourcePredicate.Builder damagesourcepredicate_builder) {
         return new KilledTrigger.TriggerInstance(CriteriaTriggers.ENTITY_KILLED_PLAYER.id, ContextAwarePredicate.ANY, EntityPredicate.wrap(entitypredicate_builder.build()), damagesourcepredicate_builder.build());
      }

      public boolean matches(ServerPlayer serverplayer, LootContext lootcontext, DamageSource damagesource) {
         return !this.killingBlow.matches(serverplayer, damagesource) ? false : this.entityPredicate.matches(lootcontext);
      }

      public JsonObject serializeToJson(SerializationContext serializationcontext) {
         JsonObject jsonobject = super.serializeToJson(serializationcontext);
         jsonobject.add("entity", this.entityPredicate.toJson(serializationcontext));
         jsonobject.add("killing_blow", this.killingBlow.serializeToJson());
         return jsonobject;
      }
   }
}
