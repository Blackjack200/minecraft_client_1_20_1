package net.minecraft.server.commands;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.suggestion.SuggestionProvider;
import java.util.Collection;
import java.util.OptionalInt;
import net.minecraft.commands.CommandFunction;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.commands.SharedSuggestionProvider;
import net.minecraft.commands.arguments.item.FunctionArgument;
import net.minecraft.network.chat.Component;
import net.minecraft.server.ServerFunctionManager;
import org.apache.commons.lang3.mutable.MutableObject;

public class FunctionCommand {
   public static final SuggestionProvider<CommandSourceStack> SUGGEST_FUNCTION = (commandcontext, suggestionsbuilder) -> {
      ServerFunctionManager serverfunctionmanager = commandcontext.getSource().getServer().getFunctions();
      SharedSuggestionProvider.suggestResource(serverfunctionmanager.getTagNames(), suggestionsbuilder, "#");
      return SharedSuggestionProvider.suggestResource(serverfunctionmanager.getFunctionNames(), suggestionsbuilder);
   };

   public static void register(CommandDispatcher<CommandSourceStack> commanddispatcher) {
      commanddispatcher.register(Commands.literal("function").requires((commandsourcestack) -> commandsourcestack.hasPermission(2)).then(Commands.argument("name", FunctionArgument.functions()).suggests(SUGGEST_FUNCTION).executes((commandcontext) -> runFunction(commandcontext.getSource(), FunctionArgument.getFunctions(commandcontext, "name")))));
   }

   private static int runFunction(CommandSourceStack commandsourcestack, Collection<CommandFunction> collection) {
      int i = 0;
      boolean flag = false;

      for(CommandFunction commandfunction : collection) {
         MutableObject<OptionalInt> mutableobject = new MutableObject<>(OptionalInt.empty());
         int j = commandsourcestack.getServer().getFunctions().execute(commandfunction, commandsourcestack.withSuppressedOutput().withMaximumPermission(2).withReturnValueConsumer((k1) -> mutableobject.setValue(OptionalInt.of(k1))));
         OptionalInt optionalint = mutableobject.getValue();
         i += optionalint.orElse(j);
         flag |= optionalint.isPresent();
      }

      int k = i;
      if (collection.size() == 1) {
         if (flag) {
            commandsourcestack.sendSuccess(() -> Component.translatable("commands.function.success.single.result", k, collection.iterator().next().getId()), true);
         } else {
            commandsourcestack.sendSuccess(() -> Component.translatable("commands.function.success.single", k, collection.iterator().next().getId()), true);
         }
      } else if (flag) {
         commandsourcestack.sendSuccess(() -> Component.translatable("commands.function.success.multiple.result", collection.size()), true);
      } else {
         commandsourcestack.sendSuccess(() -> Component.translatable("commands.function.success.multiple", k, collection.size()), true);
      }

      return i;
   }
}
