package net.minecraft.server.commands;

import com.mojang.brigadier.CommandDispatcher;
import java.util.Collection;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.commands.arguments.EntityArgument;
import net.minecraft.commands.arguments.MessageArgument;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;

public class KickCommand {
   public static void register(CommandDispatcher<CommandSourceStack> commanddispatcher) {
      commanddispatcher.register(Commands.literal("kick").requires((commandsourcestack) -> commandsourcestack.hasPermission(3)).then(Commands.argument("targets", EntityArgument.players()).executes((commandcontext1) -> kickPlayers(commandcontext1.getSource(), EntityArgument.getPlayers(commandcontext1, "targets"), Component.translatable("multiplayer.disconnect.kicked"))).then(Commands.argument("reason", MessageArgument.message()).executes((commandcontext) -> kickPlayers(commandcontext.getSource(), EntityArgument.getPlayers(commandcontext, "targets"), MessageArgument.getMessage(commandcontext, "reason"))))));
   }

   private static int kickPlayers(CommandSourceStack commandsourcestack, Collection<ServerPlayer> collection, Component component) {
      for(ServerPlayer serverplayer : collection) {
         serverplayer.connection.disconnect(component);
         commandsourcestack.sendSuccess(() -> Component.translatable("commands.kick.success", serverplayer.getDisplayName(), component), true);
      }

      return collection.size();
   }
}
