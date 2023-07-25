package net.minecraft.server.commands;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.brigadier.exceptions.SimpleCommandExceptionType;
import com.mojang.logging.LogUtils;
import java.io.IOException;
import java.io.PrintWriter;
import java.io.UncheckedIOException;
import java.io.Writer;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Collection;
import java.util.Locale;
import net.minecraft.Util;
import net.minecraft.commands.CommandFunction;
import net.minecraft.commands.CommandSource;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.commands.arguments.item.FunctionArgument;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.ServerFunctionManager;
import net.minecraft.util.TimeUtil;
import net.minecraft.util.profiling.ProfileResults;
import org.slf4j.Logger;

public class DebugCommand {
   private static final Logger LOGGER = LogUtils.getLogger();
   private static final SimpleCommandExceptionType ERROR_NOT_RUNNING = new SimpleCommandExceptionType(Component.translatable("commands.debug.notRunning"));
   private static final SimpleCommandExceptionType ERROR_ALREADY_RUNNING = new SimpleCommandExceptionType(Component.translatable("commands.debug.alreadyRunning"));

   public static void register(CommandDispatcher<CommandSourceStack> commanddispatcher) {
      commanddispatcher.register(Commands.literal("debug").requires((commandsourcestack1) -> commandsourcestack1.hasPermission(3)).then(Commands.literal("start").executes((commandcontext2) -> start(commandcontext2.getSource()))).then(Commands.literal("stop").executes((commandcontext1) -> stop(commandcontext1.getSource()))).then(Commands.literal("function").requires((commandsourcestack) -> commandsourcestack.hasPermission(3)).then(Commands.argument("name", FunctionArgument.functions()).suggests(FunctionCommand.SUGGEST_FUNCTION).executes((commandcontext) -> traceFunction(commandcontext.getSource(), FunctionArgument.getFunctions(commandcontext, "name"))))));
   }

   private static int start(CommandSourceStack commandsourcestack) throws CommandSyntaxException {
      MinecraftServer minecraftserver = commandsourcestack.getServer();
      if (minecraftserver.isTimeProfilerRunning()) {
         throw ERROR_ALREADY_RUNNING.create();
      } else {
         minecraftserver.startTimeProfiler();
         commandsourcestack.sendSuccess(() -> Component.translatable("commands.debug.started"), true);
         return 0;
      }
   }

   private static int stop(CommandSourceStack commandsourcestack) throws CommandSyntaxException {
      MinecraftServer minecraftserver = commandsourcestack.getServer();
      if (!minecraftserver.isTimeProfilerRunning()) {
         throw ERROR_NOT_RUNNING.create();
      } else {
         ProfileResults profileresults = minecraftserver.stopTimeProfiler();
         double d0 = (double)profileresults.getNanoDuration() / (double)TimeUtil.NANOSECONDS_PER_SECOND;
         double d1 = (double)profileresults.getTickDuration() / d0;
         commandsourcestack.sendSuccess(() -> Component.translatable("commands.debug.stopped", String.format(Locale.ROOT, "%.2f", d0), profileresults.getTickDuration(), String.format(Locale.ROOT, "%.2f", d1)), true);
         return (int)d1;
      }
   }

   private static int traceFunction(CommandSourceStack commandsourcestack, Collection<CommandFunction> collection) {
      int i = 0;
      MinecraftServer minecraftserver = commandsourcestack.getServer();
      String s = "debug-trace-" + Util.getFilenameFormattedDateTime() + ".txt";

      try {
         Path path = minecraftserver.getFile("debug").toPath();
         Files.createDirectories(path);
         Writer writer = Files.newBufferedWriter(path.resolve(s), StandardCharsets.UTF_8);

         try {
            PrintWriter printwriter = new PrintWriter(writer);

            for(CommandFunction commandfunction : collection) {
               printwriter.println((Object)commandfunction.getId());
               DebugCommand.Tracer debugcommand_tracer = new DebugCommand.Tracer(printwriter);
               i += commandsourcestack.getServer().getFunctions().execute(commandfunction, commandsourcestack.withSource(debugcommand_tracer).withMaximumPermission(2), debugcommand_tracer);
            }
         } catch (Throwable var12) {
            if (writer != null) {
               try {
                  writer.close();
               } catch (Throwable var11) {
                  var12.addSuppressed(var11);
               }
            }

            throw var12;
         }

         if (writer != null) {
            writer.close();
         }
      } catch (IOException | UncheckedIOException var13) {
         LOGGER.warn("Tracing failed", (Throwable)var13);
         commandsourcestack.sendFailure(Component.translatable("commands.debug.function.traceFailed"));
      }

      int j = i;
      if (collection.size() == 1) {
         commandsourcestack.sendSuccess(() -> Component.translatable("commands.debug.function.success.single", j, collection.iterator().next().getId(), s), true);
      } else {
         commandsourcestack.sendSuccess(() -> Component.translatable("commands.debug.function.success.multiple", j, collection.size(), s), true);
      }

      return i;
   }

   static class Tracer implements ServerFunctionManager.TraceCallbacks, CommandSource {
      public static final int INDENT_OFFSET = 1;
      private final PrintWriter output;
      private int lastIndent;
      private boolean waitingForResult;

      Tracer(PrintWriter printwriter) {
         this.output = printwriter;
      }

      private void indentAndSave(int i) {
         this.printIndent(i);
         this.lastIndent = i;
      }

      private void printIndent(int i) {
         for(int j = 0; j < i + 1; ++j) {
            this.output.write("    ");
         }

      }

      private void newLine() {
         if (this.waitingForResult) {
            this.output.println();
            this.waitingForResult = false;
         }

      }

      public void onCommand(int i, String s) {
         this.newLine();
         this.indentAndSave(i);
         this.output.print("[C] ");
         this.output.print(s);
         this.waitingForResult = true;
      }

      public void onReturn(int i, String s, int j) {
         if (this.waitingForResult) {
            this.output.print(" -> ");
            this.output.println(j);
            this.waitingForResult = false;
         } else {
            this.indentAndSave(i);
            this.output.print("[R = ");
            this.output.print(j);
            this.output.print("] ");
            this.output.println(s);
         }

      }

      public void onCall(int i, ResourceLocation resourcelocation, int j) {
         this.newLine();
         this.indentAndSave(i);
         this.output.print("[F] ");
         this.output.print((Object)resourcelocation);
         this.output.print(" size=");
         this.output.println(j);
      }

      public void onError(int i, String s) {
         this.newLine();
         this.indentAndSave(i + 1);
         this.output.print("[E] ");
         this.output.print(s);
      }

      public void sendSystemMessage(Component component) {
         this.newLine();
         this.printIndent(this.lastIndent + 1);
         this.output.print("[M] ");
         this.output.println(component.getString());
      }

      public boolean acceptsSuccess() {
         return true;
      }

      public boolean acceptsFailure() {
         return true;
      }

      public boolean shouldInformAdmins() {
         return false;
      }

      public boolean alwaysAccepts() {
         return true;
      }
   }
}
