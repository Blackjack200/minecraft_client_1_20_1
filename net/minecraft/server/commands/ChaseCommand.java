package net.minecraft.server.commands;

import com.google.common.collect.BiMap;
import com.google.common.collect.ImmutableBiMap;
import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.arguments.IntegerArgumentType;
import com.mojang.brigadier.arguments.StringArgumentType;
import java.io.IOException;
import javax.annotation.Nullable;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceKey;
import net.minecraft.server.chase.ChaseClient;
import net.minecraft.server.chase.ChaseServer;
import net.minecraft.world.level.Level;

public class ChaseCommand {
   private static final String DEFAULT_CONNECT_HOST = "localhost";
   private static final String DEFAULT_BIND_ADDRESS = "0.0.0.0";
   private static final int DEFAULT_PORT = 10000;
   private static final int BROADCAST_INTERVAL_MS = 100;
   public static BiMap<String, ResourceKey<Level>> DIMENSION_NAMES = ImmutableBiMap.of("o", Level.OVERWORLD, "n", Level.NETHER, "e", Level.END);
   @Nullable
   private static ChaseServer chaseServer;
   @Nullable
   private static ChaseClient chaseClient;

   public static void register(CommandDispatcher<CommandSourceStack> commanddispatcher) {
      commanddispatcher.register(Commands.literal("chase").then(Commands.literal("follow").then(Commands.argument("host", StringArgumentType.string()).executes((commandcontext6) -> follow(commandcontext6.getSource(), StringArgumentType.getString(commandcontext6, "host"), 10000)).then(Commands.argument("port", IntegerArgumentType.integer(1, 65535)).executes((commandcontext5) -> follow(commandcontext5.getSource(), StringArgumentType.getString(commandcontext5, "host"), IntegerArgumentType.getInteger(commandcontext5, "port"))))).executes((commandcontext4) -> follow(commandcontext4.getSource(), "localhost", 10000))).then(Commands.literal("lead").then(Commands.argument("bind_address", StringArgumentType.string()).executes((commandcontext3) -> lead(commandcontext3.getSource(), StringArgumentType.getString(commandcontext3, "bind_address"), 10000)).then(Commands.argument("port", IntegerArgumentType.integer(1024, 65535)).executes((commandcontext2) -> lead(commandcontext2.getSource(), StringArgumentType.getString(commandcontext2, "bind_address"), IntegerArgumentType.getInteger(commandcontext2, "port"))))).executes((commandcontext1) -> lead(commandcontext1.getSource(), "0.0.0.0", 10000))).then(Commands.literal("stop").executes((commandcontext) -> stop(commandcontext.getSource()))));
   }

   private static int stop(CommandSourceStack commandsourcestack) {
      if (chaseClient != null) {
         chaseClient.stop();
         commandsourcestack.sendSuccess(() -> Component.literal("You have now stopped chasing"), false);
         chaseClient = null;
      }

      if (chaseServer != null) {
         chaseServer.stop();
         commandsourcestack.sendSuccess(() -> Component.literal("You are no longer being chased"), false);
         chaseServer = null;
      }

      return 0;
   }

   private static boolean alreadyRunning(CommandSourceStack commandsourcestack) {
      if (chaseServer != null) {
         commandsourcestack.sendFailure(Component.literal("Chase server is already running. Stop it using /chase stop"));
         return true;
      } else if (chaseClient != null) {
         commandsourcestack.sendFailure(Component.literal("You are already chasing someone. Stop it using /chase stop"));
         return true;
      } else {
         return false;
      }
   }

   private static int lead(CommandSourceStack commandsourcestack, String s, int i) {
      if (alreadyRunning(commandsourcestack)) {
         return 0;
      } else {
         chaseServer = new ChaseServer(s, i, commandsourcestack.getServer().getPlayerList(), 100);

         try {
            chaseServer.start();
            commandsourcestack.sendSuccess(() -> Component.literal("Chase server is now running on port " + i + ". Clients can follow you using /chase follow <ip> <port>"), false);
         } catch (IOException var4) {
            var4.printStackTrace();
            commandsourcestack.sendFailure(Component.literal("Failed to start chase server on port " + i));
            chaseServer = null;
         }

         return 0;
      }
   }

   private static int follow(CommandSourceStack commandsourcestack, String s, int i) {
      if (alreadyRunning(commandsourcestack)) {
         return 0;
      } else {
         chaseClient = new ChaseClient(s, i, commandsourcestack.getServer());
         chaseClient.start();
         commandsourcestack.sendSuccess(() -> Component.literal("You are now chasing " + s + ":" + i + ". If that server does '/chase lead' then you will automatically go to the same position. Use '/chase stop' to stop chasing."), false);
         return 0;
      }
   }
}
