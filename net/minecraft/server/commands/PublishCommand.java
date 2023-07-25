package net.minecraft.server.commands;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.arguments.BoolArgumentType;
import com.mojang.brigadier.arguments.IntegerArgumentType;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.brigadier.exceptions.DynamicCommandExceptionType;
import com.mojang.brigadier.exceptions.SimpleCommandExceptionType;
import javax.annotation.Nullable;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.commands.arguments.GameModeArgument;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.ComponentUtils;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.util.HttpUtil;
import net.minecraft.world.level.GameType;

public class PublishCommand {
   private static final SimpleCommandExceptionType ERROR_FAILED = new SimpleCommandExceptionType(Component.translatable("commands.publish.failed"));
   private static final DynamicCommandExceptionType ERROR_ALREADY_PUBLISHED = new DynamicCommandExceptionType((object) -> Component.translatable("commands.publish.alreadyPublished", object));

   public static void register(CommandDispatcher<CommandSourceStack> commanddispatcher) {
      commanddispatcher.register(Commands.literal("publish").requires((commandsourcestack) -> commandsourcestack.hasPermission(4)).executes((commandcontext3) -> publish(commandcontext3.getSource(), HttpUtil.getAvailablePort(), false, (GameType)null)).then(Commands.argument("allowCommands", BoolArgumentType.bool()).executes((commandcontext2) -> publish(commandcontext2.getSource(), HttpUtil.getAvailablePort(), BoolArgumentType.getBool(commandcontext2, "allowCommands"), (GameType)null)).then(Commands.argument("gamemode", GameModeArgument.gameMode()).executes((commandcontext1) -> publish(commandcontext1.getSource(), HttpUtil.getAvailablePort(), BoolArgumentType.getBool(commandcontext1, "allowCommands"), GameModeArgument.getGameMode(commandcontext1, "gamemode"))).then(Commands.argument("port", IntegerArgumentType.integer(0, 65535)).executes((commandcontext) -> publish(commandcontext.getSource(), IntegerArgumentType.getInteger(commandcontext, "port"), BoolArgumentType.getBool(commandcontext, "allowCommands"), GameModeArgument.getGameMode(commandcontext, "gamemode")))))));
   }

   private static int publish(CommandSourceStack commandsourcestack, int i, boolean flag, @Nullable GameType gametype) throws CommandSyntaxException {
      if (commandsourcestack.getServer().isPublished()) {
         throw ERROR_ALREADY_PUBLISHED.create(commandsourcestack.getServer().getPort());
      } else if (!commandsourcestack.getServer().publishServer(gametype, flag, i)) {
         throw ERROR_FAILED.create();
      } else {
         commandsourcestack.sendSuccess(() -> getSuccessMessage(i), true);
         return i;
      }
   }

   public static MutableComponent getSuccessMessage(int i) {
      Component component = ComponentUtils.copyOnClickText(String.valueOf(i));
      return Component.translatable("commands.publish.started", component);
   }
}
