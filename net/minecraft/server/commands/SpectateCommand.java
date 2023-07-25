package net.minecraft.server.commands;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.brigadier.exceptions.DynamicCommandExceptionType;
import com.mojang.brigadier.exceptions.SimpleCommandExceptionType;
import javax.annotation.Nullable;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.commands.arguments.EntityArgument;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.level.GameType;

public class SpectateCommand {
   private static final SimpleCommandExceptionType ERROR_SELF = new SimpleCommandExceptionType(Component.translatable("commands.spectate.self"));
   private static final DynamicCommandExceptionType ERROR_NOT_SPECTATOR = new DynamicCommandExceptionType((object) -> Component.translatable("commands.spectate.not_spectator", object));

   public static void register(CommandDispatcher<CommandSourceStack> commanddispatcher) {
      commanddispatcher.register(Commands.literal("spectate").requires((commandsourcestack) -> commandsourcestack.hasPermission(2)).executes((commandcontext2) -> spectate(commandcontext2.getSource(), (Entity)null, commandcontext2.getSource().getPlayerOrException())).then(Commands.argument("target", EntityArgument.entity()).executes((commandcontext1) -> spectate(commandcontext1.getSource(), EntityArgument.getEntity(commandcontext1, "target"), commandcontext1.getSource().getPlayerOrException())).then(Commands.argument("player", EntityArgument.player()).executes((commandcontext) -> spectate(commandcontext.getSource(), EntityArgument.getEntity(commandcontext, "target"), EntityArgument.getPlayer(commandcontext, "player"))))));
   }

   private static int spectate(CommandSourceStack commandsourcestack, @Nullable Entity entity, ServerPlayer serverplayer) throws CommandSyntaxException {
      if (serverplayer == entity) {
         throw ERROR_SELF.create();
      } else if (serverplayer.gameMode.getGameModeForPlayer() != GameType.SPECTATOR) {
         throw ERROR_NOT_SPECTATOR.create(serverplayer.getDisplayName());
      } else {
         serverplayer.setCamera(entity);
         if (entity != null) {
            commandsourcestack.sendSuccess(() -> Component.translatable("commands.spectate.success.started", entity.getDisplayName()), false);
         } else {
            commandsourcestack.sendSuccess(() -> Component.translatable("commands.spectate.success.stopped"), false);
         }

         return 1;
      }
   }
}
