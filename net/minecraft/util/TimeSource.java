package net.minecraft.util;

import java.util.concurrent.TimeUnit;
import java.util.function.LongSupplier;

@FunctionalInterface
public interface TimeSource {
   long get(TimeUnit timeunit);

   public interface NanoTimeSource extends TimeSource, LongSupplier {
      default long get(TimeUnit timeunit) {
         return timeunit.convert(this.getAsLong(), TimeUnit.NANOSECONDS);
      }
   }
}
