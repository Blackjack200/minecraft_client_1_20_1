package net.minecraft.client;

public class Timer {
   public float partialTick;
   public float tickDelta;
   private long lastMs;
   private final float msPerTick;

   public Timer(float f, long i) {
      this.msPerTick = 1000.0F / f;
      this.lastMs = i;
   }

   public int advanceTime(long i) {
      this.tickDelta = (float)(i - this.lastMs) / this.msPerTick;
      this.lastMs = i;
      this.partialTick += this.tickDelta;
      int j = (int)this.partialTick;
      this.partialTick -= (float)j;
      return j;
   }
}
