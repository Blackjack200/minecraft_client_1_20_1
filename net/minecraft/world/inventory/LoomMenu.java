package net.minecraft.world.inventory;

import com.google.common.collect.ImmutableList;
import java.util.List;
import net.minecraft.core.Holder;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.tags.BannerPatternTags;
import net.minecraft.world.Container;
import net.minecraft.world.SimpleContainer;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.BannerItem;
import net.minecraft.world.item.BannerPatternItem;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.DyeColor;
import net.minecraft.world.item.DyeItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.entity.BannerPattern;
import net.minecraft.world.level.block.entity.BlockEntityType;

public class LoomMenu extends AbstractContainerMenu {
   private static final int PATTERN_NOT_SET = -1;
   private static final int INV_SLOT_START = 4;
   private static final int INV_SLOT_END = 31;
   private static final int USE_ROW_SLOT_START = 31;
   private static final int USE_ROW_SLOT_END = 40;
   private final ContainerLevelAccess access;
   final DataSlot selectedBannerPatternIndex = DataSlot.standalone();
   private List<Holder<BannerPattern>> selectablePatterns = List.of();
   Runnable slotUpdateListener = () -> {
   };
   final Slot bannerSlot;
   final Slot dyeSlot;
   private final Slot patternSlot;
   private final Slot resultSlot;
   long lastSoundTime;
   private final Container inputContainer = new SimpleContainer(3) {
      public void setChanged() {
         super.setChanged();
         LoomMenu.this.slotsChanged(this);
         LoomMenu.this.slotUpdateListener.run();
      }
   };
   private final Container outputContainer = new SimpleContainer(1) {
      public void setChanged() {
         super.setChanged();
         LoomMenu.this.slotUpdateListener.run();
      }
   };

   public LoomMenu(int i, Inventory inventory) {
      this(i, inventory, ContainerLevelAccess.NULL);
   }

   public LoomMenu(int i, Inventory inventory, final ContainerLevelAccess containerlevelaccess) {
      super(MenuType.LOOM, i);
      this.access = containerlevelaccess;
      this.bannerSlot = this.addSlot(new Slot(this.inputContainer, 0, 13, 26) {
         public boolean mayPlace(ItemStack itemstack) {
            return itemstack.getItem() instanceof BannerItem;
         }
      });
      this.dyeSlot = this.addSlot(new Slot(this.inputContainer, 1, 33, 26) {
         public boolean mayPlace(ItemStack itemstack) {
            return itemstack.getItem() instanceof DyeItem;
         }
      });
      this.patternSlot = this.addSlot(new Slot(this.inputContainer, 2, 23, 45) {
         public boolean mayPlace(ItemStack itemstack) {
            return itemstack.getItem() instanceof BannerPatternItem;
         }
      });
      this.resultSlot = this.addSlot(new Slot(this.outputContainer, 0, 143, 58) {
         public boolean mayPlace(ItemStack itemstack) {
            return false;
         }

         public void onTake(Player player, ItemStack itemstack) {
            LoomMenu.this.bannerSlot.remove(1);
            LoomMenu.this.dyeSlot.remove(1);
            if (!LoomMenu.this.bannerSlot.hasItem() || !LoomMenu.this.dyeSlot.hasItem()) {
               LoomMenu.this.selectedBannerPatternIndex.set(-1);
            }

            containerlevelaccess.execute((level, blockpos) -> {
               long i = level.getGameTime();
               if (LoomMenu.this.lastSoundTime != i) {
                  level.playSound((Player)null, blockpos, SoundEvents.UI_LOOM_TAKE_RESULT, SoundSource.BLOCKS, 1.0F, 1.0F);
                  LoomMenu.this.lastSoundTime = i;
               }

            });
            super.onTake(player, itemstack);
         }
      });

      for(int j = 0; j < 3; ++j) {
         for(int k = 0; k < 9; ++k) {
            this.addSlot(new Slot(inventory, k + j * 9 + 9, 8 + k * 18, 84 + j * 18));
         }
      }

      for(int l = 0; l < 9; ++l) {
         this.addSlot(new Slot(inventory, l, 8 + l * 18, 142));
      }

      this.addDataSlot(this.selectedBannerPatternIndex);
   }

   public boolean stillValid(Player player) {
      return stillValid(this.access, player, Blocks.LOOM);
   }

   public boolean clickMenuButton(Player player, int i) {
      if (i >= 0 && i < this.selectablePatterns.size()) {
         this.selectedBannerPatternIndex.set(i);
         this.setupResultSlot(this.selectablePatterns.get(i));
         return true;
      } else {
         return false;
      }
   }

   private List<Holder<BannerPattern>> getSelectablePatterns(ItemStack itemstack) {
      if (itemstack.isEmpty()) {
         return BuiltInRegistries.BANNER_PATTERN.getTag(BannerPatternTags.NO_ITEM_REQUIRED).map(ImmutableList::copyOf).orElse(ImmutableList.of());
      } else {
         Item var3 = itemstack.getItem();
         if (var3 instanceof BannerPatternItem) {
            BannerPatternItem bannerpatternitem = (BannerPatternItem)var3;
            return BuiltInRegistries.BANNER_PATTERN.getTag(bannerpatternitem.getBannerPattern()).map(ImmutableList::copyOf).orElse(ImmutableList.of());
         } else {
            return List.of();
         }
      }
   }

   private boolean isValidPatternIndex(int i) {
      return i >= 0 && i < this.selectablePatterns.size();
   }

   public void slotsChanged(Container container) {
      ItemStack itemstack = this.bannerSlot.getItem();
      ItemStack itemstack1 = this.dyeSlot.getItem();
      ItemStack itemstack2 = this.patternSlot.getItem();
      if (!itemstack.isEmpty() && !itemstack1.isEmpty()) {
         int i = this.selectedBannerPatternIndex.get();
         boolean flag = this.isValidPatternIndex(i);
         List<Holder<BannerPattern>> list = this.selectablePatterns;
         this.selectablePatterns = this.getSelectablePatterns(itemstack2);
         Holder<BannerPattern> holder;
         if (this.selectablePatterns.size() == 1) {
            this.selectedBannerPatternIndex.set(0);
            holder = this.selectablePatterns.get(0);
         } else if (!flag) {
            this.selectedBannerPatternIndex.set(-1);
            holder = null;
         } else {
            Holder<BannerPattern> holder2 = list.get(i);
            int j = this.selectablePatterns.indexOf(holder2);
            if (j != -1) {
               holder = holder2;
               this.selectedBannerPatternIndex.set(j);
            } else {
               holder = null;
               this.selectedBannerPatternIndex.set(-1);
            }
         }

         if (holder != null) {
            CompoundTag compoundtag = BlockItem.getBlockEntityData(itemstack);
            boolean flag1 = compoundtag != null && compoundtag.contains("Patterns", 9) && !itemstack.isEmpty() && compoundtag.getList("Patterns", 10).size() >= 6;
            if (flag1) {
               this.selectedBannerPatternIndex.set(-1);
               this.resultSlot.set(ItemStack.EMPTY);
            } else {
               this.setupResultSlot(holder);
            }
         } else {
            this.resultSlot.set(ItemStack.EMPTY);
         }

         this.broadcastChanges();
      } else {
         this.resultSlot.set(ItemStack.EMPTY);
         this.selectablePatterns = List.of();
         this.selectedBannerPatternIndex.set(-1);
      }
   }

   public List<Holder<BannerPattern>> getSelectablePatterns() {
      return this.selectablePatterns;
   }

   public int getSelectedBannerPatternIndex() {
      return this.selectedBannerPatternIndex.get();
   }

   public void registerUpdateListener(Runnable runnable) {
      this.slotUpdateListener = runnable;
   }

   public ItemStack quickMoveStack(Player player, int i) {
      ItemStack itemstack = ItemStack.EMPTY;
      Slot slot = this.slots.get(i);
      if (slot != null && slot.hasItem()) {
         ItemStack itemstack1 = slot.getItem();
         itemstack = itemstack1.copy();
         if (i == this.resultSlot.index) {
            if (!this.moveItemStackTo(itemstack1, 4, 40, true)) {
               return ItemStack.EMPTY;
            }

            slot.onQuickCraft(itemstack1, itemstack);
         } else if (i != this.dyeSlot.index && i != this.bannerSlot.index && i != this.patternSlot.index) {
            if (itemstack1.getItem() instanceof BannerItem) {
               if (!this.moveItemStackTo(itemstack1, this.bannerSlot.index, this.bannerSlot.index + 1, false)) {
                  return ItemStack.EMPTY;
               }
            } else if (itemstack1.getItem() instanceof DyeItem) {
               if (!this.moveItemStackTo(itemstack1, this.dyeSlot.index, this.dyeSlot.index + 1, false)) {
                  return ItemStack.EMPTY;
               }
            } else if (itemstack1.getItem() instanceof BannerPatternItem) {
               if (!this.moveItemStackTo(itemstack1, this.patternSlot.index, this.patternSlot.index + 1, false)) {
                  return ItemStack.EMPTY;
               }
            } else if (i >= 4 && i < 31) {
               if (!this.moveItemStackTo(itemstack1, 31, 40, false)) {
                  return ItemStack.EMPTY;
               }
            } else if (i >= 31 && i < 40 && !this.moveItemStackTo(itemstack1, 4, 31, false)) {
               return ItemStack.EMPTY;
            }
         } else if (!this.moveItemStackTo(itemstack1, 4, 40, false)) {
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

   public void removed(Player player) {
      super.removed(player);
      this.access.execute((level, blockpos) -> this.clearContainer(player, this.inputContainer));
   }

   private void setupResultSlot(Holder<BannerPattern> holder) {
      ItemStack itemstack = this.bannerSlot.getItem();
      ItemStack itemstack1 = this.dyeSlot.getItem();
      ItemStack itemstack2 = ItemStack.EMPTY;
      if (!itemstack.isEmpty() && !itemstack1.isEmpty()) {
         itemstack2 = itemstack.copyWithCount(1);
         DyeColor dyecolor = ((DyeItem)itemstack1.getItem()).getDyeColor();
         CompoundTag compoundtag = BlockItem.getBlockEntityData(itemstack2);
         ListTag listtag;
         if (compoundtag != null && compoundtag.contains("Patterns", 9)) {
            listtag = compoundtag.getList("Patterns", 10);
         } else {
            listtag = new ListTag();
            if (compoundtag == null) {
               compoundtag = new CompoundTag();
            }

            compoundtag.put("Patterns", listtag);
         }

         CompoundTag compoundtag1 = new CompoundTag();
         compoundtag1.putString("Pattern", holder.value().getHashname());
         compoundtag1.putInt("Color", dyecolor.getId());
         listtag.add(compoundtag1);
         BlockItem.setBlockEntityData(itemstack2, BlockEntityType.BANNER, compoundtag);
      }

      if (!ItemStack.matches(itemstack2, this.resultSlot.getItem())) {
         this.resultSlot.set(itemstack2);
      }

   }

   public Slot getBannerSlot() {
      return this.bannerSlot;
   }

   public Slot getDyeSlot() {
      return this.dyeSlot;
   }

   public Slot getPatternSlot() {
      return this.patternSlot;
   }

   public Slot getResultSlot() {
      return this.resultSlot;
   }
}
