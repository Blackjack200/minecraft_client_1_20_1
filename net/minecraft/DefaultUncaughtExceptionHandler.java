package net.minecraft;

import org.slf4j.Logger;

public class DefaultUncaughtExceptionHandler implements Thread.UncaughtExceptionHandler {
   private final Logger logger;

   public DefaultUncaughtExceptionHandler(Logger logger) {
      this.logger = logger;
   }

   public void uncaughtException(Thread thread, Throwable throwable) {
      this.logger.error("Caught previously unhandled exception :", throwable);
   }
}
