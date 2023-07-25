package net.minecraft.server.commands;

import com.mojang.brigadier.CommandDispatcher;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.ComponentUtils;

public class SeedCommand {
   public static void register(CommandDispatcher<CommandSourceStack> commanddispatcher, boolean flag) {
      commanddispatcher.register(Commands.literal("seed").requires((commandsourcestack) -> !flag || commandsourcestack.hasPermission(2)).executes((commandcontext) -> {
         long i = commandcontext.getSource().getLevel().getSeed();
         Component component = ComponentUtils.copyOnClickText(String.valueOf(i));
         commandcontext.getSource().sendSuccess(() -> Component.translatable("commands.seed.success", component), false);
         return (int)i;
      }));
   }
}
