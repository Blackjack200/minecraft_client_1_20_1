package net.minecraft.commands;

import com.google.common.collect.Lists;
import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.ParseResults;
import com.mojang.brigadier.StringReader;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import java.util.Deque;
import java.util.List;
import java.util.Optional;
import javax.annotation.Nullable;
import net.minecraft.Util;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.ServerFunctionManager;

public class CommandFunction {
   private final CommandFunction.Entry[] entries;
   final ResourceLocation id;

   public CommandFunction(ResourceLocation resourcelocation, CommandFunction.Entry[] acommandfunction_entry) {
      this.id = resourcelocation;
      this.entries = acommandfunction_entry;
   }

   public ResourceLocation getId() {
      return this.id;
   }

   public CommandFunction.Entry[] getEntries() {
      return this.entries;
   }

   public static CommandFunction fromLines(ResourceLocation resourcelocation, CommandDispatcher<CommandSourceStack> commanddispatcher, CommandSourceStack commandsourcestack, List<String> list) {
      List<CommandFunction.Entry> list1 = Lists.newArrayListWithCapacity(list.size());

      for(int i = 0; i < list.size(); ++i) {
         int j = i + 1;
         String s = list.get(i).trim();
         StringReader stringreader = new StringReader(s);
         if (stringreader.canRead() && stringreader.peek() != '#') {
            if (stringreader.peek() == '/') {
               stringreader.skip();
               if (stringreader.peek() == '/') {
                  throw new IllegalArgumentException("Unknown or invalid command '" + s + "' on line " + j + " (if you intended to make a comment, use '#' not '//')");
               }

               String s1 = stringreader.readUnquotedString();
               throw new IllegalArgumentException("Unknown or invalid command '" + s + "' on line " + j + " (did you mean '" + s1 + "'? Do not use a preceding forwards slash.)");
            }

            try {
               ParseResults<CommandSourceStack> parseresults = commanddispatcher.parse(stringreader, commandsourcestack);
               if (parseresults.getReader().canRead()) {
                  throw Commands.getParseException(parseresults);
               }

               list1.add(new CommandFunction.CommandEntry(parseresults));
            } catch (CommandSyntaxException var10) {
               throw new IllegalArgumentException("Whilst parsing command on line " + j + ": " + var10.getMessage());
            }
         }
      }

      return new CommandFunction(resourcelocation, list1.toArray(new CommandFunction.Entry[0]));
   }

   public static class CacheableFunction {
      public static final CommandFunction.CacheableFunction NONE = new CommandFunction.CacheableFunction((ResourceLocation)null);
      @Nullable
      private final ResourceLocation id;
      private boolean resolved;
      private Optional<CommandFunction> function = Optional.empty();

      public CacheableFunction(@Nullable ResourceLocation resourcelocation) {
         this.id = resourcelocation;
      }

      public CacheableFunction(CommandFunction commandfunction) {
         this.resolved = true;
         this.id = null;
         this.function = Optional.of(commandfunction);
      }

      public Optional<CommandFunction> get(ServerFunctionManager serverfunctionmanager) {
         if (!this.resolved) {
            if (this.id != null) {
               this.function = serverfunctionmanager.get(this.id);
            }

            this.resolved = true;
         }

         return this.function;
      }

      @Nullable
      public ResourceLocation getId() {
         return this.function.map((commandfunction) -> commandfunction.id).orElse(this.id);
      }
   }

   public static class CommandEntry implements CommandFunction.Entry {
      private final ParseResults<CommandSourceStack> parse;

      public CommandEntry(ParseResults<CommandSourceStack> parseresults) {
         this.parse = parseresults;
      }

      public void execute(ServerFunctionManager serverfunctionmanager, CommandSourceStack commandsourcestack, Deque<ServerFunctionManager.QueuedCommand> deque, int i, int j, @Nullable ServerFunctionManager.TraceCallbacks serverfunctionmanager_tracecallbacks) throws CommandSyntaxException {
         if (serverfunctionmanager_tracecallbacks != null) {
            String s = this.parse.getReader().getString();
            serverfunctionmanager_tracecallbacks.onCommand(j, s);
            int k = this.execute(serverfunctionmanager, commandsourcestack);
            serverfunctionmanager_tracecallbacks.onReturn(j, s, k);
         } else {
            this.execute(serverfunctionmanager, commandsourcestack);
         }

      }

      private int execute(ServerFunctionManager serverfunctionmanager, CommandSourceStack commandsourcestack) throws CommandSyntaxException {
         return serverfunctionmanager.getDispatcher().execute(Commands.mapSource(this.parse, (commandsourcestack2) -> commandsourcestack));
      }

      public String toString() {
         return this.parse.getReader().getString();
      }
   }

   @FunctionalInterface
   public interface Entry {
      void execute(ServerFunctionManager serverfunctionmanager, CommandSourceStack commandsourcestack, Deque<ServerFunctionManager.QueuedCommand> deque, int i, int j, @Nullable ServerFunctionManager.TraceCallbacks serverfunctionmanager_tracecallbacks) throws CommandSyntaxException;
   }

   public static class FunctionEntry implements CommandFunction.Entry {
      private final CommandFunction.CacheableFunction function;

      public FunctionEntry(CommandFunction commandfunction) {
         this.function = new CommandFunction.CacheableFunction(commandfunction);
      }

      public void execute(ServerFunctionManager serverfunctionmanager, CommandSourceStack commandsourcestack, Deque<ServerFunctionManager.QueuedCommand> deque, int i, int j, @Nullable ServerFunctionManager.TraceCallbacks serverfunctionmanager_tracecallbacks) {
         Util.ifElse(this.function.get(serverfunctionmanager), (commandfunction) -> {
            CommandFunction.Entry[] acommandfunction_entry = commandfunction.getEntries();
            if (serverfunctionmanager_tracecallbacks != null) {
               serverfunctionmanager_tracecallbacks.onCall(j, commandfunction.getId(), acommandfunction_entry.length);
            }

            int j1 = i - deque.size();
            int k1 = Math.min(acommandfunction_entry.length, j1);

            for(int l1 = k1 - 1; l1 >= 0; --l1) {
               deque.addFirst(new ServerFunctionManager.QueuedCommand(commandsourcestack, j + 1, acommandfunction_entry[l1]));
            }

         }, () -> {
            if (serverfunctionmanager_tracecallbacks != null) {
               serverfunctionmanager_tracecallbacks.onCall(j, this.function.getId(), -1);
            }

         });
      }

      public String toString() {
         return "function " + this.function.getId();
      }
   }
}
