package net.minecraft.server.commands;

import com.mojang.brigadier.CommandDispatcher;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.commands.arguments.ComponentArgument;
import net.minecraft.commands.arguments.EntityArgument;
import net.minecraft.network.chat.ComponentUtils;
import net.minecraft.server.level.ServerPlayer;

public class TellRawCommand {
   public static void register(CommandDispatcher<CommandSourceStack> commanddispatcher) {
      commanddispatcher.register(Commands.literal("tellraw").requires((commandsourcestack) -> commandsourcestack.hasPermission(2)).then(Commands.argument("targets", EntityArgument.players()).then(Commands.argument("message", ComponentArgument.textComponent()).executes((commandcontext) -> {
         int i = 0;

         for(ServerPlayer serverplayer : EntityArgument.getPlayers(commandcontext, "targets")) {
            serverplayer.sendSystemMessage(ComponentUtils.updateForEntity(commandcontext.getSource(), ComponentArgument.getComponent(commandcontext, "message"), serverplayer, 0), false);
            ++i;
         }

         return i;
      }))));
   }
}
