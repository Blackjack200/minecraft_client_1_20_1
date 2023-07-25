package net.minecraft.world.item.enchantment;

import net.minecraft.world.entity.EquipmentSlot;

public class FireAspectEnchantment extends Enchantment {
   protected FireAspectEnchantment(Enchantment.Rarity enchantment_rarity, EquipmentSlot... aequipmentslot) {
      super(enchantment_rarity, EnchantmentCategory.WEAPON, aequipmentslot);
   }

   public int getMinCost(int i) {
      return 10 + 20 * (i - 1);
   }

   public int getMaxCost(int i) {
      return super.getMinCost(i) + 50;
   }

   public int getMaxLevel() {
      return 2;
   }
}
