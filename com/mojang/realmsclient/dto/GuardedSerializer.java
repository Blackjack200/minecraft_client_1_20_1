package com.mojang.realmsclient.dto;

import com.google.gson.Gson;
import com.google.gson.JsonElement;
import javax.annotation.Nullable;

public class GuardedSerializer {
   private final Gson gson = new Gson();

   public String toJson(ReflectionBasedSerialization reflectionbasedserialization) {
      return this.gson.toJson(reflectionbasedserialization);
   }

   public String toJson(JsonElement jsonelement) {
      return this.gson.toJson(jsonelement);
   }

   @Nullable
   public <T extends ReflectionBasedSerialization> T fromJson(String s, Class<T> oclass) {
      return this.gson.fromJson(s, oclass);
   }
}
