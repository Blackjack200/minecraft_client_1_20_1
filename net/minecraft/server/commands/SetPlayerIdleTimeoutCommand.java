package net.minecraft.server.commands;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.arguments.IntegerArgumentType;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.network.chat.Component;

public class SetPlayerIdleTimeoutCommand {
   public static void register(CommandDispatcher<CommandSourceStack> commanddispatcher) {
      commanddispatcher.register(Commands.literal("setidletimeout").requires((commandsourcestack) -> commandsourcestack.hasPermission(3)).then(Commands.argument("minutes", IntegerArgumentType.integer(0)).executes((commandcontext) -> setIdleTimeout(commandcontext.getSource(), IntegerArgumentType.getInteger(commandcontext, "minutes")))));
   }

   private static int setIdleTimeout(CommandSourceStack commandsourcestack, int i) {
      commandsourcestack.getServer().setPlayerIdleTimeout(i);
      commandsourcestack.sendSuccess(() -> Component.translatable("commands.setidletimeout.success", i), true);
      return i;
   }
}
