package net.minecraft.client.telemetry;

import java.util.function.Consumer;

@FunctionalInterface
public interface TelemetryEventSender {
   TelemetryEventSender DISABLED = (telemetryeventtype, consumer) -> {
   };

   default TelemetryEventSender decorate(Consumer<TelemetryPropertyMap.Builder> consumer) {
      return (telemetryeventtype, consumer2) -> this.send(telemetryeventtype, (telemetrypropertymap_builder) -> {
            consumer2.accept(telemetrypropertymap_builder);
            consumer.accept(telemetrypropertymap_builder);
         });
   }

   void send(TelemetryEventType telemetryeventtype, Consumer<TelemetryPropertyMap.Builder> consumer);
}
