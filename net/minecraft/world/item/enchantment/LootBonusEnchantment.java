package net.minecraft.world.item.enchantment;

import net.minecraft.world.entity.EquipmentSlot;

public class LootBonusEnchantment extends Enchantment {
   protected LootBonusEnchantment(Enchantment.Rarity enchantment_rarity, EnchantmentCategory enchantmentcategory, EquipmentSlot... aequipmentslot) {
      super(enchantment_rarity, enchantmentcategory, aequipmentslot);
   }

   public int getMinCost(int i) {
      return 15 + (i - 1) * 9;
   }

   public int getMaxCost(int i) {
      return super.getMinCost(i) + 50;
   }

   public int getMaxLevel() {
      return 3;
   }

   public boolean checkCompatibility(Enchantment enchantment) {
      return super.checkCompatibility(enchantment) && enchantment != Enchantments.SILK_TOUCH;
   }
}
