package net.minecraft.server.commands;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.exceptions.SimpleCommandExceptionType;
import com.mojang.brigadier.tree.LiteralCommandNode;
import java.util.List;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.commands.arguments.MessageArgument;
import net.minecraft.network.chat.ChatType;
import net.minecraft.network.chat.ClickEvent;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.HoverEvent;
import net.minecraft.network.chat.OutgoingChatMessage;
import net.minecraft.network.chat.PlayerChatMessage;
import net.minecraft.network.chat.Style;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.server.players.PlayerList;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.scores.PlayerTeam;

public class TeamMsgCommand {
   private static final Style SUGGEST_STYLE = Style.EMPTY.withHoverEvent(new HoverEvent(HoverEvent.Action.SHOW_TEXT, Component.translatable("chat.type.team.hover"))).withClickEvent(new ClickEvent(ClickEvent.Action.SUGGEST_COMMAND, "/teammsg "));
   private static final SimpleCommandExceptionType ERROR_NOT_ON_TEAM = new SimpleCommandExceptionType(Component.translatable("commands.teammsg.failed.noteam"));

   public static void register(CommandDispatcher<CommandSourceStack> commanddispatcher) {
      LiteralCommandNode<CommandSourceStack> literalcommandnode = commanddispatcher.register(Commands.literal("teammsg").then(Commands.argument("message", MessageArgument.message()).executes((commandcontext) -> {
         CommandSourceStack commandsourcestack = commandcontext.getSource();
         Entity entity = commandsourcestack.getEntityOrException();
         PlayerTeam playerteam = (PlayerTeam)entity.getTeam();
         if (playerteam == null) {
            throw ERROR_NOT_ON_TEAM.create();
         } else {
            List<ServerPlayer> list = commandsourcestack.getServer().getPlayerList().getPlayers().stream().filter((serverplayer) -> serverplayer == entity || serverplayer.getTeam() == playerteam).toList();
            if (!list.isEmpty()) {
               MessageArgument.resolveChatMessage(commandcontext, "message", (playerchatmessage) -> sendMessage(commandsourcestack, entity, playerteam, list, playerchatmessage));
            }

            return list.size();
         }
      })));
      commanddispatcher.register(Commands.literal("tm").redirect(literalcommandnode));
   }

   private static void sendMessage(CommandSourceStack commandsourcestack, Entity entity, PlayerTeam playerteam, List<ServerPlayer> list, PlayerChatMessage playerchatmessage) {
      Component component = playerteam.getFormattedDisplayName().withStyle(SUGGEST_STYLE);
      ChatType.Bound chattype_bound = ChatType.bind(ChatType.TEAM_MSG_COMMAND_INCOMING, commandsourcestack).withTargetName(component);
      ChatType.Bound chattype_bound1 = ChatType.bind(ChatType.TEAM_MSG_COMMAND_OUTGOING, commandsourcestack).withTargetName(component);
      OutgoingChatMessage outgoingchatmessage = OutgoingChatMessage.create(playerchatmessage);
      boolean flag = false;

      for(ServerPlayer serverplayer : list) {
         ChatType.Bound chattype_bound2 = serverplayer == entity ? chattype_bound1 : chattype_bound;
         boolean flag1 = commandsourcestack.shouldFilterMessageTo(serverplayer);
         serverplayer.sendChatMessage(outgoingchatmessage, flag1, chattype_bound2);
         flag |= flag1 && playerchatmessage.isFullyFiltered();
      }

      if (flag) {
         commandsourcestack.sendSystemMessage(PlayerList.CHAT_FILTERED_FULL);
      }

   }
}
