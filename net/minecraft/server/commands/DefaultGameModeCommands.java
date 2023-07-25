package net.minecraft.server.commands;

import com.mojang.brigadier.CommandDispatcher;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.commands.arguments.GameModeArgument;
import net.minecraft.network.chat.Component;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.level.GameType;

public class DefaultGameModeCommands {
   public static void register(CommandDispatcher<CommandSourceStack> commanddispatcher) {
      commanddispatcher.register(Commands.literal("defaultgamemode").requires((commandsourcestack) -> commandsourcestack.hasPermission(2)).then(Commands.argument("gamemode", GameModeArgument.gameMode()).executes((commandcontext) -> setMode(commandcontext.getSource(), GameModeArgument.getGameMode(commandcontext, "gamemode")))));
   }

   private static int setMode(CommandSourceStack commandsourcestack, GameType gametype) {
      int i = 0;
      MinecraftServer minecraftserver = commandsourcestack.getServer();
      minecraftserver.setDefaultGameType(gametype);
      GameType gametype1 = minecraftserver.getForcedGameType();
      if (gametype1 != null) {
         for(ServerPlayer serverplayer : minecraftserver.getPlayerList().getPlayers()) {
            if (serverplayer.setGameMode(gametype1)) {
               ++i;
            }
         }
      }

      commandsourcestack.sendSuccess(() -> Component.translatable("commands.defaultgamemode.success", gametype.getLongDisplayName()), true);
      return i;
   }
}
