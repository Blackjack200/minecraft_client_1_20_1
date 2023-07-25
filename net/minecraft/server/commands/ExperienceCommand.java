package net.minecraft.server.commands;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.arguments.IntegerArgumentType;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.brigadier.exceptions.SimpleCommandExceptionType;
import com.mojang.brigadier.tree.LiteralCommandNode;
import java.util.Collection;
import java.util.function.BiConsumer;
import java.util.function.BiPredicate;
import java.util.function.ToIntFunction;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.commands.arguments.EntityArgument;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.player.Player;

public class ExperienceCommand {
   private static final SimpleCommandExceptionType ERROR_SET_POINTS_INVALID = new SimpleCommandExceptionType(Component.translatable("commands.experience.set.points.invalid"));

   public static void register(CommandDispatcher<CommandSourceStack> commanddispatcher) {
      LiteralCommandNode<CommandSourceStack> literalcommandnode = commanddispatcher.register(Commands.literal("experience").requires((commandsourcestack1) -> commandsourcestack1.hasPermission(2)).then(Commands.literal("add").then(Commands.argument("targets", EntityArgument.players()).then(Commands.argument("amount", IntegerArgumentType.integer()).executes((commandcontext7) -> addExperience(commandcontext7.getSource(), EntityArgument.getPlayers(commandcontext7, "targets"), IntegerArgumentType.getInteger(commandcontext7, "amount"), ExperienceCommand.Type.POINTS)).then(Commands.literal("points").executes((commandcontext6) -> addExperience(commandcontext6.getSource(), EntityArgument.getPlayers(commandcontext6, "targets"), IntegerArgumentType.getInteger(commandcontext6, "amount"), ExperienceCommand.Type.POINTS))).then(Commands.literal("levels").executes((commandcontext5) -> addExperience(commandcontext5.getSource(), EntityArgument.getPlayers(commandcontext5, "targets"), IntegerArgumentType.getInteger(commandcontext5, "amount"), ExperienceCommand.Type.LEVELS)))))).then(Commands.literal("set").then(Commands.argument("targets", EntityArgument.players()).then(Commands.argument("amount", IntegerArgumentType.integer(0)).executes((commandcontext4) -> setExperience(commandcontext4.getSource(), EntityArgument.getPlayers(commandcontext4, "targets"), IntegerArgumentType.getInteger(commandcontext4, "amount"), ExperienceCommand.Type.POINTS)).then(Commands.literal("points").executes((commandcontext3) -> setExperience(commandcontext3.getSource(), EntityArgument.getPlayers(commandcontext3, "targets"), IntegerArgumentType.getInteger(commandcontext3, "amount"), ExperienceCommand.Type.POINTS))).then(Commands.literal("levels").executes((commandcontext2) -> setExperience(commandcontext2.getSource(), EntityArgument.getPlayers(commandcontext2, "targets"), IntegerArgumentType.getInteger(commandcontext2, "amount"), ExperienceCommand.Type.LEVELS)))))).then(Commands.literal("query").then(Commands.argument("targets", EntityArgument.player()).then(Commands.literal("points").executes((commandcontext1) -> queryExperience(commandcontext1.getSource(), EntityArgument.getPlayer(commandcontext1, "targets"), ExperienceCommand.Type.POINTS))).then(Commands.literal("levels").executes((commandcontext) -> queryExperience(commandcontext.getSource(), EntityArgument.getPlayer(commandcontext, "targets"), ExperienceCommand.Type.LEVELS))))));
      commanddispatcher.register(Commands.literal("xp").requires((commandsourcestack) -> commandsourcestack.hasPermission(2)).redirect(literalcommandnode));
   }

   private static int queryExperience(CommandSourceStack commandsourcestack, ServerPlayer serverplayer, ExperienceCommand.Type experiencecommand_type) {
      int i = experiencecommand_type.query.applyAsInt(serverplayer);
      commandsourcestack.sendSuccess(() -> Component.translatable("commands.experience.query." + experiencecommand_type.name, serverplayer.getDisplayName(), i), false);
      return i;
   }

   private static int addExperience(CommandSourceStack commandsourcestack, Collection<? extends ServerPlayer> collection, int i, ExperienceCommand.Type experiencecommand_type) {
      for(ServerPlayer serverplayer : collection) {
         experiencecommand_type.add.accept(serverplayer, i);
      }

      if (collection.size() == 1) {
         commandsourcestack.sendSuccess(() -> Component.translatable("commands.experience.add." + experiencecommand_type.name + ".success.single", i, collection.iterator().next().getDisplayName()), true);
      } else {
         commandsourcestack.sendSuccess(() -> Component.translatable("commands.experience.add." + experiencecommand_type.name + ".success.multiple", i, collection.size()), true);
      }

      return collection.size();
   }

   private static int setExperience(CommandSourceStack commandsourcestack, Collection<? extends ServerPlayer> collection, int i, ExperienceCommand.Type experiencecommand_type) throws CommandSyntaxException {
      int j = 0;

      for(ServerPlayer serverplayer : collection) {
         if (experiencecommand_type.set.test(serverplayer, i)) {
            ++j;
         }
      }

      if (j == 0) {
         throw ERROR_SET_POINTS_INVALID.create();
      } else {
         if (collection.size() == 1) {
            commandsourcestack.sendSuccess(() -> Component.translatable("commands.experience.set." + experiencecommand_type.name + ".success.single", i, collection.iterator().next().getDisplayName()), true);
         } else {
            commandsourcestack.sendSuccess(() -> Component.translatable("commands.experience.set." + experiencecommand_type.name + ".success.multiple", i, collection.size()), true);
         }

         return collection.size();
      }
   }

   static enum Type {
      POINTS("points", Player::giveExperiencePoints, (serverplayer, integer) -> {
         if (integer >= serverplayer.getXpNeededForNextLevel()) {
            return false;
         } else {
            serverplayer.setExperiencePoints(integer);
            return true;
         }
      }, (serverplayer) -> Mth.floor(serverplayer.experienceProgress * (float)serverplayer.getXpNeededForNextLevel())),
      LEVELS("levels", ServerPlayer::giveExperienceLevels, (serverplayer, integer) -> {
         serverplayer.setExperienceLevels(integer);
         return true;
      }, (serverplayer) -> serverplayer.experienceLevel);

      public final BiConsumer<ServerPlayer, Integer> add;
      public final BiPredicate<ServerPlayer, Integer> set;
      public final String name;
      final ToIntFunction<ServerPlayer> query;

      private Type(String s, BiConsumer<ServerPlayer, Integer> biconsumer, BiPredicate<ServerPlayer, Integer> bipredicate, ToIntFunction<ServerPlayer> tointfunction) {
         this.add = biconsumer;
         this.name = s;
         this.set = bipredicate;
         this.query = tointfunction;
      }
   }
}
