package net.minecraft.world.item.enchantment;

import net.minecraft.world.entity.EquipmentSlot;

public class MendingEnchantment extends Enchantment {
   public MendingEnchantment(Enchantment.Rarity enchantment_rarity, EquipmentSlot... aequipmentslot) {
      super(enchantment_rarity, EnchantmentCategory.BREAKABLE, aequipmentslot);
   }

   public int getMinCost(int i) {
      return i * 25;
   }

   public int getMaxCost(int i) {
      return this.getMinCost(i) + 50;
   }

   public boolean isTreasureOnly() {
      return true;
   }
}
