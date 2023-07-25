package com.mojang.realmsclient.dto;

import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.mojang.logging.LogUtils;
import com.mojang.realmsclient.util.JsonUtils;
import org.slf4j.Logger;

public class Subscription extends ValueObject {
   private static final Logger LOGGER = LogUtils.getLogger();
   public long startDate;
   public int daysLeft;
   public Subscription.SubscriptionType type = Subscription.SubscriptionType.NORMAL;

   public static Subscription parse(String s) {
      Subscription subscription = new Subscription();

      try {
         JsonParser jsonparser = new JsonParser();
         JsonObject jsonobject = jsonparser.parse(s).getAsJsonObject();
         subscription.startDate = JsonUtils.getLongOr("startDate", jsonobject, 0L);
         subscription.daysLeft = JsonUtils.getIntOr("daysLeft", jsonobject, 0);
         subscription.type = typeFrom(JsonUtils.getStringOr("subscriptionType", jsonobject, Subscription.SubscriptionType.NORMAL.name()));
      } catch (Exception var4) {
         LOGGER.error("Could not parse Subscription: {}", (Object)var4.getMessage());
      }

      return subscription;
   }

   private static Subscription.SubscriptionType typeFrom(String s) {
      try {
         return Subscription.SubscriptionType.valueOf(s);
      } catch (Exception var2) {
         return Subscription.SubscriptionType.NORMAL;
      }
   }

   public static enum SubscriptionType {
      NORMAL,
      RECURRING;
   }
}
