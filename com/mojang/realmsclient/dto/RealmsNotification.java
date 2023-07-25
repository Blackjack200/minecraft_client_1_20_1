package com.mojang.realmsclient.dto;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.mojang.logging.LogUtils;
import com.mojang.realmsclient.util.JsonUtils;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.screens.ConfirmLinkScreen;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;
import org.slf4j.Logger;

public class RealmsNotification {
   private static final Logger LOGGER = LogUtils.getLogger();
   private static final String NOTIFICATION_UUID = "notificationUuid";
   private static final String DISMISSABLE = "dismissable";
   private static final String SEEN = "seen";
   private static final String TYPE = "type";
   private static final String VISIT_URL = "visitUrl";
   final UUID uuid;
   final boolean dismissable;
   final boolean seen;
   final String type;

   RealmsNotification(UUID uuid, boolean flag, boolean flag1, String s) {
      this.uuid = uuid;
      this.dismissable = flag;
      this.seen = flag1;
      this.type = s;
   }

   public boolean seen() {
      return this.seen;
   }

   public boolean dismissable() {
      return this.dismissable;
   }

   public UUID uuid() {
      return this.uuid;
   }

   public static List<RealmsNotification> parseList(String s) {
      List<RealmsNotification> list = new ArrayList<>();

      try {
         for(JsonElement jsonelement : JsonParser.parseString(s).getAsJsonObject().get("notifications").getAsJsonArray()) {
            list.add(parse(jsonelement.getAsJsonObject()));
         }
      } catch (Exception var5) {
         LOGGER.error("Could not parse list of RealmsNotifications", (Throwable)var5);
      }

      return list;
   }

   private static RealmsNotification parse(JsonObject jsonobject) {
      UUID uuid = JsonUtils.getUuidOr("notificationUuid", jsonobject, (UUID)null);
      if (uuid == null) {
         throw new IllegalStateException("Missing required property notificationUuid");
      } else {
         boolean flag = JsonUtils.getBooleanOr("dismissable", jsonobject, true);
         boolean flag1 = JsonUtils.getBooleanOr("seen", jsonobject, false);
         String s = JsonUtils.getRequiredString("type", jsonobject);
         RealmsNotification realmsnotification = new RealmsNotification(uuid, flag, flag1, s);
         return (RealmsNotification)("visitUrl".equals(s) ? RealmsNotification.VisitUrl.parse(realmsnotification, jsonobject) : realmsnotification);
      }
   }

   public static class VisitUrl extends RealmsNotification {
      private static final String URL = "url";
      private static final String BUTTON_TEXT = "buttonText";
      private static final String MESSAGE = "message";
      private final String url;
      private final RealmsText buttonText;
      private final RealmsText message;

      private VisitUrl(RealmsNotification realmsnotification, String s, RealmsText realmstext, RealmsText realmstext1) {
         super(realmsnotification.uuid, realmsnotification.dismissable, realmsnotification.seen, realmsnotification.type);
         this.url = s;
         this.buttonText = realmstext;
         this.message = realmstext1;
      }

      public static RealmsNotification.VisitUrl parse(RealmsNotification realmsnotification, JsonObject jsonobject) {
         String s = JsonUtils.getRequiredString("url", jsonobject);
         RealmsText realmstext = JsonUtils.getRequired("buttonText", jsonobject, RealmsText::parse);
         RealmsText realmstext1 = JsonUtils.getRequired("message", jsonobject, RealmsText::parse);
         return new RealmsNotification.VisitUrl(realmsnotification, s, realmstext, realmstext1);
      }

      public Component getMessage() {
         return this.message.createComponent(Component.translatable("mco.notification.visitUrl.message.default"));
      }

      public Button buildOpenLinkButton(Screen screen) {
         Component component = this.buttonText.createComponent(Component.translatable("mco.notification.visitUrl.buttonText.default"));
         return Button.builder(component, ConfirmLinkScreen.confirmLink(this.url, screen, true)).build();
      }
   }
}
