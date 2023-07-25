package net.minecraft.util;

import java.util.Arrays;
import java.util.function.IntConsumer;
import org.apache.commons.lang3.Validate;

public class ZeroBitStorage implements BitStorage {
   public static final long[] RAW = new long[0];
   private final int size;

   public ZeroBitStorage(int i) {
      this.size = i;
   }

   public int getAndSet(int i, int j) {
      Validate.inclusiveBetween(0L, (long)(this.size - 1), (long)i);
      Validate.inclusiveBetween(0L, 0L, (long)j);
      return 0;
   }

   public void set(int i, int j) {
      Validate.inclusiveBetween(0L, (long)(this.size - 1), (long)i);
      Validate.inclusiveBetween(0L, 0L, (long)j);
   }

   public int get(int i) {
      Validate.inclusiveBetween(0L, (long)(this.size - 1), (long)i);
      return 0;
   }

   public long[] getRaw() {
      return RAW;
   }

   public int getSize() {
      return this.size;
   }

   public int getBits() {
      return 0;
   }

   public void getAll(IntConsumer intconsumer) {
      for(int i = 0; i < this.size; ++i) {
         intconsumer.accept(0);
      }

   }

   public void unpack(int[] aint) {
      Arrays.fill(aint, 0, this.size, 0);
   }

   public BitStorage copy() {
      return this;
   }
}
