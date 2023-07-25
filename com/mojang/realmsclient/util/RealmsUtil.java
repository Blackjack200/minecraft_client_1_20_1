package com.mojang.realmsclient.util;

import com.google.common.cache.CacheBuilder;
import com.google.common.cache.CacheLoader;
import com.google.common.cache.LoadingCache;
import com.mojang.authlib.GameProfile;
import com.mojang.authlib.minecraft.MinecraftSessionService;
import com.mojang.util.UUIDTypeAdapter;
import java.util.Date;
import java.util.concurrent.TimeUnit;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.PlayerFaceRenderer;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;

public class RealmsUtil {
   static final MinecraftSessionService SESSION_SERVICE = Minecraft.getInstance().getMinecraftSessionService();
   private static final Component RIGHT_NOW = Component.translatable("mco.util.time.now");
   private static final LoadingCache<String, GameProfile> GAME_PROFILE_CACHE = CacheBuilder.newBuilder().expireAfterWrite(60L, TimeUnit.MINUTES).build(new CacheLoader<String, GameProfile>() {
      public GameProfile load(String s) {
         return RealmsUtil.SESSION_SERVICE.fillProfileProperties(new GameProfile(UUIDTypeAdapter.fromString(s), (String)null), false);
      }
   });
   private static final int MINUTES = 60;
   private static final int HOURS = 3600;
   private static final int DAYS = 86400;

   public static String uuidToName(String s) {
      return GAME_PROFILE_CACHE.getUnchecked(s).getName();
   }

   public static GameProfile getGameProfile(String s) {
      return GAME_PROFILE_CACHE.getUnchecked(s);
   }

   public static Component convertToAgePresentation(long i) {
      if (i < 0L) {
         return RIGHT_NOW;
      } else {
         long j = i / 1000L;
         if (j < 60L) {
            return Component.translatable("mco.time.secondsAgo", j);
         } else if (j < 3600L) {
            long k = j / 60L;
            return Component.translatable("mco.time.minutesAgo", k);
         } else if (j < 86400L) {
            long l = j / 3600L;
            return Component.translatable("mco.time.hoursAgo", l);
         } else {
            long i1 = j / 86400L;
            return Component.translatable("mco.time.daysAgo", i1);
         }
      }
   }

   public static Component convertToAgePresentationFromInstant(Date date) {
      return convertToAgePresentation(System.currentTimeMillis() - date.getTime());
   }

   public static void renderPlayerFace(GuiGraphics guigraphics, int i, int j, int k, String s) {
      GameProfile gameprofile = getGameProfile(s);
      ResourceLocation resourcelocation = Minecraft.getInstance().getSkinManager().getInsecureSkinLocation(gameprofile);
      PlayerFaceRenderer.draw(guigraphics, resourcelocation, i, j, k);
   }
}
