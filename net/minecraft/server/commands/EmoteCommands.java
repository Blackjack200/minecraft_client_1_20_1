package net.minecraft.server.commands;

import com.mojang.brigadier.CommandDispatcher;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.commands.arguments.MessageArgument;
import net.minecraft.network.chat.ChatType;
import net.minecraft.server.players.PlayerList;

public class EmoteCommands {
   public static void register(CommandDispatcher<CommandSourceStack> commanddispatcher) {
      commanddispatcher.register(Commands.literal("me").then(Commands.argument("action", MessageArgument.message()).executes((commandcontext) -> {
         MessageArgument.resolveChatMessage(commandcontext, "action", (playerchatmessage) -> {
            CommandSourceStack commandsourcestack = commandcontext.getSource();
            PlayerList playerlist = commandsourcestack.getServer().getPlayerList();
            playerlist.broadcastChatMessage(playerchatmessage, commandsourcestack, ChatType.bind(ChatType.EMOTE_COMMAND, commandsourcestack));
         });
         return 1;
      })));
   }
}
