package net.minecraft.server;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.Lists;
import com.google.common.collect.Queues;
import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import java.util.Collection;
import java.util.Deque;
import java.util.List;
import java.util.Optional;
import java.util.function.IntConsumer;
import javax.annotation.Nullable;
import net.minecraft.commands.CommandFunction;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.GameRules;

public class ServerFunctionManager {
   private static final Component NO_RECURSIVE_TRACES = Component.translatable("commands.debug.function.noRecursion");
   private static final ResourceLocation TICK_FUNCTION_TAG = new ResourceLocation("tick");
   private static final ResourceLocation LOAD_FUNCTION_TAG = new ResourceLocation("load");
   final MinecraftServer server;
   @Nullable
   private ServerFunctionManager.ExecutionContext context;
   private List<CommandFunction> ticking = ImmutableList.of();
   private boolean postReload;
   private ServerFunctionLibrary library;

   public ServerFunctionManager(MinecraftServer minecraftserver, ServerFunctionLibrary serverfunctionlibrary) {
      this.server = minecraftserver;
      this.library = serverfunctionlibrary;
      this.postReload(serverfunctionlibrary);
   }

   public int getCommandLimit() {
      return this.server.getGameRules().getInt(GameRules.RULE_MAX_COMMAND_CHAIN_LENGTH);
   }

   public CommandDispatcher<CommandSourceStack> getDispatcher() {
      return this.server.getCommands().getDispatcher();
   }

   public void tick() {
      if (this.postReload) {
         this.postReload = false;
         Collection<CommandFunction> collection = this.library.getTag(LOAD_FUNCTION_TAG);
         this.executeTagFunctions(collection, LOAD_FUNCTION_TAG);
      }

      this.executeTagFunctions(this.ticking, TICK_FUNCTION_TAG);
   }

   private void executeTagFunctions(Collection<CommandFunction> collection, ResourceLocation resourcelocation) {
      this.server.getProfiler().push(resourcelocation::toString);

      for(CommandFunction commandfunction : collection) {
         this.execute(commandfunction, this.getGameLoopSender());
      }

      this.server.getProfiler().pop();
   }

   public int execute(CommandFunction commandfunction, CommandSourceStack commandsourcestack) {
      return this.execute(commandfunction, commandsourcestack, (ServerFunctionManager.TraceCallbacks)null);
   }

   public int execute(CommandFunction commandfunction, CommandSourceStack commandsourcestack, @Nullable ServerFunctionManager.TraceCallbacks serverfunctionmanager_tracecallbacks) {
      if (this.context != null) {
         if (serverfunctionmanager_tracecallbacks != null) {
            this.context.reportError(NO_RECURSIVE_TRACES.getString());
            return 0;
         } else {
            this.context.delayFunctionCall(commandfunction, commandsourcestack);
            return 0;
         }
      } else {
         int var4;
         try {
            this.context = new ServerFunctionManager.ExecutionContext(serverfunctionmanager_tracecallbacks);
            var4 = this.context.runTopCommand(commandfunction, commandsourcestack);
         } finally {
            this.context = null;
         }

         return var4;
      }
   }

   public void replaceLibrary(ServerFunctionLibrary serverfunctionlibrary) {
      this.library = serverfunctionlibrary;
      this.postReload(serverfunctionlibrary);
   }

   private void postReload(ServerFunctionLibrary serverfunctionlibrary) {
      this.ticking = ImmutableList.copyOf(serverfunctionlibrary.getTag(TICK_FUNCTION_TAG));
      this.postReload = true;
   }

   public CommandSourceStack getGameLoopSender() {
      return this.server.createCommandSourceStack().withPermission(2).withSuppressedOutput();
   }

   public Optional<CommandFunction> get(ResourceLocation resourcelocation) {
      return this.library.getFunction(resourcelocation);
   }

   public Collection<CommandFunction> getTag(ResourceLocation resourcelocation) {
      return this.library.getTag(resourcelocation);
   }

   public Iterable<ResourceLocation> getFunctionNames() {
      return this.library.getFunctions().keySet();
   }

   public Iterable<ResourceLocation> getTagNames() {
      return this.library.getAvailableTags();
   }

   class ExecutionContext {
      private int depth;
      @Nullable
      private final ServerFunctionManager.TraceCallbacks tracer;
      private final Deque<ServerFunctionManager.QueuedCommand> commandQueue = Queues.newArrayDeque();
      private final List<ServerFunctionManager.QueuedCommand> nestedCalls = Lists.newArrayList();
      boolean abortCurrentDepth = false;

      ExecutionContext(@Nullable ServerFunctionManager.TraceCallbacks serverfunctionmanager_tracecallbacks) {
         this.tracer = serverfunctionmanager_tracecallbacks;
      }

      void delayFunctionCall(CommandFunction commandfunction, CommandSourceStack commandsourcestack) {
         int i = ServerFunctionManager.this.getCommandLimit();
         CommandSourceStack commandsourcestack1 = this.wrapSender(commandsourcestack);
         if (this.commandQueue.size() + this.nestedCalls.size() < i) {
            this.nestedCalls.add(new ServerFunctionManager.QueuedCommand(commandsourcestack1, this.depth, new CommandFunction.FunctionEntry(commandfunction)));
         }

      }

      private CommandSourceStack wrapSender(CommandSourceStack commandsourcestack) {
         IntConsumer intconsumer = commandsourcestack.getReturnValueConsumer();
         return intconsumer instanceof ServerFunctionManager.ExecutionContext.AbortingReturnValueConsumer ? commandsourcestack : commandsourcestack.withReturnValueConsumer(new ServerFunctionManager.ExecutionContext.AbortingReturnValueConsumer(intconsumer));
      }

      int runTopCommand(CommandFunction commandfunction, CommandSourceStack commandsourcestack) {
         int i = ServerFunctionManager.this.getCommandLimit();
         CommandSourceStack commandsourcestack1 = this.wrapSender(commandsourcestack);
         int j = 0;
         CommandFunction.Entry[] acommandfunction_entry = commandfunction.getEntries();

         for(int k = acommandfunction_entry.length - 1; k >= 0; --k) {
            this.commandQueue.push(new ServerFunctionManager.QueuedCommand(commandsourcestack1, 0, acommandfunction_entry[k]));
         }

         while(!this.commandQueue.isEmpty()) {
            try {
               ServerFunctionManager.QueuedCommand serverfunctionmanager_queuedcommand = this.commandQueue.removeFirst();
               ServerFunctionManager.this.server.getProfiler().push(serverfunctionmanager_queuedcommand::toString);
               this.depth = serverfunctionmanager_queuedcommand.depth;
               serverfunctionmanager_queuedcommand.execute(ServerFunctionManager.this, this.commandQueue, i, this.tracer);
               if (!this.abortCurrentDepth) {
                  if (!this.nestedCalls.isEmpty()) {
                     Lists.reverse(this.nestedCalls).forEach(this.commandQueue::addFirst);
                  }
               } else {
                  while(!this.commandQueue.isEmpty() && (this.commandQueue.peek()).depth >= this.depth) {
                     this.commandQueue.removeFirst();
                  }

                  this.abortCurrentDepth = false;
               }

               this.nestedCalls.clear();
            } finally {
               ServerFunctionManager.this.server.getProfiler().pop();
            }

            ++j;
            if (j >= i) {
               return j;
            }
         }

         return j;
      }

      public void reportError(String s) {
         if (this.tracer != null) {
            this.tracer.onError(this.depth, s);
         }

      }

      class AbortingReturnValueConsumer implements IntConsumer {
         private final IntConsumer wrapped;

         AbortingReturnValueConsumer(IntConsumer intconsumer) {
            this.wrapped = intconsumer;
         }

         public void accept(int i) {
            this.wrapped.accept(i);
            ExecutionContext.this.abortCurrentDepth = true;
         }
      }
   }

   public static class QueuedCommand {
      private final CommandSourceStack sender;
      final int depth;
      private final CommandFunction.Entry entry;

      public QueuedCommand(CommandSourceStack commandsourcestack, int i, CommandFunction.Entry commandfunction_entry) {
         this.sender = commandsourcestack;
         this.depth = i;
         this.entry = commandfunction_entry;
      }

      public void execute(ServerFunctionManager serverfunctionmanager, Deque<ServerFunctionManager.QueuedCommand> deque, int i, @Nullable ServerFunctionManager.TraceCallbacks serverfunctionmanager_tracecallbacks) {
         try {
            this.entry.execute(serverfunctionmanager, this.sender, deque, i, this.depth, serverfunctionmanager_tracecallbacks);
         } catch (CommandSyntaxException var6) {
            if (serverfunctionmanager_tracecallbacks != null) {
               serverfunctionmanager_tracecallbacks.onError(this.depth, var6.getRawMessage().getString());
            }
         } catch (Exception var7) {
            if (serverfunctionmanager_tracecallbacks != null) {
               serverfunctionmanager_tracecallbacks.onError(this.depth, var7.getMessage());
            }
         }

      }

      public String toString() {
         return this.entry.toString();
      }
   }

   public interface TraceCallbacks {
      void onCommand(int i, String s);

      void onReturn(int i, String s, int j);

      void onError(int i, String s);

      void onCall(int i, ResourceLocation resourcelocation, int j);
   }
}
