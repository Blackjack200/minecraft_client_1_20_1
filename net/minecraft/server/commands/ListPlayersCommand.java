package net.minecraft.server.commands;

import com.mojang.brigadier.CommandDispatcher;
import java.util.List;
import java.util.function.Function;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.ComponentUtils;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.server.players.PlayerList;
import net.minecraft.world.entity.player.Player;

public class ListPlayersCommand {
   public static void register(CommandDispatcher<CommandSourceStack> commanddispatcher) {
      commanddispatcher.register(Commands.literal("list").executes((commandcontext1) -> listPlayers(commandcontext1.getSource())).then(Commands.literal("uuids").executes((commandcontext) -> listPlayersWithUuids(commandcontext.getSource()))));
   }

   private static int listPlayers(CommandSourceStack commandsourcestack) {
      return format(commandsourcestack, Player::getDisplayName);
   }

   private static int listPlayersWithUuids(CommandSourceStack commandsourcestack) {
      return format(commandsourcestack, (serverplayer) -> Component.translatable("commands.list.nameAndId", serverplayer.getName(), serverplayer.getGameProfile().getId()));
   }

   private static int format(CommandSourceStack commandsourcestack, Function<ServerPlayer, Component> function) {
      PlayerList playerlist = commandsourcestack.getServer().getPlayerList();
      List<ServerPlayer> list = playerlist.getPlayers();
      Component component = ComponentUtils.formatList(list, function);
      commandsourcestack.sendSuccess(() -> Component.translatable("commands.list.players", list.size(), playerlist.getMaxPlayers(), component), false);
      return list.size();
   }
}
