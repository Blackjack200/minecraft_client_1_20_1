package com.mojang.realmsclient.dto;

import com.google.common.collect.Sets;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import java.util.Set;

public class Ops extends ValueObject {
   public Set<String> ops = Sets.newHashSet();

   public static Ops parse(String s) {
      Ops ops = new Ops();
      JsonParser jsonparser = new JsonParser();

      try {
         JsonElement jsonelement = jsonparser.parse(s);
         JsonObject jsonobject = jsonelement.getAsJsonObject();
         JsonElement jsonelement1 = jsonobject.get("ops");
         if (jsonelement1.isJsonArray()) {
            for(JsonElement jsonelement2 : jsonelement1.getAsJsonArray()) {
               ops.ops.add(jsonelement2.getAsString());
            }
         }
      } catch (Exception var8) {
      }

      return ops;
   }
}
