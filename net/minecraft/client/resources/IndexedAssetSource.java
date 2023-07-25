package net.minecraft.client.resources;

import com.google.common.base.Splitter;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParseException;
import com.mojang.logging.LogUtils;
import java.io.BufferedReader;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.Map;
import net.minecraft.server.packs.linkfs.LinkFileSystem;
import net.minecraft.util.GsonHelper;
import org.slf4j.Logger;

public class IndexedAssetSource {
   private static final Logger LOGGER = LogUtils.getLogger();
   public static final Splitter PATH_SPLITTER = Splitter.on('/');

   public static Path createIndexFs(Path path, String s) {
      Path path1 = path.resolve("objects");
      LinkFileSystem.Builder linkfilesystem_builder = LinkFileSystem.builder();
      Path path2 = path.resolve("indexes/" + s + ".json");

      try {
         BufferedReader bufferedreader = Files.newBufferedReader(path2, StandardCharsets.UTF_8);

         try {
            JsonObject jsonobject = GsonHelper.parse(bufferedreader);
            JsonObject jsonobject1 = GsonHelper.getAsJsonObject(jsonobject, "objects", (JsonObject)null);
            if (jsonobject1 != null) {
               for(Map.Entry<String, JsonElement> map_entry : jsonobject1.entrySet()) {
                  JsonObject jsonobject2 = (JsonObject)map_entry.getValue();
                  String s1 = map_entry.getKey();
                  List<String> list = PATH_SPLITTER.splitToList(s1);
                  String s2 = GsonHelper.getAsString(jsonobject2, "hash");
                  Path path3 = path1.resolve(s2.substring(0, 2) + "/" + s2);
                  linkfilesystem_builder.put(list, path3);
               }
            }
         } catch (Throwable var16) {
            if (bufferedreader != null) {
               try {
                  bufferedreader.close();
               } catch (Throwable var15) {
                  var16.addSuppressed(var15);
               }
            }

            throw var16;
         }

         if (bufferedreader != null) {
            bufferedreader.close();
         }
      } catch (JsonParseException var17) {
         LOGGER.error("Unable to parse resource index file: {}", (Object)path2);
      } catch (IOException var18) {
         LOGGER.error("Can't open the resource index file: {}", (Object)path2);
      }

      return linkfilesystem_builder.build("index-" + s).getPath("/");
   }
}
