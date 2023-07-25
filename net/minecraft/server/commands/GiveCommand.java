package net.minecraft.server.commands;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.arguments.IntegerArgumentType;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import java.util.Collection;
import net.minecraft.commands.CommandBuildContext;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.commands.arguments.EntityArgument;
import net.minecraft.commands.arguments.item.ItemArgument;
import net.minecraft.commands.arguments.item.ItemInput;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;

public class GiveCommand {
   public static final int MAX_ALLOWED_ITEMSTACKS = 100;

   public static void register(CommandDispatcher<CommandSourceStack> commanddispatcher, CommandBuildContext commandbuildcontext) {
      commanddispatcher.register(Commands.literal("give").requires((commandsourcestack) -> commandsourcestack.hasPermission(2)).then(Commands.argument("targets", EntityArgument.players()).then(Commands.argument("item", ItemArgument.item(commandbuildcontext)).executes((commandcontext1) -> giveItem(commandcontext1.getSource(), ItemArgument.getItem(commandcontext1, "item"), EntityArgument.getPlayers(commandcontext1, "targets"), 1)).then(Commands.argument("count", IntegerArgumentType.integer(1)).executes((commandcontext) -> giveItem(commandcontext.getSource(), ItemArgument.getItem(commandcontext, "item"), EntityArgument.getPlayers(commandcontext, "targets"), IntegerArgumentType.getInteger(commandcontext, "count")))))));
   }

   private static int giveItem(CommandSourceStack commandsourcestack, ItemInput iteminput, Collection<ServerPlayer> collection, int i) throws CommandSyntaxException {
      int j = iteminput.getItem().getMaxStackSize();
      int k = j * 100;
      ItemStack itemstack = iteminput.createItemStack(i, false);
      if (i > k) {
         commandsourcestack.sendFailure(Component.translatable("commands.give.failed.toomanyitems", k, itemstack.getDisplayName()));
         return 0;
      } else {
         for(ServerPlayer serverplayer : collection) {
            int l = i;

            while(l > 0) {
               int i1 = Math.min(j, l);
               l -= i1;
               ItemStack itemstack1 = iteminput.createItemStack(i1, false);
               boolean flag = serverplayer.getInventory().add(itemstack1);
               if (flag && itemstack1.isEmpty()) {
                  itemstack1.setCount(1);
                  ItemEntity itementity1 = serverplayer.drop(itemstack1, false);
                  if (itementity1 != null) {
                     itementity1.makeFakeItem();
                  }

                  serverplayer.level().playSound((Player)null, serverplayer.getX(), serverplayer.getY(), serverplayer.getZ(), SoundEvents.ITEM_PICKUP, SoundSource.PLAYERS, 0.2F, ((serverplayer.getRandom().nextFloat() - serverplayer.getRandom().nextFloat()) * 0.7F + 1.0F) * 2.0F);
                  serverplayer.containerMenu.broadcastChanges();
               } else {
                  ItemEntity itementity = serverplayer.drop(itemstack1, false);
                  if (itementity != null) {
                     itementity.setNoPickUpDelay();
                     itementity.setTarget(serverplayer.getUUID());
                  }
               }
            }
         }

         if (collection.size() == 1) {
            commandsourcestack.sendSuccess(() -> Component.translatable("commands.give.success.single", i, itemstack.getDisplayName(), collection.iterator().next().getDisplayName()), true);
         } else {
            commandsourcestack.sendSuccess(() -> Component.translatable("commands.give.success.single", i, itemstack.getDisplayName(), collection.size()), true);
         }

         return collection.size();
      }
   }
}
