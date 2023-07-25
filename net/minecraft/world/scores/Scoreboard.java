package net.minecraft.world.scores;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.mojang.logging.LogUtils;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.function.Consumer;
import javax.annotation.Nullable;
import net.minecraft.ChatFormatting;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.scores.criteria.ObjectiveCriteria;
import org.slf4j.Logger;

public class Scoreboard {
   private static final Logger LOGGER = LogUtils.getLogger();
   public static final int DISPLAY_SLOT_LIST = 0;
   public static final int DISPLAY_SLOT_SIDEBAR = 1;
   public static final int DISPLAY_SLOT_BELOW_NAME = 2;
   public static final int DISPLAY_SLOT_TEAMS_SIDEBAR_START = 3;
   public static final int DISPLAY_SLOT_TEAMS_SIDEBAR_END = 18;
   public static final int DISPLAY_SLOTS = 19;
   private final Map<String, Objective> objectivesByName = Maps.newHashMap();
   private final Map<ObjectiveCriteria, List<Objective>> objectivesByCriteria = Maps.newHashMap();
   private final Map<String, Map<Objective, Score>> playerScores = Maps.newHashMap();
   private final Objective[] displayObjectives = new Objective[19];
   private final Map<String, PlayerTeam> teamsByName = Maps.newHashMap();
   private final Map<String, PlayerTeam> teamsByPlayer = Maps.newHashMap();
   @Nullable
   private static String[] displaySlotNames;

   public boolean hasObjective(String s) {
      return this.objectivesByName.containsKey(s);
   }

   public Objective getOrCreateObjective(String s) {
      return this.objectivesByName.get(s);
   }

   @Nullable
   public Objective getObjective(@Nullable String s) {
      return this.objectivesByName.get(s);
   }

   public Objective addObjective(String s, ObjectiveCriteria objectivecriteria, Component component, ObjectiveCriteria.RenderType objectivecriteria_rendertype) {
      if (this.objectivesByName.containsKey(s)) {
         throw new IllegalArgumentException("An objective with the name '" + s + "' already exists!");
      } else {
         Objective objective = new Objective(this, s, objectivecriteria, component, objectivecriteria_rendertype);
         this.objectivesByCriteria.computeIfAbsent(objectivecriteria, (objectivecriteria1) -> Lists.newArrayList()).add(objective);
         this.objectivesByName.put(s, objective);
         this.onObjectiveAdded(objective);
         return objective;
      }
   }

   public final void forAllObjectives(ObjectiveCriteria objectivecriteria, String s, Consumer<Score> consumer) {
      this.objectivesByCriteria.getOrDefault(objectivecriteria, Collections.emptyList()).forEach((objective) -> consumer.accept(this.getOrCreatePlayerScore(s, objective)));
   }

   public boolean hasPlayerScore(String s, Objective objective) {
      Map<Objective, Score> map = this.playerScores.get(s);
      if (map == null) {
         return false;
      } else {
         Score score = map.get(objective);
         return score != null;
      }
   }

   public Score getOrCreatePlayerScore(String s, Objective objective) {
      Map<Objective, Score> map = this.playerScores.computeIfAbsent(s, (s2) -> Maps.newHashMap());
      return map.computeIfAbsent(objective, (objective1) -> {
         Score score = new Score(this, objective1, s);
         score.setScore(0);
         return score;
      });
   }

   public Collection<Score> getPlayerScores(Objective objective) {
      List<Score> list = Lists.newArrayList();

      for(Map<Objective, Score> map : this.playerScores.values()) {
         Score score = map.get(objective);
         if (score != null) {
            list.add(score);
         }
      }

      list.sort(Score.SCORE_COMPARATOR);
      return list;
   }

   public Collection<Objective> getObjectives() {
      return this.objectivesByName.values();
   }

   public Collection<String> getObjectiveNames() {
      return this.objectivesByName.keySet();
   }

   public Collection<String> getTrackedPlayers() {
      return Lists.newArrayList(this.playerScores.keySet());
   }

   public void resetPlayerScore(String s, @Nullable Objective objective) {
      if (objective == null) {
         Map<Objective, Score> map = this.playerScores.remove(s);
         if (map != null) {
            this.onPlayerRemoved(s);
         }
      } else {
         Map<Objective, Score> map1 = this.playerScores.get(s);
         if (map1 != null) {
            Score score = map1.remove(objective);
            if (map1.size() < 1) {
               Map<Objective, Score> map2 = this.playerScores.remove(s);
               if (map2 != null) {
                  this.onPlayerRemoved(s);
               }
            } else if (score != null) {
               this.onPlayerScoreRemoved(s, objective);
            }
         }
      }

   }

   public Map<Objective, Score> getPlayerScores(String s) {
      Map<Objective, Score> map = this.playerScores.get(s);
      if (map == null) {
         map = Maps.newHashMap();
      }

      return map;
   }

   public void removeObjective(Objective objective) {
      this.objectivesByName.remove(objective.getName());

      for(int i = 0; i < 19; ++i) {
         if (this.getDisplayObjective(i) == objective) {
            this.setDisplayObjective(i, (Objective)null);
         }
      }

      List<Objective> list = this.objectivesByCriteria.get(objective.getCriteria());
      if (list != null) {
         list.remove(objective);
      }

      for(Map<Objective, Score> map : this.playerScores.values()) {
         map.remove(objective);
      }

      this.onObjectiveRemoved(objective);
   }

   public void setDisplayObjective(int i, @Nullable Objective objective) {
      this.displayObjectives[i] = objective;
   }

   @Nullable
   public Objective getDisplayObjective(int i) {
      return this.displayObjectives[i];
   }

   @Nullable
   public PlayerTeam getPlayerTeam(String s) {
      return this.teamsByName.get(s);
   }

   public PlayerTeam addPlayerTeam(String s) {
      PlayerTeam playerteam = this.getPlayerTeam(s);
      if (playerteam != null) {
         LOGGER.warn("Requested creation of existing team '{}'", (Object)s);
         return playerteam;
      } else {
         playerteam = new PlayerTeam(this, s);
         this.teamsByName.put(s, playerteam);
         this.onTeamAdded(playerteam);
         return playerteam;
      }
   }

   public void removePlayerTeam(PlayerTeam playerteam) {
      this.teamsByName.remove(playerteam.getName());

      for(String s : playerteam.getPlayers()) {
         this.teamsByPlayer.remove(s);
      }

      this.onTeamRemoved(playerteam);
   }

   public boolean addPlayerToTeam(String s, PlayerTeam playerteam) {
      if (this.getPlayersTeam(s) != null) {
         this.removePlayerFromTeam(s);
      }

      this.teamsByPlayer.put(s, playerteam);
      return playerteam.getPlayers().add(s);
   }

   public boolean removePlayerFromTeam(String s) {
      PlayerTeam playerteam = this.getPlayersTeam(s);
      if (playerteam != null) {
         this.removePlayerFromTeam(s, playerteam);
         return true;
      } else {
         return false;
      }
   }

   public void removePlayerFromTeam(String s, PlayerTeam playerteam) {
      if (this.getPlayersTeam(s) != playerteam) {
         throw new IllegalStateException("Player is either on another team or not on any team. Cannot remove from team '" + playerteam.getName() + "'.");
      } else {
         this.teamsByPlayer.remove(s);
         playerteam.getPlayers().remove(s);
      }
   }

   public Collection<String> getTeamNames() {
      return this.teamsByName.keySet();
   }

   public Collection<PlayerTeam> getPlayerTeams() {
      return this.teamsByName.values();
   }

   @Nullable
   public PlayerTeam getPlayersTeam(String s) {
      return this.teamsByPlayer.get(s);
   }

   public void onObjectiveAdded(Objective objective) {
   }

   public void onObjectiveChanged(Objective objective) {
   }

   public void onObjectiveRemoved(Objective objective) {
   }

   public void onScoreChanged(Score score) {
   }

   public void onPlayerRemoved(String s) {
   }

   public void onPlayerScoreRemoved(String s, Objective objective) {
   }

   public void onTeamAdded(PlayerTeam playerteam) {
   }

   public void onTeamChanged(PlayerTeam playerteam) {
   }

   public void onTeamRemoved(PlayerTeam playerteam) {
   }

   public static String getDisplaySlotName(int i) {
      switch (i) {
         case 0:
            return "list";
         case 1:
            return "sidebar";
         case 2:
            return "belowName";
         default:
            if (i >= 3 && i <= 18) {
               ChatFormatting chatformatting = ChatFormatting.getById(i - 3);
               if (chatformatting != null && chatformatting != ChatFormatting.RESET) {
                  return "sidebar.team." + chatformatting.getName();
               }
            }

            return null;
      }
   }

   public static int getDisplaySlotByName(String s) {
      if ("list".equalsIgnoreCase(s)) {
         return 0;
      } else if ("sidebar".equalsIgnoreCase(s)) {
         return 1;
      } else if ("belowName".equalsIgnoreCase(s)) {
         return 2;
      } else {
         if (s.startsWith("sidebar.team.")) {
            String s1 = s.substring("sidebar.team.".length());
            ChatFormatting chatformatting = ChatFormatting.getByName(s1);
            if (chatformatting != null && chatformatting.getId() >= 0) {
               return chatformatting.getId() + 3;
            }
         }

         return -1;
      }
   }

   public static String[] getDisplaySlotNames() {
      if (displaySlotNames == null) {
         displaySlotNames = new String[19];

         for(int i = 0; i < 19; ++i) {
            displaySlotNames[i] = getDisplaySlotName(i);
         }
      }

      return displaySlotNames;
   }

   public void entityRemoved(Entity entity) {
      if (entity != null && !(entity instanceof Player) && !entity.isAlive()) {
         String s = entity.getStringUUID();
         this.resetPlayerScore(s, (Objective)null);
         this.removePlayerFromTeam(s);
      }
   }

   protected ListTag savePlayerScores() {
      ListTag listtag = new ListTag();
      this.playerScores.values().stream().map(Map::values).forEach((collection) -> collection.stream().filter((score1) -> score1.getObjective() != null).forEach((score) -> {
            CompoundTag compoundtag = new CompoundTag();
            compoundtag.putString("Name", score.getOwner());
            compoundtag.putString("Objective", score.getObjective().getName());
            compoundtag.putInt("Score", score.getScore());
            compoundtag.putBoolean("Locked", score.isLocked());
            listtag.add(compoundtag);
         }));
      return listtag;
   }

   protected void loadPlayerScores(ListTag listtag) {
      for(int i = 0; i < listtag.size(); ++i) {
         CompoundTag compoundtag = listtag.getCompound(i);
         Objective objective = this.getOrCreateObjective(compoundtag.getString("Objective"));
         String s = compoundtag.getString("Name");
         Score score = this.getOrCreatePlayerScore(s, objective);
         score.setScore(compoundtag.getInt("Score"));
         if (compoundtag.contains("Locked")) {
            score.setLocked(compoundtag.getBoolean("Locked"));
         }
      }

   }
}
