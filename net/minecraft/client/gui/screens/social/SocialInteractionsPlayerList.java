package net.minecraft.client.gui.screens.social;

import com.google.common.base.Strings;
import com.google.common.base.Suppliers;
import com.google.common.collect.Lists;
import com.mojang.authlib.GameProfile;
import it.unimi.dsi.fastutil.objects.ObjectLinkedOpenHashSet;
import java.util.Collection;
import java.util.Comparator;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import javax.annotation.Nullable;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.ContainerObjectSelectionList;
import net.minecraft.client.multiplayer.ClientPacketListener;
import net.minecraft.client.multiplayer.PlayerInfo;
import net.minecraft.client.multiplayer.chat.ChatLog;
import net.minecraft.client.multiplayer.chat.LoggedChatEvent;
import net.minecraft.client.multiplayer.chat.LoggedChatMessage;

public class SocialInteractionsPlayerList extends ContainerObjectSelectionList<PlayerEntry> {
   private final SocialInteractionsScreen socialInteractionsScreen;
   private final List<PlayerEntry> players = Lists.newArrayList();
   @Nullable
   private String filter;

   public SocialInteractionsPlayerList(SocialInteractionsScreen socialinteractionsscreen, Minecraft minecraft, int i, int j, int k, int l, int i1) {
      super(minecraft, i, j, k, l, i1);
      this.socialInteractionsScreen = socialinteractionsscreen;
      this.setRenderBackground(false);
      this.setRenderTopAndBottom(false);
   }

   protected void enableScissor(GuiGraphics guigraphics) {
      guigraphics.enableScissor(this.x0, this.y0 + 4, this.x1, this.y1);
   }

   public void updatePlayerList(Collection<UUID> collection, double d0, boolean flag) {
      Map<UUID, PlayerEntry> map = new HashMap<>();
      this.addOnlinePlayers(collection, map);
      this.updatePlayersFromChatLog(map, flag);
      this.updateFiltersAndScroll(map.values(), d0);
   }

   private void addOnlinePlayers(Collection<UUID> collection, Map<UUID, PlayerEntry> map) {
      ClientPacketListener clientpacketlistener = this.minecraft.player.connection;

      for(UUID uuid : collection) {
         PlayerInfo playerinfo = clientpacketlistener.getPlayerInfo(uuid);
         if (playerinfo != null) {
            boolean flag = playerinfo.hasVerifiableChat();
            map.put(uuid, new PlayerEntry(this.minecraft, this.socialInteractionsScreen, uuid, playerinfo.getProfile().getName(), playerinfo::getSkinLocation, flag));
         }
      }

   }

   private void updatePlayersFromChatLog(Map<UUID, PlayerEntry> map, boolean flag) {
      Collection<GameProfile> collection = collectProfilesFromChatLog(this.minecraft.getReportingContext().chatLog());
      Iterator var4 = collection.iterator();

      while(true) {
         PlayerEntry playerentry;
         do {
            if (!var4.hasNext()) {
               return;
            }

            GameProfile gameprofile = (GameProfile)var4.next();
            if (flag) {
               playerentry = map.computeIfAbsent(gameprofile.getId(), (uuid) -> {
                  PlayerEntry playerentry2 = new PlayerEntry(this.minecraft, this.socialInteractionsScreen, gameprofile.getId(), gameprofile.getName(), Suppliers.memoize(() -> this.minecraft.getSkinManager().getInsecureSkinLocation(gameprofile)), true);
                  playerentry2.setRemoved(true);
                  return playerentry2;
               });
               break;
            }

            playerentry = map.get(gameprofile.getId());
         } while(playerentry == null);

         playerentry.setHasRecentMessages(true);
      }
   }

   private static Collection<GameProfile> collectProfilesFromChatLog(ChatLog chatlog) {
      Set<GameProfile> set = new ObjectLinkedOpenHashSet<>();

      for(int i = chatlog.end(); i >= chatlog.start(); --i) {
         LoggedChatEvent loggedchatevent = chatlog.lookup(i);
         if (loggedchatevent instanceof LoggedChatMessage.Player loggedchatmessage_player) {
            if (loggedchatmessage_player.message().hasSignature()) {
               set.add(loggedchatmessage_player.profile());
            }
         }
      }

      return set;
   }

   private void sortPlayerEntries() {
      this.players.sort(Comparator.comparing((playerentry1) -> {
         if (playerentry1.getPlayerId().equals(this.minecraft.getUser().getProfileId())) {
            return 0;
         } else if (playerentry1.getPlayerId().version() == 2) {
            return 4;
         } else if (this.minecraft.getReportingContext().hasDraftReportFor(playerentry1.getPlayerId())) {
            return 1;
         } else {
            return playerentry1.hasRecentMessages() ? 2 : 3;
         }
      }).thenComparing((playerentry) -> {
         if (!playerentry.getPlayerName().isBlank()) {
            int i = playerentry.getPlayerName().codePointAt(0);
            if (i == 95 || i >= 97 && i <= 122 || i >= 65 && i <= 90 || i >= 48 && i <= 57) {
               return 0;
            }
         }

         return 1;
      }).thenComparing(PlayerEntry::getPlayerName, String::compareToIgnoreCase));
   }

   private void updateFiltersAndScroll(Collection<PlayerEntry> collection, double d0) {
      this.players.clear();
      this.players.addAll(collection);
      this.sortPlayerEntries();
      this.updateFilteredPlayers();
      this.replaceEntries(this.players);
      this.setScrollAmount(d0);
   }

   private void updateFilteredPlayers() {
      if (this.filter != null) {
         this.players.removeIf((playerentry) -> !playerentry.getPlayerName().toLowerCase(Locale.ROOT).contains(this.filter));
         this.replaceEntries(this.players);
      }

   }

   public void setFilter(String s) {
      this.filter = s;
   }

   public boolean isEmpty() {
      return this.players.isEmpty();
   }

   public void addPlayer(PlayerInfo playerinfo, SocialInteractionsScreen.Page socialinteractionsscreen_page) {
      UUID uuid = playerinfo.getProfile().getId();

      for(PlayerEntry playerentry : this.players) {
         if (playerentry.getPlayerId().equals(uuid)) {
            playerentry.setRemoved(false);
            return;
         }
      }

      if ((socialinteractionsscreen_page == SocialInteractionsScreen.Page.ALL || this.minecraft.getPlayerSocialManager().shouldHideMessageFrom(uuid)) && (Strings.isNullOrEmpty(this.filter) || playerinfo.getProfile().getName().toLowerCase(Locale.ROOT).contains(this.filter))) {
         boolean flag = playerinfo.hasVerifiableChat();
         PlayerEntry playerentry1 = new PlayerEntry(this.minecraft, this.socialInteractionsScreen, playerinfo.getProfile().getId(), playerinfo.getProfile().getName(), playerinfo::getSkinLocation, flag);
         this.addEntry(playerentry1);
         this.players.add(playerentry1);
      }

   }

   public void removePlayer(UUID uuid) {
      for(PlayerEntry playerentry : this.players) {
         if (playerentry.getPlayerId().equals(uuid)) {
            playerentry.setRemoved(true);
            return;
         }
      }

   }
}
