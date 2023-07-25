package net.minecraft.client.telemetry;

import java.time.Duration;
import java.util.UUID;
import javax.annotation.Nullable;
import net.minecraft.advancements.Advancement;
import net.minecraft.client.telemetry.events.PerformanceMetricsEvent;
import net.minecraft.client.telemetry.events.WorldLoadEvent;
import net.minecraft.client.telemetry.events.WorldLoadTimesEvent;
import net.minecraft.client.telemetry.events.WorldUnloadEvent;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.GameType;
import net.minecraft.world.level.Level;

public class WorldSessionTelemetryManager {
   private final UUID worldSessionId = UUID.randomUUID();
   private final TelemetryEventSender eventSender;
   private final WorldLoadEvent worldLoadEvent;
   private final WorldUnloadEvent worldUnloadEvent = new WorldUnloadEvent();
   private final PerformanceMetricsEvent performanceMetricsEvent;
   private final WorldLoadTimesEvent worldLoadTimesEvent;

   public WorldSessionTelemetryManager(TelemetryEventSender telemetryeventsender, boolean flag, @Nullable Duration duration, @Nullable String s) {
      this.worldLoadEvent = new WorldLoadEvent(s);
      this.performanceMetricsEvent = new PerformanceMetricsEvent();
      this.worldLoadTimesEvent = new WorldLoadTimesEvent(flag, duration);
      this.eventSender = telemetryeventsender.decorate((telemetrypropertymap_builder) -> {
         this.worldLoadEvent.addProperties(telemetrypropertymap_builder);
         telemetrypropertymap_builder.put(TelemetryProperty.WORLD_SESSION_ID, this.worldSessionId);
      });
   }

   public void tick() {
      this.performanceMetricsEvent.tick(this.eventSender);
   }

   public void onPlayerInfoReceived(GameType gametype, boolean flag) {
      this.worldLoadEvent.setGameMode(gametype, flag);
      this.worldUnloadEvent.onPlayerInfoReceived();
      this.worldSessionStart();
   }

   public void onServerBrandReceived(String s) {
      this.worldLoadEvent.setServerBrand(s);
      this.worldSessionStart();
   }

   public void setTime(long i) {
      this.worldUnloadEvent.setTime(i);
   }

   public void worldSessionStart() {
      if (this.worldLoadEvent.send(this.eventSender)) {
         this.worldLoadTimesEvent.send(this.eventSender);
         this.performanceMetricsEvent.start();
      }

   }

   public void onDisconnect() {
      this.worldLoadEvent.send(this.eventSender);
      this.performanceMetricsEvent.stop();
      this.worldUnloadEvent.send(this.eventSender);
   }

   public void onAdvancementDone(Level level, Advancement advancement) {
      ResourceLocation resourcelocation = advancement.getId();
      if (advancement.sendsTelemetryEvent() && "minecraft".equals(resourcelocation.getNamespace())) {
         long i = level.getGameTime();
         this.eventSender.send(TelemetryEventType.ADVANCEMENT_MADE, (telemetrypropertymap_builder) -> {
            telemetrypropertymap_builder.put(TelemetryProperty.ADVANCEMENT_ID, resourcelocation.toString());
            telemetrypropertymap_builder.put(TelemetryProperty.ADVANCEMENT_GAME_TIME, i);
         });
      }

   }
}
