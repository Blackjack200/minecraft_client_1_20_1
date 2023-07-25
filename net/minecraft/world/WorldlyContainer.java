package net.minecraft.world;

import javax.annotation.Nullable;
import net.minecraft.core.Direction;
import net.minecraft.world.item.ItemStack;

public interface WorldlyContainer extends Container {
   int[] getSlotsForFace(Direction direction);

   boolean canPlaceItemThroughFace(int i, ItemStack itemstack, @Nullable Direction direction);

   boolean canTakeItemThroughFace(int i, ItemStack itemstack, Direction direction);
}
