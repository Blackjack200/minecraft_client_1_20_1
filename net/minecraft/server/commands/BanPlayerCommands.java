package net.minecraft.server.commands;

import com.mojang.authlib.GameProfile;
import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.brigadier.exceptions.SimpleCommandExceptionType;
import java.util.Collection;
import java.util.Date;
import javax.annotation.Nullable;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.commands.arguments.GameProfileArgument;
import net.minecraft.commands.arguments.MessageArgument;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.ComponentUtils;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.server.players.UserBanList;
import net.minecraft.server.players.UserBanListEntry;

public class BanPlayerCommands {
   private static final SimpleCommandExceptionType ERROR_ALREADY_BANNED = new SimpleCommandExceptionType(Component.translatable("commands.ban.failed"));

   public static void register(CommandDispatcher<CommandSourceStack> commanddispatcher) {
      commanddispatcher.register(Commands.literal("ban").requires((commandsourcestack) -> commandsourcestack.hasPermission(3)).then(Commands.argument("targets", GameProfileArgument.gameProfile()).executes((commandcontext1) -> banPlayers(commandcontext1.getSource(), GameProfileArgument.getGameProfiles(commandcontext1, "targets"), (Component)null)).then(Commands.argument("reason", MessageArgument.message()).executes((commandcontext) -> banPlayers(commandcontext.getSource(), GameProfileArgument.getGameProfiles(commandcontext, "targets"), MessageArgument.getMessage(commandcontext, "reason"))))));
   }

   private static int banPlayers(CommandSourceStack commandsourcestack, Collection<GameProfile> collection, @Nullable Component component) throws CommandSyntaxException {
      UserBanList userbanlist = commandsourcestack.getServer().getPlayerList().getBans();
      int i = 0;

      for(GameProfile gameprofile : collection) {
         if (!userbanlist.isBanned(gameprofile)) {
            UserBanListEntry userbanlistentry = new UserBanListEntry(gameprofile, (Date)null, commandsourcestack.getTextName(), (Date)null, component == null ? null : component.getString());
            userbanlist.add(userbanlistentry);
            ++i;
            commandsourcestack.sendSuccess(() -> Component.translatable("commands.ban.success", ComponentUtils.getDisplayName(gameprofile), userbanlistentry.getReason()), true);
            ServerPlayer serverplayer = commandsourcestack.getServer().getPlayerList().getPlayer(gameprofile.getId());
            if (serverplayer != null) {
               serverplayer.connection.disconnect(Component.translatable("multiplayer.disconnect.banned"));
            }
         }
      }

      if (i == 0) {
         throw ERROR_ALREADY_BANNED.create();
      } else {
         return i;
      }
   }
}
