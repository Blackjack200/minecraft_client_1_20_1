package net.minecraft.server.packs;

import com.google.gson.JsonObject;
import com.mojang.logging.LogUtils;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import javax.annotation.Nullable;
import net.minecraft.server.packs.metadata.MetadataSectionSerializer;
import net.minecraft.server.packs.resources.IoSupplier;
import net.minecraft.util.GsonHelper;
import org.slf4j.Logger;

public abstract class AbstractPackResources implements PackResources {
   private static final Logger LOGGER = LogUtils.getLogger();
   private final String name;
   private final boolean isBuiltin;

   protected AbstractPackResources(String s, boolean flag) {
      this.name = s;
      this.isBuiltin = flag;
   }

   @Nullable
   public <T> T getMetadataSection(MetadataSectionSerializer<T> metadatasectionserializer) throws IOException {
      IoSupplier<InputStream> iosupplier = this.getRootResource(new String[]{"pack.mcmeta"});
      if (iosupplier == null) {
         return (T)null;
      } else {
         InputStream inputstream = iosupplier.get();

         Object var4;
         try {
            var4 = getMetadataFromStream(metadatasectionserializer, inputstream);
         } catch (Throwable var7) {
            if (inputstream != null) {
               try {
                  inputstream.close();
               } catch (Throwable var6) {
                  var7.addSuppressed(var6);
               }
            }

            throw var7;
         }

         if (inputstream != null) {
            inputstream.close();
         }

         return (T)var4;
      }
   }

   @Nullable
   public static <T> T getMetadataFromStream(MetadataSectionSerializer<T> metadatasectionserializer, InputStream inputstream) {
      JsonObject jsonobject;
      try {
         BufferedReader bufferedreader = new BufferedReader(new InputStreamReader(inputstream, StandardCharsets.UTF_8));

         try {
            jsonobject = GsonHelper.parse(bufferedreader);
         } catch (Throwable var8) {
            try {
               bufferedreader.close();
            } catch (Throwable var6) {
               var8.addSuppressed(var6);
            }

            throw var8;
         }

         bufferedreader.close();
      } catch (Exception var9) {
         LOGGER.error("Couldn't load {} metadata", metadatasectionserializer.getMetadataSectionName(), var9);
         return (T)null;
      }

      if (!jsonobject.has(metadatasectionserializer.getMetadataSectionName())) {
         return (T)null;
      } else {
         try {
            return metadatasectionserializer.fromJson(GsonHelper.getAsJsonObject(jsonobject, metadatasectionserializer.getMetadataSectionName()));
         } catch (Exception var7) {
            LOGGER.error("Couldn't load {} metadata", metadatasectionserializer.getMetadataSectionName(), var7);
            return (T)null;
         }
      }
   }

   public String packId() {
      return this.name;
   }

   public boolean isBuiltin() {
      return this.isBuiltin;
   }
}
