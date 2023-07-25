package net.minecraft.server.commands;

import com.mojang.brigadier.CommandDispatcher;
import java.util.Collection;
import java.util.Collections;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.commands.arguments.AngleArgument;
import net.minecraft.commands.arguments.EntityArgument;
import net.minecraft.commands.arguments.coordinates.BlockPosArgument;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceKey;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.level.Level;

public class SetSpawnCommand {
   public static void register(CommandDispatcher<CommandSourceStack> commanddispatcher) {
      commanddispatcher.register(Commands.literal("spawnpoint").requires((commandsourcestack) -> commandsourcestack.hasPermission(2)).executes((commandcontext3) -> setSpawn(commandcontext3.getSource(), Collections.singleton(commandcontext3.getSource().getPlayerOrException()), BlockPos.containing(commandcontext3.getSource().getPosition()), 0.0F)).then(Commands.argument("targets", EntityArgument.players()).executes((commandcontext2) -> setSpawn(commandcontext2.getSource(), EntityArgument.getPlayers(commandcontext2, "targets"), BlockPos.containing(commandcontext2.getSource().getPosition()), 0.0F)).then(Commands.argument("pos", BlockPosArgument.blockPos()).executes((commandcontext1) -> setSpawn(commandcontext1.getSource(), EntityArgument.getPlayers(commandcontext1, "targets"), BlockPosArgument.getSpawnablePos(commandcontext1, "pos"), 0.0F)).then(Commands.argument("angle", AngleArgument.angle()).executes((commandcontext) -> setSpawn(commandcontext.getSource(), EntityArgument.getPlayers(commandcontext, "targets"), BlockPosArgument.getSpawnablePos(commandcontext, "pos"), AngleArgument.getAngle(commandcontext, "angle")))))));
   }

   private static int setSpawn(CommandSourceStack commandsourcestack, Collection<ServerPlayer> collection, BlockPos blockpos, float f) {
      ResourceKey<Level> resourcekey = commandsourcestack.getLevel().dimension();

      for(ServerPlayer serverplayer : collection) {
         serverplayer.setRespawnPosition(resourcekey, blockpos, f, true, false);
      }

      String s = resourcekey.location().toString();
      if (collection.size() == 1) {
         commandsourcestack.sendSuccess(() -> Component.translatable("commands.spawnpoint.success.single", blockpos.getX(), blockpos.getY(), blockpos.getZ(), f, s, collection.iterator().next().getDisplayName()), true);
      } else {
         commandsourcestack.sendSuccess(() -> Component.translatable("commands.spawnpoint.success.multiple", blockpos.getX(), blockpos.getY(), blockpos.getZ(), f, s, collection.size()), true);
      }

      return collection.size();
   }
}
