package net.minecraft.world.level.block.entity;

import net.minecraft.core.BlockPos;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.Container;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;

public class EnderChestBlockEntity extends BlockEntity implements LidBlockEntity {
   private final ChestLidController chestLidController = new ChestLidController();
   private final ContainerOpenersCounter openersCounter = new ContainerOpenersCounter() {
      protected void onOpen(Level level, BlockPos blockpos, BlockState blockstate) {
         level.playSound((Player)null, (double)blockpos.getX() + 0.5D, (double)blockpos.getY() + 0.5D, (double)blockpos.getZ() + 0.5D, SoundEvents.ENDER_CHEST_OPEN, SoundSource.BLOCKS, 0.5F, level.random.nextFloat() * 0.1F + 0.9F);
      }

      protected void onClose(Level level, BlockPos blockpos, BlockState blockstate) {
         level.playSound((Player)null, (double)blockpos.getX() + 0.5D, (double)blockpos.getY() + 0.5D, (double)blockpos.getZ() + 0.5D, SoundEvents.ENDER_CHEST_CLOSE, SoundSource.BLOCKS, 0.5F, level.random.nextFloat() * 0.1F + 0.9F);
      }

      protected void openerCountChanged(Level level, BlockPos blockpos, BlockState blockstate, int i, int j) {
         level.blockEvent(EnderChestBlockEntity.this.worldPosition, Blocks.ENDER_CHEST, 1, j);
      }

      protected boolean isOwnContainer(Player player) {
         return player.getEnderChestInventory().isActiveChest(EnderChestBlockEntity.this);
      }
   };

   public EnderChestBlockEntity(BlockPos blockpos, BlockState blockstate) {
      super(BlockEntityType.ENDER_CHEST, blockpos, blockstate);
   }

   public static void lidAnimateTick(Level level, BlockPos blockpos, BlockState blockstate, EnderChestBlockEntity enderchestblockentity) {
      enderchestblockentity.chestLidController.tickLid();
   }

   public boolean triggerEvent(int i, int j) {
      if (i == 1) {
         this.chestLidController.shouldBeOpen(j > 0);
         return true;
      } else {
         return super.triggerEvent(i, j);
      }
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

   public boolean stillValid(Player player) {
      return Container.stillValidBlockEntity(this, player);
   }

   public void recheckOpen() {
      if (!this.remove) {
         this.openersCounter.recheckOpeners(this.getLevel(), this.getBlockPos(), this.getBlockState());
      }

   }

   public float getOpenNess(float f) {
      return this.chestLidController.getOpenness(f);
   }
}
