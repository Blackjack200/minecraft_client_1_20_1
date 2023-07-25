package net.minecraft.client.telemetry.events;

import com.google.common.base.Stopwatch;
import com.google.common.base.Ticker;
import com.mojang.logging.LogUtils;
import com.mojang.serialization.Codec;
import java.util.HashMap;
import java.util.Map;
import java.util.OptionalLong;
import java.util.concurrent.TimeUnit;
import java.util.function.Function;
import net.minecraft.client.telemetry.TelemetryEventSender;
import net.minecraft.client.telemetry.TelemetryEventType;
import net.minecraft.client.telemetry.TelemetryProperty;
import org.slf4j.Logger;

public class GameLoadTimesEvent {
   public static final GameLoadTimesEvent INSTANCE = new GameLoadTimesEvent(Ticker.systemTicker());
   private static final Logger LOGGER = LogUtils.getLogger();
   private final Ticker timeSource;
   private final Map<TelemetryProperty<GameLoadTimesEvent.Measurement>, Stopwatch> measurements = new HashMap<>();
   private OptionalLong bootstrapTime = OptionalLong.empty();

   protected GameLoadTimesEvent(Ticker ticker) {
      this.timeSource = ticker;
   }

   public synchronized void beginStep(TelemetryProperty<GameLoadTimesEvent.Measurement> telemetryproperty) {
      this.beginStep(telemetryproperty, (telemetryproperty1) -> Stopwatch.createStarted(this.timeSource));
   }

   public synchronized void beginStep(TelemetryProperty<GameLoadTimesEvent.Measurement> telemetryproperty, Stopwatch stopwatch) {
      this.beginStep(telemetryproperty, (telemetryproperty1) -> stopwatch);
   }

   private synchronized void beginStep(TelemetryProperty<GameLoadTimesEvent.Measurement> telemetryproperty, Function<TelemetryProperty<GameLoadTimesEvent.Measurement>, Stopwatch> function) {
      this.measurements.computeIfAbsent(telemetryproperty, function);
   }

   public synchronized void endStep(TelemetryProperty<GameLoadTimesEvent.Measurement> telemetryproperty) {
      Stopwatch stopwatch = this.measurements.get(telemetryproperty);
      if (stopwatch == null) {
         LOGGER.warn("Attempted to end step for {} before starting it", (Object)telemetryproperty.id());
      } else {
         if (stopwatch.isRunning()) {
            stopwatch.stop();
         }

      }
   }

   public void send(TelemetryEventSender telemetryeventsender) {
      telemetryeventsender.send(TelemetryEventType.GAME_LOAD_TIMES, (telemetrypropertymap_builder) -> {
         synchronized(this) {
            this.measurements.forEach((telemetryproperty, stopwatch) -> {
               if (!stopwatch.isRunning()) {
                  long j = stopwatch.elapsed(TimeUnit.MILLISECONDS);
                  telemetrypropertymap_builder.put(telemetryproperty, new GameLoadTimesEvent.Measurement((int)j));
               } else {
                  LOGGER.warn("Measurement {} was discarded since it was still ongoing when the event {} was sent.", telemetryproperty.id(), TelemetryEventType.GAME_LOAD_TIMES.id());
               }

            });
            this.bootstrapTime.ifPresent((i) -> telemetrypropertymap_builder.put(TelemetryProperty.LOAD_TIME_BOOTSTRAP_MS, new GameLoadTimesEvent.Measurement((int)i)));
            this.measurements.clear();
         }
      });
   }

   public synchronized void setBootstrapTime(long i) {
      this.bootstrapTime = OptionalLong.of(i);
   }

   public static record Measurement(int millis) {
      public static final Codec<GameLoadTimesEvent.Measurement> CODEC = Codec.INT.xmap(GameLoadTimesEvent.Measurement::new, (gameloadtimesevent_measurement) -> gameloadtimesevent_measurement.millis);
   }
}
