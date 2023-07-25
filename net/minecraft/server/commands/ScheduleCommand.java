package net.minecraft.server.commands;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.arguments.IntegerArgumentType;
import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.brigadier.exceptions.DynamicCommandExceptionType;
import com.mojang.brigadier.exceptions.SimpleCommandExceptionType;
import com.mojang.brigadier.suggestion.SuggestionProvider;
import com.mojang.datafixers.util.Either;
import com.mojang.datafixers.util.Pair;
import java.util.Collection;
import net.minecraft.commands.CommandFunction;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.commands.SharedSuggestionProvider;
import net.minecraft.commands.arguments.TimeArgument;
import net.minecraft.commands.arguments.item.FunctionArgument;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.MinecraftServer;
import net.minecraft.world.level.timers.FunctionCallback;
import net.minecraft.world.level.timers.FunctionTagCallback;
import net.minecraft.world.level.timers.TimerQueue;

public class ScheduleCommand {
   private static final SimpleCommandExceptionType ERROR_SAME_TICK = new SimpleCommandExceptionType(Component.translatable("commands.schedule.same_tick"));
   private static final DynamicCommandExceptionType ERROR_CANT_REMOVE = new DynamicCommandExceptionType((object) -> Component.translatable("commands.schedule.cleared.failure", object));
   private static final SuggestionProvider<CommandSourceStack> SUGGEST_SCHEDULE = (commandcontext, suggestionsbuilder) -> SharedSuggestionProvider.suggest(commandcontext.getSource().getServer().getWorldData().overworldData().getScheduledEvents().getEventsIds(), suggestionsbuilder);

   public static void register(CommandDispatcher<CommandSourceStack> commanddispatcher) {
      commanddispatcher.register(Commands.literal("schedule").requires((commandsourcestack) -> commandsourcestack.hasPermission(2)).then(Commands.literal("function").then(Commands.argument("function", FunctionArgument.functions()).suggests(FunctionCommand.SUGGEST_FUNCTION).then(Commands.argument("time", TimeArgument.time()).executes((commandcontext3) -> schedule(commandcontext3.getSource(), FunctionArgument.getFunctionOrTag(commandcontext3, "function"), IntegerArgumentType.getInteger(commandcontext3, "time"), true)).then(Commands.literal("append").executes((commandcontext2) -> schedule(commandcontext2.getSource(), FunctionArgument.getFunctionOrTag(commandcontext2, "function"), IntegerArgumentType.getInteger(commandcontext2, "time"), false))).then(Commands.literal("replace").executes((commandcontext1) -> schedule(commandcontext1.getSource(), FunctionArgument.getFunctionOrTag(commandcontext1, "function"), IntegerArgumentType.getInteger(commandcontext1, "time"), true)))))).then(Commands.literal("clear").then(Commands.argument("function", StringArgumentType.greedyString()).suggests(SUGGEST_SCHEDULE).executes((commandcontext) -> remove(commandcontext.getSource(), StringArgumentType.getString(commandcontext, "function"))))));
   }

   private static int schedule(CommandSourceStack commandsourcestack, Pair<ResourceLocation, Either<CommandFunction, Collection<CommandFunction>>> pair, int i, boolean flag) throws CommandSyntaxException {
      if (i == 0) {
         throw ERROR_SAME_TICK.create();
      } else {
         long j = commandsourcestack.getLevel().getGameTime() + (long)i;
         ResourceLocation resourcelocation = pair.getFirst();
         TimerQueue<MinecraftServer> timerqueue = commandsourcestack.getServer().getWorldData().overworldData().getScheduledEvents();
         pair.getSecond().ifLeft((commandfunction) -> {
            String s1 = resourcelocation.toString();
            if (flag) {
               timerqueue.remove(s1);
            }

            timerqueue.schedule(s1, j, new FunctionCallback(resourcelocation));
            commandsourcestack.sendSuccess(() -> Component.translatable("commands.schedule.created.function", resourcelocation, i, j), true);
         }).ifRight((collection) -> {
            String s = "#" + resourcelocation;
            if (flag) {
               timerqueue.remove(s);
            }

            timerqueue.schedule(s, j, new FunctionTagCallback(resourcelocation));
            commandsourcestack.sendSuccess(() -> Component.translatable("commands.schedule.created.tag", resourcelocation, i, j), true);
         });
         return Math.floorMod(j, Integer.MAX_VALUE);
      }
   }

   private static int remove(CommandSourceStack commandsourcestack, String s) throws CommandSyntaxException {
      int i = commandsourcestack.getServer().getWorldData().overworldData().getScheduledEvents().remove(s);
      if (i == 0) {
         throw ERROR_CANT_REMOVE.create(s);
      } else {
         commandsourcestack.sendSuccess(() -> Component.translatable("commands.schedule.cleared.success", i, s), true);
         return i;
      }
   }
}
