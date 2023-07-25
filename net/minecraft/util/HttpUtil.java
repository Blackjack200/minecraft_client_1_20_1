package net.minecraft.util;

import com.google.common.util.concurrent.ListeningExecutorService;
import com.google.common.util.concurrent.MoreExecutors;
import com.google.common.util.concurrent.ThreadFactoryBuilder;
import com.mojang.logging.LogUtils;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.Proxy;
import java.net.ServerSocket;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.util.Locale;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Executors;
import javax.annotation.Nullable;
import net.minecraft.DefaultUncaughtExceptionHandler;
import net.minecraft.network.chat.Component;
import org.apache.commons.io.FileUtils;
import org.apache.commons.io.IOUtils;
import org.slf4j.Logger;

public class HttpUtil {
   private static final Logger LOGGER = LogUtils.getLogger();
   public static final ListeningExecutorService DOWNLOAD_EXECUTOR = MoreExecutors.listeningDecorator(Executors.newCachedThreadPool((new ThreadFactoryBuilder()).setDaemon(true).setUncaughtExceptionHandler(new DefaultUncaughtExceptionHandler(LOGGER)).setNameFormat("Downloader %d").build()));

   private HttpUtil() {
   }

   public static CompletableFuture<?> downloadTo(File file, URL url, Map<String, String> map, int i, @Nullable ProgressListener progresslistener, Proxy proxy) {
      return CompletableFuture.supplyAsync(() -> {
         HttpURLConnection httpurlconnection = null;
         InputStream inputstream = null;
         OutputStream outputstream = null;
         if (progresslistener != null) {
            progresslistener.progressStart(Component.translatable("resourcepack.downloading"));
            progresslistener.progressStage(Component.translatable("resourcepack.requesting"));
         }

         try {
            try {
               byte[] abyte = new byte[4096];
               httpurlconnection = (HttpURLConnection)url.openConnection(proxy);
               httpurlconnection.setInstanceFollowRedirects(true);
               float f = 0.0F;
               float f1 = (float)map.entrySet().size();

               for(Map.Entry<String, String> map_entry : map.entrySet()) {
                  httpurlconnection.setRequestProperty(map_entry.getKey(), map_entry.getValue());
                  if (progresslistener != null) {
                     progresslistener.progressStagePercentage((int)(++f / f1 * 100.0F));
                  }
               }

               inputstream = httpurlconnection.getInputStream();
               f1 = (float)httpurlconnection.getContentLength();
               int k = httpurlconnection.getContentLength();
               if (progresslistener != null) {
                  progresslistener.progressStage(Component.translatable("resourcepack.progress", String.format(Locale.ROOT, "%.2f", f1 / 1000.0F / 1000.0F)));
               }

               if (file.exists()) {
                  long l = file.length();
                  if (l == (long)k) {
                     if (progresslistener != null) {
                        progresslistener.stop();
                     }

                     return null;
                  }

                  LOGGER.warn("Deleting {} as it does not match what we currently have ({} vs our {}).", file, k, l);
                  FileUtils.deleteQuietly(file);
               } else if (file.getParentFile() != null) {
                  file.getParentFile().mkdirs();
               }

               outputstream = new DataOutputStream(new FileOutputStream(file));
               if (i > 0 && f1 > (float)i) {
                  if (progresslistener != null) {
                     progresslistener.stop();
                  }

                  throw new IOException("Filesize is bigger than maximum allowed (file is " + f + ", limit is " + i + ")");
               }

               int i1;
               while((i1 = inputstream.read(abyte)) >= 0) {
                  f += (float)i1;
                  if (progresslistener != null) {
                     progresslistener.progressStagePercentage((int)(f / f1 * 100.0F));
                  }

                  if (i > 0 && f > (float)i) {
                     if (progresslistener != null) {
                        progresslistener.stop();
                     }

                     throw new IOException("Filesize was bigger than maximum allowed (got >= " + f + ", limit was " + i + ")");
                  }

                  if (Thread.interrupted()) {
                     LOGGER.error("INTERRUPTED");
                     if (progresslistener != null) {
                        progresslistener.stop();
                     }

                     return null;
                  }

                  outputstream.write(abyte, 0, i1);
               }

               if (progresslistener != null) {
                  progresslistener.stop();
                  return null;
               }
            } catch (Throwable var21) {
               LOGGER.error("Failed to download file", var21);
               if (httpurlconnection != null) {
                  InputStream inputstream1 = httpurlconnection.getErrorStream();

                  try {
                     LOGGER.error("HTTP response error: {}", (Object)IOUtils.toString(inputstream1, StandardCharsets.UTF_8));
                  } catch (IOException var20) {
                     LOGGER.error("Failed to read response from server");
                  }
               }

               if (progresslistener != null) {
                  progresslistener.stop();
                  return null;
               }
            }

            return null;
         } finally {
            IOUtils.closeQuietly(inputstream);
            IOUtils.closeQuietly(outputstream);
         }
      }, DOWNLOAD_EXECUTOR);
   }

   public static int getAvailablePort() {
      try {
         ServerSocket serversocket = new ServerSocket(0);

         int var1;
         try {
            var1 = serversocket.getLocalPort();
         } catch (Throwable var4) {
            try {
               serversocket.close();
            } catch (Throwable var3) {
               var4.addSuppressed(var3);
            }

            throw var4;
         }

         serversocket.close();
         return var1;
      } catch (IOException var5) {
         return 25564;
      }
   }

   public static boolean isPortAvailable(int i) {
      if (i >= 0 && i <= 65535) {
         try {
            ServerSocket serversocket = new ServerSocket(i);

            boolean var2;
            try {
               var2 = serversocket.getLocalPort() == i;
            } catch (Throwable var5) {
               try {
                  serversocket.close();
               } catch (Throwable var4) {
                  var5.addSuppressed(var4);
               }

               throw var5;
            }

            serversocket.close();
            return var2;
         } catch (IOException var6) {
            return false;
         }
      } else {
         return false;
      }
   }
}
