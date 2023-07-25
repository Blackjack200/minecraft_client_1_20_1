package net.minecraft.world.level.storage.loot.functions;

import com.google.common.collect.ImmutableSet;
import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonObject;
import com.google.gson.JsonSerializationContext;
import com.mojang.logging.LogUtils;
import java.util.Locale;
import java.util.Set;
import net.minecraft.core.BlockPos;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.tags.StructureTags;
import net.minecraft.tags.TagKey;
import net.minecraft.util.GsonHelper;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.MapItem;
import net.minecraft.world.level.levelgen.structure.Structure;
import net.minecraft.world.level.saveddata.maps.MapDecoration;
import net.minecraft.world.level.saveddata.maps.MapItemSavedData;
import net.minecraft.world.level.storage.loot.LootContext;
import net.minecraft.world.level.storage.loot.parameters.LootContextParam;
import net.minecraft.world.level.storage.loot.parameters.LootContextParams;
import net.minecraft.world.level.storage.loot.predicates.LootItemCondition;
import net.minecraft.world.phys.Vec3;
import org.slf4j.Logger;

public class ExplorationMapFunction extends LootItemConditionalFunction {
   static final Logger LOGGER = LogUtils.getLogger();
   public static final TagKey<Structure> DEFAULT_DESTINATION = StructureTags.ON_TREASURE_MAPS;
   public static final String DEFAULT_DECORATION_NAME = "mansion";
   public static final MapDecoration.Type DEFAULT_DECORATION = MapDecoration.Type.MANSION;
   public static final byte DEFAULT_ZOOM = 2;
   public static final int DEFAULT_SEARCH_RADIUS = 50;
   public static final boolean DEFAULT_SKIP_EXISTING = true;
   final TagKey<Structure> destination;
   final MapDecoration.Type mapDecoration;
   final byte zoom;
   final int searchRadius;
   final boolean skipKnownStructures;

   ExplorationMapFunction(LootItemCondition[] alootitemcondition, TagKey<Structure> tagkey, MapDecoration.Type mapdecoration_type, byte b0, int i, boolean flag) {
      super(alootitemcondition);
      this.destination = tagkey;
      this.mapDecoration = mapdecoration_type;
      this.zoom = b0;
      this.searchRadius = i;
      this.skipKnownStructures = flag;
   }

   public LootItemFunctionType getType() {
      return LootItemFunctions.EXPLORATION_MAP;
   }

   public Set<LootContextParam<?>> getReferencedContextParams() {
      return ImmutableSet.of(LootContextParams.ORIGIN);
   }

   public ItemStack run(ItemStack itemstack, LootContext lootcontext) {
      if (!itemstack.is(Items.MAP)) {
         return itemstack;
      } else {
         Vec3 vec3 = lootcontext.getParamOrNull(LootContextParams.ORIGIN);
         if (vec3 != null) {
            ServerLevel serverlevel = lootcontext.getLevel();
            BlockPos blockpos = serverlevel.findNearestMapStructure(this.destination, BlockPos.containing(vec3), this.searchRadius, this.skipKnownStructures);
            if (blockpos != null) {
               ItemStack itemstack1 = MapItem.create(serverlevel, blockpos.getX(), blockpos.getZ(), this.zoom, true, true);
               MapItem.renderBiomePreviewMap(serverlevel, itemstack1);
               MapItemSavedData.addTargetDecoration(itemstack1, blockpos, "+", this.mapDecoration);
               return itemstack1;
            }
         }

         return itemstack;
      }
   }

   public static ExplorationMapFunction.Builder makeExplorationMap() {
      return new ExplorationMapFunction.Builder();
   }

   public static class Builder extends LootItemConditionalFunction.Builder<ExplorationMapFunction.Builder> {
      private TagKey<Structure> destination = ExplorationMapFunction.DEFAULT_DESTINATION;
      private MapDecoration.Type mapDecoration = ExplorationMapFunction.DEFAULT_DECORATION;
      private byte zoom = 2;
      private int searchRadius = 50;
      private boolean skipKnownStructures = true;

      protected ExplorationMapFunction.Builder getThis() {
         return this;
      }

      public ExplorationMapFunction.Builder setDestination(TagKey<Structure> tagkey) {
         this.destination = tagkey;
         return this;
      }

      public ExplorationMapFunction.Builder setMapDecoration(MapDecoration.Type mapdecoration_type) {
         this.mapDecoration = mapdecoration_type;
         return this;
      }

      public ExplorationMapFunction.Builder setZoom(byte b0) {
         this.zoom = b0;
         return this;
      }

      public ExplorationMapFunction.Builder setSearchRadius(int i) {
         this.searchRadius = i;
         return this;
      }

      public ExplorationMapFunction.Builder setSkipKnownStructures(boolean flag) {
         this.skipKnownStructures = flag;
         return this;
      }

      public LootItemFunction build() {
         return new ExplorationMapFunction(this.getConditions(), this.destination, this.mapDecoration, this.zoom, this.searchRadius, this.skipKnownStructures);
      }
   }

   public static class Serializer extends LootItemConditionalFunction.Serializer<ExplorationMapFunction> {
      public void serialize(JsonObject jsonobject, ExplorationMapFunction explorationmapfunction, JsonSerializationContext jsonserializationcontext) {
         super.serialize(jsonobject, explorationmapfunction, jsonserializationcontext);
         if (!explorationmapfunction.destination.equals(ExplorationMapFunction.DEFAULT_DESTINATION)) {
            jsonobject.addProperty("destination", explorationmapfunction.destination.location().toString());
         }

         if (explorationmapfunction.mapDecoration != ExplorationMapFunction.DEFAULT_DECORATION) {
            jsonobject.add("decoration", jsonserializationcontext.serialize(explorationmapfunction.mapDecoration.toString().toLowerCase(Locale.ROOT)));
         }

         if (explorationmapfunction.zoom != 2) {
            jsonobject.addProperty("zoom", explorationmapfunction.zoom);
         }

         if (explorationmapfunction.searchRadius != 50) {
            jsonobject.addProperty("search_radius", explorationmapfunction.searchRadius);
         }

         if (!explorationmapfunction.skipKnownStructures) {
            jsonobject.addProperty("skip_existing_chunks", explorationmapfunction.skipKnownStructures);
         }

      }

      public ExplorationMapFunction deserialize(JsonObject jsonobject, JsonDeserializationContext jsondeserializationcontext, LootItemCondition[] alootitemcondition) {
         TagKey<Structure> tagkey = readStructure(jsonobject);
         String s = jsonobject.has("decoration") ? GsonHelper.getAsString(jsonobject, "decoration") : "mansion";
         MapDecoration.Type mapdecoration_type = ExplorationMapFunction.DEFAULT_DECORATION;

         try {
            mapdecoration_type = MapDecoration.Type.valueOf(s.toUpperCase(Locale.ROOT));
         } catch (IllegalArgumentException var10) {
            ExplorationMapFunction.LOGGER.error("Error while parsing loot table decoration entry. Found {}. Defaulting to {}", s, ExplorationMapFunction.DEFAULT_DECORATION);
         }

         byte b0 = GsonHelper.getAsByte(jsonobject, "zoom", (byte)2);
         int i = GsonHelper.getAsInt(jsonobject, "search_radius", 50);
         boolean flag = GsonHelper.getAsBoolean(jsonobject, "skip_existing_chunks", true);
         return new ExplorationMapFunction(alootitemcondition, tagkey, mapdecoration_type, b0, i, flag);
      }

      private static TagKey<Structure> readStructure(JsonObject jsonobject) {
         if (jsonobject.has("destination")) {
            String s = GsonHelper.getAsString(jsonobject, "destination");
            return TagKey.create(Registries.STRUCTURE, new ResourceLocation(s));
         } else {
            return ExplorationMapFunction.DEFAULT_DESTINATION;
         }
      }
   }
}
