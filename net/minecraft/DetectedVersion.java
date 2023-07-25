package net.minecraft;

import com.google.gson.JsonObject;
import com.google.gson.JsonParseException;
import com.mojang.logging.LogUtils;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.time.ZonedDateTime;
import java.util.Date;
import java.util.UUID;
import net.minecraft.server.packs.PackType;
import net.minecraft.util.GsonHelper;
import net.minecraft.world.level.storage.DataVersion;
import org.slf4j.Logger;

public class DetectedVersion implements WorldVersion {
   private static final Logger LOGGER = LogUtils.getLogger();
   public static final WorldVersion BUILT_IN = new DetectedVersion();
   private final String id;
   private final String name;
   private final boolean stable;
   private final DataVersion worldVersion;
   private final int protocolVersion;
   private final int resourcePackVersion;
   private final int dataPackVersion;
   private final Date buildTime;

   private DetectedVersion() {
      this.id = UUID.randomUUID().toString().replaceAll("-", "");
      this.name = "1.20.1";
      this.stable = true;
      this.worldVersion = new DataVersion(3465, "main");
      this.protocolVersion = SharedConstants.getProtocolVersion();
      this.resourcePackVersion = 15;
      this.dataPackVersion = 15;
      this.buildTime = new Date();
   }

   private DetectedVersion(JsonObject jsonobject) {
      this.id = GsonHelper.getAsString(jsonobject, "id");
      this.name = GsonHelper.getAsString(jsonobject, "name");
      this.stable = GsonHelper.getAsBoolean(jsonobject, "stable");
      this.worldVersion = new DataVersion(GsonHelper.getAsInt(jsonobject, "world_version"), GsonHelper.getAsString(jsonobject, "series_id", DataVersion.MAIN_SERIES));
      this.protocolVersion = GsonHelper.getAsInt(jsonobject, "protocol_version");
      JsonObject jsonobject1 = GsonHelper.getAsJsonObject(jsonobject, "pack_version");
      this.resourcePackVersion = GsonHelper.getAsInt(jsonobject1, "resource");
      this.dataPackVersion = GsonHelper.getAsInt(jsonobject1, "data");
      this.buildTime = Date.from(ZonedDateTime.parse(GsonHelper.getAsString(jsonobject, "build_time")).toInstant());
   }

   public static WorldVersion tryDetectVersion() {
      try {
         InputStream inputstream = DetectedVersion.class.getResourceAsStream("/version.json");

         WorldVersion var9;
         label63: {
            DetectedVersion var2;
            try {
               if (inputstream == null) {
                  LOGGER.warn("Missing version information!");
                  var9 = BUILT_IN;
                  break label63;
               }

               InputStreamReader inputstreamreader = new InputStreamReader(inputstream);

               try {
                  var2 = new DetectedVersion(GsonHelper.parse(inputstreamreader));
               } catch (Throwable var6) {
                  try {
                     inputstreamreader.close();
                  } catch (Throwable var5) {
                     var6.addSuppressed(var5);
                  }

                  throw var6;
               }

               inputstreamreader.close();
            } catch (Throwable var7) {
               if (inputstream != null) {
                  try {
                     inputstream.close();
                  } catch (Throwable var4) {
                     var7.addSuppressed(var4);
                  }
               }

               throw var7;
            }

            if (inputstream != null) {
               inputstream.close();
            }

            return var2;
         }

         if (inputstream != null) {
            inputstream.close();
         }

         return var9;
      } catch (JsonParseException | IOException var8) {
         throw new IllegalStateException("Game version information is corrupt", var8);
      }
   }

   public String getId() {
      return this.id;
   }

   public String getName() {
      return this.name;
   }

   public DataVersion getDataVersion() {
      return this.worldVersion;
   }

   public int getProtocolVersion() {
      return this.protocolVersion;
   }

   public int getPackVersion(PackType packtype) {
      return packtype == PackType.SERVER_DATA ? this.dataPackVersion : this.resourcePackVersion;
   }

   public Date getBuildTime() {
      return this.buildTime;
   }

   public boolean isStable() {
      return this.stable;
   }
}
