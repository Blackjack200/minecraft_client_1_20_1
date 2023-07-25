package net.minecraft.server;

import com.google.common.collect.Maps;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.mojang.logging.LogUtils;
import java.util.Collection;
import java.util.Map;
import javax.annotation.Nullable;
import net.minecraft.advancements.Advancement;
import net.minecraft.advancements.AdvancementList;
import net.minecraft.advancements.TreeNodePosition;
import net.minecraft.advancements.critereon.DeserializationContext;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.packs.resources.ResourceManager;
import net.minecraft.server.packs.resources.SimpleJsonResourceReloadListener;
import net.minecraft.util.GsonHelper;
import net.minecraft.util.profiling.ProfilerFiller;
import net.minecraft.world.level.storage.loot.LootDataManager;
import org.slf4j.Logger;

public class ServerAdvancementManager extends SimpleJsonResourceReloadListener {
   private static final Logger LOGGER = LogUtils.getLogger();
   private static final Gson GSON = (new GsonBuilder()).create();
   private AdvancementList advancements = new AdvancementList();
   private final LootDataManager lootData;

   public ServerAdvancementManager(LootDataManager lootdatamanager) {
      super(GSON, "advancements");
      this.lootData = lootdatamanager;
   }

   protected void apply(Map<ResourceLocation, JsonElement> map, ResourceManager resourcemanager, ProfilerFiller profilerfiller) {
      Map<ResourceLocation, Advancement.Builder> map1 = Maps.newHashMap();
      map.forEach((resourcelocation, jsonelement) -> {
         try {
            JsonObject jsonobject = GsonHelper.convertToJsonObject(jsonelement, "advancement");
            Advancement.Builder advancement_builder = Advancement.Builder.fromJson(jsonobject, new DeserializationContext(resourcelocation, this.lootData));
            map1.put(resourcelocation, advancement_builder);
         } catch (Exception var6) {
            LOGGER.error("Parsing error loading custom advancement {}: {}", resourcelocation, var6.getMessage());
         }

      });
      AdvancementList advancementlist = new AdvancementList();
      advancementlist.add(map1);

      for(Advancement advancement : advancementlist.getRoots()) {
         if (advancement.getDisplay() != null) {
            TreeNodePosition.run(advancement);
         }
      }

      this.advancements = advancementlist;
   }

   @Nullable
   public Advancement getAdvancement(ResourceLocation resourcelocation) {
      return this.advancements.get(resourcelocation);
   }

   public Collection<Advancement> getAllAdvancements() {
      return this.advancements.getAllAdvancements();
   }
}
