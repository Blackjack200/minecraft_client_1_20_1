package net.minecraft.world.item.enchantment;

import net.minecraft.world.entity.EquipmentSlot;

public class TridentRiptideEnchantment extends Enchantment {
   public TridentRiptideEnchantment(Enchantment.Rarity enchantment_rarity, EquipmentSlot... aequipmentslot) {
      super(enchantment_rarity, EnchantmentCategory.TRIDENT, aequipmentslot);
   }

   public int getMinCost(int i) {
      return 10 + i * 7;
   }

   public int getMaxCost(int i) {
      return 50;
   }

   public int getMaxLevel() {
      return 3;
   }

   public boolean checkCompatibility(Enchantment enchantment) {
      return super.checkCompatibility(enchantment) && enchantment != Enchantments.LOYALTY && enchantment != Enchantments.CHANNELING;
   }
}
