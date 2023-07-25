package net.minecraft.server.commands;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.brigadier.exceptions.SimpleCommandExceptionType;
import com.mojang.logging.LogUtils;
import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Locale;
import java.util.function.Consumer;
import net.minecraft.FileUtil;
import net.minecraft.SharedConstants;
import net.minecraft.SystemReport;
import net.minecraft.Util;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.network.chat.Component;
import net.minecraft.server.MinecraftServer;
import net.minecraft.util.FileZipper;
import net.minecraft.util.TimeUtil;
import net.minecraft.util.profiling.EmptyProfileResults;
import net.minecraft.util.profiling.ProfileResults;
import net.minecraft.util.profiling.metrics.storage.MetricsPersister;
import org.apache.commons.io.FileUtils;
import org.slf4j.Logger;

public class PerfCommand {
   private static final Logger LOGGER = LogUtils.getLogger();
   private static final SimpleCommandExceptionType ERROR_NOT_RUNNING = new SimpleCommandExceptionType(Component.translatable("commands.perf.notRunning"));
   private static final SimpleCommandExceptionType ERROR_ALREADY_RUNNING = new SimpleCommandExceptionType(Component.translatable("commands.perf.alreadyRunning"));

   public static void register(CommandDispatcher<CommandSourceStack> commanddispatcher) {
      commanddispatcher.register(Commands.literal("perf").requires((commandsourcestack) -> commandsourcestack.hasPermission(4)).then(Commands.literal("start").executes((commandcontext1) -> startProfilingDedicatedServer(commandcontext1.getSource()))).then(Commands.literal("stop").executes((commandcontext) -> stopProfilingDedicatedServer(commandcontext.getSource()))));
   }

   private static int startProfilingDedicatedServer(CommandSourceStack commandsourcestack) throws CommandSyntaxException {
      MinecraftServer minecraftserver = commandsourcestack.getServer();
      if (minecraftserver.isRecordingMetrics()) {
         throw ERROR_ALREADY_RUNNING.create();
      } else {
         Consumer<ProfileResults> consumer = (profileresults) -> whenStopped(commandsourcestack, profileresults);
         Consumer<Path> consumer1 = (path) -> saveResults(commandsourcestack, path, minecraftserver);
         minecraftserver.startRecordingMetrics(consumer, consumer1);
         commandsourcestack.sendSuccess(() -> Component.translatable("commands.perf.started"), false);
         return 0;
      }
   }

   private static int stopProfilingDedicatedServer(CommandSourceStack commandsourcestack) throws CommandSyntaxException {
      MinecraftServer minecraftserver = commandsourcestack.getServer();
      if (!minecraftserver.isRecordingMetrics()) {
         throw ERROR_NOT_RUNNING.create();
      } else {
         minecraftserver.finishRecordingMetrics();
         return 0;
      }
   }

   private static void saveResults(CommandSourceStack commandsourcestack, Path path, MinecraftServer minecraftserver) {
      String s = String.format(Locale.ROOT, "%s-%s-%s", Util.getFilenameFormattedDateTime(), minecraftserver.getWorldData().getLevelName(), SharedConstants.getCurrentVersion().getId());

      String s1;
      try {
         s1 = FileUtil.findAvailableName(MetricsPersister.PROFILING_RESULTS_DIR, s, ".zip");
      } catch (IOException var11) {
         commandsourcestack.sendFailure(Component.translatable("commands.perf.reportFailed"));
         LOGGER.error("Failed to create report name", (Throwable)var11);
         return;
      }

      FileZipper filezipper = new FileZipper(MetricsPersister.PROFILING_RESULTS_DIR.resolve(s1));

      try {
         filezipper.add(Paths.get("system.txt"), minecraftserver.fillSystemReport(new SystemReport()).toLineSeparatedString());
         filezipper.add(path);
      } catch (Throwable var10) {
         try {
            filezipper.close();
         } catch (Throwable var8) {
            var10.addSuppressed(var8);
         }

         throw var10;
      }

      filezipper.close();

      try {
         FileUtils.forceDelete(path.toFile());
      } catch (IOException var9) {
         LOGGER.warn("Failed to delete temporary profiling file {}", path, var9);
      }

      commandsourcestack.sendSuccess(() -> Component.translatable("commands.perf.reportSaved", s1), false);
   }

   private static void whenStopped(CommandSourceStack commandsourcestack, ProfileResults profileresults) {
      if (profileresults != EmptyProfileResults.EMPTY) {
         int i = profileresults.getTickDuration();
         double d0 = (double)profileresults.getNanoDuration() / (double)TimeUtil.NANOSECONDS_PER_SECOND;
         commandsourcestack.sendSuccess(() -> Component.translatable("commands.perf.stopped", String.format(Locale.ROOT, "%.2f", d0), i, String.format(Locale.ROOT, "%.2f", (double)i / d0)), false);
      }
   }
}
