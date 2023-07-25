package net.minecraft.client.gui.components;

import com.mojang.authlib.GameProfile;
import com.mojang.blaze3d.systems.RenderSystem;
import it.unimi.dsi.fastutil.objects.Object2ObjectOpenHashMap;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;
import javax.annotation.Nullable;
import net.minecraft.ChatFormatting;
import net.minecraft.Optionull;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Gui;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.multiplayer.PlayerInfo;
import net.minecraft.client.renderer.entity.LivingEntityRenderer;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.ComponentUtils;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.FormattedCharSequence;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.player.PlayerModelPart;
import net.minecraft.world.level.GameType;
import net.minecraft.world.scores.Objective;
import net.minecraft.world.scores.PlayerTeam;
import net.minecraft.world.scores.Scoreboard;
import net.minecraft.world.scores.criteria.ObjectiveCriteria;

public class PlayerTabOverlay {
   private static final Comparator<PlayerInfo> PLAYER_COMPARATOR = Comparator.comparingInt((playerinfo) -> playerinfo.getGameMode() == GameType.SPECTATOR ? 1 : 0).thenComparing((playerinfo) -> Optionull.mapOrDefault(playerinfo.getTeam(), PlayerTeam::getName, "")).thenComparing((playerinfo) -> playerinfo.getProfile().getName(), String::compareToIgnoreCase);
   private static final ResourceLocation GUI_ICONS_LOCATION = new ResourceLocation("textures/gui/icons.png");
   public static final int MAX_ROWS_PER_COL = 20;
   public static final int HEART_EMPTY_CONTAINER = 16;
   public static final int HEART_EMPTY_CONTAINER_BLINKING = 25;
   public static final int HEART_FULL = 52;
   public static final int HEART_HALF_FULL = 61;
   public static final int HEART_GOLDEN_FULL = 160;
   public static final int HEART_GOLDEN_HALF_FULL = 169;
   public static final int HEART_GHOST_FULL = 70;
   public static final int HEART_GHOST_HALF_FULL = 79;
   private final Minecraft minecraft;
   private final Gui gui;
   @Nullable
   private Component footer;
   @Nullable
   private Component header;
   private boolean visible;
   private final Map<UUID, PlayerTabOverlay.HealthState> healthStates = new Object2ObjectOpenHashMap<>();

   public PlayerTabOverlay(Minecraft minecraft, Gui gui) {
      this.minecraft = minecraft;
      this.gui = gui;
   }

   public Component getNameForDisplay(PlayerInfo playerinfo) {
      return playerinfo.getTabListDisplayName() != null ? this.decorateName(playerinfo, playerinfo.getTabListDisplayName().copy()) : this.decorateName(playerinfo, PlayerTeam.formatNameForTeam(playerinfo.getTeam(), Component.literal(playerinfo.getProfile().getName())));
   }

   private Component decorateName(PlayerInfo playerinfo, MutableComponent mutablecomponent) {
      return playerinfo.getGameMode() == GameType.SPECTATOR ? mutablecomponent.withStyle(ChatFormatting.ITALIC) : mutablecomponent;
   }

   public void setVisible(boolean flag) {
      if (this.visible != flag) {
         this.healthStates.clear();
         this.visible = flag;
         if (flag) {
            Component component = ComponentUtils.formatList(this.getPlayerInfos(), Component.literal(", "), this::getNameForDisplay);
            this.minecraft.getNarrator().sayNow(Component.translatable("multiplayer.player.list.narration", component));
         }
      }

   }

   private List<PlayerInfo> getPlayerInfos() {
      return this.minecraft.player.connection.getListedOnlinePlayers().stream().sorted(PLAYER_COMPARATOR).limit(80L).toList();
   }

   public void render(GuiGraphics guigraphics, int i, Scoreboard scoreboard, @Nullable Objective objective) {
      List<PlayerInfo> list = this.getPlayerInfos();
      int j = 0;
      int k = 0;

      for(PlayerInfo playerinfo : list) {
         int l = this.minecraft.font.width(this.getNameForDisplay(playerinfo));
         j = Math.max(j, l);
         if (objective != null && objective.getRenderType() != ObjectiveCriteria.RenderType.HEARTS) {
            l = this.minecraft.font.width(" " + scoreboard.getOrCreatePlayerScore(playerinfo.getProfile().getName(), objective).getScore());
            k = Math.max(k, l);
         }
      }

      if (!this.healthStates.isEmpty()) {
         Set<UUID> set = list.stream().map((playerinfo2) -> playerinfo2.getProfile().getId()).collect(Collectors.toSet());
         this.healthStates.keySet().removeIf((uuid) -> !set.contains(uuid));
      }

      int i1 = list.size();
      int j1 = i1;

      int k1;
      for(k1 = 1; j1 > 20; j1 = (i1 + k1 - 1) / k1) {
         ++k1;
      }

      boolean flag = this.minecraft.isLocalServer() || this.minecraft.getConnection().getConnection().isEncrypted();
      int l1;
      if (objective != null) {
         if (objective.getRenderType() == ObjectiveCriteria.RenderType.HEARTS) {
            l1 = 90;
         } else {
            l1 = k;
         }
      } else {
         l1 = 0;
      }

      int k2 = Math.min(k1 * ((flag ? 9 : 0) + j + l1 + 13), i - 50) / k1;
      int l2 = i / 2 - (k2 * k1 + (k1 - 1) * 5) / 2;
      int i3 = 10;
      int j3 = k2 * k1 + (k1 - 1) * 5;
      List<FormattedCharSequence> list1 = null;
      if (this.header != null) {
         list1 = this.minecraft.font.split(this.header, i - 50);

         for(FormattedCharSequence formattedcharsequence : list1) {
            j3 = Math.max(j3, this.minecraft.font.width(formattedcharsequence));
         }
      }

      List<FormattedCharSequence> list2 = null;
      if (this.footer != null) {
         list2 = this.minecraft.font.split(this.footer, i - 50);

         for(FormattedCharSequence formattedcharsequence1 : list2) {
            j3 = Math.max(j3, this.minecraft.font.width(formattedcharsequence1));
         }
      }

      if (list1 != null) {
         guigraphics.fill(i / 2 - j3 / 2 - 1, i3 - 1, i / 2 + j3 / 2 + 1, i3 + list1.size() * 9, Integer.MIN_VALUE);

         for(FormattedCharSequence formattedcharsequence2 : list1) {
            int k3 = this.minecraft.font.width(formattedcharsequence2);
            guigraphics.drawString(this.minecraft.font, formattedcharsequence2, i / 2 - k3 / 2, i3, -1);
            i3 += 9;
         }

         ++i3;
      }

      guigraphics.fill(i / 2 - j3 / 2 - 1, i3 - 1, i / 2 + j3 / 2 + 1, i3 + j1 * 9, Integer.MIN_VALUE);
      int l3 = this.minecraft.options.getBackgroundColor(553648127);

      for(int i4 = 0; i4 < i1; ++i4) {
         int j4 = i4 / j1;
         int k4 = i4 % j1;
         int l4 = l2 + j4 * k2 + j4 * 5;
         int i5 = i3 + k4 * 9;
         guigraphics.fill(l4, i5, l4 + k2, i5 + 8, l3);
         RenderSystem.enableBlend();
         if (i4 < list.size()) {
            PlayerInfo playerinfo1 = list.get(i4);
            GameProfile gameprofile = playerinfo1.getProfile();
            if (flag) {
               Player player = this.minecraft.level.getPlayerByUUID(gameprofile.getId());
               boolean flag1 = player != null && LivingEntityRenderer.isEntityUpsideDown(player);
               boolean flag2 = player != null && player.isModelPartShown(PlayerModelPart.HAT);
               PlayerFaceRenderer.draw(guigraphics, playerinfo1.getSkinLocation(), l4, i5, 8, flag2, flag1);
               l4 += 9;
            }

            guigraphics.drawString(this.minecraft.font, this.getNameForDisplay(playerinfo1), l4, i5, playerinfo1.getGameMode() == GameType.SPECTATOR ? -1862270977 : -1);
            if (objective != null && playerinfo1.getGameMode() != GameType.SPECTATOR) {
               int j5 = l4 + j + 1;
               int k5 = j5 + l1;
               if (k5 - j5 > 5) {
                  this.renderTablistScore(objective, i5, gameprofile.getName(), j5, k5, gameprofile.getId(), guigraphics);
               }
            }

            this.renderPingIcon(guigraphics, k2, l4 - (flag ? 9 : 0), i5, playerinfo1);
         }
      }

      if (list2 != null) {
         i3 += j1 * 9 + 1;
         guigraphics.fill(i / 2 - j3 / 2 - 1, i3 - 1, i / 2 + j3 / 2 + 1, i3 + list2.size() * 9, Integer.MIN_VALUE);

         for(FormattedCharSequence formattedcharsequence3 : list2) {
            int l5 = this.minecraft.font.width(formattedcharsequence3);
            guigraphics.drawString(this.minecraft.font, formattedcharsequence3, i / 2 - l5 / 2, i3, -1);
            i3 += 9;
         }
      }

   }

   protected void renderPingIcon(GuiGraphics guigraphics, int i, int j, int k, PlayerInfo playerinfo) {
      int l = 0;
      int i1;
      if (playerinfo.getLatency() < 0) {
         i1 = 5;
      } else if (playerinfo.getLatency() < 150) {
         i1 = 0;
      } else if (playerinfo.getLatency() < 300) {
         i1 = 1;
      } else if (playerinfo.getLatency() < 600) {
         i1 = 2;
      } else if (playerinfo.getLatency() < 1000) {
         i1 = 3;
      } else {
         i1 = 4;
      }

      guigraphics.pose().pushPose();
      guigraphics.pose().translate(0.0F, 0.0F, 100.0F);
      guigraphics.blit(GUI_ICONS_LOCATION, j + i - 11, k, 0, 176 + i1 * 8, 10, 8);
      guigraphics.pose().popPose();
   }

   private void renderTablistScore(Objective objective, int i, String s, int j, int k, UUID uuid, GuiGraphics guigraphics) {
      int l = objective.getScoreboard().getOrCreatePlayerScore(s, objective).getScore();
      if (objective.getRenderType() == ObjectiveCriteria.RenderType.HEARTS) {
         this.renderTablistHearts(i, j, k, uuid, guigraphics, l);
      } else {
         String s1 = "" + ChatFormatting.YELLOW + l;
         guigraphics.drawString(this.minecraft.font, s1, k - this.minecraft.font.width(s1), i, 16777215);
      }
   }

   private void renderTablistHearts(int i, int j, int k, UUID uuid, GuiGraphics guigraphics, int l) {
      PlayerTabOverlay.HealthState playertaboverlay_healthstate = this.healthStates.computeIfAbsent(uuid, (uuid1) -> new PlayerTabOverlay.HealthState(l));
      playertaboverlay_healthstate.update(l, (long)this.gui.getGuiTicks());
      int i1 = Mth.positiveCeilDiv(Math.max(l, playertaboverlay_healthstate.displayedValue()), 2);
      int j1 = Math.max(l, Math.max(playertaboverlay_healthstate.displayedValue(), 20)) / 2;
      boolean flag = playertaboverlay_healthstate.isBlinking((long)this.gui.getGuiTicks());
      if (i1 > 0) {
         int k1 = Mth.floor(Math.min((float)(k - j - 4) / (float)j1, 9.0F));
         if (k1 <= 3) {
            float f = Mth.clamp((float)l / 20.0F, 0.0F, 1.0F);
            int l1 = (int)((1.0F - f) * 255.0F) << 16 | (int)(f * 255.0F) << 8;
            String s = "" + (float)l / 2.0F;
            if (k - this.minecraft.font.width(s + "hp") >= j) {
               s = s + "hp";
            }

            guigraphics.drawString(this.minecraft.font, s, (k + j - this.minecraft.font.width(s)) / 2, i, l1);
         } else {
            for(int i2 = i1; i2 < j1; ++i2) {
               guigraphics.blit(GUI_ICONS_LOCATION, j + i2 * k1, i, flag ? 25 : 16, 0, 9, 9);
            }

            for(int j2 = 0; j2 < i1; ++j2) {
               guigraphics.blit(GUI_ICONS_LOCATION, j + j2 * k1, i, flag ? 25 : 16, 0, 9, 9);
               if (flag) {
                  if (j2 * 2 + 1 < playertaboverlay_healthstate.displayedValue()) {
                     guigraphics.blit(GUI_ICONS_LOCATION, j + j2 * k1, i, 70, 0, 9, 9);
                  }

                  if (j2 * 2 + 1 == playertaboverlay_healthstate.displayedValue()) {
                     guigraphics.blit(GUI_ICONS_LOCATION, j + j2 * k1, i, 79, 0, 9, 9);
                  }
               }

               if (j2 * 2 + 1 < l) {
                  guigraphics.blit(GUI_ICONS_LOCATION, j + j2 * k1, i, j2 >= 10 ? 160 : 52, 0, 9, 9);
               }

               if (j2 * 2 + 1 == l) {
                  guigraphics.blit(GUI_ICONS_LOCATION, j + j2 * k1, i, j2 >= 10 ? 169 : 61, 0, 9, 9);
               }
            }

         }
      }
   }

   public void setFooter(@Nullable Component component) {
      this.footer = component;
   }

   public void setHeader(@Nullable Component component) {
      this.header = component;
   }

   public void reset() {
      this.header = null;
      this.footer = null;
   }

   static class HealthState {
      private static final long DISPLAY_UPDATE_DELAY = 20L;
      private static final long DECREASE_BLINK_DURATION = 20L;
      private static final long INCREASE_BLINK_DURATION = 10L;
      private int lastValue;
      private int displayedValue;
      private long lastUpdateTick;
      private long blinkUntilTick;

      public HealthState(int i) {
         this.displayedValue = i;
         this.lastValue = i;
      }

      public void update(int i, long j) {
         if (i != this.lastValue) {
            long k = i < this.lastValue ? 20L : 10L;
            this.blinkUntilTick = j + k;
            this.lastValue = i;
            this.lastUpdateTick = j;
         }

         if (j - this.lastUpdateTick > 20L) {
            this.displayedValue = i;
         }

      }

      public int displayedValue() {
         return this.displayedValue;
      }

      public boolean isBlinking(long i) {
         return this.blinkUntilTick > i && (this.blinkUntilTick - i) % 6L >= 3L;
      }
   }
}
