package net.minecraft.world.inventory;

import java.util.Map;
import java.util.stream.Collectors;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.Container;
import net.minecraft.world.SimpleContainer;
import net.minecraft.world.entity.ExperienceOrb;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.enchantment.Enchantment;
import net.minecraft.world.item.enchantment.EnchantmentHelper;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.phys.Vec3;

public class GrindstoneMenu extends AbstractContainerMenu {
   public static final int MAX_NAME_LENGTH = 35;
   public static final int INPUT_SLOT = 0;
   public static final int ADDITIONAL_SLOT = 1;
   public static final int RESULT_SLOT = 2;
   private static final int INV_SLOT_START = 3;
   private static final int INV_SLOT_END = 30;
   private static final int USE_ROW_SLOT_START = 30;
   private static final int USE_ROW_SLOT_END = 39;
   private final Container resultSlots = new ResultContainer();
   final Container repairSlots = new SimpleContainer(2) {
      public void setChanged() {
         super.setChanged();
         GrindstoneMenu.this.slotsChanged(this);
      }
   };
   private final ContainerLevelAccess access;

   public GrindstoneMenu(int i, Inventory inventory) {
      this(i, inventory, ContainerLevelAccess.NULL);
   }

   public GrindstoneMenu(int i, Inventory inventory, final ContainerLevelAccess containerlevelaccess) {
      super(MenuType.GRINDSTONE, i);
      this.access = containerlevelaccess;
      this.addSlot(new Slot(this.repairSlots, 0, 49, 19) {
         public boolean mayPlace(ItemStack itemstack) {
            return itemstack.isDamageableItem() || itemstack.is(Items.ENCHANTED_BOOK) || itemstack.isEnchanted();
         }
      });
      this.addSlot(new Slot(this.repairSlots, 1, 49, 40) {
         public boolean mayPlace(ItemStack itemstack) {
            return itemstack.isDamageableItem() || itemstack.is(Items.ENCHANTED_BOOK) || itemstack.isEnchanted();
         }
      });
      this.addSlot(new Slot(this.resultSlots, 2, 129, 34) {
         public boolean mayPlace(ItemStack itemstack) {
            return false;
         }

         public void onTake(Player player, ItemStack itemstack) {
            containerlevelaccess.execute((level, blockpos) -> {
               if (level instanceof ServerLevel) {
                  ExperienceOrb.award((ServerLevel)level, Vec3.atCenterOf(blockpos), this.getExperienceAmount(level));
               }

               level.levelEvent(1042, blockpos, 0);
            });
            GrindstoneMenu.this.repairSlots.setItem(0, ItemStack.EMPTY);
            GrindstoneMenu.this.repairSlots.setItem(1, ItemStack.EMPTY);
         }

         private int getExperienceAmount(Level level) {
            int i = 0;
            i += this.getExperienceFromItem(GrindstoneMenu.this.repairSlots.getItem(0));
            i += this.getExperienceFromItem(GrindstoneMenu.this.repairSlots.getItem(1));
            if (i > 0) {
               int j = (int)Math.ceil((double)i / 2.0D);
               return j + level.random.nextInt(j);
            } else {
               return 0;
            }
         }

         private int getExperienceFromItem(ItemStack itemstack) {
            int i = 0;
            Map<Enchantment, Integer> map = EnchantmentHelper.getEnchantments(itemstack);

            for(Map.Entry<Enchantment, Integer> map_entry : map.entrySet()) {
               Enchantment enchantment = map_entry.getKey();
               Integer integer = map_entry.getValue();
               if (!enchantment.isCurse()) {
                  i += enchantment.getMinCost(integer);
               }
            }

            return i;
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

   }

   public void slotsChanged(Container container) {
      super.slotsChanged(container);
      if (container == this.repairSlots) {
         this.createResult();
      }

   }

   private void createResult() {
      ItemStack itemstack = this.repairSlots.getItem(0);
      ItemStack itemstack1 = this.repairSlots.getItem(1);
      boolean flag = !itemstack.isEmpty() || !itemstack1.isEmpty();
      boolean flag1 = !itemstack.isEmpty() && !itemstack1.isEmpty();
      if (!flag) {
         this.resultSlots.setItem(0, ItemStack.EMPTY);
      } else {
         boolean flag2 = !itemstack.isEmpty() && !itemstack.is(Items.ENCHANTED_BOOK) && !itemstack.isEnchanted() || !itemstack1.isEmpty() && !itemstack1.is(Items.ENCHANTED_BOOK) && !itemstack1.isEnchanted();
         if (itemstack.getCount() > 1 || itemstack1.getCount() > 1 || !flag1 && flag2) {
            this.resultSlots.setItem(0, ItemStack.EMPTY);
            this.broadcastChanges();
            return;
         }

         int i = 1;
         int i1;
         ItemStack itemstack2;
         if (flag1) {
            if (!itemstack.is(itemstack1.getItem())) {
               this.resultSlots.setItem(0, ItemStack.EMPTY);
               this.broadcastChanges();
               return;
            }

            Item item = itemstack.getItem();
            int j = item.getMaxDamage() - itemstack.getDamageValue();
            int k = item.getMaxDamage() - itemstack1.getDamageValue();
            int l = j + k + item.getMaxDamage() * 5 / 100;
            i1 = Math.max(item.getMaxDamage() - l, 0);
            itemstack2 = this.mergeEnchants(itemstack, itemstack1);
            if (!itemstack2.isDamageableItem()) {
               if (!ItemStack.matches(itemstack, itemstack1)) {
                  this.resultSlots.setItem(0, ItemStack.EMPTY);
                  this.broadcastChanges();
                  return;
               }

               i = 2;
            }
         } else {
            boolean flag3 = !itemstack.isEmpty();
            i1 = flag3 ? itemstack.getDamageValue() : itemstack1.getDamageValue();
            itemstack2 = flag3 ? itemstack : itemstack1;
         }

         this.resultSlots.setItem(0, this.removeNonCurses(itemstack2, i1, i));
      }

      this.broadcastChanges();
   }

   private ItemStack mergeEnchants(ItemStack itemstack, ItemStack itemstack1) {
      ItemStack itemstack2 = itemstack.copy();
      Map<Enchantment, Integer> map = EnchantmentHelper.getEnchantments(itemstack1);

      for(Map.Entry<Enchantment, Integer> map_entry : map.entrySet()) {
         Enchantment enchantment = map_entry.getKey();
         if (!enchantment.isCurse() || EnchantmentHelper.getItemEnchantmentLevel(enchantment, itemstack2) == 0) {
            itemstack2.enchant(enchantment, map_entry.getValue());
         }
      }

      return itemstack2;
   }

   private ItemStack removeNonCurses(ItemStack itemstack, int i, int j) {
      ItemStack itemstack1 = itemstack.copyWithCount(j);
      itemstack1.removeTagKey("Enchantments");
      itemstack1.removeTagKey("StoredEnchantments");
      if (i > 0) {
         itemstack1.setDamageValue(i);
      } else {
         itemstack1.removeTagKey("Damage");
      }

      Map<Enchantment, Integer> map = EnchantmentHelper.getEnchantments(itemstack).entrySet().stream().filter((map_entry) -> map_entry.getKey().isCurse()).collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue));
      EnchantmentHelper.setEnchantments(map, itemstack1);
      itemstack1.setRepairCost(0);
      if (itemstack1.is(Items.ENCHANTED_BOOK) && map.size() == 0) {
         itemstack1 = new ItemStack(Items.BOOK);
         if (itemstack.hasCustomHoverName()) {
            itemstack1.setHoverName(itemstack.getHoverName());
         }
      }

      for(int k = 0; k < map.size(); ++k) {
         itemstack1.setRepairCost(AnvilMenu.calculateIncreasedRepairCost(itemstack1.getBaseRepairCost()));
      }

      return itemstack1;
   }

   public void removed(Player player) {
      super.removed(player);
      this.access.execute((level, blockpos) -> this.clearContainer(player, this.repairSlots));
   }

   public boolean stillValid(Player player) {
      return stillValid(this.access, player, Blocks.GRINDSTONE);
   }

   public ItemStack quickMoveStack(Player player, int i) {
      ItemStack itemstack = ItemStack.EMPTY;
      Slot slot = this.slots.get(i);
      if (slot != null && slot.hasItem()) {
         ItemStack itemstack1 = slot.getItem();
         itemstack = itemstack1.copy();
         ItemStack itemstack2 = this.repairSlots.getItem(0);
         ItemStack itemstack3 = this.repairSlots.getItem(1);
         if (i == 2) {
            if (!this.moveItemStackTo(itemstack1, 3, 39, true)) {
               return ItemStack.EMPTY;
            }

            slot.onQuickCraft(itemstack1, itemstack);
         } else if (i != 0 && i != 1) {
            if (!itemstack2.isEmpty() && !itemstack3.isEmpty()) {
               if (i >= 3 && i < 30) {
                  if (!this.moveItemStackTo(itemstack1, 30, 39, false)) {
                     return ItemStack.EMPTY;
                  }
               } else if (i >= 30 && i < 39 && !this.moveItemStackTo(itemstack1, 3, 30, false)) {
                  return ItemStack.EMPTY;
               }
            } else if (!this.moveItemStackTo(itemstack1, 0, 2, false)) {
               return ItemStack.EMPTY;
            }
         } else if (!this.moveItemStackTo(itemstack1, 3, 39, false)) {
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
}
