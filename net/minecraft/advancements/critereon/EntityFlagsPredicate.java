package net.minecraft.advancements.critereon;

import com.google.gson.JsonElement;
import com.google.gson.JsonNull;
import com.google.gson.JsonObject;
import javax.annotation.Nullable;
import net.minecraft.util.GsonHelper;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;

public class EntityFlagsPredicate {
   public static final EntityFlagsPredicate ANY = (new EntityFlagsPredicate.Builder()).build();
   @Nullable
   private final Boolean isOnFire;
   @Nullable
   private final Boolean isCrouching;
   @Nullable
   private final Boolean isSprinting;
   @Nullable
   private final Boolean isSwimming;
   @Nullable
   private final Boolean isBaby;

   public EntityFlagsPredicate(@Nullable Boolean obool, @Nullable Boolean obool1, @Nullable Boolean obool2, @Nullable Boolean obool3, @Nullable Boolean obool4) {
      this.isOnFire = obool;
      this.isCrouching = obool1;
      this.isSprinting = obool2;
      this.isSwimming = obool3;
      this.isBaby = obool4;
   }

   public boolean matches(Entity entity) {
      if (this.isOnFire != null && entity.isOnFire() != this.isOnFire) {
         return false;
      } else if (this.isCrouching != null && entity.isCrouching() != this.isCrouching) {
         return false;
      } else if (this.isSprinting != null && entity.isSprinting() != this.isSprinting) {
         return false;
      } else if (this.isSwimming != null && entity.isSwimming() != this.isSwimming) {
         return false;
      } else {
         return this.isBaby == null || !(entity instanceof LivingEntity) || ((LivingEntity)entity).isBaby() == this.isBaby;
      }
   }

   @Nullable
   private static Boolean getOptionalBoolean(JsonObject jsonobject, String s) {
      return jsonobject.has(s) ? GsonHelper.getAsBoolean(jsonobject, s) : null;
   }

   public static EntityFlagsPredicate fromJson(@Nullable JsonElement jsonelement) {
      if (jsonelement != null && !jsonelement.isJsonNull()) {
         JsonObject jsonobject = GsonHelper.convertToJsonObject(jsonelement, "entity flags");
         Boolean obool = getOptionalBoolean(jsonobject, "is_on_fire");
         Boolean obool1 = getOptionalBoolean(jsonobject, "is_sneaking");
         Boolean obool2 = getOptionalBoolean(jsonobject, "is_sprinting");
         Boolean obool3 = getOptionalBoolean(jsonobject, "is_swimming");
         Boolean obool4 = getOptionalBoolean(jsonobject, "is_baby");
         return new EntityFlagsPredicate(obool, obool1, obool2, obool3, obool4);
      } else {
         return ANY;
      }
   }

   private void addOptionalBoolean(JsonObject jsonobject, String s, @Nullable Boolean obool) {
      if (obool != null) {
         jsonobject.addProperty(s, obool);
      }

   }

   public JsonElement serializeToJson() {
      if (this == ANY) {
         return JsonNull.INSTANCE;
      } else {
         JsonObject jsonobject = new JsonObject();
         this.addOptionalBoolean(jsonobject, "is_on_fire", this.isOnFire);
         this.addOptionalBoolean(jsonobject, "is_sneaking", this.isCrouching);
         this.addOptionalBoolean(jsonobject, "is_sprinting", this.isSprinting);
         this.addOptionalBoolean(jsonobject, "is_swimming", this.isSwimming);
         this.addOptionalBoolean(jsonobject, "is_baby", this.isBaby);
         return jsonobject;
      }
   }

   public static class Builder {
      @Nullable
      private Boolean isOnFire;
      @Nullable
      private Boolean isCrouching;
      @Nullable
      private Boolean isSprinting;
      @Nullable
      private Boolean isSwimming;
      @Nullable
      private Boolean isBaby;

      public static EntityFlagsPredicate.Builder flags() {
         return new EntityFlagsPredicate.Builder();
      }

      public EntityFlagsPredicate.Builder setOnFire(@Nullable Boolean obool) {
         this.isOnFire = obool;
         return this;
      }

      public EntityFlagsPredicate.Builder setCrouching(@Nullable Boolean obool) {
         this.isCrouching = obool;
         return this;
      }

      public EntityFlagsPredicate.Builder setSprinting(@Nullable Boolean obool) {
         this.isSprinting = obool;
         return this;
      }

      public EntityFlagsPredicate.Builder setSwimming(@Nullable Boolean obool) {
         this.isSwimming = obool;
         return this;
      }

      public EntityFlagsPredicate.Builder setIsBaby(@Nullable Boolean obool) {
         this.isBaby = obool;
         return this;
      }

      public EntityFlagsPredicate build() {
         return new EntityFlagsPredicate(this.isOnFire, this.isCrouching, this.isSprinting, this.isSwimming, this.isBaby);
      }
   }
}
