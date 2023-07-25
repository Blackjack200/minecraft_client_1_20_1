package com.mojang.realmsclient.dto;

import com.google.common.annotations.VisibleForTesting;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.mojang.logging.LogUtils;
import com.mojang.realmsclient.util.JsonUtils;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import javax.annotation.Nullable;
import org.slf4j.Logger;

public class UploadInfo extends ValueObject {
   private static final Logger LOGGER = LogUtils.getLogger();
   private static final String DEFAULT_SCHEMA = "http://";
   private static final int DEFAULT_PORT = 8080;
   private static final Pattern URI_SCHEMA_PATTERN = Pattern.compile("^[a-zA-Z][-a-zA-Z0-9+.]+:");
   private final boolean worldClosed;
   @Nullable
   private final String token;
   private final URI uploadEndpoint;

   private UploadInfo(boolean flag, @Nullable String s, URI uri) {
      this.worldClosed = flag;
      this.token = s;
      this.uploadEndpoint = uri;
   }

   @Nullable
   public static UploadInfo parse(String s) {
      try {
         JsonParser jsonparser = new JsonParser();
         JsonObject jsonobject = jsonparser.parse(s).getAsJsonObject();
         String s1 = JsonUtils.getStringOr("uploadEndpoint", jsonobject, (String)null);
         if (s1 != null) {
            int i = JsonUtils.getIntOr("port", jsonobject, -1);
            URI uri = assembleUri(s1, i);
            if (uri != null) {
               boolean flag = JsonUtils.getBooleanOr("worldClosed", jsonobject, false);
               String s2 = JsonUtils.getStringOr("token", jsonobject, (String)null);
               return new UploadInfo(flag, s2, uri);
            }
         }
      } catch (Exception var8) {
         LOGGER.error("Could not parse UploadInfo: {}", (Object)var8.getMessage());
      }

      return null;
   }

   @Nullable
   @VisibleForTesting
   public static URI assembleUri(String s, int i) {
      Matcher matcher = URI_SCHEMA_PATTERN.matcher(s);
      String s1 = ensureEndpointSchema(s, matcher);

      try {
         URI uri = new URI(s1);
         int j = selectPortOrDefault(i, uri.getPort());
         return j != uri.getPort() ? new URI(uri.getScheme(), uri.getUserInfo(), uri.getHost(), j, uri.getPath(), uri.getQuery(), uri.getFragment()) : uri;
      } catch (URISyntaxException var6) {
         LOGGER.warn("Failed to parse URI {}", s1, var6);
         return null;
      }
   }

   private static int selectPortOrDefault(int i, int j) {
      if (i != -1) {
         return i;
      } else {
         return j != -1 ? j : 8080;
      }
   }

   private static String ensureEndpointSchema(String s, Matcher matcher) {
      return matcher.find() ? s : "http://" + s;
   }

   public static String createRequest(@Nullable String s) {
      JsonObject jsonobject = new JsonObject();
      if (s != null) {
         jsonobject.addProperty("token", s);
      }

      return jsonobject.toString();
   }

   @Nullable
   public String getToken() {
      return this.token;
   }

   public URI getUploadEndpoint() {
      return this.uploadEndpoint;
   }

   public boolean isWorldClosed() {
      return this.worldClosed;
   }
}
