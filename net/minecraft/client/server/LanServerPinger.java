package net.minecraft.client.server;

import com.mojang.logging.LogUtils;
import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.nio.charset.StandardCharsets;
import java.util.concurrent.atomic.AtomicInteger;
import net.minecraft.DefaultUncaughtExceptionHandler;
import org.slf4j.Logger;

public class LanServerPinger extends Thread {
   private static final AtomicInteger UNIQUE_THREAD_ID = new AtomicInteger(0);
   private static final Logger LOGGER = LogUtils.getLogger();
   public static final String MULTICAST_GROUP = "224.0.2.60";
   public static final int PING_PORT = 4445;
   private static final long PING_INTERVAL = 1500L;
   private final String motd;
   private final DatagramSocket socket;
   private boolean isRunning = true;
   private final String serverAddress;

   public LanServerPinger(String s, String s1) throws IOException {
      super("LanServerPinger #" + UNIQUE_THREAD_ID.incrementAndGet());
      this.motd = s;
      this.serverAddress = s1;
      this.setDaemon(true);
      this.setUncaughtExceptionHandler(new DefaultUncaughtExceptionHandler(LOGGER));
      this.socket = new DatagramSocket();
   }

   public void run() {
      String s = createPingString(this.motd, this.serverAddress);
      byte[] abyte = s.getBytes(StandardCharsets.UTF_8);

      while(!this.isInterrupted() && this.isRunning) {
         try {
            InetAddress inetaddress = InetAddress.getByName("224.0.2.60");
            DatagramPacket datagrampacket = new DatagramPacket(abyte, abyte.length, inetaddress, 4445);
            this.socket.send(datagrampacket);
         } catch (IOException var6) {
            LOGGER.warn("LanServerPinger: {}", (Object)var6.getMessage());
            break;
         }

         try {
            sleep(1500L);
         } catch (InterruptedException var5) {
         }
      }

   }

   public void interrupt() {
      super.interrupt();
      this.isRunning = false;
   }

   public static String createPingString(String s, String s1) {
      return "[MOTD]" + s + "[/MOTD][AD]" + s1 + "[/AD]";
   }

   public static String parseMotd(String s) {
      int i = s.indexOf("[MOTD]");
      if (i < 0) {
         return "missing no";
      } else {
         int j = s.indexOf("[/MOTD]", i + "[MOTD]".length());
         return j < i ? "missing no" : s.substring(i + "[MOTD]".length(), j);
      }
   }

   public static String parseAddress(String s) {
      int i = s.indexOf("[/MOTD]");
      if (i < 0) {
         return null;
      } else {
         int j = s.indexOf("[/MOTD]", i + "[/MOTD]".length());
         if (j >= 0) {
            return null;
         } else {
            int k = s.indexOf("[AD]", i + "[/MOTD]".length());
            if (k < 0) {
               return null;
            } else {
               int l = s.indexOf("[/AD]", k + "[AD]".length());
               return l < k ? null : s.substring(k + "[AD]".length(), l);
            }
         }
      }
   }
}
