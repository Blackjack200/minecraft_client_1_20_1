package net.minecraft.advancements.critereon;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonNull;
import com.google.gson.JsonObject;
import javax.annotation.Nullable;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.tags.TagKey;
import net.minecraft.util.GsonHelper;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.level.storage.loot.LootContext;
import net.minecraft.world.level.storage.loot.LootParams;
import net.minecraft.world.level.storage.loot.parameters.LootContextParamSets;
import net.minecraft.world.level.storage.loot.parameters.LootContextParams;
import net.minecraft.world.level.storage.loot.predicates.LootItemCondition;
import net.minecraft.world.level.storage.loot.predicates.LootItemEntityPropertyCondition;
import net.minecraft.world.phys.Vec3;
import net.minecraft.world.scores.Team;

public class EntityPredicate {
   public static final EntityPredicate ANY = new EntityPredicate(EntityTypePredicate.ANY, DistancePredicate.ANY, LocationPredicate.ANY, LocationPredicate.ANY, MobEffectsPredicate.ANY, NbtPredicate.ANY, EntityFlagsPredicate.ANY, EntityEquipmentPredicate.ANY, EntitySubPredicate.ANY, (String)null);
   private final EntityTypePredicate entityType;
   private final DistancePredicate distanceToPlayer;
   private final LocationPredicate location;
   private final LocationPredicate steppingOnLocation;
   private final MobEffectsPredicate effects;
   private final NbtPredicate nbt;
   private final EntityFlagsPredicate flags;
   private final EntityEquipmentPredicate equipment;
   private final EntitySubPredicate subPredicate;
   private final EntityPredicate vehicle;
   private final EntityPredicate passenger;
   private final EntityPredicate targetedEntity;
   @Nullable
   private final String team;

   private EntityPredicate(EntityTypePredicate entitytypepredicate, DistancePredicate distancepredicate, LocationPredicate locationpredicate, LocationPredicate locationpredicate1, MobEffectsPredicate mobeffectspredicate, NbtPredicate nbtpredicate, EntityFlagsPredicate entityflagspredicate, EntityEquipmentPredicate entityequipmentpredicate, EntitySubPredicate entitysubpredicate, @Nullable String s) {
      this.entityType = entitytypepredicate;
      this.distanceToPlayer = distancepredicate;
      this.location = locationpredicate;
      this.steppingOnLocation = locationpredicate1;
      this.effects = mobeffectspredicate;
      this.nbt = nbtpredicate;
      this.flags = entityflagspredicate;
      this.equipment = entityequipmentpredicate;
      this.subPredicate = entitysubpredicate;
      this.passenger = this;
      this.vehicle = this;
      this.targetedEntity = this;
      this.team = s;
   }

   EntityPredicate(EntityTypePredicate entitytypepredicate, DistancePredicate distancepredicate, LocationPredicate locationpredicate, LocationPredicate locationpredicate1, MobEffectsPredicate mobeffectspredicate, NbtPredicate nbtpredicate, EntityFlagsPredicate entityflagspredicate, EntityEquipmentPredicate entityequipmentpredicate, EntitySubPredicate entitysubpredicate, EntityPredicate entitypredicate, EntityPredicate entitypredicate1, EntityPredicate entitypredicate2, @Nullable String s) {
      this.entityType = entitytypepredicate;
      this.distanceToPlayer = distancepredicate;
      this.location = locationpredicate;
      this.steppingOnLocation = locationpredicate1;
      this.effects = mobeffectspredicate;
      this.nbt = nbtpredicate;
      this.flags = entityflagspredicate;
      this.equipment = entityequipmentpredicate;
      this.subPredicate = entitysubpredicate;
      this.vehicle = entitypredicate;
      this.passenger = entitypredicate1;
      this.targetedEntity = entitypredicate2;
      this.team = s;
   }

   public static ContextAwarePredicate fromJson(JsonObject jsonobject, String s, DeserializationContext deserializationcontext) {
      JsonElement jsonelement = jsonobject.get(s);
      return fromElement(s, deserializationcontext, jsonelement);
   }

   public static ContextAwarePredicate[] fromJsonArray(JsonObject jsonobject, String s, DeserializationContext deserializationcontext) {
      JsonElement jsonelement = jsonobject.get(s);
      if (jsonelement != null && !jsonelement.isJsonNull()) {
         JsonArray jsonarray = GsonHelper.convertToJsonArray(jsonelement, s);
         ContextAwarePredicate[] acontextawarepredicate = new ContextAwarePredicate[jsonarray.size()];

         for(int i = 0; i < jsonarray.size(); ++i) {
            acontextawarepredicate[i] = fromElement(s + "[" + i + "]", deserializationcontext, jsonarray.get(i));
         }

         return acontextawarepredicate;
      } else {
         return new ContextAwarePredicate[0];
      }
   }

   private static ContextAwarePredicate fromElement(String s, DeserializationContext deserializationcontext, @Nullable JsonElement jsonelement) {
      ContextAwarePredicate contextawarepredicate = ContextAwarePredicate.fromElement(s, deserializationcontext, jsonelement, LootContextParamSets.ADVANCEMENT_ENTITY);
      if (contextawarepredicate != null) {
         return contextawarepredicate;
      } else {
         EntityPredicate entitypredicate = fromJson(jsonelement);
         return wrap(entitypredicate);
      }
   }

   public static ContextAwarePredicate wrap(EntityPredicate entitypredicate) {
      if (entitypredicate == ANY) {
         return ContextAwarePredicate.ANY;
      } else {
         LootItemCondition lootitemcondition = LootItemEntityPropertyCondition.hasProperties(LootContext.EntityTarget.THIS, entitypredicate).build();
         return new ContextAwarePredicate(new LootItemCondition[]{lootitemcondition});
      }
   }

   public boolean matches(ServerPlayer serverplayer, @Nullable Entity entity) {
      return this.matches(serverplayer.serverLevel(), serverplayer.position(), entity);
   }

   public boolean matches(ServerLevel serverlevel, @Nullable Vec3 vec3, @Nullable Entity entity) {
      if (this == ANY) {
         return true;
      } else if (entity == null) {
         return false;
      } else if (!this.entityType.matches(entity.getType())) {
         return false;
      } else {
         if (vec3 == null) {
            if (this.distanceToPlayer != DistancePredicate.ANY) {
               return false;
            }
         } else if (!this.distanceToPlayer.matches(vec3.x, vec3.y, vec3.z, entity.getX(), entity.getY(), entity.getZ())) {
            return false;
         }

         if (!this.location.matches(serverlevel, entity.getX(), entity.getY(), entity.getZ())) {
            return false;
         } else {
            if (this.steppingOnLocation != LocationPredicate.ANY) {
               Vec3 vec31 = Vec3.atCenterOf(entity.getOnPos());
               if (!this.steppingOnLocation.matches(serverlevel, vec31.x(), vec31.y(), vec31.z())) {
                  return false;
               }
            }

            if (!this.effects.matches(entity)) {
               return false;
            } else if (!this.nbt.matches(entity)) {
               return false;
            } else if (!this.flags.matches(entity)) {
               return false;
            } else if (!this.equipment.matches(entity)) {
               return false;
            } else if (!this.subPredicate.matches(entity, serverlevel, vec3)) {
               return false;
            } else if (!this.vehicle.matches(serverlevel, vec3, entity.getVehicle())) {
               return false;
            } else if (this.passenger != ANY && entity.getPassengers().stream().noneMatch((entity1) -> this.passenger.matches(serverlevel, vec3, entity1))) {
               return false;
            } else if (!this.targetedEntity.matches(serverlevel, vec3, entity instanceof Mob ? ((Mob)entity).getTarget() : null)) {
               return false;
            } else {
               if (this.team != null) {
                  Team team = entity.getTeam();
                  if (team == null || !this.team.equals(team.getName())) {
                     return false;
                  }
               }

               return true;
            }
         }
      }
   }

   public static EntityPredicate fromJson(@Nullable JsonElement jsonelement) {
      if (jsonelement != null && !jsonelement.isJsonNull()) {
         JsonObject jsonobject = GsonHelper.convertToJsonObject(jsonelement, "entity");
         EntityTypePredicate entitytypepredicate = EntityTypePredicate.fromJson(jsonobject.get("type"));
         DistancePredicate distancepredicate = DistancePredicate.fromJson(jsonobject.get("distance"));
         LocationPredicate locationpredicate = LocationPredicate.fromJson(jsonobject.get("location"));
         LocationPredicate locationpredicate1 = LocationPredicate.fromJson(jsonobject.get("stepping_on"));
         MobEffectsPredicate mobeffectspredicate = MobEffectsPredicate.fromJson(jsonobject.get("effects"));
         NbtPredicate nbtpredicate = NbtPredicate.fromJson(jsonobject.get("nbt"));
         EntityFlagsPredicate entityflagspredicate = EntityFlagsPredicate.fromJson(jsonobject.get("flags"));
         EntityEquipmentPredicate entityequipmentpredicate = EntityEquipmentPredicate.fromJson(jsonobject.get("equipment"));
         EntitySubPredicate entitysubpredicate = EntitySubPredicate.fromJson(jsonobject.get("type_specific"));
         EntityPredicate entitypredicate = fromJson(jsonobject.get("vehicle"));
         EntityPredicate entitypredicate1 = fromJson(jsonobject.get("passenger"));
         EntityPredicate entitypredicate2 = fromJson(jsonobject.get("targeted_entity"));
         String s = GsonHelper.getAsString(jsonobject, "team", (String)null);
         return (new EntityPredicate.Builder()).entityType(entitytypepredicate).distance(distancepredicate).located(locationpredicate).steppingOn(locationpredicate1).effects(mobeffectspredicate).nbt(nbtpredicate).flags(entityflagspredicate).equipment(entityequipmentpredicate).subPredicate(entitysubpredicate).team(s).vehicle(entitypredicate).passenger(entitypredicate1).targetedEntity(entitypredicate2).build();
      } else {
         return ANY;
      }
   }

   public JsonElement serializeToJson() {
      if (this == ANY) {
         return JsonNull.INSTANCE;
      } else {
         JsonObject jsonobject = new JsonObject();
         jsonobject.add("type", this.entityType.serializeToJson());
         jsonobject.add("distance", this.distanceToPlayer.serializeToJson());
         jsonobject.add("location", this.location.serializeToJson());
         jsonobject.add("stepping_on", this.steppingOnLocation.serializeToJson());
         jsonobject.add("effects", this.effects.serializeToJson());
         jsonobject.add("nbt", this.nbt.serializeToJson());
         jsonobject.add("flags", this.flags.serializeToJson());
         jsonobject.add("equipment", this.equipment.serializeToJson());
         jsonobject.add("type_specific", this.subPredicate.serialize());
         jsonobject.add("vehicle", this.vehicle.serializeToJson());
         jsonobject.add("passenger", this.passenger.serializeToJson());
         jsonobject.add("targeted_entity", this.targetedEntity.serializeToJson());
         jsonobject.addProperty("team", this.team);
         return jsonobject;
      }
   }

   public static LootContext createContext(ServerPlayer serverplayer, Entity entity) {
      LootParams lootparams = (new LootParams.Builder(serverplayer.serverLevel())).withParameter(LootContextParams.THIS_ENTITY, entity).withParameter(LootContextParams.ORIGIN, serverplayer.position()).create(LootContextParamSets.ADVANCEMENT_ENTITY);
      return (new LootContext.Builder(lootparams)).create((ResourceLocation)null);
   }

   public static class Builder {
      private EntityTypePredicate entityType = EntityTypePredicate.ANY;
      private DistancePredicate distanceToPlayer = DistancePredicate.ANY;
      private LocationPredicate location = LocationPredicate.ANY;
      private LocationPredicate steppingOnLocation = LocationPredicate.ANY;
      private MobEffectsPredicate effects = MobEffectsPredicate.ANY;
      private NbtPredicate nbt = NbtPredicate.ANY;
      private EntityFlagsPredicate flags = EntityFlagsPredicate.ANY;
      private EntityEquipmentPredicate equipment = EntityEquipmentPredicate.ANY;
      private EntitySubPredicate subPredicate = EntitySubPredicate.ANY;
      private EntityPredicate vehicle = EntityPredicate.ANY;
      private EntityPredicate passenger = EntityPredicate.ANY;
      private EntityPredicate targetedEntity = EntityPredicate.ANY;
      @Nullable
      private String team;

      public static EntityPredicate.Builder entity() {
         return new EntityPredicate.Builder();
      }

      public EntityPredicate.Builder of(EntityType<?> entitytype) {
         this.entityType = EntityTypePredicate.of(entitytype);
         return this;
      }

      public EntityPredicate.Builder of(TagKey<EntityType<?>> tagkey) {
         this.entityType = EntityTypePredicate.of(tagkey);
         return this;
      }

      public EntityPredicate.Builder entityType(EntityTypePredicate entitytypepredicate) {
         this.entityType = entitytypepredicate;
         return this;
      }

      public EntityPredicate.Builder distance(DistancePredicate distancepredicate) {
         this.distanceToPlayer = distancepredicate;
         return this;
      }

      public EntityPredicate.Builder located(LocationPredicate locationpredicate) {
         this.location = locationpredicate;
         return this;
      }

      public EntityPredicate.Builder steppingOn(LocationPredicate locationpredicate) {
         this.steppingOnLocation = locationpredicate;
         return this;
      }

      public EntityPredicate.Builder effects(MobEffectsPredicate mobeffectspredicate) {
         this.effects = mobeffectspredicate;
         return this;
      }

      public EntityPredicate.Builder nbt(NbtPredicate nbtpredicate) {
         this.nbt = nbtpredicate;
         return this;
      }

      public EntityPredicate.Builder flags(EntityFlagsPredicate entityflagspredicate) {
         this.flags = entityflagspredicate;
         return this;
      }

      public EntityPredicate.Builder equipment(EntityEquipmentPredicate entityequipmentpredicate) {
         this.equipment = entityequipmentpredicate;
         return this;
      }

      public EntityPredicate.Builder subPredicate(EntitySubPredicate entitysubpredicate) {
         this.subPredicate = entitysubpredicate;
         return this;
      }

      public EntityPredicate.Builder vehicle(EntityPredicate entitypredicate) {
         this.vehicle = entitypredicate;
         return this;
      }

      public EntityPredicate.Builder passenger(EntityPredicate entitypredicate) {
         this.passenger = entitypredicate;
         return this;
      }

      public EntityPredicate.Builder targetedEntity(EntityPredicate entitypredicate) {
         this.targetedEntity = entitypredicate;
         return this;
      }

      public EntityPredicate.Builder team(@Nullable String s) {
         this.team = s;
         return this;
      }

      public EntityPredicate build() {
         return new EntityPredicate(this.entityType, this.distanceToPlayer, this.location, this.steppingOnLocation, this.effects, this.nbt, this.flags, this.equipment, this.subPredicate, this.vehicle, this.passenger, this.targetedEntity, this.team);
      }
   }
}
