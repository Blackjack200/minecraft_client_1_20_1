package net.minecraft.client.renderer;

public class RunningTrimmedMean {
   private final long[] values;
   private int count;
   private int cursor;

   public RunningTrimmedMean(int i) {
      this.values = new long[i];
   }

   public long registerValueAndGetMean(long i) {
      if (this.count < this.values.length) {
         ++this.count;
      }

      this.values[this.cursor] = i;
      this.cursor = (this.cursor + 1) % this.values.length;
      long j = Long.MAX_VALUE;
      long k = Long.MIN_VALUE;
      long l = 0L;

      for(int i1 = 0; i1 < this.count; ++i1) {
         long j1 = this.values[i1];
         l += j1;
         j = Math.min(j, j1);
         k = Math.max(k, j1);
      }

      if (this.count > 2) {
         l -= j + k;
         return l / (long)(this.count - 2);
      } else {
         return l > 0L ? (long)this.count / l : 0L;
      }
   }
}
