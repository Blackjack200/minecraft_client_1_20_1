package com.mojang.realmsclient.gui.task;

import com.mojang.logging.LogUtils;
import org.slf4j.Logger;

public interface RepeatedDelayStrategy {
   RepeatedDelayStrategy CONSTANT = new RepeatedDelayStrategy() {
      public long delayCyclesAfterSuccess() {
         return 1L;
      }

      public long delayCyclesAfterFailure() {
         return 1L;
      }
   };

   long delayCyclesAfterSuccess();

   long delayCyclesAfterFailure();

   static RepeatedDelayStrategy exponentialBackoff(final int i) {
      return new RepeatedDelayStrategy() {
         private static final Logger LOGGER = LogUtils.getLogger();
         private int failureCount;

         public long delayCyclesAfterSuccess() {
            this.failureCount = 0;
            return 1L;
         }

         public long delayCyclesAfterFailure() {
            ++this.failureCount;
            long i = Math.min(1L << this.failureCount, (long)i);
            LOGGER.debug("Skipping for {} extra cycles", (long)i);
            return i;
         }
      };
   }
}
