package net.minecraft.advancements.critereon;

import com.google.gson.JsonElement;
import com.google.gson.JsonNull;
import com.google.gson.JsonObject;
import com.mojang.logging.LogUtils;
import com.mojang.serialization.JsonOps;
import javax.annotation.Nullable;
import net.minecraft.core.BlockPos;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.GsonHelper;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.biome.Biome;
import net.minecraft.world.level.block.CampfireBlock;
import net.minecraft.world.level.levelgen.structure.Structure;
import org.slf4j.Logger;

public class LocationPredicate {
   private static final Logger LOGGER = LogUtils.getLogger();
   public static final LocationPredicate ANY = new LocationPredicate(MinMaxBounds.Doubles.ANY, MinMaxBounds.Doubles.ANY, MinMaxBounds.Doubles.ANY, (ResourceKey<Biome>)null, (ResourceKey<Structure>)null, (ResourceKey<Level>)null, (Boolean)null, LightPredicate.ANY, BlockPredicate.ANY, FluidPredicate.ANY);
   private final MinMaxBounds.Doubles x;
   private final MinMaxBounds.Doubles y;
   private final MinMaxBounds.Doubles z;
   @Nullable
   private final ResourceKey<Biome> biome;
   @Nullable
   private final ResourceKey<Structure> structure;
   @Nullable
   private final ResourceKey<Level> dimension;
   @Nullable
   private final Boolean smokey;
   private final LightPredicate light;
   private final BlockPredicate block;
   private final FluidPredicate fluid;

   public LocationPredicate(MinMaxBounds.Doubles minmaxbounds_doubles, MinMaxBounds.Doubles minmaxbounds_doubles1, MinMaxBounds.Doubles minmaxbounds_doubles2, @Nullable ResourceKey<Biome> resourcekey, @Nullable ResourceKey<Structure> resourcekey1, @Nullable ResourceKey<Level> resourcekey2, @Nullable Boolean obool, LightPredicate lightpredicate, BlockPredicate blockpredicate, FluidPredicate fluidpredicate) {
      this.x = minmaxbounds_doubles;
      this.y = minmaxbounds_doubles1;
      this.z = minmaxbounds_doubles2;
      this.biome = resourcekey;
      this.structure = resourcekey1;
      this.dimension = resourcekey2;
      this.smokey = obool;
      this.light = lightpredicate;
      this.block = blockpredicate;
      this.fluid = fluidpredicate;
   }

   public static LocationPredicate inBiome(ResourceKey<Biome> resourcekey) {
      return new LocationPredicate(MinMaxBounds.Doubles.ANY, MinMaxBounds.Doubles.ANY, MinMaxBounds.Doubles.ANY, resourcekey, (ResourceKey<Structure>)null, (ResourceKey<Level>)null, (Boolean)null, LightPredicate.ANY, BlockPredicate.ANY, FluidPredicate.ANY);
   }

   public static LocationPredicate inDimension(ResourceKey<Level> resourcekey) {
      return new LocationPredicate(MinMaxBounds.Doubles.ANY, MinMaxBounds.Doubles.ANY, MinMaxBounds.Doubles.ANY, (ResourceKey<Biome>)null, (ResourceKey<Structure>)null, resourcekey, (Boolean)null, LightPredicate.ANY, BlockPredicate.ANY, FluidPredicate.ANY);
   }

   public static LocationPredicate inStructure(ResourceKey<Structure> resourcekey) {
      return new LocationPredicate(MinMaxBounds.Doubles.ANY, MinMaxBounds.Doubles.ANY, MinMaxBounds.Doubles.ANY, (ResourceKey<Biome>)null, resourcekey, (ResourceKey<Level>)null, (Boolean)null, LightPredicate.ANY, BlockPredicate.ANY, FluidPredicate.ANY);
   }

   public static LocationPredicate atYLocation(MinMaxBounds.Doubles minmaxbounds_doubles) {
      return new LocationPredicate(MinMaxBounds.Doubles.ANY, minmaxbounds_doubles, MinMaxBounds.Doubles.ANY, (ResourceKey<Biome>)null, (ResourceKey<Structure>)null, (ResourceKey<Level>)null, (Boolean)null, LightPredicate.ANY, BlockPredicate.ANY, FluidPredicate.ANY);
   }

   public boolean matches(ServerLevel serverlevel, double d0, double d1, double d2) {
      if (!this.x.matches(d0)) {
         return false;
      } else if (!this.y.matches(d1)) {
         return false;
      } else if (!this.z.matches(d2)) {
         return false;
      } else if (this.dimension != null && this.dimension != serverlevel.dimension()) {
         return false;
      } else {
         BlockPos blockpos = BlockPos.containing(d0, d1, d2);
         boolean flag = serverlevel.isLoaded(blockpos);
         if (this.biome == null || flag && serverlevel.getBiome(blockpos).is(this.biome)) {
            if (this.structure == null || flag && serverlevel.structureManager().getStructureWithPieceAt(blockpos, this.structure).isValid()) {
               if (this.smokey == null || flag && this.smokey == CampfireBlock.isSmokeyPos(serverlevel, blockpos)) {
                  if (!this.light.matches(serverlevel, blockpos)) {
                     return false;
                  } else if (!this.block.matches(serverlevel, blockpos)) {
                     return false;
                  } else {
                     return this.fluid.matches(serverlevel, blockpos);
                  }
               } else {
                  return false;
               }
            } else {
               return false;
            }
         } else {
            return false;
         }
      }
   }

   public JsonElement serializeToJson() {
      if (this == ANY) {
         return JsonNull.INSTANCE;
      } else {
         JsonObject jsonobject = new JsonObject();
         if (!this.x.isAny() || !this.y.isAny() || !this.z.isAny()) {
            JsonObject jsonobject1 = new JsonObject();
            jsonobject1.add("x", this.x.serializeToJson());
            jsonobject1.add("y", this.y.serializeToJson());
            jsonobject1.add("z", this.z.serializeToJson());
            jsonobject.add("position", jsonobject1);
         }

         if (this.dimension != null) {
            Level.RESOURCE_KEY_CODEC.encodeStart(JsonOps.INSTANCE, this.dimension).resultOrPartial(LOGGER::error).ifPresent((jsonelement) -> jsonobject.add("dimension", jsonelement));
         }

         if (this.structure != null) {
            jsonobject.addProperty("structure", this.structure.location().toString());
         }

         if (this.biome != null) {
            jsonobject.addProperty("biome", this.biome.location().toString());
         }

         if (this.smokey != null) {
            jsonobject.addProperty("smokey", this.smokey);
         }

         jsonobject.add("light", this.light.serializeToJson());
         jsonobject.add("block", this.block.serializeToJson());
         jsonobject.add("fluid", this.fluid.serializeToJson());
         return jsonobject;
      }
   }

   public static LocationPredicate fromJson(@Nullable JsonElement jsonelement) {
      if (jsonelement != null && !jsonelement.isJsonNull()) {
         JsonObject jsonobject = GsonHelper.convertToJsonObject(jsonelement, "location");
         JsonObject jsonobject1 = GsonHelper.getAsJsonObject(jsonobject, "position", new JsonObject());
         MinMaxBounds.Doubles minmaxbounds_doubles = MinMaxBounds.Doubles.fromJson(jsonobject1.get("x"));
         MinMaxBounds.Doubles minmaxbounds_doubles1 = MinMaxBounds.Doubles.fromJson(jsonobject1.get("y"));
         MinMaxBounds.Doubles minmaxbounds_doubles2 = MinMaxBounds.Doubles.fromJson(jsonobject1.get("z"));
         ResourceKey<Level> resourcekey = jsonobject.has("dimension") ? ResourceLocation.CODEC.parse(JsonOps.INSTANCE, jsonobject.get("dimension")).resultOrPartial(LOGGER::error).map((resourcelocation2) -> ResourceKey.create(Registries.DIMENSION, resourcelocation2)).orElse((ResourceKey<Level>)null) : null;
         ResourceKey<Structure> resourcekey1 = jsonobject.has("structure") ? ResourceLocation.CODEC.parse(JsonOps.INSTANCE, jsonobject.get("structure")).resultOrPartial(LOGGER::error).map((resourcelocation1) -> ResourceKey.create(Registries.STRUCTURE, resourcelocation1)).orElse((ResourceKey<Structure>)null) : null;
         ResourceKey<Biome> resourcekey2 = null;
         if (jsonobject.has("biome")) {
            ResourceLocation resourcelocation = new ResourceLocation(GsonHelper.getAsString(jsonobject, "biome"));
            resourcekey2 = ResourceKey.create(Registries.BIOME, resourcelocation);
         }

         Boolean obool = jsonobject.has("smokey") ? jsonobject.get("smokey").getAsBoolean() : null;
         LightPredicate lightpredicate = LightPredicate.fromJson(jsonobject.get("light"));
         BlockPredicate blockpredicate = BlockPredicate.fromJson(jsonobject.get("block"));
         FluidPredicate fluidpredicate = FluidPredicate.fromJson(jsonobject.get("fluid"));
         return new LocationPredicate(minmaxbounds_doubles, minmaxbounds_doubles1, minmaxbounds_doubles2, resourcekey2, resourcekey1, resourcekey, obool, lightpredicate, blockpredicate, fluidpredicate);
      } else {
         return ANY;
      }
   }

   public static class Builder {
      private MinMaxBounds.Doubles x = MinMaxBounds.Doubles.ANY;
      private MinMaxBounds.Doubles y = MinMaxBounds.Doubles.ANY;
      private MinMaxBounds.Doubles z = MinMaxBounds.Doubles.ANY;
      @Nullable
      private ResourceKey<Biome> biome;
      @Nullable
      private ResourceKey<Structure> structure;
      @Nullable
      private ResourceKey<Level> dimension;
      @Nullable
      private Boolean smokey;
      private LightPredicate light = LightPredicate.ANY;
      private BlockPredicate block = BlockPredicate.ANY;
      private FluidPredicate fluid = FluidPredicate.ANY;

      public static LocationPredicate.Builder location() {
         return new LocationPredicate.Builder();
      }

      public LocationPredicate.Builder setX(MinMaxBounds.Doubles minmaxbounds_doubles) {
         this.x = minmaxbounds_doubles;
         return this;
      }

      public LocationPredicate.Builder setY(MinMaxBounds.Doubles minmaxbounds_doubles) {
         this.y = minmaxbounds_doubles;
         return this;
      }

      public LocationPredicate.Builder setZ(MinMaxBounds.Doubles minmaxbounds_doubles) {
         this.z = minmaxbounds_doubles;
         return this;
      }

      public LocationPredicate.Builder setBiome(@Nullable ResourceKey<Biome> resourcekey) {
         this.biome = resourcekey;
         return this;
      }

      public LocationPredicate.Builder setStructure(@Nullable ResourceKey<Structure> resourcekey) {
         this.structure = resourcekey;
         return this;
      }

      public LocationPredicate.Builder setDimension(@Nullable ResourceKey<Level> resourcekey) {
         this.dimension = resourcekey;
         return this;
      }

      public LocationPredicate.Builder setLight(LightPredicate lightpredicate) {
         this.light = lightpredicate;
         return this;
      }

      public LocationPredicate.Builder setBlock(BlockPredicate blockpredicate) {
         this.block = blockpredicate;
         return this;
      }

      public LocationPredicate.Builder setFluid(FluidPredicate fluidpredicate) {
         this.fluid = fluidpredicate;
         return this;
      }

      public LocationPredicate.Builder setSmokey(Boolean obool) {
         this.smokey = obool;
         return this;
      }

      public LocationPredicate build() {
         return new LocationPredicate(this.x, this.y, this.z, this.biome, this.structure, this.dimension, this.smokey, this.light, this.block, this.fluid);
      }
   }
}
