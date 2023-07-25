package com.mojang.realmsclient.client;

import com.google.common.base.Strings;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.mojang.logging.LogUtils;
import com.mojang.realmsclient.util.JsonUtils;
import javax.annotation.Nullable;
import org.slf4j.Logger;

public class RealmsError {
   private static final Logger LOGGER = LogUtils.getLogger();
   private final String errorMessage;
   private final int errorCode;

   private RealmsError(String s, int i) {
      this.errorMessage = s;
      this.errorCode = i;
   }

   @Nullable
   public static RealmsError parse(String s) {
      if (Strings.isNullOrEmpty(s)) {
         return null;
      } else {
         try {
            JsonObject jsonobject = JsonParser.parseString(s).getAsJsonObject();
            String s1 = JsonUtils.getStringOr("errorMsg", jsonobject, "");
            int i = JsonUtils.getIntOr("errorCode", jsonobject, -1);
            return new RealmsError(s1, i);
         } catch (Exception var4) {
            LOGGER.error("Could not parse RealmsError: {}", (Object)var4.getMessage());
            LOGGER.error("The error was: {}", (Object)s);
            return null;
         }
      }
   }

   public String getErrorMessage() {
      return this.errorMessage;
   }

   public int getErrorCode() {
      return this.errorCode;
   }
}
