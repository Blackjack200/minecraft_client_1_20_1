package net.minecraft.world.item.enchantment;

import net.minecraft.world.entity.EquipmentSlot;

public class UntouchingEnchantment extends Enchantment {
   protected UntouchingEnchantment(Enchantment.Rarity enchantment_rarity, EquipmentSlot... aequipmentslot) {
      super(enchantment_rarity, EnchantmentCategory.DIGGER, aequipmentslot);
   }

   public int getMinCost(int i) {
      return 15;
   }

   public int getMaxCost(int i) {
      return super.getMinCost(i) + 50;
   }

   public boolean checkCompatibility(Enchantment enchantment) {
      return super.checkCompatibility(enchantment) && enchantment != Enchantments.BLOCK_FORTUNE;
   }
}
