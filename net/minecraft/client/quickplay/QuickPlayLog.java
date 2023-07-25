package net.minecraft.client.quickplay;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.mojang.logging.LogUtils;
import com.mojang.serialization.Codec;
import com.mojang.serialization.JsonOps;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.Instant;
import java.util.List;
import javax.annotation.Nullable;
import net.minecraft.Util;
import net.minecraft.client.Minecraft;
import net.minecraft.util.ExtraCodecs;
import net.minecraft.util.StringRepresentable;
import net.minecraft.world.level.GameType;
import org.slf4j.Logger;

public class QuickPlayLog {
   private static final QuickPlayLog INACTIVE = new QuickPlayLog("") {
      public void log(Minecraft minecraft) {
      }

      public void setWorldData(QuickPlayLog.Type quickplaylog_type, String s, String s1) {
      }
   };
   private static final Logger LOGGER = LogUtils.getLogger();
   private static final Gson GSON = (new GsonBuilder()).create();
   private final Path path;
   @Nullable
   private QuickPlayLog.QuickPlayWorld worldData;

   QuickPlayLog(String s) {
      this.path = Minecraft.getInstance().gameDirectory.toPath().resolve(s);
   }

   public static QuickPlayLog of(@Nullable String s) {
      return s == null ? INACTIVE : new QuickPlayLog(s);
   }

   public void setWorldData(QuickPlayLog.Type quickplaylog_type, String s, String s1) {
      this.worldData = new QuickPlayLog.QuickPlayWorld(quickplaylog_type, s, s1);
   }

   public void log(Minecraft minecraft) {
      if (minecraft.gameMode != null && this.worldData != null) {
         Util.ioPool().execute(() -> {
            try {
               Files.deleteIfExists(this.path);
            } catch (IOException var3) {
               LOGGER.error("Failed to delete quickplay log file {}", this.path, var3);
            }

            QuickPlayLog.QuickPlayEntry quickplaylog_quickplayentry = new QuickPlayLog.QuickPlayEntry(this.worldData, Instant.now(), minecraft.gameMode.getPlayerMode());
            Codec.list(QuickPlayLog.QuickPlayEntry.CODEC).encodeStart(JsonOps.INSTANCE, List.of(quickplaylog_quickplayentry)).resultOrPartial(Util.prefix("Quick Play: ", LOGGER::error)).ifPresent((jsonelement) -> {
               try {
                  Files.createDirectories(this.path.getParent());
                  Files.writeString(this.path, GSON.toJson(jsonelement));
               } catch (IOException var3) {
                  LOGGER.error("Failed to write to quickplay log file {}", this.path, var3);
               }

            });
         });
      } else {
         LOGGER.error("Failed to log session for quickplay. Missing world data or gamemode");
      }
   }

   static record QuickPlayEntry(QuickPlayLog.QuickPlayWorld quickPlayWorld, Instant lastPlayedTime, GameType gamemode) {
      public static final Codec<QuickPlayLog.QuickPlayEntry> CODEC = RecordCodecBuilder.create((recordcodecbuilder_instance) -> recordcodecbuilder_instance.group(QuickPlayLog.QuickPlayWorld.MAP_CODEC.forGetter(QuickPlayLog.QuickPlayEntry::quickPlayWorld), ExtraCodecs.INSTANT_ISO8601.fieldOf("lastPlayedTime").forGetter(QuickPlayLog.QuickPlayEntry::lastPlayedTime), GameType.CODEC.fieldOf("gamemode").forGetter(QuickPlayLog.QuickPlayEntry::gamemode)).apply(recordcodecbuilder_instance, QuickPlayLog.QuickPlayEntry::new));
   }

   static record QuickPlayWorld(QuickPlayLog.Type type, String id, String name) {
      public static final MapCodec<QuickPlayLog.QuickPlayWorld> MAP_CODEC = RecordCodecBuilder.mapCodec((recordcodecbuilder_instance) -> recordcodecbuilder_instance.group(QuickPlayLog.Type.CODEC.fieldOf("type").forGetter(QuickPlayLog.QuickPlayWorld::type), Codec.STRING.fieldOf("id").forGetter(QuickPlayLog.QuickPlayWorld::id), Codec.STRING.fieldOf("name").forGetter(QuickPlayLog.QuickPlayWorld::name)).apply(recordcodecbuilder_instance, QuickPlayLog.QuickPlayWorld::new));
   }

   public static enum Type implements StringRepresentable {
      SINGLEPLAYER("singleplayer"),
      MULTIPLAYER("multiplayer"),
      REALMS("realms");

      static final Codec<QuickPlayLog.Type> CODEC = StringRepresentable.fromEnum(QuickPlayLog.Type::values);
      private final String name;

      private Type(String s) {
         this.name = s;
      }

      public String getSerializedName() {
         return this.name;
      }
   }
}
