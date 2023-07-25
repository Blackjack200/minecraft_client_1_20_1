package net.minecraft.server.rcon.thread;

import com.mojang.logging.LogUtils;
import java.io.BufferedInputStream;
import java.io.ByteArrayOutputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.Socket;
import java.nio.charset.StandardCharsets;
import java.util.Locale;
import net.minecraft.server.ServerInterface;
import net.minecraft.server.rcon.PktUtils;
import org.slf4j.Logger;

public class RconClient extends GenericThread {
   private static final Logger LOGGER = LogUtils.getLogger();
   private static final int SERVERDATA_AUTH = 3;
   private static final int SERVERDATA_EXECCOMMAND = 2;
   private static final int SERVERDATA_RESPONSE_VALUE = 0;
   private static final int SERVERDATA_AUTH_RESPONSE = 2;
   private static final int SERVERDATA_AUTH_FAILURE = -1;
   private boolean authed;
   private final Socket client;
   private final byte[] buf = new byte[1460];
   private final String rconPassword;
   private final ServerInterface serverInterface;

   RconClient(ServerInterface serverinterface, String s, Socket socket) {
      super("RCON Client " + socket.getInetAddress());
      this.serverInterface = serverinterface;
      this.client = socket;

      try {
         this.client.setSoTimeout(0);
      } catch (Exception var5) {
         this.running = false;
      }

      this.rconPassword = s;
   }

   public void run() {
      while(true) {
         try {
            if (!this.running) {
               return;
            }

            BufferedInputStream bufferedinputstream = new BufferedInputStream(this.client.getInputStream());
            int i = bufferedinputstream.read(this.buf, 0, 1460);
            if (10 <= i) {
               int j = 0;
               int k = PktUtils.intFromByteArray(this.buf, 0, i);
               if (k != i - 4) {
                  return;
               }

               j += 4;
               int l = PktUtils.intFromByteArray(this.buf, j, i);
               j += 4;
               int i1 = PktUtils.intFromByteArray(this.buf, j);
               j += 4;
               switch (i1) {
                  case 2:
                     if (this.authed) {
                        String s1 = PktUtils.stringFromByteArray(this.buf, j, i);

                        try {
                           this.sendCmdResponse(l, this.serverInterface.runCommand(s1));
                        } catch (Exception var15) {
                           this.sendCmdResponse(l, "Error executing: " + s1 + " (" + var15.getMessage() + ")");
                        }
                        continue;
                     }

                     this.sendAuthFailure();
                     continue;
                  case 3:
                     String s = PktUtils.stringFromByteArray(this.buf, j, i);
                     int var10000 = j + s.length();
                     if (!s.isEmpty() && s.equals(this.rconPassword)) {
                        this.authed = true;
                        this.send(l, 2, "");
                        continue;
                     }

                     this.authed = false;
                     this.sendAuthFailure();
                     continue;
                  default:
                     this.sendCmdResponse(l, String.format(Locale.ROOT, "Unknown request %s", Integer.toHexString(i1)));
                     continue;
               }
            }
         } catch (IOException var16) {
            return;
         } catch (Exception var17) {
            LOGGER.error("Exception whilst parsing RCON input", (Throwable)var17);
            return;
         } finally {
            this.closeSocket();
            LOGGER.info("Thread {} shutting down", (Object)this.name);
            this.running = false;
         }

         return;
      }
   }

   private void send(int i, int j, String s) throws IOException {
      ByteArrayOutputStream bytearrayoutputstream = new ByteArrayOutputStream(1248);
      DataOutputStream dataoutputstream = new DataOutputStream(bytearrayoutputstream);
      byte[] abyte = s.getBytes(StandardCharsets.UTF_8);
      dataoutputstream.writeInt(Integer.reverseBytes(abyte.length + 10));
      dataoutputstream.writeInt(Integer.reverseBytes(i));
      dataoutputstream.writeInt(Integer.reverseBytes(j));
      dataoutputstream.write(abyte);
      dataoutputstream.write(0);
      dataoutputstream.write(0);
      this.client.getOutputStream().write(bytearrayoutputstream.toByteArray());
   }

   private void sendAuthFailure() throws IOException {
      this.send(-1, 2, "");
   }

   private void sendCmdResponse(int i, String s) throws IOException {
      int j = s.length();

      do {
         int k = 4096 <= j ? 4096 : j;
         this.send(i, 0, s.substring(0, k));
         s = s.substring(k);
         j = s.length();
      } while(0 != j);

   }

   public void stop() {
      this.running = false;
      this.closeSocket();
      super.stop();
   }

   private void closeSocket() {
      try {
         this.client.close();
      } catch (IOException var2) {
         LOGGER.warn("Failed to close socket", (Throwable)var2);
      }

   }
}
