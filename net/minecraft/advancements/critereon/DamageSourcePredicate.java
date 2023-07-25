package net.minecraft.advancements.critereon;

import com.google.common.collect.ImmutableList;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonNull;
import com.google.gson.JsonObject;
import java.util.ArrayList;
import java.util.List;
import javax.annotation.Nullable;
import net.minecraft.core.registries.Registries;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.util.GsonHelper;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.damagesource.DamageType;
import net.minecraft.world.phys.Vec3;

public class DamageSourcePredicate {
   public static final DamageSourcePredicate ANY = DamageSourcePredicate.Builder.damageType().build();
   private final List<TagPredicate<DamageType>> tags;
   private final EntityPredicate directEntity;
   private final EntityPredicate sourceEntity;

   public DamageSourcePredicate(List<TagPredicate<DamageType>> list, EntityPredicate entitypredicate, EntityPredicate entitypredicate1) {
      this.tags = list;
      this.directEntity = entitypredicate;
      this.sourceEntity = entitypredicate1;
   }

   public boolean matches(ServerPlayer serverplayer, DamageSource damagesource) {
      return this.matches(serverplayer.serverLevel(), serverplayer.position(), damagesource);
   }

   public boolean matches(ServerLevel serverlevel, Vec3 vec3, DamageSource damagesource) {
      if (this == ANY) {
         return true;
      } else {
         for(TagPredicate<DamageType> tagpredicate : this.tags) {
            if (!tagpredicate.matches(damagesource.typeHolder())) {
               return false;
            }
         }

         if (!this.directEntity.matches(serverlevel, vec3, damagesource.getDirectEntity())) {
            return false;
         } else {
            return this.sourceEntity.matches(serverlevel, vec3, damagesource.getEntity());
         }
      }
   }

   public static DamageSourcePredicate fromJson(@Nullable JsonElement jsonelement) {
      if (jsonelement != null && !jsonelement.isJsonNull()) {
         JsonObject jsonobject = GsonHelper.convertToJsonObject(jsonelement, "damage type");
         JsonArray jsonarray = GsonHelper.getAsJsonArray(jsonobject, "tags", (JsonArray)null);
         List<TagPredicate<DamageType>> list;
         if (jsonarray != null) {
            list = new ArrayList<>(jsonarray.size());

            for(JsonElement jsonelement1 : jsonarray) {
               list.add(TagPredicate.fromJson(jsonelement1, Registries.DAMAGE_TYPE));
            }
         } else {
            list = List.of();
         }

         EntityPredicate entitypredicate = EntityPredicate.fromJson(jsonobject.get("direct_entity"));
         EntityPredicate entitypredicate1 = EntityPredicate.fromJson(jsonobject.get("source_entity"));
         return new DamageSourcePredicate(list, entitypredicate, entitypredicate1);
      } else {
         return ANY;
      }
   }

   public JsonElement serializeToJson() {
      if (this == ANY) {
         return JsonNull.INSTANCE;
      } else {
         JsonObject jsonobject = new JsonObject();
         if (!this.tags.isEmpty()) {
            JsonArray jsonarray = new JsonArray(this.tags.size());

            for(int i = 0; i < this.tags.size(); ++i) {
               jsonarray.add(this.tags.get(i).serializeToJson());
            }

            jsonobject.add("tags", jsonarray);
         }

         jsonobject.add("direct_entity", this.directEntity.serializeToJson());
         jsonobject.add("source_entity", this.sourceEntity.serializeToJson());
         return jsonobject;
      }
   }

   public static class Builder {
      private final ImmutableList.Builder<TagPredicate<DamageType>> tags = ImmutableList.builder();
      private EntityPredicate directEntity = EntityPredicate.ANY;
      private EntityPredicate sourceEntity = EntityPredicate.ANY;

      public static DamageSourcePredicate.Builder damageType() {
         return new DamageSourcePredicate.Builder();
      }

      public DamageSourcePredicate.Builder tag(TagPredicate<DamageType> tagpredicate) {
         this.tags.add(tagpredicate);
         return this;
      }

      public DamageSourcePredicate.Builder direct(EntityPredicate entitypredicate) {
         this.directEntity = entitypredicate;
         return this;
      }

      public DamageSourcePredicate.Builder direct(EntityPredicate.Builder entitypredicate_builder) {
         this.directEntity = entitypredicate_builder.build();
         return this;
      }

      public DamageSourcePredicate.Builder source(EntityPredicate entitypredicate) {
         this.sourceEntity = entitypredicate;
         return this;
      }

      public DamageSourcePredicate.Builder source(EntityPredicate.Builder entitypredicate_builder) {
         this.sourceEntity = entitypredicate_builder.build();
         return this;
      }

      public DamageSourcePredicate build() {
         return new DamageSourcePredicate(this.tags.build(), this.directEntity, this.sourceEntity);
      }
   }
}
