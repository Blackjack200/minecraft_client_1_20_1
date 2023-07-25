package net.minecraft.world.inventory;

import com.mojang.logging.LogUtils;
import java.util.Map;
import javax.annotation.Nullable;
import net.minecraft.SharedConstants;
import net.minecraft.Util;
import net.minecraft.network.chat.Component;
import net.minecraft.tags.BlockTags;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.EnchantedBookItem;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.enchantment.Enchantment;
import net.minecraft.world.item.enchantment.EnchantmentHelper;
import net.minecraft.world.level.block.AnvilBlock;
import net.minecraft.world.level.block.state.BlockState;
import org.slf4j.Logger;

public class AnvilMenu extends ItemCombinerMenu {
   public static final int INPUT_SLOT = 0;
   public static final int ADDITIONAL_SLOT = 1;
   public static final int RESULT_SLOT = 2;
   private static final Logger LOGGER = LogUtils.getLogger();
   private static final boolean DEBUG_COST = false;
   public static final int MAX_NAME_LENGTH = 50;
   private int repairItemCountCost;
   @Nullable
   private String itemName;
   private final DataSlot cost = DataSlot.standalone();
   private static final int COST_FAIL = 0;
   private static final int COST_BASE = 1;
   private static final int COST_ADDED_BASE = 1;
   private static final int COST_REPAIR_MATERIAL = 1;
   private static final int COST_REPAIR_SACRIFICE = 2;
   private static final int COST_INCOMPATIBLE_PENALTY = 1;
   private static final int COST_RENAME = 1;
   private static final int INPUT_SLOT_X_PLACEMENT = 27;
   private static final int ADDITIONAL_SLOT_X_PLACEMENT = 76;
   private static final int RESULT_SLOT_X_PLACEMENT = 134;
   private static final int SLOT_Y_PLACEMENT = 47;

   public AnvilMenu(int i, Inventory inventory) {
      this(i, inventory, ContainerLevelAccess.NULL);
   }

   public AnvilMenu(int i, Inventory inventory, ContainerLevelAccess containerlevelaccess) {
      super(MenuType.ANVIL, i, inventory, containerlevelaccess);
      this.addDataSlot(this.cost);
   }

   protected ItemCombinerMenuSlotDefinition createInputSlotDefinitions() {
      return ItemCombinerMenuSlotDefinition.create().withSlot(0, 27, 47, (itemstack1) -> true).withSlot(1, 76, 47, (itemstack) -> true).withResultSlot(2, 134, 47).build();
   }

   protected boolean isValidBlock(BlockState blockstate) {
      return blockstate.is(BlockTags.ANVIL);
   }

   protected boolean mayPickup(Player player, boolean flag) {
      return (player.getAbilities().instabuild || player.experienceLevel >= this.cost.get()) && this.cost.get() > 0;
   }

   protected void onTake(Player player, ItemStack itemstack) {
      if (!player.getAbilities().instabuild) {
         player.giveExperienceLevels(-this.cost.get());
      }

      this.inputSlots.setItem(0, ItemStack.EMPTY);
      if (this.repairItemCountCost > 0) {
         ItemStack itemstack1 = this.inputSlots.getItem(1);
         if (!itemstack1.isEmpty() && itemstack1.getCount() > this.repairItemCountCost) {
            itemstack1.shrink(this.repairItemCountCost);
            this.inputSlots.setItem(1, itemstack1);
         } else {
            this.inputSlots.setItem(1, ItemStack.EMPTY);
         }
      } else {
         this.inputSlots.setItem(1, ItemStack.EMPTY);
      }

      this.cost.set(0);
      this.access.execute((level, blockpos) -> {
         BlockState blockstate = level.getBlockState(blockpos);
         if (!player.getAbilities().instabuild && blockstate.is(BlockTags.ANVIL) && player.getRandom().nextFloat() < 0.12F) {
            BlockState blockstate1 = AnvilBlock.damage(blockstate);
            if (blockstate1 == null) {
               level.removeBlock(blockpos, false);
               level.levelEvent(1029, blockpos, 0);
            } else {
               level.setBlock(blockpos, blockstate1, 2);
               level.levelEvent(1030, blockpos, 0);
            }
         } else {
            level.levelEvent(1030, blockpos, 0);
         }

      });
   }

   public void createResult() {
      ItemStack itemstack = this.inputSlots.getItem(0);
      this.cost.set(1);
      int i = 0;
      int j = 0;
      int k = 0;
      if (itemstack.isEmpty()) {
         this.resultSlots.setItem(0, ItemStack.EMPTY);
         this.cost.set(0);
      } else {
         ItemStack itemstack1 = itemstack.copy();
         ItemStack itemstack2 = this.inputSlots.getItem(1);
         Map<Enchantment, Integer> map = EnchantmentHelper.getEnchantments(itemstack1);
         j += itemstack.getBaseRepairCost() + (itemstack2.isEmpty() ? 0 : itemstack2.getBaseRepairCost());
         this.repairItemCountCost = 0;
         if (!itemstack2.isEmpty()) {
            boolean flag = itemstack2.is(Items.ENCHANTED_BOOK) && !EnchantedBookItem.getEnchantments(itemstack2).isEmpty();
            if (itemstack1.isDamageableItem() && itemstack1.getItem().isValidRepairItem(itemstack, itemstack2)) {
               int l = Math.min(itemstack1.getDamageValue(), itemstack1.getMaxDamage() / 4);
               if (l <= 0) {
                  this.resultSlots.setItem(0, ItemStack.EMPTY);
                  this.cost.set(0);
                  return;
               }

               int i1;
               for(i1 = 0; l > 0 && i1 < itemstack2.getCount(); ++i1) {
                  int j1 = itemstack1.getDamageValue() - l;
                  itemstack1.setDamageValue(j1);
                  ++i;
                  l = Math.min(itemstack1.getDamageValue(), itemstack1.getMaxDamage() / 4);
               }

               this.repairItemCountCost = i1;
            } else {
               if (!flag && (!itemstack1.is(itemstack2.getItem()) || !itemstack1.isDamageableItem())) {
                  this.resultSlots.setItem(0, ItemStack.EMPTY);
                  this.cost.set(0);
                  return;
               }

               if (itemstack1.isDamageableItem() && !flag) {
                  int k1 = itemstack.getMaxDamage() - itemstack.getDamageValue();
                  int l1 = itemstack2.getMaxDamage() - itemstack2.getDamageValue();
                  int i2 = l1 + itemstack1.getMaxDamage() * 12 / 100;
                  int j2 = k1 + i2;
                  int k2 = itemstack1.getMaxDamage() - j2;
                  if (k2 < 0) {
                     k2 = 0;
                  }

                  if (k2 < itemstack1.getDamageValue()) {
                     itemstack1.setDamageValue(k2);
                     i += 2;
                  }
               }

               Map<Enchantment, Integer> map1 = EnchantmentHelper.getEnchantments(itemstack2);
               boolean flag1 = false;
               boolean flag2 = false;

               for(Enchantment enchantment : map1.keySet()) {
                  if (enchantment != null) {
                     int l2 = map.getOrDefault(enchantment, 0);
                     int i3 = map1.get(enchantment);
                     i3 = l2 == i3 ? i3 + 1 : Math.max(i3, l2);
                     boolean flag3 = enchantment.canEnchant(itemstack);
                     if (this.player.getAbilities().instabuild || itemstack.is(Items.ENCHANTED_BOOK)) {
                        flag3 = true;
                     }

                     for(Enchantment enchantment1 : map.keySet()) {
                        if (enchantment1 != enchantment && !enchantment.isCompatibleWith(enchantment1)) {
                           flag3 = false;
                           ++i;
                        }
                     }

                     if (!flag3) {
                        flag2 = true;
                     } else {
                        flag1 = true;
                        if (i3 > enchantment.getMaxLevel()) {
                           i3 = enchantment.getMaxLevel();
                        }

                        map.put(enchantment, i3);
                        int j3 = 0;
                        switch (enchantment.getRarity()) {
                           case COMMON:
                              j3 = 1;
                              break;
                           case UNCOMMON:
                              j3 = 2;
                              break;
                           case RARE:
                              j3 = 4;
                              break;
                           case VERY_RARE:
                              j3 = 8;
                        }

                        if (flag) {
                           j3 = Math.max(1, j3 / 2);
                        }

                        i += j3 * i3;
                        if (itemstack.getCount() > 1) {
                           i = 40;
                        }
                     }
                  }
               }

               if (flag2 && !flag1) {
                  this.resultSlots.setItem(0, ItemStack.EMPTY);
                  this.cost.set(0);
                  return;
               }
            }
         }

         if (this.itemName != null && !Util.isBlank(this.itemName)) {
            if (!this.itemName.equals(itemstack.getHoverName().getString())) {
               k = 1;
               i += k;
               itemstack1.setHoverName(Component.literal(this.itemName));
            }
         } else if (itemstack.hasCustomHoverName()) {
            k = 1;
            i += k;
            itemstack1.resetHoverName();
         }

         this.cost.set(j + i);
         if (i <= 0) {
            itemstack1 = ItemStack.EMPTY;
         }

         if (k == i && k > 0 && this.cost.get() >= 40) {
            this.cost.set(39);
         }

         if (this.cost.get() >= 40 && !this.player.getAbilities().instabuild) {
            itemstack1 = ItemStack.EMPTY;
         }

         if (!itemstack1.isEmpty()) {
            int k3 = itemstack1.getBaseRepairCost();
            if (!itemstack2.isEmpty() && k3 < itemstack2.getBaseRepairCost()) {
               k3 = itemstack2.getBaseRepairCost();
            }

            if (k != i || k == 0) {
               k3 = calculateIncreasedRepairCost(k3);
            }

            itemstack1.setRepairCost(k3);
            EnchantmentHelper.setEnchantments(map, itemstack1);
         }

         this.resultSlots.setItem(0, itemstack1);
         this.broadcastChanges();
      }
   }

   public static int calculateIncreasedRepairCost(int i) {
      return i * 2 + 1;
   }

   public boolean setItemName(String s) {
      String s1 = validateName(s);
      if (s1 != null && !s1.equals(this.itemName)) {
         this.itemName = s1;
         if (this.getSlot(2).hasItem()) {
            ItemStack itemstack = this.getSlot(2).getItem();
            if (Util.isBlank(s1)) {
               itemstack.resetHoverName();
            } else {
               itemstack.setHoverName(Component.literal(s1));
            }
         }

         this.createResult();
         return true;
      } else {
         return false;
      }
   }

   @Nullable
   private static String validateName(String s) {
      String s1 = SharedConstants.filterText(s);
      return s1.length() <= 50 ? s1 : null;
   }

   public int getCost() {
      return this.cost.get();
   }
}
