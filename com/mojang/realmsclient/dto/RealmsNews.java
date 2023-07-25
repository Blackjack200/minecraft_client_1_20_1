package com.mojang.realmsclient.dto;

import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.mojang.logging.LogUtils;
import com.mojang.realmsclient.util.JsonUtils;
import org.slf4j.Logger;

public class RealmsNews extends ValueObject {
   private static final Logger LOGGER = LogUtils.getLogger();
   public String newsLink;

   public static RealmsNews parse(String s) {
      RealmsNews realmsnews = new RealmsNews();

      try {
         JsonParser jsonparser = new JsonParser();
         JsonObject jsonobject = jsonparser.parse(s).getAsJsonObject();
         realmsnews.newsLink = JsonUtils.getStringOr("newsLink", jsonobject, (String)null);
      } catch (Exception var4) {
         LOGGER.error("Could not parse RealmsNews: {}", (Object)var4.getMessage());
      }

      return realmsnews;
   }
}
