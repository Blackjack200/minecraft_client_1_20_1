package net.minecraft.world.item.enchantment;

import net.minecraft.world.entity.EquipmentSlot;

public class ArrowInfiniteEnchantment extends Enchantment {
   public ArrowInfiniteEnchantment(Enchantment.Rarity enchantment_rarity, EquipmentSlot... aequipmentslot) {
      super(enchantment_rarity, EnchantmentCategory.BOW, aequipmentslot);
   }

   public int getMinCost(int i) {
      return 20;
   }

   public int getMaxCost(int i) {
      return 50;
   }

   public boolean checkCompatibility(Enchantment enchantment) {
      return enchantment instanceof MendingEnchantment ? false : super.checkCompatibility(enchantment);
   }
}
