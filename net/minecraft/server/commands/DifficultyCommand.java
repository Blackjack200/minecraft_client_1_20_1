package net.minecraft.server.commands;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.brigadier.exceptions.DynamicCommandExceptionType;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.network.chat.Component;
import net.minecraft.server.MinecraftServer;
import net.minecraft.world.Difficulty;

public class DifficultyCommand {
   private static final DynamicCommandExceptionType ERROR_ALREADY_DIFFICULT = new DynamicCommandExceptionType((object) -> Component.translatable("commands.difficulty.failure", object));

   public static void register(CommandDispatcher<CommandSourceStack> commanddispatcher) {
      LiteralArgumentBuilder<CommandSourceStack> literalargumentbuilder = Commands.literal("difficulty");

      for(Difficulty difficulty : Difficulty.values()) {
         literalargumentbuilder.then(Commands.literal(difficulty.getKey()).executes((commandcontext1) -> setDifficulty(commandcontext1.getSource(), difficulty)));
      }

      commanddispatcher.register(literalargumentbuilder.requires((commandsourcestack) -> commandsourcestack.hasPermission(2)).executes((commandcontext) -> {
         Difficulty difficulty1 = commandcontext.getSource().getLevel().getDifficulty();
         commandcontext.getSource().sendSuccess(() -> Component.translatable("commands.difficulty.query", difficulty1.getDisplayName()), false);
         return difficulty1.getId();
      }));
   }

   public static int setDifficulty(CommandSourceStack commandsourcestack, Difficulty difficulty) throws CommandSyntaxException {
      MinecraftServer minecraftserver = commandsourcestack.getServer();
      if (minecraftserver.getWorldData().getDifficulty() == difficulty) {
         throw ERROR_ALREADY_DIFFICULT.create(difficulty.getKey());
      } else {
         minecraftserver.setDifficulty(difficulty, true);
         commandsourcestack.sendSuccess(() -> Component.translatable("commands.difficulty.success", difficulty.getDisplayName()), true);
         return 0;
      }
   }
}
