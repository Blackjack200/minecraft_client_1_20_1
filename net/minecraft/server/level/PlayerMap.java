package net.minecraft.server.level;

import it.unimi.dsi.fastutil.objects.Object2BooleanMap;
import it.unimi.dsi.fastutil.objects.Object2BooleanOpenHashMap;
import java.util.Set;

public final class PlayerMap {
   private final Object2BooleanMap<ServerPlayer> players = new Object2BooleanOpenHashMap<>();

   public Set<ServerPlayer> getPlayers(long i) {
      return this.players.keySet();
   }

   public void addPlayer(long i, ServerPlayer serverplayer, boolean flag) {
      this.players.put(serverplayer, flag);
   }

   public void removePlayer(long i, ServerPlayer serverplayer) {
      this.players.removeBoolean(serverplayer);
   }

   public void ignorePlayer(ServerPlayer serverplayer) {
      this.players.replace(serverplayer, true);
   }

   public void unIgnorePlayer(ServerPlayer serverplayer) {
      this.players.replace(serverplayer, false);
   }

   public boolean ignoredOrUnknown(ServerPlayer serverplayer) {
      return this.players.getOrDefault(serverplayer, true);
   }

   public boolean ignored(ServerPlayer serverplayer) {
      return this.players.getBoolean(serverplayer);
   }

   public void updatePlayer(long i, long j, ServerPlayer serverplayer) {
   }
}
