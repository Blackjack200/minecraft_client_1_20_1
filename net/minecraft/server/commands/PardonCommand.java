package net.minecraft.server.commands;

import com.mojang.authlib.GameProfile;
import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.brigadier.exceptions.SimpleCommandExceptionType;
import java.util.Collection;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.commands.SharedSuggestionProvider;
import net.minecraft.commands.arguments.GameProfileArgument;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.ComponentUtils;
import net.minecraft.server.players.UserBanList;

public class PardonCommand {
   private static final SimpleCommandExceptionType ERROR_NOT_BANNED = new SimpleCommandExceptionType(Component.translatable("commands.pardon.failed"));

   public static void register(CommandDispatcher<CommandSourceStack> commanddispatcher) {
      commanddispatcher.register(Commands.literal("pardon").requires((commandsourcestack) -> commandsourcestack.hasPermission(3)).then(Commands.argument("targets", GameProfileArgument.gameProfile()).suggests((commandcontext1, suggestionsbuilder) -> SharedSuggestionProvider.suggest(commandcontext1.getSource().getServer().getPlayerList().getBans().getUserList(), suggestionsbuilder)).executes((commandcontext) -> pardonPlayers(commandcontext.getSource(), GameProfileArgument.getGameProfiles(commandcontext, "targets")))));
   }

   private static int pardonPlayers(CommandSourceStack commandsourcestack, Collection<GameProfile> collection) throws CommandSyntaxException {
      UserBanList userbanlist = commandsourcestack.getServer().getPlayerList().getBans();
      int i = 0;

      for(GameProfile gameprofile : collection) {
         if (userbanlist.isBanned(gameprofile)) {
            userbanlist.remove(gameprofile);
            ++i;
            commandsourcestack.sendSuccess(() -> Component.translatable("commands.pardon.success", ComponentUtils.getDisplayName(gameprofile)), true);
         }
      }

      if (i == 0) {
         throw ERROR_NOT_BANNED.create();
      } else {
         return i;
      }
   }
}
