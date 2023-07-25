package net.minecraft.advancements.critereon;

import com.google.common.collect.Maps;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParseException;
import com.google.gson.JsonPrimitive;
import it.unimi.dsi.fastutil.objects.Object2BooleanMap;
import it.unimi.dsi.fastutil.objects.Object2BooleanOpenHashMap;
import java.util.Map;
import java.util.function.Predicate;
import javax.annotation.Nullable;
import net.minecraft.advancements.Advancement;
import net.minecraft.advancements.AdvancementProgress;
import net.minecraft.advancements.CriterionProgress;
import net.minecraft.core.Registry;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.PlayerAdvancements;
import net.minecraft.server.ServerAdvancementManager;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.stats.RecipeBook;
import net.minecraft.stats.Stat;
import net.minecraft.stats.StatType;
import net.minecraft.stats.StatsCounter;
import net.minecraft.util.GsonHelper;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.projectile.ProjectileUtil;
import net.minecraft.world.level.GameType;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.EntityHitResult;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.Vec3;

public class PlayerPredicate implements EntitySubPredicate {
   public static final int LOOKING_AT_RANGE = 100;
   private final MinMaxBounds.Ints level;
   @Nullable
   private final GameType gameType;
   private final Map<Stat<?>, MinMaxBounds.Ints> stats;
   private final Object2BooleanMap<ResourceLocation> recipes;
   private final Map<ResourceLocation, PlayerPredicate.AdvancementPredicate> advancements;
   private final EntityPredicate lookingAt;

   private static PlayerPredicate.AdvancementPredicate advancementPredicateFromJson(JsonElement jsonelement) {
      if (jsonelement.isJsonPrimitive()) {
         boolean flag = jsonelement.getAsBoolean();
         return new PlayerPredicate.AdvancementDonePredicate(flag);
      } else {
         Object2BooleanMap<String> object2booleanmap = new Object2BooleanOpenHashMap<>();
         JsonObject jsonobject = GsonHelper.convertToJsonObject(jsonelement, "criterion data");
         jsonobject.entrySet().forEach((map_entry) -> {
            boolean flag1 = GsonHelper.convertToBoolean(map_entry.getValue(), "criterion test");
            object2booleanmap.put(map_entry.getKey(), flag1);
         });
         return new PlayerPredicate.AdvancementCriterionsPredicate(object2booleanmap);
      }
   }

   PlayerPredicate(MinMaxBounds.Ints minmaxbounds_ints, @Nullable GameType gametype, Map<Stat<?>, MinMaxBounds.Ints> map, Object2BooleanMap<ResourceLocation> object2booleanmap, Map<ResourceLocation, PlayerPredicate.AdvancementPredicate> map1, EntityPredicate entitypredicate) {
      this.level = minmaxbounds_ints;
      this.gameType = gametype;
      this.stats = map;
      this.recipes = object2booleanmap;
      this.advancements = map1;
      this.lookingAt = entitypredicate;
   }

   public boolean matches(Entity entity, ServerLevel serverlevel, @Nullable Vec3 vec3) {
      if (!(entity instanceof ServerPlayer serverplayer)) {
         return false;
      } else if (!this.level.matches(serverplayer.experienceLevel)) {
         return false;
      } else if (this.gameType != null && this.gameType != serverplayer.gameMode.getGameModeForPlayer()) {
         return false;
      } else {
         StatsCounter statscounter = serverplayer.getStats();

         for(Map.Entry<Stat<?>, MinMaxBounds.Ints> map_entry : this.stats.entrySet()) {
            int i = statscounter.getValue(map_entry.getKey());
            if (!map_entry.getValue().matches(i)) {
               return false;
            }
         }

         RecipeBook recipebook = serverplayer.getRecipeBook();

         for(Object2BooleanMap.Entry<ResourceLocation> object2booleanmap_entry : this.recipes.object2BooleanEntrySet()) {
            if (recipebook.contains(object2booleanmap_entry.getKey()) != object2booleanmap_entry.getBooleanValue()) {
               return false;
            }
         }

         if (!this.advancements.isEmpty()) {
            PlayerAdvancements playeradvancements = serverplayer.getAdvancements();
            ServerAdvancementManager serveradvancementmanager = serverplayer.getServer().getAdvancements();

            for(Map.Entry<ResourceLocation, PlayerPredicate.AdvancementPredicate> map_entry1 : this.advancements.entrySet()) {
               Advancement advancement = serveradvancementmanager.getAdvancement(map_entry1.getKey());
               if (advancement == null || !map_entry1.getValue().test(playeradvancements.getOrStartProgress(advancement))) {
                  return false;
               }
            }
         }

         if (this.lookingAt != EntityPredicate.ANY) {
            Vec3 vec31 = serverplayer.getEyePosition();
            Vec3 vec32 = serverplayer.getViewVector(1.0F);
            Vec3 vec33 = vec31.add(vec32.x * 100.0D, vec32.y * 100.0D, vec32.z * 100.0D);
            EntityHitResult entityhitresult = ProjectileUtil.getEntityHitResult(serverplayer.level(), serverplayer, vec31, vec33, (new AABB(vec31, vec33)).inflate(1.0D), (entity2) -> !entity2.isSpectator(), 0.0F);
            if (entityhitresult == null || entityhitresult.getType() != HitResult.Type.ENTITY) {
               return false;
            }

            Entity entity1 = entityhitresult.getEntity();
            if (!this.lookingAt.matches(serverplayer, entity1) || !serverplayer.hasLineOfSight(entity1)) {
               return false;
            }
         }

         return true;
      }
   }

   public static PlayerPredicate fromJson(JsonObject jsonobject) {
      MinMaxBounds.Ints minmaxbounds_ints = MinMaxBounds.Ints.fromJson(jsonobject.get("level"));
      String s = GsonHelper.getAsString(jsonobject, "gamemode", "");
      GameType gametype = GameType.byName(s, (GameType)null);
      Map<Stat<?>, MinMaxBounds.Ints> map = Maps.newHashMap();
      JsonArray jsonarray = GsonHelper.getAsJsonArray(jsonobject, "stats", (JsonArray)null);
      if (jsonarray != null) {
         for(JsonElement jsonelement : jsonarray) {
            JsonObject jsonobject1 = GsonHelper.convertToJsonObject(jsonelement, "stats entry");
            ResourceLocation resourcelocation = new ResourceLocation(GsonHelper.getAsString(jsonobject1, "type"));
            StatType<?> stattype = BuiltInRegistries.STAT_TYPE.get(resourcelocation);
            if (stattype == null) {
               throw new JsonParseException("Invalid stat type: " + resourcelocation);
            }

            ResourceLocation resourcelocation1 = new ResourceLocation(GsonHelper.getAsString(jsonobject1, "stat"));
            Stat<?> stat = getStat(stattype, resourcelocation1);
            MinMaxBounds.Ints minmaxbounds_ints1 = MinMaxBounds.Ints.fromJson(jsonobject1.get("value"));
            map.put(stat, minmaxbounds_ints1);
         }
      }

      Object2BooleanMap<ResourceLocation> object2booleanmap = new Object2BooleanOpenHashMap<>();
      JsonObject jsonobject2 = GsonHelper.getAsJsonObject(jsonobject, "recipes", new JsonObject());

      for(Map.Entry<String, JsonElement> map_entry : jsonobject2.entrySet()) {
         ResourceLocation resourcelocation2 = new ResourceLocation(map_entry.getKey());
         boolean flag = GsonHelper.convertToBoolean(map_entry.getValue(), "recipe present");
         object2booleanmap.put(resourcelocation2, flag);
      }

      Map<ResourceLocation, PlayerPredicate.AdvancementPredicate> map1 = Maps.newHashMap();
      JsonObject jsonobject3 = GsonHelper.getAsJsonObject(jsonobject, "advancements", new JsonObject());

      for(Map.Entry<String, JsonElement> map_entry1 : jsonobject3.entrySet()) {
         ResourceLocation resourcelocation3 = new ResourceLocation(map_entry1.getKey());
         PlayerPredicate.AdvancementPredicate playerpredicate_advancementpredicate = advancementPredicateFromJson(map_entry1.getValue());
         map1.put(resourcelocation3, playerpredicate_advancementpredicate);
      }

      EntityPredicate entitypredicate = EntityPredicate.fromJson(jsonobject.get("looking_at"));
      return new PlayerPredicate(minmaxbounds_ints, gametype, map, object2booleanmap, map1, entitypredicate);
   }

   private static <T> Stat<T> getStat(StatType<T> stattype, ResourceLocation resourcelocation) {
      Registry<T> registry = stattype.getRegistry();
      T object = registry.get(resourcelocation);
      if (object == null) {
         throw new JsonParseException("Unknown object " + resourcelocation + " for stat type " + BuiltInRegistries.STAT_TYPE.getKey(stattype));
      } else {
         return stattype.get(object);
      }
   }

   private static <T> ResourceLocation getStatValueId(Stat<T> stat) {
      return stat.getType().getRegistry().getKey(stat.getValue());
   }

   public JsonObject serializeCustomData() {
      JsonObject jsonobject = new JsonObject();
      jsonobject.add("level", this.level.serializeToJson());
      if (this.gameType != null) {
         jsonobject.addProperty("gamemode", this.gameType.getName());
      }

      if (!this.stats.isEmpty()) {
         JsonArray jsonarray = new JsonArray();
         this.stats.forEach((stat, minmaxbounds_ints) -> {
            JsonObject jsonobject5 = new JsonObject();
            jsonobject5.addProperty("type", BuiltInRegistries.STAT_TYPE.getKey(stat.getType()).toString());
            jsonobject5.addProperty("stat", getStatValueId(stat).toString());
            jsonobject5.add("value", minmaxbounds_ints.serializeToJson());
            jsonarray.add(jsonobject5);
         });
         jsonobject.add("stats", jsonarray);
      }

      if (!this.recipes.isEmpty()) {
         JsonObject jsonobject1 = new JsonObject();
         this.recipes.forEach((resourcelocation1, obool) -> jsonobject1.addProperty(resourcelocation1.toString(), obool));
         jsonobject.add("recipes", jsonobject1);
      }

      if (!this.advancements.isEmpty()) {
         JsonObject jsonobject2 = new JsonObject();
         this.advancements.forEach((resourcelocation, playerpredicate_advancementpredicate) -> jsonobject2.add(resourcelocation.toString(), playerpredicate_advancementpredicate.toJson()));
         jsonobject.add("advancements", jsonobject2);
      }

      jsonobject.add("looking_at", this.lookingAt.serializeToJson());
      return jsonobject;
   }

   public EntitySubPredicate.Type type() {
      return EntitySubPredicate.Types.PLAYER;
   }

   static class AdvancementCriterionsPredicate implements PlayerPredicate.AdvancementPredicate {
      private final Object2BooleanMap<String> criterions;

      public AdvancementCriterionsPredicate(Object2BooleanMap<String> object2booleanmap) {
         this.criterions = object2booleanmap;
      }

      public JsonElement toJson() {
         JsonObject jsonobject = new JsonObject();
         this.criterions.forEach(jsonobject::addProperty);
         return jsonobject;
      }

      public boolean test(AdvancementProgress advancementprogress) {
         for(Object2BooleanMap.Entry<String> object2booleanmap_entry : this.criterions.object2BooleanEntrySet()) {
            CriterionProgress criterionprogress = advancementprogress.getCriterion(object2booleanmap_entry.getKey());
            if (criterionprogress == null || criterionprogress.isDone() != object2booleanmap_entry.getBooleanValue()) {
               return false;
            }
         }

         return true;
      }
   }

   static class AdvancementDonePredicate implements PlayerPredicate.AdvancementPredicate {
      private final boolean state;

      public AdvancementDonePredicate(boolean flag) {
         this.state = flag;
      }

      public JsonElement toJson() {
         return new JsonPrimitive(this.state);
      }

      public boolean test(AdvancementProgress advancementprogress) {
         return advancementprogress.isDone() == this.state;
      }
   }

   interface AdvancementPredicate extends Predicate<AdvancementProgress> {
      JsonElement toJson();
   }

   public static class Builder {
      private MinMaxBounds.Ints level = MinMaxBounds.Ints.ANY;
      @Nullable
      private GameType gameType;
      private final Map<Stat<?>, MinMaxBounds.Ints> stats = Maps.newHashMap();
      private final Object2BooleanMap<ResourceLocation> recipes = new Object2BooleanOpenHashMap<>();
      private final Map<ResourceLocation, PlayerPredicate.AdvancementPredicate> advancements = Maps.newHashMap();
      private EntityPredicate lookingAt = EntityPredicate.ANY;

      public static PlayerPredicate.Builder player() {
         return new PlayerPredicate.Builder();
      }

      public PlayerPredicate.Builder setLevel(MinMaxBounds.Ints minmaxbounds_ints) {
         this.level = minmaxbounds_ints;
         return this;
      }

      public PlayerPredicate.Builder addStat(Stat<?> stat, MinMaxBounds.Ints minmaxbounds_ints) {
         this.stats.put(stat, minmaxbounds_ints);
         return this;
      }

      public PlayerPredicate.Builder addRecipe(ResourceLocation resourcelocation, boolean flag) {
         this.recipes.put(resourcelocation, flag);
         return this;
      }

      public PlayerPredicate.Builder setGameType(GameType gametype) {
         this.gameType = gametype;
         return this;
      }

      public PlayerPredicate.Builder setLookingAt(EntityPredicate entitypredicate) {
         this.lookingAt = entitypredicate;
         return this;
      }

      public PlayerPredicate.Builder checkAdvancementDone(ResourceLocation resourcelocation, boolean flag) {
         this.advancements.put(resourcelocation, new PlayerPredicate.AdvancementDonePredicate(flag));
         return this;
      }

      public PlayerPredicate.Builder checkAdvancementCriterions(ResourceLocation resourcelocation, Map<String, Boolean> map) {
         this.advancements.put(resourcelocation, new PlayerPredicate.AdvancementCriterionsPredicate(new Object2BooleanOpenHashMap<>(map)));
         return this;
      }

      public PlayerPredicate build() {
         return new PlayerPredicate(this.level, this.gameType, this.stats, this.recipes, this.advancements, this.lookingAt);
      }
   }
}
