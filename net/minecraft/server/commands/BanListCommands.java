package net.minecraft.server.commands;

import com.google.common.collect.Iterables;
import com.google.common.collect.Lists;
import com.mojang.brigadier.CommandDispatcher;
import java.util.Collection;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.network.chat.Component;
import net.minecraft.server.players.BanListEntry;
import net.minecraft.server.players.PlayerList;

public class BanListCommands {
   public static void register(CommandDispatcher<CommandSourceStack> commanddispatcher) {
      commanddispatcher.register(Commands.literal("banlist").requires((commandsourcestack) -> commandsourcestack.hasPermission(3)).executes((commandcontext2) -> {
         PlayerList playerlist = commandcontext2.getSource().getServer().getPlayerList();
         return showList(commandcontext2.getSource(), Lists.newArrayList(Iterables.concat(playerlist.getBans().getEntries(), playerlist.getIpBans().getEntries())));
      }).then(Commands.literal("ips").executes((commandcontext1) -> showList(commandcontext1.getSource(), commandcontext1.getSource().getServer().getPlayerList().getIpBans().getEntries()))).then(Commands.literal("players").executes((commandcontext) -> showList(commandcontext.getSource(), commandcontext.getSource().getServer().getPlayerList().getBans().getEntries()))));
   }

   private static int showList(CommandSourceStack commandsourcestack, Collection<? extends BanListEntry<?>> collection) {
      if (collection.isEmpty()) {
         commandsourcestack.sendSuccess(() -> Component.translatable("commands.banlist.none"), false);
      } else {
         commandsourcestack.sendSuccess(() -> Component.translatable("commands.banlist.list", collection.size()), false);

         for(BanListEntry<?> banlistentry : collection) {
            commandsourcestack.sendSuccess(() -> Component.translatable("commands.banlist.entry", banlistentry.getDisplayName(), banlistentry.getSource(), banlistentry.getReason()), false);
         }
      }

      return collection.size();
   }
}
