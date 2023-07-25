package net.minecraft.server.commands;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.arguments.IntegerArgumentType;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.brigadier.exceptions.DynamicCommandExceptionType;
import java.util.Collection;
import java.util.Collections;
import java.util.function.Predicate;
import net.minecraft.commands.CommandBuildContext;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.commands.arguments.EntityArgument;
import net.minecraft.commands.arguments.item.ItemPredicateArgument;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.item.ItemStack;

public class ClearInventoryCommands {
   private static final DynamicCommandExceptionType ERROR_SINGLE = new DynamicCommandExceptionType((object) -> Component.translatable("clear.failed.single", object));
   private static final DynamicCommandExceptionType ERROR_MULTIPLE = new DynamicCommandExceptionType((object) -> Component.translatable("clear.failed.multiple", object));

   public static void register(CommandDispatcher<CommandSourceStack> commanddispatcher, CommandBuildContext commandbuildcontext) {
      commanddispatcher.register(Commands.literal("clear").requires((commandsourcestack) -> commandsourcestack.hasPermission(2)).executes((commandcontext3) -> clearInventory(commandcontext3.getSource(), Collections.singleton(commandcontext3.getSource().getPlayerOrException()), (itemstack1) -> true, -1)).then(Commands.argument("targets", EntityArgument.players()).executes((commandcontext2) -> clearInventory(commandcontext2.getSource(), EntityArgument.getPlayers(commandcontext2, "targets"), (itemstack) -> true, -1)).then(Commands.argument("item", ItemPredicateArgument.itemPredicate(commandbuildcontext)).executes((commandcontext1) -> clearInventory(commandcontext1.getSource(), EntityArgument.getPlayers(commandcontext1, "targets"), ItemPredicateArgument.getItemPredicate(commandcontext1, "item"), -1)).then(Commands.argument("maxCount", IntegerArgumentType.integer(0)).executes((commandcontext) -> clearInventory(commandcontext.getSource(), EntityArgument.getPlayers(commandcontext, "targets"), ItemPredicateArgument.getItemPredicate(commandcontext, "item"), IntegerArgumentType.getInteger(commandcontext, "maxCount")))))));
   }

   private static int clearInventory(CommandSourceStack commandsourcestack, Collection<ServerPlayer> collection, Predicate<ItemStack> predicate, int i) throws CommandSyntaxException {
      int j = 0;

      for(ServerPlayer serverplayer : collection) {
         j += serverplayer.getInventory().clearOrCountMatchingItems(predicate, i, serverplayer.inventoryMenu.getCraftSlots());
         serverplayer.containerMenu.broadcastChanges();
         serverplayer.inventoryMenu.slotsChanged(serverplayer.getInventory());
      }

      if (j == 0) {
         if (collection.size() == 1) {
            throw ERROR_SINGLE.create(collection.iterator().next().getName());
         } else {
            throw ERROR_MULTIPLE.create(collection.size());
         }
      } else {
         int k = j;
         if (i == 0) {
            if (collection.size() == 1) {
               commandsourcestack.sendSuccess(() -> Component.translatable("commands.clear.test.single", k, collection.iterator().next().getDisplayName()), true);
            } else {
               commandsourcestack.sendSuccess(() -> Component.translatable("commands.clear.test.multiple", k, collection.size()), true);
            }
         } else if (collection.size() == 1) {
            commandsourcestack.sendSuccess(() -> Component.translatable("commands.clear.success.single", k, collection.iterator().next().getDisplayName()), true);
         } else {
            commandsourcestack.sendSuccess(() -> Component.translatable("commands.clear.success.multiple", k, collection.size()), true);
         }

         return j;
      }
   }
}
