package net.minecraft.server.commands;

import com.google.common.collect.ImmutableList;
import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.arguments.IntegerArgumentType;
import java.util.Collection;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.monster.warden.WardenSpawnTracker;
import net.minecraft.world.entity.player.Player;

public class WardenSpawnTrackerCommand {
   public static void register(CommandDispatcher<CommandSourceStack> commanddispatcher) {
      commanddispatcher.register(Commands.literal("warden_spawn_tracker").requires((commandsourcestack) -> commandsourcestack.hasPermission(2)).then(Commands.literal("clear").executes((commandcontext1) -> resetTracker(commandcontext1.getSource(), ImmutableList.of(commandcontext1.getSource().getPlayerOrException())))).then(Commands.literal("set").then(Commands.argument("warning_level", IntegerArgumentType.integer(0, 4)).executes((commandcontext) -> setWarningLevel(commandcontext.getSource(), ImmutableList.of(commandcontext.getSource().getPlayerOrException()), IntegerArgumentType.getInteger(commandcontext, "warning_level"))))));
   }

   private static int setWarningLevel(CommandSourceStack commandsourcestack, Collection<? extends Player> collection, int i) {
      for(Player player : collection) {
         player.getWardenSpawnTracker().ifPresent((wardenspawntracker) -> wardenspawntracker.setWarningLevel(i));
      }

      if (collection.size() == 1) {
         commandsourcestack.sendSuccess(() -> Component.translatable("commands.warden_spawn_tracker.set.success.single", collection.iterator().next().getDisplayName()), true);
      } else {
         commandsourcestack.sendSuccess(() -> Component.translatable("commands.warden_spawn_tracker.set.success.multiple", collection.size()), true);
      }

      return collection.size();
   }

   private static int resetTracker(CommandSourceStack commandsourcestack, Collection<? extends Player> collection) {
      for(Player player : collection) {
         player.getWardenSpawnTracker().ifPresent(WardenSpawnTracker::reset);
      }

      if (collection.size() == 1) {
         commandsourcestack.sendSuccess(() -> Component.translatable("commands.warden_spawn_tracker.clear.success.single", collection.iterator().next().getDisplayName()), true);
      } else {
         commandsourcestack.sendSuccess(() -> Component.translatable("commands.warden_spawn_tracker.clear.success.multiple", collection.size()), true);
      }

      return collection.size();
   }
}
