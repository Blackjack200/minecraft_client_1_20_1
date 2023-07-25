package net.minecraft.server.commands;

import com.mojang.authlib.GameProfile;
import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.brigadier.exceptions.SimpleCommandExceptionType;
import java.util.Collection;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.commands.SharedSuggestionProvider;
import net.minecraft.commands.arguments.GameProfileArgument;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.ComponentUtils;
import net.minecraft.server.players.PlayerList;
import net.minecraft.server.players.UserWhiteList;
import net.minecraft.server.players.UserWhiteListEntry;

public class WhitelistCommand {
   private static final SimpleCommandExceptionType ERROR_ALREADY_ENABLED = new SimpleCommandExceptionType(Component.translatable("commands.whitelist.alreadyOn"));
   private static final SimpleCommandExceptionType ERROR_ALREADY_DISABLED = new SimpleCommandExceptionType(Component.translatable("commands.whitelist.alreadyOff"));
   private static final SimpleCommandExceptionType ERROR_ALREADY_WHITELISTED = new SimpleCommandExceptionType(Component.translatable("commands.whitelist.add.failed"));
   private static final SimpleCommandExceptionType ERROR_NOT_WHITELISTED = new SimpleCommandExceptionType(Component.translatable("commands.whitelist.remove.failed"));

   public static void register(CommandDispatcher<CommandSourceStack> commanddispatcher) {
      commanddispatcher.register(Commands.literal("whitelist").requires((commandsourcestack) -> commandsourcestack.hasPermission(3)).then(Commands.literal("on").executes((commandcontext7) -> enableWhitelist(commandcontext7.getSource()))).then(Commands.literal("off").executes((commandcontext6) -> disableWhitelist(commandcontext6.getSource()))).then(Commands.literal("list").executes((commandcontext5) -> showList(commandcontext5.getSource()))).then(Commands.literal("add").then(Commands.argument("targets", GameProfileArgument.gameProfile()).suggests((commandcontext4, suggestionsbuilder1) -> {
         PlayerList playerlist = commandcontext4.getSource().getServer().getPlayerList();
         return SharedSuggestionProvider.suggest(playerlist.getPlayers().stream().filter((serverplayer1) -> !playerlist.getWhiteList().isWhiteListed(serverplayer1.getGameProfile())).map((serverplayer) -> serverplayer.getGameProfile().getName()), suggestionsbuilder1);
      }).executes((commandcontext3) -> addPlayers(commandcontext3.getSource(), GameProfileArgument.getGameProfiles(commandcontext3, "targets"))))).then(Commands.literal("remove").then(Commands.argument("targets", GameProfileArgument.gameProfile()).suggests((commandcontext2, suggestionsbuilder) -> SharedSuggestionProvider.suggest(commandcontext2.getSource().getServer().getPlayerList().getWhiteListNames(), suggestionsbuilder)).executes((commandcontext1) -> removePlayers(commandcontext1.getSource(), GameProfileArgument.getGameProfiles(commandcontext1, "targets"))))).then(Commands.literal("reload").executes((commandcontext) -> reload(commandcontext.getSource()))));
   }

   private static int reload(CommandSourceStack commandsourcestack) {
      commandsourcestack.getServer().getPlayerList().reloadWhiteList();
      commandsourcestack.sendSuccess(() -> Component.translatable("commands.whitelist.reloaded"), true);
      commandsourcestack.getServer().kickUnlistedPlayers(commandsourcestack);
      return 1;
   }

   private static int addPlayers(CommandSourceStack commandsourcestack, Collection<GameProfile> collection) throws CommandSyntaxException {
      UserWhiteList userwhitelist = commandsourcestack.getServer().getPlayerList().getWhiteList();
      int i = 0;

      for(GameProfile gameprofile : collection) {
         if (!userwhitelist.isWhiteListed(gameprofile)) {
            UserWhiteListEntry userwhitelistentry = new UserWhiteListEntry(gameprofile);
            userwhitelist.add(userwhitelistentry);
            commandsourcestack.sendSuccess(() -> Component.translatable("commands.whitelist.add.success", ComponentUtils.getDisplayName(gameprofile)), true);
            ++i;
         }
      }

      if (i == 0) {
         throw ERROR_ALREADY_WHITELISTED.create();
      } else {
         return i;
      }
   }

   private static int removePlayers(CommandSourceStack commandsourcestack, Collection<GameProfile> collection) throws CommandSyntaxException {
      UserWhiteList userwhitelist = commandsourcestack.getServer().getPlayerList().getWhiteList();
      int i = 0;

      for(GameProfile gameprofile : collection) {
         if (userwhitelist.isWhiteListed(gameprofile)) {
            UserWhiteListEntry userwhitelistentry = new UserWhiteListEntry(gameprofile);
            userwhitelist.remove(userwhitelistentry);
            commandsourcestack.sendSuccess(() -> Component.translatable("commands.whitelist.remove.success", ComponentUtils.getDisplayName(gameprofile)), true);
            ++i;
         }
      }

      if (i == 0) {
         throw ERROR_NOT_WHITELISTED.create();
      } else {
         commandsourcestack.getServer().kickUnlistedPlayers(commandsourcestack);
         return i;
      }
   }

   private static int enableWhitelist(CommandSourceStack commandsourcestack) throws CommandSyntaxException {
      PlayerList playerlist = commandsourcestack.getServer().getPlayerList();
      if (playerlist.isUsingWhitelist()) {
         throw ERROR_ALREADY_ENABLED.create();
      } else {
         playerlist.setUsingWhiteList(true);
         commandsourcestack.sendSuccess(() -> Component.translatable("commands.whitelist.enabled"), true);
         commandsourcestack.getServer().kickUnlistedPlayers(commandsourcestack);
         return 1;
      }
   }

   private static int disableWhitelist(CommandSourceStack commandsourcestack) throws CommandSyntaxException {
      PlayerList playerlist = commandsourcestack.getServer().getPlayerList();
      if (!playerlist.isUsingWhitelist()) {
         throw ERROR_ALREADY_DISABLED.create();
      } else {
         playerlist.setUsingWhiteList(false);
         commandsourcestack.sendSuccess(() -> Component.translatable("commands.whitelist.disabled"), true);
         return 1;
      }
   }

   private static int showList(CommandSourceStack commandsourcestack) {
      String[] astring = commandsourcestack.getServer().getPlayerList().getWhiteListNames();
      if (astring.length == 0) {
         commandsourcestack.sendSuccess(() -> Component.translatable("commands.whitelist.none"), false);
      } else {
         commandsourcestack.sendSuccess(() -> Component.translatable("commands.whitelist.list", astring.length, String.join(", ", astring)), false);
      }

      return astring.length;
   }
}
