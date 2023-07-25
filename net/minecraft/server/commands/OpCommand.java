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

public class OpCommand {
   private static final SimpleCommandExceptionType ERROR_ALREADY_OP = new SimpleCommandExceptionType(Component.translatable("commands.op.failed"));

   public static void register(CommandDispatcher<CommandSourceStack> commanddispatcher) {
      commanddispatcher.register(Commands.literal("op").requires((commandsourcestack) -> commandsourcestack.hasPermission(3)).then(Commands.argument("targets", GameProfileArgument.gameProfile()).suggests((commandcontext1, suggestionsbuilder) -> {
         PlayerList playerlist = commandcontext1.getSource().getServer().getPlayerList();
         return SharedSuggestionProvider.suggest(playerlist.getPlayers().stream().filter((serverplayer1) -> !playerlist.isOp(serverplayer1.getGameProfile())).map((serverplayer) -> serverplayer.getGameProfile().getName()), suggestionsbuilder);
      }).executes((commandcontext) -> opPlayers(commandcontext.getSource(), GameProfileArgument.getGameProfiles(commandcontext, "targets")))));
   }

   private static int opPlayers(CommandSourceStack commandsourcestack, Collection<GameProfile> collection) throws CommandSyntaxException {
      PlayerList playerlist = commandsourcestack.getServer().getPlayerList();
      int i = 0;

      for(GameProfile gameprofile : collection) {
         if (!playerlist.isOp(gameprofile)) {
            playerlist.op(gameprofile);
            ++i;
            commandsourcestack.sendSuccess(() -> Component.translatable("commands.op.success", collection.iterator().next().getName()), true);
         }
      }

      if (i == 0) {
         throw ERROR_ALREADY_OP.create();
      } else {
         return i;
      }
   }
}
