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
import net.minecraft.server.players.PlayerList;

public class DeOpCommands {
   private static final SimpleCommandExceptionType ERROR_NOT_OP = new SimpleCommandExceptionType(Component.translatable("commands.deop.failed"));

   public static void register(CommandDispatcher<CommandSourceStack> commanddispatcher) {
      commanddispatcher.register(Commands.literal("deop").requires((commandsourcestack) -> commandsourcestack.hasPermission(3)).then(Commands.argument("targets", GameProfileArgument.gameProfile()).suggests((commandcontext1, suggestionsbuilder) -> SharedSuggestionProvider.suggest(commandcontext1.getSource().getServer().getPlayerList().getOpNames(), suggestionsbuilder)).executes((commandcontext) -> deopPlayers(commandcontext.getSource(), GameProfileArgument.getGameProfiles(commandcontext, "targets")))));
   }

   private static int deopPlayers(CommandSourceStack commandsourcestack, Collection<GameProfile> collection) throws CommandSyntaxException {
      PlayerList playerlist = commandsourcestack.getServer().getPlayerList();
      int i = 0;

      for(GameProfile gameprofile : collection) {
         if (playerlist.isOp(gameprofile)) {
            playerlist.deop(gameprofile);
            ++i;
            commandsourcestack.sendSuccess(() -> Component.translatable("commands.deop.success", collection.iterator().next().getName()), true);
         }
      }

      if (i == 0) {
         throw ERROR_NOT_OP.create();
      } else {
         commandsourcestack.getServer().kickUnlistedPlayers(commandsourcestack);
         return i;
      }
   }
}
