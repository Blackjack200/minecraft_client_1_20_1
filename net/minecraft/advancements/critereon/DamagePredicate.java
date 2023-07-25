package net.minecraft.advancements.critereon;

import com.google.gson.JsonElement;
import com.google.gson.JsonNull;
import com.google.gson.JsonObject;
import javax.annotation.Nullable;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.util.GsonHelper;
import net.minecraft.world.damagesource.DamageSource;

public class DamagePredicate {
   public static final DamagePredicate ANY = DamagePredicate.Builder.damageInstance().build();
   private final MinMaxBounds.Doubles dealtDamage;
   private final MinMaxBounds.Doubles takenDamage;
   private final EntityPredicate sourceEntity;
   @Nullable
   private final Boolean blocked;
   private final DamageSourcePredicate type;

   public DamagePredicate() {
      this.dealtDamage = MinMaxBounds.Doubles.ANY;
      this.takenDamage = MinMaxBounds.Doubles.ANY;
      this.sourceEntity = EntityPredicate.ANY;
      this.blocked = null;
      this.type = DamageSourcePredicate.ANY;
   }

   public DamagePredicate(MinMaxBounds.Doubles minmaxbounds_doubles, MinMaxBounds.Doubles minmaxbounds_doubles1, EntityPredicate entitypredicate, @Nullable Boolean obool, DamageSourcePredicate damagesourcepredicate) {
      this.dealtDamage = minmaxbounds_doubles;
      this.takenDamage = minmaxbounds_doubles1;
      this.sourceEntity = entitypredicate;
      this.blocked = obool;
      this.type = damagesourcepredicate;
   }

   public boolean matches(ServerPlayer serverplayer, DamageSource damagesource, float f, float f1, boolean flag) {
      if (this == ANY) {
         return true;
      } else if (!this.dealtDamage.matches((double)f)) {
         return false;
      } else if (!this.takenDamage.matches((double)f1)) {
         return false;
      } else if (!this.sourceEntity.matches(serverplayer, damagesource.getEntity())) {
         return false;
      } else if (this.blocked != null && this.blocked != flag) {
         return false;
      } else {
         return this.type.matches(serverplayer, damagesource);
      }
   }

   public static DamagePredicate fromJson(@Nullable JsonElement jsonelement) {
      if (jsonelement != null && !jsonelement.isJsonNull()) {
         JsonObject jsonobject = GsonHelper.convertToJsonObject(jsonelement, "damage");
         MinMaxBounds.Doubles minmaxbounds_doubles = MinMaxBounds.Doubles.fromJson(jsonobject.get("dealt"));
         MinMaxBounds.Doubles minmaxbounds_doubles1 = MinMaxBounds.Doubles.fromJson(jsonobject.get("taken"));
         Boolean obool = jsonobject.has("blocked") ? GsonHelper.getAsBoolean(jsonobject, "blocked") : null;
         EntityPredicate entitypredicate = EntityPredicate.fromJson(jsonobject.get("source_entity"));
         DamageSourcePredicate damagesourcepredicate = DamageSourcePredicate.fromJson(jsonobject.get("type"));
         return new DamagePredicate(minmaxbounds_doubles, minmaxbounds_doubles1, entitypredicate, obool, damagesourcepredicate);
      } else {
         return ANY;
      }
   }

   public JsonElement serializeToJson() {
      if (this == ANY) {
         return JsonNull.INSTANCE;
      } else {
         JsonObject jsonobject = new JsonObject();
         jsonobject.add("dealt", this.dealtDamage.serializeToJson());
         jsonobject.add("taken", this.takenDamage.serializeToJson());
         jsonobject.add("source_entity", this.sourceEntity.serializeToJson());
         jsonobject.add("type", this.type.serializeToJson());
         if (this.blocked != null) {
            jsonobject.addProperty("blocked", this.blocked);
         }

         return jsonobject;
      }
   }

   public static class Builder {
      private MinMaxBounds.Doubles dealtDamage = MinMaxBounds.Doubles.ANY;
      private MinMaxBounds.Doubles takenDamage = MinMaxBounds.Doubles.ANY;
      private EntityPredicate sourceEntity = EntityPredicate.ANY;
      @Nullable
      private Boolean blocked;
      private DamageSourcePredicate type = DamageSourcePredicate.ANY;

      public static DamagePredicate.Builder damageInstance() {
         return new DamagePredicate.Builder();
      }

      public DamagePredicate.Builder dealtDamage(MinMaxBounds.Doubles minmaxbounds_doubles) {
         this.dealtDamage = minmaxbounds_doubles;
         return this;
      }

      public DamagePredicate.Builder takenDamage(MinMaxBounds.Doubles minmaxbounds_doubles) {
         this.takenDamage = minmaxbounds_doubles;
         return this;
      }

      public DamagePredicate.Builder sourceEntity(EntityPredicate entitypredicate) {
         this.sourceEntity = entitypredicate;
         return this;
      }

      public DamagePredicate.Builder blocked(Boolean obool) {
         this.blocked = obool;
         return this;
      }

      public DamagePredicate.Builder type(DamageSourcePredicate damagesourcepredicate) {
         this.type = damagesourcepredicate;
         return this;
      }

      public DamagePredicate.Builder type(DamageSourcePredicate.Builder damagesourcepredicate_builder) {
         this.type = damagesourcepredicate_builder.build();
         return this;
      }

      public DamagePredicate build() {
         return new DamagePredicate(this.dealtDamage, this.takenDamage, this.sourceEntity, this.blocked, this.type);
      }
   }
}
