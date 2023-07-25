package com.mojang.realmsclient.util;

import it.unimi.dsi.fastutil.longs.Long2ObjectMap;
import it.unimi.dsi.fastutil.longs.Long2ObjectOpenHashMap;

public class UploadTokenCache {
   private static final Long2ObjectMap<String> TOKEN_CACHE = new Long2ObjectOpenHashMap<>();

   public static String get(long i) {
      return TOKEN_CACHE.get(i);
   }

   public static void invalidate(long i) {
      TOKEN_CACHE.remove(i);
   }

   public static void put(long i, String s) {
      TOKEN_CACHE.put(i, s);
   }
}
