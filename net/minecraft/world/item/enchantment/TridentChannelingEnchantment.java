package net.minecraft.world.item.enchantment;

import net.minecraft.world.entity.EquipmentSlot;

public class TridentChannelingEnchantment extends Enchantment {
   public TridentChannelingEnchantment(Enchantment.Rarity enchantment_rarity, EquipmentSlot... aequipmentslot) {
      super(enchantment_rarity, EnchantmentCategory.TRIDENT, aequipmentslot);
   }

   public int getMinCost(int i) {
      return 25;
   }

   public int getMaxCost(int i) {
      return 50;
   }
}
