package net.minecraft.server.commands;

import com.google.common.collect.Lists;
import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.arguments.BoolArgumentType;
import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.brigadier.exceptions.SimpleCommandExceptionType;
import java.util.Collection;
import java.util.Collections;
import net.minecraft.ChatFormatting;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.commands.arguments.ColorArgument;
import net.minecraft.commands.arguments.ComponentArgument;
import net.minecraft.commands.arguments.ScoreHolderArgument;
import net.minecraft.commands.arguments.TeamArgument;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.ComponentUtils;
import net.minecraft.world.scores.PlayerTeam;
import net.minecraft.world.scores.Scoreboard;
import net.minecraft.world.scores.Team;

public class TeamCommand {
   private static final SimpleCommandExceptionType ERROR_TEAM_ALREADY_EXISTS = new SimpleCommandExceptionType(Component.translatable("commands.team.add.duplicate"));
   private static final SimpleCommandExceptionType ERROR_TEAM_ALREADY_EMPTY = new SimpleCommandExceptionType(Component.translatable("commands.team.empty.unchanged"));
   private static final SimpleCommandExceptionType ERROR_TEAM_ALREADY_NAME = new SimpleCommandExceptionType(Component.translatable("commands.team.option.name.unchanged"));
   private static final SimpleCommandExceptionType ERROR_TEAM_ALREADY_COLOR = new SimpleCommandExceptionType(Component.translatable("commands.team.option.color.unchanged"));
   private static final SimpleCommandExceptionType ERROR_TEAM_ALREADY_FRIENDLYFIRE_ENABLED = new SimpleCommandExceptionType(Component.translatable("commands.team.option.friendlyfire.alreadyEnabled"));
   private static final SimpleCommandExceptionType ERROR_TEAM_ALREADY_FRIENDLYFIRE_DISABLED = new SimpleCommandExceptionType(Component.translatable("commands.team.option.friendlyfire.alreadyDisabled"));
   private static final SimpleCommandExceptionType ERROR_TEAM_ALREADY_FRIENDLYINVISIBLES_ENABLED = new SimpleCommandExceptionType(Component.translatable("commands.team.option.seeFriendlyInvisibles.alreadyEnabled"));
   private static final SimpleCommandExceptionType ERROR_TEAM_ALREADY_FRIENDLYINVISIBLES_DISABLED = new SimpleCommandExceptionType(Component.translatable("commands.team.option.seeFriendlyInvisibles.alreadyDisabled"));
   private static final SimpleCommandExceptionType ERROR_TEAM_NAMETAG_VISIBLITY_UNCHANGED = new SimpleCommandExceptionType(Component.translatable("commands.team.option.nametagVisibility.unchanged"));
   private static final SimpleCommandExceptionType ERROR_TEAM_DEATH_MESSAGE_VISIBLITY_UNCHANGED = new SimpleCommandExceptionType(Component.translatable("commands.team.option.deathMessageVisibility.unchanged"));
   private static final SimpleCommandExceptionType ERROR_TEAM_COLLISION_UNCHANGED = new SimpleCommandExceptionType(Component.translatable("commands.team.option.collisionRule.unchanged"));

   public static void register(CommandDispatcher<CommandSourceStack> commanddispatcher) {
      commanddispatcher.register(Commands.literal("team").requires((commandsourcestack) -> commandsourcestack.hasPermission(2)).then(Commands.literal("list").executes((commandcontext26) -> listTeams(commandcontext26.getSource())).then(Commands.argument("team", TeamArgument.team()).executes((commandcontext25) -> listMembers(commandcontext25.getSource(), TeamArgument.getTeam(commandcontext25, "team"))))).then(Commands.literal("add").then(Commands.argument("team", StringArgumentType.word()).executes((commandcontext24) -> createTeam(commandcontext24.getSource(), StringArgumentType.getString(commandcontext24, "team"))).then(Commands.argument("displayName", ComponentArgument.textComponent()).executes((commandcontext23) -> createTeam(commandcontext23.getSource(), StringArgumentType.getString(commandcontext23, "team"), ComponentArgument.getComponent(commandcontext23, "displayName")))))).then(Commands.literal("remove").then(Commands.argument("team", TeamArgument.team()).executes((commandcontext22) -> deleteTeam(commandcontext22.getSource(), TeamArgument.getTeam(commandcontext22, "team"))))).then(Commands.literal("empty").then(Commands.argument("team", TeamArgument.team()).executes((commandcontext21) -> emptyTeam(commandcontext21.getSource(), TeamArgument.getTeam(commandcontext21, "team"))))).then(Commands.literal("join").then(Commands.argument("team", TeamArgument.team()).executes((commandcontext20) -> joinTeam(commandcontext20.getSource(), TeamArgument.getTeam(commandcontext20, "team"), Collections.singleton(commandcontext20.getSource().getEntityOrException().getScoreboardName()))).then(Commands.argument("members", ScoreHolderArgument.scoreHolders()).suggests(ScoreHolderArgument.SUGGEST_SCORE_HOLDERS).executes((commandcontext19) -> joinTeam(commandcontext19.getSource(), TeamArgument.getTeam(commandcontext19, "team"), ScoreHolderArgument.getNamesWithDefaultWildcard(commandcontext19, "members")))))).then(Commands.literal("leave").then(Commands.argument("members", ScoreHolderArgument.scoreHolders()).suggests(ScoreHolderArgument.SUGGEST_SCORE_HOLDERS).executes((commandcontext18) -> leaveTeam(commandcontext18.getSource(), ScoreHolderArgument.getNamesWithDefaultWildcard(commandcontext18, "members"))))).then(Commands.literal("modify").then(Commands.argument("team", TeamArgument.team()).then(Commands.literal("displayName").then(Commands.argument("displayName", ComponentArgument.textComponent()).executes((commandcontext17) -> setDisplayName(commandcontext17.getSource(), TeamArgument.getTeam(commandcontext17, "team"), ComponentArgument.getComponent(commandcontext17, "displayName"))))).then(Commands.literal("color").then(Commands.argument("value", ColorArgument.color()).executes((commandcontext16) -> setColor(commandcontext16.getSource(), TeamArgument.getTeam(commandcontext16, "team"), ColorArgument.getColor(commandcontext16, "value"))))).then(Commands.literal("friendlyFire").then(Commands.argument("allowed", BoolArgumentType.bool()).executes((commandcontext15) -> setFriendlyFire(commandcontext15.getSource(), TeamArgument.getTeam(commandcontext15, "team"), BoolArgumentType.getBool(commandcontext15, "allowed"))))).then(Commands.literal("seeFriendlyInvisibles").then(Commands.argument("allowed", BoolArgumentType.bool()).executes((commandcontext14) -> setFriendlySight(commandcontext14.getSource(), TeamArgument.getTeam(commandcontext14, "team"), BoolArgumentType.getBool(commandcontext14, "allowed"))))).then(Commands.literal("nametagVisibility").then(Commands.literal("never").executes((commandcontext13) -> setNametagVisibility(commandcontext13.getSource(), TeamArgument.getTeam(commandcontext13, "team"), Team.Visibility.NEVER))).then(Commands.literal("hideForOtherTeams").executes((commandcontext12) -> setNametagVisibility(commandcontext12.getSource(), TeamArgument.getTeam(commandcontext12, "team"), Team.Visibility.HIDE_FOR_OTHER_TEAMS))).then(Commands.literal("hideForOwnTeam").executes((commandcontext11) -> setNametagVisibility(commandcontext11.getSource(), TeamArgument.getTeam(commandcontext11, "team"), Team.Visibility.HIDE_FOR_OWN_TEAM))).then(Commands.literal("always").executes((commandcontext10) -> setNametagVisibility(commandcontext10.getSource(), TeamArgument.getTeam(commandcontext10, "team"), Team.Visibility.ALWAYS)))).then(Commands.literal("deathMessageVisibility").then(Commands.literal("never").executes((commandcontext9) -> setDeathMessageVisibility(commandcontext9.getSource(), TeamArgument.getTeam(commandcontext9, "team"), Team.Visibility.NEVER))).then(Commands.literal("hideForOtherTeams").executes((commandcontext8) -> setDeathMessageVisibility(commandcontext8.getSource(), TeamArgument.getTeam(commandcontext8, "team"), Team.Visibility.HIDE_FOR_OTHER_TEAMS))).then(Commands.literal("hideForOwnTeam").executes((commandcontext7) -> setDeathMessageVisibility(commandcontext7.getSource(), TeamArgument.getTeam(commandcontext7, "team"), Team.Visibility.HIDE_FOR_OWN_TEAM))).then(Commands.literal("always").executes((commandcontext6) -> setDeathMessageVisibility(commandcontext6.getSource(), TeamArgument.getTeam(commandcontext6, "team"), Team.Visibility.ALWAYS)))).then(Commands.literal("collisionRule").then(Commands.literal("never").executes((commandcontext5) -> setCollision(commandcontext5.getSource(), TeamArgument.getTeam(commandcontext5, "team"), Team.CollisionRule.NEVER))).then(Commands.literal("pushOwnTeam").executes((commandcontext4) -> setCollision(commandcontext4.getSource(), TeamArgument.getTeam(commandcontext4, "team"), Team.CollisionRule.PUSH_OWN_TEAM))).then(Commands.literal("pushOtherTeams").executes((commandcontext3) -> setCollision(commandcontext3.getSource(), TeamArgument.getTeam(commandcontext3, "team"), Team.CollisionRule.PUSH_OTHER_TEAMS))).then(Commands.literal("always").executes((commandcontext2) -> setCollision(commandcontext2.getSource(), TeamArgument.getTeam(commandcontext2, "team"), Team.CollisionRule.ALWAYS)))).then(Commands.literal("prefix").then(Commands.argument("prefix", ComponentArgument.textComponent()).executes((commandcontext1) -> setPrefix(commandcontext1.getSource(), TeamArgument.getTeam(commandcontext1, "team"), ComponentArgument.getComponent(commandcontext1, "prefix"))))).then(Commands.literal("suffix").then(Commands.argument("suffix", ComponentArgument.textComponent()).executes((commandcontext) -> setSuffix(commandcontext.getSource(), TeamArgument.getTeam(commandcontext, "team"), ComponentArgument.getComponent(commandcontext, "suffix"))))))));
   }

   private static int leaveTeam(CommandSourceStack commandsourcestack, Collection<String> collection) {
      Scoreboard scoreboard = commandsourcestack.getServer().getScoreboard();

      for(String s : collection) {
         scoreboard.removePlayerFromTeam(s);
      }

      if (collection.size() == 1) {
         commandsourcestack.sendSuccess(() -> Component.translatable("commands.team.leave.success.single", collection.iterator().next()), true);
      } else {
         commandsourcestack.sendSuccess(() -> Component.translatable("commands.team.leave.success.multiple", collection.size()), true);
      }

      return collection.size();
   }

   private static int joinTeam(CommandSourceStack commandsourcestack, PlayerTeam playerteam, Collection<String> collection) {
      Scoreboard scoreboard = commandsourcestack.getServer().getScoreboard();

      for(String s : collection) {
         scoreboard.addPlayerToTeam(s, playerteam);
      }

      if (collection.size() == 1) {
         commandsourcestack.sendSuccess(() -> Component.translatable("commands.team.join.success.single", collection.iterator().next(), playerteam.getFormattedDisplayName()), true);
      } else {
         commandsourcestack.sendSuccess(() -> Component.translatable("commands.team.join.success.multiple", collection.size(), playerteam.getFormattedDisplayName()), true);
      }

      return collection.size();
   }

   private static int setNametagVisibility(CommandSourceStack commandsourcestack, PlayerTeam playerteam, Team.Visibility team_visibility) throws CommandSyntaxException {
      if (playerteam.getNameTagVisibility() == team_visibility) {
         throw ERROR_TEAM_NAMETAG_VISIBLITY_UNCHANGED.create();
      } else {
         playerteam.setNameTagVisibility(team_visibility);
         commandsourcestack.sendSuccess(() -> Component.translatable("commands.team.option.nametagVisibility.success", playerteam.getFormattedDisplayName(), team_visibility.getDisplayName()), true);
         return 0;
      }
   }

   private static int setDeathMessageVisibility(CommandSourceStack commandsourcestack, PlayerTeam playerteam, Team.Visibility team_visibility) throws CommandSyntaxException {
      if (playerteam.getDeathMessageVisibility() == team_visibility) {
         throw ERROR_TEAM_DEATH_MESSAGE_VISIBLITY_UNCHANGED.create();
      } else {
         playerteam.setDeathMessageVisibility(team_visibility);
         commandsourcestack.sendSuccess(() -> Component.translatable("commands.team.option.deathMessageVisibility.success", playerteam.getFormattedDisplayName(), team_visibility.getDisplayName()), true);
         return 0;
      }
   }

   private static int setCollision(CommandSourceStack commandsourcestack, PlayerTeam playerteam, Team.CollisionRule team_collisionrule) throws CommandSyntaxException {
      if (playerteam.getCollisionRule() == team_collisionrule) {
         throw ERROR_TEAM_COLLISION_UNCHANGED.create();
      } else {
         playerteam.setCollisionRule(team_collisionrule);
         commandsourcestack.sendSuccess(() -> Component.translatable("commands.team.option.collisionRule.success", playerteam.getFormattedDisplayName(), team_collisionrule.getDisplayName()), true);
         return 0;
      }
   }

   private static int setFriendlySight(CommandSourceStack commandsourcestack, PlayerTeam playerteam, boolean flag) throws CommandSyntaxException {
      if (playerteam.canSeeFriendlyInvisibles() == flag) {
         if (flag) {
            throw ERROR_TEAM_ALREADY_FRIENDLYINVISIBLES_ENABLED.create();
         } else {
            throw ERROR_TEAM_ALREADY_FRIENDLYINVISIBLES_DISABLED.create();
         }
      } else {
         playerteam.setSeeFriendlyInvisibles(flag);
         commandsourcestack.sendSuccess(() -> Component.translatable("commands.team.option.seeFriendlyInvisibles." + (flag ? "enabled" : "disabled"), playerteam.getFormattedDisplayName()), true);
         return 0;
      }
   }

   private static int setFriendlyFire(CommandSourceStack commandsourcestack, PlayerTeam playerteam, boolean flag) throws CommandSyntaxException {
      if (playerteam.isAllowFriendlyFire() == flag) {
         if (flag) {
            throw ERROR_TEAM_ALREADY_FRIENDLYFIRE_ENABLED.create();
         } else {
            throw ERROR_TEAM_ALREADY_FRIENDLYFIRE_DISABLED.create();
         }
      } else {
         playerteam.setAllowFriendlyFire(flag);
         commandsourcestack.sendSuccess(() -> Component.translatable("commands.team.option.friendlyfire." + (flag ? "enabled" : "disabled"), playerteam.getFormattedDisplayName()), true);
         return 0;
      }
   }

   private static int setDisplayName(CommandSourceStack commandsourcestack, PlayerTeam playerteam, Component component) throws CommandSyntaxException {
      if (playerteam.getDisplayName().equals(component)) {
         throw ERROR_TEAM_ALREADY_NAME.create();
      } else {
         playerteam.setDisplayName(component);
         commandsourcestack.sendSuccess(() -> Component.translatable("commands.team.option.name.success", playerteam.getFormattedDisplayName()), true);
         return 0;
      }
   }

   private static int setColor(CommandSourceStack commandsourcestack, PlayerTeam playerteam, ChatFormatting chatformatting) throws CommandSyntaxException {
      if (playerteam.getColor() == chatformatting) {
         throw ERROR_TEAM_ALREADY_COLOR.create();
      } else {
         playerteam.setColor(chatformatting);
         commandsourcestack.sendSuccess(() -> Component.translatable("commands.team.option.color.success", playerteam.getFormattedDisplayName(), chatformatting.getName()), true);
         return 0;
      }
   }

   private static int emptyTeam(CommandSourceStack commandsourcestack, PlayerTeam playerteam) throws CommandSyntaxException {
      Scoreboard scoreboard = commandsourcestack.getServer().getScoreboard();
      Collection<String> collection = Lists.newArrayList(playerteam.getPlayers());
      if (collection.isEmpty()) {
         throw ERROR_TEAM_ALREADY_EMPTY.create();
      } else {
         for(String s : collection) {
            scoreboard.removePlayerFromTeam(s, playerteam);
         }

         commandsourcestack.sendSuccess(() -> Component.translatable("commands.team.empty.success", collection.size(), playerteam.getFormattedDisplayName()), true);
         return collection.size();
      }
   }

   private static int deleteTeam(CommandSourceStack commandsourcestack, PlayerTeam playerteam) {
      Scoreboard scoreboard = commandsourcestack.getServer().getScoreboard();
      scoreboard.removePlayerTeam(playerteam);
      commandsourcestack.sendSuccess(() -> Component.translatable("commands.team.remove.success", playerteam.getFormattedDisplayName()), true);
      return scoreboard.getPlayerTeams().size();
   }

   private static int createTeam(CommandSourceStack commandsourcestack, String s) throws CommandSyntaxException {
      return createTeam(commandsourcestack, s, Component.literal(s));
   }

   private static int createTeam(CommandSourceStack commandsourcestack, String s, Component component) throws CommandSyntaxException {
      Scoreboard scoreboard = commandsourcestack.getServer().getScoreboard();
      if (scoreboard.getPlayerTeam(s) != null) {
         throw ERROR_TEAM_ALREADY_EXISTS.create();
      } else {
         PlayerTeam playerteam = scoreboard.addPlayerTeam(s);
         playerteam.setDisplayName(component);
         commandsourcestack.sendSuccess(() -> Component.translatable("commands.team.add.success", playerteam.getFormattedDisplayName()), true);
         return scoreboard.getPlayerTeams().size();
      }
   }

   private static int listMembers(CommandSourceStack commandsourcestack, PlayerTeam playerteam) {
      Collection<String> collection = playerteam.getPlayers();
      if (collection.isEmpty()) {
         commandsourcestack.sendSuccess(() -> Component.translatable("commands.team.list.members.empty", playerteam.getFormattedDisplayName()), false);
      } else {
         commandsourcestack.sendSuccess(() -> Component.translatable("commands.team.list.members.success", playerteam.getFormattedDisplayName(), collection.size(), ComponentUtils.formatList(collection)), false);
      }

      return collection.size();
   }

   private static int listTeams(CommandSourceStack commandsourcestack) {
      Collection<PlayerTeam> collection = commandsourcestack.getServer().getScoreboard().getPlayerTeams();
      if (collection.isEmpty()) {
         commandsourcestack.sendSuccess(() -> Component.translatable("commands.team.list.teams.empty"), false);
      } else {
         commandsourcestack.sendSuccess(() -> Component.translatable("commands.team.list.teams.success", collection.size(), ComponentUtils.formatList(collection, PlayerTeam::getFormattedDisplayName)), false);
      }

      return collection.size();
   }

   private static int setPrefix(CommandSourceStack commandsourcestack, PlayerTeam playerteam, Component component) {
      playerteam.setPlayerPrefix(component);
      commandsourcestack.sendSuccess(() -> Component.translatable("commands.team.option.prefix.success", component), false);
      return 1;
   }

   private static int setSuffix(CommandSourceStack commandsourcestack, PlayerTeam playerteam, Component component) {
      playerteam.setPlayerSuffix(component);
      commandsourcestack.sendSuccess(() -> Component.translatable("commands.team.option.suffix.success", component), false);
      return 1;
   }
}
