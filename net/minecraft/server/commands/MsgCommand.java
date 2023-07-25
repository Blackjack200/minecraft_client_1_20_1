package net.minecraft.server.commands;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.tree.LiteralCommandNode;
import java.util.Collection;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.commands.arguments.EntityArgument;
import net.minecraft.commands.arguments.MessageArgument;
import net.minecraft.network.chat.ChatType;
import net.minecraft.network.chat.OutgoingChatMessage;
import net.minecraft.network.chat.PlayerChatMessage;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.server.players.PlayerList;

public class MsgCommand {
   public static void register(CommandDispatcher<CommandSourceStack> commanddispatcher) {
      LiteralCommandNode<CommandSourceStack> literalcommandnode = commanddispatcher.register(Commands.literal("msg").then(Commands.argument("targets", EntityArgument.players()).then(Commands.argument("message", MessageArgument.message()).executes((commandcontext) -> {
         Collection<ServerPlayer> collection = EntityArgument.getPlayers(commandcontext, "targets");
         if (!collection.isEmpty()) {
            MessageArgument.resolveChatMessage(commandcontext, "message", (playerchatmessage) -> sendMessage(commandcontext.getSource(), collection, playerchatmessage));
         }

         return collection.size();
      }))));
      commanddispatcher.register(Commands.literal("tell").redirect(literalcommandnode));
      commanddispatcher.register(Commands.literal("w").redirect(literalcommandnode));
   }

   private static void sendMessage(CommandSourceStack commandsourcestack, Collection<ServerPlayer> collection, PlayerChatMessage playerchatmessage) {
      ChatType.Bound chattype_bound = ChatType.bind(ChatType.MSG_COMMAND_INCOMING, commandsourcestack);
      OutgoingChatMessage outgoingchatmessage = OutgoingChatMessage.create(playerchatmessage);
      boolean flag = false;

      for(ServerPlayer serverplayer : collection) {
         ChatType.Bound chattype_bound1 = ChatType.bind(ChatType.MSG_COMMAND_OUTGOING, commandsourcestack).withTargetName(serverplayer.getDisplayName());
         commandsourcestack.sendChatMessage(outgoingchatmessage, false, chattype_bound1);
         boolean flag1 = commandsourcestack.shouldFilterMessageTo(serverplayer);
         serverplayer.sendChatMessage(outgoingchatmessage, flag1, chattype_bound);
         flag |= flag1 && playerchatmessage.isFullyFiltered();
      }

      if (flag) {
         commandsourcestack.sendSystemMessage(PlayerList.CHAT_FILTERED_FULL);
      }

   }
}
