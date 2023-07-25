package net.minecraft.server.commands;

import com.google.common.collect.ImmutableList;
import com.mojang.brigadier.CommandDispatcher;
import java.util.Collection;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.commands.arguments.EntityArgument;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.Entity;

public class KillCommand {
   public static void register(CommandDispatcher<CommandSourceStack> commanddispatcher) {
      commanddispatcher.register(Commands.literal("kill").requires((commandsourcestack) -> commandsourcestack.hasPermission(2)).executes((commandcontext1) -> kill(commandcontext1.getSource(), ImmutableList.of(commandcontext1.getSource().getEntityOrException()))).then(Commands.argument("targets", EntityArgument.entities()).executes((commandcontext) -> kill(commandcontext.getSource(), EntityArgument.getEntities(commandcontext, "targets")))));
   }

   private static int kill(CommandSourceStack commandsourcestack, Collection<? extends Entity> collection) {
      for(Entity entity : collection) {
         entity.kill();
      }

      if (collection.size() == 1) {
         commandsourcestack.sendSuccess(() -> Component.translatable("commands.kill.success.single", collection.iterator().next().getDisplayName()), true);
      } else {
         commandsourcestack.sendSuccess(() -> Component.translatable("commands.kill.success.multiple", collection.size()), true);
      }

      return collection.size();
   }
}
