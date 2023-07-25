package net.minecraft.client.telemetry.events;

import java.time.Duration;
import java.time.Instant;
import java.util.Optional;
import net.minecraft.client.telemetry.TelemetryEventSender;
import net.minecraft.client.telemetry.TelemetryEventType;
import net.minecraft.client.telemetry.TelemetryProperty;

public class WorldUnloadEvent {
   private static final int NOT_TRACKING_TIME = -1;
   private Optional<Instant> worldLoadedTime = Optional.empty();
   private long totalTicks;
   private long lastGameTime;

   public void onPlayerInfoReceived() {
      this.lastGameTime = -1L;
      if (this.worldLoadedTime.isEmpty()) {
         this.worldLoadedTime = Optional.of(Instant.now());
      }

   }

   public void setTime(long i) {
      if (this.lastGameTime != -1L) {
         this.totalTicks += Math.max(0L, i - this.lastGameTime);
      }

      this.lastGameTime = i;
   }

   private int getTimeInSecondsSinceLoad(Instant instant) {
      Duration duration = Duration.between(instant, Instant.now());
      return (int)duration.toSeconds();
   }

   public void send(TelemetryEventSender telemetryeventsender) {
      this.worldLoadedTime.ifPresent((instant) -> telemetryeventsender.send(TelemetryEventType.WORLD_UNLOADED, (telemetrypropertymap_builder) -> {
            telemetrypropertymap_builder.put(TelemetryProperty.SECONDS_SINCE_LOAD, this.getTimeInSecondsSinceLoad(instant));
            telemetrypropertymap_builder.put(TelemetryProperty.TICKS_SINCE_LOAD, (int)this.totalTicks);
         }));
   }
}
