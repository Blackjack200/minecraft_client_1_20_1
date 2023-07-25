package net.minecraft.util.thread;

import com.mojang.logging.LogUtils;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.atomic.AtomicInteger;
import org.slf4j.Logger;

public class NamedThreadFactory implements ThreadFactory {
   private static final Logger LOGGER = LogUtils.getLogger();
   private final ThreadGroup group;
   private final AtomicInteger threadNumber = new AtomicInteger(1);
   private final String namePrefix;

   public NamedThreadFactory(String s) {
      SecurityManager securitymanager = System.getSecurityManager();
      this.group = securitymanager != null ? securitymanager.getThreadGroup() : Thread.currentThread().getThreadGroup();
      this.namePrefix = s + "-";
   }

   public Thread newThread(Runnable runnable) {
      Thread thread = new Thread(this.group, runnable, this.namePrefix + this.threadNumber.getAndIncrement(), 0L);
      thread.setUncaughtExceptionHandler((thread1, throwable) -> {
         LOGGER.error("Caught exception in thread {} from {}", thread1, runnable);
         LOGGER.error("", throwable);
      });
      if (thread.getPriority() != 5) {
         thread.setPriority(5);
      }

      return thread;
   }
}
