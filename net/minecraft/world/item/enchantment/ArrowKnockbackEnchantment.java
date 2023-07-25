package net.minecraft.world.item.enchantment;

import net.minecraft.world.entity.EquipmentSlot;

public class ArrowKnockbackEnchantment extends Enchantment {
   public ArrowKnockbackEnchantment(Enchantment.Rarity enchantment_rarity, EquipmentSlot... aequipmentslot) {
      super(enchantment_rarity, EnchantmentCategory.BOW, aequipmentslot);
   }

   public int getMinCost(int i) {
      return 12 + (i - 1) * 20;
   }

   public int getMaxCost(int i) {
      return this.getMinCost(i) + 25;
   }

   public int getMaxLevel() {
      return 2;
   }
}
