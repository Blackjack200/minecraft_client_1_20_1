package net.minecraft.client.gui.screens;

import com.mojang.authlib.minecraft.BanDetails;
import it.unimi.dsi.fastutil.booleans.BooleanConsumer;
import java.time.Duration;
import java.time.Instant;
import net.minecraft.ChatFormatting;
import net.minecraft.client.multiplayer.chat.report.BanReason;
import net.minecraft.network.chat.CommonComponents;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.ComponentUtils;
import net.minecraft.network.chat.Style;
import org.apache.commons.lang3.StringUtils;

public class BanNoticeScreen {
   private static final Component TEMPORARY_BAN_TITLE = Component.translatable("gui.banned.title.temporary").withStyle(ChatFormatting.BOLD);
   private static final Component PERMANENT_BAN_TITLE = Component.translatable("gui.banned.title.permanent").withStyle(ChatFormatting.BOLD);

   public static ConfirmLinkScreen create(BooleanConsumer booleanconsumer, BanDetails bandetails) {
      return new ConfirmLinkScreen(booleanconsumer, getBannedTitle(bandetails), getBannedScreenText(bandetails), "https://aka.ms/mcjavamoderation", CommonComponents.GUI_ACKNOWLEDGE, true);
   }

   private static Component getBannedTitle(BanDetails bandetails) {
      return isTemporaryBan(bandetails) ? TEMPORARY_BAN_TITLE : PERMANENT_BAN_TITLE;
   }

   private static Component getBannedScreenText(BanDetails bandetails) {
      return Component.translatable("gui.banned.description", getBanReasonText(bandetails), getBanStatusText(bandetails), Component.literal("https://aka.ms/mcjavamoderation"));
   }

   private static Component getBanReasonText(BanDetails bandetails) {
      String s = bandetails.reason();
      String s1 = bandetails.reasonMessage();
      if (StringUtils.isNumeric(s)) {
         int i = Integer.parseInt(s);
         BanReason banreason = BanReason.byId(i);
         Component component;
         if (banreason != null) {
            component = ComponentUtils.mergeStyles(banreason.title().copy(), Style.EMPTY.withBold(true));
         } else if (s1 != null) {
            component = Component.translatable("gui.banned.description.reason_id_message", i, s1).withStyle(ChatFormatting.BOLD);
         } else {
            component = Component.translatable("gui.banned.description.reason_id", i).withStyle(ChatFormatting.BOLD);
         }

         return Component.translatable("gui.banned.description.reason", component);
      } else {
         return Component.translatable("gui.banned.description.unknownreason");
      }
   }

   private static Component getBanStatusText(BanDetails bandetails) {
      if (isTemporaryBan(bandetails)) {
         Component component = getBanDurationText(bandetails);
         return Component.translatable("gui.banned.description.temporary", Component.translatable("gui.banned.description.temporary.duration", component).withStyle(ChatFormatting.BOLD));
      } else {
         return Component.translatable("gui.banned.description.permanent").withStyle(ChatFormatting.BOLD);
      }
   }

   private static Component getBanDurationText(BanDetails bandetails) {
      Duration duration = Duration.between(Instant.now(), bandetails.expires());
      long i = duration.toHours();
      if (i > 72L) {
         return CommonComponents.days(duration.toDays());
      } else {
         return i < 1L ? CommonComponents.minutes(duration.toMinutes()) : CommonComponents.hours(duration.toHours());
      }
   }

   private static boolean isTemporaryBan(BanDetails bandetails) {
      return bandetails.expires() != null;
   }
}
