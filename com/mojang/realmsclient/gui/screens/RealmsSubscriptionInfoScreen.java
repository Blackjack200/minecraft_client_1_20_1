package com.mojang.realmsclient.gui.screens;

import com.mojang.logging.LogUtils;
import com.mojang.realmsclient.client.RealmsClient;
import com.mojang.realmsclient.dto.RealmsServer;
import com.mojang.realmsclient.dto.Subscription;
import com.mojang.realmsclient.exception.RealmsServiceException;
import java.text.DateFormat;
import java.util.Calendar;
import java.util.GregorianCalendar;
import java.util.TimeZone;
import javax.annotation.Nullable;
import net.minecraft.Util;
import net.minecraft.client.GameNarrator;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.FittingMultiLineTextWidget;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.CommonComponents;
import net.minecraft.network.chat.Component;
import net.minecraft.realms.RealmsScreen;
import net.minecraft.util.CommonLinks;
import org.slf4j.Logger;

public class RealmsSubscriptionInfoScreen extends RealmsScreen {
   static final Logger LOGGER = LogUtils.getLogger();
   private static final Component SUBSCRIPTION_TITLE = Component.translatable("mco.configure.world.subscription.title");
   private static final Component SUBSCRIPTION_START_LABEL = Component.translatable("mco.configure.world.subscription.start");
   private static final Component TIME_LEFT_LABEL = Component.translatable("mco.configure.world.subscription.timeleft");
   private static final Component DAYS_LEFT_LABEL = Component.translatable("mco.configure.world.subscription.recurring.daysleft");
   private static final Component SUBSCRIPTION_EXPIRED_TEXT = Component.translatable("mco.configure.world.subscription.expired");
   private static final Component SUBSCRIPTION_LESS_THAN_A_DAY_TEXT = Component.translatable("mco.configure.world.subscription.less_than_a_day");
   private static final Component UNKNOWN = Component.translatable("mco.configure.world.subscription.unknown");
   private static final Component RECURRING_INFO = Component.translatable("mco.configure.world.subscription.recurring.info");
   private final Screen lastScreen;
   final RealmsServer serverData;
   final Screen mainScreen;
   private Component daysLeft = UNKNOWN;
   private Component startDate = UNKNOWN;
   @Nullable
   private Subscription.SubscriptionType type;

   public RealmsSubscriptionInfoScreen(Screen screen, RealmsServer realmsserver, Screen screen1) {
      super(GameNarrator.NO_TITLE);
      this.lastScreen = screen;
      this.serverData = realmsserver;
      this.mainScreen = screen1;
   }

   public void init() {
      this.getSubscription(this.serverData.id);
      this.addRenderableWidget(Button.builder(Component.translatable("mco.configure.world.subscription.extend"), (button2) -> {
         String s = CommonLinks.extendRealms(this.serverData.remoteSubscriptionId, this.minecraft.getUser().getUuid());
         this.minecraft.keyboardHandler.setClipboard(s);
         Util.getPlatform().openUri(s);
      }).bounds(this.width / 2 - 100, row(6), 200, 20).build());
      if (this.serverData.expired) {
         this.addRenderableWidget(Button.builder(Component.translatable("mco.configure.world.delete.button"), (button1) -> {
            Component component = Component.translatable("mco.configure.world.delete.question.line1");
            Component component1 = Component.translatable("mco.configure.world.delete.question.line2");
            this.minecraft.setScreen(new RealmsLongConfirmationScreen(this::deleteRealm, RealmsLongConfirmationScreen.Type.WARNING, component, component1, true));
         }).bounds(this.width / 2 - 100, row(10), 200, 20).build());
      } else {
         this.addRenderableWidget((new FittingMultiLineTextWidget(this.width / 2 - 100, row(8), 200, 46, RECURRING_INFO, this.font)).setColor(10526880));
      }

      this.addRenderableWidget(Button.builder(CommonComponents.GUI_BACK, (button) -> this.minecraft.setScreen(this.lastScreen)).bounds(this.width / 2 - 100, row(12), 200, 20).build());
   }

   public Component getNarrationMessage() {
      return CommonComponents.joinLines(SUBSCRIPTION_TITLE, SUBSCRIPTION_START_LABEL, this.startDate, TIME_LEFT_LABEL, this.daysLeft);
   }

   private void deleteRealm(boolean flag) {
      if (flag) {
         (new Thread("Realms-delete-realm") {
            public void run() {
               try {
                  RealmsClient realmsclient = RealmsClient.create();
                  realmsclient.deleteWorld(RealmsSubscriptionInfoScreen.this.serverData.id);
               } catch (RealmsServiceException var2) {
                  RealmsSubscriptionInfoScreen.LOGGER.error("Couldn't delete world", (Throwable)var2);
               }

               RealmsSubscriptionInfoScreen.this.minecraft.execute(() -> RealmsSubscriptionInfoScreen.this.minecraft.setScreen(RealmsSubscriptionInfoScreen.this.mainScreen));
            }
         }).start();
      }

      this.minecraft.setScreen(this);
   }

   private void getSubscription(long i) {
      RealmsClient realmsclient = RealmsClient.create();

      try {
         Subscription subscription = realmsclient.subscriptionFor(i);
         this.daysLeft = this.daysLeftPresentation(subscription.daysLeft);
         this.startDate = localPresentation(subscription.startDate);
         this.type = subscription.type;
      } catch (RealmsServiceException var5) {
         LOGGER.error("Couldn't get subscription");
         this.minecraft.setScreen(new RealmsGenericErrorScreen(var5, this.lastScreen));
      }

   }

   private static Component localPresentation(long i) {
      Calendar calendar = new GregorianCalendar(TimeZone.getDefault());
      calendar.setTimeInMillis(i);
      return Component.literal(DateFormat.getDateTimeInstance().format(calendar.getTime()));
   }

   public boolean keyPressed(int i, int j, int k) {
      if (i == 256) {
         this.minecraft.setScreen(this.lastScreen);
         return true;
      } else {
         return super.keyPressed(i, j, k);
      }
   }

   public void render(GuiGraphics guigraphics, int i, int j, float f) {
      this.renderBackground(guigraphics);
      int k = this.width / 2 - 100;
      guigraphics.drawCenteredString(this.font, SUBSCRIPTION_TITLE, this.width / 2, 17, 16777215);
      guigraphics.drawString(this.font, SUBSCRIPTION_START_LABEL, k, row(0), 10526880, false);
      guigraphics.drawString(this.font, this.startDate, k, row(1), 16777215, false);
      if (this.type == Subscription.SubscriptionType.NORMAL) {
         guigraphics.drawString(this.font, TIME_LEFT_LABEL, k, row(3), 10526880, false);
      } else if (this.type == Subscription.SubscriptionType.RECURRING) {
         guigraphics.drawString(this.font, DAYS_LEFT_LABEL, k, row(3), 10526880, false);
      }

      guigraphics.drawString(this.font, this.daysLeft, k, row(4), 16777215, false);
      super.render(guigraphics, i, j, f);
   }

   private Component daysLeftPresentation(int i) {
      if (i < 0 && this.serverData.expired) {
         return SUBSCRIPTION_EXPIRED_TEXT;
      } else if (i <= 1) {
         return SUBSCRIPTION_LESS_THAN_A_DAY_TEXT;
      } else {
         int j = i / 30;
         int k = i % 30;
         boolean flag = j > 0;
         boolean flag1 = k > 0;
         if (flag && flag1) {
            return Component.translatable("mco.configure.world.subscription.remaining.months.days", j, k);
         } else if (flag) {
            return Component.translatable("mco.configure.world.subscription.remaining.months", j);
         } else {
            return flag1 ? Component.translatable("mco.configure.world.subscription.remaining.days", k) : Component.empty();
         }
      }
   }
}
