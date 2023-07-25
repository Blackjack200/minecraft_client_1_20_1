package net.minecraft.server.commands;

import com.google.common.net.InetAddresses;
import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.brigadier.exceptions.SimpleCommandExceptionType;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.commands.SharedSuggestionProvider;
import net.minecraft.network.chat.Component;
import net.minecraft.server.players.IpBanList;

public class PardonIpCommand {
   private static final SimpleCommandExceptionType ERROR_INVALID = new SimpleCommandExceptionType(Component.translatable("commands.pardonip.invalid"));
   private static final SimpleCommandExceptionType ERROR_NOT_BANNED = new SimpleCommandExceptionType(Component.translatable("commands.pardonip.failed"));

   public static void register(CommandDispatcher<CommandSourceStack> commanddispatcher) {
      commanddispatcher.register(Commands.literal("pardon-ip").requires((commandsourcestack) -> commandsourcestack.hasPermission(3)).then(Commands.argument("target", StringArgumentType.word()).suggests((commandcontext1, suggestionsbuilder) -> SharedSuggestionProvider.suggest(commandcontext1.getSource().getServer().getPlayerList().getIpBans().getUserList(), suggestionsbuilder)).executes((commandcontext) -> unban(commandcontext.getSource(), StringArgumentType.getString(commandcontext, "target")))));
   }

   private static int unban(CommandSourceStack commandsourcestack, String s) throws CommandSyntaxException {
      if (!InetAddresses.isInetAddress(s)) {
         throw ERROR_INVALID.create();
      } else {
         IpBanList ipbanlist = commandsourcestack.getServer().getPlayerList().getIpBans();
         if (!ipbanlist.isBanned(s)) {
            throw ERROR_NOT_BANNED.create();
         } else {
            ipbanlist.remove(s);
            commandsourcestack.sendSuccess(() -> Component.translatable("commands.pardonip.success", s), true);
            return 1;
         }
      }
   }
}
