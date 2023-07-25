package net.minecraft.world.item.enchantment;

import net.minecraft.world.entity.EquipmentSlot;

public class WaterWorkerEnchantment extends Enchantment {
   public WaterWorkerEnchantment(Enchantment.Rarity enchantment_rarity, EquipmentSlot... aequipmentslot) {
      super(enchantment_rarity, EnchantmentCategory.ARMOR_HEAD, aequipmentslot);
   }

   public int getMinCost(int i) {
      return 1;
   }

   public int getMaxCost(int i) {
      return this.getMinCost(i) + 40;
   }
}
