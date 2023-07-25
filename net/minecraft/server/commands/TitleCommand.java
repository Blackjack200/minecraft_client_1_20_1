package net.minecraft.server.commands;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.arguments.IntegerArgumentType;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import java.util.Collection;
import java.util.function.Function;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.commands.arguments.ComponentArgument;
import net.minecraft.commands.arguments.EntityArgument;
import net.minecraft.commands.arguments.TimeArgument;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.ComponentUtils;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.game.ClientboundClearTitlesPacket;
import net.minecraft.network.protocol.game.ClientboundSetActionBarTextPacket;
import net.minecraft.network.protocol.game.ClientboundSetSubtitleTextPacket;
import net.minecraft.network.protocol.game.ClientboundSetTitleTextPacket;
import net.minecraft.network.protocol.game.ClientboundSetTitlesAnimationPacket;
import net.minecraft.server.level.ServerPlayer;

public class TitleCommand {
   public static void register(CommandDispatcher<CommandSourceStack> commanddispatcher) {
      commanddispatcher.register(Commands.literal("title").requires((commandsourcestack) -> commandsourcestack.hasPermission(2)).then(Commands.argument("targets", EntityArgument.players()).then(Commands.literal("clear").executes((commandcontext5) -> clearTitle(commandcontext5.getSource(), EntityArgument.getPlayers(commandcontext5, "targets")))).then(Commands.literal("reset").executes((commandcontext4) -> resetTitle(commandcontext4.getSource(), EntityArgument.getPlayers(commandcontext4, "targets")))).then(Commands.literal("title").then(Commands.argument("title", ComponentArgument.textComponent()).executes((commandcontext3) -> showTitle(commandcontext3.getSource(), EntityArgument.getPlayers(commandcontext3, "targets"), ComponentArgument.getComponent(commandcontext3, "title"), "title", ClientboundSetTitleTextPacket::new)))).then(Commands.literal("subtitle").then(Commands.argument("title", ComponentArgument.textComponent()).executes((commandcontext2) -> showTitle(commandcontext2.getSource(), EntityArgument.getPlayers(commandcontext2, "targets"), ComponentArgument.getComponent(commandcontext2, "title"), "subtitle", ClientboundSetSubtitleTextPacket::new)))).then(Commands.literal("actionbar").then(Commands.argument("title", ComponentArgument.textComponent()).executes((commandcontext1) -> showTitle(commandcontext1.getSource(), EntityArgument.getPlayers(commandcontext1, "targets"), ComponentArgument.getComponent(commandcontext1, "title"), "actionbar", ClientboundSetActionBarTextPacket::new)))).then(Commands.literal("times").then(Commands.argument("fadeIn", TimeArgument.time()).then(Commands.argument("stay", TimeArgument.time()).then(Commands.argument("fadeOut", TimeArgument.time()).executes((commandcontext) -> setTimes(commandcontext.getSource(), EntityArgument.getPlayers(commandcontext, "targets"), IntegerArgumentType.getInteger(commandcontext, "fadeIn"), IntegerArgumentType.getInteger(commandcontext, "stay"), IntegerArgumentType.getInteger(commandcontext, "fadeOut")))))))));
   }

   private static int clearTitle(CommandSourceStack commandsourcestack, Collection<ServerPlayer> collection) {
      ClientboundClearTitlesPacket clientboundcleartitlespacket = new ClientboundClearTitlesPacket(false);

      for(ServerPlayer serverplayer : collection) {
         serverplayer.connection.send(clientboundcleartitlespacket);
      }

      if (collection.size() == 1) {
         commandsourcestack.sendSuccess(() -> Component.translatable("commands.title.cleared.single", collection.iterator().next().getDisplayName()), true);
      } else {
         commandsourcestack.sendSuccess(() -> Component.translatable("commands.title.cleared.multiple", collection.size()), true);
      }

      return collection.size();
   }

   private static int resetTitle(CommandSourceStack commandsourcestack, Collection<ServerPlayer> collection) {
      ClientboundClearTitlesPacket clientboundcleartitlespacket = new ClientboundClearTitlesPacket(true);

      for(ServerPlayer serverplayer : collection) {
         serverplayer.connection.send(clientboundcleartitlespacket);
      }

      if (collection.size() == 1) {
         commandsourcestack.sendSuccess(() -> Component.translatable("commands.title.reset.single", collection.iterator().next().getDisplayName()), true);
      } else {
         commandsourcestack.sendSuccess(() -> Component.translatable("commands.title.reset.multiple", collection.size()), true);
      }

      return collection.size();
   }

   private static int showTitle(CommandSourceStack commandsourcestack, Collection<ServerPlayer> collection, Component component, String s, Function<Component, Packet<?>> function) throws CommandSyntaxException {
      for(ServerPlayer serverplayer : collection) {
         serverplayer.connection.send(function.apply(ComponentUtils.updateForEntity(commandsourcestack, component, serverplayer, 0)));
      }

      if (collection.size() == 1) {
         commandsourcestack.sendSuccess(() -> Component.translatable("commands.title.show." + s + ".single", collection.iterator().next().getDisplayName()), true);
      } else {
         commandsourcestack.sendSuccess(() -> Component.translatable("commands.title.show." + s + ".multiple", collection.size()), true);
      }

      return collection.size();
   }

   private static int setTimes(CommandSourceStack commandsourcestack, Collection<ServerPlayer> collection, int i, int j, int k) {
      ClientboundSetTitlesAnimationPacket clientboundsettitlesanimationpacket = new ClientboundSetTitlesAnimationPacket(i, j, k);

      for(ServerPlayer serverplayer : collection) {
         serverplayer.connection.send(clientboundsettitlesanimationpacket);
      }

      if (collection.size() == 1) {
         commandsourcestack.sendSuccess(() -> Component.translatable("commands.title.times.single", collection.iterator().next().getDisplayName()), true);
      } else {
         commandsourcestack.sendSuccess(() -> Component.translatable("commands.title.times.multiple", collection.size()), true);
      }

      return collection.size();
   }
}
