package net.minecraft.commands.arguments;

import com.mojang.brigadier.StringReader;
import com.mojang.brigadier.arguments.ArgumentType;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.brigadier.exceptions.DynamicCommandExceptionType;
import com.mojang.brigadier.suggestion.Suggestions;
import com.mojang.brigadier.suggestion.SuggestionsBuilder;
import java.util.Arrays;
import java.util.Collection;
import java.util.concurrent.CompletableFuture;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.SharedSuggestionProvider;
import net.minecraft.network.chat.Component;
import net.minecraft.world.scores.Objective;
import net.minecraft.world.scores.Scoreboard;

public class ObjectiveArgument implements ArgumentType<String> {
   private static final Collection<String> EXAMPLES = Arrays.asList("foo", "*", "012");
   private static final DynamicCommandExceptionType ERROR_OBJECTIVE_NOT_FOUND = new DynamicCommandExceptionType((object) -> Component.translatable("arguments.objective.notFound", object));
   private static final DynamicCommandExceptionType ERROR_OBJECTIVE_READ_ONLY = new DynamicCommandExceptionType((object) -> Component.translatable("arguments.objective.readonly", object));

   public static ObjectiveArgument objective() {
      return new ObjectiveArgument();
   }

   public static Objective getObjective(CommandContext<CommandSourceStack> commandcontext, String s) throws CommandSyntaxException {
      String s1 = commandcontext.getArgument(s, String.class);
      Scoreboard scoreboard = commandcontext.getSource().getServer().getScoreboard();
      Objective objective = scoreboard.getObjective(s1);
      if (objective == null) {
         throw ERROR_OBJECTIVE_NOT_FOUND.create(s1);
      } else {
         return objective;
      }
   }

   public static Objective getWritableObjective(CommandContext<CommandSourceStack> commandcontext, String s) throws CommandSyntaxException {
      Objective objective = getObjective(commandcontext, s);
      if (objective.getCriteria().isReadOnly()) {
         throw ERROR_OBJECTIVE_READ_ONLY.create(objective.getName());
      } else {
         return objective;
      }
   }

   public String parse(StringReader stringreader) throws CommandSyntaxException {
      return stringreader.readUnquotedString();
   }

   public <S> CompletableFuture<Suggestions> listSuggestions(CommandContext<S> commandcontext, SuggestionsBuilder suggestionsbuilder) {
      S object = commandcontext.getSource();
      if (object instanceof CommandSourceStack commandsourcestack) {
         return SharedSuggestionProvider.suggest(commandsourcestack.getServer().getScoreboard().getObjectiveNames(), suggestionsbuilder);
      } else if (object instanceof SharedSuggestionProvider sharedsuggestionprovider) {
         return sharedsuggestionprovider.customSuggestion(commandcontext);
      } else {
         return Suggestions.empty();
      }
   }

   public Collection<String> getExamples() {
      return EXAMPLES;
   }
}
