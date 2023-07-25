package net.minecraft.server.packs.resources;

import com.google.gson.JsonObject;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.Optional;
import net.minecraft.server.packs.metadata.MetadataSectionSerializer;
import net.minecraft.util.GsonHelper;

public interface ResourceMetadata {
   ResourceMetadata EMPTY = new ResourceMetadata() {
      public <T> Optional<T> getSection(MetadataSectionSerializer<T> metadatasectionserializer) {
         return Optional.empty();
      }
   };
   IoSupplier<ResourceMetadata> EMPTY_SUPPLIER = () -> EMPTY;

   static ResourceMetadata fromJsonStream(InputStream inputstream) throws IOException {
      BufferedReader bufferedreader = new BufferedReader(new InputStreamReader(inputstream, StandardCharsets.UTF_8));

      ResourceMetadata var3;
      try {
         final JsonObject jsonobject = GsonHelper.parse(bufferedreader);
         var3 = new ResourceMetadata() {
            public <T> Optional<T> getSection(MetadataSectionSerializer<T> metadatasectionserializer) {
               String s = metadatasectionserializer.getMetadataSectionName();
               return jsonobject.has(s) ? Optional.of(metadatasectionserializer.fromJson(GsonHelper.getAsJsonObject(jsonobject, s))) : Optional.empty();
            }
         };
      } catch (Throwable var5) {
         try {
            bufferedreader.close();
         } catch (Throwable var4) {
            var5.addSuppressed(var4);
         }

         throw var5;
      }

      bufferedreader.close();
      return var3;
   }

   <T> Optional<T> getSection(MetadataSectionSerializer<T> metadatasectionserializer);
}
