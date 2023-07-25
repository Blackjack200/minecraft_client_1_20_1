package net.minecraft.world.level.storage.loot.predicates;

import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonObject;
import com.google.gson.JsonSerializationContext;
import net.minecraft.advancements.critereon.LocationPredicate;
import net.minecraft.core.BlockPos;
import net.minecraft.util.GsonHelper;
import net.minecraft.world.level.storage.loot.LootContext;
import net.minecraft.world.level.storage.loot.parameters.LootContextParams;
import net.minecraft.world.phys.Vec3;

public class LocationCheck implements LootItemCondition {
   final LocationPredicate predicate;
   final BlockPos offset;

   LocationCheck(LocationPredicate locationpredicate, BlockPos blockpos) {
      this.predicate = locationpredicate;
      this.offset = blockpos;
   }

   public LootItemConditionType getType() {
      return LootItemConditions.LOCATION_CHECK;
   }

   public boolean test(LootContext lootcontext) {
      Vec3 vec3 = lootcontext.getParamOrNull(LootContextParams.ORIGIN);
      return vec3 != null && this.predicate.matches(lootcontext.getLevel(), vec3.x() + (double)this.offset.getX(), vec3.y() + (double)this.offset.getY(), vec3.z() + (double)this.offset.getZ());
   }

   public static LootItemCondition.Builder checkLocation(LocationPredicate.Builder locationpredicate_builder) {
      return () -> new LocationCheck(locationpredicate_builder.build(), BlockPos.ZERO);
   }

   public static LootItemCondition.Builder checkLocation(LocationPredicate.Builder locationpredicate_builder, BlockPos blockpos) {
      return () -> new LocationCheck(locationpredicate_builder.build(), blockpos);
   }

   public static class Serializer implements net.minecraft.world.level.storage.loot.Serializer<LocationCheck> {
      public void serialize(JsonObject jsonobject, LocationCheck locationcheck, JsonSerializationContext jsonserializationcontext) {
         jsonobject.add("predicate", locationcheck.predicate.serializeToJson());
         if (locationcheck.offset.getX() != 0) {
            jsonobject.addProperty("offsetX", locationcheck.offset.getX());
         }

         if (locationcheck.offset.getY() != 0) {
            jsonobject.addProperty("offsetY", locationcheck.offset.getY());
         }

         if (locationcheck.offset.getZ() != 0) {
            jsonobject.addProperty("offsetZ", locationcheck.offset.getZ());
         }

      }

      public LocationCheck deserialize(JsonObject jsonobject, JsonDeserializationContext jsondeserializationcontext) {
         LocationPredicate locationpredicate = LocationPredicate.fromJson(jsonobject.get("predicate"));
         int i = GsonHelper.getAsInt(jsonobject, "offsetX", 0);
         int j = GsonHelper.getAsInt(jsonobject, "offsetY", 0);
         int k = GsonHelper.getAsInt(jsonobject, "offsetZ", 0);
         return new LocationCheck(locationpredicate, new BlockPos(i, j, k));
      }
   }
}
