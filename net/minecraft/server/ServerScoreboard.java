package net.minecraft.server;

import com.google.common.collect.Lists;
import com.google.common.collect.Sets;
import java.util.List;
import java.util.Set;
import javax.annotation.Nullable;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.game.ClientboundSetDisplayObjectivePacket;
import net.minecraft.network.protocol.game.ClientboundSetObjectivePacket;
import net.minecraft.network.protocol.game.ClientboundSetPlayerTeamPacket;
import net.minecraft.network.protocol.game.ClientboundSetScorePacket;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.scores.Objective;
import net.minecraft.world.scores.PlayerTeam;
import net.minecraft.world.scores.Score;
import net.minecraft.world.scores.Scoreboard;
import net.minecraft.world.scores.ScoreboardSaveData;

public class ServerScoreboard extends Scoreboard {
   private final MinecraftServer server;
   private final Set<Objective> trackedObjectives = Sets.newHashSet();
   private final List<Runnable> dirtyListeners = Lists.newArrayList();

   public ServerScoreboard(MinecraftServer minecraftserver) {
      this.server = minecraftserver;
   }

   public void onScoreChanged(Score score) {
      super.onScoreChanged(score);
      if (this.trackedObjectives.contains(score.getObjective())) {
         this.server.getPlayerList().broadcastAll(new ClientboundSetScorePacket(ServerScoreboard.Method.CHANGE, score.getObjective().getName(), score.getOwner(), score.getScore()));
      }

      this.setDirty();
   }

   public void onPlayerRemoved(String s) {
      super.onPlayerRemoved(s);
      this.server.getPlayerList().broadcastAll(new ClientboundSetScorePacket(ServerScoreboard.Method.REMOVE, (String)null, s, 0));
      this.setDirty();
   }

   public void onPlayerScoreRemoved(String s, Objective objective) {
      super.onPlayerScoreRemoved(s, objective);
      if (this.trackedObjectives.contains(objective)) {
         this.server.getPlayerList().broadcastAll(new ClientboundSetScorePacket(ServerScoreboard.Method.REMOVE, objective.getName(), s, 0));
      }

      this.setDirty();
   }

   public void setDisplayObjective(int i, @Nullable Objective objective) {
      Objective objective1 = this.getDisplayObjective(i);
      super.setDisplayObjective(i, objective);
      if (objective1 != objective && objective1 != null) {
         if (this.getObjectiveDisplaySlotCount(objective1) > 0) {
            this.server.getPlayerList().broadcastAll(new ClientboundSetDisplayObjectivePacket(i, objective));
         } else {
            this.stopTrackingObjective(objective1);
         }
      }

      if (objective != null) {
         if (this.trackedObjectives.contains(objective)) {
            this.server.getPlayerList().broadcastAll(new ClientboundSetDisplayObjectivePacket(i, objective));
         } else {
            this.startTrackingObjective(objective);
         }
      }

      this.setDirty();
   }

   public boolean addPlayerToTeam(String s, PlayerTeam playerteam) {
      if (super.addPlayerToTeam(s, playerteam)) {
         this.server.getPlayerList().broadcastAll(ClientboundSetPlayerTeamPacket.createPlayerPacket(playerteam, s, ClientboundSetPlayerTeamPacket.Action.ADD));
         this.setDirty();
         return true;
      } else {
         return false;
      }
   }

   public void removePlayerFromTeam(String s, PlayerTeam playerteam) {
      super.removePlayerFromTeam(s, playerteam);
      this.server.getPlayerList().broadcastAll(ClientboundSetPlayerTeamPacket.createPlayerPacket(playerteam, s, ClientboundSetPlayerTeamPacket.Action.REMOVE));
      this.setDirty();
   }

   public void onObjectiveAdded(Objective objective) {
      super.onObjectiveAdded(objective);
      this.setDirty();
   }

   public void onObjectiveChanged(Objective objective) {
      super.onObjectiveChanged(objective);
      if (this.trackedObjectives.contains(objective)) {
         this.server.getPlayerList().broadcastAll(new ClientboundSetObjectivePacket(objective, 2));
      }

      this.setDirty();
   }

   public void onObjectiveRemoved(Objective objective) {
      super.onObjectiveRemoved(objective);
      if (this.trackedObjectives.contains(objective)) {
         this.stopTrackingObjective(objective);
      }

      this.setDirty();
   }

   public void onTeamAdded(PlayerTeam playerteam) {
      super.onTeamAdded(playerteam);
      this.server.getPlayerList().broadcastAll(ClientboundSetPlayerTeamPacket.createAddOrModifyPacket(playerteam, true));
      this.setDirty();
   }

   public void onTeamChanged(PlayerTeam playerteam) {
      super.onTeamChanged(playerteam);
      this.server.getPlayerList().broadcastAll(ClientboundSetPlayerTeamPacket.createAddOrModifyPacket(playerteam, false));
      this.setDirty();
   }

   public void onTeamRemoved(PlayerTeam playerteam) {
      super.onTeamRemoved(playerteam);
      this.server.getPlayerList().broadcastAll(ClientboundSetPlayerTeamPacket.createRemovePacket(playerteam));
      this.setDirty();
   }

   public void addDirtyListener(Runnable runnable) {
      this.dirtyListeners.add(runnable);
   }

   protected void setDirty() {
      for(Runnable runnable : this.dirtyListeners) {
         runnable.run();
      }

   }

   public List<Packet<?>> getStartTrackingPackets(Objective objective) {
      List<Packet<?>> list = Lists.newArrayList();
      list.add(new ClientboundSetObjectivePacket(objective, 0));

      for(int i = 0; i < 19; ++i) {
         if (this.getDisplayObjective(i) == objective) {
            list.add(new ClientboundSetDisplayObjectivePacket(i, objective));
         }
      }

      for(Score score : this.getPlayerScores(objective)) {
         list.add(new ClientboundSetScorePacket(ServerScoreboard.Method.CHANGE, score.getObjective().getName(), score.getOwner(), score.getScore()));
      }

      return list;
   }

   public void startTrackingObjective(Objective objective) {
      List<Packet<?>> list = this.getStartTrackingPackets(objective);

      for(ServerPlayer serverplayer : this.server.getPlayerList().getPlayers()) {
         for(Packet<?> packet : list) {
            serverplayer.connection.send(packet);
         }
      }

      this.trackedObjectives.add(objective);
   }

   public List<Packet<?>> getStopTrackingPackets(Objective objective) {
      List<Packet<?>> list = Lists.newArrayList();
      list.add(new ClientboundSetObjectivePacket(objective, 1));

      for(int i = 0; i < 19; ++i) {
         if (this.getDisplayObjective(i) == objective) {
            list.add(new ClientboundSetDisplayObjectivePacket(i, objective));
         }
      }

      return list;
   }

   public void stopTrackingObjective(Objective objective) {
      List<Packet<?>> list = this.getStopTrackingPackets(objective);

      for(ServerPlayer serverplayer : this.server.getPlayerList().getPlayers()) {
         for(Packet<?> packet : list) {
            serverplayer.connection.send(packet);
         }
      }

      this.trackedObjectives.remove(objective);
   }

   public int getObjectiveDisplaySlotCount(Objective objective) {
      int i = 0;

      for(int j = 0; j < 19; ++j) {
         if (this.getDisplayObjective(j) == objective) {
            ++i;
         }
      }

      return i;
   }

   public ScoreboardSaveData createData() {
      ScoreboardSaveData scoreboardsavedata = new ScoreboardSaveData(this);
      this.addDirtyListener(scoreboardsavedata::setDirty);
      return scoreboardsavedata;
   }

   public ScoreboardSaveData createData(CompoundTag compoundtag) {
      return this.createData().load(compoundtag);
   }

   public static enum Method {
      CHANGE,
      REMOVE;
   }
}
