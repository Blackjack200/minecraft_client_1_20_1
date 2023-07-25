package net.minecraft.world.level.block.entity;

import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.BlastFurnaceMenu;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.RecipeType;
import net.minecraft.world.level.block.state.BlockState;

public class BlastFurnaceBlockEntity extends AbstractFurnaceBlockEntity {
   public BlastFurnaceBlockEntity(BlockPos blockpos, BlockState blockstate) {
      super(BlockEntityType.BLAST_FURNACE, blockpos, blockstate, RecipeType.BLASTING);
   }

   protected Component getDefaultName() {
      return Component.translatable("container.blast_furnace");
   }

   protected int getBurnDuration(ItemStack itemstack) {
      return super.getBurnDuration(itemstack) / 2;
   }

   protected AbstractContainerMenu createMenu(int i, Inventory inventory) {
      return new BlastFurnaceMenu(i, inventory, this, this.dataAccess);
   }
}
