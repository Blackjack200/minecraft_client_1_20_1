package net.minecraft.server.commands;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.arguments.IntegerArgumentType;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;

public class ReturnCommand {
   public static void register(CommandDispatcher<CommandSourceStack> commanddispatcher) {
      commanddispatcher.register(Commands.literal("return").requires((commandsourcestack) -> commandsourcestack.hasPermission(2)).then(Commands.argument("value", IntegerArgumentType.integer()).executes((commandcontext) -> setReturn(commandcontext.getSource(), IntegerArgumentType.getInteger(commandcontext, "value")))));
   }

   private static int setReturn(CommandSourceStack commandsourcestack, int i) {
      commandsourcestack.getReturnValueConsumer().accept(i);
      return i;
   }
}
