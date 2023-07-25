package net.minecraft.world.level.block.entity;

import java.util.List;
import java.util.function.BooleanSupplier;
import java.util.stream.Collectors;
import java.util.stream.IntStream;
import javax.annotation.Nullable;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.NonNullList;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.world.Container;
import net.minecraft.world.ContainerHelper;
import net.minecraft.world.WorldlyContainer;
import net.minecraft.world.WorldlyContainerHolder;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntitySelector;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.HopperMenu;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.ChestBlock;
import net.minecraft.world.level.block.HopperBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.shapes.BooleanOp;
import net.minecraft.world.phys.shapes.Shapes;

public class HopperBlockEntity extends RandomizableContainerBlockEntity implements Hopper {
   public static final int MOVE_ITEM_SPEED = 8;
   public static final int HOPPER_CONTAINER_SIZE = 5;
   private NonNullList<ItemStack> items = NonNullList.withSize(5, ItemStack.EMPTY);
   private int cooldownTime = -1;
   private long tickedGameTime;

   public HopperBlockEntity(BlockPos blockpos, BlockState blockstate) {
      super(BlockEntityType.HOPPER, blockpos, blockstate);
   }

   public void load(CompoundTag compoundtag) {
      super.load(compoundtag);
      this.items = NonNullList.withSize(this.getContainerSize(), ItemStack.EMPTY);
      if (!this.tryLoadLootTable(compoundtag)) {
         ContainerHelper.loadAllItems(compoundtag, this.items);
      }

      this.cooldownTime = compoundtag.getInt("TransferCooldown");
   }

   protected void saveAdditional(CompoundTag compoundtag) {
      super.saveAdditional(compoundtag);
      if (!this.trySaveLootTable(compoundtag)) {
         ContainerHelper.saveAllItems(compoundtag, this.items);
      }

      compoundtag.putInt("TransferCooldown", this.cooldownTime);
   }

   public int getContainerSize() {
      return this.items.size();
   }

   public ItemStack removeItem(int i, int j) {
      this.unpackLootTable((Player)null);
      return ContainerHelper.removeItem(this.getItems(), i, j);
   }

   public void setItem(int i, ItemStack itemstack) {
      this.unpackLootTable((Player)null);
      this.getItems().set(i, itemstack);
      if (itemstack.getCount() > this.getMaxStackSize()) {
         itemstack.setCount(this.getMaxStackSize());
      }

   }

   protected Component getDefaultName() {
      return Component.translatable("container.hopper");
   }

   public static void pushItemsTick(Level level, BlockPos blockpos, BlockState blockstate, HopperBlockEntity hopperblockentity) {
      --hopperblockentity.cooldownTime;
      hopperblockentity.tickedGameTime = level.getGameTime();
      if (!hopperblockentity.isOnCooldown()) {
         hopperblockentity.setCooldown(0);
         tryMoveItems(level, blockpos, blockstate, hopperblockentity, () -> suckInItems(level, hopperblockentity));
      }

   }

   private static boolean tryMoveItems(Level level, BlockPos blockpos, BlockState blockstate, HopperBlockEntity hopperblockentity, BooleanSupplier booleansupplier) {
      if (level.isClientSide) {
         return false;
      } else {
         if (!hopperblockentity.isOnCooldown() && blockstate.getValue(HopperBlock.ENABLED)) {
            boolean flag = false;
            if (!hopperblockentity.isEmpty()) {
               flag = ejectItems(level, blockpos, blockstate, hopperblockentity);
            }

            if (!hopperblockentity.inventoryFull()) {
               flag |= booleansupplier.getAsBoolean();
            }

            if (flag) {
               hopperblockentity.setCooldown(8);
               setChanged(level, blockpos, blockstate);
               return true;
            }
         }

         return false;
      }
   }

   private boolean inventoryFull() {
      for(ItemStack itemstack : this.items) {
         if (itemstack.isEmpty() || itemstack.getCount() != itemstack.getMaxStackSize()) {
            return false;
         }
      }

      return true;
   }

   private static boolean ejectItems(Level level, BlockPos blockpos, BlockState blockstate, Container container) {
      Container container1 = getAttachedContainer(level, blockpos, blockstate);
      if (container1 == null) {
         return false;
      } else {
         Direction direction = blockstate.getValue(HopperBlock.FACING).getOpposite();
         if (isFullContainer(container1, direction)) {
            return false;
         } else {
            for(int i = 0; i < container.getContainerSize(); ++i) {
               if (!container.getItem(i).isEmpty()) {
                  ItemStack itemstack = container.getItem(i).copy();
                  ItemStack itemstack1 = addItem(container, container1, container.removeItem(i, 1), direction);
                  if (itemstack1.isEmpty()) {
                     container1.setChanged();
                     return true;
                  }

                  container.setItem(i, itemstack);
               }
            }

            return false;
         }
      }
   }

   private static IntStream getSlots(Container container, Direction direction) {
      return container instanceof WorldlyContainer ? IntStream.of(((WorldlyContainer)container).getSlotsForFace(direction)) : IntStream.range(0, container.getContainerSize());
   }

   private static boolean isFullContainer(Container container, Direction direction) {
      return getSlots(container, direction).allMatch((i) -> {
         ItemStack itemstack = container.getItem(i);
         return itemstack.getCount() >= itemstack.getMaxStackSize();
      });
   }

   private static boolean isEmptyContainer(Container container, Direction direction) {
      return getSlots(container, direction).allMatch((i) -> container.getItem(i).isEmpty());
   }

   public static boolean suckInItems(Level level, Hopper hopper) {
      Container container = getSourceContainer(level, hopper);
      if (container != null) {
         Direction direction = Direction.DOWN;
         return isEmptyContainer(container, direction) ? false : getSlots(container, direction).anyMatch((i) -> tryTakeInItemFromSlot(hopper, container, i, direction));
      } else {
         for(ItemEntity itementity : getItemsAtAndAbove(level, hopper)) {
            if (addItem(hopper, itementity)) {
               return true;
            }
         }

         return false;
      }
   }

   private static boolean tryTakeInItemFromSlot(Hopper hopper, Container container, int i, Direction direction) {
      ItemStack itemstack = container.getItem(i);
      if (!itemstack.isEmpty() && canTakeItemFromContainer(hopper, container, itemstack, i, direction)) {
         ItemStack itemstack1 = itemstack.copy();
         ItemStack itemstack2 = addItem(container, hopper, container.removeItem(i, 1), (Direction)null);
         if (itemstack2.isEmpty()) {
            container.setChanged();
            return true;
         }

         container.setItem(i, itemstack1);
      }

      return false;
   }

   public static boolean addItem(Container container, ItemEntity itementity) {
      boolean flag = false;
      ItemStack itemstack = itementity.getItem().copy();
      ItemStack itemstack1 = addItem((Container)null, container, itemstack, (Direction)null);
      if (itemstack1.isEmpty()) {
         flag = true;
         itementity.discard();
      } else {
         itementity.setItem(itemstack1);
      }

      return flag;
   }

   public static ItemStack addItem(@Nullable Container container, Container container1, ItemStack itemstack, @Nullable Direction direction) {
      if (container1 instanceof WorldlyContainer worldlycontainer) {
         if (direction != null) {
            int[] aint = worldlycontainer.getSlotsForFace(direction);

            for(int i = 0; i < aint.length && !itemstack.isEmpty(); ++i) {
               itemstack = tryMoveInItem(container, container1, itemstack, aint[i], direction);
            }

            return itemstack;
         }
      }

      int j = container1.getContainerSize();

      for(int k = 0; k < j && !itemstack.isEmpty(); ++k) {
         itemstack = tryMoveInItem(container, container1, itemstack, k, direction);
      }

      return itemstack;
   }

   private static boolean canPlaceItemInContainer(Container container, ItemStack itemstack, int i, @Nullable Direction direction) {
      if (!container.canPlaceItem(i, itemstack)) {
         return false;
      } else {
         if (container instanceof WorldlyContainer) {
            WorldlyContainer worldlycontainer = (WorldlyContainer)container;
            if (!worldlycontainer.canPlaceItemThroughFace(i, itemstack, direction)) {
               return false;
            }
         }

         return true;
      }
   }

   private static boolean canTakeItemFromContainer(Container container, Container container1, ItemStack itemstack, int i, Direction direction) {
      if (!container1.canTakeItem(container, i, itemstack)) {
         return false;
      } else {
         if (container1 instanceof WorldlyContainer) {
            WorldlyContainer worldlycontainer = (WorldlyContainer)container1;
            if (!worldlycontainer.canTakeItemThroughFace(i, itemstack, direction)) {
               return false;
            }
         }

         return true;
      }
   }

   private static ItemStack tryMoveInItem(@Nullable Container container, Container container1, ItemStack itemstack, int i, @Nullable Direction direction) {
      ItemStack itemstack1 = container1.getItem(i);
      if (canPlaceItemInContainer(container1, itemstack, i, direction)) {
         boolean flag = false;
         boolean flag1 = container1.isEmpty();
         if (itemstack1.isEmpty()) {
            container1.setItem(i, itemstack);
            itemstack = ItemStack.EMPTY;
            flag = true;
         } else if (canMergeItems(itemstack1, itemstack)) {
            int j = itemstack.getMaxStackSize() - itemstack1.getCount();
            int k = Math.min(itemstack.getCount(), j);
            itemstack.shrink(k);
            itemstack1.grow(k);
            flag = k > 0;
         }

         if (flag) {
            if (flag1 && container1 instanceof HopperBlockEntity) {
               HopperBlockEntity hopperblockentity = (HopperBlockEntity)container1;
               if (!hopperblockentity.isOnCustomCooldown()) {
                  int l = 0;
                  if (container instanceof HopperBlockEntity) {
                     HopperBlockEntity hopperblockentity1 = (HopperBlockEntity)container;
                     if (hopperblockentity.tickedGameTime >= hopperblockentity1.tickedGameTime) {
                        l = 1;
                     }
                  }

                  hopperblockentity.setCooldown(8 - l);
               }
            }

            container1.setChanged();
         }
      }

      return itemstack;
   }

   @Nullable
   private static Container getAttachedContainer(Level level, BlockPos blockpos, BlockState blockstate) {
      Direction direction = blockstate.getValue(HopperBlock.FACING);
      return getContainerAt(level, blockpos.relative(direction));
   }

   @Nullable
   private static Container getSourceContainer(Level level, Hopper hopper) {
      return getContainerAt(level, hopper.getLevelX(), hopper.getLevelY() + 1.0D, hopper.getLevelZ());
   }

   public static List<ItemEntity> getItemsAtAndAbove(Level level, Hopper hopper) {
      return hopper.getSuckShape().toAabbs().stream().flatMap((aabb) -> level.getEntitiesOfClass(ItemEntity.class, aabb.move(hopper.getLevelX() - 0.5D, hopper.getLevelY() - 0.5D, hopper.getLevelZ() - 0.5D), EntitySelector.ENTITY_STILL_ALIVE).stream()).collect(Collectors.toList());
   }

   @Nullable
   public static Container getContainerAt(Level level, BlockPos blockpos) {
      return getContainerAt(level, (double)blockpos.getX() + 0.5D, (double)blockpos.getY() + 0.5D, (double)blockpos.getZ() + 0.5D);
   }

   @Nullable
   private static Container getContainerAt(Level level, double d0, double d1, double d2) {
      Container container = null;
      BlockPos blockpos = BlockPos.containing(d0, d1, d2);
      BlockState blockstate = level.getBlockState(blockpos);
      Block block = blockstate.getBlock();
      if (block instanceof WorldlyContainerHolder) {
         container = ((WorldlyContainerHolder)block).getContainer(blockstate, level, blockpos);
      } else if (blockstate.hasBlockEntity()) {
         BlockEntity blockentity = level.getBlockEntity(blockpos);
         if (blockentity instanceof Container) {
            container = (Container)blockentity;
            if (container instanceof ChestBlockEntity && block instanceof ChestBlock) {
               container = ChestBlock.getContainer((ChestBlock)block, blockstate, level, blockpos, true);
            }
         }
      }

      if (container == null) {
         List<Entity> list = level.getEntities((Entity)null, new AABB(d0 - 0.5D, d1 - 0.5D, d2 - 0.5D, d0 + 0.5D, d1 + 0.5D, d2 + 0.5D), EntitySelector.CONTAINER_ENTITY_SELECTOR);
         if (!list.isEmpty()) {
            container = (Container)list.get(level.random.nextInt(list.size()));
         }
      }

      return container;
   }

   private static boolean canMergeItems(ItemStack itemstack, ItemStack itemstack1) {
      return itemstack.getCount() <= itemstack.getMaxStackSize() && ItemStack.isSameItemSameTags(itemstack, itemstack1);
   }

   public double getLevelX() {
      return (double)this.worldPosition.getX() + 0.5D;
   }

   public double getLevelY() {
      return (double)this.worldPosition.getY() + 0.5D;
   }

   public double getLevelZ() {
      return (double)this.worldPosition.getZ() + 0.5D;
   }

   private void setCooldown(int i) {
      this.cooldownTime = i;
   }

   private boolean isOnCooldown() {
      return this.cooldownTime > 0;
   }

   private boolean isOnCustomCooldown() {
      return this.cooldownTime > 8;
   }

   protected NonNullList<ItemStack> getItems() {
      return this.items;
   }

   protected void setItems(NonNullList<ItemStack> nonnulllist) {
      this.items = nonnulllist;
   }

   public static void entityInside(Level level, BlockPos blockpos, BlockState blockstate, Entity entity, HopperBlockEntity hopperblockentity) {
      if (entity instanceof ItemEntity && Shapes.joinIsNotEmpty(Shapes.create(entity.getBoundingBox().move((double)(-blockpos.getX()), (double)(-blockpos.getY()), (double)(-blockpos.getZ()))), hopperblockentity.getSuckShape(), BooleanOp.AND)) {
         tryMoveItems(level, blockpos, blockstate, hopperblockentity, () -> addItem(hopperblockentity, (ItemEntity)entity));
      }

   }

   protected AbstractContainerMenu createMenu(int i, Inventory inventory) {
      return new HopperMenu(i, inventory, this);
   }
}
