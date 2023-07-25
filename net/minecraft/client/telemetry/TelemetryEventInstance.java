package net.minecraft.client.telemetry;

import com.mojang.authlib.minecraft.TelemetryEvent;
import com.mojang.authlib.minecraft.TelemetrySession;
import com.mojang.serialization.Codec;

public record TelemetryEventInstance(TelemetryEventType type, TelemetryPropertyMap properties) {
   public static final Codec<TelemetryEventInstance> CODEC = TelemetryEventType.CODEC.dispatchStable(TelemetryEventInstance::type, TelemetryEventType::codec);

   public TelemetryEventInstance {
      telemetrypropertymap.propertySet().forEach((telemetryproperty) -> {
         if (!telemetryeventtype.contains(telemetryproperty)) {
            throw new IllegalArgumentException("Property '" + telemetryproperty.id() + "' not expected for event: '" + telemetryeventtype.id() + "'");
         }
      });
   }

   public TelemetryEvent export(TelemetrySession telemetrysession) {
      return this.type.export(telemetrysession, this.properties);
   }
}
