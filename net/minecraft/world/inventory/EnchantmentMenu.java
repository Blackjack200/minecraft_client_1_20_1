package net.minecraft.world.inventory;

import java.util.List;
import net.minecraft.Util;
import net.minecraft.advancements.CriteriaTriggers;
import net.minecraft.core.BlockPos;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.stats.Stats;
import net.minecraft.util.RandomSource;
import net.minecraft.world.Container;
import net.minecraft.world.SimpleContainer;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.EnchantedBookItem;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.enchantment.EnchantmentHelper;
import net.minecraft.world.item.enchantment.EnchantmentInstance;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.EnchantmentTableBlock;

public class EnchantmentMenu extends AbstractContainerMenu {
   private final Container enchantSlots = new SimpleContainer(2) {
      public void setChanged() {
         super.setChanged();
         EnchantmentMenu.this.slotsChanged(this);
      }
   };
   private final ContainerLevelAccess access;
   private final RandomSource random = RandomSource.create();
   private final DataSlot enchantmentSeed = DataSlot.standalone();
   public final int[] costs = new int[3];
   public final int[] enchantClue = new int[]{-1, -1, -1};
   public final int[] levelClue = new int[]{-1, -1, -1};

   public EnchantmentMenu(int i, Inventory inventory) {
      this(i, inventory, ContainerLevelAccess.NULL);
   }

   public EnchantmentMenu(int i, Inventory inventory, ContainerLevelAccess containerlevelaccess) {
      super(MenuType.ENCHANTMENT, i);
      this.access = containerlevelaccess;
      this.addSlot(new Slot(this.enchantSlots, 0, 15, 47) {
         public boolean mayPlace(ItemStack itemstack) {
            return true;
         }

         public int getMaxStackSize() {
            return 1;
         }
      });
      this.addSlot(new Slot(this.enchantSlots, 1, 35, 47) {
         public boolean mayPlace(ItemStack itemstack) {
            return itemstack.is(Items.LAPIS_LAZULI);
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

      this.addDataSlot(DataSlot.shared(this.costs, 0));
      this.addDataSlot(DataSlot.shared(this.costs, 1));
      this.addDataSlot(DataSlot.shared(this.costs, 2));
      this.addDataSlot(this.enchantmentSeed).set(inventory.player.getEnchantmentSeed());
      this.addDataSlot(DataSlot.shared(this.enchantClue, 0));
      this.addDataSlot(DataSlot.shared(this.enchantClue, 1));
      this.addDataSlot(DataSlot.shared(this.enchantClue, 2));
      this.addDataSlot(DataSlot.shared(this.levelClue, 0));
      this.addDataSlot(DataSlot.shared(this.levelClue, 1));
      this.addDataSlot(DataSlot.shared(this.levelClue, 2));
   }

   public void slotsChanged(Container container) {
      if (container == this.enchantSlots) {
         ItemStack itemstack = container.getItem(0);
         if (!itemstack.isEmpty() && itemstack.isEnchantable()) {
            this.access.execute((level, blockpos) -> {
               int j = 0;

               for(BlockPos blockpos1 : EnchantmentTableBlock.BOOKSHELF_OFFSETS) {
                  if (EnchantmentTableBlock.isValidBookShelf(level, blockpos, blockpos1)) {
                     ++j;
                  }
               }

               this.random.setSeed((long)this.enchantmentSeed.get());

               for(int k = 0; k < 3; ++k) {
                  this.costs[k] = EnchantmentHelper.getEnchantmentCost(this.random, k, j, itemstack);
                  this.enchantClue[k] = -1;
                  this.levelClue[k] = -1;
                  if (this.costs[k] < k + 1) {
                     this.costs[k] = 0;
                  }
               }

               for(int l = 0; l < 3; ++l) {
                  if (this.costs[l] > 0) {
                     List<EnchantmentInstance> list = this.getEnchantmentList(itemstack, l, this.costs[l]);
                     if (list != null && !list.isEmpty()) {
                        EnchantmentInstance enchantmentinstance = list.get(this.random.nextInt(list.size()));
                        this.enchantClue[l] = BuiltInRegistries.ENCHANTMENT.getId(enchantmentinstance.enchantment);
                        this.levelClue[l] = enchantmentinstance.level;
                     }
                  }
               }

               this.broadcastChanges();
            });
         } else {
            for(int i = 0; i < 3; ++i) {
               this.costs[i] = 0;
               this.enchantClue[i] = -1;
               this.levelClue[i] = -1;
            }
         }
      }

   }

   public boolean clickMenuButton(Player player, int i) {
      if (i >= 0 && i < this.costs.length) {
         ItemStack itemstack = this.enchantSlots.getItem(0);
         ItemStack itemstack1 = this.enchantSlots.getItem(1);
         int j = i + 1;
         if ((itemstack1.isEmpty() || itemstack1.getCount() < j) && !player.getAbilities().instabuild) {
            return false;
         } else if (this.costs[i] <= 0 || itemstack.isEmpty() || (player.experienceLevel < j || player.experienceLevel < this.costs[i]) && !player.getAbilities().instabuild) {
            return false;
         } else {
            this.access.execute((level, blockpos) -> {
               ItemStack itemstack4 = itemstack;
               List<EnchantmentInstance> list = this.getEnchantmentList(itemstack, i, this.costs[i]);
               if (!list.isEmpty()) {
                  player.onEnchantmentPerformed(itemstack, j);
                  boolean flag = itemstack.is(Items.BOOK);
                  if (flag) {
                     itemstack4 = new ItemStack(Items.ENCHANTED_BOOK);
                     CompoundTag compoundtag = itemstack.getTag();
                     if (compoundtag != null) {
                        itemstack4.setTag(compoundtag.copy());
                     }

                     this.enchantSlots.setItem(0, itemstack4);
                  }

                  for(int i1 = 0; i1 < list.size(); ++i1) {
                     EnchantmentInstance enchantmentinstance = list.get(i1);
                     if (flag) {
                        EnchantedBookItem.addEnchantment(itemstack4, enchantmentinstance);
                     } else {
                        itemstack4.enchant(enchantmentinstance.enchantment, enchantmentinstance.level);
                     }
                  }

                  if (!player.getAbilities().instabuild) {
                     itemstack1.shrink(j);
                     if (itemstack1.isEmpty()) {
                        this.enchantSlots.setItem(1, ItemStack.EMPTY);
                     }
                  }

                  player.awardStat(Stats.ENCHANT_ITEM);
                  if (player instanceof ServerPlayer) {
                     CriteriaTriggers.ENCHANTED_ITEM.trigger((ServerPlayer)player, itemstack4, j);
                  }

                  this.enchantSlots.setChanged();
                  this.enchantmentSeed.set(player.getEnchantmentSeed());
                  this.slotsChanged(this.enchantSlots);
                  level.playSound((Player)null, blockpos, SoundEvents.ENCHANTMENT_TABLE_USE, SoundSource.BLOCKS, 1.0F, level.random.nextFloat() * 0.1F + 0.9F);
               }

            });
            return true;
         }
      } else {
         Util.logAndPauseIfInIde(player.getName() + " pressed invalid button id: " + i);
         return false;
      }
   }

   private List<EnchantmentInstance> getEnchantmentList(ItemStack itemstack, int i, int j) {
      this.random.setSeed((long)(this.enchantmentSeed.get() + i));
      List<EnchantmentInstance> list = EnchantmentHelper.selectEnchantment(this.random, itemstack, j, false);
      if (itemstack.is(Items.BOOK) && list.size() > 1) {
         list.remove(this.random.nextInt(list.size()));
      }

      return list;
   }

   public int getGoldCount() {
      ItemStack itemstack = this.enchantSlots.getItem(1);
      return itemstack.isEmpty() ? 0 : itemstack.getCount();
   }

   public int getEnchantmentSeed() {
      return this.enchantmentSeed.get();
   }

   public void removed(Player player) {
      super.removed(player);
      this.access.execute((level, blockpos) -> this.clearContainer(player, this.enchantSlots));
   }

   public boolean stillValid(Player player) {
      return stillValid(this.access, player, Blocks.ENCHANTING_TABLE);
   }

   public ItemStack quickMoveStack(Player player, int i) {
      ItemStack itemstack = ItemStack.EMPTY;
      Slot slot = this.slots.get(i);
      if (slot != null && slot.hasItem()) {
         ItemStack itemstack1 = slot.getItem();
         itemstack = itemstack1.copy();
         if (i == 0) {
            if (!this.moveItemStackTo(itemstack1, 2, 38, true)) {
               return ItemStack.EMPTY;
            }
         } else if (i == 1) {
            if (!this.moveItemStackTo(itemstack1, 2, 38, true)) {
               return ItemStack.EMPTY;
            }
         } else if (itemstack1.is(Items.LAPIS_LAZULI)) {
            if (!this.moveItemStackTo(itemstack1, 1, 2, true)) {
               return ItemStack.EMPTY;
            }
         } else {
            if (this.slots.get(0).hasItem() || !this.slots.get(0).mayPlace(itemstack1)) {
               return ItemStack.EMPTY;
            }

            ItemStack itemstack2 = itemstack1.copyWithCount(1);
            itemstack1.shrink(1);
            this.slots.get(0).setByPlayer(itemstack2);
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
}
