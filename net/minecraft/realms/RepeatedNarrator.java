package net.minecraft.realms;

import com.google.common.util.concurrent.RateLimiter;
import java.time.Duration;
import java.util.concurrent.atomic.AtomicReference;
import net.minecraft.client.GameNarrator;
import net.minecraft.network.chat.Component;

public class RepeatedNarrator {
   private final float permitsPerSecond;
   private final AtomicReference<RepeatedNarrator.Params> params = new AtomicReference<>();

   public RepeatedNarrator(Duration duration) {
      this.permitsPerSecond = 1000.0F / (float)duration.toMillis();
   }

   public void narrate(GameNarrator gamenarrator, Component component) {
      RepeatedNarrator.Params repeatednarrator_params = this.params.updateAndGet((repeatednarrator_params1) -> repeatednarrator_params1 != null && component.equals(repeatednarrator_params1.narration) ? repeatednarrator_params1 : new RepeatedNarrator.Params(component, RateLimiter.create((double)this.permitsPerSecond)));
      if (repeatednarrator_params.rateLimiter.tryAcquire(1)) {
         gamenarrator.sayNow(component);
      }

   }

   static class Params {
      final Component narration;
      final RateLimiter rateLimiter;

      Params(Component component, RateLimiter ratelimiter) {
         this.narration = component;
         this.rateLimiter = ratelimiter;
      }
   }
}
