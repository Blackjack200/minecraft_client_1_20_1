package net.minecraft.world.level.block.entity;

import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.FurnaceMenu;
import net.minecraft.world.item.crafting.RecipeType;
import net.minecraft.world.level.block.state.BlockState;

public class FurnaceBlockEntity extends AbstractFurnaceBlockEntity {
   public FurnaceBlockEntity(BlockPos blockpos, BlockState blockstate) {
      super(BlockEntityType.FURNACE, blockpos, blockstate, RecipeType.SMELTING);
   }

   protected Component getDefaultName() {
      return Component.translatable("container.furnace");
   }

   protected AbstractContainerMenu createMenu(int i, Inventory inventory) {
      return new FurnaceMenu(i, inventory, this, this.dataAccess);
   }
}
