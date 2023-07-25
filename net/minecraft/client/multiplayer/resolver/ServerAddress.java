package net.minecraft.client.multiplayer.resolver;

import com.google.common.net.HostAndPort;
import com.mojang.logging.LogUtils;
import java.net.IDN;
import org.slf4j.Logger;

public final class ServerAddress {
   private static final Logger LOGGER = LogUtils.getLogger();
   private final HostAndPort hostAndPort;
   private static final ServerAddress INVALID = new ServerAddress(HostAndPort.fromParts("server.invalid", 25565));

   public ServerAddress(String s, int i) {
      this(HostAndPort.fromParts(s, i));
   }

   private ServerAddress(HostAndPort hostandport) {
      this.hostAndPort = hostandport;
   }

   public String getHost() {
      try {
         return IDN.toASCII(this.hostAndPort.getHost());
      } catch (IllegalArgumentException var2) {
         return "";
      }
   }

   public int getPort() {
      return this.hostAndPort.getPort();
   }

   public static ServerAddress parseString(String s) {
      if (s == null) {
         return INVALID;
      } else {
         try {
            HostAndPort hostandport = HostAndPort.fromString(s).withDefaultPort(25565);
            return hostandport.getHost().isEmpty() ? INVALID : new ServerAddress(hostandport);
         } catch (IllegalArgumentException var2) {
            LOGGER.info("Failed to parse URL {}", s, var2);
            return INVALID;
         }
      }
   }

   public static boolean isValidAddress(String s) {
      try {
         HostAndPort hostandport = HostAndPort.fromString(s);
         String s1 = hostandport.getHost();
         if (!s1.isEmpty()) {
            IDN.toASCII(s1);
            return true;
         }
      } catch (IllegalArgumentException var3) {
      }

      return false;
   }

   static int parsePort(String s) {
      try {
         return Integer.parseInt(s.trim());
      } catch (Exception var2) {
         return 25565;
      }
   }

   public String toString() {
      return this.hostAndPort.toString();
   }

   public boolean equals(Object object) {
      if (this == object) {
         return true;
      } else {
         return object instanceof ServerAddress ? this.hostAndPort.equals(((ServerAddress)object).hostAndPort) : false;
      }
   }

   public int hashCode() {
      return this.hostAndPort.hashCode();
   }
}
