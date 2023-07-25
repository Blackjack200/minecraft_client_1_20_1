package net.minecraft.server.commands;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.brigadier.exceptions.SimpleCommandExceptionType;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.network.chat.Component;
import net.minecraft.server.MinecraftServer;

public class SaveAllCommand {
   private static final SimpleCommandExceptionType ERROR_FAILED = new SimpleCommandExceptionType(Component.translatable("commands.save.failed"));

   public static void register(CommandDispatcher<CommandSourceStack> commanddispatcher) {
      commanddispatcher.register(Commands.literal("save-all").requires((commandsourcestack) -> commandsourcestack.hasPermission(4)).executes((commandcontext1) -> saveAll(commandcontext1.getSource(), false)).then(Commands.literal("flush").executes((commandcontext) -> saveAll(commandcontext.getSource(), true))));
   }

   private static int saveAll(CommandSourceStack commandsourcestack, boolean flag) throws CommandSyntaxException {
      commandsourcestack.sendSuccess(() -> Component.translatable("commands.save.saving"), false);
      MinecraftServer minecraftserver = commandsourcestack.getServer();
      boolean flag1 = minecraftserver.saveEverything(true, flag, true);
      if (!flag1) {
         throw ERROR_FAILED.create();
      } else {
         commandsourcestack.sendSuccess(() -> Component.translatable("commands.save.success"), true);
         return 1;
      }
   }
}
