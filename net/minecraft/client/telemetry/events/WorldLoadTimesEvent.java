package net.minecraft.client.telemetry.events;

import java.time.Duration;
import javax.annotation.Nullable;
import net.minecraft.client.telemetry.TelemetryEventSender;
import net.minecraft.client.telemetry.TelemetryEventType;
import net.minecraft.client.telemetry.TelemetryProperty;

public class WorldLoadTimesEvent {
   private final boolean newWorld;
   @Nullable
   private final Duration worldLoadDuration;

   public WorldLoadTimesEvent(boolean flag, @Nullable Duration duration) {
      this.worldLoadDuration = duration;
      this.newWorld = flag;
   }

   public void send(TelemetryEventSender telemetryeventsender) {
      if (this.worldLoadDuration != null) {
         telemetryeventsender.send(TelemetryEventType.WORLD_LOAD_TIMES, (telemetrypropertymap_builder) -> {
            telemetrypropertymap_builder.put(TelemetryProperty.WORLD_LOAD_TIME_MS, (int)this.worldLoadDuration.toMillis());
            telemetrypropertymap_builder.put(TelemetryProperty.NEW_WORLD, this.newWorld);
         });
      }

   }
}
