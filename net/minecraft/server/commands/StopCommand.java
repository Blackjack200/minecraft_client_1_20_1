package net.minecraft.server.commands;

import com.mojang.brigadier.CommandDispatcher;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.network.chat.Component;

public class StopCommand {
   public static void register(CommandDispatcher<CommandSourceStack> commanddispatcher) {
      commanddispatcher.register(Commands.literal("stop").requires((commandsourcestack) -> commandsourcestack.hasPermission(4)).executes((commandcontext) -> {
         commandcontext.getSource().sendSuccess(() -> Component.translatable("commands.stop.stopping"), true);
         commandcontext.getSource().getServer().halt(false);
         return 1;
      }));
   }
}
