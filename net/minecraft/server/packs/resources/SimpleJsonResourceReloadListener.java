package net.minecraft.server.packs.resources;

import com.google.gson.Gson;
import com.google.gson.JsonElement;
import com.google.gson.JsonParseException;
import com.mojang.logging.LogUtils;
import java.io.IOException;
import java.io.Reader;
import java.util.HashMap;
import java.util.Map;
import net.minecraft.resources.FileToIdConverter;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.GsonHelper;
import net.minecraft.util.profiling.ProfilerFiller;
import org.slf4j.Logger;

public abstract class SimpleJsonResourceReloadListener extends SimplePreparableReloadListener<Map<ResourceLocation, JsonElement>> {
   private static final Logger LOGGER = LogUtils.getLogger();
   private final Gson gson;
   private final String directory;

   public SimpleJsonResourceReloadListener(Gson gson, String s) {
      this.gson = gson;
      this.directory = s;
   }

   protected Map<ResourceLocation, JsonElement> prepare(ResourceManager resourcemanager, ProfilerFiller profilerfiller) {
      Map<ResourceLocation, JsonElement> map = new HashMap<>();
      scanDirectory(resourcemanager, this.directory, this.gson, map);
      return map;
   }

   public static void scanDirectory(ResourceManager resourcemanager, String s, Gson gson, Map<ResourceLocation, JsonElement> map) {
      FileToIdConverter filetoidconverter = FileToIdConverter.json(s);

      for(Map.Entry<ResourceLocation, Resource> map_entry : filetoidconverter.listMatchingResources(resourcemanager).entrySet()) {
         ResourceLocation resourcelocation = map_entry.getKey();
         ResourceLocation resourcelocation1 = filetoidconverter.fileToId(resourcelocation);

         try {
            Reader reader = map_entry.getValue().openAsReader();

            try {
               JsonElement jsonelement = GsonHelper.fromJson(gson, reader, JsonElement.class);
               JsonElement jsonelement1 = map.put(resourcelocation1, jsonelement);
               if (jsonelement1 != null) {
                  throw new IllegalStateException("Duplicate data file ignored with ID " + resourcelocation1);
               }
            } catch (Throwable var13) {
               if (reader != null) {
                  try {
                     reader.close();
                  } catch (Throwable var12) {
                     var13.addSuppressed(var12);
                  }
               }

               throw var13;
            }

            if (reader != null) {
               reader.close();
            }
         } catch (IllegalArgumentException | IOException | JsonParseException var14) {
            LOGGER.error("Couldn't parse data file {} from {}", resourcelocation1, resourcelocation, var14);
         }
      }

   }
}
