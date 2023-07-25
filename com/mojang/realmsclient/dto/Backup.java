package com.mojang.realmsclient.dto;

import com.google.common.collect.Maps;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.mojang.logging.LogUtils;
import com.mojang.realmsclient.util.JsonUtils;
import java.util.Date;
import java.util.Map;
import org.slf4j.Logger;

public class Backup extends ValueObject {
   private static final Logger LOGGER = LogUtils.getLogger();
   public String backupId;
   public Date lastModifiedDate;
   public long size;
   private boolean uploadedVersion;
   public Map<String, String> metadata = Maps.newHashMap();
   public Map<String, String> changeList = Maps.newHashMap();

   public static Backup parse(JsonElement jsonelement) {
      JsonObject jsonobject = jsonelement.getAsJsonObject();
      Backup backup = new Backup();

      try {
         backup.backupId = JsonUtils.getStringOr("backupId", jsonobject, "");
         backup.lastModifiedDate = JsonUtils.getDateOr("lastModifiedDate", jsonobject);
         backup.size = JsonUtils.getLongOr("size", jsonobject, 0L);
         if (jsonobject.has("metadata")) {
            JsonObject jsonobject1 = jsonobject.getAsJsonObject("metadata");

            for(Map.Entry<String, JsonElement> map_entry : jsonobject1.entrySet()) {
               if (!map_entry.getValue().isJsonNull()) {
                  backup.metadata.put(map_entry.getKey(), map_entry.getValue().getAsString());
               }
            }
         }
      } catch (Exception var7) {
         LOGGER.error("Could not parse Backup: {}", (Object)var7.getMessage());
      }

      return backup;
   }

   public boolean isUploadedVersion() {
      return this.uploadedVersion;
   }

   public void setUploadedVersion(boolean flag) {
      this.uploadedVersion = flag;
   }
}
