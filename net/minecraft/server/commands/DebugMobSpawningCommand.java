package net.minecraft.server.commands;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.commands.arguments.coordinates.BlockPosArgument;
import net.minecraft.core.BlockPos;
import net.minecraft.world.entity.MobCategory;
import net.minecraft.world.level.NaturalSpawner;

public class DebugMobSpawningCommand {
   public static void register(CommandDispatcher<CommandSourceStack> commanddispatcher) {
      LiteralArgumentBuilder<CommandSourceStack> literalargumentbuilder = Commands.literal("debugmobspawning").requires((commandsourcestack) -> commandsourcestack.hasPermission(2));

      for(MobCategory mobcategory : MobCategory.values()) {
         literalargumentbuilder.then(Commands.literal(mobcategory.getName()).then(Commands.argument("at", BlockPosArgument.blockPos()).executes((commandcontext) -> spawnMobs(commandcontext.getSource(), mobcategory, BlockPosArgument.getLoadedBlockPos(commandcontext, "at")))));
      }

      commanddispatcher.register(literalargumentbuilder);
   }

   private static int spawnMobs(CommandSourceStack commandsourcestack, MobCategory mobcategory, BlockPos blockpos) {
      NaturalSpawner.spawnCategoryForPosition(mobcategory, commandsourcestack.getLevel(), blockpos);
      return 1;
   }
}
