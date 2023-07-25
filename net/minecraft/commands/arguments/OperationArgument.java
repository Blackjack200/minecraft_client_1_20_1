package net.minecraft.commands.arguments;

import com.mojang.brigadier.StringReader;
import com.mojang.brigadier.arguments.ArgumentType;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.brigadier.exceptions.SimpleCommandExceptionType;
import com.mojang.brigadier.suggestion.Suggestions;
import com.mojang.brigadier.suggestion.SuggestionsBuilder;
import java.util.Arrays;
import java.util.Collection;
import java.util.concurrent.CompletableFuture;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.SharedSuggestionProvider;
import net.minecraft.network.chat.Component;
import net.minecraft.util.Mth;
import net.minecraft.world.scores.Score;

public class OperationArgument implements ArgumentType<OperationArgument.Operation> {
   private static final Collection<String> EXAMPLES = Arrays.asList("=", ">", "<");
   private static final SimpleCommandExceptionType ERROR_INVALID_OPERATION = new SimpleCommandExceptionType(Component.translatable("arguments.operation.invalid"));
   private static final SimpleCommandExceptionType ERROR_DIVIDE_BY_ZERO = new SimpleCommandExceptionType(Component.translatable("arguments.operation.div0"));

   public static OperationArgument operation() {
      return new OperationArgument();
   }

   public static OperationArgument.Operation getOperation(CommandContext<CommandSourceStack> commandcontext, String s) {
      return commandcontext.getArgument(s, OperationArgument.Operation.class);
   }

   public OperationArgument.Operation parse(StringReader stringreader) throws CommandSyntaxException {
      if (!stringreader.canRead()) {
         throw ERROR_INVALID_OPERATION.create();
      } else {
         int i = stringreader.getCursor();

         while(stringreader.canRead() && stringreader.peek() != ' ') {
            stringreader.skip();
         }

         return getOperation(stringreader.getString().substring(i, stringreader.getCursor()));
      }
   }

   public <S> CompletableFuture<Suggestions> listSuggestions(CommandContext<S> commandcontext, SuggestionsBuilder suggestionsbuilder) {
      return SharedSuggestionProvider.suggest(new String[]{"=", "+=", "-=", "*=", "/=", "%=", "<", ">", "><"}, suggestionsbuilder);
   }

   public Collection<String> getExamples() {
      return EXAMPLES;
   }

   private static OperationArgument.Operation getOperation(String s) throws CommandSyntaxException {
      return (OperationArgument.Operation)(s.equals("><") ? (score, score1) -> {
         int i = score.getScore();
         score.setScore(score1.getScore());
         score1.setScore(i);
      } : getSimpleOperation(s));
   }

   private static OperationArgument.SimpleOperation getSimpleOperation(String s) throws CommandSyntaxException {
      switch (s) {
         case "=":
            return (k2, l2) -> l2;
         case "+=":
            return (i2, j2) -> i2 + j2;
         case "-=":
            return (k1, l1) -> k1 - l1;
         case "*=":
            return (i1, j1) -> i1 * j1;
         case "/=":
            return (k, l) -> {
               if (l == 0) {
                  throw ERROR_DIVIDE_BY_ZERO.create();
               } else {
                  return Mth.floorDiv(k, l);
               }
            };
         case "%=":
            return (i, j) -> {
               if (j == 0) {
                  throw ERROR_DIVIDE_BY_ZERO.create();
               } else {
                  return Mth.positiveModulo(i, j);
               }
            };
         case "<":
            return Math::min;
         case ">":
            return Math::max;
         default:
            throw ERROR_INVALID_OPERATION.create();
      }
   }

   @FunctionalInterface
   public interface Operation {
      void apply(Score score, Score score1) throws CommandSyntaxException;
   }

   @FunctionalInterface
   interface SimpleOperation extends OperationArgument.Operation {
      int apply(int i, int j) throws CommandSyntaxException;

      default void apply(Score score, Score score1) throws CommandSyntaxException {
         score.setScore(this.apply(score.getScore(), score1.getScore()));
      }
   }
}
