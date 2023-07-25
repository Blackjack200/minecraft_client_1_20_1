package net.minecraft.world.item;

import java.util.stream.Stream;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;

public class ItemUtils {
   public static InteractionResultHolder<ItemStack> startUsingInstantly(Level level, Player player, InteractionHand interactionhand) {
      player.startUsingItem(interactionhand);
      return InteractionResultHolder.consume(player.getItemInHand(interactionhand));
   }

   public static ItemStack createFilledResult(ItemStack itemstack, Player player, ItemStack itemstack1, boolean flag) {
      boolean flag1 = player.getAbilities().instabuild;
      if (flag && flag1) {
         if (!player.getInventory().contains(itemstack1)) {
            player.getInventory().add(itemstack1);
         }

         return itemstack;
      } else {
         if (!flag1) {
            itemstack.shrink(1);
         }

         if (itemstack.isEmpty()) {
            return itemstack1;
         } else {
            if (!player.getInventory().add(itemstack1)) {
               player.drop(itemstack1, false);
            }

            return itemstack;
         }
      }
   }

   public static ItemStack createFilledResult(ItemStack itemstack, Player player, ItemStack itemstack1) {
      return createFilledResult(itemstack, player, itemstack1, true);
   }

   public static void onContainerDestroyed(ItemEntity itementity, Stream<ItemStack> stream) {
      Level level = itementity.level();
      if (!level.isClientSide) {
         stream.forEach((itemstack) -> level.addFreshEntity(new ItemEntity(level, itementity.getX(), itementity.getY(), itementity.getZ(), itemstack)));
      }
   }
}
