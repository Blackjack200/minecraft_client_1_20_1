package net.minecraft.server.commands;

import com.google.common.collect.Lists;
import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.arguments.IntegerArgumentType;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.brigadier.exceptions.SimpleCommandExceptionType;
import com.mojang.brigadier.suggestion.Suggestions;
import com.mojang.brigadier.suggestion.SuggestionsBuilder;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.commands.SharedSuggestionProvider;
import net.minecraft.commands.arguments.ObjectiveArgument;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.scores.Objective;
import net.minecraft.world.scores.Score;
import net.minecraft.world.scores.Scoreboard;
import net.minecraft.world.scores.criteria.ObjectiveCriteria;

public class TriggerCommand {
   private static final SimpleCommandExceptionType ERROR_NOT_PRIMED = new SimpleCommandExceptionType(Component.translatable("commands.trigger.failed.unprimed"));
   private static final SimpleCommandExceptionType ERROR_INVALID_OBJECTIVE = new SimpleCommandExceptionType(Component.translatable("commands.trigger.failed.invalid"));

   public static void register(CommandDispatcher<CommandSourceStack> commanddispatcher) {
      commanddispatcher.register(Commands.literal("trigger").then(Commands.argument("objective", ObjectiveArgument.objective()).suggests((commandcontext3, suggestionsbuilder) -> suggestObjectives(commandcontext3.getSource(), suggestionsbuilder)).executes((commandcontext2) -> simpleTrigger(commandcontext2.getSource(), getScore(commandcontext2.getSource().getPlayerOrException(), ObjectiveArgument.getObjective(commandcontext2, "objective")))).then(Commands.literal("add").then(Commands.argument("value", IntegerArgumentType.integer()).executes((commandcontext1) -> addValue(commandcontext1.getSource(), getScore(commandcontext1.getSource().getPlayerOrException(), ObjectiveArgument.getObjective(commandcontext1, "objective")), IntegerArgumentType.getInteger(commandcontext1, "value"))))).then(Commands.literal("set").then(Commands.argument("value", IntegerArgumentType.integer()).executes((commandcontext) -> setValue(commandcontext.getSource(), getScore(commandcontext.getSource().getPlayerOrException(), ObjectiveArgument.getObjective(commandcontext, "objective")), IntegerArgumentType.getInteger(commandcontext, "value")))))));
   }

   public static CompletableFuture<Suggestions> suggestObjectives(CommandSourceStack commandsourcestack, SuggestionsBuilder suggestionsbuilder) {
      Entity entity = commandsourcestack.getEntity();
      List<String> list = Lists.newArrayList();
      if (entity != null) {
         Scoreboard scoreboard = commandsourcestack.getServer().getScoreboard();
         String s = entity.getScoreboardName();

         for(Objective objective : scoreboard.getObjectives()) {
            if (objective.getCriteria() == ObjectiveCriteria.TRIGGER && scoreboard.hasPlayerScore(s, objective)) {
               Score score = scoreboard.getOrCreatePlayerScore(s, objective);
               if (!score.isLocked()) {
                  list.add(objective.getName());
               }
            }
         }
      }

      return SharedSuggestionProvider.suggest(list, suggestionsbuilder);
   }

   private static int addValue(CommandSourceStack commandsourcestack, Score score, int i) {
      score.add(i);
      commandsourcestack.sendSuccess(() -> Component.translatable("commands.trigger.add.success", score.getObjective().getFormattedDisplayName(), i), true);
      return score.getScore();
   }

   private static int setValue(CommandSourceStack commandsourcestack, Score score, int i) {
      score.setScore(i);
      commandsourcestack.sendSuccess(() -> Component.translatable("commands.trigger.set.success", score.getObjective().getFormattedDisplayName(), i), true);
      return i;
   }

   private static int simpleTrigger(CommandSourceStack commandsourcestack, Score score) {
      score.add(1);
      commandsourcestack.sendSuccess(() -> Component.translatable("commands.trigger.simple.success", score.getObjective().getFormattedDisplayName()), true);
      return score.getScore();
   }

   private static Score getScore(ServerPlayer serverplayer, Objective objective) throws CommandSyntaxException {
      if (objective.getCriteria() != ObjectiveCriteria.TRIGGER) {
         throw ERROR_INVALID_OBJECTIVE.create();
      } else {
         Scoreboard scoreboard = serverplayer.getScoreboard();
         String s = serverplayer.getScoreboardName();
         if (!scoreboard.hasPlayerScore(s, objective)) {
            throw ERROR_NOT_PRIMED.create();
         } else {
            Score score = scoreboard.getOrCreatePlayerScore(s, objective);
            if (score.isLocked()) {
               throw ERROR_NOT_PRIMED.create();
            } else {
               score.setLocked(true);
               return score;
            }
         }
      }
   }
}
