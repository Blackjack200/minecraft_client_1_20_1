package net.minecraft.server.commands;

import com.google.common.collect.Lists;
import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.arguments.IntegerArgumentType;
import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.brigadier.exceptions.Dynamic2CommandExceptionType;
import com.mojang.brigadier.exceptions.SimpleCommandExceptionType;
import com.mojang.brigadier.suggestion.Suggestions;
import com.mojang.brigadier.suggestion.SuggestionsBuilder;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.commands.SharedSuggestionProvider;
import net.minecraft.commands.arguments.ComponentArgument;
import net.minecraft.commands.arguments.ObjectiveArgument;
import net.minecraft.commands.arguments.ObjectiveCriteriaArgument;
import net.minecraft.commands.arguments.OperationArgument;
import net.minecraft.commands.arguments.ScoreHolderArgument;
import net.minecraft.commands.arguments.ScoreboardSlotArgument;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.ComponentUtils;
import net.minecraft.world.scores.Objective;
import net.minecraft.world.scores.Score;
import net.minecraft.world.scores.Scoreboard;
import net.minecraft.world.scores.criteria.ObjectiveCriteria;

public class ScoreboardCommand {
   private static final SimpleCommandExceptionType ERROR_OBJECTIVE_ALREADY_EXISTS = new SimpleCommandExceptionType(Component.translatable("commands.scoreboard.objectives.add.duplicate"));
   private static final SimpleCommandExceptionType ERROR_DISPLAY_SLOT_ALREADY_EMPTY = new SimpleCommandExceptionType(Component.translatable("commands.scoreboard.objectives.display.alreadyEmpty"));
   private static final SimpleCommandExceptionType ERROR_DISPLAY_SLOT_ALREADY_SET = new SimpleCommandExceptionType(Component.translatable("commands.scoreboard.objectives.display.alreadySet"));
   private static final SimpleCommandExceptionType ERROR_TRIGGER_ALREADY_ENABLED = new SimpleCommandExceptionType(Component.translatable("commands.scoreboard.players.enable.failed"));
   private static final SimpleCommandExceptionType ERROR_NOT_TRIGGER = new SimpleCommandExceptionType(Component.translatable("commands.scoreboard.players.enable.invalid"));
   private static final Dynamic2CommandExceptionType ERROR_NO_VALUE = new Dynamic2CommandExceptionType((object, object1) -> Component.translatable("commands.scoreboard.players.get.null", object, object1));

   public static void register(CommandDispatcher<CommandSourceStack> commanddispatcher) {
      commanddispatcher.register(Commands.literal("scoreboard").requires((commandsourcestack) -> commandsourcestack.hasPermission(2)).then(Commands.literal("objectives").then(Commands.literal("list").executes((commandcontext17) -> listObjectives(commandcontext17.getSource()))).then(Commands.literal("add").then(Commands.argument("objective", StringArgumentType.word()).then(Commands.argument("criteria", ObjectiveCriteriaArgument.criteria()).executes((commandcontext16) -> addObjective(commandcontext16.getSource(), StringArgumentType.getString(commandcontext16, "objective"), ObjectiveCriteriaArgument.getCriteria(commandcontext16, "criteria"), Component.literal(StringArgumentType.getString(commandcontext16, "objective")))).then(Commands.argument("displayName", ComponentArgument.textComponent()).executes((commandcontext15) -> addObjective(commandcontext15.getSource(), StringArgumentType.getString(commandcontext15, "objective"), ObjectiveCriteriaArgument.getCriteria(commandcontext15, "criteria"), ComponentArgument.getComponent(commandcontext15, "displayName"))))))).then(Commands.literal("modify").then(Commands.argument("objective", ObjectiveArgument.objective()).then(Commands.literal("displayname").then(Commands.argument("displayName", ComponentArgument.textComponent()).executes((commandcontext14) -> setDisplayName(commandcontext14.getSource(), ObjectiveArgument.getObjective(commandcontext14, "objective"), ComponentArgument.getComponent(commandcontext14, "displayName"))))).then(createRenderTypeModify()))).then(Commands.literal("remove").then(Commands.argument("objective", ObjectiveArgument.objective()).executes((commandcontext13) -> removeObjective(commandcontext13.getSource(), ObjectiveArgument.getObjective(commandcontext13, "objective"))))).then(Commands.literal("setdisplay").then(Commands.argument("slot", ScoreboardSlotArgument.displaySlot()).executes((commandcontext12) -> clearDisplaySlot(commandcontext12.getSource(), ScoreboardSlotArgument.getDisplaySlot(commandcontext12, "slot"))).then(Commands.argument("objective", ObjectiveArgument.objective()).executes((commandcontext11) -> setDisplaySlot(commandcontext11.getSource(), ScoreboardSlotArgument.getDisplaySlot(commandcontext11, "slot"), ObjectiveArgument.getObjective(commandcontext11, "objective"))))))).then(Commands.literal("players").then(Commands.literal("list").executes((commandcontext10) -> listTrackedPlayers(commandcontext10.getSource())).then(Commands.argument("target", ScoreHolderArgument.scoreHolder()).suggests(ScoreHolderArgument.SUGGEST_SCORE_HOLDERS).executes((commandcontext9) -> listTrackedPlayerScores(commandcontext9.getSource(), ScoreHolderArgument.getName(commandcontext9, "target"))))).then(Commands.literal("set").then(Commands.argument("targets", ScoreHolderArgument.scoreHolders()).suggests(ScoreHolderArgument.SUGGEST_SCORE_HOLDERS).then(Commands.argument("objective", ObjectiveArgument.objective()).then(Commands.argument("score", IntegerArgumentType.integer()).executes((commandcontext8) -> setScore(commandcontext8.getSource(), ScoreHolderArgument.getNamesWithDefaultWildcard(commandcontext8, "targets"), ObjectiveArgument.getWritableObjective(commandcontext8, "objective"), IntegerArgumentType.getInteger(commandcontext8, "score"))))))).then(Commands.literal("get").then(Commands.argument("target", ScoreHolderArgument.scoreHolder()).suggests(ScoreHolderArgument.SUGGEST_SCORE_HOLDERS).then(Commands.argument("objective", ObjectiveArgument.objective()).executes((commandcontext7) -> getScore(commandcontext7.getSource(), ScoreHolderArgument.getName(commandcontext7, "target"), ObjectiveArgument.getObjective(commandcontext7, "objective")))))).then(Commands.literal("add").then(Commands.argument("targets", ScoreHolderArgument.scoreHolders()).suggests(ScoreHolderArgument.SUGGEST_SCORE_HOLDERS).then(Commands.argument("objective", ObjectiveArgument.objective()).then(Commands.argument("score", IntegerArgumentType.integer(0)).executes((commandcontext6) -> addScore(commandcontext6.getSource(), ScoreHolderArgument.getNamesWithDefaultWildcard(commandcontext6, "targets"), ObjectiveArgument.getWritableObjective(commandcontext6, "objective"), IntegerArgumentType.getInteger(commandcontext6, "score"))))))).then(Commands.literal("remove").then(Commands.argument("targets", ScoreHolderArgument.scoreHolders()).suggests(ScoreHolderArgument.SUGGEST_SCORE_HOLDERS).then(Commands.argument("objective", ObjectiveArgument.objective()).then(Commands.argument("score", IntegerArgumentType.integer(0)).executes((commandcontext5) -> removeScore(commandcontext5.getSource(), ScoreHolderArgument.getNamesWithDefaultWildcard(commandcontext5, "targets"), ObjectiveArgument.getWritableObjective(commandcontext5, "objective"), IntegerArgumentType.getInteger(commandcontext5, "score"))))))).then(Commands.literal("reset").then(Commands.argument("targets", ScoreHolderArgument.scoreHolders()).suggests(ScoreHolderArgument.SUGGEST_SCORE_HOLDERS).executes((commandcontext4) -> resetScores(commandcontext4.getSource(), ScoreHolderArgument.getNamesWithDefaultWildcard(commandcontext4, "targets"))).then(Commands.argument("objective", ObjectiveArgument.objective()).executes((commandcontext3) -> resetScore(commandcontext3.getSource(), ScoreHolderArgument.getNamesWithDefaultWildcard(commandcontext3, "targets"), ObjectiveArgument.getObjective(commandcontext3, "objective")))))).then(Commands.literal("enable").then(Commands.argument("targets", ScoreHolderArgument.scoreHolders()).suggests(ScoreHolderArgument.SUGGEST_SCORE_HOLDERS).then(Commands.argument("objective", ObjectiveArgument.objective()).suggests((commandcontext2, suggestionsbuilder) -> suggestTriggers(commandcontext2.getSource(), ScoreHolderArgument.getNamesWithDefaultWildcard(commandcontext2, "targets"), suggestionsbuilder)).executes((commandcontext1) -> enableTrigger(commandcontext1.getSource(), ScoreHolderArgument.getNamesWithDefaultWildcard(commandcontext1, "targets"), ObjectiveArgument.getObjective(commandcontext1, "objective")))))).then(Commands.literal("operation").then(Commands.argument("targets", ScoreHolderArgument.scoreHolders()).suggests(ScoreHolderArgument.SUGGEST_SCORE_HOLDERS).then(Commands.argument("targetObjective", ObjectiveArgument.objective()).then(Commands.argument("operation", OperationArgument.operation()).then(Commands.argument("source", ScoreHolderArgument.scoreHolders()).suggests(ScoreHolderArgument.SUGGEST_SCORE_HOLDERS).then(Commands.argument("sourceObjective", ObjectiveArgument.objective()).executes((commandcontext) -> performOperation(commandcontext.getSource(), ScoreHolderArgument.getNamesWithDefaultWildcard(commandcontext, "targets"), ObjectiveArgument.getWritableObjective(commandcontext, "targetObjective"), OperationArgument.getOperation(commandcontext, "operation"), ScoreHolderArgument.getNamesWithDefaultWildcard(commandcontext, "source"), ObjectiveArgument.getObjective(commandcontext, "sourceObjective")))))))))));
   }

   private static LiteralArgumentBuilder<CommandSourceStack> createRenderTypeModify() {
      LiteralArgumentBuilder<CommandSourceStack> literalargumentbuilder = Commands.literal("rendertype");

      for(ObjectiveCriteria.RenderType objectivecriteria_rendertype : ObjectiveCriteria.RenderType.values()) {
         literalargumentbuilder.then(Commands.literal(objectivecriteria_rendertype.getId()).executes((commandcontext) -> setRenderType(commandcontext.getSource(), ObjectiveArgument.getObjective(commandcontext, "objective"), objectivecriteria_rendertype)));
      }

      return literalargumentbuilder;
   }

   private static CompletableFuture<Suggestions> suggestTriggers(CommandSourceStack commandsourcestack, Collection<String> collection, SuggestionsBuilder suggestionsbuilder) {
      List<String> list = Lists.newArrayList();
      Scoreboard scoreboard = commandsourcestack.getServer().getScoreboard();

      for(Objective objective : scoreboard.getObjectives()) {
         if (objective.getCriteria() == ObjectiveCriteria.TRIGGER) {
            boolean flag = false;

            for(String s : collection) {
               if (!scoreboard.hasPlayerScore(s, objective) || scoreboard.getOrCreatePlayerScore(s, objective).isLocked()) {
                  flag = true;
                  break;
               }
            }

            if (flag) {
               list.add(objective.getName());
            }
         }
      }

      return SharedSuggestionProvider.suggest(list, suggestionsbuilder);
   }

   private static int getScore(CommandSourceStack commandsourcestack, String s, Objective objective) throws CommandSyntaxException {
      Scoreboard scoreboard = commandsourcestack.getServer().getScoreboard();
      if (!scoreboard.hasPlayerScore(s, objective)) {
         throw ERROR_NO_VALUE.create(objective.getName(), s);
      } else {
         Score score = scoreboard.getOrCreatePlayerScore(s, objective);
         commandsourcestack.sendSuccess(() -> Component.translatable("commands.scoreboard.players.get.success", s, score.getScore(), objective.getFormattedDisplayName()), false);
         return score.getScore();
      }
   }

   private static int performOperation(CommandSourceStack commandsourcestack, Collection<String> collection, Objective objective, OperationArgument.Operation operationargument_operation, Collection<String> collection1, Objective objective1) throws CommandSyntaxException {
      Scoreboard scoreboard = commandsourcestack.getServer().getScoreboard();
      int i = 0;

      for(String s : collection) {
         Score score = scoreboard.getOrCreatePlayerScore(s, objective);

         for(String s1 : collection1) {
            Score score1 = scoreboard.getOrCreatePlayerScore(s1, objective1);
            operationargument_operation.apply(score, score1);
         }

         i += score.getScore();
      }

      if (collection.size() == 1) {
         int j = i;
         commandsourcestack.sendSuccess(() -> Component.translatable("commands.scoreboard.players.operation.success.single", objective.getFormattedDisplayName(), collection.iterator().next(), j), true);
      } else {
         commandsourcestack.sendSuccess(() -> Component.translatable("commands.scoreboard.players.operation.success.multiple", objective.getFormattedDisplayName(), collection.size()), true);
      }

      return i;
   }

   private static int enableTrigger(CommandSourceStack commandsourcestack, Collection<String> collection, Objective objective) throws CommandSyntaxException {
      if (objective.getCriteria() != ObjectiveCriteria.TRIGGER) {
         throw ERROR_NOT_TRIGGER.create();
      } else {
         Scoreboard scoreboard = commandsourcestack.getServer().getScoreboard();
         int i = 0;

         for(String s : collection) {
            Score score = scoreboard.getOrCreatePlayerScore(s, objective);
            if (score.isLocked()) {
               score.setLocked(false);
               ++i;
            }
         }

         if (i == 0) {
            throw ERROR_TRIGGER_ALREADY_ENABLED.create();
         } else {
            if (collection.size() == 1) {
               commandsourcestack.sendSuccess(() -> Component.translatable("commands.scoreboard.players.enable.success.single", objective.getFormattedDisplayName(), collection.iterator().next()), true);
            } else {
               commandsourcestack.sendSuccess(() -> Component.translatable("commands.scoreboard.players.enable.success.multiple", objective.getFormattedDisplayName(), collection.size()), true);
            }

            return i;
         }
      }
   }

   private static int resetScores(CommandSourceStack commandsourcestack, Collection<String> collection) {
      Scoreboard scoreboard = commandsourcestack.getServer().getScoreboard();

      for(String s : collection) {
         scoreboard.resetPlayerScore(s, (Objective)null);
      }

      if (collection.size() == 1) {
         commandsourcestack.sendSuccess(() -> Component.translatable("commands.scoreboard.players.reset.all.single", collection.iterator().next()), true);
      } else {
         commandsourcestack.sendSuccess(() -> Component.translatable("commands.scoreboard.players.reset.all.multiple", collection.size()), true);
      }

      return collection.size();
   }

   private static int resetScore(CommandSourceStack commandsourcestack, Collection<String> collection, Objective objective) {
      Scoreboard scoreboard = commandsourcestack.getServer().getScoreboard();

      for(String s : collection) {
         scoreboard.resetPlayerScore(s, objective);
      }

      if (collection.size() == 1) {
         commandsourcestack.sendSuccess(() -> Component.translatable("commands.scoreboard.players.reset.specific.single", objective.getFormattedDisplayName(), collection.iterator().next()), true);
      } else {
         commandsourcestack.sendSuccess(() -> Component.translatable("commands.scoreboard.players.reset.specific.multiple", objective.getFormattedDisplayName(), collection.size()), true);
      }

      return collection.size();
   }

   private static int setScore(CommandSourceStack commandsourcestack, Collection<String> collection, Objective objective, int i) {
      Scoreboard scoreboard = commandsourcestack.getServer().getScoreboard();

      for(String s : collection) {
         Score score = scoreboard.getOrCreatePlayerScore(s, objective);
         score.setScore(i);
      }

      if (collection.size() == 1) {
         commandsourcestack.sendSuccess(() -> Component.translatable("commands.scoreboard.players.set.success.single", objective.getFormattedDisplayName(), collection.iterator().next(), i), true);
      } else {
         commandsourcestack.sendSuccess(() -> Component.translatable("commands.scoreboard.players.set.success.multiple", objective.getFormattedDisplayName(), collection.size(), i), true);
      }

      return i * collection.size();
   }

   private static int addScore(CommandSourceStack commandsourcestack, Collection<String> collection, Objective objective, int i) {
      Scoreboard scoreboard = commandsourcestack.getServer().getScoreboard();
      int j = 0;

      for(String s : collection) {
         Score score = scoreboard.getOrCreatePlayerScore(s, objective);
         score.setScore(score.getScore() + i);
         j += score.getScore();
      }

      if (collection.size() == 1) {
         int k = j;
         commandsourcestack.sendSuccess(() -> Component.translatable("commands.scoreboard.players.add.success.single", i, objective.getFormattedDisplayName(), collection.iterator().next(), k), true);
      } else {
         commandsourcestack.sendSuccess(() -> Component.translatable("commands.scoreboard.players.add.success.multiple", i, objective.getFormattedDisplayName(), collection.size()), true);
      }

      return j;
   }

   private static int removeScore(CommandSourceStack commandsourcestack, Collection<String> collection, Objective objective, int i) {
      Scoreboard scoreboard = commandsourcestack.getServer().getScoreboard();
      int j = 0;

      for(String s : collection) {
         Score score = scoreboard.getOrCreatePlayerScore(s, objective);
         score.setScore(score.getScore() - i);
         j += score.getScore();
      }

      if (collection.size() == 1) {
         int k = j;
         commandsourcestack.sendSuccess(() -> Component.translatable("commands.scoreboard.players.remove.success.single", i, objective.getFormattedDisplayName(), collection.iterator().next(), k), true);
      } else {
         commandsourcestack.sendSuccess(() -> Component.translatable("commands.scoreboard.players.remove.success.multiple", i, objective.getFormattedDisplayName(), collection.size()), true);
      }

      return j;
   }

   private static int listTrackedPlayers(CommandSourceStack commandsourcestack) {
      Collection<String> collection = commandsourcestack.getServer().getScoreboard().getTrackedPlayers();
      if (collection.isEmpty()) {
         commandsourcestack.sendSuccess(() -> Component.translatable("commands.scoreboard.players.list.empty"), false);
      } else {
         commandsourcestack.sendSuccess(() -> Component.translatable("commands.scoreboard.players.list.success", collection.size(), ComponentUtils.formatList(collection)), false);
      }

      return collection.size();
   }

   private static int listTrackedPlayerScores(CommandSourceStack commandsourcestack, String s) {
      Map<Objective, Score> map = commandsourcestack.getServer().getScoreboard().getPlayerScores(s);
      if (map.isEmpty()) {
         commandsourcestack.sendSuccess(() -> Component.translatable("commands.scoreboard.players.list.entity.empty", s), false);
      } else {
         commandsourcestack.sendSuccess(() -> Component.translatable("commands.scoreboard.players.list.entity.success", s, map.size()), false);

         for(Map.Entry<Objective, Score> map_entry : map.entrySet()) {
            commandsourcestack.sendSuccess(() -> Component.translatable("commands.scoreboard.players.list.entity.entry", map_entry.getKey().getFormattedDisplayName(), map_entry.getValue().getScore()), false);
         }
      }

      return map.size();
   }

   private static int clearDisplaySlot(CommandSourceStack commandsourcestack, int i) throws CommandSyntaxException {
      Scoreboard scoreboard = commandsourcestack.getServer().getScoreboard();
      if (scoreboard.getDisplayObjective(i) == null) {
         throw ERROR_DISPLAY_SLOT_ALREADY_EMPTY.create();
      } else {
         scoreboard.setDisplayObjective(i, (Objective)null);
         commandsourcestack.sendSuccess(() -> Component.translatable("commands.scoreboard.objectives.display.cleared", Scoreboard.getDisplaySlotNames()[i]), true);
         return 0;
      }
   }

   private static int setDisplaySlot(CommandSourceStack commandsourcestack, int i, Objective objective) throws CommandSyntaxException {
      Scoreboard scoreboard = commandsourcestack.getServer().getScoreboard();
      if (scoreboard.getDisplayObjective(i) == objective) {
         throw ERROR_DISPLAY_SLOT_ALREADY_SET.create();
      } else {
         scoreboard.setDisplayObjective(i, objective);
         commandsourcestack.sendSuccess(() -> Component.translatable("commands.scoreboard.objectives.display.set", Scoreboard.getDisplaySlotNames()[i], objective.getDisplayName()), true);
         return 0;
      }
   }

   private static int setDisplayName(CommandSourceStack commandsourcestack, Objective objective, Component component) {
      if (!objective.getDisplayName().equals(component)) {
         objective.setDisplayName(component);
         commandsourcestack.sendSuccess(() -> Component.translatable("commands.scoreboard.objectives.modify.displayname", objective.getName(), objective.getFormattedDisplayName()), true);
      }

      return 0;
   }

   private static int setRenderType(CommandSourceStack commandsourcestack, Objective objective, ObjectiveCriteria.RenderType objectivecriteria_rendertype) {
      if (objective.getRenderType() != objectivecriteria_rendertype) {
         objective.setRenderType(objectivecriteria_rendertype);
         commandsourcestack.sendSuccess(() -> Component.translatable("commands.scoreboard.objectives.modify.rendertype", objective.getFormattedDisplayName()), true);
      }

      return 0;
   }

   private static int removeObjective(CommandSourceStack commandsourcestack, Objective objective) {
      Scoreboard scoreboard = commandsourcestack.getServer().getScoreboard();
      scoreboard.removeObjective(objective);
      commandsourcestack.sendSuccess(() -> Component.translatable("commands.scoreboard.objectives.remove.success", objective.getFormattedDisplayName()), true);
      return scoreboard.getObjectives().size();
   }

   private static int addObjective(CommandSourceStack commandsourcestack, String s, ObjectiveCriteria objectivecriteria, Component component) throws CommandSyntaxException {
      Scoreboard scoreboard = commandsourcestack.getServer().getScoreboard();
      if (scoreboard.getObjective(s) != null) {
         throw ERROR_OBJECTIVE_ALREADY_EXISTS.create();
      } else {
         scoreboard.addObjective(s, objectivecriteria, component, objectivecriteria.getDefaultRenderType());
         Objective objective = scoreboard.getObjective(s);
         commandsourcestack.sendSuccess(() -> Component.translatable("commands.scoreboard.objectives.add.success", objective.getFormattedDisplayName()), true);
         return scoreboard.getObjectives().size();
      }
   }

   private static int listObjectives(CommandSourceStack commandsourcestack) {
      Collection<Objective> collection = commandsourcestack.getServer().getScoreboard().getObjectives();
      if (collection.isEmpty()) {
         commandsourcestack.sendSuccess(() -> Component.translatable("commands.scoreboard.objectives.list.empty"), false);
      } else {
         commandsourcestack.sendSuccess(() -> Component.translatable("commands.scoreboard.objectives.list.success", collection.size(), ComponentUtils.formatList(collection, Objective::getFormattedDisplayName)), false);
      }

      return collection.size();
   }
}
