package net.minecraft.server.commands;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.brigadier.exceptions.SimpleCommandExceptionType;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.commands.arguments.coordinates.BlockPosArgument;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.network.protocol.game.DebugPackets;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.ai.navigation.GroundPathNavigation;
import net.minecraft.world.entity.ai.navigation.PathNavigation;
import net.minecraft.world.level.pathfinder.Path;

public class DebugPathCommand {
   private static final SimpleCommandExceptionType ERROR_NOT_MOB = new SimpleCommandExceptionType(Component.literal("Source is not a mob"));
   private static final SimpleCommandExceptionType ERROR_NO_PATH = new SimpleCommandExceptionType(Component.literal("Path not found"));
   private static final SimpleCommandExceptionType ERROR_NOT_COMPLETE = new SimpleCommandExceptionType(Component.literal("Target not reached"));

   public static void register(CommandDispatcher<CommandSourceStack> commanddispatcher) {
      commanddispatcher.register(Commands.literal("debugpath").requires((commandsourcestack) -> commandsourcestack.hasPermission(2)).then(Commands.argument("to", BlockPosArgument.blockPos()).executes((commandcontext) -> fillBlocks(commandcontext.getSource(), BlockPosArgument.getLoadedBlockPos(commandcontext, "to")))));
   }

   private static int fillBlocks(CommandSourceStack commandsourcestack, BlockPos blockpos) throws CommandSyntaxException {
      Entity entity = commandsourcestack.getEntity();
      if (!(entity instanceof Mob mob)) {
         throw ERROR_NOT_MOB.create();
      } else {
         PathNavigation pathnavigation = new GroundPathNavigation(mob, commandsourcestack.getLevel());
         Path path = pathnavigation.createPath(blockpos, 0);
         DebugPackets.sendPathFindingPacket(commandsourcestack.getLevel(), mob, path, pathnavigation.getMaxDistanceToWaypoint());
         if (path == null) {
            throw ERROR_NO_PATH.create();
         } else if (!path.canReach()) {
            throw ERROR_NOT_COMPLETE.create();
         } else {
            commandsourcestack.sendSuccess(() -> Component.literal("Made path"), true);
            return 1;
         }
      }
   }
}
