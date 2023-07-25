package net.minecraft.util.eventlog;

import com.google.gson.JsonElement;
import com.google.gson.JsonParseException;
import com.google.gson.JsonParser;
import com.google.gson.stream.JsonReader;
import com.mojang.serialization.Codec;
import com.mojang.serialization.JsonOps;
import java.io.Closeable;
import java.io.EOFException;
import java.io.IOException;
import java.io.Reader;
import javax.annotation.Nullable;
import net.minecraft.Util;

public interface JsonEventLogReader<T> extends Closeable {
   static <T> JsonEventLogReader<T> create(final Codec<T> codec, Reader reader) {
      final JsonReader jsonreader = new JsonReader(reader);
      jsonreader.setLenient(true);
      return new JsonEventLogReader<T>() {
         @Nullable
         public T next() throws IOException {
            try {
               if (!jsonreader.hasNext()) {
                  return (T)null;
               } else {
                  JsonElement jsonelement = JsonParser.parseReader(jsonreader);
                  return Util.getOrThrow(codec.parse(JsonOps.INSTANCE, jsonelement), IOException::new);
               }
            } catch (JsonParseException var2) {
               throw new IOException(var2);
            } catch (EOFException var3) {
               return (T)null;
            }
         }

         public void close() throws IOException {
            jsonreader.close();
         }
      };
   }

   @Nullable
   T next() throws IOException;
}
