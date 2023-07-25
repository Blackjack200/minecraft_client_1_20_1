package com.mojang.realmsclient.dto;

import com.google.common.collect.Lists;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.mojang.logging.LogUtils;
import com.mojang.realmsclient.util.JsonUtils;
import java.util.List;
import org.slf4j.Logger;

public class RealmsServerPlayerList extends ValueObject {
   private static final Logger LOGGER = LogUtils.getLogger();
   private static final JsonParser JSON_PARSER = new JsonParser();
   public long serverId;
   public List<String> players;

   public static RealmsServerPlayerList parse(JsonObject jsonobject) {
      RealmsServerPlayerList realmsserverplayerlist = new RealmsServerPlayerList();

      try {
         realmsserverplayerlist.serverId = JsonUtils.getLongOr("serverId", jsonobject, -1L);
         String s = JsonUtils.getStringOr("playerList", jsonobject, (String)null);
         if (s != null) {
            JsonElement jsonelement = JSON_PARSER.parse(s);
            if (jsonelement.isJsonArray()) {
               realmsserverplayerlist.players = parsePlayers(jsonelement.getAsJsonArray());
            } else {
               realmsserverplayerlist.players = Lists.newArrayList();
            }
         } else {
            realmsserverplayerlist.players = Lists.newArrayList();
         }
      } catch (Exception var4) {
         LOGGER.error("Could not parse RealmsServerPlayerList: {}", (Object)var4.getMessage());
      }

      return realmsserverplayerlist;
   }

   private static List<String> parsePlayers(JsonArray jsonarray) {
      List<String> list = Lists.newArrayList();

      for(JsonElement jsonelement : jsonarray) {
         try {
            list.add(jsonelement.getAsString());
         } catch (Exception var5) {
         }
      }

      return list;
   }
}
