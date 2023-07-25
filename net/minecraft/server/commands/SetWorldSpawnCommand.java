package net.minecraft.server.commands;

import com.mojang.brigadier.CommandDispatcher;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.commands.arguments.AngleArgument;
import net.minecraft.commands.arguments.coordinates.BlockPosArgument;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;

public class SetWorldSpawnCommand {
   public static void register(CommandDispatcher<CommandSourceStack> commanddispatcher) {
      commanddispatcher.register(Commands.literal("setworldspawn").requires((commandsourcestack) -> commandsourcestack.hasPermission(2)).executes((commandcontext2) -> setSpawn(commandcontext2.getSource(), BlockPos.containing(commandcontext2.getSource().getPosition()), 0.0F)).then(Commands.argument("pos", BlockPosArgument.blockPos()).executes((commandcontext1) -> setSpawn(commandcontext1.getSource(), BlockPosArgument.getSpawnablePos(commandcontext1, "pos"), 0.0F)).then(Commands.argument("angle", AngleArgument.angle()).executes((commandcontext) -> setSpawn(commandcontext.getSource(), BlockPosArgument.getSpawnablePos(commandcontext, "pos"), AngleArgument.getAngle(commandcontext, "angle"))))));
   }

   private static int setSpawn(CommandSourceStack commandsourcestack, BlockPos blockpos, float f) {
      commandsourcestack.getLevel().setDefaultSpawnPos(blockpos, f);
      commandsourcestack.sendSuccess(() -> Component.translatable("commands.setworldspawn.success", blockpos.getX(), blockpos.getY(), blockpos.getZ(), f), true);
      return 1;
   }
}
