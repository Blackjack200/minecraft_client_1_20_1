package net.minecraft.gametest.framework;

import javax.annotation.Nullable;

class GameTestEvent {
   @Nullable
   public final Long expectedDelay;
   public final Runnable assertion;

   private GameTestEvent(@Nullable Long olong, Runnable runnable) {
      this.expectedDelay = olong;
      this.assertion = runnable;
   }

   static GameTestEvent create(Runnable runnable) {
      return new GameTestEvent((Long)null, runnable);
   }

   static GameTestEvent create(long i, Runnable runnable) {
      return new GameTestEvent(i, runnable);
   }
}
