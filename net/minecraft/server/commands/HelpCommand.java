package net.minecraft.server.commands;

import com.google.common.collect.Iterables;
import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.ParseResults;
import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.exceptions.SimpleCommandExceptionType;
import com.mojang.brigadier.tree.CommandNode;
import java.util.Map;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.network.chat.Component;

public class HelpCommand {
   private static final SimpleCommandExceptionType ERROR_FAILED = new SimpleCommandExceptionType(Component.translatable("commands.help.failed"));

   public static void register(CommandDispatcher<CommandSourceStack> commanddispatcher) {
      commanddispatcher.register(Commands.literal("help").executes((commandcontext1) -> {
         Map<CommandNode<CommandSourceStack>, String> map1 = commanddispatcher.getSmartUsage(commanddispatcher.getRoot(), commandcontext1.getSource());

         for(String s2 : map1.values()) {
            commandcontext1.getSource().sendSuccess(() -> Component.literal("/" + s2), false);
         }

         return map1.size();
      }).then(Commands.argument("command", StringArgumentType.greedyString()).executes((commandcontext) -> {
         ParseResults<CommandSourceStack> parseresults = commanddispatcher.parse(StringArgumentType.getString(commandcontext, "command"), commandcontext.getSource());
         if (parseresults.getContext().getNodes().isEmpty()) {
            throw ERROR_FAILED.create();
         } else {
            Map<CommandNode<CommandSourceStack>, String> map = commanddispatcher.getSmartUsage(Iterables.getLast(parseresults.getContext().getNodes()).getNode(), commandcontext.getSource());

            for(String s : map.values()) {
               commandcontext.getSource().sendSuccess(() -> Component.literal("/" + parseresults.getReader().getString() + " " + s), false);
            }

            return map.size();
         }
      })));
   }
}
