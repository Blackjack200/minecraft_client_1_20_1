package net.minecraft.world;

import java.util.Set;
import java.util.function.Predicate;
import net.minecraft.core.BlockPos;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;

public interface Container extends Clearable {
   int LARGE_MAX_STACK_SIZE = 64;
   int DEFAULT_DISTANCE_LIMIT = 8;

   int getContainerSize();

   boolean isEmpty();

   ItemStack getItem(int i);

   ItemStack removeItem(int i, int j);

   ItemStack removeItemNoUpdate(int i);

   void setItem(int i, ItemStack itemstack);

   default int getMaxStackSize() {
      return 64;
   }

   void setChanged();

   boolean stillValid(Player player);

   default void startOpen(Player player) {
   }

   default void stopOpen(Player player) {
   }

   default boolean canPlaceItem(int i, ItemStack itemstack) {
      return true;
   }

   default boolean canTakeItem(Container container, int i, ItemStack itemstack) {
      return true;
   }

   default int countItem(Item item) {
      int i = 0;

      for(int j = 0; j < this.getContainerSize(); ++j) {
         ItemStack itemstack = this.getItem(j);
         if (itemstack.getItem().equals(item)) {
            i += itemstack.getCount();
         }
      }

      return i;
   }

   default boolean hasAnyOf(Set<Item> set) {
      return this.hasAnyMatching((itemstack) -> !itemstack.isEmpty() && set.contains(itemstack.getItem()));
   }

   default boolean hasAnyMatching(Predicate<ItemStack> predicate) {
      for(int i = 0; i < this.getContainerSize(); ++i) {
         ItemStack itemstack = this.getItem(i);
         if (predicate.test(itemstack)) {
            return true;
         }
      }

      return false;
   }

   static boolean stillValidBlockEntity(BlockEntity blockentity, Player player) {
      return stillValidBlockEntity(blockentity, player, 8);
   }

   static boolean stillValidBlockEntity(BlockEntity blockentity, Player player, int i) {
      Level level = blockentity.getLevel();
      BlockPos blockpos = blockentity.getBlockPos();
      if (level == null) {
         return false;
      } else if (level.getBlockEntity(blockpos) != blockentity) {
         return false;
      } else {
         return player.distanceToSqr((double)blockpos.getX() + 0.5D, (double)blockpos.getY() + 0.5D, (double)blockpos.getZ() + 0.5D) <= (double)(i * i);
      }
   }
}
