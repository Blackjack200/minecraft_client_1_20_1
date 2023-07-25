package net.minecraft.world.level.block.entity;

import net.minecraft.core.BlockPos;
import net.minecraft.core.NonNullList;
import net.minecraft.core.Vec3i;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.Container;
import net.minecraft.world.ContainerHelper;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.ChestMenu;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.BarrelBlock;
import net.minecraft.world.level.block.state.BlockState;

public class BarrelBlockEntity extends RandomizableContainerBlockEntity {
   private NonNullList<ItemStack> items = NonNullList.withSize(27, ItemStack.EMPTY);
   private final ContainerOpenersCounter openersCounter = new ContainerOpenersCounter() {
      protected void onOpen(Level level, BlockPos blockpos, BlockState blockstate) {
         BarrelBlockEntity.this.playSound(blockstate, SoundEvents.BARREL_OPEN);
         BarrelBlockEntity.this.updateBlockState(blockstate, true);
      }

      protected void onClose(Level level, BlockPos blockpos, BlockState blockstate) {
         BarrelBlockEntity.this.playSound(blockstate, SoundEvents.BARREL_CLOSE);
         BarrelBlockEntity.this.updateBlockState(blockstate, false);
      }

      protected void openerCountChanged(Level level, BlockPos blockpos, BlockState blockstate, int i, int j) {
      }

      protected boolean isOwnContainer(Player player) {
         if (player.containerMenu instanceof ChestMenu) {
            Container container = ((ChestMenu)player.containerMenu).getContainer();
            return container == BarrelBlockEntity.this;
         } else {
            return false;
         }
      }
   };

   public BarrelBlockEntity(BlockPos blockpos, BlockState blockstate) {
      super(BlockEntityType.BARREL, blockpos, blockstate);
   }

   protected void saveAdditional(CompoundTag compoundtag) {
      super.saveAdditional(compoundtag);
      if (!this.trySaveLootTable(compoundtag)) {
         ContainerHelper.saveAllItems(compoundtag, this.items);
      }

   }

   public void load(CompoundTag compoundtag) {
      super.load(compoundtag);
      this.items = NonNullList.withSize(this.getContainerSize(), ItemStack.EMPTY);
      if (!this.tryLoadLootTable(compoundtag)) {
         ContainerHelper.loadAllItems(compoundtag, this.items);
      }

   }

   public int getContainerSize() {
      return 27;
   }

   protected NonNullList<ItemStack> getItems() {
      return this.items;
   }

   protected void setItems(NonNullList<ItemStack> nonnulllist) {
      this.items = nonnulllist;
   }

   protected Component getDefaultName() {
      return Component.translatable("container.barrel");
   }

   protected AbstractContainerMenu createMenu(int i, Inventory inventory) {
      return ChestMenu.threeRows(i, inventory, this);
   }

   public void startOpen(Player player) {
      if (!this.remove && !player.isSpectator()) {
         this.openersCounter.incrementOpeners(player, this.getLevel(), this.getBlockPos(), this.getBlockState());
      }

   }

   public void stopOpen(Player player) {
      if (!this.remove && !player.isSpectator()) {
         this.openersCounter.decrementOpeners(player, this.getLevel(), this.getBlockPos(), this.getBlockState());
      }

   }

   public void recheckOpen() {
      if (!this.remove) {
         this.openersCounter.recheckOpeners(this.getLevel(), this.getBlockPos(), this.getBlockState());
      }

   }

   void updateBlockState(BlockState blockstate, boolean flag) {
      this.level.setBlock(this.getBlockPos(), blockstate.setValue(BarrelBlock.OPEN, Boolean.valueOf(flag)), 3);
   }

   void playSound(BlockState blockstate, SoundEvent soundevent) {
      Vec3i vec3i = blockstate.getValue(BarrelBlock.FACING).getNormal();
      double d0 = (double)this.worldPosition.getX() + 0.5D + (double)vec3i.getX() / 2.0D;
      double d1 = (double)this.worldPosition.getY() + 0.5D + (double)vec3i.getY() / 2.0D;
      double d2 = (double)this.worldPosition.getZ() + 0.5D + (double)vec3i.getZ() / 2.0D;
      this.level.playSound((Player)null, d0, d1, d2, soundevent, SoundSource.BLOCKS, 0.5F, this.level.random.nextFloat() * 0.1F + 0.9F);
   }
}
