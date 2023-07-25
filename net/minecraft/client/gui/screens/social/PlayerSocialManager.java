package net.minecraft.client.gui.screens.social;

import com.google.common.collect.Maps;
import com.google.common.collect.Sets;
import com.mojang.authlib.GameProfile;
import com.mojang.authlib.minecraft.UserApiService;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import net.minecraft.Util;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.multiplayer.PlayerInfo;

public class PlayerSocialManager {
   private final Minecraft minecraft;
   private final Set<UUID> hiddenPlayers = Sets.newHashSet();
   private final UserApiService service;
   private final Map<String, UUID> discoveredNamesToUUID = Maps.newHashMap();
   private boolean onlineMode;
   private CompletableFuture<?> pendingBlockListRefresh = CompletableFuture.completedFuture((Object)null);

   public PlayerSocialManager(Minecraft minecraft, UserApiService userapiservice) {
      this.minecraft = minecraft;
      this.service = userapiservice;
   }

   public void hidePlayer(UUID uuid) {
      this.hiddenPlayers.add(uuid);
   }

   public void showPlayer(UUID uuid) {
      this.hiddenPlayers.remove(uuid);
   }

   public boolean shouldHideMessageFrom(UUID uuid) {
      return this.isHidden(uuid) || this.isBlocked(uuid);
   }

   public boolean isHidden(UUID uuid) {
      return this.hiddenPlayers.contains(uuid);
   }

   public void startOnlineMode() {
      this.onlineMode = true;
      this.pendingBlockListRefresh = this.pendingBlockListRefresh.thenRunAsync(this.service::refreshBlockList, Util.ioPool());
   }

   public void stopOnlineMode() {
      this.onlineMode = false;
   }

   public boolean isBlocked(UUID uuid) {
      if (!this.onlineMode) {
         return false;
      } else {
         this.pendingBlockListRefresh.join();
         return this.service.isBlockedPlayer(uuid);
      }
   }

   public Set<UUID> getHiddenPlayers() {
      return this.hiddenPlayers;
   }

   public UUID getDiscoveredUUID(String s) {
      return this.discoveredNamesToUUID.getOrDefault(s, Util.NIL_UUID);
   }

   public void addPlayer(PlayerInfo playerinfo) {
      GameProfile gameprofile = playerinfo.getProfile();
      if (gameprofile.isComplete()) {
         this.discoveredNamesToUUID.put(gameprofile.getName(), gameprofile.getId());
      }

      Screen screen = this.minecraft.screen;
      if (screen instanceof SocialInteractionsScreen socialinteractionsscreen) {
         socialinteractionsscreen.onAddPlayer(playerinfo);
      }

   }

   public void removePlayer(UUID uuid) {
      Screen screen = this.minecraft.screen;
      if (screen instanceof SocialInteractionsScreen socialinteractionsscreen) {
         socialinteractionsscreen.onRemovePlayer(uuid);
      }

   }
}
