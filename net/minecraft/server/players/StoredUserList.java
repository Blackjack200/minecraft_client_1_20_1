package net.minecraft.server.players;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.google.common.io.Files;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.mojang.logging.LogUtils;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import javax.annotation.Nullable;
import net.minecraft.Util;
import net.minecraft.util.GsonHelper;
import org.slf4j.Logger;

public abstract class StoredUserList<K, V extends StoredUserEntry<K>> {
   private static final Logger LOGGER = LogUtils.getLogger();
   private static final Gson GSON = (new GsonBuilder()).setPrettyPrinting().create();
   private final File file;
   private final Map<String, V> map = Maps.newHashMap();

   public StoredUserList(File file) {
      this.file = file;
   }

   public File getFile() {
      return this.file;
   }

   public void add(V storeduserentry) {
      this.map.put(this.getKeyForUser(storeduserentry.getUser()), storeduserentry);

      try {
         this.save();
      } catch (IOException var3) {
         LOGGER.warn("Could not save the list after adding a user.", (Throwable)var3);
      }

   }

   @Nullable
   public V get(K object) {
      this.removeExpired();
      return this.map.get(this.getKeyForUser(object));
   }

   public void remove(K object) {
      this.map.remove(this.getKeyForUser(object));

      try {
         this.save();
      } catch (IOException var3) {
         LOGGER.warn("Could not save the list after removing a user.", (Throwable)var3);
      }

   }

   public void remove(StoredUserEntry<K> storeduserentry) {
      this.remove(storeduserentry.getUser());
   }

   public String[] getUserList() {
      return this.map.keySet().toArray(new String[0]);
   }

   public boolean isEmpty() {
      return this.map.size() < 1;
   }

   protected String getKeyForUser(K object) {
      return object.toString();
   }

   protected boolean contains(K object) {
      return this.map.containsKey(this.getKeyForUser(object));
   }

   private void removeExpired() {
      List<K> list = Lists.newArrayList();

      for(V storeduserentry : this.map.values()) {
         if (storeduserentry.hasExpired()) {
            list.add(storeduserentry.getUser());
         }
      }

      for(K object : list) {
         this.map.remove(this.getKeyForUser(object));
      }

   }

   protected abstract StoredUserEntry<K> createEntry(JsonObject jsonobject);

   public Collection<V> getEntries() {
      return this.map.values();
   }

   public void save() throws IOException {
      JsonArray jsonarray = new JsonArray();
      this.map.values().stream().map((storeduserentry) -> Util.make(new JsonObject(), storeduserentry::serialize)).forEach(jsonarray::add);
      BufferedWriter bufferedwriter = Files.newWriter(this.file, StandardCharsets.UTF_8);

      try {
         GSON.toJson((JsonElement)jsonarray, bufferedwriter);
      } catch (Throwable var6) {
         if (bufferedwriter != null) {
            try {
               bufferedwriter.close();
            } catch (Throwable var5) {
               var6.addSuppressed(var5);
            }
         }

         throw var6;
      }

      if (bufferedwriter != null) {
         bufferedwriter.close();
      }

   }

   public void load() throws IOException {
      if (this.file.exists()) {
         BufferedReader bufferedreader = Files.newReader(this.file, StandardCharsets.UTF_8);

         try {
            JsonArray jsonarray = GSON.fromJson(bufferedreader, JsonArray.class);
            this.map.clear();

            for(JsonElement jsonelement : jsonarray) {
               JsonObject jsonobject = GsonHelper.convertToJsonObject(jsonelement, "entry");
               StoredUserEntry<K> storeduserentry = this.createEntry(jsonobject);
               if (storeduserentry.getUser() != null) {
                  this.map.put(this.getKeyForUser(storeduserentry.getUser()), (V)storeduserentry);
               }
            }
         } catch (Throwable var8) {
            if (bufferedreader != null) {
               try {
                  bufferedreader.close();
               } catch (Throwable var7) {
                  var8.addSuppressed(var7);
               }
            }

            throw var8;
         }

         if (bufferedreader != null) {
            bufferedreader.close();
         }

      }
   }
}
