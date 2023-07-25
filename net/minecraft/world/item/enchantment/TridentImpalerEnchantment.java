package net.minecraft.world.item.enchantment;

import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.MobType;

public class TridentImpalerEnchantment extends Enchantment {
   public TridentImpalerEnchantment(Enchantment.Rarity enchantment_rarity, EquipmentSlot... aequipmentslot) {
      super(enchantment_rarity, EnchantmentCategory.TRIDENT, aequipmentslot);
   }

   public int getMinCost(int i) {
      return 1 + (i - 1) * 8;
   }

   public int getMaxCost(int i) {
      return this.getMinCost(i) + 20;
   }

   public int getMaxLevel() {
      return 5;
   }

   public float getDamageBonus(int i, MobType mobtype) {
      return mobtype == MobType.WATER ? (float)i * 2.5F : 0.0F;
   }
}
