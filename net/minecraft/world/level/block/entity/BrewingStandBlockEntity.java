package net.minecraft.world.level.block.entity;

import java.util.Arrays;
import javax.annotation.Nullable;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.NonNullList;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.world.Container;
import net.minecraft.world.ContainerHelper;
import net.minecraft.world.Containers;
import net.minecraft.world.WorldlyContainer;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.BrewingStandMenu;
import net.minecraft.world.inventory.ContainerData;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.alchemy.PotionBrewing;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.BrewingStandBlock;
import net.minecraft.world.level.block.state.BlockState;

public class BrewingStandBlockEntity extends BaseContainerBlockEntity implements WorldlyContainer {
   private static final int INGREDIENT_SLOT = 3;
   private static final int FUEL_SLOT = 4;
   private static final int[] SLOTS_FOR_UP = new int[]{3};
   private static final int[] SLOTS_FOR_DOWN = new int[]{0, 1, 2, 3};
   private static final int[] SLOTS_FOR_SIDES = new int[]{0, 1, 2, 4};
   public static final int FUEL_USES = 20;
   public static final int DATA_BREW_TIME = 0;
   public static final int DATA_FUEL_USES = 1;
   public static final int NUM_DATA_VALUES = 2;
   private NonNullList<ItemStack> items = NonNullList.withSize(5, ItemStack.EMPTY);
   int brewTime;
   private boolean[] lastPotionCount;
   private Item ingredient;
   int fuel;
   protected final ContainerData dataAccess = new ContainerData() {
      public int get(int i) {
         switch (i) {
            case 0:
               return BrewingStandBlockEntity.this.brewTime;
            case 1:
               return BrewingStandBlockEntity.this.fuel;
            default:
               return 0;
         }
      }

      public void set(int i, int j) {
         switch (i) {
            case 0:
               BrewingStandBlockEntity.this.brewTime = j;
               break;
            case 1:
               BrewingStandBlockEntity.this.fuel = j;
         }

      }

      public int getCount() {
         return 2;
      }
   };

   public BrewingStandBlockEntity(BlockPos blockpos, BlockState blockstate) {
      super(BlockEntityType.BREWING_STAND, blockpos, blockstate);
   }

   protected Component getDefaultName() {
      return Component.translatable("container.brewing");
   }

   public int getContainerSize() {
      return this.items.size();
   }

   public boolean isEmpty() {
      for(ItemStack itemstack : this.items) {
         if (!itemstack.isEmpty()) {
            return false;
         }
      }

      return true;
   }

   public static void serverTick(Level level, BlockPos blockpos, BlockState blockstate, BrewingStandBlockEntity brewingstandblockentity) {
      ItemStack itemstack = brewingstandblockentity.items.get(4);
      if (brewingstandblockentity.fuel <= 0 && itemstack.is(Items.BLAZE_POWDER)) {
         brewingstandblockentity.fuel = 20;
         itemstack.shrink(1);
         setChanged(level, blockpos, blockstate);
      }

      boolean flag = isBrewable(brewingstandblockentity.items);
      boolean flag1 = brewingstandblockentity.brewTime > 0;
      ItemStack itemstack1 = brewingstandblockentity.items.get(3);
      if (flag1) {
         --brewingstandblockentity.brewTime;
         boolean flag2 = brewingstandblockentity.brewTime == 0;
         if (flag2 && flag) {
            doBrew(level, blockpos, brewingstandblockentity.items);
            setChanged(level, blockpos, blockstate);
         } else if (!flag || !itemstack1.is(brewingstandblockentity.ingredient)) {
            brewingstandblockentity.brewTime = 0;
            setChanged(level, blockpos, blockstate);
         }
      } else if (flag && brewingstandblockentity.fuel > 0) {
         --brewingstandblockentity.fuel;
         brewingstandblockentity.brewTime = 400;
         brewingstandblockentity.ingredient = itemstack1.getItem();
         setChanged(level, blockpos, blockstate);
      }

      boolean[] aboolean = brewingstandblockentity.getPotionBits();
      if (!Arrays.equals(aboolean, brewingstandblockentity.lastPotionCount)) {
         brewingstandblockentity.lastPotionCount = aboolean;
         BlockState blockstate1 = blockstate;
         if (!(blockstate.getBlock() instanceof BrewingStandBlock)) {
            return;
         }

         for(int i = 0; i < BrewingStandBlock.HAS_BOTTLE.length; ++i) {
            blockstate1 = blockstate1.setValue(BrewingStandBlock.HAS_BOTTLE[i], Boolean.valueOf(aboolean[i]));
         }

         level.setBlock(blockpos, blockstate1, 2);
      }

   }

   private boolean[] getPotionBits() {
      boolean[] aboolean = new boolean[3];

      for(int i = 0; i < 3; ++i) {
         if (!this.items.get(i).isEmpty()) {
            aboolean[i] = true;
         }
      }

      return aboolean;
   }

   private static boolean isBrewable(NonNullList<ItemStack> nonnulllist) {
      ItemStack itemstack = nonnulllist.get(3);
      if (itemstack.isEmpty()) {
         return false;
      } else if (!PotionBrewing.isIngredient(itemstack)) {
         return false;
      } else {
         for(int i = 0; i < 3; ++i) {
            ItemStack itemstack1 = nonnulllist.get(i);
            if (!itemstack1.isEmpty() && PotionBrewing.hasMix(itemstack1, itemstack)) {
               return true;
            }
         }

         return false;
      }
   }

   private static void doBrew(Level level, BlockPos blockpos, NonNullList<ItemStack> nonnulllist) {
      ItemStack itemstack = nonnulllist.get(3);

      for(int i = 0; i < 3; ++i) {
         nonnulllist.set(i, PotionBrewing.mix(itemstack, nonnulllist.get(i)));
      }

      itemstack.shrink(1);
      if (itemstack.getItem().hasCraftingRemainingItem()) {
         ItemStack itemstack1 = new ItemStack(itemstack.getItem().getCraftingRemainingItem());
         if (itemstack.isEmpty()) {
            itemstack = itemstack1;
         } else {
            Containers.dropItemStack(level, (double)blockpos.getX(), (double)blockpos.getY(), (double)blockpos.getZ(), itemstack1);
         }
      }

      nonnulllist.set(3, itemstack);
      level.levelEvent(1035, blockpos, 0);
   }

   public void load(CompoundTag compoundtag) {
      super.load(compoundtag);
      this.items = NonNullList.withSize(this.getContainerSize(), ItemStack.EMPTY);
      ContainerHelper.loadAllItems(compoundtag, this.items);
      this.brewTime = compoundtag.getShort("BrewTime");
      this.fuel = compoundtag.getByte("Fuel");
   }

   protected void saveAdditional(CompoundTag compoundtag) {
      super.saveAdditional(compoundtag);
      compoundtag.putShort("BrewTime", (short)this.brewTime);
      ContainerHelper.saveAllItems(compoundtag, this.items);
      compoundtag.putByte("Fuel", (byte)this.fuel);
   }

   public ItemStack getItem(int i) {
      return i >= 0 && i < this.items.size() ? this.items.get(i) : ItemStack.EMPTY;
   }

   public ItemStack removeItem(int i, int j) {
      return ContainerHelper.removeItem(this.items, i, j);
   }

   public ItemStack removeItemNoUpdate(int i) {
      return ContainerHelper.takeItem(this.items, i);
   }

   public void setItem(int i, ItemStack itemstack) {
      if (i >= 0 && i < this.items.size()) {
         this.items.set(i, itemstack);
      }

   }

   public boolean stillValid(Player player) {
      return Container.stillValidBlockEntity(this, player);
   }

   public boolean canPlaceItem(int i, ItemStack itemstack) {
      if (i == 3) {
         return PotionBrewing.isIngredient(itemstack);
      } else if (i == 4) {
         return itemstack.is(Items.BLAZE_POWDER);
      } else {
         return (itemstack.is(Items.POTION) || itemstack.is(Items.SPLASH_POTION) || itemstack.is(Items.LINGERING_POTION) || itemstack.is(Items.GLASS_BOTTLE)) && this.getItem(i).isEmpty();
      }
   }

   public int[] getSlotsForFace(Direction direction) {
      if (direction == Direction.UP) {
         return SLOTS_FOR_UP;
      } else {
         return direction == Direction.DOWN ? SLOTS_FOR_DOWN : SLOTS_FOR_SIDES;
      }
   }

   public boolean canPlaceItemThroughFace(int i, ItemStack itemstack, @Nullable Direction direction) {
      return this.canPlaceItem(i, itemstack);
   }

   public boolean canTakeItemThroughFace(int i, ItemStack itemstack, Direction direction) {
      return i == 3 ? itemstack.is(Items.GLASS_BOTTLE) : true;
   }

   public void clearContent() {
      this.items.clear();
   }

   protected AbstractContainerMenu createMenu(int i, Inventory inventory) {
      return new BrewingStandMenu(i, inventory, this, this.dataAccess);
   }
}
