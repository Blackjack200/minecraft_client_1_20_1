package net.minecraft.util;

import com.google.common.collect.Maps;
import com.google.gson.Gson;
import com.google.gson.TypeAdapter;
import com.google.gson.TypeAdapterFactory;
import com.google.gson.reflect.TypeToken;
import com.google.gson.stream.JsonReader;
import com.google.gson.stream.JsonToken;
import com.google.gson.stream.JsonWriter;
import java.io.IOException;
import java.util.Locale;
import java.util.Map;
import javax.annotation.Nullable;

public class LowerCaseEnumTypeAdapterFactory implements TypeAdapterFactory {
   @Nullable
   public <T> TypeAdapter<T> create(Gson gson, TypeToken<T> typetoken) {
      Class<T> oclass = typetoken.getRawType();
      if (!oclass.isEnum()) {
         return null;
      } else {
         final Map<String, T> map = Maps.newHashMap();

         for(T object : oclass.getEnumConstants()) {
            map.put(this.toLowercase(object), object);
         }

         return new TypeAdapter<T>() {
            public void write(JsonWriter jsonwriter, T object) throws IOException {
               if (object == null) {
                  jsonwriter.nullValue();
               } else {
                  jsonwriter.value(LowerCaseEnumTypeAdapterFactory.this.toLowercase(object));
               }

            }

            @Nullable
            public T read(JsonReader jsonreader) throws IOException {
               if (jsonreader.peek() == JsonToken.NULL) {
                  jsonreader.nextNull();
                  return (T)null;
               } else {
                  return map.get(jsonreader.nextString());
               }
            }
         };
      }
   }

   String toLowercase(Object object) {
      return object instanceof Enum ? ((Enum)object).name().toLowerCase(Locale.ROOT) : object.toString().toLowerCase(Locale.ROOT);
   }
}
