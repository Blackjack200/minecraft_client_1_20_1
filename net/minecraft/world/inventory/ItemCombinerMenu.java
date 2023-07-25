package net.minecraft.world.inventory;

import java.util.List;
import javax.annotation.Nullable;
import net.minecraft.world.Container;
import net.minecraft.world.SimpleContainer;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.state.BlockState;

public abstract class ItemCombinerMenu extends AbstractContainerMenu {
   private static final int INVENTORY_SLOTS_PER_ROW = 9;
   private static final int INVENTORY_SLOTS_PER_COLUMN = 3;
   protected final ContainerLevelAccess access;
   protected final Player player;
   protected final Container inputSlots;
   private final List<Integer> inputSlotIndexes;
   protected final ResultContainer resultSlots = new ResultContainer();
   private final int resultSlotIndex;

   protected abstract boolean mayPickup(Player player, boolean flag);

   protected abstract void onTake(Player player, ItemStack itemstack);

   protected abstract boolean isValidBlock(BlockState blockstate);

   public ItemCombinerMenu(@Nullable MenuType<?> menutype, int i, Inventory inventory, ContainerLevelAccess containerlevelaccess) {
      super(menutype, i);
      this.access = containerlevelaccess;
      this.player = inventory.player;
      ItemCombinerMenuSlotDefinition itemcombinermenuslotdefinition = this.createInputSlotDefinitions();
      this.inputSlots = this.createContainer(itemcombinermenuslotdefinition.getNumOfInputSlots());
      this.inputSlotIndexes = itemcombinermenuslotdefinition.getInputSlotIndexes();
      this.resultSlotIndex = itemcombinermenuslotdefinition.getResultSlotIndex();
      this.createInputSlots(itemcombinermenuslotdefinition);
      this.createResultSlot(itemcombinermenuslotdefinition);
      this.createInventorySlots(inventory);
   }

   private void createInputSlots(ItemCombinerMenuSlotDefinition itemcombinermenuslotdefinition) {
      for(final ItemCombinerMenuSlotDefinition.SlotDefinition itemcombinermenuslotdefinition_slotdefinition : itemcombinermenuslotdefinition.getSlots()) {
         this.addSlot(new Slot(this.inputSlots, itemcombinermenuslotdefinition_slotdefinition.slotIndex(), itemcombinermenuslotdefinition_slotdefinition.x(), itemcombinermenuslotdefinition_slotdefinition.y()) {
            public boolean mayPlace(ItemStack itemstack) {
               return itemcombinermenuslotdefinition_slotdefinition.mayPlace().test(itemstack);
            }
         });
      }

   }

   private void createResultSlot(ItemCombinerMenuSlotDefinition itemcombinermenuslotdefinition) {
      this.addSlot(new Slot(this.resultSlots, itemcombinermenuslotdefinition.getResultSlot().slotIndex(), itemcombinermenuslotdefinition.getResultSlot().x(), itemcombinermenuslotdefinition.getResultSlot().y()) {
         public boolean mayPlace(ItemStack itemstack) {
            return false;
         }

         public boolean mayPickup(Player player) {
            return ItemCombinerMenu.this.mayPickup(player, this.hasItem());
         }

         public void onTake(Player player, ItemStack itemstack) {
            ItemCombinerMenu.this.onTake(player, itemstack);
         }
      });
   }

   private void createInventorySlots(Inventory inventory) {
      for(int i = 0; i < 3; ++i) {
         for(int j = 0; j < 9; ++j) {
            this.addSlot(new Slot(inventory, j + i * 9 + 9, 8 + j * 18, 84 + i * 18));
         }
      }

      for(int k = 0; k < 9; ++k) {
         this.addSlot(new Slot(inventory, k, 8 + k * 18, 142));
      }

   }

   public abstract void createResult();

   protected abstract ItemCombinerMenuSlotDefinition createInputSlotDefinitions();

   private SimpleContainer createContainer(int i) {
      return new SimpleContainer(i) {
         public void setChanged() {
            super.setChanged();
            ItemCombinerMenu.this.slotsChanged(this);
         }
      };
   }

   public void slotsChanged(Container container) {
      super.slotsChanged(container);
      if (container == this.inputSlots) {
         this.createResult();
      }

   }

   public void removed(Player player) {
      super.removed(player);
      this.access.execute((level, blockpos) -> this.clearContainer(player, this.inputSlots));
   }

   public boolean stillValid(Player player) {
      return this.access.evaluate((level, blockpos) -> !this.isValidBlock(level.getBlockState(blockpos)) ? false : player.distanceToSqr((double)blockpos.getX() + 0.5D, (double)blockpos.getY() + 0.5D, (double)blockpos.getZ() + 0.5D) <= 64.0D, true);
   }

   public ItemStack quickMoveStack(Player player, int i) {
      ItemStack itemstack = ItemStack.EMPTY;
      Slot slot = this.slots.get(i);
      if (slot != null && slot.hasItem()) {
         ItemStack itemstack1 = slot.getItem();
         itemstack = itemstack1.copy();
         int j = this.getInventorySlotStart();
         int k = this.getUseRowEnd();
         if (i == this.getResultSlot()) {
            if (!this.moveItemStackTo(itemstack1, j, k, true)) {
               return ItemStack.EMPTY;
            }

            slot.onQuickCraft(itemstack1, itemstack);
         } else if (this.inputSlotIndexes.contains(i)) {
            if (!this.moveItemStackTo(itemstack1, j, k, false)) {
               return ItemStack.EMPTY;
            }
         } else if (this.canMoveIntoInputSlots(itemstack1) && i >= this.getInventorySlotStart() && i < this.getUseRowEnd()) {
            int l = this.getSlotToQuickMoveTo(itemstack);
            if (!this.moveItemStackTo(itemstack1, l, this.getResultSlot(), false)) {
               return ItemStack.EMPTY;
            }
         } else if (i >= this.getInventorySlotStart() && i < this.getInventorySlotEnd()) {
            if (!this.moveItemStackTo(itemstack1, this.getUseRowStart(), this.getUseRowEnd(), false)) {
               return ItemStack.EMPTY;
            }
         } else if (i >= this.getUseRowStart() && i < this.getUseRowEnd() && !this.moveItemStackTo(itemstack1, this.getInventorySlotStart(), this.getInventorySlotEnd(), false)) {
            return ItemStack.EMPTY;
         }

         if (itemstack1.isEmpty()) {
            slot.setByPlayer(ItemStack.EMPTY);
         } else {
            slot.setChanged();
         }

         if (itemstack1.getCount() == itemstack.getCount()) {
            return ItemStack.EMPTY;
         }

         slot.onTake(player, itemstack1);
      }

      return itemstack;
   }

   protected boolean canMoveIntoInputSlots(ItemStack itemstack) {
      return true;
   }

   public int getSlotToQuickMoveTo(ItemStack itemstack) {
      return this.inputSlots.isEmpty() ? 0 : this.inputSlotIndexes.get(0);
   }

   public int getResultSlot() {
      return this.resultSlotIndex;
   }

   private int getInventorySlotStart() {
      return this.getResultSlot() + 1;
   }

   private int getInventorySlotEnd() {
      return this.getInventorySlotStart() + 27;
   }

   private int getUseRowStart() {
      return this.getInventorySlotEnd();
   }

   private int getUseRowEnd() {
      return this.getUseRowStart() + 9;
   }
}
