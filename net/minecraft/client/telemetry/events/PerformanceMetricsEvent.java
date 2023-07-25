package net.minecraft.client.telemetry.events;

import it.unimi.dsi.fastutil.longs.LongArrayList;
import it.unimi.dsi.fastutil.longs.LongList;
import net.minecraft.client.Minecraft;
import net.minecraft.client.telemetry.TelemetryEventSender;
import net.minecraft.client.telemetry.TelemetryEventType;
import net.minecraft.client.telemetry.TelemetryProperty;

public final class PerformanceMetricsEvent extends AggregatedTelemetryEvent {
   private static final long DEDICATED_MEMORY_KB = toKilobytes(Runtime.getRuntime().maxMemory());
   private final LongList fpsSamples = new LongArrayList();
   private final LongList frameTimeSamples = new LongArrayList();
   private final LongList usedMemorySamples = new LongArrayList();

   public void tick(TelemetryEventSender telemetryeventsender) {
      if (Minecraft.getInstance().telemetryOptInExtra()) {
         super.tick(telemetryeventsender);
      }

   }

   private void resetValues() {
      this.fpsSamples.clear();
      this.frameTimeSamples.clear();
      this.usedMemorySamples.clear();
   }

   public void takeSample() {
      this.fpsSamples.add((long)Minecraft.getInstance().getFps());
      this.takeUsedMemorySample();
      this.frameTimeSamples.add(Minecraft.getInstance().getFrameTimeNs());
   }

   private void takeUsedMemorySample() {
      long i = Runtime.getRuntime().totalMemory();
      long j = Runtime.getRuntime().freeMemory();
      long k = i - j;
      this.usedMemorySamples.add(toKilobytes(k));
   }

   public void sendEvent(TelemetryEventSender telemetryeventsender) {
      telemetryeventsender.send(TelemetryEventType.PERFORMANCE_METRICS, (telemetrypropertymap_builder) -> {
         telemetrypropertymap_builder.put(TelemetryProperty.FRAME_RATE_SAMPLES, new LongArrayList(this.fpsSamples));
         telemetrypropertymap_builder.put(TelemetryProperty.RENDER_TIME_SAMPLES, new LongArrayList(this.frameTimeSamples));
         telemetrypropertymap_builder.put(TelemetryProperty.USED_MEMORY_SAMPLES, new LongArrayList(this.usedMemorySamples));
         telemetrypropertymap_builder.put(TelemetryProperty.NUMBER_OF_SAMPLES, this.getSampleCount());
         telemetrypropertymap_builder.put(TelemetryProperty.RENDER_DISTANCE, Minecraft.getInstance().options.getEffectiveRenderDistance());
         telemetrypropertymap_builder.put(TelemetryProperty.DEDICATED_MEMORY_KB, (int)DEDICATED_MEMORY_KB);
      });
      this.resetValues();
   }

   private static long toKilobytes(long i) {
      return i / 1000L;
   }
}
