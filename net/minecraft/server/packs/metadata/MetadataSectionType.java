package net.minecraft.server.packs.metadata;

import com.google.gson.JsonObject;
import com.mojang.serialization.Codec;
import com.mojang.serialization.JsonOps;

public interface MetadataSectionType<T> extends MetadataSectionSerializer<T> {
   JsonObject toJson(T object);

   static <T> MetadataSectionType<T> fromCodec(final String s, final Codec<T> codec) {
      return new MetadataSectionType<T>() {
         public String getMetadataSectionName() {
            return s;
         }

         public T fromJson(JsonObject jsonobject) {
            return codec.parse(JsonOps.INSTANCE, jsonobject).getOrThrow(false, (sx) -> {
            });
         }

         public JsonObject toJson(T object) {
            return codec.encodeStart(JsonOps.INSTANCE, object).getOrThrow(false, (sx) -> {
            }).getAsJsonObject();
         }
      };
   }
}
