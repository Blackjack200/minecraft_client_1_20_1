package net.minecraft.world.item.enchantment;

import net.minecraft.world.entity.EquipmentSlot;

public class OxygenEnchantment extends Enchantment {
   public OxygenEnchantment(Enchantment.Rarity enchantment_rarity, EquipmentSlot... aequipmentslot) {
      super(enchantment_rarity, EnchantmentCategory.ARMOR_HEAD, aequipmentslot);
   }

   public int getMinCost(int i) {
      return 10 * i;
   }

   public int getMaxCost(int i) {
      return this.getMinCost(i) + 30;
   }

   public int getMaxLevel() {
      return 3;
   }
}
