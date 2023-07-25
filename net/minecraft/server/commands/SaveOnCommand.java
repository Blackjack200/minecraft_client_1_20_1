package net.minecraft.server.commands;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.exceptions.SimpleCommandExceptionType;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;

public class SaveOnCommand {
   private static final SimpleCommandExceptionType ERROR_ALREADY_ON = new SimpleCommandExceptionType(Component.translatable("commands.save.alreadyOn"));

   public static void register(CommandDispatcher<CommandSourceStack> commanddispatcher) {
      commanddispatcher.register(Commands.literal("save-on").requires((commandsourcestack1) -> commandsourcestack1.hasPermission(4)).executes((commandcontext) -> {
         CommandSourceStack commandsourcestack = commandcontext.getSource();
         boolean flag = false;

         for(ServerLevel serverlevel : commandsourcestack.getServer().getAllLevels()) {
            if (serverlevel != null && serverlevel.noSave) {
               serverlevel.noSave = false;
               flag = true;
            }
         }

         if (!flag) {
            throw ERROR_ALREADY_ON.create();
         } else {
            commandsourcestack.sendSuccess(() -> Component.translatable("commands.save.enabled"), true);
            return 1;
         }
      }));
   }
}
