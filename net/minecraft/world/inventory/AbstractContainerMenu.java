package net.minecraft.world.inventory;

import com.google.common.base.Suppliers;
import com.google.common.collect.HashBasedTable;
import com.google.common.collect.Lists;
import com.google.common.collect.Sets;
import com.google.common.collect.Table;
import com.mojang.logging.LogUtils;
import it.unimi.dsi.fastutil.ints.IntArrayList;
import it.unimi.dsi.fastutil.ints.IntList;
import java.util.List;
import java.util.Optional;
import java.util.OptionalInt;
import java.util.Set;
import java.util.function.Supplier;
import javax.annotation.Nullable;
import net.minecraft.CrashReport;
import net.minecraft.CrashReportCategory;
import net.minecraft.ReportedException;
import net.minecraft.core.NonNullList;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.util.Mth;
import net.minecraft.world.Container;
import net.minecraft.world.entity.SlotAccess;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.flag.FeatureFlagSet;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntity;
import org.slf4j.Logger;

public abstract class AbstractContainerMenu {
   private static final Logger LOGGER = LogUtils.getLogger();
   public static final int SLOT_CLICKED_OUTSIDE = -999;
   public static final int QUICKCRAFT_TYPE_CHARITABLE = 0;
   public static final int QUICKCRAFT_TYPE_GREEDY = 1;
   public static final int QUICKCRAFT_TYPE_CLONE = 2;
   public static final int QUICKCRAFT_HEADER_START = 0;
   public static final int QUICKCRAFT_HEADER_CONTINUE = 1;
   public static final int QUICKCRAFT_HEADER_END = 2;
   public static final int CARRIED_SLOT_SIZE = Integer.MAX_VALUE;
   private final NonNullList<ItemStack> lastSlots = NonNullList.create();
   public final NonNullList<Slot> slots = NonNullList.create();
   private final List<DataSlot> dataSlots = Lists.newArrayList();
   private ItemStack carried = ItemStack.EMPTY;
   private final NonNullList<ItemStack> remoteSlots = NonNullList.create();
   private final IntList remoteDataSlots = new IntArrayList();
   private ItemStack remoteCarried = ItemStack.EMPTY;
   private int stateId;
   @Nullable
   private final MenuType<?> menuType;
   public final int containerId;
   private int quickcraftType = -1;
   private int quickcraftStatus;
   private final Set<Slot> quickcraftSlots = Sets.newHashSet();
   private final List<ContainerListener> containerListeners = Lists.newArrayList();
   @Nullable
   private ContainerSynchronizer synchronizer;
   private boolean suppressRemoteUpdates;

   protected AbstractContainerMenu(@Nullable MenuType<?> menutype, int i) {
      this.menuType = menutype;
      this.containerId = i;
   }

   protected static boolean stillValid(ContainerLevelAccess containerlevelaccess, Player player, Block block) {
      return containerlevelaccess.evaluate((level, blockpos) -> !level.getBlockState(blockpos).is(block) ? false : player.distanceToSqr((double)blockpos.getX() + 0.5D, (double)blockpos.getY() + 0.5D, (double)blockpos.getZ() + 0.5D) <= 64.0D, true);
   }

   public MenuType<?> getType() {
      if (this.menuType == null) {
         throw new UnsupportedOperationException("Unable to construct this menu by type");
      } else {
         return this.menuType;
      }
   }

   protected static void checkContainerSize(Container container, int i) {
      int j = container.getContainerSize();
      if (j < i) {
         throw new IllegalArgumentException("Container size " + j + " is smaller than expected " + i);
      }
   }

   protected static void checkContainerDataCount(ContainerData containerdata, int i) {
      int j = containerdata.getCount();
      if (j < i) {
         throw new IllegalArgumentException("Container data count " + j + " is smaller than expected " + i);
      }
   }

   public boolean isValidSlotIndex(int i) {
      return i == -1 || i == -999 || i < this.slots.size();
   }

   protected Slot addSlot(Slot slot) {
      slot.index = this.slots.size();
      this.slots.add(slot);
      this.lastSlots.add(ItemStack.EMPTY);
      this.remoteSlots.add(ItemStack.EMPTY);
      return slot;
   }

   protected DataSlot addDataSlot(DataSlot dataslot) {
      this.dataSlots.add(dataslot);
      this.remoteDataSlots.add(0);
      return dataslot;
   }

   protected void addDataSlots(ContainerData containerdata) {
      for(int i = 0; i < containerdata.getCount(); ++i) {
         this.addDataSlot(DataSlot.forContainer(containerdata, i));
      }

   }

   public void addSlotListener(ContainerListener containerlistener) {
      if (!this.containerListeners.contains(containerlistener)) {
         this.containerListeners.add(containerlistener);
         this.broadcastChanges();
      }
   }

   public void setSynchronizer(ContainerSynchronizer containersynchronizer) {
      this.synchronizer = containersynchronizer;
      this.sendAllDataToRemote();
   }

   public void sendAllDataToRemote() {
      int i = 0;

      for(int j = this.slots.size(); i < j; ++i) {
         this.remoteSlots.set(i, this.slots.get(i).getItem().copy());
      }

      this.remoteCarried = this.getCarried().copy();
      i = 0;

      for(int l = this.dataSlots.size(); i < l; ++i) {
         this.remoteDataSlots.set(i, this.dataSlots.get(i).get());
      }

      if (this.synchronizer != null) {
         this.synchronizer.sendInitialData(this, this.remoteSlots, this.remoteCarried, this.remoteDataSlots.toIntArray());
      }

   }

   public void removeSlotListener(ContainerListener containerlistener) {
      this.containerListeners.remove(containerlistener);
   }

   public NonNullList<ItemStack> getItems() {
      NonNullList<ItemStack> nonnulllist = NonNullList.create();

      for(Slot slot : this.slots) {
         nonnulllist.add(slot.getItem());
      }

      return nonnulllist;
   }

   public void broadcastChanges() {
      for(int i = 0; i < this.slots.size(); ++i) {
         ItemStack itemstack = this.slots.get(i).getItem();
         Supplier<ItemStack> supplier = Suppliers.memoize(itemstack::copy);
         this.triggerSlotListeners(i, itemstack, supplier);
         this.synchronizeSlotToRemote(i, itemstack, supplier);
      }

      this.synchronizeCarriedToRemote();

      for(int j = 0; j < this.dataSlots.size(); ++j) {
         DataSlot dataslot = this.dataSlots.get(j);
         int k = dataslot.get();
         if (dataslot.checkAndClearUpdateFlag()) {
            this.updateDataSlotListeners(j, k);
         }

         this.synchronizeDataSlotToRemote(j, k);
      }

   }

   public void broadcastFullState() {
      for(int i = 0; i < this.slots.size(); ++i) {
         ItemStack itemstack = this.slots.get(i).getItem();
         this.triggerSlotListeners(i, itemstack, itemstack::copy);
      }

      for(int j = 0; j < this.dataSlots.size(); ++j) {
         DataSlot dataslot = this.dataSlots.get(j);
         if (dataslot.checkAndClearUpdateFlag()) {
            this.updateDataSlotListeners(j, dataslot.get());
         }
      }

      this.sendAllDataToRemote();
   }

   private void updateDataSlotListeners(int i, int j) {
      for(ContainerListener containerlistener : this.containerListeners) {
         containerlistener.dataChanged(this, i, j);
      }

   }

   private void triggerSlotListeners(int i, ItemStack itemstack, Supplier<ItemStack> supplier) {
      ItemStack itemstack1 = this.lastSlots.get(i);
      if (!ItemStack.matches(itemstack1, itemstack)) {
         ItemStack itemstack2 = supplier.get();
         this.lastSlots.set(i, itemstack2);

         for(ContainerListener containerlistener : this.containerListeners) {
            containerlistener.slotChanged(this, i, itemstack2);
         }
      }

   }

   private void synchronizeSlotToRemote(int i, ItemStack itemstack, Supplier<ItemStack> supplier) {
      if (!this.suppressRemoteUpdates) {
         ItemStack itemstack1 = this.remoteSlots.get(i);
         if (!ItemStack.matches(itemstack1, itemstack)) {
            ItemStack itemstack2 = supplier.get();
            this.remoteSlots.set(i, itemstack2);
            if (this.synchronizer != null) {
               this.synchronizer.sendSlotChange(this, i, itemstack2);
            }
         }

      }
   }

   private void synchronizeDataSlotToRemote(int i, int j) {
      if (!this.suppressRemoteUpdates) {
         int k = this.remoteDataSlots.getInt(i);
         if (k != j) {
            this.remoteDataSlots.set(i, j);
            if (this.synchronizer != null) {
               this.synchronizer.sendDataChange(this, i, j);
            }
         }

      }
   }

   private void synchronizeCarriedToRemote() {
      if (!this.suppressRemoteUpdates) {
         if (!ItemStack.matches(this.getCarried(), this.remoteCarried)) {
            this.remoteCarried = this.getCarried().copy();
            if (this.synchronizer != null) {
               this.synchronizer.sendCarriedChange(this, this.remoteCarried);
            }
         }

      }
   }

   public void setRemoteSlot(int i, ItemStack itemstack) {
      this.remoteSlots.set(i, itemstack.copy());
   }

   public void setRemoteSlotNoCopy(int i, ItemStack itemstack) {
      if (i >= 0 && i < this.remoteSlots.size()) {
         this.remoteSlots.set(i, itemstack);
      } else {
         LOGGER.debug("Incorrect slot index: {} available slots: {}", i, this.remoteSlots.size());
      }
   }

   public void setRemoteCarried(ItemStack itemstack) {
      this.remoteCarried = itemstack.copy();
   }

   public boolean clickMenuButton(Player player, int i) {
      return false;
   }

   public Slot getSlot(int i) {
      return this.slots.get(i);
   }

   public abstract ItemStack quickMoveStack(Player player, int i);

   public void clicked(int i, int j, ClickType clicktype, Player player) {
      try {
         this.doClick(i, j, clicktype, player);
      } catch (Exception var8) {
         CrashReport crashreport = CrashReport.forThrowable(var8, "Container click");
         CrashReportCategory crashreportcategory = crashreport.addCategory("Click info");
         crashreportcategory.setDetail("Menu Type", () -> this.menuType != null ? BuiltInRegistries.MENU.getKey(this.menuType).toString() : "<no type>");
         crashreportcategory.setDetail("Menu Class", () -> this.getClass().getCanonicalName());
         crashreportcategory.setDetail("Slot Count", this.slots.size());
         crashreportcategory.setDetail("Slot", i);
         crashreportcategory.setDetail("Button", j);
         crashreportcategory.setDetail("Type", clicktype);
         throw new ReportedException(crashreport);
      }
   }

   private void doClick(int i, int j, ClickType clicktype, Player player) {
      Inventory inventory = player.getInventory();
      if (clicktype == ClickType.QUICK_CRAFT) {
         int k = this.quickcraftStatus;
         this.quickcraftStatus = getQuickcraftHeader(j);
         if ((k != 1 || this.quickcraftStatus != 2) && k != this.quickcraftStatus) {
            this.resetQuickCraft();
         } else if (this.getCarried().isEmpty()) {
            this.resetQuickCraft();
         } else if (this.quickcraftStatus == 0) {
            this.quickcraftType = getQuickcraftType(j);
            if (isValidQuickcraftType(this.quickcraftType, player)) {
               this.quickcraftStatus = 1;
               this.quickcraftSlots.clear();
            } else {
               this.resetQuickCraft();
            }
         } else if (this.quickcraftStatus == 1) {
            Slot slot = this.slots.get(i);
            ItemStack itemstack = this.getCarried();
            if (canItemQuickReplace(slot, itemstack, true) && slot.mayPlace(itemstack) && (this.quickcraftType == 2 || itemstack.getCount() > this.quickcraftSlots.size()) && this.canDragTo(slot)) {
               this.quickcraftSlots.add(slot);
            }
         } else if (this.quickcraftStatus == 2) {
            if (!this.quickcraftSlots.isEmpty()) {
               if (this.quickcraftSlots.size() == 1) {
                  int l = (this.quickcraftSlots.iterator().next()).index;
                  this.resetQuickCraft();
                  this.doClick(l, this.quickcraftType, ClickType.PICKUP, player);
                  return;
               }

               ItemStack itemstack1 = this.getCarried().copy();
               if (itemstack1.isEmpty()) {
                  this.resetQuickCraft();
                  return;
               }

               int i1 = this.getCarried().getCount();

               for(Slot slot1 : this.quickcraftSlots) {
                  ItemStack itemstack2 = this.getCarried();
                  if (slot1 != null && canItemQuickReplace(slot1, itemstack2, true) && slot1.mayPlace(itemstack2) && (this.quickcraftType == 2 || itemstack2.getCount() >= this.quickcraftSlots.size()) && this.canDragTo(slot1)) {
                     int j1 = slot1.hasItem() ? slot1.getItem().getCount() : 0;
                     int k1 = Math.min(itemstack1.getMaxStackSize(), slot1.getMaxStackSize(itemstack1));
                     int l1 = Math.min(getQuickCraftPlaceCount(this.quickcraftSlots, this.quickcraftType, itemstack1) + j1, k1);
                     i1 -= l1 - j1;
                     slot1.setByPlayer(itemstack1.copyWithCount(l1));
                  }
               }

               itemstack1.setCount(i1);
               this.setCarried(itemstack1);
            }

            this.resetQuickCraft();
         } else {
            this.resetQuickCraft();
         }
      } else if (this.quickcraftStatus != 0) {
         this.resetQuickCraft();
      } else if ((clicktype == ClickType.PICKUP || clicktype == ClickType.QUICK_MOVE) && (j == 0 || j == 1)) {
         ClickAction clickaction = j == 0 ? ClickAction.PRIMARY : ClickAction.SECONDARY;
         if (i == -999) {
            if (!this.getCarried().isEmpty()) {
               if (clickaction == ClickAction.PRIMARY) {
                  player.drop(this.getCarried(), true);
                  this.setCarried(ItemStack.EMPTY);
               } else {
                  player.drop(this.getCarried().split(1), true);
               }
            }
         } else if (clicktype == ClickType.QUICK_MOVE) {
            if (i < 0) {
               return;
            }

            Slot slot2 = this.slots.get(i);
            if (!slot2.mayPickup(player)) {
               return;
            }

            for(ItemStack itemstack3 = this.quickMoveStack(player, i); !itemstack3.isEmpty() && ItemStack.isSameItem(slot2.getItem(), itemstack3); itemstack3 = this.quickMoveStack(player, i)) {
            }
         } else {
            if (i < 0) {
               return;
            }

            Slot slot3 = this.slots.get(i);
            ItemStack itemstack4 = slot3.getItem();
            ItemStack itemstack5 = this.getCarried();
            player.updateTutorialInventoryAction(itemstack5, slot3.getItem(), clickaction);
            if (!this.tryItemClickBehaviourOverride(player, clickaction, slot3, itemstack4, itemstack5)) {
               if (itemstack4.isEmpty()) {
                  if (!itemstack5.isEmpty()) {
                     int i2 = clickaction == ClickAction.PRIMARY ? itemstack5.getCount() : 1;
                     this.setCarried(slot3.safeInsert(itemstack5, i2));
                  }
               } else if (slot3.mayPickup(player)) {
                  if (itemstack5.isEmpty()) {
                     int j2 = clickaction == ClickAction.PRIMARY ? itemstack4.getCount() : (itemstack4.getCount() + 1) / 2;
                     Optional<ItemStack> optional = slot3.tryRemove(j2, Integer.MAX_VALUE, player);
                     optional.ifPresent((itemstack15) -> {
                        this.setCarried(itemstack15);
                        slot3.onTake(player, itemstack15);
                     });
                  } else if (slot3.mayPlace(itemstack5)) {
                     if (ItemStack.isSameItemSameTags(itemstack4, itemstack5)) {
                        int k2 = clickaction == ClickAction.PRIMARY ? itemstack5.getCount() : 1;
                        this.setCarried(slot3.safeInsert(itemstack5, k2));
                     } else if (itemstack5.getCount() <= slot3.getMaxStackSize(itemstack5)) {
                        this.setCarried(itemstack4);
                        slot3.setByPlayer(itemstack5);
                     }
                  } else if (ItemStack.isSameItemSameTags(itemstack4, itemstack5)) {
                     Optional<ItemStack> optional1 = slot3.tryRemove(itemstack4.getCount(), itemstack5.getMaxStackSize() - itemstack5.getCount(), player);
                     optional1.ifPresent((itemstack14) -> {
                        itemstack5.grow(itemstack14.getCount());
                        slot3.onTake(player, itemstack14);
                     });
                  }
               }
            }

            slot3.setChanged();
         }
      } else if (clicktype == ClickType.SWAP) {
         Slot slot4 = this.slots.get(i);
         ItemStack itemstack6 = inventory.getItem(j);
         ItemStack itemstack7 = slot4.getItem();
         if (!itemstack6.isEmpty() || !itemstack7.isEmpty()) {
            if (itemstack6.isEmpty()) {
               if (slot4.mayPickup(player)) {
                  inventory.setItem(j, itemstack7);
                  slot4.onSwapCraft(itemstack7.getCount());
                  slot4.setByPlayer(ItemStack.EMPTY);
                  slot4.onTake(player, itemstack7);
               }
            } else if (itemstack7.isEmpty()) {
               if (slot4.mayPlace(itemstack6)) {
                  int l2 = slot4.getMaxStackSize(itemstack6);
                  if (itemstack6.getCount() > l2) {
                     slot4.setByPlayer(itemstack6.split(l2));
                  } else {
                     inventory.setItem(j, ItemStack.EMPTY);
                     slot4.setByPlayer(itemstack6);
                  }
               }
            } else if (slot4.mayPickup(player) && slot4.mayPlace(itemstack6)) {
               int i3 = slot4.getMaxStackSize(itemstack6);
               if (itemstack6.getCount() > i3) {
                  slot4.setByPlayer(itemstack6.split(i3));
                  slot4.onTake(player, itemstack7);
                  if (!inventory.add(itemstack7)) {
                     player.drop(itemstack7, true);
                  }
               } else {
                  inventory.setItem(j, itemstack7);
                  slot4.setByPlayer(itemstack6);
                  slot4.onTake(player, itemstack7);
               }
            }
         }
      } else if (clicktype == ClickType.CLONE && player.getAbilities().instabuild && this.getCarried().isEmpty() && i >= 0) {
         Slot slot5 = this.slots.get(i);
         if (slot5.hasItem()) {
            ItemStack itemstack8 = slot5.getItem();
            this.setCarried(itemstack8.copyWithCount(itemstack8.getMaxStackSize()));
         }
      } else if (clicktype == ClickType.THROW && this.getCarried().isEmpty() && i >= 0) {
         Slot slot6 = this.slots.get(i);
         int j3 = j == 0 ? 1 : slot6.getItem().getCount();
         ItemStack itemstack9 = slot6.safeTake(j3, Integer.MAX_VALUE, player);
         player.drop(itemstack9, true);
      } else if (clicktype == ClickType.PICKUP_ALL && i >= 0) {
         Slot slot7 = this.slots.get(i);
         ItemStack itemstack10 = this.getCarried();
         if (!itemstack10.isEmpty() && (!slot7.hasItem() || !slot7.mayPickup(player))) {
            int k3 = j == 0 ? 0 : this.slots.size() - 1;
            int l3 = j == 0 ? 1 : -1;

            for(int i4 = 0; i4 < 2; ++i4) {
               for(int j4 = k3; j4 >= 0 && j4 < this.slots.size() && itemstack10.getCount() < itemstack10.getMaxStackSize(); j4 += l3) {
                  Slot slot8 = this.slots.get(j4);
                  if (slot8.hasItem() && canItemQuickReplace(slot8, itemstack10, true) && slot8.mayPickup(player) && this.canTakeItemForPickAll(itemstack10, slot8)) {
                     ItemStack itemstack11 = slot8.getItem();
                     if (i4 != 0 || itemstack11.getCount() != itemstack11.getMaxStackSize()) {
                        ItemStack itemstack12 = slot8.safeTake(itemstack11.getCount(), itemstack10.getMaxStackSize() - itemstack10.getCount(), player);
                        itemstack10.grow(itemstack12.getCount());
                     }
                  }
               }
            }
         }
      }

   }

   private boolean tryItemClickBehaviourOverride(Player player, ClickAction clickaction, Slot slot, ItemStack itemstack, ItemStack itemstack1) {
      FeatureFlagSet featureflagset = player.level().enabledFeatures();
      if (itemstack1.isItemEnabled(featureflagset) && itemstack1.overrideStackedOnOther(slot, clickaction, player)) {
         return true;
      } else {
         return itemstack.isItemEnabled(featureflagset) && itemstack.overrideOtherStackedOnMe(itemstack1, slot, clickaction, player, this.createCarriedSlotAccess());
      }
   }

   private SlotAccess createCarriedSlotAccess() {
      return new SlotAccess() {
         public ItemStack get() {
            return AbstractContainerMenu.this.getCarried();
         }

         public boolean set(ItemStack itemstack) {
            AbstractContainerMenu.this.setCarried(itemstack);
            return true;
         }
      };
   }

   public boolean canTakeItemForPickAll(ItemStack itemstack, Slot slot) {
      return true;
   }

   public void removed(Player player) {
      if (player instanceof ServerPlayer) {
         ItemStack itemstack = this.getCarried();
         if (!itemstack.isEmpty()) {
            if (player.isAlive() && !((ServerPlayer)player).hasDisconnected()) {
               player.getInventory().placeItemBackInInventory(itemstack);
            } else {
               player.drop(itemstack, false);
            }

            this.setCarried(ItemStack.EMPTY);
         }
      }

   }

   protected void clearContainer(Player player, Container container) {
      if (!player.isAlive() || player instanceof ServerPlayer && ((ServerPlayer)player).hasDisconnected()) {
         for(int i = 0; i < container.getContainerSize(); ++i) {
            player.drop(container.removeItemNoUpdate(i), false);
         }

      } else {
         for(int j = 0; j < container.getContainerSize(); ++j) {
            Inventory inventory = player.getInventory();
            if (inventory.player instanceof ServerPlayer) {
               inventory.placeItemBackInInventory(container.removeItemNoUpdate(j));
            }
         }

      }
   }

   public void slotsChanged(Container container) {
      this.broadcastChanges();
   }

   public void setItem(int i, int j, ItemStack itemstack) {
      this.getSlot(i).set(itemstack);
      this.stateId = j;
   }

   public void initializeContents(int i, List<ItemStack> list, ItemStack itemstack) {
      for(int j = 0; j < list.size(); ++j) {
         this.getSlot(j).set(list.get(j));
      }

      this.carried = itemstack;
      this.stateId = i;
   }

   public void setData(int i, int j) {
      this.dataSlots.get(i).set(j);
   }

   public abstract boolean stillValid(Player player);

   protected boolean moveItemStackTo(ItemStack itemstack, int i, int j, boolean flag) {
      boolean flag1 = false;
      int k = i;
      if (flag) {
         k = j - 1;
      }

      if (itemstack.isStackable()) {
         while(!itemstack.isEmpty()) {
            if (flag) {
               if (k < i) {
                  break;
               }
            } else if (k >= j) {
               break;
            }

            Slot slot = this.slots.get(k);
            ItemStack itemstack1 = slot.getItem();
            if (!itemstack1.isEmpty() && ItemStack.isSameItemSameTags(itemstack, itemstack1)) {
               int l = itemstack1.getCount() + itemstack.getCount();
               if (l <= itemstack.getMaxStackSize()) {
                  itemstack.setCount(0);
                  itemstack1.setCount(l);
                  slot.setChanged();
                  flag1 = true;
               } else if (itemstack1.getCount() < itemstack.getMaxStackSize()) {
                  itemstack.shrink(itemstack.getMaxStackSize() - itemstack1.getCount());
                  itemstack1.setCount(itemstack.getMaxStackSize());
                  slot.setChanged();
                  flag1 = true;
               }
            }

            if (flag) {
               --k;
            } else {
               ++k;
            }
         }
      }

      if (!itemstack.isEmpty()) {
         if (flag) {
            k = j - 1;
         } else {
            k = i;
         }

         while(true) {
            if (flag) {
               if (k < i) {
                  break;
               }
            } else if (k >= j) {
               break;
            }

            Slot slot1 = this.slots.get(k);
            ItemStack itemstack2 = slot1.getItem();
            if (itemstack2.isEmpty() && slot1.mayPlace(itemstack)) {
               if (itemstack.getCount() > slot1.getMaxStackSize()) {
                  slot1.setByPlayer(itemstack.split(slot1.getMaxStackSize()));
               } else {
                  slot1.setByPlayer(itemstack.split(itemstack.getCount()));
               }

               slot1.setChanged();
               flag1 = true;
               break;
            }

            if (flag) {
               --k;
            } else {
               ++k;
            }
         }
      }

      return flag1;
   }

   public static int getQuickcraftType(int i) {
      return i >> 2 & 3;
   }

   public static int getQuickcraftHeader(int i) {
      return i & 3;
   }

   public static int getQuickcraftMask(int i, int j) {
      return i & 3 | (j & 3) << 2;
   }

   public static boolean isValidQuickcraftType(int i, Player player) {
      if (i == 0) {
         return true;
      } else if (i == 1) {
         return true;
      } else {
         return i == 2 && player.getAbilities().instabuild;
      }
   }

   protected void resetQuickCraft() {
      this.quickcraftStatus = 0;
      this.quickcraftSlots.clear();
   }

   public static boolean canItemQuickReplace(@Nullable Slot slot, ItemStack itemstack, boolean flag) {
      boolean flag1 = slot == null || !slot.hasItem();
      if (!flag1 && ItemStack.isSameItemSameTags(itemstack, slot.getItem())) {
         return slot.getItem().getCount() + (flag ? 0 : itemstack.getCount()) <= itemstack.getMaxStackSize();
      } else {
         return flag1;
      }
   }

   public static int getQuickCraftPlaceCount(Set<Slot> set, int i, ItemStack itemstack) {
      int var10000;
      switch (i) {
         case 0:
            var10000 = Mth.floor((float)itemstack.getCount() / (float)set.size());
            break;
         case 1:
            var10000 = 1;
            break;
         case 2:
            var10000 = itemstack.getItem().getMaxStackSize();
            break;
         default:
            var10000 = itemstack.getCount();
      }

      return var10000;
   }

   public boolean canDragTo(Slot slot) {
      return true;
   }

   public static int getRedstoneSignalFromBlockEntity(@Nullable BlockEntity blockentity) {
      return blockentity instanceof Container ? getRedstoneSignalFromContainer((Container)blockentity) : 0;
   }

   public static int getRedstoneSignalFromContainer(@Nullable Container container) {
      if (container == null) {
         return 0;
      } else {
         int i = 0;
         float f = 0.0F;

         for(int j = 0; j < container.getContainerSize(); ++j) {
            ItemStack itemstack = container.getItem(j);
            if (!itemstack.isEmpty()) {
               f += (float)itemstack.getCount() / (float)Math.min(container.getMaxStackSize(), itemstack.getMaxStackSize());
               ++i;
            }
         }

         f /= (float)container.getContainerSize();
         return Mth.floor(f * 14.0F) + (i > 0 ? 1 : 0);
      }
   }

   public void setCarried(ItemStack itemstack) {
      this.carried = itemstack;
   }

   public ItemStack getCarried() {
      return this.carried;
   }

   public void suppressRemoteUpdates() {
      this.suppressRemoteUpdates = true;
   }

   public void resumeRemoteUpdates() {
      this.suppressRemoteUpdates = false;
   }

   public void transferState(AbstractContainerMenu abstractcontainermenu) {
      Table<Container, Integer, Integer> table = HashBasedTable.create();

      for(int i = 0; i < abstractcontainermenu.slots.size(); ++i) {
         Slot slot = abstractcontainermenu.slots.get(i);
         table.put(slot.container, slot.getContainerSlot(), i);
      }

      for(int j = 0; j < this.slots.size(); ++j) {
         Slot slot1 = this.slots.get(j);
         Integer integer = table.get(slot1.container, slot1.getContainerSlot());
         if (integer != null) {
            this.lastSlots.set(j, abstractcontainermenu.lastSlots.get(integer));
            this.remoteSlots.set(j, abstractcontainermenu.remoteSlots.get(integer));
         }
      }

   }

   public OptionalInt findSlot(Container container, int i) {
      for(int j = 0; j < this.slots.size(); ++j) {
         Slot slot = this.slots.get(j);
         if (slot.container == container && i == slot.getContainerSlot()) {
            return OptionalInt.of(j);
         }
      }

      return OptionalInt.empty();
   }

   public int getStateId() {
      return this.stateId;
   }

   public int incrementStateId() {
      this.stateId = this.stateId + 1 & 32767;
      return this.stateId;
   }
}
