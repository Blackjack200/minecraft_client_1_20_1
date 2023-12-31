package com.mojang.realmsclient.exception;

public class RetryCallException extends RealmsServiceException {
   public static final int DEFAULT_DELAY = 5;
   public final int delaySeconds;

   public RetryCallException(int i, int j) {
      super(j, "Retry operation");
      if (i >= 0 && i <= 120) {
         this.delaySeconds = i;
      } else {
         this.delaySeconds = 5;
      }

   }
}
