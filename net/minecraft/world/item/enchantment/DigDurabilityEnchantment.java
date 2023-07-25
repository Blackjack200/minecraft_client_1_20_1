package net.minecraft.world.item.enchantment;

import net.minecraft.util.RandomSource;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.item.ArmorItem;
import net.minecraft.world.item.ItemStack;

public class DigDurabilityEnchantment extends Enchantment {
   protected DigDurabilityEnchantment(Enchantment.Rarity enchantment_rarity, EquipmentSlot... aequipmentslot) {
      super(enchantment_rarity, EnchantmentCategory.BREAKABLE, aequipmentslot);
   }

   public int getMinCost(int i) {
      return 5 + (i - 1) * 8;
   }

   public int getMaxCost(int i) {
      return super.getMinCost(i) + 50;
   }

   public int getMaxLevel() {
      return 3;
   }

   public boolean canEnchant(ItemStack itemstack) {
      return itemstack.isDamageableItem() ? true : super.canEnchant(itemstack);
   }

   public static boolean shouldIgnoreDurabilityDrop(ItemStack itemstack, int i, RandomSource randomsource) {
      if (itemstack.getItem() instanceof ArmorItem && randomsource.nextFloat() < 0.6F) {
         return false;
      } else {
         return randomsource.nextInt(i + 1) > 0;
      }
   }
}
