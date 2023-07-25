package com.mojang.realmsclient.util;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import java.util.Date;
import java.util.UUID;
import java.util.function.Function;
import javax.annotation.Nullable;

public class JsonUtils {
   public static <T> T getRequired(String s, JsonObject jsonobject, Function<JsonObject, T> function) {
      JsonElement jsonelement = jsonobject.get(s);
      if (jsonelement != null && !jsonelement.isJsonNull()) {
         if (!jsonelement.isJsonObject()) {
            throw new IllegalStateException("Required property " + s + " was not a JsonObject as espected");
         } else {
            return function.apply(jsonelement.getAsJsonObject());
         }
      } else {
         throw new IllegalStateException("Missing required property: " + s);
      }
   }

   public static String getRequiredString(String s, JsonObject jsonobject) {
      String s1 = getStringOr(s, jsonobject, (String)null);
      if (s1 == null) {
         throw new IllegalStateException("Missing required property: " + s);
      } else {
         return s1;
      }
   }

   @Nullable
   public static String getStringOr(String s, JsonObject jsonobject, @Nullable String s1) {
      JsonElement jsonelement = jsonobject.get(s);
      if (jsonelement != null) {
         return jsonelement.isJsonNull() ? s1 : jsonelement.getAsString();
      } else {
         return s1;
      }
   }

   @Nullable
   public static UUID getUuidOr(String s, JsonObject jsonobject, @Nullable UUID uuid) {
      String s1 = getStringOr(s, jsonobject, (String)null);
      return s1 == null ? uuid : UUID.fromString(s1);
   }

   public static int getIntOr(String s, JsonObject jsonobject, int i) {
      JsonElement jsonelement = jsonobject.get(s);
      if (jsonelement != null) {
         return jsonelement.isJsonNull() ? i : jsonelement.getAsInt();
      } else {
         return i;
      }
   }

   public static long getLongOr(String s, JsonObject jsonobject, long i) {
      JsonElement jsonelement = jsonobject.get(s);
      if (jsonelement != null) {
         return jsonelement.isJsonNull() ? i : jsonelement.getAsLong();
      } else {
         return i;
      }
   }

   public static boolean getBooleanOr(String s, JsonObject jsonobject, boolean flag) {
      JsonElement jsonelement = jsonobject.get(s);
      if (jsonelement != null) {
         return jsonelement.isJsonNull() ? flag : jsonelement.getAsBoolean();
      } else {
         return flag;
      }
   }

   public static Date getDateOr(String s, JsonObject jsonobject) {
      JsonElement jsonelement = jsonobject.get(s);
      return jsonelement != null ? new Date(Long.parseLong(jsonelement.getAsString())) : new Date();
   }
}
