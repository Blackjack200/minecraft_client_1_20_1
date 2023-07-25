package com.mojang.realmsclient.dto;

import com.google.common.collect.Lists;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.mojang.logging.LogUtils;
import java.util.Iterator;
import java.util.List;
import org.slf4j.Logger;

public class RealmsServerList extends ValueObject {
   private static final Logger LOGGER = LogUtils.getLogger();
   public List<RealmsServer> servers;

   public static RealmsServerList parse(String s) {
      RealmsServerList realmsserverlist = new RealmsServerList();
      realmsserverlist.servers = Lists.newArrayList();

      try {
         JsonParser jsonparser = new JsonParser();
         JsonObject jsonobject = jsonparser.parse(s).getAsJsonObject();
         if (jsonobject.get("servers").isJsonArray()) {
            JsonArray jsonarray = jsonobject.get("servers").getAsJsonArray();
            Iterator<JsonElement> iterator = jsonarray.iterator();

            while(iterator.hasNext()) {
               realmsserverlist.servers.add(RealmsServer.parse(iterator.next().getAsJsonObject()));
            }
         }
      } catch (Exception var6) {
         LOGGER.error("Could not parse McoServerList: {}", (Object)var6.getMessage());
      }

      return realmsserverlist;
   }
}
