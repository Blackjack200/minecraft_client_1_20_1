package com.mojang.realmsclient.client;

import com.google.gson.JsonElement;
import com.google.gson.JsonParser;
import com.mojang.logging.LogUtils;
import com.mojang.realmsclient.dto.UploadInfo;
import com.mojang.realmsclient.gui.screens.UploadResult;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.time.Duration;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.function.Consumer;
import javax.annotation.Nullable;
import net.minecraft.client.User;
import org.apache.http.HttpResponse;
import org.apache.http.NameValuePair;
import org.apache.http.client.config.RequestConfig;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.InputStreamEntity;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClientBuilder;
import org.apache.http.util.Args;
import org.apache.http.util.EntityUtils;
import org.slf4j.Logger;

public class FileUpload {
   private static final Logger LOGGER = LogUtils.getLogger();
   private static final int MAX_RETRIES = 5;
   private static final String UPLOAD_PATH = "/upload";
   private final File file;
   private final long worldId;
   private final int slotId;
   private final UploadInfo uploadInfo;
   private final String sessionId;
   private final String username;
   private final String clientVersion;
   private final UploadStatus uploadStatus;
   private final AtomicBoolean cancelled = new AtomicBoolean(false);
   @Nullable
   private CompletableFuture<UploadResult> uploadTask;
   private final RequestConfig requestConfig = RequestConfig.custom().setSocketTimeout((int)TimeUnit.MINUTES.toMillis(10L)).setConnectTimeout((int)TimeUnit.SECONDS.toMillis(15L)).build();

   public FileUpload(File file, long i, int j, UploadInfo uploadinfo, User user, String s, UploadStatus uploadstatus) {
      this.file = file;
      this.worldId = i;
      this.slotId = j;
      this.uploadInfo = uploadinfo;
      this.sessionId = user.getSessionId();
      this.username = user.getName();
      this.clientVersion = s;
      this.uploadStatus = uploadstatus;
   }

   public void upload(Consumer<UploadResult> consumer) {
      if (this.uploadTask == null) {
         this.uploadTask = CompletableFuture.supplyAsync(() -> this.requestUpload(0));
         this.uploadTask.thenAccept(consumer);
      }
   }

   public void cancel() {
      this.cancelled.set(true);
      if (this.uploadTask != null) {
         this.uploadTask.cancel(false);
         this.uploadTask = null;
      }

   }

   private UploadResult requestUpload(int i) {
      UploadResult.Builder uploadresult_builder = new UploadResult.Builder();
      if (this.cancelled.get()) {
         return uploadresult_builder.build();
      } else {
         this.uploadStatus.totalBytes = this.file.length();
         HttpPost httppost = new HttpPost(this.uploadInfo.getUploadEndpoint().resolve("/upload/" + this.worldId + "/" + this.slotId));
         CloseableHttpClient closeablehttpclient = HttpClientBuilder.create().setDefaultRequestConfig(this.requestConfig).build();

         UploadResult var8;
         try {
            this.setupRequest(httppost);
            HttpResponse httpresponse = closeablehttpclient.execute(httppost);
            long j = this.getRetryDelaySeconds(httpresponse);
            if (!this.shouldRetry(j, i)) {
               this.handleResponse(httpresponse, uploadresult_builder);
               return uploadresult_builder.build();
            }

            var8 = this.retryUploadAfter(j, i);
         } catch (Exception var12) {
            if (!this.cancelled.get()) {
               LOGGER.error("Caught exception while uploading: ", (Throwable)var12);
            }

            return uploadresult_builder.build();
         } finally {
            this.cleanup(httppost, closeablehttpclient);
         }

         return var8;
      }
   }

   private void cleanup(HttpPost httppost, @Nullable CloseableHttpClient closeablehttpclient) {
      httppost.releaseConnection();
      if (closeablehttpclient != null) {
         try {
            closeablehttpclient.close();
         } catch (IOException var4) {
            LOGGER.error("Failed to close Realms upload client");
         }
      }

   }

   private void setupRequest(HttpPost httppost) throws FileNotFoundException {
      httppost.setHeader("Cookie", "sid=" + this.sessionId + ";token=" + this.uploadInfo.getToken() + ";user=" + this.username + ";version=" + this.clientVersion);
      FileUpload.CustomInputStreamEntity fileupload_custominputstreamentity = new FileUpload.CustomInputStreamEntity(new FileInputStream(this.file), this.file.length(), this.uploadStatus);
      fileupload_custominputstreamentity.setContentType("application/octet-stream");
      httppost.setEntity(fileupload_custominputstreamentity);
   }

   private void handleResponse(HttpResponse httpresponse, UploadResult.Builder uploadresult_builder) throws IOException {
      int i = httpresponse.getStatusLine().getStatusCode();
      if (i == 401) {
         LOGGER.debug("Realms server returned 401: {}", (Object)httpresponse.getFirstHeader("WWW-Authenticate"));
      }

      uploadresult_builder.withStatusCode(i);
      if (httpresponse.getEntity() != null) {
         String s = EntityUtils.toString(httpresponse.getEntity(), "UTF-8");
         if (s != null) {
            try {
               JsonParser jsonparser = new JsonParser();
               JsonElement jsonelement = jsonparser.parse(s).getAsJsonObject().get("errorMsg");
               Optional<String> optional = Optional.ofNullable(jsonelement).map(JsonElement::getAsString);
               uploadresult_builder.withErrorMessage(optional.orElse((String)null));
            } catch (Exception var8) {
            }
         }
      }

   }

   private boolean shouldRetry(long i, int j) {
      return i > 0L && j + 1 < 5;
   }

   private UploadResult retryUploadAfter(long i, int j) throws InterruptedException {
      Thread.sleep(Duration.ofSeconds(i).toMillis());
      return this.requestUpload(j + 1);
   }

   private long getRetryDelaySeconds(HttpResponse httpresponse) {
      return Optional.ofNullable(httpresponse.getFirstHeader("Retry-After")).map(NameValuePair::getValue).map(Long::valueOf).orElse(0L);
   }

   public boolean isFinished() {
      return this.uploadTask.isDone() || this.uploadTask.isCancelled();
   }

   static class CustomInputStreamEntity extends InputStreamEntity {
      private final long length;
      private final InputStream content;
      private final UploadStatus uploadStatus;

      public CustomInputStreamEntity(InputStream inputstream, long i, UploadStatus uploadstatus) {
         super(inputstream);
         this.content = inputstream;
         this.length = i;
         this.uploadStatus = uploadstatus;
      }

      public void writeTo(OutputStream outputstream) throws IOException {
         Args.notNull(outputstream, "Output stream");
         InputStream inputstream = this.content;

         try {
            byte[] abyte = new byte[4096];
            int i;
            if (this.length < 0L) {
               while((i = inputstream.read(abyte)) != -1) {
                  outputstream.write(abyte, 0, i);
                  this.uploadStatus.bytesWritten += (long)i;
               }
            } else {
               long j = this.length;

               while(j > 0L) {
                  i = inputstream.read(abyte, 0, (int)Math.min(4096L, j));
                  if (i == -1) {
                     break;
                  }

                  outputstream.write(abyte, 0, i);
                  this.uploadStatus.bytesWritten += (long)i;
                  j -= (long)i;
                  outputstream.flush();
               }
            }
         } finally {
            inputstream.close();
         }

      }
   }
}
