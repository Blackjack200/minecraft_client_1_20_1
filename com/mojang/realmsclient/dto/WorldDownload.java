package com.mojang.realmsclient.dto;

import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.mojang.logging.LogUtils;
import com.mojang.realmsclient.util.JsonUtils;
import org.slf4j.Logger;

public class WorldDownload extends ValueObject {
   private static final Logger LOGGER = LogUtils.getLogger();
   public String downloadLink;
   public String resourcePackUrl;
   public String resourcePackHash;

   public static WorldDownload parse(String s) {
      JsonParser jsonparser = new JsonParser();
      JsonObject jsonobject = jsonparser.parse(s).getAsJsonObject();
      WorldDownload worlddownload = new WorldDownload();

      try {
         worlddownload.downloadLink = JsonUtils.getStringOr("downloadLink", jsonobject, "");
         worlddownload.resourcePackUrl = JsonUtils.getStringOr("resourcePackUrl", jsonobject, "");
         worlddownload.resourcePackHash = JsonUtils.getStringOr("resourcePackHash", jsonobject, "");
      } catch (Exception var5) {
         LOGGER.error("Could not parse WorldDownload: {}", (Object)var5.getMessage());
      }

      return worlddownload;
   }
}
