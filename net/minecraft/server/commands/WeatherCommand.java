package net.minecraft.server.commands;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.arguments.IntegerArgumentType;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.commands.arguments.TimeArgument;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.valueproviders.IntProvider;

public class WeatherCommand {
   private static final int DEFAULT_TIME = -1;

   public static void register(CommandDispatcher<CommandSourceStack> commanddispatcher) {
      commanddispatcher.register(Commands.literal("weather").requires((commandsourcestack) -> commandsourcestack.hasPermission(2)).then(Commands.literal("clear").executes((commandcontext5) -> setClear(commandcontext5.getSource(), -1)).then(Commands.argument("duration", TimeArgument.time(1)).executes((commandcontext4) -> setClear(commandcontext4.getSource(), IntegerArgumentType.getInteger(commandcontext4, "duration"))))).then(Commands.literal("rain").executes((commandcontext3) -> setRain(commandcontext3.getSource(), -1)).then(Commands.argument("duration", TimeArgument.time(1)).executes((commandcontext2) -> setRain(commandcontext2.getSource(), IntegerArgumentType.getInteger(commandcontext2, "duration"))))).then(Commands.literal("thunder").executes((commandcontext1) -> setThunder(commandcontext1.getSource(), -1)).then(Commands.argument("duration", TimeArgument.time(1)).executes((commandcontext) -> setThunder(commandcontext.getSource(), IntegerArgumentType.getInteger(commandcontext, "duration"))))));
   }

   private static int getDuration(CommandSourceStack commandsourcestack, int i, IntProvider intprovider) {
      return i == -1 ? intprovider.sample(commandsourcestack.getLevel().getRandom()) : i;
   }

   private static int setClear(CommandSourceStack commandsourcestack, int i) {
      commandsourcestack.getLevel().setWeatherParameters(getDuration(commandsourcestack, i, ServerLevel.RAIN_DELAY), 0, false, false);
      commandsourcestack.sendSuccess(() -> Component.translatable("commands.weather.set.clear"), true);
      return i;
   }

   private static int setRain(CommandSourceStack commandsourcestack, int i) {
      commandsourcestack.getLevel().setWeatherParameters(0, getDuration(commandsourcestack, i, ServerLevel.RAIN_DURATION), true, false);
      commandsourcestack.sendSuccess(() -> Component.translatable("commands.weather.set.rain"), true);
      return i;
   }

   private static int setThunder(CommandSourceStack commandsourcestack, int i) {
      commandsourcestack.getLevel().setWeatherParameters(0, getDuration(commandsourcestack, i, ServerLevel.THUNDER_DURATION), true, true);
      commandsourcestack.sendSuccess(() -> Component.translatable("commands.weather.set.thunder"), true);
      return i;
   }
}
