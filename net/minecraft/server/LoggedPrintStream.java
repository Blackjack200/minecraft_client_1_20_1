package net.minecraft.server;

import com.mojang.logging.LogUtils;
import java.io.OutputStream;
import java.io.PrintStream;
import javax.annotation.Nullable;
import org.slf4j.Logger;

public class LoggedPrintStream extends PrintStream {
   private static final Logger LOGGER = LogUtils.getLogger();
   protected final String name;

   public LoggedPrintStream(String s, OutputStream outputstream) {
      super(outputstream);
      this.name = s;
   }

   public void println(@Nullable String s) {
      this.logLine(s);
   }

   public void println(Object object) {
      this.logLine(String.valueOf(object));
   }

   protected void logLine(@Nullable String s) {
      LOGGER.info("[{}]: {}", this.name, s);
   }
}
