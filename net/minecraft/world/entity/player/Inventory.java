package net.minecraft.world.entity.player;

import com.google.common.collect.ImmutableList;
import java.util.List;
import java.util.function.Predicate;
import net.minecraft.CrashReport;
import net.minecraft.CrashReportCategory;
import net.minecraft.ReportedException;
import net.minecraft.core.NonNullList;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.network.chat.Component;
import net.minecraft.network.protocol.game.ClientboundContainerSetSlotPacket;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.tags.DamageTypeTags;
import net.minecraft.tags.TagKey;
import net.minecraft.world.Container;
import net.minecraft.world.ContainerHelper;
import net.minecraft.world.Nameable;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.item.ArmorItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.state.BlockState;

public class Inventory implements Container, Nameable {
   public static final int POP_TIME_DURATION = 5;
   public static final int INVENTORY_SIZE = 36;
   private static final int SELECTION_SIZE = 9;
   public static final int SLOT_OFFHAND = 40;
   public static final int NOT_FOUND_INDEX = -1;
   public static final int[] ALL_ARMOR_SLOTS = new int[]{0, 1, 2, 3};
   public static final int[] HELMET_SLOT_ONLY = new int[]{3};
   public final NonNullList<ItemStack> items = NonNullList.withSize(36, ItemStack.EMPTY);
   public final NonNullList<ItemStack> armor = NonNullList.withSize(4, ItemStack.EMPTY);
   public final NonNullList<ItemStack> offhand = NonNullList.withSize(1, ItemStack.EMPTY);
   private final List<NonNullList<ItemStack>> compartments = ImmutableList.of(this.items, this.armor, this.offhand);
   public int selected;
   public final Player player;
   private int timesChanged;

   public Inventory(Player player) {
      this.player = player;
   }

   public ItemStack getSelected() {
      return isHotbarSlot(this.selected) ? this.items.get(this.selected) : ItemStack.EMPTY;
   }

   public static int getSelectionSize() {
      return 9;
   }

   private boolean hasRemainingSpaceForItem(ItemStack itemstack, ItemStack itemstack1) {
      return !itemstack.isEmpty() && ItemStack.isSameItemSameTags(itemstack, itemstack1) && itemstack.isStackable() && itemstack.getCount() < itemstack.getMaxStackSize() && itemstack.getCount() < this.getMaxStackSize();
   }

   public int getFreeSlot() {
      for(int i = 0; i < this.items.size(); ++i) {
         if (this.items.get(i).isEmpty()) {
            return i;
         }
      }

      return -1;
   }

   public void setPickedItem(ItemStack itemstack) {
      int i = this.findSlotMatchingItem(itemstack);
      if (isHotbarSlot(i)) {
         this.selected = i;
      } else {
         if (i == -1) {
            this.selected = this.getSuitableHotbarSlot();
            if (!this.items.get(this.selected).isEmpty()) {
               int j = this.getFreeSlot();
               if (j != -1) {
                  this.items.set(j, this.items.get(this.selected));
               }
            }

            this.items.set(this.selected, itemstack);
         } else {
            this.pickSlot(i);
         }

      }
   }

   public void pickSlot(int i) {
      this.selected = this.getSuitableHotbarSlot();
      ItemStack itemstack = this.items.get(this.selected);
      this.items.set(this.selected, this.items.get(i));
      this.items.set(i, itemstack);
   }

   public static boolean isHotbarSlot(int i) {
      return i >= 0 && i < 9;
   }

   public int findSlotMatchingItem(ItemStack itemstack) {
      for(int i = 0; i < this.items.size(); ++i) {
         if (!this.items.get(i).isEmpty() && ItemStack.isSameItemSameTags(itemstack, this.items.get(i))) {
            return i;
         }
      }

      return -1;
   }

   public int findSlotMatchingUnusedItem(ItemStack itemstack) {
      for(int i = 0; i < this.items.size(); ++i) {
         ItemStack itemstack1 = this.items.get(i);
         if (!this.items.get(i).isEmpty() && ItemStack.isSameItemSameTags(itemstack, this.items.get(i)) && !this.items.get(i).isDamaged() && !itemstack1.isEnchanted() && !itemstack1.hasCustomHoverName()) {
            return i;
         }
      }

      return -1;
   }

   public int getSuitableHotbarSlot() {
      for(int i = 0; i < 9; ++i) {
         int j = (this.selected + i) % 9;
         if (this.items.get(j).isEmpty()) {
            return j;
         }
      }

      for(int k = 0; k < 9; ++k) {
         int l = (this.selected + k) % 9;
         if (!this.items.get(l).isEnchanted()) {
            return l;
         }
      }

      return this.selected;
   }

   public void swapPaint(double d0) {
      int i = (int)Math.signum(d0);

      for(this.selected -= i; this.selected < 0; this.selected += 9) {
      }

      while(this.selected >= 9) {
         this.selected -= 9;
      }

   }

   public int clearOrCountMatchingItems(Predicate<ItemStack> predicate, int i, Container container) {
      int j = 0;
      boolean flag = i == 0;
      j += ContainerHelper.clearOrCountMatchingItems(this, predicate, i - j, flag);
      j += ContainerHelper.clearOrCountMatchingItems(container, predicate, i - j, flag);
      ItemStack itemstack = this.player.containerMenu.getCarried();
      j += ContainerHelper.clearOrCountMatchingItems(itemstack, predicate, i - j, flag);
      if (itemstack.isEmpty()) {
         this.player.containerMenu.setCarried(ItemStack.EMPTY);
      }

      return j;
   }

   private int addResource(ItemStack itemstack) {
      int i = this.getSlotWithRemainingSpace(itemstack);
      if (i == -1) {
         i = this.getFreeSlot();
      }

      return i == -1 ? itemstack.getCount() : this.addResource(i, itemstack);
   }

   private int addResource(int i, ItemStack itemstack) {
      Item item = itemstack.getItem();
      int j = itemstack.getCount();
      ItemStack itemstack1 = this.getItem(i);
      if (itemstack1.isEmpty()) {
         itemstack1 = new ItemStack(item, 0);
         if (itemstack.hasTag()) {
            itemstack1.setTag(itemstack.getTag().copy());
         }

         this.setItem(i, itemstack1);
      }

      int k = j;
      if (j > itemstack1.getMaxStackSize() - itemstack1.getCount()) {
         k = itemstack1.getMaxStackSize() - itemstack1.getCount();
      }

      if (k > this.getMaxStackSize() - itemstack1.getCount()) {
         k = this.getMaxStackSize() - itemstack1.getCount();
      }

      if (k == 0) {
         return j;
      } else {
         j -= k;
         itemstack1.grow(k);
         itemstack1.setPopTime(5);
         return j;
      }
   }

   public int getSlotWithRemainingSpace(ItemStack itemstack) {
      if (this.hasRemainingSpaceForItem(this.getItem(this.selected), itemstack)) {
         return this.selected;
      } else if (this.hasRemainingSpaceForItem(this.getItem(40), itemstack)) {
         return 40;
      } else {
         for(int i = 0; i < this.items.size(); ++i) {
            if (this.hasRemainingSpaceForItem(this.items.get(i), itemstack)) {
               return i;
            }
         }

         return -1;
      }
   }

   public void tick() {
      for(NonNullList<ItemStack> nonnulllist : this.compartments) {
         for(int i = 0; i < nonnulllist.size(); ++i) {
            if (!nonnulllist.get(i).isEmpty()) {
               nonnulllist.get(i).inventoryTick(this.player.level(), this.player, i, this.selected == i);
            }
         }
      }

   }

   public boolean add(ItemStack itemstack) {
      return this.add(-1, itemstack);
   }

   public boolean add(int i, ItemStack itemstack) {
      if (itemstack.isEmpty()) {
         return false;
      } else {
         try {
            if (itemstack.isDamaged()) {
               if (i == -1) {
                  i = this.getFreeSlot();
               }

               if (i >= 0) {
                  this.items.set(i, itemstack.copyAndClear());
                  this.items.get(i).setPopTime(5);
                  return true;
               } else if (this.player.getAbilities().instabuild) {
                  itemstack.setCount(0);
                  return true;
               } else {
                  return false;
               }
            } else {
               int j;
               do {
                  j = itemstack.getCount();
                  if (i == -1) {
                     itemstack.setCount(this.addResource(itemstack));
                  } else {
                     itemstack.setCount(this.addResource(i, itemstack));
                  }
               } while(!itemstack.isEmpty() && itemstack.getCount() < j);

               if (itemstack.getCount() == j && this.player.getAbilities().instabuild) {
                  itemstack.setCount(0);
                  return true;
               } else {
                  return itemstack.getCount() < j;
               }
            }
         } catch (Throwable var6) {
            CrashReport crashreport = CrashReport.forThrowable(var6, "Adding item to inventory");
            CrashReportCategory crashreportcategory = crashreport.addCategory("Item being added");
            crashreportcategory.setDetail("Item ID", Item.getId(itemstack.getItem()));
            crashreportcategory.setDetail("Item data", itemstack.getDamageValue());
            crashreportcategory.setDetail("Item name", () -> itemstack.getHoverName().getString());
            throw new ReportedException(crashreport);
         }
      }
   }

   public void placeItemBackInInventory(ItemStack itemstack) {
      this.placeItemBackInInventory(itemstack, true);
   }

   public void placeItemBackInInventory(ItemStack itemstack, boolean flag) {
      while(true) {
         if (!itemstack.isEmpty()) {
            int i = this.getSlotWithRemainingSpace(itemstack);
            if (i == -1) {
               i = this.getFreeSlot();
            }

            if (i != -1) {
               int j = itemstack.getMaxStackSize() - this.getItem(i).getCount();
               if (this.add(i, itemstack.split(j)) && flag && this.player instanceof ServerPlayer) {
                  ((ServerPlayer)this.player).connection.send(new ClientboundContainerSetSlotPacket(-2, 0, i, this.getItem(i)));
               }
               continue;
            }

            this.player.drop(itemstack, false);
         }

         return;
      }
   }

   public ItemStack removeItem(int i, int j) {
      List<ItemStack> list = null;

      for(NonNullList<ItemStack> nonnulllist : this.compartments) {
         if (i < nonnulllist.size()) {
            list = nonnulllist;
            break;
         }

         i -= nonnulllist.size();
      }

      return list != null && !list.get(i).isEmpty() ? ContainerHelper.removeItem(list, i, j) : ItemStack.EMPTY;
   }

   public void removeItem(ItemStack itemstack) {
      for(NonNullList<ItemStack> nonnulllist : this.compartments) {
         for(int i = 0; i < nonnulllist.size(); ++i) {
            if (nonnulllist.get(i) == itemstack) {
               nonnulllist.set(i, ItemStack.EMPTY);
               break;
            }
         }
      }

   }

   public ItemStack removeItemNoUpdate(int i) {
      NonNullList<ItemStack> nonnulllist = null;

      for(NonNullList<ItemStack> nonnulllist1 : this.compartments) {
         if (i < nonnulllist1.size()) {
            nonnulllist = nonnulllist1;
            break;
         }

         i -= nonnulllist1.size();
      }

      if (nonnulllist != null && !nonnulllist.get(i).isEmpty()) {
         ItemStack itemstack = nonnulllist.get(i);
         nonnulllist.set(i, ItemStack.EMPTY);
         return itemstack;
      } else {
         return ItemStack.EMPTY;
      }
   }

   public void setItem(int i, ItemStack itemstack) {
      NonNullList<ItemStack> nonnulllist = null;

      for(NonNullList<ItemStack> nonnulllist1 : this.compartments) {
         if (i < nonnulllist1.size()) {
            nonnulllist = nonnulllist1;
            break;
         }

         i -= nonnulllist1.size();
      }

      if (nonnulllist != null) {
         nonnulllist.set(i, itemstack);
      }

   }

   public float getDestroySpeed(BlockState blockstate) {
      return this.items.get(this.selected).getDestroySpeed(blockstate);
   }

   public ListTag save(ListTag listtag) {
      for(int i = 0; i < this.items.size(); ++i) {
         if (!this.items.get(i).isEmpty()) {
            CompoundTag compoundtag = new CompoundTag();
            compoundtag.putByte("Slot", (byte)i);
            this.items.get(i).save(compoundtag);
            listtag.add(compoundtag);
         }
      }

      for(int j = 0; j < this.armor.size(); ++j) {
         if (!this.armor.get(j).isEmpty()) {
            CompoundTag compoundtag1 = new CompoundTag();
            compoundtag1.putByte("Slot", (byte)(j + 100));
            this.armor.get(j).save(compoundtag1);
            listtag.add(compoundtag1);
         }
      }

      for(int k = 0; k < this.offhand.size(); ++k) {
         if (!this.offhand.get(k).isEmpty()) {
            CompoundTag compoundtag2 = new CompoundTag();
            compoundtag2.putByte("Slot", (byte)(k + 150));
            this.offhand.get(k).save(compoundtag2);
            listtag.add(compoundtag2);
         }
      }

      return listtag;
   }

   public void load(ListTag listtag) {
      this.items.clear();
      this.armor.clear();
      this.offhand.clear();

      for(int i = 0; i < listtag.size(); ++i) {
         CompoundTag compoundtag = listtag.getCompound(i);
         int j = compoundtag.getByte("Slot") & 255;
         ItemStack itemstack = ItemStack.of(compoundtag);
         if (!itemstack.isEmpty()) {
            if (j >= 0 && j < this.items.size()) {
               this.items.set(j, itemstack);
            } else if (j >= 100 && j < this.armor.size() + 100) {
               this.armor.set(j - 100, itemstack);
            } else if (j >= 150 && j < this.offhand.size() + 150) {
               this.offhand.set(j - 150, itemstack);
            }
         }
      }

   }

   public int getContainerSize() {
      return this.items.size() + this.armor.size() + this.offhand.size();
   }

   public boolean isEmpty() {
      for(ItemStack itemstack : this.items) {
         if (!itemstack.isEmpty()) {
            return false;
         }
      }

      for(ItemStack itemstack1 : this.armor) {
         if (!itemstack1.isEmpty()) {
            return false;
         }
      }

      for(ItemStack itemstack2 : this.offhand) {
         if (!itemstack2.isEmpty()) {
            return false;
         }
      }

      return true;
   }

   public ItemStack getItem(int i) {
      List<ItemStack> list = null;

      for(NonNullList<ItemStack> nonnulllist : this.compartments) {
         if (i < nonnulllist.size()) {
            list = nonnulllist;
            break;
         }

         i -= nonnulllist.size();
      }

      return list == null ? ItemStack.EMPTY : list.get(i);
   }

   public Component getName() {
      return Component.translatable("container.inventory");
   }

   public ItemStack getArmor(int i) {
      return this.armor.get(i);
   }

   public void hurtArmor(DamageSource damagesource, float f, int[] aint) {
      if (!(f <= 0.0F)) {
         f /= 4.0F;
         if (f < 1.0F) {
            f = 1.0F;
         }

         for(int i : aint) {
            ItemStack itemstack = this.armor.get(i);
            if ((!damagesource.is(DamageTypeTags.IS_FIRE) || !itemstack.getItem().isFireResistant()) && itemstack.getItem() instanceof ArmorItem) {
               itemstack.hurtAndBreak((int)f, this.player, (player) -> player.broadcastBreakEvent(EquipmentSlot.byTypeAndIndex(EquipmentSlot.Type.ARMOR, i)));
            }
         }

      }
   }

   public void dropAll() {
      for(List<ItemStack> list : this.compartments) {
         for(int i = 0; i < list.size(); ++i) {
            ItemStack itemstack = list.get(i);
            if (!itemstack.isEmpty()) {
               this.player.drop(itemstack, true, false);
               list.set(i, ItemStack.EMPTY);
            }
         }
      }

   }

   public void setChanged() {
      ++this.timesChanged;
   }

   public int getTimesChanged() {
      return this.timesChanged;
   }

   public boolean stillValid(Player player) {
      if (this.player.isRemoved()) {
         return false;
      } else {
         return !(player.distanceToSqr(this.player) > 64.0D);
      }
   }

   public boolean contains(ItemStack itemstack) {
      for(List<ItemStack> list : this.compartments) {
         for(ItemStack itemstack1 : list) {
            if (!itemstack1.isEmpty() && ItemStack.isSameItemSameTags(itemstack1, itemstack)) {
               return true;
            }
         }
      }

      return false;
   }

   public boolean contains(TagKey<Item> tagkey) {
      for(List<ItemStack> list : this.compartments) {
         for(ItemStack itemstack : list) {
            if (!itemstack.isEmpty() && itemstack.is(tagkey)) {
               return true;
            }
         }
      }

      return false;
   }

   public void replaceWith(Inventory inventory) {
      for(int i = 0; i < this.getContainerSize(); ++i) {
         this.setItem(i, inventory.getItem(i));
      }

      this.selected = inventory.selected;
   }

   public void clearContent() {
      for(List<ItemStack> list : this.compartments) {
         list.clear();
      }

   }

   public void fillStackedContents(StackedContents stackedcontents) {
      for(ItemStack itemstack : this.items) {
         stackedcontents.accountSimpleStack(itemstack);
      }

   }

   public ItemStack removeFromSelected(boolean flag) {
      ItemStack itemstack = this.getSelected();
      return itemstack.isEmpty() ? ItemStack.EMPTY : this.removeItem(this.selected, flag ? itemstack.getCount() : 1);
   }
}
