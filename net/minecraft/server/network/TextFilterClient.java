package net.minecraft.server.network;

import com.google.common.base.Strings;
import com.google.common.collect.ImmutableList;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.google.gson.internal.Streams;
import com.google.gson.stream.JsonReader;
import com.google.gson.stream.JsonWriter;
import com.mojang.authlib.GameProfile;
import com.mojang.logging.LogUtils;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URI;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.util.Base64;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Executor;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.atomic.AtomicInteger;
import javax.annotation.Nullable;
import net.minecraft.SharedConstants;
import net.minecraft.Util;
import net.minecraft.network.chat.FilterMask;
import net.minecraft.util.GsonHelper;
import net.minecraft.util.thread.ProcessorMailbox;
import org.slf4j.Logger;

public class TextFilterClient implements AutoCloseable {
   private static final Logger LOGGER = LogUtils.getLogger();
   private static final AtomicInteger WORKER_COUNT = new AtomicInteger(1);
   private static final ThreadFactory THREAD_FACTORY = (runnable) -> {
      Thread thread = new Thread(runnable);
      thread.setName("Chat-Filter-Worker-" + WORKER_COUNT.getAndIncrement());
      return thread;
   };
   private static final String DEFAULT_ENDPOINT = "v1/chat";
   private final URL chatEndpoint;
   private final TextFilterClient.MessageEncoder chatEncoder;
   final URL joinEndpoint;
   final TextFilterClient.JoinOrLeaveEncoder joinEncoder;
   final URL leaveEndpoint;
   final TextFilterClient.JoinOrLeaveEncoder leaveEncoder;
   private final String authKey;
   final TextFilterClient.IgnoreStrategy chatIgnoreStrategy;
   final ExecutorService workerPool;

   private TextFilterClient(URL url, TextFilterClient.MessageEncoder textfilterclient_messageencoder, URL url1, TextFilterClient.JoinOrLeaveEncoder textfilterclient_joinorleaveencoder, URL url2, TextFilterClient.JoinOrLeaveEncoder textfilterclient_joinorleaveencoder1, String s, TextFilterClient.IgnoreStrategy textfilterclient_ignorestrategy, int i) {
      this.authKey = s;
      this.chatIgnoreStrategy = textfilterclient_ignorestrategy;
      this.chatEndpoint = url;
      this.chatEncoder = textfilterclient_messageencoder;
      this.joinEndpoint = url1;
      this.joinEncoder = textfilterclient_joinorleaveencoder;
      this.leaveEndpoint = url2;
      this.leaveEncoder = textfilterclient_joinorleaveencoder1;
      this.workerPool = Executors.newFixedThreadPool(i, THREAD_FACTORY);
   }

   private static URL getEndpoint(URI uri, @Nullable JsonObject jsonobject, String s, String s1) throws MalformedURLException {
      String s2 = getEndpointFromConfig(jsonobject, s, s1);
      return uri.resolve("/" + s2).toURL();
   }

   private static String getEndpointFromConfig(@Nullable JsonObject jsonobject, String s, String s1) {
      return jsonobject != null ? GsonHelper.getAsString(jsonobject, s, s1) : s1;
   }

   @Nullable
   public static TextFilterClient createFromConfig(String s) {
      if (Strings.isNullOrEmpty(s)) {
         return null;
      } else {
         try {
            JsonObject jsonobject = GsonHelper.parse(s);
            URI uri = new URI(GsonHelper.getAsString(jsonobject, "apiServer"));
            String s1 = GsonHelper.getAsString(jsonobject, "apiKey");
            if (s1.isEmpty()) {
               throw new IllegalArgumentException("Missing API key");
            } else {
               int i = GsonHelper.getAsInt(jsonobject, "ruleId", 1);
               String s2 = GsonHelper.getAsString(jsonobject, "serverId", "");
               String s3 = GsonHelper.getAsString(jsonobject, "roomId", "Java:Chat");
               int j = GsonHelper.getAsInt(jsonobject, "hashesToDrop", -1);
               int k = GsonHelper.getAsInt(jsonobject, "maxConcurrentRequests", 7);
               JsonObject jsonobject1 = GsonHelper.getAsJsonObject(jsonobject, "endpoints", (JsonObject)null);
               String s4 = getEndpointFromConfig(jsonobject1, "chat", "v1/chat");
               boolean flag = s4.equals("v1/chat");
               URL url = uri.resolve("/" + s4).toURL();
               URL url1 = getEndpoint(uri, jsonobject1, "join", "v1/join");
               URL url2 = getEndpoint(uri, jsonobject1, "leave", "v1/leave");
               TextFilterClient.JoinOrLeaveEncoder textfilterclient_joinorleaveencoder = (gameprofile2) -> {
                  JsonObject jsonobject4 = new JsonObject();
                  jsonobject4.addProperty("server", s2);
                  jsonobject4.addProperty("room", s3);
                  jsonobject4.addProperty("user_id", gameprofile2.getId().toString());
                  jsonobject4.addProperty("user_display_name", gameprofile2.getName());
                  return jsonobject4;
               };
               TextFilterClient.MessageEncoder textfilterclient_messageencoder;
               if (flag) {
                  textfilterclient_messageencoder = (gameprofile1, s13) -> {
                     JsonObject jsonobject3 = new JsonObject();
                     jsonobject3.addProperty("rule", i);
                     jsonobject3.addProperty("server", s2);
                     jsonobject3.addProperty("room", s3);
                     jsonobject3.addProperty("player", gameprofile1.getId().toString());
                     jsonobject3.addProperty("player_display_name", gameprofile1.getName());
                     jsonobject3.addProperty("text", s13);
                     jsonobject3.addProperty("language", "*");
                     return jsonobject3;
                  };
               } else {
                  String s5 = String.valueOf(i);
                  textfilterclient_messageencoder = (gameprofile, s10) -> {
                     JsonObject jsonobject2 = new JsonObject();
                     jsonobject2.addProperty("rule_id", s5);
                     jsonobject2.addProperty("category", s2);
                     jsonobject2.addProperty("subcategory", s3);
                     jsonobject2.addProperty("user_id", gameprofile.getId().toString());
                     jsonobject2.addProperty("user_display_name", gameprofile.getName());
                     jsonobject2.addProperty("text", s10);
                     jsonobject2.addProperty("language", "*");
                     return jsonobject2;
                  };
               }

               TextFilterClient.IgnoreStrategy textfilterclient_ignorestrategy = TextFilterClient.IgnoreStrategy.select(j);
               String s6 = Base64.getEncoder().encodeToString(s1.getBytes(StandardCharsets.US_ASCII));
               return new TextFilterClient(url, textfilterclient_messageencoder, url1, textfilterclient_joinorleaveencoder, url2, textfilterclient_joinorleaveencoder, s6, textfilterclient_ignorestrategy, k);
            }
         } catch (Exception var19) {
            LOGGER.warn("Failed to parse chat filter config {}", s, var19);
            return null;
         }
      }
   }

   void processJoinOrLeave(GameProfile gameprofile, URL url, TextFilterClient.JoinOrLeaveEncoder textfilterclient_joinorleaveencoder, Executor executor) {
      executor.execute(() -> {
         JsonObject jsonobject = textfilterclient_joinorleaveencoder.encode(gameprofile);

         try {
            this.processRequest(jsonobject, url);
         } catch (Exception var6) {
            LOGGER.warn("Failed to send join/leave packet to {} for player {}", url, gameprofile, var6);
         }

      });
   }

   CompletableFuture<FilteredText> requestMessageProcessing(GameProfile gameprofile, String s, TextFilterClient.IgnoreStrategy textfilterclient_ignorestrategy, Executor executor) {
      return s.isEmpty() ? CompletableFuture.completedFuture(FilteredText.EMPTY) : CompletableFuture.supplyAsync(() -> {
         JsonObject jsonobject = this.chatEncoder.encode(gameprofile, s);

         try {
            JsonObject jsonobject1 = this.processRequestResponse(jsonobject, this.chatEndpoint);
            boolean flag = GsonHelper.getAsBoolean(jsonobject1, "response", false);
            if (flag) {
               return FilteredText.passThrough(s);
            } else {
               String s2 = GsonHelper.getAsString(jsonobject1, "hashed", (String)null);
               if (s2 == null) {
                  return FilteredText.fullyFiltered(s);
               } else {
                  JsonArray jsonarray = GsonHelper.getAsJsonArray(jsonobject1, "hashes");
                  FilterMask filtermask = this.parseMask(s, jsonarray, textfilterclient_ignorestrategy);
                  return new FilteredText(s, filtermask);
               }
            }
         } catch (Exception var10) {
            LOGGER.warn("Failed to validate message '{}'", s, var10);
            return FilteredText.fullyFiltered(s);
         }
      }, executor);
   }

   private FilterMask parseMask(String s, JsonArray jsonarray, TextFilterClient.IgnoreStrategy textfilterclient_ignorestrategy) {
      if (jsonarray.isEmpty()) {
         return FilterMask.PASS_THROUGH;
      } else if (textfilterclient_ignorestrategy.shouldIgnore(s, jsonarray.size())) {
         return FilterMask.FULLY_FILTERED;
      } else {
         FilterMask filtermask = new FilterMask(s.length());

         for(int i = 0; i < jsonarray.size(); ++i) {
            filtermask.setFiltered(jsonarray.get(i).getAsInt());
         }

         return filtermask;
      }
   }

   public void close() {
      this.workerPool.shutdownNow();
   }

   private void drainStream(InputStream inputstream) throws IOException {
      byte[] abyte = new byte[1024];

      while(inputstream.read(abyte) != -1) {
      }

   }

   private JsonObject processRequestResponse(JsonObject jsonobject, URL url) throws IOException {
      HttpURLConnection httpurlconnection = this.makeRequest(jsonobject, url);
      InputStream inputstream = httpurlconnection.getInputStream();

      JsonObject var13;
      label89: {
         try {
            if (httpurlconnection.getResponseCode() == 204) {
               var13 = new JsonObject();
               break label89;
            }

            try {
               var13 = Streams.parse(new JsonReader(new InputStreamReader(inputstream, StandardCharsets.UTF_8))).getAsJsonObject();
            } finally {
               this.drainStream(inputstream);
            }
         } catch (Throwable var12) {
            if (inputstream != null) {
               try {
                  inputstream.close();
               } catch (Throwable var10) {
                  var12.addSuppressed(var10);
               }
            }

            throw var12;
         }

         if (inputstream != null) {
            inputstream.close();
         }

         return var13;
      }

      if (inputstream != null) {
         inputstream.close();
      }

      return var13;
   }

   private void processRequest(JsonObject jsonobject, URL url) throws IOException {
      HttpURLConnection httpurlconnection = this.makeRequest(jsonobject, url);
      InputStream inputstream = httpurlconnection.getInputStream();

      try {
         this.drainStream(inputstream);
      } catch (Throwable var8) {
         if (inputstream != null) {
            try {
               inputstream.close();
            } catch (Throwable var7) {
               var8.addSuppressed(var7);
            }
         }

         throw var8;
      }

      if (inputstream != null) {
         inputstream.close();
      }

   }

   private HttpURLConnection makeRequest(JsonObject jsonobject, URL url) throws IOException {
      HttpURLConnection httpurlconnection = (HttpURLConnection)url.openConnection();
      httpurlconnection.setConnectTimeout(15000);
      httpurlconnection.setReadTimeout(2000);
      httpurlconnection.setUseCaches(false);
      httpurlconnection.setDoOutput(true);
      httpurlconnection.setDoInput(true);
      httpurlconnection.setRequestMethod("POST");
      httpurlconnection.setRequestProperty("Content-Type", "application/json; charset=utf-8");
      httpurlconnection.setRequestProperty("Accept", "application/json");
      httpurlconnection.setRequestProperty("Authorization", "Basic " + this.authKey);
      httpurlconnection.setRequestProperty("User-Agent", "Minecraft server" + SharedConstants.getCurrentVersion().getName());
      OutputStreamWriter outputstreamwriter = new OutputStreamWriter(httpurlconnection.getOutputStream(), StandardCharsets.UTF_8);

      try {
         JsonWriter jsonwriter = new JsonWriter(outputstreamwriter);

         try {
            Streams.write(jsonobject, jsonwriter);
         } catch (Throwable var10) {
            try {
               jsonwriter.close();
            } catch (Throwable var9) {
               var10.addSuppressed(var9);
            }

            throw var10;
         }

         jsonwriter.close();
      } catch (Throwable var11) {
         try {
            outputstreamwriter.close();
         } catch (Throwable var8) {
            var11.addSuppressed(var8);
         }

         throw var11;
      }

      outputstreamwriter.close();
      int i = httpurlconnection.getResponseCode();
      if (i >= 200 && i < 300) {
         return httpurlconnection;
      } else {
         throw new TextFilterClient.RequestFailedException(i + " " + httpurlconnection.getResponseMessage());
      }
   }

   public TextFilter createContext(GameProfile gameprofile) {
      return new TextFilterClient.PlayerContext(gameprofile);
   }

   @FunctionalInterface
   public interface IgnoreStrategy {
      TextFilterClient.IgnoreStrategy NEVER_IGNORE = (s, i) -> false;
      TextFilterClient.IgnoreStrategy IGNORE_FULLY_FILTERED = (s, i) -> s.length() == i;

      static TextFilterClient.IgnoreStrategy ignoreOverThreshold(int i) {
         return (s, k) -> k >= i;
      }

      static TextFilterClient.IgnoreStrategy select(int i) {
         TextFilterClient.IgnoreStrategy var10000;
         switch (i) {
            case -1:
               var10000 = NEVER_IGNORE;
               break;
            case 0:
               var10000 = IGNORE_FULLY_FILTERED;
               break;
            default:
               var10000 = ignoreOverThreshold(i);
         }

         return var10000;
      }

      boolean shouldIgnore(String s, int i);
   }

   @FunctionalInterface
   interface JoinOrLeaveEncoder {
      JsonObject encode(GameProfile gameprofile);
   }

   @FunctionalInterface
   interface MessageEncoder {
      JsonObject encode(GameProfile gameprofile, String s);
   }

   class PlayerContext implements TextFilter {
      private final GameProfile profile;
      private final Executor streamExecutor;

      PlayerContext(GameProfile gameprofile) {
         this.profile = gameprofile;
         ProcessorMailbox<Runnable> processormailbox = ProcessorMailbox.create(TextFilterClient.this.workerPool, "chat stream for " + gameprofile.getName());
         this.streamExecutor = processormailbox::tell;
      }

      public void join() {
         TextFilterClient.this.processJoinOrLeave(this.profile, TextFilterClient.this.joinEndpoint, TextFilterClient.this.joinEncoder, this.streamExecutor);
      }

      public void leave() {
         TextFilterClient.this.processJoinOrLeave(this.profile, TextFilterClient.this.leaveEndpoint, TextFilterClient.this.leaveEncoder, this.streamExecutor);
      }

      public CompletableFuture<List<FilteredText>> processMessageBundle(List<String> list) {
         List<CompletableFuture<FilteredText>> list1 = list.stream().map((s) -> TextFilterClient.this.requestMessageProcessing(this.profile, s, TextFilterClient.this.chatIgnoreStrategy, this.streamExecutor)).collect(ImmutableList.toImmutableList());
         return Util.sequenceFailFast(list1).exceptionally((throwable) -> ImmutableList.of());
      }

      public CompletableFuture<FilteredText> processStreamMessage(String s) {
         return TextFilterClient.this.requestMessageProcessing(this.profile, s, TextFilterClient.this.chatIgnoreStrategy, this.streamExecutor);
      }
   }

   public static class RequestFailedException extends RuntimeException {
      RequestFailedException(String s) {
         super(s);
      }
   }
}
