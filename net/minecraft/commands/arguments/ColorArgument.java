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
import net.minecraft.ChatFormatting;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.SharedSuggestionProvider;
import net.minecraft.network.chat.Component;

public class ColorArgument implements ArgumentType<ChatFormatting> {
   private static final Collection<String> EXAMPLES = Arrays.asList("red", "green");
   public static final DynamicCommandExceptionType ERROR_INVALID_VALUE = new DynamicCommandExceptionType((object) -> Component.translatable("argument.color.invalid", object));

   private ColorArgument() {
   }

   public static ColorArgument color() {
      return new ColorArgument();
   }

   public static ChatFormatting getColor(CommandContext<CommandSourceStack> commandcontext, String s) {
      return commandcontext.getArgument(s, ChatFormatting.class);
   }

   public ChatFormatting parse(StringReader stringreader) throws CommandSyntaxException {
      String s = stringreader.readUnquotedString();
      ChatFormatting chatformatting = ChatFormatting.getByName(s);
      if (chatformatting != null && !chatformatting.isFormat()) {
         return chatformatting;
      } else {
         throw ERROR_INVALID_VALUE.create(s);
      }
   }

   public <S> CompletableFuture<Suggestions> listSuggestions(CommandContext<S> commandcontext, SuggestionsBuilder suggestionsbuilder) {
      return SharedSuggestionProvider.suggest(ChatFormatting.getNames(true, false), suggestionsbuilder);
   }

   public Collection<String> getExamples() {
      return EXAMPLES;
   }
}
