package net.minecraft.server.commands;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.arguments.IntegerArgumentType;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.commands.arguments.TimeArgument;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;

public class TimeCommand {
   public static void register(CommandDispatcher<CommandSourceStack> commanddispatcher) {
      commanddispatcher.register(Commands.literal("time").requires((commandsourcestack) -> commandsourcestack.hasPermission(2)).then(Commands.literal("set").then(Commands.literal("day").executes((commandcontext8) -> setTime(commandcontext8.getSource(), 1000))).then(Commands.literal("noon").executes((commandcontext7) -> setTime(commandcontext7.getSource(), 6000))).then(Commands.literal("night").executes((commandcontext6) -> setTime(commandcontext6.getSource(), 13000))).then(Commands.literal("midnight").executes((commandcontext5) -> setTime(commandcontext5.getSource(), 18000))).then(Commands.argument("time", TimeArgument.time()).executes((commandcontext4) -> setTime(commandcontext4.getSource(), IntegerArgumentType.getInteger(commandcontext4, "time"))))).then(Commands.literal("add").then(Commands.argument("time", TimeArgument.time()).executes((commandcontext3) -> addTime(commandcontext3.getSource(), IntegerArgumentType.getInteger(commandcontext3, "time"))))).then(Commands.literal("query").then(Commands.literal("daytime").executes((commandcontext2) -> queryTime(commandcontext2.getSource(), getDayTime(commandcontext2.getSource().getLevel())))).then(Commands.literal("gametime").executes((commandcontext1) -> queryTime(commandcontext1.getSource(), (int)(commandcontext1.getSource().getLevel().getGameTime() % 2147483647L)))).then(Commands.literal("day").executes((commandcontext) -> queryTime(commandcontext.getSource(), (int)(commandcontext.getSource().getLevel().getDayTime() / 24000L % 2147483647L))))));
   }

   private static int getDayTime(ServerLevel serverlevel) {
      return (int)(serverlevel.getDayTime() % 24000L);
   }

   private static int queryTime(CommandSourceStack commandsourcestack, int i) {
      commandsourcestack.sendSuccess(() -> Component.translatable("commands.time.query", i), false);
      return i;
   }

   public static int setTime(CommandSourceStack commandsourcestack, int i) {
      for(ServerLevel serverlevel : commandsourcestack.getServer().getAllLevels()) {
         serverlevel.setDayTime((long)i);
      }

      commandsourcestack.sendSuccess(() -> Component.translatable("commands.time.set", i), true);
      return getDayTime(commandsourcestack.getLevel());
   }

   public static int addTime(CommandSourceStack commandsourcestack, int i) {
      for(ServerLevel serverlevel : commandsourcestack.getServer().getAllLevels()) {
         serverlevel.setDayTime(serverlevel.getDayTime() + (long)i);
      }

      int j = getDayTime(commandsourcestack.getLevel());
      commandsourcestack.sendSuccess(() -> Component.translatable("commands.time.set", j), true);
      return j;
   }
}
