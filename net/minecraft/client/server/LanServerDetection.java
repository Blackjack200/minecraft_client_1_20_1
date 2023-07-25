package net.minecraft.client.server;

import com.google.common.collect.Lists;
import com.mojang.logging.LogUtils;
import java.io.IOException;
import java.net.DatagramPacket;
import java.net.InetAddress;
import java.net.MulticastSocket;
import java.net.SocketTimeoutException;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;
import javax.annotation.Nullable;
import net.minecraft.DefaultUncaughtExceptionHandler;
import org.slf4j.Logger;

public class LanServerDetection {
   static final AtomicInteger UNIQUE_THREAD_ID = new AtomicInteger(0);
   static final Logger LOGGER = LogUtils.getLogger();

   public static class LanServerDetector extends Thread {
      private final LanServerDetection.LanServerList serverList;
      private final InetAddress pingGroup;
      private final MulticastSocket socket;

      public LanServerDetector(LanServerDetection.LanServerList lanserverdetection_lanserverlist) throws IOException {
         super("LanServerDetector #" + LanServerDetection.UNIQUE_THREAD_ID.incrementAndGet());
         this.serverList = lanserverdetection_lanserverlist;
         this.setDaemon(true);
         this.setUncaughtExceptionHandler(new DefaultUncaughtExceptionHandler(LanServerDetection.LOGGER));
         this.socket = new MulticastSocket(4445);
         this.pingGroup = InetAddress.getByName("224.0.2.60");
         this.socket.setSoTimeout(5000);
         this.socket.joinGroup(this.pingGroup);
      }

      public void run() {
         byte[] abyte = new byte[1024];

         while(!this.isInterrupted()) {
            DatagramPacket datagrampacket = new DatagramPacket(abyte, abyte.length);

            try {
               this.socket.receive(datagrampacket);
            } catch (SocketTimeoutException var5) {
               continue;
            } catch (IOException var6) {
               LanServerDetection.LOGGER.error("Couldn't ping server", (Throwable)var6);
               break;
            }

            String s = new String(datagrampacket.getData(), datagrampacket.getOffset(), datagrampacket.getLength(), StandardCharsets.UTF_8);
            LanServerDetection.LOGGER.debug("{}: {}", datagrampacket.getAddress(), s);
            this.serverList.addServer(s, datagrampacket.getAddress());
         }

         try {
            this.socket.leaveGroup(this.pingGroup);
         } catch (IOException var4) {
         }

         this.socket.close();
      }
   }

   public static class LanServerList {
      private final List<LanServer> servers = Lists.newArrayList();
      private boolean isDirty;

      @Nullable
      public synchronized List<LanServer> takeDirtyServers() {
         if (this.isDirty) {
            List<LanServer> list = List.copyOf(this.servers);
            this.isDirty = false;
            return list;
         } else {
            return null;
         }
      }

      public synchronized void addServer(String s, InetAddress inetaddress) {
         String s1 = LanServerPinger.parseMotd(s);
         String s2 = LanServerPinger.parseAddress(s);
         if (s2 != null) {
            s2 = inetaddress.getHostAddress() + ":" + s2;
            boolean flag = false;

            for(LanServer lanserver : this.servers) {
               if (lanserver.getAddress().equals(s2)) {
                  lanserver.updatePingTime();
                  flag = true;
                  break;
               }
            }

            if (!flag) {
               this.servers.add(new LanServer(s1, s2));
               this.isDirty = true;
            }

         }
      }
   }
}
