package com.mojang.realmsclient.exception;

import org.slf4j.Logger;

public class RealmsDefaultUncaughtExceptionHandler implements Thread.UncaughtExceptionHandler {
   private final Logger logger;

   public RealmsDefaultUncaughtExceptionHandler(Logger logger) {
      this.logger = logger;
   }

   public void uncaughtException(Thread thread, Throwable throwable) {
      this.logger.error("Caught previously unhandled exception", throwable);
   }
}
