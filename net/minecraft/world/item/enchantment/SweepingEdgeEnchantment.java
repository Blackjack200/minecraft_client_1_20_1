package net.minecraft.world.item.enchantment;

import net.minecraft.world.entity.EquipmentSlot;

public class SweepingEdgeEnchantment extends Enchantment {
   public SweepingEdgeEnchantment(Enchantment.Rarity enchantment_rarity, EquipmentSlot... aequipmentslot) {
      super(enchantment_rarity, EnchantmentCategory.WEAPON, aequipmentslot);
   }

   public int getMinCost(int i) {
      return 5 + (i - 1) * 9;
   }

   public int getMaxCost(int i) {
      return this.getMinCost(i) + 15;
   }

   public int getMaxLevel() {
      return 3;
   }

   public static float getSweepingDamageRatio(int i) {
      return 1.0F - 1.0F / (float)(i + 1);
   }
}
