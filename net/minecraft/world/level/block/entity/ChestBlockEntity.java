package net.minecraft.world.level.block.entity;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.NonNullList;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.CompoundContainer;
import net.minecraft.world.Container;
import net.minecraft.world.ContainerHelper;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.ChestMenu;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.ChestBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.ChestType;

public class ChestBlockEntity extends RandomizableContainerBlockEntity implements LidBlockEntity {
   private static final int EVENT_SET_OPEN_COUNT = 1;
   private NonNullList<ItemStack> items = NonNullList.withSize(27, ItemStack.EMPTY);
   private final ContainerOpenersCounter openersCounter = new ContainerOpenersCounter() {
      protected void onOpen(Level level, BlockPos blockpos, BlockState blockstate) {
         ChestBlockEntity.playSound(level, blockpos, blockstate, SoundEvents.CHEST_OPEN);
      }

      protected void onClose(Level level, BlockPos blockpos, BlockState blockstate) {
         ChestBlockEntity.playSound(level, blockpos, blockstate, SoundEvents.CHEST_CLOSE);
      }

      protected void openerCountChanged(Level level, BlockPos blockpos, BlockState blockstate, int i, int j) {
         ChestBlockEntity.this.signalOpenCount(level, blockpos, blockstate, i, j);
      }

      protected boolean isOwnContainer(Player player) {
         if (!(player.containerMenu instanceof ChestMenu)) {
            return false;
         } else {
            Container container = ((ChestMenu)player.containerMenu).getContainer();
            return container == ChestBlockEntity.this || container instanceof CompoundContainer && ((CompoundContainer)container).contains(ChestBlockEntity.this);
         }
      }
   };
   private final ChestLidController chestLidController = new ChestLidController();

   protected ChestBlockEntity(BlockEntityType<?> blockentitytype, BlockPos blockpos, BlockState blockstate) {
      super(blockentitytype, blockpos, blockstate);
   }

   public ChestBlockEntity(BlockPos blockpos, BlockState blockstate) {
      this(BlockEntityType.CHEST, blockpos, blockstate);
   }

   public int getContainerSize() {
      return 27;
   }

   protected Component getDefaultName() {
      return Component.translatable("container.chest");
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

   public static void lidAnimateTick(Level level, BlockPos blockpos, BlockState blockstate, ChestBlockEntity chestblockentity) {
      chestblockentity.chestLidController.tickLid();
   }

   static void playSound(Level level, BlockPos blockpos, BlockState blockstate, SoundEvent soundevent) {
      ChestType chesttype = blockstate.getValue(ChestBlock.TYPE);
      if (chesttype != ChestType.LEFT) {
         double d0 = (double)blockpos.getX() + 0.5D;
         double d1 = (double)blockpos.getY() + 0.5D;
         double d2 = (double)blockpos.getZ() + 0.5D;
         if (chesttype == ChestType.RIGHT) {
            Direction direction = ChestBlock.getConnectedDirection(blockstate);
            d0 += (double)direction.getStepX() * 0.5D;
            d2 += (double)direction.getStepZ() * 0.5D;
         }

         level.playSound((Player)null, d0, d1, d2, soundevent, SoundSource.BLOCKS, 0.5F, level.random.nextFloat() * 0.1F + 0.9F);
      }
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

   protected NonNullList<ItemStack> getItems() {
      return this.items;
   }

   protected void setItems(NonNullList<ItemStack> nonnulllist) {
      this.items = nonnulllist;
   }

   public float getOpenNess(float f) {
      return this.chestLidController.getOpenness(f);
   }

   public static int getOpenCount(BlockGetter blockgetter, BlockPos blockpos) {
      BlockState blockstate = blockgetter.getBlockState(blockpos);
      if (blockstate.hasBlockEntity()) {
         BlockEntity blockentity = blockgetter.getBlockEntity(blockpos);
         if (blockentity instanceof ChestBlockEntity) {
            return ((ChestBlockEntity)blockentity).openersCounter.getOpenerCount();
         }
      }

      return 0;
   }

   public static void swapContents(ChestBlockEntity chestblockentity, ChestBlockEntity chestblockentity1) {
      NonNullList<ItemStack> nonnulllist = chestblockentity.getItems();
      chestblockentity.setItems(chestblockentity1.getItems());
      chestblockentity1.setItems(nonnulllist);
   }

   protected AbstractContainerMenu createMenu(int i, Inventory inventory) {
      return ChestMenu.threeRows(i, inventory, this);
   }

   public void recheckOpen() {
      if (!this.remove) {
         this.openersCounter.recheckOpeners(this.getLevel(), this.getBlockPos(), this.getBlockState());
      }

   }

   protected void signalOpenCount(Level level, BlockPos blockpos, BlockState blockstate, int i, int j) {
      Block block = blockstate.getBlock();
      level.blockEvent(blockpos, block, 1, j);
   }
}
