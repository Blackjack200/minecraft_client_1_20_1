package com.mojang.realmsclient.client;

import com.mojang.realmsclient.exception.RealmsHttpException;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.Proxy;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import javax.annotation.Nullable;

public abstract class Request<T extends Request<T>> {
   protected HttpURLConnection connection;
   private boolean connected;
   protected String url;
   private static final int DEFAULT_READ_TIMEOUT = 60000;
   private static final int DEFAULT_CONNECT_TIMEOUT = 5000;

   public Request(String s, int i, int j) {
      try {
         this.url = s;
         Proxy proxy = RealmsClientConfig.getProxy();
         if (proxy != null) {
            this.connection = (HttpURLConnection)(new URL(s)).openConnection(proxy);
         } else {
            this.connection = (HttpURLConnection)(new URL(s)).openConnection();
         }

         this.connection.setConnectTimeout(i);
         this.connection.setReadTimeout(j);
      } catch (MalformedURLException var5) {
         throw new RealmsHttpException(var5.getMessage(), var5);
      } catch (IOException var6) {
         throw new RealmsHttpException(var6.getMessage(), var6);
      }
   }

   public void cookie(String s, String s1) {
      cookie(this.connection, s, s1);
   }

   public static void cookie(HttpURLConnection httpurlconnection, String s, String s1) {
      String s2 = httpurlconnection.getRequestProperty("Cookie");
      if (s2 == null) {
         httpurlconnection.setRequestProperty("Cookie", s + "=" + s1);
      } else {
         httpurlconnection.setRequestProperty("Cookie", s2 + ";" + s + "=" + s1);
      }

   }

   public T header(String s, String s1) {
      this.connection.addRequestProperty(s, s1);
      return (T)this;
   }

   public int getRetryAfterHeader() {
      return getRetryAfterHeader(this.connection);
   }

   public static int getRetryAfterHeader(HttpURLConnection httpurlconnection) {
      String s = httpurlconnection.getHeaderField("Retry-After");

      try {
         return Integer.valueOf(s);
      } catch (Exception var3) {
         return 5;
      }
   }

   public int responseCode() {
      try {
         this.connect();
         return this.connection.getResponseCode();
      } catch (Exception var2) {
         throw new RealmsHttpException(var2.getMessage(), var2);
      }
   }

   public String text() {
      try {
         this.connect();
         String s;
         if (this.responseCode() >= 400) {
            s = this.read(this.connection.getErrorStream());
         } else {
            s = this.read(this.connection.getInputStream());
         }

         this.dispose();
         return s;
      } catch (IOException var2) {
         throw new RealmsHttpException(var2.getMessage(), var2);
      }
   }

   private String read(@Nullable InputStream inputstream) throws IOException {
      if (inputstream == null) {
         return "";
      } else {
         InputStreamReader inputstreamreader = new InputStreamReader(inputstream, StandardCharsets.UTF_8);
         StringBuilder stringbuilder = new StringBuilder();

         for(int i = inputstreamreader.read(); i != -1; i = inputstreamreader.read()) {
            stringbuilder.append((char)i);
         }

         return stringbuilder.toString();
      }
   }

   private void dispose() {
      byte[] abyte = new byte[1024];

      try {
         InputStream inputstream = this.connection.getInputStream();

         while(inputstream.read(abyte) > 0) {
         }

         inputstream.close();
         return;
      } catch (Exception var9) {
         try {
            InputStream inputstream1 = this.connection.getErrorStream();
            if (inputstream1 != null) {
               while(inputstream1.read(abyte) > 0) {
               }

               inputstream1.close();
               return;
            }
         } catch (IOException var8) {
            return;
         }
      } finally {
         if (this.connection != null) {
            this.connection.disconnect();
         }

      }

   }

   protected T connect() {
      if (this.connected) {
         return (T)this;
      } else {
         T request = this.doConnect();
         this.connected = true;
         return request;
      }
   }

   protected abstract T doConnect();

   public static Request<?> get(String s) {
      return new Request.Get(s, 5000, 60000);
   }

   public static Request<?> get(String s, int i, int j) {
      return new Request.Get(s, i, j);
   }

   public static Request<?> post(String s, String s1) {
      return new Request.Post(s, s1, 5000, 60000);
   }

   public static Request<?> post(String s, String s1, int i, int j) {
      return new Request.Post(s, s1, i, j);
   }

   public static Request<?> delete(String s) {
      return new Request.Delete(s, 5000, 60000);
   }

   public static Request<?> put(String s, String s1) {
      return new Request.Put(s, s1, 5000, 60000);
   }

   public static Request<?> put(String s, String s1, int i, int j) {
      return new Request.Put(s, s1, i, j);
   }

   public String getHeader(String s) {
      return getHeader(this.connection, s);
   }

   public static String getHeader(HttpURLConnection httpurlconnection, String s) {
      try {
         return httpurlconnection.getHeaderField(s);
      } catch (Exception var3) {
         return "";
      }
   }

   public static class Delete extends Request<Request.Delete> {
      public Delete(String s, int i, int j) {
         super(s, i, j);
      }

      public Request.Delete doConnect() {
         try {
            this.connection.setDoOutput(true);
            this.connection.setRequestMethod("DELETE");
            this.connection.connect();
            return this;
         } catch (Exception var2) {
            throw new RealmsHttpException(var2.getMessage(), var2);
         }
      }
   }

   public static class Get extends Request<Request.Get> {
      public Get(String s, int i, int j) {
         super(s, i, j);
      }

      public Request.Get doConnect() {
         try {
            this.connection.setDoInput(true);
            this.connection.setDoOutput(true);
            this.connection.setUseCaches(false);
            this.connection.setRequestMethod("GET");
            return this;
         } catch (Exception var2) {
            throw new RealmsHttpException(var2.getMessage(), var2);
         }
      }
   }

   public static class Post extends Request<Request.Post> {
      private final String content;

      public Post(String s, String s1, int i, int j) {
         super(s, i, j);
         this.content = s1;
      }

      public Request.Post doConnect() {
         try {
            if (this.content != null) {
               this.connection.setRequestProperty("Content-Type", "application/json; charset=utf-8");
            }

            this.connection.setDoInput(true);
            this.connection.setDoOutput(true);
            this.connection.setUseCaches(false);
            this.connection.setRequestMethod("POST");
            OutputStream outputstream = this.connection.getOutputStream();
            OutputStreamWriter outputstreamwriter = new OutputStreamWriter(outputstream, "UTF-8");
            outputstreamwriter.write(this.content);
            outputstreamwriter.close();
            outputstream.flush();
            return this;
         } catch (Exception var3) {
            throw new RealmsHttpException(var3.getMessage(), var3);
         }
      }
   }

   public static class Put extends Request<Request.Put> {
      private final String content;

      public Put(String s, String s1, int i, int j) {
         super(s, i, j);
         this.content = s1;
      }

      public Request.Put doConnect() {
         try {
            if (this.content != null) {
               this.connection.setRequestProperty("Content-Type", "application/json; charset=utf-8");
            }

            this.connection.setDoOutput(true);
            this.connection.setDoInput(true);
            this.connection.setRequestMethod("PUT");
            OutputStream outputstream = this.connection.getOutputStream();
            OutputStreamWriter outputstreamwriter = new OutputStreamWriter(outputstream, "UTF-8");
            outputstreamwriter.write(this.content);
            outputstreamwriter.close();
            outputstream.flush();
            return this;
         } catch (Exception var3) {
            throw new RealmsHttpException(var3.getMessage(), var3);
         }
      }
   }
}
