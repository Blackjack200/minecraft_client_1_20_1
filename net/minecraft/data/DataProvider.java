package net.minecraft.data;

import com.google.common.hash.Hashing;
import com.google.common.hash.HashingOutputStream;
import com.google.gson.JsonElement;
import com.google.gson.stream.JsonWriter;
import com.mojang.logging.LogUtils;
import it.unimi.dsi.fastutil.objects.Object2IntOpenHashMap;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.nio.charset.StandardCharsets;
import java.nio.file.Path;
import java.util.Comparator;
import java.util.concurrent.CompletableFuture;
import java.util.function.ToIntFunction;
import net.minecraft.Util;
import net.minecraft.util.GsonHelper;
import org.slf4j.Logger;

public interface DataProvider {
   ToIntFunction<String> FIXED_ORDER_FIELDS = Util.make(new Object2IntOpenHashMap<>(), (object2intopenhashmap) -> {
      object2intopenhashmap.put("type", 0);
      object2intopenhashmap.put("parent", 1);
      object2intopenhashmap.defaultReturnValue(2);
   });
   Comparator<String> KEY_COMPARATOR = Comparator.comparingInt(FIXED_ORDER_FIELDS).thenComparing((s) -> s);
   Logger LOGGER = LogUtils.getLogger();

   CompletableFuture<?> run(CachedOutput cachedoutput);

   String getName();

   static CompletableFuture<?> saveStable(CachedOutput cachedoutput, JsonElement jsonelement, Path path) {
      return CompletableFuture.runAsync(() -> {
         try {
            ByteArrayOutputStream bytearrayoutputstream = new ByteArrayOutputStream();
            HashingOutputStream hashingoutputstream = new HashingOutputStream(Hashing.sha1(), bytearrayoutputstream);
            JsonWriter jsonwriter = new JsonWriter(new OutputStreamWriter(hashingoutputstream, StandardCharsets.UTF_8));

            try {
               jsonwriter.setSerializeNulls(false);
               jsonwriter.setIndent("  ");
               GsonHelper.writeValue(jsonwriter, jsonelement, KEY_COMPARATOR);
            } catch (Throwable var9) {
               try {
                  jsonwriter.close();
               } catch (Throwable var8) {
                  var9.addSuppressed(var8);
               }

               throw var9;
            }

            jsonwriter.close();
            cachedoutput.writeIfNeeded(path, bytearrayoutputstream.toByteArray(), hashingoutputstream.hash());
         } catch (IOException var10) {
            LOGGER.error("Failed to save file to {}", path, var10);
         }

      }, Util.backgroundExecutor());
   }

   @FunctionalInterface
   public interface Factory<T extends DataProvider> {
      T create(PackOutput packoutput);
   }
}
