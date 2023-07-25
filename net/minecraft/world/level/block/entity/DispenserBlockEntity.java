package net.minecraft.world.level.block.entity;

import net.minecraft.core.BlockPos;
import net.minecraft.core.NonNullList;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.util.RandomSource;
import net.minecraft.world.ContainerHelper;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.DispenserMenu;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.state.BlockState;

public class DispenserBlockEntity extends RandomizableContainerBlockEntity {
   public static final int CONTAINER_SIZE = 9;
   private NonNullList<ItemStack> items = NonNullList.withSize(9, ItemStack.EMPTY);

   protected DispenserBlockEntity(BlockEntityType<?> blockentitytype, BlockPos blockpos, BlockState blockstate) {
      super(blockentitytype, blockpos, blockstate);
   }

   public DispenserBlockEntity(BlockPos blockpos, BlockState blockstate) {
      this(BlockEntityType.DISPENSER, blockpos, blockstate);
   }

   public int getContainerSize() {
      return 9;
   }

   public int getRandomSlot(RandomSource randomsource) {
      this.unpackLootTable((Player)null);
      int i = -1;
      int j = 1;

      for(int k = 0; k < this.items.size(); ++k) {
         if (!this.items.get(k).isEmpty() && randomsource.nextInt(j++) == 0) {
            i = k;
         }
      }

      return i;
   }

   public int addItem(ItemStack itemstack) {
      for(int i = 0; i < this.items.size(); ++i) {
         if (this.items.get(i).isEmpty()) {
            this.setItem(i, itemstack);
            return i;
         }
      }

      return -1;
   }

   protected Component getDefaultName() {
      return Component.translatable("container.dispenser");
   }

   public void load(CompoundTag compoundtag) {
      super.load(compoundtag);
      this.items = NonNullList.withSize(this.getContainerSize(), ItemStack.EMPTY);
      if (!this.tryLoadLootTable(compoundtag)) {
         ContainerHelper.loadAllItems(compoundtag, this.items);
      }

   }

   protected void saveAdditional(CompoundTag compoundtag) {
      super.saveAdditional(compoundtag);
      if (!this.trySaveLootTable(compoundtag)) {
         ContainerHelper.saveAllItems(compoundtag, this.items);
      }

   }

   protected NonNullList<ItemStack> getItems() {
      return this.items;
   }

   protected void setItems(NonNullList<ItemStack> nonnulllist) {
      this.items = nonnulllist;
   }

   protected AbstractContainerMenu createMenu(int i, Inventory inventory) {
      return new DispenserMenu(i, inventory, this);
   }
}
