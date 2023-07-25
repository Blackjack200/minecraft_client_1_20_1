package net.minecraft.client.telemetry.events;

import javax.annotation.Nullable;
import net.minecraft.client.Minecraft;
import net.minecraft.client.telemetry.TelemetryEventSender;
import net.minecraft.client.telemetry.TelemetryEventType;
import net.minecraft.client.telemetry.TelemetryProperty;
import net.minecraft.client.telemetry.TelemetryPropertyMap;
import net.minecraft.world.level.GameType;

public class WorldLoadEvent {
   private boolean eventSent;
   @Nullable
   private TelemetryProperty.GameMode gameMode;
   @Nullable
   private String serverBrand;
   @Nullable
   private final String minigameName;

   public WorldLoadEvent(@Nullable String s) {
      this.minigameName = s;
   }

   public void addProperties(TelemetryPropertyMap.Builder telemetrypropertymap_builder) {
      if (this.serverBrand != null) {
         telemetrypropertymap_builder.put(TelemetryProperty.SERVER_MODDED, !this.serverBrand.equals("vanilla"));
      }

      telemetrypropertymap_builder.put(TelemetryProperty.SERVER_TYPE, this.getServerType());
   }

   private TelemetryProperty.ServerType getServerType() {
      if (Minecraft.getInstance().isConnectedToRealms()) {
         return TelemetryProperty.ServerType.REALM;
      } else {
         return Minecraft.getInstance().hasSingleplayerServer() ? TelemetryProperty.ServerType.LOCAL : TelemetryProperty.ServerType.OTHER;
      }
   }

   public boolean send(TelemetryEventSender telemetryeventsender) {
      if (!this.eventSent && this.gameMode != null && this.serverBrand != null) {
         this.eventSent = true;
         telemetryeventsender.send(TelemetryEventType.WORLD_LOADED, (telemetrypropertymap_builder) -> {
            telemetrypropertymap_builder.put(TelemetryProperty.GAME_MODE, this.gameMode);
            if (this.minigameName != null) {
               telemetrypropertymap_builder.put(TelemetryProperty.REALMS_MAP_CONTENT, this.minigameName);
            }

         });
         return true;
      } else {
         return false;
      }
   }

   public void setGameMode(GameType gametype, boolean flag) {
      TelemetryProperty.GameMode var10001;
      switch (gametype) {
         case SURVIVAL:
            var10001 = flag ? TelemetryProperty.GameMode.HARDCORE : TelemetryProperty.GameMode.SURVIVAL;
            break;
         case CREATIVE:
            var10001 = TelemetryProperty.GameMode.CREATIVE;
            break;
         case ADVENTURE:
            var10001 = TelemetryProperty.GameMode.ADVENTURE;
            break;
         case SPECTATOR:
            var10001 = TelemetryProperty.GameMode.SPECTATOR;
            break;
         default:
            throw new IncompatibleClassChangeError();
      }

      this.gameMode = var10001;
   }

   public void setServerBrand(String s) {
      this.serverBrand = s;
   }
}
