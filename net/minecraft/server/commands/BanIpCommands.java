package net.minecraft.server.commands;

import com.google.common.net.InetAddresses;
import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.brigadier.exceptions.SimpleCommandExceptionType;
import java.util.Date;
import java.util.List;
import javax.annotation.Nullable;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.commands.arguments.MessageArgument;
import net.minecraft.commands.arguments.selector.EntitySelector;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.server.players.IpBanList;
import net.minecraft.server.players.IpBanListEntry;

public class BanIpCommands {
   private static final SimpleCommandExceptionType ERROR_INVALID_IP = new SimpleCommandExceptionType(Component.translatable("commands.banip.invalid"));
   private static final SimpleCommandExceptionType ERROR_ALREADY_BANNED = new SimpleCommandExceptionType(Component.translatable("commands.banip.failed"));

   public static void register(CommandDispatcher<CommandSourceStack> commanddispatcher) {
      commanddispatcher.register(Commands.literal("ban-ip").requires((commandsourcestack) -> commandsourcestack.hasPermission(3)).then(Commands.argument("target", StringArgumentType.word()).executes((commandcontext1) -> banIpOrName(commandcontext1.getSource(), StringArgumentType.getString(commandcontext1, "target"), (Component)null)).then(Commands.argument("reason", MessageArgument.message()).executes((commandcontext) -> banIpOrName(commandcontext.getSource(), StringArgumentType.getString(commandcontext, "target"), MessageArgument.getMessage(commandcontext, "reason"))))));
   }

   private static int banIpOrName(CommandSourceStack commandsourcestack, String s, @Nullable Component component) throws CommandSyntaxException {
      if (InetAddresses.isInetAddress(s)) {
         return banIp(commandsourcestack, s, component);
      } else {
         ServerPlayer serverplayer = commandsourcestack.getServer().getPlayerList().getPlayerByName(s);
         if (serverplayer != null) {
            return banIp(commandsourcestack, serverplayer.getIpAddress(), component);
         } else {
            throw ERROR_INVALID_IP.create();
         }
      }
   }

   private static int banIp(CommandSourceStack commandsourcestack, String s, @Nullable Component component) throws CommandSyntaxException {
      IpBanList ipbanlist = commandsourcestack.getServer().getPlayerList().getIpBans();
      if (ipbanlist.isBanned(s)) {
         throw ERROR_ALREADY_BANNED.create();
      } else {
         List<ServerPlayer> list = commandsourcestack.getServer().getPlayerList().getPlayersWithAddress(s);
         IpBanListEntry ipbanlistentry = new IpBanListEntry(s, (Date)null, commandsourcestack.getTextName(), (Date)null, component == null ? null : component.getString());
         ipbanlist.add(ipbanlistentry);
         commandsourcestack.sendSuccess(() -> Component.translatable("commands.banip.success", s, ipbanlistentry.getReason()), true);
         if (!list.isEmpty()) {
            commandsourcestack.sendSuccess(() -> Component.translatable("commands.banip.info", list.size(), EntitySelector.joinNames(list)), true);
         }

         for(ServerPlayer serverplayer : list) {
            serverplayer.connection.disconnect(Component.translatable("multiplayer.disconnect.ip_banned"));
         }

         return list.size();
      }
   }
}
