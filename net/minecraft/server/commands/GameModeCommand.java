package net.minecraft.server.commands;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.context.CommandContext;
import java.util.Collection;
import java.util.Collections;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.commands.arguments.EntityArgument;
import net.minecraft.commands.arguments.GameModeArgument;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.level.GameRules;
import net.minecraft.world.level.GameType;

public class GameModeCommand {
   public static final int PERMISSION_LEVEL = 2;

   public static void register(CommandDispatcher<CommandSourceStack> commanddispatcher) {
      commanddispatcher.register(Commands.literal("gamemode").requires((commandsourcestack) -> commandsourcestack.hasPermission(2)).then(Commands.argument("gamemode", GameModeArgument.gameMode()).executes((commandcontext1) -> setMode(commandcontext1, Collections.singleton(commandcontext1.getSource().getPlayerOrException()), GameModeArgument.getGameMode(commandcontext1, "gamemode"))).then(Commands.argument("target", EntityArgument.players()).executes((commandcontext) -> setMode(commandcontext, EntityArgument.getPlayers(commandcontext, "target"), GameModeArgument.getGameMode(commandcontext, "gamemode"))))));
   }

   private static void logGamemodeChange(CommandSourceStack commandsourcestack, ServerPlayer serverplayer, GameType gametype) {
      Component component = Component.translatable("gameMode." + gametype.getName());
      if (commandsourcestack.getEntity() == serverplayer) {
         commandsourcestack.sendSuccess(() -> Component.translatable("commands.gamemode.success.self", component), true);
      } else {
         if (commandsourcestack.getLevel().getGameRules().getBoolean(GameRules.RULE_SENDCOMMANDFEEDBACK)) {
            serverplayer.sendSystemMessage(Component.translatable("gameMode.changed", component));
         }

         commandsourcestack.sendSuccess(() -> Component.translatable("commands.gamemode.success.other", serverplayer.getDisplayName(), component), true);
      }

   }

   private static int setMode(CommandContext<CommandSourceStack> commandcontext, Collection<ServerPlayer> collection, GameType gametype) {
      int i = 0;

      for(ServerPlayer serverplayer : collection) {
         if (serverplayer.setGameMode(gametype)) {
            logGamemodeChange(commandcontext.getSource(), serverplayer, gametype);
            ++i;
         }
      }

      return i;
   }
}
